package ssin.sqlhelper;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import ssin.sqlhelper.anno.MetaField;
import ssin.sqlhelper.anno.MetaTable;

/**
 * sqlite3 数据库的操作
 * @author IcekingT420
 *
 */
public class SqlOperForSqlite extends SqlCommOperImpl{
	
	private Logger logger = SsinLoggerFactory.getLogger(SqlOperForSqlite.class);

	public SqlOperForSqlite(SsinDatabaseFactory factory) {
		super(factory);
	}
	
	@Override
	public void createDataBaseOnInit() {
		logger.debug("Sqlite createDataBaseOnInit ...");
		try {
			//1、判断数据库是否存在，sqlite可以直接判断文件是否存在的
			File dbFile = new File(getFactory().getConfigXML().getDatabase().getUrl().substring(12));
			if(!dbFile.exists()){
				//不存在，全部重新创建
				logger.debug("Database file is not exist: "+dbFile.getPath());
				logger.debug("Start to create new database");
				//开启事务
				if(!getFactory().startTransaction()){
					logger.error("createDataBaseOnInit start transaction error.");
					return;
				}
				//创建表格
				Iterator<Class<?>> iterator = getFactory().getMetaInfo().getTableMap().keySet().iterator();
				while(iterator.hasNext()){
					Class<?> tableClass = iterator.next();
					MetaTable metaTable = getFactory().getMetaInfo().getTableMap().get(tableClass);
					if(metaTable.getSqlCreate() == null){
						logger.error("MetaTable has no Create sql. Class="+tableClass.getName());
						continue;
					}
					if(!getFactory().executeUpdate(metaTable.getSqlCreate())){
						logger.error("executeUpdate error: "+metaTable.getSqlCreate());
						continue;
					}
					logger.debug(metaTable.getSqlCreate());
					//表格建立成功之后，建立索引，索引不一定有
					if(metaTable.getSqlIndex() != null){
						if(!getFactory().executeUpdate(metaTable.getSqlIndex())){
							logger.error("executeUpdate error: "+metaTable.getSqlIndex());
							continue;
						}
						logger.debug(metaTable.getSqlIndex());
					}
				}
				//创建外键等其它
				
				//提交
				if(!getFactory().commit()){
					logger.error("createDataBaseOnInit. commit transaction error.");
				}
				
			}else{
				//存在，则需要考虑修改字段等问题了
				logger.debug("Database file is exist: "+dbFile.getPath());
				logger.debug("Start to check and modify database");
				//开启事务
				if(!getFactory().startTransaction()){
					logger.error("createDataBaseOnInit start transaction error.");
					return;
				}
				//每张表格去判断，是否存在
				Iterator<Class<?>> iterator = getFactory().getMetaInfo().getTableMap().keySet().iterator();
				while(iterator.hasNext()){
					Class<?> tableClass = iterator.next();
					MetaTable metaTable = getFactory().getMetaInfo().getTableMap().get(tableClass);
					if(isTableExist(tableClass)){
						logger.debug("Table exist: "+metaTable.getTableName()+", "+metaTable.getClassName());
						//表格存在，再判断字段是否存在
						Iterator<String> iterField = metaTable.getMapField().keySet().iterator();
						//表格现有的字段
						List<String> nowTableColumns = getColumnNames(tableClass);
						while(iterField.hasNext()){
							MetaField field = metaTable.getMapField().get(iterField.next());
							if(nowTableColumns.contains(field.getColumnName())){
								//字段存在，则不用处理
								continue;
							}
							//字段不存在，则加入进去
							String sqlAdd = "ALTER TABLE "+metaTable.getTableName()+" ADD "+field.getColumnName()+" "+field.getColumnType();
							if(!getFactory().executeUpdate(sqlAdd)){
								logger.error("executeUpdate error: "+sqlAdd);
								continue;
							}
							logger.debug(sqlAdd);
							//如果有默认值，那么就设置默认值
							if(!field.getDefaultValue().equals("")){
								String sqlDft = "update "+metaTable.getTableName()+" set "+field.getColumnName()+"="+(field.isTextCnt()?"'"+field.getDefaultValue()+"'":field.getDefaultValue());
								if(!getFactory().executeUpdate(sqlDft)){
									logger.error("executeUpdate error: "+sqlDft);
									continue;
								}
							}
						}
						
						if(metaTable.getSqlIndex() != null){
							//判断默认的索引autoIndex_Tablename是否存在
							boolean dftIndexExist = false;
							//现在数据库中的索引和应该的是否一致
							boolean dftIndexSame = true;
							try {
								String sqlIndex = "select * from sqlite_master where type='index' and name='"+metaTable.getIndexName()+"'";
								ResultSet rs = getFactory().executeQuery(sqlIndex);
								if(rs.next()){
									dftIndexExist = true;
									String dbIndexSql = rs.getString("sql");
									if(dbIndexSql==null || !dbIndexSql.equals(metaTable.getSqlIndex()))
										dftIndexSame = false;
								}
								rs.close();
							} catch (Exception e1) {
								dftIndexExist = true;
								dftIndexSame = true;
							}
							
							if(dftIndexExist){
								//索引存在
								if(!dftIndexSame){
									logger.debug("Index "+metaTable.getIndexName()+" is changed.");
									//但是和原来的不一样，则删除原来的，并且添加新的
									String sqlDrop = "DROP INDEX "+metaTable.getIndexName();
									if(!getFactory().executeUpdate(sqlDrop)){
										logger.error("executeUpdate error: "+sqlDrop);
										continue;
									}
									logger.debug(sqlDrop);
									if(!getFactory().executeUpdate(metaTable.getSqlIndex())){
										logger.error("executeUpdate error: "+metaTable.getSqlIndex());
										continue;
									}
									logger.debug(metaTable.getSqlIndex());
								}
							}else{
								//索引不存在
								logger.debug("Index "+metaTable.getIndexName()+" is not exist.");
								if(!getFactory().executeUpdate(metaTable.getSqlIndex())){
									logger.error("executeUpdate error: "+metaTable.getSqlIndex());
									continue;
								}
								logger.debug(metaTable.getSqlIndex());
							}
							
						}
						
					}else{
						//表格不存在，直接建立
						logger.debug("Table not exist: "+metaTable.getTableName()+", "+metaTable.getClassName());
						if(metaTable.getSqlCreate() == null){
							logger.error("MetaTable has no Create sql. Class="+tableClass.getName());
							continue;
						}
						if(!getFactory().executeUpdate(metaTable.getSqlCreate())){
							logger.error("executeUpdate error: "+metaTable.getSqlCreate());
							continue;
						}
						logger.debug(metaTable.getSqlCreate());
						//表格建立成功之后，建立索引，索引不一定有
						if(metaTable.getSqlIndex() != null){
							if(!getFactory().executeUpdate(metaTable.getSqlIndex())){
								logger.error("executeUpdate error: "+metaTable.getSqlIndex());
								continue;
							}
							logger.debug(metaTable.getSqlIndex());
						}
					}
				}
				
				//提交
				if(!getFactory().commit()){
					logger.error("createDataBaseOnInit. commit transaction error.");
				}
			}
		} catch (Exception e) {
			logger.error("Sqlite createDataBaseOnInit error: "+e.getMessage(), e);
		}
	}
	
	@Override
	public <T> boolean isTableExist(Connection conn, Class<T> tableClass) {
		MetaTable metaTable = getFactory().getMetaInfo().getTableMap().get(tableClass);
		if(metaTable == null)
			return true;
		String sql = "select * from sqlite_master where type='table' and name='"+metaTable.getTableName()+"'";
		try {
			boolean tableExist = false;
			ResultSet rs = getFactory().executeQuery(sql,conn);
			if(rs.next()){
				tableExist = true;
			}
			rs.close();
			return tableExist;
		} catch (Exception e) {
			logger.error("isTableExist error: "+e.getMessage(), e);
			return true;
		}
	}
	
	@Override
	public <T> boolean isIndexExist(Connection conn, Class<T> tableClass) {
		MetaTable metaTable = getFactory().getMetaInfo().getTableMap().get(tableClass);
		if(metaTable == null)
			return true;
		String sql = "select * from sqlite_master where type='index' and name='"+metaTable.getIndexName()+"'";
		try {
			boolean indexExist = false;
			ResultSet rs = getFactory().executeQuery(sql,conn);
			if(rs.next()){
				indexExist = true;
			}
			rs.close();
			return indexExist;
		} catch (Exception e) {
			logger.error("isTableExist error: "+e.getMessage(), e);
			return true;
		}
	}
	
	@Override
	public <T> boolean isColumnExist(Connection conn, Class<T> tableClass, String columnName) {
		MetaTable metaTable = getFactory().getMetaInfo().getTableMap().get(tableClass);
		if(metaTable == null)
			return true;
		String sql = "PRAGMA table_info("+metaTable.getTableName()+")";
		try {
			List<Object[]> list = select(conn,sql);
			boolean fieldExist = false;
			for(Object[] objs: list){
				if(objs[1].equals(columnName)){
					fieldExist = true;
					break;
				}
			}
			list.clear();
			return fieldExist;
		} catch (Exception e) {
			logger.error("isColumnExist error: "+e.getMessage(), e);
			return true;
		}
	}
	
	@Override
	public List<String> getColumnNames(Connection conn, Class<?> tableClass) {
		List<String> list = new ArrayList<String>();
		MetaTable metaTable = getFactory().getMetaInfo().getTableMap().get(tableClass);
		if(metaTable == null)
			return list;
		String sql = "PRAGMA table_info("+metaTable.getTableName()+")";
		List<Object[]> objsList = select(conn,sql);
		if(objsList == null || objsList.isEmpty())
			return list;
		for(Object[] objs: objsList){
			list.add(objs[1].toString());
		}
		objsList.clear();
		return list;
	}
	
}

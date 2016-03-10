package ssin.sqlhelper;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import ssin.sqlhelper.anno.MetaField;
import ssin.sqlhelper.anno.MetaTable;

public class SqlOperForMysql extends SqlCommOperImpl{
	
	private Logger logger = SsinLoggerFactory.getLogger(SqlOperForMysql.class);

	public SqlOperForMysql(SsinDatabaseFactory factory) {
		super(factory);
	}
	
	/**
	 * mysql中的数据库是否存在，不存在的话，建立
	 */
	private void createDBIfNotExist(){
		try {
			String url = getFactory().getConfigXML().getDatabase().getUrl();
			String mysqlP = null;
			int mysqlPort = 0;
			//数据库的名字从url中解析出来
			String mysqlDbName = "";
			String[] tmp11 = url.split("/");
			if(tmp11 != null && tmp11.length==4){
				String[] tmp22 = tmp11[2].split(":");
				if(tmp22!=null && tmp22.length == 2){
					mysqlP = tmp22[0];
					mysqlPort = Integer.parseInt(tmp22[1]);
				}
				
				//数据库的名字，最后一项，?之前的
				int idxWh = tmp11[3].indexOf('?');
				mysqlDbName = idxWh!=-1?tmp11[3].substring(0, idxWh):tmp11[3];
			}
			logger.debug("Mysql IP: "+mysqlP);
			logger.debug("Mysql Port: "+mysqlPort);
			logger.debug("Mysql DB: "+mysqlDbName);
			if(mysqlP==null || mysqlPort==0){
				logger.error("Can not spit ip and port from url: "+url);
				return;
			}
			
			JDBC jdbc = new JDBC("com.mysql.jdbc.Driver", "jdbc:mysql://"+mysqlP+":"+mysqlPort+"/mysql", getFactory().getConfigXML().getDatabase().getUser(), getFactory().getConfigXML().getDatabase().getPass());
			jdbc.startConnection();
			//判断数据库是否存在
			String dbName = mysqlDbName;
			boolean dbExist = false;
			String sql = "show databases";
			ResultSet rs = jdbc.executeQuery(sql);
			while(rs.next()){
				if(rs.getString(1).equals(dbName)){
					dbExist = true;
					break;
				}
			}
			rs.close();
			if(dbExist){
				logger.debug("Database "+dbName+" is already exist.");
				jdbc.stopConnection();
				return;
			}
			//开始创建新的数据库
			logger.debug("Database "+dbName+" is not exist, start to create...");
			sql = "CREATE DATABASE "+dbName+";";
			jdbc.executeUpdate(sql);
			
			jdbc.stopConnection();
			
		} catch (Exception e) {
			logger.error("Mysql createDBIfNotExist error: "+e.getMessage(), e);
		}
	}
	
	@Override
	public void createDataBaseOnInit() {
		logger.debug("Mysql createDataBaseOnInit ...");
		
		//创建库，要根据mysql这个数据库来进行连接，独立的JDBC连接
		createDBIfNotExist();
		
		try {
			//开启事务
			if(!getFactory().startTransaction()){
				logger.error("createDataBaseOnInit start transaction error.");
				return;
			}
			
			
			//每张表格判断是否存在
			//保存数据库中全部表的名字
			List<String> allTableNamesList = new ArrayList<String>();
			String sql = "show tables";
			ResultSet rs = getFactory().executeQuery(sql);
			while(rs.next()){
				String tnStr = rs.getString(1);
				if(tnStr != null && !tnStr.equals(""))
					allTableNamesList.add(tnStr.toLowerCase());
			}
			rs.close();
			
			//根据配置中的类去判断
			Iterator<Class<?>> iterator = getFactory().getMetaInfo().getTableMap().keySet().iterator();
			while(iterator.hasNext()){
				Class<?> tableClass = iterator.next();
				MetaTable metaTable = getFactory().getMetaInfo().getTableMap().get(tableClass);
				if(allTableNamesList.contains(metaTable.getTableName().toLowerCase())){
					//表格存在
					logger.debug("Table exist: "+metaTable.getTableName()+", "+metaTable.getClassName());
					//保存表格中的全部字段
					List<String> allColumnsList = new ArrayList<String>();
					sql = "show columns from "+metaTable.getTableName();
					rs = getFactory().executeQuery(sql);
					while(rs.next()){
						allColumnsList.add(rs.getString(1));
					}
					rs.close();
					//表格存在，再判断字段是否存在
					Iterator<String> iterField = metaTable.getMapField().keySet().iterator();
					while(iterField.hasNext()){
						MetaField field = metaTable.getMapField().get(iterField.next());
						if(allColumnsList.contains(field.getColumnName())){
							//字段存在，则不用处理
							continue;
						}
						//字段不存在，则加入进去
						//mysql增加类型的时候，是需要增加长度的
						String curColType = field.getColumnType();
						if(curColType.equals("TINYINT")){
							curColType += "(3)";
						}else if (curColType.equals("SMALLINT")) {
							curColType += "(6)";
						}else if (curColType.equals("INTEGER")) {
							curColType += "(11)";
						}else if (curColType.equals("BIGINT")) {
							curColType += "(20)";
						}else if (curColType.equals("VARCHAR")) {
							curColType += "("+field.getColumnLength()+")";
						}
						
						String sqlAdd = "ALTER TABLE "+metaTable.getTableName()+" ADD "+field.getColumnName()+" "+curColType;
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
						try {
							String sqlIndex = "show index from "+metaTable.getTableName()+" where Key_name='"+metaTable.getIndexName()+"'";
							rs = getFactory().executeQuery(sqlIndex);
							if(rs.next()){
								dftIndexExist = true;
							}
							rs.close();
						} catch (Exception e1) {
							dftIndexExist = true;
						}
						
						if(!dftIndexExist){
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
					//表格不存在
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
			
		} catch (Exception e) {
			logger.error("Mysql createDataBaseOnInit error: "+e.getMessage(), e);
		}
	}
	
	@Override
	public <T> boolean isTableExist(Connection conn, Class<T> tableClass) {
		MetaTable metaTable = getFactory().getMetaInfo().getTableMap().get(tableClass);
		if(metaTable == null)
			return true;
		
		try {
			boolean tableExist = false;
			ResultSet rs = getFactory().executeQuery("show tables",conn);
			while(rs.next()){
				String tn = rs.getString(1);
				if(tn != null && tn.equals(metaTable.getTableName())){
					tableExist = true;
					break;
				}
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
		String sql = "show index from "+metaTable.getTableName()+" where Key_name='"+metaTable.getIndexName()+"'";
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
		String sql = "show columns from "+metaTable.getTableName()+" where Field='"+columnName+"'";
		try {
			boolean indexExist = false;
			ResultSet rs = getFactory().executeQuery(sql,conn);
			if(rs.next()){
				indexExist = true;
			}
			rs.close();
			return indexExist;
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
		String sql = "show columns from "+metaTable.getTableName();
		List<Object[]> objsList = select(conn,sql);
		if(objsList == null || objsList.isEmpty())
			return list;
		for(Object[] objs: objsList){
			list.add(objs[0].toString());
		}
		objsList.clear();
		return list;
	}

}

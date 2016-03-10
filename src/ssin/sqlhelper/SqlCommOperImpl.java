package ssin.sqlhelper;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import ssin.sqlhelper.anno.MetaField;
import ssin.sqlhelper.anno.MetaTable;

/**
 * 数据库操作的基础类
 * 
 * 大部分的方法，已经支持 sqlite和mysql的语法
 * 
 * @author IcekingT420
 *
 */
public abstract class SqlCommOperImpl implements SqlCommOper{
	
	private Logger logger = SsinLoggerFactory.getLogger(SqlCommOperImpl.class);
	
	/**
	 * 父对象
	 */
	private SsinDatabaseFactory factory;

	public SqlCommOperImpl(SsinDatabaseFactory factory) {
		this.factory = factory;
	}
	
	protected SsinDatabaseFactory getFactory() {
		return factory;
	}

	/**
	 * 判断字符str是否已关键字开始的，不论大小写
	 * @param str
	 * @return
	 */
	private boolean startWithKeyword(String str,String keyword){
		String tmpstr = str.trim();
		String starts = tmpstr.length()>keyword.length()?tmpstr.substring(0, keyword.length()):"";
		return starts.equalsIgnoreCase(keyword);
	}

	/**
	 * 将where语句组装到sql的后面去
	 * @param sql
	 * @param where
	 * @return
	 */
	private String concatSqlWhere(String sql,String whereStr){
		String str = sql;
		if(whereStr != null){
			if(startWithKeyword(whereStr,"where"))
				str += " "+whereStr;
			else
				str += " where "+whereStr;
		}
		return str;
	}
	
	private String concatSqlOrderBy(String sql,String orderByStr){
		String str = sql;
		if(orderByStr != null){
			if(startWithKeyword(orderByStr, "order by"))
				str += " "+orderByStr;
			else
				str += " order by "+orderByStr;
		}
		return str;
	}

	@Override
	public List<Object[]> select(Connection conn, String sql) {
		List<Object[]> list = new ArrayList<Object[]>();
		try {
			ResultSet rs = getFactory().executeQuery(sql,conn);
			if(rs == null)
				return list;
			ResultSetMetaData metaData = rs.getMetaData();
			int colCount = metaData.getColumnCount();
			//得到metadata中的所有查询字段名字
			String[] colNames = new String[colCount];
			for(int i=1;i<=colCount;i++){
				String colName = metaData.getColumnName(i);
				colNames[i-1] = colName;
			}
			while(rs.next()){
				Object[] objs = new Object[colNames.length];
				for(int i=0;i<objs.length;i++){
					objs[i] = rs.getObject(colNames[i]);
				}
				list.add(objs);
			}
			rs.close();
			
		} catch (Exception e) {
			logger.error("select error: "+e.getMessage(), e);
			list.clear();
		}
		return list;
	}

	@Override
	public List<Object[]> select(String sql) {
		Connection conn = null;
		return select(conn, sql);
	}

	@Override
	public PageData<Object[]> select(Connection conn, String sql, PageParam page) {
		PageData<Object[]> pageData = new PageData<Object[]>();
		//先找total
		String msql = sql.trim();
		if(!startWithKeyword(msql, "select")){
			logger.error("select error: "+sql);
			return pageData;
		}
		int idxFrom = msql.indexOf("from");
		if(idxFrom == -1){
			logger.error("select error: "+sql);
			return pageData;
		}
		msql = "select count(*) "+sql.substring(idxFrom);
		long total = 0l;
		try {
			ResultSet rs = getFactory().executeQuery(msql,conn);
			if(rs.next()){
				total = rs.getLong(1);
			}
			rs.close();
		} catch (Exception e) {
			logger.error("select error: "+e.getMessage(),e);
			total = 0l;
		}
		if(total == 0l)
			return pageData;
		
		pageData.setTotal(total);
		
		//再找内容
		if(page != null && page.isPageValid()){
			msql = sql + " limit "+page.getRows()+" offset "+page.getFirst();
		}
		
		List<Object[]> listCnt = select(conn,msql);
		pageData.setContent(listCnt);
		
		return pageData;
	}

	@Override
	public PageData<Object[]> select(String sql, PageParam page) {
		Connection conn = null;
		return select(conn,sql,page);
	}

	@Override
	public <T> List<T> select(Connection conn, Class<T> tableClass,
			String whereStr) {
		List<T> list = new ArrayList<T>();
		MetaTable metaTable = getFactory().getMetaInfo().getTableMap().get(tableClass);
		if(metaTable == null)
			return list;
		String sql = "select * from "+metaTable.getTableName();
		sql = concatSqlWhere(sql, whereStr);
		ResultSet rs = getFactory().executeQuery(sql,conn);
		if(rs == null)
			return list;
		try {
			while(rs.next()){
				T t = tableClass.newInstance();
				Iterator<String> iterator = metaTable.getMapField().keySet().iterator();
				while(iterator.hasNext()){
					MetaField field = metaTable.getMapField().get(iterator.next());
					Object obj = rs.getObject(field.getColumnName());
					if(obj == null)
						continue;
					getFactory().getMetaInfo().setFieldValueByName(t, field.getFieldName(), obj);
				}
				list.add(t);
			}
			rs.close();
		} catch (Exception e) {
			logger.error("select error: "+e.getMessage(), e);
			list.clear();
		}
		
		return list;
	}

	@Override
	public <T> List<T> select(Class<T> tableClass, String whereStr) {
		Connection conn = null;
		return select(conn,tableClass,whereStr);
	}

	@Override
	public <T> PageData<T> select(Connection conn, Class<T> tableClass,
			String whereStr, PageParam page) {
		PageData<T> pageData = new PageData<T>();
		MetaTable metaTable = getFactory().getMetaInfo().getTableMap().get(tableClass);
		if(metaTable == null)
			return pageData;
		//先找total
		String sql = "select count(*) from "+metaTable.getTableName();
		sql = concatSqlWhere(sql, whereStr);
		long total = 0l;
		try {
			ResultSet rs = getFactory().executeQuery(sql,conn);
			if(rs.next()){
				total = rs.getLong(1);
			}
			rs.close();
		} catch (Exception e) {
			logger.error("select error: "+e.getMessage(),e);
			total = 0l;
		}
		if(total == 0l)
			return pageData;
		
		pageData.setTotal(total);
		
		//再找内容
		List<T> listCnt = new ArrayList<T>();
		sql = "select * from "+metaTable.getTableName();
		sql = concatSqlWhere(sql, whereStr);
		if(page != null && page.isPageValid()){
			sql = sql + " limit "+page.getRows()+" offset "+page.getFirst();
		}
		ResultSet rs = getFactory().executeQuery(sql,conn);
		if(rs == null){
			pageData.setContent(listCnt);
			return pageData;
		}
			
		try {
			while(rs.next()){
				T t = tableClass.newInstance();
				Iterator<String> iterator = metaTable.getMapField().keySet().iterator();
				while(iterator.hasNext()){
					MetaField field = metaTable.getMapField().get(iterator.next());
					Object obj = rs.getObject(field.getColumnName());
					if(obj == null)
						continue;
					getFactory().getMetaInfo().setFieldValueByName(t, field.getFieldName(), obj);
				}
				listCnt.add(t);
			}
			rs.close();
		} catch (Exception e) {
			logger.error("select error: "+e.getMessage(), e);
			listCnt.clear();
		}
		
		pageData.setContent(listCnt);
		return pageData;
	}

	@Override
	public <T> PageData<T> select(Class<T> tableClass, String whereStr,String orderByStr,
			PageParam page) {
		Connection conn = null;
		return select(conn,tableClass,whereStr,orderByStr,page);
	}

	@Override
	public Object[] selectOne(Connection conn, String sql) {
		String msql = (sql.indexOf("limit") != -1)?sql:sql+" limit 1 offset 0";
		List<Object[]> list = select(conn,msql);
		if(list!=null && list.size()>0)
			return list.get(0);
		return null;
	}

	@Override
	public Object[] selectOne(String sql) {
		Connection conn = null;
		return selectOne(conn,sql);
	}

	@Override
	public <T> T selectOne(Connection conn, Class<T> tableClass, String whereStr) {
		MetaTable metaTable = getFactory().getMetaInfo().getTableMap().get(tableClass);
		if(metaTable == null)
			return null;
		String sql = "select * from "+metaTable.getTableName();
		sql = concatSqlWhere(sql, whereStr);
		sql += " limit 1 offset 0";
		ResultSet rs = getFactory().executeQuery(sql,conn);
		if(rs == null)
			return null;
		try {
			T t = null;
			if(rs.next()){
				t = tableClass.newInstance();
				Iterator<String> iterator = metaTable.getMapField().keySet().iterator();
				while(iterator.hasNext()){
					MetaField field = metaTable.getMapField().get(iterator.next());
					Object obj = rs.getObject(field.getColumnName());
					if(obj == null)
						continue;
					getFactory().getMetaInfo().setFieldValueByName(t, field.getFieldName(), obj);
				}
			}
			rs.close();
			return t;
		} catch (Exception e) {
			logger.error("selectOne error: "+e.getMessage(), e);
			return null;
		}
	}

	@Override
	public <T> T selectOne(Class<T> tableClass, String whereStr) {
		Connection conn = null;
		return selectOne(conn,tableClass, whereStr);
	}
	
	@Override
	public <T> PageData<T> select(Class<T> tableClass, String whereStr,
			PageParam page) {
		Connection conn = null;
		return select(conn,tableClass, whereStr, page);
	}
	
	@Override
	public <T> PageData<T> select(Connection conn, Class<T> tableClass,
			String whereStr, String orderByStr, PageParam page) {
		PageData<T> pageData = new PageData<T>();
		MetaTable metaTable = getFactory().getMetaInfo().getTableMap().get(tableClass);
		if(metaTable == null)
			return pageData;
		//先找total
		String sql = "select count(*) from "+metaTable.getTableName();
		sql = concatSqlWhere(sql, whereStr);
		long total = 0l;
		try {
			ResultSet rs = getFactory().executeQuery(sql,conn);
			if(rs.next()){
				total = rs.getLong(1);
			}
			rs.close();
		} catch (Exception e) {
			logger.error("select error: "+e.getMessage(),e);
			total = 0l;
		}
		if(total == 0l)
			return pageData;
		
		pageData.setTotal(total);
		
		//再找内容
		List<T> listCnt = new ArrayList<T>();
		sql = "select * from "+metaTable.getTableName();
		sql = concatSqlWhere(sql, whereStr);
		sql = concatSqlOrderBy(sql, orderByStr);
		if(page != null && page.isPageValid()){
			sql = sql + " limit "+page.getRows()+" offset "+page.getFirst();
		}
		ResultSet rs = getFactory().executeQuery(sql,conn);
		if(rs == null){
			pageData.setContent(listCnt);
			return pageData;
		}
			
		try {
			while(rs.next()){
				T t = tableClass.newInstance();
				Iterator<String> iterator = metaTable.getMapField().keySet().iterator();
				while(iterator.hasNext()){
					MetaField field = metaTable.getMapField().get(iterator.next());
					Object obj = rs.getObject(field.getColumnName());
					if(obj == null)
						continue;
					getFactory().getMetaInfo().setFieldValueByName(t, field.getFieldName(), obj);
				}
				listCnt.add(t);
			}
			rs.close();
		} catch (Exception e) {
			logger.error("select error: "+e.getMessage(), e);
			listCnt.clear();
		}
		
		pageData.setContent(listCnt);
		return pageData;
	}

	@Override
	public <T> long total(Connection conn, Class<T> tableClass, String whereStr) {
		long total = 0l;
		MetaTable metaTable = getFactory().getMetaInfo().getTableMap().get(tableClass);
		if(metaTable == null)
			return total;
		try {
			String sql = "select count(*) from "+metaTable.getTableName();
			if(whereStr != null){
				String ws = whereStr.trim();
				if(ws.startsWith("where") || ws.startsWith("WHERE")){
					sql += " "+ws;
				}else{
					sql += " where "+ws;
				}
			}
			ResultSet rs = getFactory().executeQuery(sql,conn);
			if(rs.next()){
				total = rs.getLong(1);
			}
			rs.close();
		} catch (Exception e) {
			logger.error("total error: "+e.getMessage(), e);
			total = 0l;
		}
		return total;
	}

	@Override
	public <T> long total(Connection conn, Class<T> tableClass) {
		return total(conn,tableClass,null);
	}

	@Override
	public <T> long total(Class<T> tableClass, String whereStr) {
		Connection conn = null;
		return total(conn,tableClass,whereStr);
	}

	@Override
	public <T> long total(Class<T> tableClass) {
		return total(tableClass,null);
	}

	@Override
	public <T> long maxPkId(Connection conn, Class<T> tableClass) {
		long max = 0l;
		MetaTable metaTable = getFactory().getMetaInfo().getTableMap().get(tableClass);
		if(metaTable == null)
			return max;
		MetaField pkField = metaTable.primaryKey();
		if(pkField == null || pkField.isTextCnt())
			return max;
		try {
			String sql = "select max("+pkField.getColumnName()+") from "+metaTable.getTableName();
			ResultSet rs = getFactory().executeQuery(sql,conn);
			if(rs.next()){
				max = rs.getLong(1);
			}
			rs.close();
		} catch (Exception e) {
			logger.error("maxPkId error: "+e.getMessage(), e);
			max = 0l;
		}
		return max;
	}

	@Override
	public <T> long maxPkId(Class<T> tableClass) {
		Connection conn = null;
		return maxPkId(conn, tableClass);
	}

	@Override
	public <T> T findByPrimaryKey(Connection conn, Class<T> tableClass,
			Object pkValue) {
		MetaTable metaTable = getFactory().getMetaInfo().getTableMap().get(tableClass);
		if(metaTable == null)
			return null;
		MetaField pkField = metaTable.primaryKey();
		if(pkField == null)
			return null;
		String sql = "select * from "+metaTable.getTableName();
		sql += " where "+pkField.getColumnName()+"="+(pkField.isTextCnt()?"'"+pkValue.toString()+"'":pkValue.toString());
		ResultSet rs = getFactory().executeQuery(sql,conn);
		if(rs == null)
			return null;
		try {
			T t = null;
			if(rs.next()){
				t = tableClass.newInstance();
				Iterator<String> iterator = metaTable.getMapField().keySet().iterator();
				while(iterator.hasNext()){
					MetaField field = metaTable.getMapField().get(iterator.next());
					Object obj = rs.getObject(field.getColumnName());
					if(obj == null)
						continue;
					getFactory().getMetaInfo().setFieldValueByName(t, field.getFieldName(), obj);
				}
			}
			rs.close();
			return t;
		} catch (Exception e) {
			logger.error("findByPrimaryKey error: "+e.getMessage(), e);
			return null;
		}
	}

	@Override
	public <T> T findByPrimaryKey(Class<T> tableClass, Object pkValue) {
		return findByPrimaryKey(null,tableClass,pkValue);
	}

	@Override
	public <T> T save(Connection conn, T po) {
		MetaTable metaTable = getFactory().getMetaInfo().getTableMap().get(po.getClass());
		if(metaTable == null)
			return null;
		//获得主键
		MetaField pkField = metaTable.primaryKey();
		if(pkField == null)
			return null;
		//判断主键是否为null，数值型的，是否>0
		Object pkValue = getFactory().getMetaInfo().getFieldValueByName(po, pkField.getFieldName());
		//默认是insert
		boolean insert = true;
		if(pkValue == null){
			insert = true;
		}else{
			insert = false;
			if(!pkField.isTextCnt()){
				//数值型，还需要判断是否>0
				try {
					long ll = Long.parseLong(pkValue.toString());
					if(ll<=0)
						insert = true;
				} catch (Exception e) {
					
				}
			}else {
				//字符型，要去查询记录是否已经存在了，不存在，还是insert
				long tctotal = total(conn, po.getClass(), "where "+pkField.getColumnName()+"='"+pkValue.toString()+"'");
				if(tctotal == 0)
					insert = true;
			}
		}
		
		
		if(insert){
			//插入，先插入，再查找新的
			String sql = getFactory().getMetaInfo().gnrSqlInsert(po);
			if(getFactory().executeUpdate(sql,conn)){
				if(pkField.isAutoIncrement()){
					//自动增的话，需要找到现在的最大值
					long maxPkId = maxPkId(conn,po.getClass());
					if(maxPkId>0)
						getFactory().getMetaInfo().setFieldValueByName(po, pkField.getFieldName(), maxPkId);
				}
				return po;
			}else{
				return null;
			}
		}else{
			//更新
			String sql = getFactory().getMetaInfo().gnrSqlUpdate(po);
			if(getFactory().executeUpdate(sql,conn)){
				return po;
			}else{
				return null;
			}
		}
	}
	
	@Override
	public <T> T save(T po) {
		return save(null, po);
	}
	
	@Override
	public <T> void insert(Connection conn, List<T> poList) {
		List<String> sqlList = getFactory().getMetaInfo().gnrSqlInserts(poList);
		if(sqlList.isEmpty())
			return;
		for(String sql: sqlList){
			getFactory().executeUpdate(sql, conn);
		}
	}
	
	@Override
	public <T> void insert(List<T> poList) {
		insert(null, poList);
	}

	@Override
	public <T> boolean delete(Connection conn, T... po) {
		if(po==null || po.length==0)
			return true;
		return getFactory().executeUpdate(getFactory().getMetaInfo().gnrSqlDelete(po),conn);
	}

	@Override
	public <T> boolean delete(T... po) {
		return delete(null, po);
	}

	@Override
	public <T> boolean delete(Connection conn, List<T> poList) {
		if(poList == null || poList.isEmpty())
			return true;
		return getFactory().executeUpdate(getFactory().getMetaInfo().gnrSqlDelete(poList),conn);
	}

	@Override
	public <T> boolean delete(List<T> poList) {
		return delete(null, poList);
	}

	@Override
	public <T> boolean isTableExist(Connection conn, Class<T> tableClass) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public <T> boolean isTableExist(Class<T> tableClass) {
		return isTableExist(null, tableClass);
	}

	@Override
	public <T> boolean isColumnExist(Connection conn, Class<T> tableClass,
			String columnName) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public <T> boolean isColumnExist(Class<T> tableClass, String columnName) {
		return isColumnExist(null, tableClass, columnName);
	}

	@Override
	public void createDataBaseOnInit() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public <T> boolean isIndexExist(Class<T> tableClass) {
		return isIndexExist(null, tableClass);
	}
	
	@Override
	public <T> boolean isIndexExist(Connection conn, Class<T> tableClass) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public long totalOrMax(Connection conn, String sql) {
		long total = 0l;
		ResultSet rs = getFactory().executeQuery(sql,conn);
		if(rs == null)
			return total;
		try {
			if(rs.next()){
				total = rs.getLong(1);
			}
			rs.close();
		} catch (Exception e) {
			logger.error("totalOrMax error: "+e.getMessage(), e);
			total = 0l;
		}
		return total;
	}
	
	@Override
	public long totalOrMax(String sql) {
		Connection conn = null;
		return totalOrMax(conn, sql);
	}
	
	@Override
	public <T> T parseRSOneline(Class<T> tableClass, ResultSet rs) {
		MetaTable metaTable = getFactory().getMetaInfo().getTableMap().get(tableClass);
		if(metaTable == null)
			return null;
		try {
			T t = tableClass.newInstance();
			Iterator<String> iterator = metaTable.getMapField().keySet().iterator();
			while(iterator.hasNext()){
				MetaField field = metaTable.getMapField().get(iterator.next());
				Object obj = rs.getObject(field.getColumnName());
				if(obj == null)
					continue;
				getFactory().getMetaInfo().setFieldValueByName(t, field.getFieldName(), obj);
			}
			return t;
		} catch (Exception e) {
			logger.error("parseRSOneline error: "+e.getMessage(), e);
			return null;
		}
	}
	
	@Override
	public List<String> getColumnNames(Class<?> tableClass) {
		return getColumnNames(null,tableClass);
	}
	
	@Override
	public List<String> getColumnNames(Connection conn, Class<?> tableClass) {
		// TODO Auto-generated method stub
		return null;
	}

}

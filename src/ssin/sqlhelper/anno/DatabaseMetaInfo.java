package ssin.sqlhelper.anno;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import ssin.sqlhelper.SqlOperForSqlite;
import ssin.sqlhelper.SsinLoggerFactory;

/**
 * 整体数据库的描述信息
 * @author IcekingT420
 *
 */
public class DatabaseMetaInfo {
	/**
	 * sqlite的列类型和java类型的对应
	 * 数组的第一个表示sqlite中的类型，后面的表示java的类型（全名）
	 */
	public static final String[][] AutoColumnMatch_Sqlite = {
		{"INTEGER",byte.class.getName(),Byte.class.getName(),short.class.getName(),Short.class.getName(),int.class.getName(),Integer.class.getName(),long.class.getName(),Long.class.getName()},
		{"TEXT",String.class.getName()},
		{"REAL",float.class.getName(),Float.class.getName(),double.class.getName(),Double.class.getName()}
	};
	
	/**
	 * mysql数据类型
	 */
	public static final String[][] AutoColumnMatch_Mysql = {
		{"TINYINT",byte.class.getName(),Byte.class.getName()},
		{"SMALLINT",short.class.getName(),Short.class.getName()},
		{"INTEGER",int.class.getName(),Integer.class.getName()},
		{"BIGINT",long.class.getName(),Long.class.getName()},
		{"FLOAT",float.class.getName(),Float.class.getName()},
		{"DOUBLE",double.class.getName(),Double.class.getName()},
		{"VARCHAR",String.class.getName()},
		{"DATETIME",Timestamp.class.getName(),Date.class.getName()}
	};
	
	private Logger logger = SsinLoggerFactory.getLogger(this.getClass());
	
	/**
	 * 需要指定数据库的类型
	 */
	private String databaseType;
	
	/**
	 * 该数据库中总共多少张表，
	 * 需要一个个的set进去
	 */
	private List<Class<?>> classList = new ArrayList<Class<?>>();
	
	/**
	 * 根据每个class得到的表格元数据信息
	 */
	private Map<Class<?>, MetaTable> tableMap = new HashMap<Class<?>, MetaTable>();
	
	
	public DatabaseMetaInfo(String databaseType) {
		this.databaseType = databaseType;
	}

	/**
	 * 添加表格
	 * @param tableClass
	 */
	public void addTableClass(Class<?> tableClass){
		//不能重复添加
		if(classList.contains(tableClass))
			return;
		//该类，必须有 @SASqlTable 的注解
		SASqlTable anno = tableClass.getAnnotation(SASqlTable.class);
		if(anno == null)
			return;
		classList.add(tableClass);
	}
	
	/**
	 * 根据列表中的class进行元数据的解析
	 */
	public void parseMetaInfo(){
		if(classList.isEmpty())
			return;
		//先得到全部的属性字段，然后再解析manytoone格式
		for(Class<?> clsOne: classList){
			parseOneForMetaField(clsOne);
		}
		
		//基本字段解析之后，再解析manytoone格式
		for(Class<?> clsOne: classList){
			parseOneForMTO(clsOne);
		}
		
		//全部解析完成之后，可以生成create语句了
		for(Class<?> clsOne: classList){
			MetaTable table = tableMap.get(clsOne);
			if(table == null)
				continue;
			table.gnrSqlCreate(databaseType);
			table.gnrSqlIndex(databaseType);
		}
	}
	
	/**
	 * 解析表格的field，即数据库字段
	 * @param clsOne
	 */
	private void parseOneForMetaField(Class<?> clsOne){
		MetaTable metaTable = new MetaTable();
		metaTable.setClassName(clsOne.getName());
		//表的注解
		SASqlTable annoTable = clsOne.getAnnotation(SASqlTable.class);
		if(annoTable.tableName().equals("className")){
			//表名使用类名字，前面自动加T
			metaTable.setTableName("T"+clsOne.getSimpleName());
		}else {
			metaTable.setTableName(annoTable.tableName());
		}
		metaTable.setTableMark(annoTable.tableMark());
		
		//解析该类的所有 @SASqlField 注解的字段
		for(Field field: clsOne.getDeclaredFields()){
			SASqlField anno = field.getAnnotation(SASqlField.class);
			if(anno == null)
				continue;
			MetaField metaField = new MetaField();
			metaField.setFieldName(field.getName());
			metaField.setFieldClass(field.getType().getName());
			metaField.setTableName(metaTable.getTableName());
			//从注解中获得信息
			
			//列名，如果是默认的，则使用属性名
			if(anno.columnName().equals("fieldName")){
				metaField.setColumnName(field.getName());
			}else {
				metaField.setColumnName(anno.columnName());
			}
			
			//列类型，如果是自动的，需要根据不同数据库进行解析
			if(anno.columnType().equals("auto")){
				//解析，暂未实现
				metaField.setColumnType(autoMatchColumnType(databaseType, metaField.getFieldClass()));
			}else{
				metaField.setColumnType(anno.columnType());
			}
			
			//根据类型来判断是否是一个文本内容，即需要加单引号
			metaField.setTextCnt(autoMatchIsTextContent(databaseType, metaField.getColumnType()));
			
			//列备注
			metaField.setColumnMark(anno.columnMark());
			metaField.setColumnLength(anno.columnLength());
			
			//主键、自动增、notnull
			metaField.setPrimaryKey(anno.primaryKey());
			metaField.setAutoIncrement(anno.autoIncrement());
			metaField.setNotNull(anno.notNull());
			
			//默认值
			metaField.setDefaultValue(anno.defaultValue());
			
			//索引
			metaField.setIndex(anno.index());
			metaField.setIndexAsc(anno.indexAsc());
			
			metaTable.getMapField().put(metaField.getFieldName(), metaField);
		}
		
		//加入map
		tableMap.put(clsOne, metaTable);
	}
	
	/**
	 * 解析单个的manytoone，此时，已经有了metatable在map中了
	 * @param clsOne
	 */
	private void parseOneForMTO(Class<?> clsOne){
		MetaTable metaTable = tableMap.get(clsOne);
		if(metaTable == null)
			return;
		
		//解析该类的所有 @SASqlManyToOne 注解的字段
		for(Field field: clsOne.getDeclaredFields()){
			SASqlManyToOne anno = field.getAnnotation(SASqlManyToOne.class);
			if(anno == null)
				continue;
			MetaManyToOne metaMTO = new MetaManyToOne();
			metaMTO.setFieldName(field.getName());
			
			//此属性的类型，必须是一个tableclass，即对方表格
			Class<?> otherClass = field.getType();
			if(!tableMap.containsKey(otherClass))
				continue;
			metaMTO.setFieldClass(otherClass.getName());
			
			metaMTO.setSelfTableName(metaTable.getTableName());
			
			//外键的字段
			if(anno.fkFieldName().equals(""))
				continue;
			metaMTO.setFkFieldName(anno.fkFieldName());
			//判断外键自动是否存在的
			if(!metaTable.getMapField().containsKey(metaMTO.getFkFieldName()))
				continue;
			//判断外键字段和对方的主键字段的类型是否一致的
			MetaField fkMetaField = metaTable.getMapField().get(metaMTO.getFkFieldName());
			metaMTO.setSelfFkName(fkMetaField.getColumnName());
			//找到对方表格的主键字段
			MetaField pkMetaField = tableMap.get(otherClass).primaryKey();
			metaMTO.setOtherTableName(pkMetaField.getTableName());
			metaMTO.setOtherPkName(pkMetaField.getColumnName());
			if(pkMetaField==null || !fkMetaField.getFieldClass().equals(pkMetaField.getFieldClass()))
				continue;
			
			metaMTO.setLazy(anno.lazy());
			
			metaTable.getMapMTO().put(metaMTO.getFieldName(), metaMTO);
		}
		
	}
	
	/**
	 * 打印调试信息
	 * @param text
	 */
	private void debug(String text){
		SsinLoggerFactory.debug(getClass(), text);
	}
	
	/**
	 * 打印出现在的全部元数据信息
	 */
	public void showMetaInfo(){
		debug("========Print Database Meta Info: Type="+databaseType+", TableSize="+classList.size()+"========");
		for(Class<?> clsOne: classList){
			MetaTable table = tableMap.get(clsOne);
			if(table == null)
				continue;
			debug("==Table->"+table.toString());
			
			//打印字段
			if(!table.getMapField().isEmpty()){
				Iterator<String> iterator = table.getMapField().keySet().iterator();
				while(iterator.hasNext()){
					MetaField field = table.getMapField().get(iterator.next());
					debug("====Field->"+field.toString());
				}
			}
			
			//打印manytoone
			if(!table.getMapMTO().isEmpty()){
				Iterator<String> iterator = table.getMapMTO().keySet().iterator();
				while(iterator.hasNext()){
					MetaManyToOne mto = table.getMapMTO().get(iterator.next());
					debug("====MTO->"+mto.toString());
				}
			}
			
			//打印sql create
			if(table.getSqlCreate()!=null){
				debug("== "+table.getSqlCreate()+" ==");
			}
			
			//打印索引语句
			if(table.getSqlIndex()!=null){
				debug("== "+table.getSqlIndex()+" ==");
			}
			
		}
	}

	public List<Class<?>> getClassList() {
		return classList;
	}

	public Map<Class<?>, MetaTable> getTableMap() {
		return tableMap;
	}
	
	/**
	 * 根据数据库类型自动匹配字段类型
	 * @param dbType
	 * @param javaFieldType
	 * @param columnLength
	 * @return
	 */
	private String autoMatchColumnType(String dbType,String javaFieldType){
		if(dbType.equals("sqlite")){
			for(int i=0;i<AutoColumnMatch_Sqlite.length;i++){
				for(int j=1;j<AutoColumnMatch_Sqlite[i].length;j++){
					if(javaFieldType.equals(AutoColumnMatch_Sqlite[i][j]))
						return AutoColumnMatch_Sqlite[i][0];
				}
			}
		}else if (dbType.equals("mysql")) {
			for(int i=0;i<AutoColumnMatch_Mysql.length;i++){
				for(int j=1;j<AutoColumnMatch_Mysql[i].length;j++){
					if(javaFieldType.equals(AutoColumnMatch_Mysql[i][j]))
						return AutoColumnMatch_Mysql[i][0];
				}
			}
		}
		return null;
	}
	
	/**
	 * 根据字段类型
	 * @param dbType
	 * @param columnType
	 * @return
	 */
	private boolean autoMatchIsTextContent(String dbType,String columnType){
		if(dbType.equals("sqlite")){
			if(columnType.toUpperCase().equals("TEXT"))
				return true;
		}else if (dbType.equals("mysql")) {
			String upper = columnType.toUpperCase();
			if(upper.equals("VARCHAR") || 
					upper.equals("CHAR") || 
					upper.equals("TEXT") || 
					upper.equals("DATE") || 
					upper.equals("DATETIME")
					)
				return true;
		}
		return false;
	}
	
	/**
	 * 批量导入时候产生insert语句
	 * @param po
	 * @return
	 */
	public <T> List<String> gnrSqlInserts(List<T> poList){
		List<String> resultList = new ArrayList<String>();
		if(poList == null || poList.isEmpty())
			return resultList;
		//必须是metatable中存在的
		MetaTable metaTable = tableMap.get(poList.get(0).getClass());
		if(metaTable == null)
			return resultList;
		
		for(T po : poList){
			String sql = "insert into "+metaTable.getTableName();
			//insert 语句中的 字段和value两部分分开，不包括括号的
			String sqlField = "";
			String sqlValue="";
			int count = 0;
			Iterator<String> iterator = metaTable.getMapField().keySet().iterator();
			while(iterator.hasNext()){
				MetaField field = metaTable.getMapField().get(iterator.next());
				if(field.isPrimaryKey() && field.isAutoIncrement())
					continue;
				//前面加过数据了，需要逗号
				if(count>0){
					sqlField += ",";
					sqlValue += ",";
				}
				sqlField += field.getColumnName();
				//属性的值
				Object fieldValue = getFieldValueByName(po,field.getFieldName());
				String fieldValueStr = "NULL";
				if(fieldValue != null){
					fieldValueStr = field.isTextCnt()?"'"+fieldValue.toString()+"'":fieldValue.toString();
				}else{
					//默认值
					fieldValueStr = field.isTextCnt()?"''":"0";
					if(!field.getDefaultValue().equals("")){
						fieldValueStr = field.isTextCnt()?"'"+field.getDefaultValue()+"'":field.getDefaultValue();
					}
				}
				sqlValue += fieldValueStr;
				count++;
			}
			sql += "("+sqlField+") values("+sqlValue+");";
			
			resultList.add(sql);
		}
		
		return resultList;
	}
	
	/**
	 * 产生sql insert语句
	 * @param po
	 * @return
	 */
	public <T> String gnrSqlInsert(T po){
		String sql = "";
		if(po == null)
			return sql;
		//必须是metatable中存在的
		MetaTable metaTable = tableMap.get(po.getClass());
		if(metaTable == null)
			return sql;
		sql = "insert into "+metaTable.getTableName();
		//insert 语句中的 字段和value两部分分开，不包括括号的
		String sqlField = "";
		String sqlValue="";
		int count = 0;
		Iterator<String> iterator = metaTable.getMapField().keySet().iterator();
		while(iterator.hasNext()){
			MetaField field = metaTable.getMapField().get(iterator.next());
			if(field.isPrimaryKey() && field.isAutoIncrement())
				continue;
			//前面加过数据了，需要逗号
			if(count>0){
				sqlField += ",";
				sqlValue += ",";
			}
			sqlField += field.getColumnName();
			//属性的值
			Object fieldValue = getFieldValueByName(po,field.getFieldName());
			String fieldValueStr = "NULL";
			if(fieldValue != null){
				fieldValueStr = field.isTextCnt()?"'"+fieldValue.toString()+"'":fieldValue.toString();
			}else{
				//默认值
				fieldValueStr = field.isTextCnt()?"''":"0";
				if(!field.getDefaultValue().equals("")){
					fieldValueStr = field.isTextCnt()?"'"+field.getDefaultValue()+"'":field.getDefaultValue();
				}
			}
			sqlValue += fieldValueStr;
			count++;
		}
		sql += "("+sqlField+") values("+sqlValue+");";
		
		return sql;
	}
	
	/**
	 * 产生sql update 语句
	 * @param po
	 * @return
	 */
	public <T> String gnrSqlUpdate(T po){
		String sql = "";
		if(po == null)
			return sql;
		//必须是metatable中存在的
		MetaTable metaTable = tableMap.get(po.getClass());
		if(metaTable == null)
			return sql;
		sql = "update "+metaTable.getTableName()+" set ";
		//update语句的部分分开来
		String sqlField = "";
		String sqlWhere = "";
		int count = 0;
		//更新操作，必须有主键
		boolean hasPrimaryKey = false;
		Iterator<String> iterator = metaTable.getMapField().keySet().iterator();
		while(iterator.hasNext()){
			MetaField field = metaTable.getMapField().get(iterator.next());
			if(field.isPrimaryKey()){
				hasPrimaryKey = true;
				//主键，生成where语句
				//属性的值
				Object fieldValue = getFieldValueByName(po,field.getFieldName());
				//主键必须有值
				if(fieldValue == null)
					return "";
				sqlWhere = "where "+field.getColumnName()+"=";
				sqlWhere += field.isTextCnt()?"'"+fieldValue.toString()+"'":fieldValue.toString();
			}else{
				//非主键
				if(count>0){
					sqlField += ",";
				}
				Object fieldValue = getFieldValueByName(po,field.getFieldName());
				String fieldValueStr = field.isTextCnt()?"''":"0";
				if(fieldValue != null){
					fieldValueStr = field.isTextCnt()?"'"+fieldValue.toString()+"'":fieldValue.toString();
				}
				sqlField += field.getColumnName()+"="+fieldValueStr;
				count++;
			}
		}
		if(!hasPrimaryKey)
			return "";
		sql += sqlField+" "+sqlWhere;
		return sql;
	}
	
	/**
	 * 产生删除语句
	 * @param po
	 * @return
	 */
	public <T> String gnrSqlDelete(T... po){
		String sql = "";
		if(po == null || po.length==0)
			return sql;
		//必须是metatable中存在的
		MetaTable metaTable = tableMap.get(po[0].getClass());
		if(metaTable == null)
			return sql;
		//删除必须要有主键
		MetaField primaryKey = metaTable.primaryKey();
		if(primaryKey == null)
			return sql;
		if(po.length == 1){
			//单个
			sql = "delete from "+metaTable.getTableName()+" where "+primaryKey.getColumnName()+"=";
			//主键的值
			Object fieldValue = getFieldValueByName(po[0],primaryKey.getFieldName());
			if(fieldValue == null)
				return "";
			sql += primaryKey.isTextCnt()?"'"+fieldValue.toString()+"'":fieldValue.toString();
		}else{
			//多个，使用in
			sql = "delete from "+metaTable.getTableName()+" where "+primaryKey.getColumnName()+" in ";
			String instr = "";
			int count = 0;
			for(T tmp: po){
				if(count>0)
					instr += ",";
				Object fieldValue = getFieldValueByName(tmp,primaryKey.getFieldName());
				if(fieldValue == null)
					continue;
				instr += primaryKey.isTextCnt()?"'"+fieldValue.toString()+"'":fieldValue.toString();
				count++;
			}
			if(instr.equals(""))
				return "";
			sql += "("+instr+")";
		}
		return sql;
	}
	
	public <T> String gnrSqlDelete(List<T> poList){
		return gnrSqlDelete(poList.toArray());
	}
	
	/**
	 * 通过反射调用getter方法
	 * @param fieldName
	 * @param o
	 * @return
	 */
	public Object getFieldValueByName(Object o,String fieldName) {
		try {
			String firstLetter = fieldName.substring(0, 1).toUpperCase();
			String getter = "get" + firstLetter + fieldName.substring(1);
			Method method = o.getClass().getMethod(getter, new Class[] {});
			Object value = method.invoke(o, new Object[] {});
			return value;
		} catch (Exception e) {
			logger.error("getFieldValueByName error: objectClass="+o.getClass().getName()+", fieldName="+fieldName+", error="+e.getMessage(), e);
			return null;
		}
	}
	
	/**
	 * 通过反射调用setter方法
	 * @param o
	 * @param fieldName
	 * @param fieldValue
	 */
	public void setFieldValueByName(Object o,String fieldName,Object fieldValue){
		try {
			Field field = o.getClass().getDeclaredField(fieldName);
			if(field == null)
				return;
			String firstLetter = fieldName.substring(0, 1).toUpperCase();
			String setter = "set" + firstLetter + fieldName.substring(1);
			Method method = o.getClass().getMethod(setter, new Class[] {field.getType()});
			method.invoke(o, new Object[] {fieldValue});
		} catch (Exception e) {
			logger.error("setFieldValueByName error: objectClass="+o.getClass().getName()+", fieldName="+fieldName+", fieldValue="+fieldValue.toString()+", error="+e.getMessage(), e);
		}
	}
}

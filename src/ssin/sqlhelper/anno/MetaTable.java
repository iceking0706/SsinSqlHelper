package ssin.sqlhelper.anno;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 表类的描述
 * @author IcekingT420
 *
 */
public class MetaTable {
	/**
	 * 类名称，全名
	 */
	private String className;
	/**
	 * 数据表名称
	 */
	private String tableName;
	/**
	 * 数据表的说明
	 */
	private String tableMark;
	
	/**
	 * 有了字段之后就可以产生create语句了
	 */
	private String sqlCreate;
	
	/**
	 * 有了字段之后，就可以产生索引语句了
	 */
	private String sqlIndex;
	
	/**
	 * 默认的一个索引的名字
	 */
	private String indexName;
	
	/**
	 * 包含的字段的描述信息
	 */
	private Map<String, MetaField> mapField = new HashMap<String, MetaField>();
	
	/**
	 * 包含的manytoone的描述信息
	 */
	private Map<String, MetaManyToOne> mapMTO = new HashMap<String, MetaManyToOne>();
	
	/**
	 * 得到主键字段，主键自动必须有
	 * @return
	 */
	public MetaField primaryKey(){
		Iterator<String> iterator = mapField.keySet().iterator();
		while(iterator.hasNext()){
			MetaField field = mapField.get(iterator.next());
			if(field.isPrimaryKey())
				return field;
		}
		return null;
	}
	
	/**
	 * 判断是否存在manytoone的属性
	 * @return
	 */
	public boolean hasMTO(){
		return !mapMTO.isEmpty();
	}
	
	/**
	 * 全部列的的名字数组，查询组装对象的时候需要
	 * @return
	 */
	public String[] getColumnNames(){
		List<String> list = new ArrayList<String>();
		Iterator<String> iterator = mapField.keySet().iterator();
		while(iterator.hasNext()){
			MetaField field = mapField.get(iterator.next());
			list.add(field.getColumnName());
		}
		String[] array = new String[list.size()];
		for(int i=0;i<array.length;i++){
			array[i] = list.get(i);
		}
		return array;
	}
	
	/**
	 * 产生索引语句，每张表格只有一个索引
	 * @param dbType
	 */
	public void gnrSqlIndex(String dbType){
		//索引名字，autoIndex_tableName
		indexName = "autoIndex_"+tableName;
		if(dbType.equals("sqlite")){
			int count = 0;
			String indexStr = "";
			Iterator<String> iterator = mapField.keySet().iterator();
			while(iterator.hasNext()){
				MetaField field = mapField.get(iterator.next());
				//主键，非索引字段，跳过
				if(field.isPrimaryKey() || !field.isIndex())
					continue;
				if(count>0)
					indexStr += ",";
				indexStr += field.getColumnName();
				count++;
			}
			if(indexStr.equals(""))
				return;
			sqlIndex = "CREATE INDEX "+indexName+" ON "+tableName+" ("+indexStr+")";
		}else if (dbType.equals("mysql")) {
			int count = 0;
			String indexStr = "";
			Iterator<String> iterator = mapField.keySet().iterator();
			while(iterator.hasNext()){
				MetaField field = mapField.get(iterator.next());
				//主键，非索引字段，跳过
				if(field.isPrimaryKey() || !field.isIndex())
					continue;
				if(count>0)
					indexStr += ",";
				indexStr += field.getColumnName();
				count++;
			}
			if(indexStr.equals(""))
				return;
			sqlIndex = "ALTER TABLE "+tableName+" ADD INDEX "+indexName+" ("+indexStr+")";
		}
	}
	
	/**
	 * 通过解析之后的内容，产生create语句
	 */
	public void gnrSqlCreate(String dbType){
		if(dbType.equals("sqlite")){
			sqlCreate = "CREATE TABLE \""+tableName+"\" (";
			Iterator<String> iterator = mapField.keySet().iterator();
			//已经加入的字段数量
			int count = 0;
			while(iterator.hasNext()){
				MetaField field = mapField.get(iterator.next());
				if(count>0)
					sqlCreate += ",";
				sqlCreate += "\""+field.getColumnName()+"\" "+field.getColumnType();
				if(field.isPrimaryKey()){
					//主键
					sqlCreate += " PRIMARY KEY";
					//如果是integer，才可以自动增
					if(field.isAutoIncrement() && field.getColumnType().equals("INTEGER"))
						sqlCreate += " AUTOINCREMENT";
					//主键一定是not null
					sqlCreate += " NOT NULL";
				}else{
					//非主键
					if(field.isNotNull())
						sqlCreate += " NOT NULL";
				}
				count++;
			}
			sqlCreate += ")";
		}else if (dbType.equals("mysql")) {
			sqlCreate = "CREATE TABLE "+tableName+" (";
			Iterator<String> iterator = mapField.keySet().iterator();
			//已经加入的字段数量
			int count = 0;
			while(iterator.hasNext()){
				MetaField field = mapField.get(iterator.next());
				if(count>0)
					sqlCreate += ",";
				//mysql的一些类型需要跟上长度的
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
				
				sqlCreate += field.getColumnName()+" "+curColType;
				if(field.isPrimaryKey()){
					//主键
					sqlCreate += " PRIMARY KEY";
					//如果是integer，才可以自动增
					if(field.isAutoIncrement() && (field.getColumnType().equals("INTEGER") || field.getColumnType().equals("BIGINT")))
						sqlCreate += " AUTO_INCREMENT";
					//主键一定是not null
					sqlCreate += " NOT NULL";
				}else{
					//非主键
					if(field.isNotNull())
						sqlCreate += " NOT NULL";
				}
				count++;
			}
			sqlCreate += ")";
		}
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getTableMark() {
		return tableMark;
	}

	public void setTableMark(String tableMark) {
		this.tableMark = tableMark;
	}

	public Map<String, MetaField> getMapField() {
		return mapField;
	}

	public Map<String, MetaManyToOne> getMapMTO() {
		return mapMTO;
	}
	
	public String getSqlCreate() {
		return sqlCreate;
	}

	public void setSqlCreate(String sqlCreate) {
		this.sqlCreate = sqlCreate;
	}

	public String getSqlIndex() {
		return sqlIndex;
	}

	public void setSqlIndex(String sqlIndex) {
		this.sqlIndex = sqlIndex;
	}

	public String getIndexName() {
		return indexName;
	}

	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}

	@Override
	public String toString() {
		String str = "className="+className;
		str += ", tableName="+tableName;
		str += ", tableMark="+tableMark;
		str += ", fieldSize="+mapField.size();
		str += ", mtoSize="+mapMTO.size();
		return str;
	}
	
}

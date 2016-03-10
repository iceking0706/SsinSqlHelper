package ssin.sqlhelper.anno;

/**
 * 字段的描述信息
 * @author IcekingT420
 *
 */
public class MetaField {
	/**
	 * java中的属性名，作为map中的唯一健
	 */
	private String fieldName;
	/**
	 * java中的属性类型，完整的类路径和名称
	 */
	private String fieldClass;
	/**
	 * 该字段所属的表格名称
	 */
	private String tableName;
	/**
	 * 数据库中的列名
	 */
	private String columnName;
	/**
	 * 数据库里面的类型
	 */
	private String columnType;
	/**
	 * 数据库中该列的描述
	 */
	private String columnMark;
	/**
	 * 字段的长度，一般varchar需要
	 */
	private int columnLength;
	/**
	 * 是否主键
	 */
	private boolean primaryKey;
	/**
	 * 主键是否自动增
	 */
	private boolean autoIncrement;
	/**
	 * 不允许为空
	 */
	private boolean notNull;
	/**
	 * 默认值
	 */
	private String defaultValue;
	
	private boolean index;
	
	private boolean indexAsc;
	
	/**
	 * 内容是否是文本，即需要在值中加上 单引号，这个要根据类型来判断的
	 */
	private boolean textCnt;
	
	public String getFieldName() {
		return fieldName;
	}
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	
	public String getFieldClass() {
		return fieldClass;
	}
	public void setFieldClass(String fieldClass) {
		this.fieldClass = fieldClass;
	}
	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	public String getColumnType() {
		return columnType;
	}
	public void setColumnType(String columnType) {
		this.columnType = columnType;
	}
	public String getColumnMark() {
		return columnMark;
	}
	public void setColumnMark(String columnMark) {
		this.columnMark = columnMark;
	}
	public boolean isPrimaryKey() {
		return primaryKey;
	}
	public void setPrimaryKey(boolean primaryKey) {
		this.primaryKey = primaryKey;
	}
	public boolean isAutoIncrement() {
		return autoIncrement;
	}
	public void setAutoIncrement(boolean autoIncrement) {
		this.autoIncrement = autoIncrement;
	}
	public boolean isNotNull() {
		return notNull;
	}
	public void setNotNull(boolean notNull) {
		this.notNull = notNull;
	}
	public String getDefaultValue() {
		return defaultValue;
	}
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	public boolean isTextCnt() {
		return textCnt;
	}
	public void setTextCnt(boolean textCnt) {
		this.textCnt = textCnt;
	}
	
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public int getColumnLength() {
		return columnLength;
	}
	public void setColumnLength(int columnLength) {
		this.columnLength = columnLength;
	}
	
	public boolean isIndex() {
		return index;
	}
	public void setIndex(boolean index) {
		this.index = index;
	}
	public boolean isIndexAsc() {
		return indexAsc;
	}
	public void setIndexAsc(boolean indexAsc) {
		this.indexAsc = indexAsc;
	}
	@Override
	public String toString() {
		String str = "fieldName="+fieldName;
		str += ", fieldClass="+fieldClass;
		str += ", tableName="+tableName;
		str += ", columnName="+columnName;
		str += ", columnType="+columnType;
		str += ", columnLength="+columnLength;
		str += ", columnMark="+columnMark;
		str += ", primaryKey="+primaryKey;
		if(primaryKey && !textCnt){
			str += ", autoIncrement="+autoIncrement;
		}
		str += ", notNull="+notNull;
		str += ", defaultValue="+defaultValue;
		str += ", textCnt="+textCnt;
		str += ", index="+index;
		if(index){
			str += ", indexAsc="+indexAsc;
		}
		return str;
	}
}

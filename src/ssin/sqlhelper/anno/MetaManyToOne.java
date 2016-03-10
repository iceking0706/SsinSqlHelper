package ssin.sqlhelper.anno;

/**
 * 对manytoone属性的描述
 * @author IcekingT420
 *
 */
public class MetaManyToOne {
	/**
	 * java中的属性名，作为map中的唯一健
	 * 一般是一个对象的名字
	 */
	private String fieldName;
	/**
	 * java中的属性类型，完整的类路径和名称
	 */
	private String fieldClass;
	/**
	 * 本类中，外键的名称
	 */
	private String fkFieldName;
	/**
	 * 是否立即加载
	 */
	private boolean lazy;
	
	/**
	 * 本张表中的外键情况
	 * selfFkName指的是数据库中的column名字
	 */
	private String selfTableName;
	private String selfFkName;
	
	/**
	 * 关联到对方表格的主键情况
	 * otherPkName指的是数据库中的column名字
	 */
	private String otherTableName;
	private String otherPkName;
	
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
	public String getFkFieldName() {
		return fkFieldName;
	}
	public void setFkFieldName(String fkFieldName) {
		this.fkFieldName = fkFieldName;
	}
	public boolean isLazy() {
		return lazy;
	}
	public void setLazy(boolean lazy) {
		this.lazy = lazy;
	}
	
	public String getSelfTableName() {
		return selfTableName;
	}
	public void setSelfTableName(String selfTableName) {
		this.selfTableName = selfTableName;
	}
	public String getSelfFkName() {
		return selfFkName;
	}
	public void setSelfFkName(String selfFkName) {
		this.selfFkName = selfFkName;
	}
	public String getOtherTableName() {
		return otherTableName;
	}
	public void setOtherTableName(String otherTableName) {
		this.otherTableName = otherTableName;
	}
	public String getOtherPkName() {
		return otherPkName;
	}
	public void setOtherPkName(String otherPkName) {
		this.otherPkName = otherPkName;
	}
	@Override
	public String toString() {
		String str = "fieldName="+fieldName;
		str += ", fieldClass="+fieldClass;
		str += ", fkFieldName="+fkFieldName;
		str += ", selfTableName="+selfTableName;
		str += ", selfFkName="+selfFkName;
		str += ", otherTableName="+otherTableName;
		str += ", otherPkName="+otherPkName;
		return str;
	}
}

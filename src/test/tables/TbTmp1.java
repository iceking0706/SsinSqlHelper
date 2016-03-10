package test.tables;

import ssin.sqlhelper.anno.SASqlField;
import ssin.sqlhelper.anno.SASqlTable;

/**
 * 临时表，测试使用
 * @author IcekingT420
 *
 */
@SASqlTable(tableName="TbTmp1")
public class TbTmp1 {
	@SASqlField(primaryKey = true, autoIncrement = true, columnMark = "主键，自动增")
	private long id;
	@SASqlField
	private String str1;
	@SASqlField
	private String str2;
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getStr1() {
		return str1;
	}
	public void setStr1(String str1) {
		this.str1 = str1;
	}
	public String getStr2() {
		return str2;
	}
	public void setStr2(String str2) {
		this.str2 = str2;
	}
}

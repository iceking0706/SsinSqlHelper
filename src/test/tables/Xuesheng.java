package test.tables;


import ssin.sqlhelper.anno.SASqlField;
import ssin.sqlhelper.anno.SASqlManyToOne;
import ssin.sqlhelper.anno.SASqlTable;

@SASqlTable(tableName="tStudent",tableMark="学生信息表")
public class Xuesheng {
	@SASqlField(primaryKey=true,autoIncrement=false,columnMark="学号",columnLength=50)
	private String uuid;
	
	@SASqlField(columnMark="姓名")
	private String name;
	
	@SASqlField(columnMark="班级表的主键",index=true)
	private long bjId;
	
	@SASqlManyToOne(fkFieldName="bjId")
	private Banji banji;
	
	@SASqlField
	private int a1;
	@SASqlField
	private float b1;
	@SASqlField
	private String c1;
	@SASqlField
	private double d1;
	@SASqlField
	private int e1;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getBjId() {
		return bjId;
	}

	public void setBjId(long bjId) {
		this.bjId = bjId;
	}

	public Banji getBanji() {
		return banji;
	}

	public void setBanji(Banji banji) {
		this.banji = banji;
	}

	protected int getA1() {
		return a1;
	}

	protected void setA1(int a1) {
		this.a1 = a1;
	}

	protected float getB1() {
		return b1;
	}

	protected void setB1(float b1) {
		this.b1 = b1;
	}

	protected String getC1() {
		return c1;
	}

	protected void setC1(String c1) {
		this.c1 = c1;
	}

	protected double getD1() {
		return d1;
	}

	protected void setD1(double d1) {
		this.d1 = d1;
	}

	public int getE1() {
		return e1;
	}

	public void setE1(int e1) {
		this.e1 = e1;
	}
}

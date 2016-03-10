package test.tables;

import ssin.sqlhelper.anno.SASqlField;
import ssin.sqlhelper.anno.SASqlTable;

@SASqlTable
public class Banji {
	@SASqlField(primaryKey=true)
	private long id;
	
	@SASqlField(columnName="bjName")
	private String name;
	
	@SASqlField(index=true)
	private double abc;
	
	@SASqlField(index=true)
	private double f1;
	
	@SASqlField(defaultValue="21")
	private int f2;
	
	@SASqlField(defaultValue="hello")
	private String f3;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getAbc() {
		return abc;
	}

	public void setAbc(double abc) {
		this.abc = abc;
	}


	public double getF1() {
		return f1;
	}

	public void setF1(double f1) {
		this.f1 = f1;
	}

	public int getF2() {
		return f2;
	}

	public void setF2(int f2) {
		this.f2 = f2;
	}

	public String getF3() {
		return f3;
	}

	public void setF3(String f3) {
		this.f3 = f3;
	}

	
}

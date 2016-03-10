package test;

import java.lang.reflect.Field;


import ssin.sqlhelper.LocalMacAddr;
import ssin.sqlhelper.anno.DatabaseMetaInfo;
import ssin.sqlhelper.anno.SASqlTable;
import test.tables.Banji;
import test.tables.Xuesheng;

public class TestAnno {
	public static void test1(){
		SASqlTable anno = Banji.class.getAnnotation(SASqlTable.class);
		//System.out.println(anno.tableName());
		Field[] fields = Banji.class.getDeclaredFields();
		for(Field field: fields){
			System.out.println(field.getName()+" , "+field.getType().getSimpleName());
		}
	}
	
	public static void test2(){
		System.out.println(short.class.getName());
		System.out.println(Short.class.getName());
	}
	
	public static void test3(Object v){
		System.out.println(v.toString());
	}
	
	public static void test4(){
		String sql = "select 1,2,3 from aa where 1=2";
		String mysql = sql.toLowerCase();
		if(mysql.indexOf("from") != -1){
			int idxStart = 6;
			int idxEnd = mysql.indexOf("from");
			mysql = "select count(*) "+mysql.substring(idxEnd);
			System.out.println(mysql);
		}
		
	}
	
	public static void main(String[] args) {
		String str = "mydb33?characterEncoding=utf8&amp;reConnect=true";
		int idxWh = str.indexOf('?');
		String dn = idxWh!=-1?str.substring(0, idxWh):str;
		System.out.println(dn);
	}
}

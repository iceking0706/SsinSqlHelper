package test;

import java.io.File;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import ssin.sqlhelper.PageData;
import ssin.sqlhelper.PageParam;
import ssin.sqlhelper.SsinDatabaseFactory;
import ssin.sqlhelper.SsinLoggerFactory;
import ssin.sqlhelper.config.ConfigXML;
import test.tables.Banji;

public class TestDatabase {
	/**
	 * 获得工程目录下的
	 * @return
	 */
	public static File getCfgXml(){
		File clsDir = new File(TestDatabase.class.getResource("/").getPath());
		File cfgXml = new File(clsDir.getParentFile(), "SsinSqlHelperCfg.xml");
		return cfgXml;
	}
	
	public static void test1(){
		ConfigXML configXML = new ConfigXML(getCfgXml());
		SsinLoggerFactory.setConfigXML(configXML);
		SsinDatabaseFactory databaseFactory = new SsinDatabaseFactory(configXML);

		
		
		databaseFactory.unInit();
		
	}
	
	public static void test2(){
		
		
		ConfigXML configXML = new ConfigXML(getCfgXml());
		SsinLoggerFactory.setConfigXML(configXML);
		SsinDatabaseFactory factory = new SsinDatabaseFactory(configXML);
		
		factory.startQuery();
		
		String sql = "select * from tbanji";
		List<Object[]> list = factory.getSqlOper().select(sql);
		if(list != null){
			System.out.println("22222222222--"+list.size());
		}
		
		factory.stopQuery();
		
		factory.unInit();
	}
	
	public static void test3(){
		ConfigXML configXML = new ConfigXML();
		SsinLoggerFactory.setConfigXML(configXML);
		SsinDatabaseFactory factory = new SsinDatabaseFactory(configXML);
		
		factory.startTransaction();
		
		Banji po = new Banji();
		po.setName("商务11班");
		factory.getSqlOper().save(po);
		
		factory.commit();
		
		factory.unInit();
	}
	
	public static void main(String[] args) {
		test2();
	}
}

package test;

import java.io.File;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

import ssin.sqlhelper.SsinLoggerFactory;
import ssin.sqlhelper.config.ConfigXML;
import ssin.util.MyClassUtil;

public class TestLog4j {
	public static void test2(){
		Logger logger = Logger.getLogger("AAA1");
		logger.removeAllAppenders();
		logger.setAdditivity(false);
		//控制台的输出
		PatternLayout layout = new PatternLayout();
		layout.setConversionPattern("%d{yyyy-MM-dd HH:mm:ss} %-5p [%c{1}] %m%n");
		logger.addAppender(new ConsoleAppender(layout, "System.out"));
		logger.setLevel(Level.DEBUG);
		
		//文件的输出，按照大小滚动文件
		try {
			PatternLayout layout2 = new PatternLayout("%d{yyyy-MM-dd HH:mm:ss} %-5p [%c{1}] %m%n");
			RollingFileAppender fileAppender = new RollingFileAppender(layout2, "E:\\临时文件\\log4jtest\\aaa1.log");
			fileAppender.setMaximumFileSize(200*1024*1024);
			fileAppender.setMaxBackupIndex(100);
			fileAppender.setAppend(true);
			logger.addAppender(fileAppender);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//安装日期滚动文件
		try {
			PatternLayout layout3 = new PatternLayout("%d{yyyy-MM-dd HH:mm:ss} %-5p [%c{1}] %m%n");
			DailyRollingFileAppender appender = new DailyRollingFileAppender(layout3, "E:\\临时文件\\log4jtest\\bbb1.log", "'_'yyyyMMdd'.log'");
			appender.setLayout(layout3);
			//appender.setFile(file)
			appender.setAppend(true);
			logger.addAppender(appender);
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		
		logger.debug("aaaaaaaaaaaaaa");
	}
	
	public static void main(String[] args) {
//		test2();
//		String str = "aaaa${user.dir}bbbb";
//		System.out.println(str.replace("${user.dir}", System.getProperty("user.dir")));
//		System.out.println(str);
//		String attri = "100M";
//		attri = attri.substring(0,attri.length()-1);
//		System.out.println(attri);
		
		
		
//		ConfigXML configXML = new ConfigXML();
//		System.out.println(configXML.getError());
//		if(configXML.isValid()){
//			System.out.println(configXML.getDatabase().getUrl());
//			System.out.println(configXML.getLog4j().getRollingMaxFileSize());
//			System.out.println(configXML.getLog4j().getSaveDir().getPath());
//		}
		
//		SsinLoggerFactory.info("hello");
//		SsinLoggerFactory.error(TestLog4j.class, "好的，我马上回来");
		
		String[] names = MyClassUtil.getClassesNames("java.util");
		for(String cls: names){
			System.out.println(cls);
		}
		
	}
}

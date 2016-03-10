package ssin.sqlhelper;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

import ssin.sqlhelper.config.ConfigXML;

/**
 * 根据配置文件得到的日志管理器
 * @author IcekingT420
 *
 */
public class SsinLoggerFactory {
	
	//私有构造函数，无法实例化
	private SsinLoggerFactory(){
		
	}
	
	/**
	 * 配置文件
	 */
	private static ConfigXML configXML;
	
	private static Map<String, Logger> map = new HashMap<String, Logger>();

	public static ConfigXML getConfigXML() {
		return configXML;
	}

	public static void setConfigXML(ConfigXML configXML) {
		SsinLoggerFactory.configXML = configXML;
	}

	/**
	 * 获得一个日志对象
	 * @param name
	 * @return
	 */
	public static Logger getLogger(String logname){
		String name = (logname!=null && !logname.equals(""))?logname:"SsinSqlHelperDefaultLogger";
		Logger logger = map.get(name);
		if(logger != null)
			return logger;
		if(configXML == null){
			//此时未指定的话，去默认位置获取
			configXML = new ConfigXML();
		}
		if(!configXML.isValid())
			return null;
		logger = Logger.getLogger(name);
		logger.removeAllAppenders();
		logger.setAdditivity(false);
		PatternLayout layout = new PatternLayout(configXML.getLog4j().getLayoutPattern());
		if(configXML.getLog4j().isConsoleAppender()){
			logger.addAppender(new ConsoleAppender(layout, "System.out"));
		}
		if(configXML.getLog4j().isRollingFileAppender()){
			try {
				File file = new File(configXML.getLog4j().getSaveDir(),configXML.getLog4j().getRollingFilepath());
				RollingFileAppender appender = new RollingFileAppender(layout, file.getPath());
				appender.setMaximumFileSize(configXML.getLog4j().getRollingMaxFileSize());
				appender.setMaxBackupIndex(configXML.getLog4j().getRollingMaxFileCount());
				appender.setAppend(true);
				logger.addAppender(appender);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if(configXML.getLog4j().isDailyRollingFileAppender()){
			try {
				File file = new File(configXML.getLog4j().getSaveDir(),configXML.getLog4j().getDailyFilepath());
				DailyRollingFileAppender appender = new DailyRollingFileAppender(layout, file.getPath(), "'_'yyyy-MM-dd'.log'");
				appender.setAppend(true);
				logger.addAppender(appender);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		logger.setLevel(configXML.getLog4j().getLevel());
		
		//放入map
		map.put(name, logger);
		return logger;
	}
	
	/**
	 * 根据类的全名来得到日志对象
	 * @param cls
	 * @return
	 */
	public static Logger getLogger(Class<?> cls){
		return getLogger(cls.getName());
	}
	
	/**
	 * 得到默认的日志，名称就是SsinSqlHelperDefaultLogger
	 * @return
	 */
	public static Logger getLogger(){
		return getLogger("");
	}
	
	/**
	 * 四种类型的日志方法
	 * @param name
	 * @param info
	 */
	public static void debug(String name,String text){
		getLogger(name).debug(text);
	}
	
	public static void debug(Class<?> cls,String text){
		getLogger(cls).debug(text);
	}
	
	public static void debug(String text){
		getLogger().debug(text);
	}
	
	public static void info(String name,String text){
		getLogger(name).info(text);
	}
	
	public static void info(Class<?> cls,String text){
		getLogger(cls).info(text);
	}
	
	public static void info(String text){
		getLogger().info(text);
	}
	
	public static void warn(String name,String text){
		getLogger(name).warn(text);
	}
	
	public static void warn(Class<?> cls,String text){
		getLogger(cls).warn(text);
	}
	
	public static void warn(String text){
		getLogger().warn(text);
	}
	
	public static void error(String name,String text){
		getLogger(name).error(text);
	}
	
	public static void error(Class<?> cls,String text){
		getLogger(cls).error(text);
	}
	
	public static void error(String text){
		getLogger().error(text);
	}
	
	public static void error(String name,String text,Throwable t){
		getLogger(name).error(text, t);
	}
	
	public static void error(Class<?> cls,String text, Throwable t){
		getLogger(cls).error(text,t);
	}
	
	public static void error(String text, Throwable t){
		getLogger().error(text,t);
	}
}

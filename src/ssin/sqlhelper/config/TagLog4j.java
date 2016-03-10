package ssin.sqlhelper.config;

import java.io.File;

import org.apache.log4j.Level;

/**
 * 针对log4j的tag解析
 * 
 * @author IcekingT420
 * 
 */
public class TagLog4j {
	private File saveDir;
	private Level level;
	private String layoutPattern;

	/**
	 * 控制台
	 */
	private boolean consoleAppender;
	
	/**
	 * 按大小滚动的日志
	 */
	private boolean rollingFileAppender;
	private String rollingFilepath;
	private long rollingMaxFileSize;
	private int rollingMaxFileCount;
	
	/**
	 * 按每天滚动的日志 日期格式_yyyy-MM-dd.log 
	 */
	private boolean dailyRollingFileAppender;
	private String dailyFilepath;
	
	public File getSaveDir() {
		return saveDir;
	}

	public void setSaveDir(File saveDir) {
		this.saveDir = saveDir;
	}

	public Level getLevel() {
		return level;
	}

	public void setLevel(Level level) {
		this.level = level;
	}

	public String getLayoutPattern() {
		return layoutPattern;
	}

	public void setLayoutPattern(String layoutPattern) {
		this.layoutPattern = layoutPattern;
	}

	public boolean isConsoleAppender() {
		return consoleAppender;
	}

	public void setConsoleAppender(boolean consoleAppender) {
		this.consoleAppender = consoleAppender;
	}

	public boolean isRollingFileAppender() {
		return rollingFileAppender;
	}

	public void setRollingFileAppender(boolean rollingFileAppender) {
		this.rollingFileAppender = rollingFileAppender;
	}

	public String getRollingFilepath() {
		return rollingFilepath;
	}

	public void setRollingFilepath(String rollingFilepath) {
		this.rollingFilepath = rollingFilepath;
	}

	public long getRollingMaxFileSize() {
		return rollingMaxFileSize;
	}

	public void setRollingMaxFileSize(long rollingMaxFileSize) {
		this.rollingMaxFileSize = rollingMaxFileSize;
	}

	public int getRollingMaxFileCount() {
		return rollingMaxFileCount;
	}

	public void setRollingMaxFileCount(int rollingMaxFileCount) {
		this.rollingMaxFileCount = rollingMaxFileCount;
	}

	public boolean isDailyRollingFileAppender() {
		return dailyRollingFileAppender;
	}

	public void setDailyRollingFileAppender(boolean dailyRollingFileAppender) {
		this.dailyRollingFileAppender = dailyRollingFileAppender;
	}

	public String getDailyFilepath() {
		return dailyFilepath;
	}

	public void setDailyFilepath(String dailyFilepath) {
		this.dailyFilepath = dailyFilepath;
	}

	
}

package ssin.sqlhelper.config;

import java.util.ArrayList;
import java.util.List;

/**
 * database的解析
 * 
 * @author IcekingT420
 * 
 */
public class TagDatabase {
	private String type;
	private String name;
	private String driver;
	private String url;
	private String user;
	private String pass;
	private boolean updateDB;
	private boolean showSql;
	private TagDbcp dbcp;
	private List<String> tableClasses = new ArrayList<String>();
	
	/**
	 * 是否采用dbcp连接池
	 * @return
	 */
	public boolean useDbcp(){
		return dbcp!=null;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDriver() {
		return driver;
	}

	public void setDriver(String driver) {
		this.driver = driver;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPass() {
		return pass;
	}

	public void setPass(String pass) {
		this.pass = pass;
	}

	public TagDbcp getDbcp() {
		return dbcp;
	}

	public void setDbcp(TagDbcp dbcp) {
		this.dbcp = dbcp;
	}

	public List<String> getTableClasses() {
		return tableClasses;
	}

	public void setTableClasses(List<String> tableClasses) {
		this.tableClasses = tableClasses;
	}

	public boolean isUpdateDB() {
		return updateDB;
	}

	public void setUpdateDB(boolean updateDB) {
		this.updateDB = updateDB;
	}

	public boolean isShowSql() {
		return showSql;
	}

	public void setShowSql(boolean showSql) {
		this.showSql = showSql;
	}
}

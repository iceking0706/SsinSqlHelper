package ssin.sqlhelper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;



/**
 * 直连数据库
 * @author IcekingT420
 *
 */
public class JDBC {
	/**
	 * 连接数据库的驱动、路径、用户名、密码
	 */
	private String dbDriver;
	private String dbUrl;
	private String dbUser;
	private String dbPass;
	/**
	 * 每个JDBC对象包含一个数据库的连接
	 */
	private Connection conn;
	/**
	 * 每次结果集的获得条数
	 */
	private int fetchSize = 30;
	
	public JDBC(){
		
	}

	public JDBC(String dbDriver, String dbUrl, String dbUser, String dbPass) {
		this();
		this.dbDriver = dbDriver;
		this.dbUrl = dbUrl;
		this.dbUser = dbUser;
		this.dbPass = dbPass;
	}
	
	/**
	 * 初始化数据库连接，获得Connection对象
	 */
	public void startConnection() throws Exception{
		Class.forName(this.dbDriver);
		this.conn = DriverManager.getConnection(this.dbUrl, this.dbUser, this.dbPass);
	}
	/**
	 * 关闭连接
	 * @throws Exception
	 */
	public void stopConnection() throws Exception{
		if(this.conn != null){
			this.conn.close();
			this.conn = null;
		}
	}

	public Connection getConnection() throws Exception {
		if(conn == null){
			startConnection();
		}
		return conn;
	}
	
	/**
	 * 执行数据库查询操作，select
	 * @param sql
	 * @return
	 * @throws Exception 
	 */
	public ResultSet executeQuery(String sql) throws Exception {
		if(conn == null){
			startConnection();
		}
		Statement tmpstmt = conn.createStatement(
				ResultSet.TYPE_FORWARD_ONLY,
				ResultSet.CONCUR_READ_ONLY);
//		if(fetchSize>0)
//			tmpstmt.setFetchSize(fetchSize);
		return tmpstmt.executeQuery(sql);
	}
	
	/**
	 * 执行数据库的更新，insert、update、delete
	 * @param sql
	 * @return
	 * @throws Exception
	 */
	public boolean executeUpdate(String sql) throws Exception {
		if(conn == null){
			startConnection();
		}
		Statement tmpstmt = conn.createStatement();
		int rstCount = tmpstmt.executeUpdate(sql);
		return rstCount>0;
	}
	
	/**
	 * 开启事务操作
	 * @throws Exception 
	 */
	public void startTransaction() throws Exception {
		if(conn == null){
			startConnection();
		}
		this.conn.setAutoCommit(false);
	}
	
	/**
	 * 关闭事务，事务开启后，必须手工关闭
	 * @throws Exception
	 */
	public void stopTransaction() throws Exception {
		//事务如果没有开启，就直接返回
		if(this.conn == null || this.conn.getAutoCommit())
			return;
		this.conn.setAutoCommit(true);
	}
	
	public void commit() throws Exception {
		// 事务如果没有开启，就直接返回
		if (this.conn == null || this.conn.getAutoCommit())
			return;
		this.conn.commit();
	}
	
	public void rollback() throws Exception {
		// 事务如果没有开启，就直接返回
		if (this.conn == null || this.conn.getAutoCommit())
			return;
		this.conn.rollback();
	}

	public void setFetchSize(int fetchSize) {
		this.fetchSize = fetchSize;
	}

	public String getDbDriver() {
		return dbDriver;
	}

	public void setDbDriver(String dbDriver) {
		this.dbDriver = dbDriver;
	}

	public String getDbUrl() {
		return dbUrl;
	}

	public void setDbUrl(String dbUrl) {
		this.dbUrl = dbUrl;
	}

	public String getDbUser() {
		return dbUser;
	}

	public void setDbUser(String dbUser) {
		this.dbUser = dbUser;
	}

	public String getDbPass() {
		return dbPass;
	}

	public void setDbPass(String dbPass) {
		this.dbPass = dbPass;
	}
	
}

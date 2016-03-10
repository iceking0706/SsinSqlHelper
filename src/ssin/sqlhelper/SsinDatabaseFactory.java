package ssin.sqlhelper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.dbcp.BasicDataSource;

import ssin.sqlhelper.anno.DatabaseMetaInfo;
import ssin.sqlhelper.config.ConfigXML;

/**
 * 数据的操作封装
 * @author IcekingT420
 *
 */
public class SsinDatabaseFactory {
	
	/**
	 * 版本信息：
	 * V1.0，2015-6-23
	 * 		基本功能，仅支持sqlite
	 * 
	 * V1.1, 2015-7-3
	 * 		将一些统一的数据库操作，统一移动到SqlCommOperImpl中实现，目前支持sqlite和mysql
	 * 
	 * V1.2, 2015-7-24
	 * 		增加了mysql的支持
	 * 
	 * V1.3, 2015-9-24
	 * 		1、修正了字符型作为主键无法save的问题
	 * 
	 * 		2016-3-10
	 * 		1、修正mysql数据库自动创建数据的问题
	 * 		
	 */
	public static final String Version_Info = "V1.3";
	
	/**
	 * 线程中保存连接信息
	 */
	private ThreadLocal<Connection> threadLocal = new ThreadLocal<Connection>(); 
	
	/**
	 * 配置文件
	 */
	private ConfigXML configXML;
	
	/**
	 * 数据源
	 */
	private BasicDataSource dataSource;
	
	/**
	 * 判断配置文件是否准备就绪
	 */
	private boolean ready = false;
	
	/**
	 * 数据库元数据的描述信息
	 */
	private DatabaseMetaInfo metaInfo;
	
	/**
	 * 数据库的操作
	 */
	private SqlCommOper sqlOper;
	
	public SsinDatabaseFactory(ConfigXML configXML) {
		if(configXML == null || !configXML.isValid())
			return;
		this.configXML = configXML;
		init();
	}
	
	/**
	 * 构造时候的启动
	 */
	private void init(){
		SsinLoggerFactory.info(SsinDatabaseFactory.class, "SsinSqlHelper( By Iceking ) "+Version_Info+" init......");
		//如果使用了dbcp，就直接使用，否则就直接连接
		if(configXML.getDatabase() != null){
			if(configXML.getDatabase().useDbcp()){
				dataSource = new BasicDataSource();
				dataSource.setDriverClassName(configXML.getDatabase().getDriver());
				dataSource.setUrl(configXML.getDatabase().getUrl());
				dataSource.setUsername(configXML.getDatabase().getUser());
				dataSource.setPassword(configXML.getDatabase().getPass());
				
				//连接池的配置信息
				dataSource.setInitialSize(configXML.getDatabase().getDbcp().getInitialSize());
				dataSource.setMaxActive(configXML.getDatabase().getDbcp().getMaxActive());
				dataSource.setMaxIdle(configXML.getDatabase().getDbcp().getMaxIdle());
				dataSource.setMinIdle(configXML.getDatabase().getDbcp().getMinIdle());
				dataSource.setMaxWait(configXML.getDatabase().getDbcp().getMaxWait());
				dataSource.setRemoveAbandoned(configXML.getDatabase().getDbcp().isRemoveAbandoned());
				dataSource.setRemoveAbandonedTimeout(configXML.getDatabase().getDbcp().getRemoveAbandonedTimeout());
			}
			
			//构建数据库的元数据描述信息
			this.metaInfo = new DatabaseMetaInfo(configXML.getDatabase().getType());
			if(!configXML.getDatabase().getTableClasses().isEmpty()){
				for(String clsName: configXML.getDatabase().getTableClasses()){
					Class<?> cls = loadClass(clsName);
					if(cls == null)
						continue;
					metaInfo.addTableClass(cls);
				}
				metaInfo.parseMetaInfo();
			}
			
			//数据库操作的实现类
			if(configXML.getDatabase().getType().equals("sqlite")){
				sqlOper = new SqlOperForSqlite(this);
			}else if (configXML.getDatabase().getType().equals("mysql")) {
				sqlOper = new SqlOperForMysql(this);
			}
			
		}
		
		ready = true;
		
		//第一开始就要创建数据库
		if(sqlOper != null && configXML.getDatabase().isUpdateDB()){
			sqlOper.createDataBaseOnInit();
		}
	}
	
	private Class<?> loadClass(String clsName){
		try {
			return Class.forName(clsName);
		} catch (ClassNotFoundException e) {
			SsinLoggerFactory.error(SsinDatabaseFactory.class, "loadClass error, clsName="+clsName+": "+e.getMessage(), e);
			return null;
		}
	}
	
	public void unInit(){
		if(!isReady())
			return;
		if(isDbcp()){
			try {
				dataSource.close();
			} catch (SQLException e) {
				SsinLoggerFactory.error(SsinDatabaseFactory.class, "unInit.dataSource.close() error: "+e.getMessage(), e);
			}
		}
	}
	
	/**
	 * 打印出一些dbcp的信息
	 */
	public void showDBCPInfo(){
		if(!isDbcp())
			return;
		System.out.println("NumActive->"+dataSource.getNumActive());
		System.out.println("NumIdle->"+dataSource.getNumIdle());
	}
	
	/**
	 * 获得一个数据库的连接
	 * @return
	 */
	public Connection getConnection(){
		if(!isReady()){
			SsinLoggerFactory.error(SsinDatabaseFactory.class, "getConnection error: is not ready.");
			return null;
		}
		try {
			if(isDbcp()){
				//通过dbcp连接池去获得一次连接
				return dataSource.getConnection();
			}else{
				//直连数据库
				Class.forName(configXML.getDatabase().getDriver());
				return DriverManager.getConnection(configXML.getDatabase().getUrl(), configXML.getDatabase().getUser(), configXML.getDatabase().getPass());
			}
		} catch (Exception e) {
			SsinLoggerFactory.error(SsinDatabaseFactory.class, "getConnection error: "+e.getMessage(), e);
			return null;
		}
	}
	
	/**
	 * 关闭一个连接
	 * @param conn
	 */
	public boolean closeConnection(Connection conn){
		if(conn == null){
			SsinLoggerFactory.error(SsinDatabaseFactory.class, "closeConnection error: conn is null.");
			return false;
		}
		try {
			conn.close();
			return true;
		} catch (Exception e) {
			SsinLoggerFactory.error(SsinDatabaseFactory.class, "closeConnection error: "+e.getMessage(), e);
			return false;
		}
	}
	
	/**
	 * 开启一个查询，仅仅是connection放入threadlocal
	 * 只能用于查询
	 * @return
	 */
	public boolean startQuery(){
		//如果线程中已经有了Connection，就直接返回
		if(threadLocal.get() != null)
			return true;
		Connection conn = getConnection();
		if(conn == null){
			SsinLoggerFactory.error(SsinDatabaseFactory.class, "startQuery error: conn is null.");
			return false;
		}
		try {
			threadLocal.set(conn);
			return true;
		} catch (Exception e) {
			SsinLoggerFactory.error(SsinDatabaseFactory.class, "startQuery error: "+e.getMessage(), e);
			return false;
		}
	}
	
	/**
	 * 关闭一个查询
	 */
	public boolean stopQuery(){
		if(threadLocal.get()==null){
			//SsinLoggerFactory.error(SsinDatabaseFactory.class, "stopQuery error: query is not started, conn in threadLocal is null.");
			return true;
		}
		try {
			threadLocal.get().close();
			//从线程中去掉
			threadLocal.remove();
			return true;
		} catch (Exception e) {
			SsinLoggerFactory.error(SsinDatabaseFactory.class, "stopQuery error: "+e.getMessage(), e);
			return false;
		}
	}
	
	/**
	 * 开启一个事务，事务和当前线程相关，
	 * 事务之内的操纵，都使用同一个连接
	 * @return
	 */
	public boolean startTransaction(){
		//如果线程中已经存在，则直接返回
		if(threadLocal.get() != null){
			try {
				//如果不是事务模式，则设置
				if(threadLocal.get().getAutoCommit())
					threadLocal.get().setAutoCommit(false);
			} catch (Exception e) {
				SsinLoggerFactory.error(SsinDatabaseFactory.class, "startTransaction error: "+e.getMessage(), e);
				return false;
			}
			return true;
		}
		
		Connection conn = getConnection();
		if(conn == null){
			SsinLoggerFactory.error(SsinDatabaseFactory.class, "startTransaction error: conn is null.");
			return false;
		}
		try {
			conn.setAutoCommit(false);
			threadLocal.set(conn);
			return true;
		} catch (Exception e) {
			SsinLoggerFactory.error(SsinDatabaseFactory.class, "startTransaction error: "+e.getMessage(), e);
			return false;
		}
	}
	
	/**
	 * 事务启动之后的提交
	 * @return
	 */
	public boolean commit(){
		if(threadLocal.get()==null){
			//SsinLoggerFactory.error(SsinDatabaseFactory.class, "commit error: transaction is not started, conn in threadLocal is null.");
			return true;
		}
		try {
			threadLocal.get().commit();
			//并关闭事务了
			threadLocal.get().setAutoCommit(true);
			threadLocal.get().close();
			//从线程中去掉
			threadLocal.remove();
			return true;
		} catch (Exception e) {
			SsinLoggerFactory.error(SsinDatabaseFactory.class, "commit error: "+e.getMessage(), e);
			return false;
		}
	}
	
	/**
	 * 事务的回滚
	 * @return
	 */
	public boolean rollback(){
		if(threadLocal.get()==null){
			//SsinLoggerFactory.error(SsinDatabaseFactory.class, "rollback error: transaction is not started, conn in threadLocal is null.");
			return true;
		}
		try {
			threadLocal.get().rollback();
			//并关闭事务了
			threadLocal.get().setAutoCommit(true);
			threadLocal.get().close();
			//从线程中去掉
			threadLocal.remove();
			return true;
		} catch (Exception e) {
			SsinLoggerFactory.error(SsinDatabaseFactory.class, "commit error: "+e.getMessage(), e);
			return false;
		}
	}
	
	/**
	 * 直接使用事务中的连接
	 * @param sql
	 * @return
	 */
	public ResultSet executeQuery(String sql){
		return executeQuery(sql,null);
	}
	
	/**
	 * 直接使用事务中的删除
	 * @param sql
	 * @return
	 */
	public boolean executeUpdate(String sql){
		return executeUpdate(sql,null);
	}
	
	/**
	 * 执行一条sql语句，每次自己打开和关闭连接
	 * 使用事务中的连接
	 * @param sql
	 * @return
	 */
	public ResultSet executeQuery(String sql,Connection connection){
		Connection conn = connection;
		if(conn == null){
			//使用事务中的连接
			conn = threadLocal.get();
		}
		if(conn == null){
			SsinLoggerFactory.error(SsinDatabaseFactory.class, "executeQuery error: transaction is not started, conn in threadLocal is null.");
			return null;
		}
		//查询的类型，如果是sqlite，只有forward了
		int rstType = ResultSet.TYPE_SCROLL_SENSITIVE;
		if(configXML.getDatabase().getType().equals("sqlite")){
			rstType = ResultSet.TYPE_FORWARD_ONLY;
		}
		
		ResultSet rs = null;
		try {
			Statement stmt = conn.createStatement(rstType,ResultSet.CONCUR_READ_ONLY);
			rs = stmt.executeQuery(sql);
			if(configXML.getDatabase().isShowSql())
				SsinLoggerFactory.debug("SQL Query->"+sql);
		} catch (Exception e) {
			SsinLoggerFactory.error(SsinDatabaseFactory.class, "executeQuery.createStatement error: "+e.getMessage(), e);
			rs = null;
		}
		
		return rs;
	}
	
	/**
	 * 执行数据库更新，使用事务中的连接
	 * @param sql
	 * @return
	 */
	public boolean executeUpdate(String sql,Connection connection){
		Connection conn = connection;
		if(conn == null){
			//使用事务中的连接
			conn = threadLocal.get();
		}
		if(conn == null){
			SsinLoggerFactory.error(SsinDatabaseFactory.class, "executeUpdate error: transaction is not started, conn in threadLocal is null.");
			return false;
		}
		try {
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(sql);
			if(configXML.getDatabase().isShowSql())
				SsinLoggerFactory.debug("SQL Update->"+sql);
			return true;
		} catch (Exception e) {
			SsinLoggerFactory.error(SsinDatabaseFactory.class, "executeUpdate.createStatement error: "+e.getMessage(), e);
			return false;
		}
	}
	
	/**
	 * 判断是否使用dbcp连接池
	 * @return
	 */
	public boolean isDbcp(){
		return dataSource!=null;
	}

	public BasicDataSource getDataSource() {
		return dataSource;
	}

	public boolean isReady() {
		return ready;
	}

	public DatabaseMetaInfo getMetaInfo() {
		return metaInfo;
	}

	public ConfigXML getConfigXML() {
		return configXML;
	}

	public SqlCommOper getSqlOper() {
		return sqlOper;
	}
	
}

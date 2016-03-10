package ssin.sqlhelper;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;




/**
 * 数据库常用操作的封装
 * @author IcekingT420
 *
 */
public interface SqlCommOper {
	/**
	 * 最基础的查询，通过sql语句，和一个连接对象
	 * @param sql
	 * @param conn
	 * @return
	 */
	public List<Object[]> select(Connection conn,String sql);
	public List<Object[]> select(String sql);
	
	/**
	 * 加翻页的基础查询
	 * @param sql
	 * @param conn
	 * @param page
	 * @return
	 */
	public PageData<Object[]> select(Connection conn,String sql,PageParam page);
	public PageData<Object[]> select(String sql,PageParam page);
	
	/**
	 * 查询对象列表，where语句自定义
	 * @param conn
	 * @param tableClass
	 * @param whereStr
	 * @return
	 */
	public <T> List<T> select(Connection conn,Class<T> tableClass,String whereStr);
	public <T> List<T> select(Class<T> tableClass,String whereStr);
	
	/**
	 * 查询对象，翻页
	 * @param conn
	 * @param tableClass
	 * @param whereStr
	 * @param page
	 * @return
	 */
	public <T> PageData<T> select(Connection conn,Class<T> tableClass,String whereStr,PageParam page);
	public <T> PageData<T> select(Class<T> tableClass,String whereStr,PageParam page);
	
	public <T> PageData<T> select(Connection conn,Class<T> tableClass,String whereStr,String orderByStr,PageParam page);
	public <T> PageData<T> select(Class<T> tableClass,String whereStr,String orderByStr,PageParam page);
	
	/**
	 * 仅返回第一个结果
	 * @param conn
	 * @param sql
	 * @return
	 */
	public Object[] selectOne(Connection conn,String sql);
	public Object[] selectOne(String sql);
	
	/**
	 * 仅返回第一个对象结果
	 * @param conn
	 * @param tableClass
	 * @param whereStr
	 * @return
	 */
	public <T> T selectOne(Connection conn,Class<T> tableClass,String whereStr);
	public <T> T selectOne(Class<T> tableClass,String whereStr);
	
	/**
	 * 将rs中的某一行解析为对象
	 * rs本身的循环之类还是自己控制
	 * @param tableClass
	 * @param rs
	 * @return
	 */
	public <T> T parseRSOneline(Class<T> tableClass,ResultSet rs);
	
	/**
	 * 查询满足条件的数量
	 * @param tableClass
	 * @param whereStr
	 * @return
	 */
	public <T> long total(Connection conn,Class<T> tableClass,String whereStr);
	public <T> long total(Connection conn,Class<T> tableClass);
	public <T> long total(Class<T> tableClass,String whereStr);
	public <T> long total(Class<T> tableClass);
	
	/**
	 * 主键为数值的，返回最大值
	 * @param conn
	 * @param tableClass
	 * @return
	 */
	public <T> long maxPkId(Connection conn,Class<T> tableClass);
	public <T> long maxPkId(Class<T> tableClass);
	
	/**
	 * 直接根据一个sql得到total或max的long结果
	 * @param sql
	 * @return
	 */
	public long totalOrMax(Connection conn,String sql);
	public long totalOrMax(String sql);
	
	/**
	 * 根据主键查询对象
	 * @param conn
	 * @param tableClass
	 * @param pkValue
	 * @return
	 */
	public <T> T findByPrimaryKey(Connection conn,Class<T> tableClass,Object pkValue);
	public <T> T findByPrimaryKey(Class<T> tableClass,Object pkValue);
	
	/**
	 * 保存，新增和修改，自动判断
	 * @param conn
	 * @param po
	 * @return
	 */
	public <T> T save(Connection conn,T po);
	public <T> T save(T po);
	
	/**
	 * 批量插入数据
	 * 单个的话，使用save
	 * @param conn
	 * @param poList
	 */
	public <T> void insert(Connection conn,List<T> poList);
	public <T> void insert(List<T> poList);
	
	/**
	 * 删除，数组形式
	 * @param conn
	 * @param po
	 * @return
	 */
	public <T> boolean delete(Connection conn,T...po);
	public <T> boolean delete(T...po);
	
	/**
	 * 删除，列表形式
	 * @param conn
	 * @param poList
	 * @return
	 */
	public <T> boolean delete(Connection conn,List<T> poList);
	public <T> boolean delete(List<T> poList);
	
	/**
	 * 判断表格是否存在
	 * @param conn
	 * @param tableClass
	 * @return
	 */
	public <T> boolean isTableExist(Connection conn,Class<T> tableClass);
	public <T> boolean isTableExist(Class<T> tableClass);
	
	/**
	 * 判断索引是否存在
	 */
	public <T> boolean isIndexExist(Connection conn,Class<T> tableClass);
	public <T> boolean isIndexExist(Class<T> tableClass);
	
	/**
	 * 判断表格的某个字段是否存在
	 * @param conn
	 * @param tableClass
	 * @param columnName
	 * @return
	 */
	public <T> boolean isColumnExist(Connection conn,Class<T> tableClass,String columnName);
	public <T> boolean isColumnExist(Class<T> tableClass,String columnName);
	
	/**
	 * 得到某张表格现有的数据库字段名字列表
	 * @param tableClass
	 * @return
	 */
	public List<String> getColumnNames(Connection conn,Class<?> tableClass);
	public List<String> getColumnNames(Class<?> tableClass);
	
	/**
	 * 系统一开始时候，建立数据库，并且要去匹配对象的各个属性
	 * 以及外键，索引
	 */
	public void createDataBaseOnInit();
	
}

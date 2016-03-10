package ssin.sqlhelper.config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Level;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * 配置的xml，每个数据库可以有一个配置文件
 * 一般一个项目中一个配置文件即可
 * 默认的路径是在user.dir下面的
 * 
 * 默认的名字为：SsinSqlHelperCfg.xml
 * @author IcekingT420
 *
 */
public class ConfigXML {
	private static final String defaultConfigXmlFileName = "SsinSqlHelperCfg.xml";
	
	/**
	 * 数据库类型的合法字符
	 */
	private static final String[] dbTypeValidArray = new String[]{"sqlite","mysql","sqlserver","oracle"};
	/**
	 * log4j的默认日志输出级别
	 */
	private static final String[] log4jLevelValidArray = new String[]{"debug","info","warn","error"};
	
	/**
	 * 配置文件 SsinSqlHelperCfg.xml 所在
	 */
	private File configFile;
	
	/**
	 * 解析得到xml中的内容
	 */
	private TagDatabase database;
	private TagLog4j log4j;
	
	/**
	 * 判断xml是否正确
	 */
	private boolean valid = false;
	
	/**
	 * 解析失败的时候，给出原因
	 */
	private String error = "ok";
	
	/**
	 * 默认的构造函数，使用user.dir下面的SsinSqlHelperCfg.xml
	 */
	public ConfigXML(){
		File userDir = new File(System.getProperty("user.dir"));
		this.configFile = new File(userDir, defaultConfigXmlFileName);
		if(this.configFile!=null && this.configFile.exists() && this.configFile.isFile()){
			parseXml();
		}
	}
	
	/**
	 * 使用指定的配置文件
	 * @param xmlFile
	 */
	public ConfigXML(File xmlFile){
		if(xmlFile!=null && xmlFile.exists() && xmlFile.isFile()){
			this.configFile = xmlFile;
			parseXml();
		}
	}
	
	/**
	 * 解析xml
	 */
	private void parseXml(){
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document document = builder.parse(configFile);
			Element root = document.getDocumentElement();
			if(root==null || !root.getTagName().equals("SsinSqlHelperCfg")){
				error = "Root element tag name is not SsinSqlHelperCfg";
				return;
			}
			//开始解析 database 中的内容
			Element elmDatabase = findElementByTag(root, "database");
			
			if(elmDatabase!=null && parseBoolean(elmDatabase.getAttribute("use"))){
				database = new TagDatabase();
				NodeList nodeList = elmDatabase.getChildNodes();
				if(nodeList==null || nodeList.getLength() == 0){
					error = "Children nodes of tag database is empty";
					return;
				}
				for(int i=0;i<nodeList.getLength();i++){
					Node node = nodeList.item(i);
					if(node.getNodeName().equals("type")){
						String str = node.getTextContent().trim();
						if(!isStrInArray(str, dbTypeValidArray)){
							error = "Value of database->type must be: "+showStringArray(dbTypeValidArray, "|");
							return;
						}
						database.setType(str);
					}else if (node.getNodeName().equals("name")) {
						database.setName(node.getTextContent().trim());
					}else if (node.getNodeName().equals("driver")) {
						String driver = node.getTextContent().trim();
						//需要根据不同数据库类型对驱动进行判断
						if(database.getType().equals("sqlite")){
							if(!driver.equals("org.sqlite.JDBC")){
								error = "Sqlite driver must be: org.sqlite.JDBC. Now is: "+driver;
								return;
							}
						}else if (database.getType().equals("mysql")) {
							if(!driver.equals("com.mysql.jdbc.Driver")){
								error = "Mysql driver must be: com.mysql.jdbc.Driver. Now is: "+driver;
								return;
							}
						}
						database.setDriver(node.getTextContent().trim());
					}else if (node.getNodeName().equals("url")) {
						//url的地方需要替换${user.dir}
						String url = node.getTextContent().trim();
						if(url.indexOf("${user.dir}") != -1){
							url = url.replace("${user.dir}", System.getProperty("user.dir"));
						}
						//不同数据库的url需要判断
						if(database.getType().equals("sqlite")){
							if(!url.startsWith("jdbc:sqlite:")){
								error = "Sqlite url must start with: jdbc:sqlite: . Now is: "+url;
								return;
							}
						}else if (database.getType().equals("mysql")) {
							if(!url.startsWith("jdbc:mysql://")){
								error = "Mysql url must start with: jdbc:mysql:// . Now is: "+url;
								return;
							}
						}
						database.setUrl(url);
					}else if (node.getNodeName().equals("user")) {
						database.setUser(node.getTextContent().trim());
					}else if (node.getNodeName().equals("pass")) {
						database.setPass(node.getTextContent().trim());
					}else if (node.getNodeName().equals("updateDB")) {
						boolean updateDB = true;
						try {
							updateDB = Boolean.parseBoolean(node.getTextContent().trim());
						} catch (Exception e) {
							updateDB = true;
						}
						database.setUpdateDB(updateDB);
					}else if (node.getNodeName().equals("showSql")) {
						boolean showSql = true;
						try {
							showSql = Boolean.parseBoolean(node.getTextContent().trim());
						} catch (Exception e) {
							showSql = true;
						}
						database.setShowSql(showSql);
					}else if (node.getNodeName().equals("dbcp")) {
						//如果是dbcp，并且use=true
						Element elmDbcp = (Element)node;
						String attrUse = elmDbcp.getAttribute("use");
						if(parseBoolean(attrUse)){
							TagDbcp dbcp = new TagDbcp();
							NodeList tmpNodeList = elmDbcp.getChildNodes();
							if(tmpNodeList==null || tmpNodeList.getLength()==0){
								error = "Children nodes of tag database->dbcp is empty";
								return;
							}
							for(int j=0;j<tmpNodeList.getLength();j++){
								Node tmpNode = tmpNodeList.item(j);
								if(tmpNode.getNodeName().equals("initialSize")){
									int initialSize = parseInteger(tmpNode.getTextContent().trim());
									if(initialSize<0){
										error = "Value of tag database->dbcp->initialSize must >=0";
										return;
									}
									dbcp.setInitialSize(initialSize);
								}else if (tmpNode.getNodeName().equals("maxActive")) {
									int maxActive = parseInteger(tmpNode.getTextContent().trim());
									if(maxActive<0){
										error = "Value of tag database->dbcp->maxActive must >=0";
										return;
									}
									dbcp.setMaxActive(maxActive);
								}else if (tmpNode.getNodeName().equals("maxIdle")) {
									int maxIdle = parseInteger(tmpNode.getTextContent().trim());
									if(maxIdle<0){
										error = "Value of tag database->dbcp->maxIdle must >=0";
										return;
									}
									dbcp.setMaxIdle(maxIdle);
								}else if (tmpNode.getNodeName().equals("minIdle")) {
									int minIdle = parseInteger(tmpNode.getTextContent().trim());
									if(minIdle<0){
										error = "Value of tag database->dbcp->minIdle must >=0";
										return;
									}
									dbcp.setMinIdle(minIdle);
								}else if (tmpNode.getNodeName().equals("maxWait")) {
									long maxWait = parseLong(tmpNode.getTextContent().trim());
									if(maxWait<0l){
										error = "Value of tag database->dbcp->maxWait must >=0";
										return;
									}
									dbcp.setMaxWait(maxWait);
								}else if (tmpNode.getNodeName().equals("removeAbandoned")) {
									boolean removeAbandoned = parseBoolean(tmpNode.getTextContent().trim());
									dbcp.setRemoveAbandoned(removeAbandoned);
								}else if (tmpNode.getNodeName().equals("removeAbandonedTimeout")) {
									int removeAbandonedTimeout = parseInteger(tmpNode.getTextContent().trim());
									if(removeAbandonedTimeout<0){
										error = "Value of tag database->dbcp->removeAbandonedTimeout must >=0";
										return;
									}
									dbcp.setRemoveAbandonedTimeout(removeAbandonedTimeout);
								}
							}
							database.setDbcp(dbcp);
						}
					}else if (node.getNodeName().equals("tableClasses")) {
						Element elmTableClasses = (Element)node;
						NodeList tcNodeList = elmTableClasses.getChildNodes();
						if(tcNodeList==null || tcNodeList.getLength()==0){
							error = "Children nodes of tag database->tableClasses is empty";
							return;
						}
						for(int j=0;j<tcNodeList.getLength();j++){
							Node tcNode = tcNodeList.item(j);
							if(tcNode.getNodeName().equals("Class")){
								database.getTableClasses().add(tcNode.getTextContent());
							}
						}
					}
				}
			}
			
			//开始解析log4j的部分
			Element elmLog4j = findElementByTag(root, "log4j");
			
			if(elmLog4j!=null && parseBoolean(elmLog4j.getAttribute("use"))){
				NodeList nodeList2 = elmLog4j.getChildNodes();
				if(nodeList2==null || nodeList2.getLength()==0){
					error = "Children nodes of tag log4j is empty";
					return;
				}
				log4j = new TagLog4j();
				for(int i=0;i<nodeList2.getLength();i++){
					Node node = nodeList2.item(i);
					if(node.getNodeName().equals("saveDir")){
						String str = node.getTextContent().trim();
						if(str.indexOf("${user.dir}") != -1){
							str = str.replace("${user.dir}", System.getProperty("user.dir"));
						}
						File dir = new File(str);
						if(!dir.exists()){
							//不存在，创建文件夹
							if(!dir.mkdirs()){
								error = "Value of log4j->saveDir is not exist, make fail.: "+str;
								return;
							}
						}
						if(!dir.isDirectory()){
							error = "Value of log4j->saveDir is not a valid directory: "+str;
							return;
						}
						log4j.setSaveDir(dir);
					}else if (node.getNodeName().equals("level")) {
						String str = node.getTextContent().trim();
						if(!isStrInArray(str, log4jLevelValidArray)){
							error = "Value of log4j->level must be: "+showStringArray(log4jLevelValidArray, "|");
							return;
						}
						if(str.equalsIgnoreCase("debug")){
							log4j.setLevel(Level.DEBUG);
						}else if (str.equalsIgnoreCase("info")) {
							log4j.setLevel(Level.INFO);
						}else if (str.equalsIgnoreCase("warn")) {
							log4j.setLevel(Level.WARN);
						}else if (str.equalsIgnoreCase("error")) {
							log4j.setLevel(Level.ERROR);
						}
					}else if (node.getNodeName().equals("layoutPattern")) {
						log4j.setLayoutPattern(node.getTextContent().trim());
					}else if (node.getNodeName().equals("ConsoleAppender")) {
						Element tmpElm = (Element)node;
						String attri = tmpElm.getAttribute("use");
						log4j.setConsoleAppender(parseBoolean(attri));
					}else if (node.getNodeName().equals("RollingFileAppender")) {
						Element tmpElm = (Element)node;
						String attri = tmpElm.getAttribute("use");
						log4j.setRollingFileAppender(parseBoolean(attri));
						if(log4j.isRollingFileAppender()){
							//文件路径
							attri = tmpElm.getAttribute("filepath");
							if(attri==null || attri.equals("")){
								error = "Value of log4j->RollingFileAppender.filepath is empty";
								return;
							}
							log4j.setRollingFilepath(attri);
							//单个文件大小
							attri = tmpElm.getAttribute("maxFileSize");
							if(attri==null || attri.equals("")){
								error = "Value of log4j->RollingFileAppender.maxFileSize is empty";
								return;
							}
							attri = attri.toUpperCase();
							//文件大小，必须以G | M | K结尾
							if(!attri.endsWith("G") && !attri.endsWith("M") && !attri.endsWith("K")){
								error = "Value of log4j->RollingFileAppender.maxFileSize format error: "+attri;
								return;
							}
							//基数，要转换为字节
							long baseB = 1024l;
							if(attri.endsWith("M"))
								baseB = baseB * 1024;
							else if(attri.endsWith("G"))
								baseB = baseB * 1024 * 1024;
							
							attri = attri.substring(0,attri.length()-1);
							long maxFileSize = parseLong(attri);
							if(maxFileSize<=0l){
								error = "Value of log4j->RollingFileAppender.maxFileSize format error: "+attri;
								return;
							}
							log4j.setRollingMaxFileSize(maxFileSize*baseB);
							//文件数量
							attri = tmpElm.getAttribute("maxFileCount");
							if(attri==null || attri.equals("")){
								error = "Value of log4j->RollingFileAppender.maxFileCount is empty";
								return;
							}
							int maxFileCount = parseInteger(attri);
							if(maxFileCount<=0){
								error = "Value of log4j->RollingFileAppender.maxFileCount format error: "+attri;
								return;
							}
							log4j.setRollingMaxFileCount(maxFileCount);
						}
					}else if (node.getNodeName().equals("DailyRollingFileAppender")) {
						Element tmpElm = (Element)node;
						String attri = tmpElm.getAttribute("use");
						log4j.setDailyRollingFileAppender(parseBoolean(attri));
						if(log4j.isDailyRollingFileAppender()){
							attri = tmpElm.getAttribute("filepath");
							if(attri==null || attri.equals("")){
								error = "Value of log4j->DailyRollingFileAppender.filepath is empty";
								return;
							}
							log4j.setDailyFilepath(attri);
						}
					}
				}
				//三个Appender至少一个，保存路径必须存在
				if(!log4j.isConsoleAppender() && !log4j.isRollingFileAppender() && !log4j.isDailyRollingFileAppender()){
					error = "log4j at least specify one Appender";
					return;
				}
			}
			
			//databse 和 log4j 至少使用一个
			if(database==null && log4j == null){
				error = "Function database or log4j at least use one";
				return;
			}
			
			valid = true;
		} catch (Exception e) {
			e.printStackTrace();
			error = "Parse Exception: "+e.getMessage();
			valid = false;
		}
	}
	
	/**
	 * 判断是否是整数
	 * @param str
	 * @return
	 */
	private boolean isNumber(String str){
		try {
			Long.parseLong(str);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	private int parseInteger(String str){
		int value = -1;
		try {
			value = Integer.parseInt(str);
		} catch (Exception e) {
			value = -1;
		}
		return value;
	}
	
	private long parseLong(String str){
		long value = -1l;
		try {
			value = Long.parseLong(str);
		} catch (Exception e) {
			value = -1l;
		}
		return value;
	}
	
	private boolean parseBoolean(String str){
		boolean value = false;
		try {
			value = Boolean.parseBoolean(str);
		} catch (Exception e) {
			value = false;
		}
		return value;
	}
	
	/**
	 * 判断字符串是否在数组之内
	 * @param str
	 * @param array
	 * @return
	 */
	private boolean isStrInArray(String str,String[] array){
		for(String arr: array){
			if(str.equalsIgnoreCase(arr))
				return true;
		}
		return false;
	}
	
	/**
	 * 将数组变成一个字符
	 * @param array
	 * @param sep
	 * @return
	 */
	private String showStringArray(String[] array,String sep){
		String str = "";
		for(int i=0;i<array.length;i++){
			if(i>0){
				str += " "+(sep!=null?sep:",")+"";
			}
			str += array[i];
		}
		return str;
	}
	
	/**
	 * 从一个element中找到第一个满足tagName的节点
	 * @param parent
	 * @param tagName
	 * @return
	 */
	private Element findElementByTag(Element parent,String tagName){
		NodeList list = parent.getElementsByTagName(tagName);
		if(list == null || list.getLength() == 0)
			return null;
		return (Element)list.item(0);
	}
	
	/**
	 * 从一个element中返回多个满足tagName的节点
	 * @param parent
	 * @param tagName
	 * @return
	 */
	private List<Element> findElementsByTag(Element parent,String tagName){
		List<Element> list = new ArrayList<Element>();
		NodeList nodes = parent.getElementsByTagName(tagName);
		if(nodes == null || nodes.getLength() == 0)
			return list;
		for(int i=0;i<nodes.getLength();i++){
			list.add((Element)nodes.item(i));
		}
		return list;
	}

	public TagDatabase getDatabase() {
		return database;
	}

	public TagLog4j getLog4j() {
		return log4j;
	}

	public boolean isValid() {
		return valid;
	}

	public String getError() {
		return error;
	}
	
}

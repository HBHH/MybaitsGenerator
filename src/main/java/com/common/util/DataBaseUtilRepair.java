package com.common.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



class Column {
	private String columnName;
	private String typeName;
	private String comment;
	private String primaryKey;

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(String primaryKey) {
		this.primaryKey = primaryKey;
	}
}

public class DataBaseUtilRepair {
	private static final Logger LOG = LoggerFactory.getLogger(DataBaseUtilRepair.class);

	private static Properties properties;
	static {
		properties = PropertiesUtil.getProperties(PathUtil.getEclipseWorkspaceProjectPath("") + "src/main/resources/database.properties");
	}

	private DataBaseUtilRepair() {
	}

	public static Connection getConnection() {
		String conUrl = properties.getProperty("databaseUrl");
		String user = properties.getProperty("databaseUsername");
		String password = properties.getProperty("databasePasswd");
		return getConnection(conUrl, user, password);
	}

	public static Connection getConnection(String conUrl, String user, String password) {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();// oracle.jdbc.driver.OracleDriver
			return DriverManager.getConnection(conUrl, user, password);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	// http://www.cr173.com/html/18961_1.html
	// http://blog.csdn.net/icejadelove/article/details/4966317

	// mysql --host=localhost -u root -p --default-character-set=gbk 数据库名�?<
	// E:back.sql

	/**
	 * 使用java进行数据库备�?DatabaseMetaData meta =
	 * DBUtil.getDataSource().getConnection().getMetaData(); 2 int
	 * defaultIsolation = meta.getDefaultTransactionIsolation();
	 * 
	 * @param mysqldumpPath
	 * @param host
	 * @param port
	 * @param databaseName
	 * @param tableName
	 * @param mysqlUser
	 * @param mysqlPassword
	 * @param charset
	 * @param sqlFile
	 * @return
	 */
	public static String[] mysqlBackupDatabase(String mysqldumpPath, String host, int port, String databaseName, String tableName, String mysqlUser, String mysqlPassword, String charset, String sqlFile) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("backupDatabase(String mysqldumpPath, String host, int port, String databaseName, String tableName, String mysqlUser, String mysqlPassword, String charset, String sqlFileName)");
			LOG.debug("mysqldumpPath=" + mysqldumpPath);
			LOG.debug("host=" + host);
			LOG.debug("port=" + port);
			LOG.debug("databaseName=" + databaseName);
			LOG.debug("tableName=" + tableName);
			LOG.debug("mysqlUser=" + mysqlUser);
			LOG.debug("mysqlPassword=" + mysqlPassword);
			LOG.debug("charset=" + charset);
			LOG.debug("sqlFile=" + sqlFile);
		}
		if (mysqlUser == null) {
			throw new NullPointerException("user == null");
		}

		// {"status":"","message":""}
		String[] resultArr = new String[2];

		String osName = System.getProperties().getProperty("os.name");
		if (osName == null || "".equals(osName.trim())) {
			osName = "linux";
		} else {
			osName = osName.trim().toLowerCase();
		}

		if (mysqldumpPath == null || "".equals(mysqldumpPath.trim())) {
			mysqldumpPath = "mysqldump";
		} else {
			mysqldumpPath = mysqldumpPath.trim().replaceAll("////", "/");
			if (mysqldumpPath.endsWith("/")) {
				mysqldumpPath = mysqldumpPath + "mysqldump";
			} else if (!mysqldumpPath.endsWith("mysqldump")) {
				mysqldumpPath = mysqldumpPath + "/mysqldump";
			}
		}

		StringBuilder mysqldumpSb = new StringBuilder(mysqldumpPath);
		if (host != null && !"".equals(host.trim())) {
			mysqldumpSb.append(" --host=").append(host);
		}
		if (port > 0 && port < 65535) {
			mysqldumpSb.append(" --port=").append(port);
		}
		mysqldumpSb.append(" --user=").append(mysqlUser);
		mysqldumpSb.append(" --password=").append(mysqlPassword == null ? "" : mysqlPassword);

		if (charset != null && !"".equals(charset.trim())) {
			mysqldumpSb.append(" --default-character-set=").append(charset.equalsIgnoreCase(WebConstant.CHARSET_UTF8) ? "utf8" : charset);
		}
		mysqldumpSb.append(" --opt ");

		if (databaseName != null && !"".equals(databaseName)) {
			mysqldumpSb.append(" --databases ").append(databaseName);
		} else {
			tableName = null;
		}
		if (tableName != null && !"".equals(tableName)) {
			mysqldumpSb.append(" --tables ").append(tableName);
		}
		if (osName.indexOf("win") < 0) {
			mysqldumpSb.append(" |gzip ");
			sqlFile = sqlFile + ".gz";
		}
		mysqldumpSb.append(" > ").append(sqlFile);

		if (LOG.isDebugEnabled()) {
			LOG.debug("osName=" + osName);
			LOG.debug(mysqldumpSb.toString());
		}

		String[] execArr = new String[3];
		if (osName.indexOf("win") > -1) {
			execArr[0] = "cmd";
			execArr[1] = "/c";
		} else {
			execArr[0] = "sh";
			execArr[1] = "-c";
		}
		execArr[2] = mysqldumpSb.toString();

		int result = -1;
		Process process = null;
		try {
			process = Runtime.getRuntime().exec(execArr);
			result = process.waitFor();// linux下host错误怎么没有抛出错误？依然返�?
			resultArr[0] = String.valueOf(result);
			if (result == 0) {
				resultArr[1] = "success";
			} else {
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream(),WebConstant.CHARSET_UTF8));
				StringBuilder errorSb = new StringBuilder();
				for (String str = bufferedReader.readLine(); str != null; str = bufferedReader.readLine()) {
					errorSb.append(str);
				}
				bufferedReader.close();
				resultArr[1] = errorSb.toString();
				LOG.error(resultArr[1]);
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}finally {
			try {
				process.getErrorStream().close();
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}
			try {
				process.getInputStream().close();
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}
			try {
				process.getOutputStream().close();
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}
		}

		File file = new File(sqlFile);
		if (result == 0) {// 代表成功
			if (!file.exists()) {
				LOG.error("File not Found sqlFile=" + sqlFile);
			}
		} else {
			if (file.exists()) {
				file.delete();
			}
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("resultArr=" + Arrays.deepToString(resultArr));
		}
		return resultArr;
	}

	public static void resultSetMetaData(ResultSet rs) {
		try {
			ResultSetMetaData resultSetMetaData = rs.getMetaData();
			for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
				String field = resultSetMetaData.getColumnName(i);
				////System.out.println(field);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取类型名称,与数据库无关,与开发语�?���?
	 * 
	 * @param column
	 * @param isNumAllToInt
	 *            默认不使用short(Short) byte(Byte)类型,直接使用int(Integer)
	 * @param useBaseType
	 *            不同的类型有不同的get set方法 <br>
	 *            1=默认使用long int short byte boolean 基础类型 不使用引用类�?br>
	 *            2=使用Long Integer Short Byte,boolean <br>
	 *            3=Long Integer Short Byte,Boolean 因为boolean �?Boolean自动生成的get
	 *            set不一�?
	 * @return
	 */
	public static String getPropertyType(String sqlType, boolean isNumAllToInt, int useBaseType) {
		if (sqlType == null || "".equals(sqlType)) {
			return "Object";
		}

		int index = sqlType.indexOf(' ');
		String typeName = sqlType;
		if (index > -1) {
			typeName = typeName.substring(0, index);
		}
		if (typeName.equalsIgnoreCase("BIT")) {
			if (useBaseType == 3) {
				return "Boolean";
			} else {
				return "boolean";
			}
		} else if (typeName.equalsIgnoreCase("TINYINT")) {
			if (isNumAllToInt) {
				if (useBaseType == 1) {
					return "int";
				} else {
					return "Integer";
				}
			} else {
				if (useBaseType == 1) {
					return "byte";
				} else {
					return "Byte";
				}
			}
		} else if (typeName.equalsIgnoreCase("BIGINT")) {
			if (useBaseType == 1) {
				return "long";
			} else {
				return "Long";
			}
		} else if (typeName.equalsIgnoreCase("LONGVARBINARY")) {
			return "byte[]";
		} else if (typeName.equalsIgnoreCase("LONGBLOB")) {
			return "byte[]";
		} else if (typeName.equalsIgnoreCase("MEDIUMBLOB")) {
			return "byte[]";
		} else if (typeName.equalsIgnoreCase("VARBINARY")) {
			return "byte[]";
		} else if (typeName.equalsIgnoreCase("BINARY")) {
			return "byte[]";
		} else if (typeName.equalsIgnoreCase("TINYBLOB")) {
			return "byte[]";
		} else if (typeName.equalsIgnoreCase("LONGVARCHAR")) {
			return "String";
		} else if (typeName.equalsIgnoreCase("TEXT")) {
			return "String";
		} else if (typeName.equalsIgnoreCase("LONGTEXT")) {
			return "String";
		} else if (typeName.equalsIgnoreCase("MEDIUMTEXT")) {
			return "String";
		} else if (typeName.equalsIgnoreCase("NULL")) {
			return "Object";
		} else if (typeName.equalsIgnoreCase("CHAR")) {
			return "String";
		} else if (typeName.equalsIgnoreCase("NUMERIC")) {
			return "BigDecimal";
		} else if (typeName.equalsIgnoreCase("DECIMAL")) {
			// return "BigDecimal";
			if (useBaseType == 1) {
				return "double";
			} else {
				return "Double";
			}
		} else if (typeName.equalsIgnoreCase("DEC")) {
			return "BigDecimal";
		} else if (typeName.equalsIgnoreCase("FIXED")) {
			return "BigDecimal";
		} else if (typeName.equalsIgnoreCase("INT") || typeName.equalsIgnoreCase("INTEGER")) {
			if (useBaseType == 1) {
				return "int";
			} else {
				return "Integer";
			}
		} else if (typeName.equalsIgnoreCase("MEDIUMINT")) {
			if (useBaseType == 1) {
				return "long";
			} else {
				return "Long";
			}
		} else if (typeName.equalsIgnoreCase("SMALLINT")) {
			if (isNumAllToInt) {
				if (useBaseType == 1) {
					return "int";
				} else {
					return "Integer";
				}
			} else {
				if (useBaseType == 1) {
					return "short";
				} else {
					return "Short";
				}
			}
		} else if (typeName.equalsIgnoreCase("FLOAT")) {
			if (useBaseType == 1) {
				return "float";
			} else {
				return "Float";
			}
		} else if (typeName.equalsIgnoreCase("REAL")) {
			if (useBaseType == 1) {
				return "double";
			} else {
				return "Double";
			}
		} else if (typeName.equalsIgnoreCase("DOUBLE")) {
			if (useBaseType == 1) {
				return "double";
			} else {
				return "Double";
			}
		} else if (typeName.equalsIgnoreCase("VARCHAR")) {
			return "String";
		} else if (typeName.equalsIgnoreCase("TINYTEXT")) {
			return "String";
		} else if (typeName.equalsIgnoreCase("BOOLEAN")) {
			if (useBaseType == 1) {
				return "boolean";
			} else {
				return "Boolean";
			}
		} else if (typeName.equalsIgnoreCase("DATALINK")) {
			return "Object";
		} else if (typeName.equalsIgnoreCase("DATE")) {
			return "Date";// ibatis使用的是java.util.Date
		} else if (typeName.equalsIgnoreCase("YEAR")) {
			return "Date";// ibatis使用的是java.util.Date
		} else if (typeName.equalsIgnoreCase("TIME")) {
			return "Time";// ibatis使用的是java.util.Date
		} else if (typeName.equalsIgnoreCase("DATETIME")) {
			return "Date";
		} else if (typeName.equalsIgnoreCase("TIMESTAMP")) {
			return "Date";// ibatis使用的是java.util.Date
		} else if (typeName.equalsIgnoreCase("OTHER")) {
			return "Object";
		} else if (typeName.equalsIgnoreCase("JAVA_OBJECT")) {
			return "Object";
		} else if (typeName.equalsIgnoreCase("DISTINCT")) {
			return "Object";
		} else if (typeName.equalsIgnoreCase("STRUCT")) {
			return "Object";
		} else if (typeName.equalsIgnoreCase("ARRAY")) {
			return "Object";
		} else if (typeName.equalsIgnoreCase("BLOB")) {
			return "Blob";// ibatis使用的是byte[]
		} else if (typeName.equalsIgnoreCase("CLOB")) {
			return "Clob";// �?��不要使用这个类型
		} else if (typeName.equalsIgnoreCase("URL")) {
			return "URL";
		} else if (typeName.equalsIgnoreCase("REF")) {
			return "Object";
		} else {
			return "Object";
		}
	}

	public static String getModelJava(Connection connection, String tableName) {

		String modelJavaContent = "";
		try {
			DatabaseMetaData databaseMetaData = connection.getMetaData();
			ResultSet rs = databaseMetaData.getColumns(null, "%", tableName, "%");
			String modelJavaField = "";
			while (rs.next()) {
				String columnName = rs.getString("COLUMN_NAME");
				if (columnName == null) {
					continue;
				}
				String comment = rs.getString("REMARKS");
				String typeName = rs.getString("TYPE_NAME");
				modelJavaField = modelJavaField + (StringUtils.isBlank(comment) ? "" : "/** " + comment + " **/\r\n");
				modelJavaField = modelJavaField + "private " + getPropertyType(typeName, true, 3) + " " + StringUtils.toClassPropertyName(columnName) + ";\r\n";
			}
			modelJavaContent = modelJavaContent + modelJavaField;
			rs.close();
			rs = null;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				connection.close();
				connection = null;
			} catch (Exception e2) {
			}
		}
		return modelJavaContent;
	}

	/**
	 * 
	 * @param connection
	 * @param tableName
	 * @return String[][] columnArr:columnName,typeName,comment,primaryKey
	 */
	public static List<Column> getColumnList(Connection connection, String tableName) {
		List<Column> columnList = new ArrayList<Column>();
		try {
			DatabaseMetaData databaseMetaData = connection.getMetaData();
			ResultSet rs = databaseMetaData.getPrimaryKeys(null, null, tableName);

			String primaryKeyColumnName = "";
			while (rs.next()) {
				primaryKeyColumnName = rs.getString("column_name");
			}
			rs.close();
			rs = null;

			rs = databaseMetaData.getColumns(null, "%", tableName, "%");
			while (rs.next()) {
				String columnName = rs.getString("COLUMN_NAME");
				if (columnName == null) {
					continue;
				}
				Column column = new Column();
				column.setColumnName(columnName);
				column.setTypeName(rs.getString("TYPE_NAME"));
				column.setComment(rs.getString("REMARKS"));
				if (columnName.equals(primaryKeyColumnName)) {
					column.setPrimaryKey("1");
				} else {
					column.setPrimaryKey("0");
				}
				columnList.add(column);
			}

			rs.close();
			rs = null;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				connection.close();
				connection = null;
			} catch (Exception e2) {
			}
		}
		return columnList;
	}

	public static String mybatisXmlResultMap(Connection connection, String tableName) {
		String result = "";
		String primaryKeyColumnName = "";
		try {
			DatabaseMetaData databaseMetaData = connection.getMetaData();
			ResultSet rs = databaseMetaData.getPrimaryKeys(null, null, tableName);

			while (rs.next()) {
				primaryKeyColumnName = rs.getString("column_name");
			}
			rs.close();
			rs = null;

			rs = databaseMetaData.getColumns(null, "%", tableName, "%");
			String modelJavaField = "";
			while (rs.next()) {
				String columnName = rs.getString("COLUMN_NAME");
				if (columnName == null) {
					continue;
				}
				modelJavaField = modelJavaField + "<" + (columnName.equals(primaryKeyColumnName) ? "id" : "result") + " property=\"" + StringUtils.toClassPropertyName(columnName) + "\" column=\"" + columnName + "\"/>\r\n";
			}
			result = result + modelJavaField;
			rs.close();
			rs = null;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				connection.close();
				connection = null;
			} catch (Exception e2) {
			}
		}
		return result;
	}

	public static String selectItem(Connection connection, String tableName) {
		String result = "";
		try {
			DatabaseMetaData databaseMetaData = connection.getMetaData();
			ResultSet rs = databaseMetaData.getColumns(null, "%", tableName, "%");
			while (rs.next()) {
				String columnName = rs.getString("COLUMN_NAME");
				if (columnName == null) {
					continue;
				}
				result = result + "," + columnName;
			}
			rs.close();
			rs = null;
			return result.substring(1);
		} catch (Exception e) {
			System.err.println("selectItem(Connection connection, String tableName) tableName=" + tableName + " result=" + result);

			e.printStackTrace();
		} finally {
			try {
				connection.close();
				connection = null;
			} catch (Exception e2) {
			}
		}
		return "";
	}

	public static void beanFieldProperties(Connection connection, String tableName, String modelClassName) {
		try {
			DatabaseMetaData databaseMetaData = connection.getMetaData();
			ResultSet rs = databaseMetaData.getColumns(null, "%", tableName, "%");
			String result = "";
			while (rs.next()) {
				String columnName = rs.getString("COLUMN_NAME");
				String comment = rs.getString("REMARKS");
				if (columnName == null || columnName.equalsIgnoreCase("update_object") || columnName.equalsIgnoreCase("create_object")) {
					continue;
				}
				result = result + modelClassName + "." + StringUtils.toClassPropertyName(columnName) + "=" + (comment != null ? comment : "") + "\r\n";
			}
			//System.out.println(result);
			rs.close();
			rs = null;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				connection.close();
				connection = null;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static String tableToClassName(String tableName, String filterStr){
		if(StringUtils.isEmpty(tableName)){
			throw new MCLException("table is empty");
		}
		tableName = tableName.toLowerCase();
		if(StringUtils.isEmpty(filterStr)){
			return StringUtils.toClassName(tableName);
		}
		return StringUtils.toClassName(tableName.replace(filterStr, ""));
	}
	
	public static void main(String[] args) throws Exception {
		String eclipseWorkspaceProjectPath = PathUtil.getEclipseWorkspaceProjectPath("star-admins");

		Connection connection = null;
		String modelPackage = "com.test";
		String daoPackage = "com.test";
		String daoImplPackage = "com.test";
		String servicePackage = "com.test";
		String serviceImplPackage = "com.test";
		String tableNames = "xds_boxer_starwish_info";
		/*String tableNames = "xds_acc_exch_dtl_lc_map,xds_account_exchange_detail,xds_accservice_op_log,xds_biz_domain,"
				+ "xds_biz_domain_value,xds_boxer_info,xds_boxer_starwish_info,xds_content_received_info,"
				+ "xds_ent_checker_info,xds_ent_info,xds_ent_recharge_rec,xds_home_place_title,xds_invoice_info"
				+ ",xds_lc_info,xds_place,xds_place_background,xds_place_batch,xds_place_batch_dist,xds_place_comment"
				+ ",xds_place_share,xds_place_visit_rec,xds_ronge_app_download,xds_starwish,xds_starwish_content"
				+ ",xds_starwish_cycle_info,xds_starwish_lc_map,xds_starwish_package,xds_starwish_received_info"
				+ ",xds_starwish_received_list,xds_starwish_rwd,xds_starwish_sec,xds_starwish_sensitive_word,xds_starwish_stamp"
				+ ",xds_user_info,xds_user_login_rec,xds_user_recharge_rec,xds_useronline";*/
		String filterStr = "xds_";
		// model xml daoImpl dao serviceImpl service
		boolean[] fileBooleanArr = new boolean[] { true, true, true, true, false, false };
		String[] tableNamesArr = tableNames.split(",");
		String tableName = "";
		String mapperXmlContent = "";
		for (int i = 0; i < tableNamesArr.length; i++) {
			tableName = tableNamesArr[i].trim();
			connection = getConnection();
			String modelJavaContent = "package " + modelPackage + ";\r\n";
			modelJavaContent = modelJavaContent + "import java.io.Serializable;\r\n\r\n";

			modelJavaContent = modelJavaContent + "public class " + tableToClassName(tableName,filterStr) + " implements Serializable {\r\n\r\n";
			modelJavaContent = modelJavaContent + "private static final long serialVersionUID = 19700101000000000L;\r\n";
			modelJavaContent = modelJavaContent + getModelJava(connection, tableName);
			modelJavaContent = modelJavaContent + "\r\n}\r\n";
			if (fileBooleanArr[0]) {
				FileUtil.createFile(eclipseWorkspaceProjectPath + "/src/main/java/" + modelPackage.replace(".", "/") + "/" + tableToClassName(tableName,filterStr) + ".java", modelJavaContent, WebConstant.CHARSET_UTF8);
			}
			// //////////////////////////////////////////////////
			connection = getConnection();
			List<Column> columnList = getColumnList(connection, tableName);
			int columnListSize = (columnList != null ? columnList.size() : 0);
			String primaryKeyType = getPropertyType(columnList.get(0).getTypeName(), true, 3);

			String columnNames = ",";
			for (int j = 0; j < columnListSize; j++) {
				columnNames = columnNames + columnList.get(j).getColumnName() + ",";
			}

			connection = getConnection();
			mapperXmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n";
			mapperXmlContent = mapperXmlContent + "<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\"\r\n";
			mapperXmlContent = mapperXmlContent + "\"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\r\n\r\n";
			mapperXmlContent = mapperXmlContent + "<mapper namespace=\"" + daoPackage + "." + tableToClassName(tableName,filterStr) + "Dao\">\r\n";
			mapperXmlContent = mapperXmlContent + "<resultMap id=\"baseResultMap\" type=\"" + tableToClassName(tableName,filterStr) + "\">";
			mapperXmlContent = mapperXmlContent + mybatisXmlResultMap(connection, tableName);
			mapperXmlContent = mapperXmlContent + "</resultMap>\r\n\r\n";

			// //////////////////////////////////////////////////
			connection = getConnection();
			mapperXmlContent = mapperXmlContent + "<sql id=\"baseColumns\">";
			mapperXmlContent = mapperXmlContent + selectItem(connection, tableName);
			mapperXmlContent = mapperXmlContent + "</sql>\r\n\r\n";

			mapperXmlContent = mapperXmlContent + "<select id=\"get\" statementType=\"PREPARED\" parameterType=\"java.lang." + primaryKeyType + "\" resultMap=\"baseResultMap\">\r\n";
			mapperXmlContent = mapperXmlContent + "SELECT <include refid=\"baseColumns\" /> FROM " + tableName + " WHERE " + columnList.get(0).getColumnName() + "=#{" + columnList.get(0).getColumnName() + "}";
			mapperXmlContent = mapperXmlContent + "</select>\r\n\r\n";

			// //////////////////////////////////////////////////
			mapperXmlContent = mapperXmlContent + "<insert id=\"add\" statementType=\"PREPARED\" parameterType=\"" + tableToClassName(tableName,filterStr) + "\">\r\n";
			mapperXmlContent = mapperXmlContent + "<selectKey keyProperty=\"" + columnList.get(0).getColumnName() + "\" order=\"AFTER\" resultType=\"java.lang." + primaryKeyType + "\">\r\n";
			mapperXmlContent = mapperXmlContent + "SELECT LAST_INSERT_ID()";
			mapperXmlContent = mapperXmlContent + "</selectKey>\r\n";

			mapperXmlContent = mapperXmlContent + "INSERT INTO " + tableName + "(";
			for (int j = 1; j < columnListSize; j++) {
				mapperXmlContent = mapperXmlContent + (j == 1 ? "" : ",") + columnList.get(j).getColumnName();
			}
			mapperXmlContent = mapperXmlContent + ") VALUES(";
			for (int j = 1; j < columnListSize; j++) {

				mapperXmlContent = mapperXmlContent + (j == 1 ? "" : ",") + "#{" + StringUtils.toClassPropertyName(columnList.get(j).getColumnName()) + "}";
			}
			mapperXmlContent = mapperXmlContent + ")";
			mapperXmlContent = mapperXmlContent + "</insert>\r\n\r\n";

			// //////////////////////////////////////////////////
			mapperXmlContent = mapperXmlContent + "<insert id=\"addSelective\" statementType=\"PREPARED\" parameterType=\"" + tableToClassName(tableName,filterStr) + "\">\r\n";
			mapperXmlContent = mapperXmlContent + "<selectKey keyProperty=\"" + columnList.get(0).getColumnName() + "\" order=\"AFTER\" resultType=\"java.lang." + primaryKeyType + "\">\r\n";
			mapperXmlContent = mapperXmlContent + "SELECT LAST_INSERT_ID()";
			mapperXmlContent = mapperXmlContent + "</selectKey>";

			mapperXmlContent = mapperXmlContent + "INSERT INTO " + tableName + "(";

			for (int j = 1; j < columnListSize; j++) {
				if ("CREATE_DATE".equals(columnList.get(j).getColumnName()) || "UPDATE_DATE".equals(columnList.get(j).getColumnName())) {
					continue;
				}
				mapperXmlContent = mapperXmlContent + "<if test=\"" + StringUtils.toClassPropertyName(columnList.get(j).getColumnName()) + " != null\">" + columnList.get(j).getColumnName() + ",</if>\r\n";
			}
			mapperXmlContent = mapperXmlContent + "CREATE_DATE";
			if (columnNames.indexOf("UPDATE_DATE") > -1) {
				mapperXmlContent = mapperXmlContent + ",UPDATE_DATE";
			}

			mapperXmlContent = mapperXmlContent + ") VALUES(";

			for (int j = 1; j < columnListSize; j++) {
				if ("CREATE_DATE".equals(columnList.get(j).getColumnName()) || "UPDATE_DATE".equals(columnList.get(j).getColumnName())) {
					continue;
				}
				mapperXmlContent = mapperXmlContent + "<if test=\"" + StringUtils.toClassPropertyName(columnList.get(j).getColumnName()) + " != null\">#{" + StringUtils.toClassPropertyName(columnList.get(j).getColumnName()) + "},</if>\r\n";
			}

			mapperXmlContent = mapperXmlContent + "#{createDate}";
			if (columnNames.indexOf("UPDATE_DATE") > -1) {
				mapperXmlContent = mapperXmlContent + ",#{updateDate}";
			}

			mapperXmlContent = mapperXmlContent + ")";
			mapperXmlContent = mapperXmlContent + "</insert>\r\n\r\n";

			// //////////////////////////////////////////////////
			mapperXmlContent = mapperXmlContent + "<update id=\"update\" statementType=\"PREPARED\" parameterType=\"" + tableToClassName(tableName,filterStr) + "\">\r\n";
			mapperXmlContent = mapperXmlContent + "UPDATE " + tableName + " SET ";
			for (int j = 1; j < columnListSize; j++) {
				if ("CREATE_DATE".equals(columnList.get(j).getColumnName()) || "UPDATE_DATE".equals(columnList.get(j).getColumnName()) || "version".equals(columnList.get(j).getColumnName())) {
					continue;
				}
				mapperXmlContent = mapperXmlContent + (j == 1 ? "" : ",") + columnList.get(j).getColumnName() + "=#{" + StringUtils.toClassPropertyName(columnList.get(j).getColumnName()) + "}";
			}

			if (columnNames.indexOf("UPDATE_DATE") > -1) {
				mapperXmlContent = mapperXmlContent + ",UPDATE_DATE=#{updateDate}";
			}
			if (columnNames.indexOf("version") > -1) {
				mapperXmlContent = mapperXmlContent + ",version=(version+1)";
			}

			mapperXmlContent = mapperXmlContent + "\r\nWHERE " + columnList.get(0).getColumnName() + "=#{" + StringUtils.toClassPropertyName(columnList.get(0).getColumnName()) + "}";
			if (columnNames.indexOf("version") > -1) {
				mapperXmlContent = mapperXmlContent + " AND version=#{version}";
			}
			mapperXmlContent = mapperXmlContent + "\r\n</update>\r\n\r\n";

			// //////////////////////////////////////////////////
			mapperXmlContent = mapperXmlContent + "<update id=\"updateSelective\" statementType=\"PREPARED\" parameterType=\"" + tableToClassName(tableName,filterStr) + "\">\r\n";
			mapperXmlContent = mapperXmlContent + "UPDATE " + tableName + "\r\n";
			mapperXmlContent = mapperXmlContent + "<set>\r\n";
			for (int j = 1; j < columnListSize; j++) {
				if ("CREATE_DATE".equals(columnList.get(j).getColumnName()) || "UPDATE_DATE".equals(columnList.get(j).getColumnName()) || "version".equals(columnList.get(j).getColumnName())) {
					continue;
				}
				mapperXmlContent = mapperXmlContent + "<if test=\"" + StringUtils.toClassPropertyName(columnList.get(j).getColumnName()) + " != null\">" + columnList.get(j).getColumnName() + "=#{" + StringUtils.toClassPropertyName(columnList.get(j).getColumnName()) + "},</if>\r\n";
			}

			if (columnNames.indexOf("UPDATE_DATE") > -1) {
				mapperXmlContent = mapperXmlContent + "UPDATE_DATE=#{updateDate}";
			}
			if (columnNames.indexOf("version") > -1) {
				if (columnNames.indexOf("UPDATE_DATE") > -1) {
					mapperXmlContent = mapperXmlContent + ",";
				}
				mapperXmlContent = mapperXmlContent + "version=(version+1)";
			}

			mapperXmlContent = mapperXmlContent + "</set>\r\n";
			mapperXmlContent = mapperXmlContent + "WHERE " + columnList.get(0).getColumnName() + "=#{" + StringUtils.toClassPropertyName(columnList.get(0).getColumnName()) + "}";

			if (columnNames.indexOf("version") > -1) {
				mapperXmlContent = mapperXmlContent + " AND version=#{version}";
			}
			mapperXmlContent = mapperXmlContent + "\r\n";

			mapperXmlContent = mapperXmlContent + "</update>\r\n\r\n";
			// //////////////////////////////////////////////////
			mapperXmlContent = mapperXmlContent + "<delete id=\"delete\" statementType=\"PREPARED\" parameterType=\"" + tableToClassName(tableName,filterStr) + "\">\r\n";
			mapperXmlContent = mapperXmlContent + "DELETE FROM " + tableName + " WHERE " + columnList.get(0).getColumnName() + "=#{" + StringUtils.toClassPropertyName(columnList.get(0).getColumnName()) + "}";
			if (columnNames.indexOf("version") > -1) {
				mapperXmlContent = mapperXmlContent + " AND version=#{version}";
			}
			mapperXmlContent = mapperXmlContent + "\r\n";

			mapperXmlContent = mapperXmlContent + "</delete>\r\n\r\n";
			// //////////////////////////////////////////////////
			mapperXmlContent = mapperXmlContent + "<select id=\"getList\" statementType=\"PREPARED\" resultMap=\"baseResultMap\">\r\n";
			mapperXmlContent = mapperXmlContent + "SELECT <include refid=\"baseColumns\" /> FROM " + tableName + " ORDER BY update_time\r\n";
			mapperXmlContent = mapperXmlContent + "</select>\r\n";

			// //////////////////////////////////////////////////
			mapperXmlContent = mapperXmlContent + "<select id=\"getCount\" statementType=\"PREPARED\" resultType=\"Integer\">\r\n";
			mapperXmlContent = mapperXmlContent + "SELECT COUNT(*) FROM " + tableName + "\r\n";
			mapperXmlContent = mapperXmlContent + "</select>\r\n";

			mapperXmlContent = mapperXmlContent + "</mapper>";
			if (fileBooleanArr[1]) {
				FileUtil.createFile(eclipseWorkspaceProjectPath + "/src/main/resources/" + daoPackage.replace(".", "/") +"/" + tableToClassName(tableName,filterStr) + "Mapper.xml", mapperXmlContent, WebConstant.CHARSET_UTF8);
			}

			// ////////////////////////////////////////////////////////
			String daoImplContent = "package " + daoImplPackage + ";\r\n\r\n";
			daoImplContent = daoImplContent + "import java.util.Collections;\r\n";
			daoImplContent = daoImplContent + "import java.util.List;\r\n";
			daoImplContent = daoImplContent + "import com.cifpay.starframework.dao.impl.CommonDaoImpl;\r\n";
			daoImplContent = daoImplContent + "import org.springframework.stereotype.Repository;\r\n";
			daoImplContent = daoImplContent + "import " + daoPackage + "." + tableToClassName(tableName,filterStr) + "Dao;\r\n";
			daoImplContent = daoImplContent + "import " + modelPackage + "." + tableToClassName(tableName,filterStr) + ";\r\n\r\n";
			daoImplContent = daoImplContent + "@Repository(\"" + StringUtils.toFirstLowerCase(tableToClassName(tableName,filterStr)) + "Dao\")\r\n";
			daoImplContent = daoImplContent + "public class " + tableToClassName(tableName,filterStr) + "DaoImpl extends CommonDaoImpl<" + tableToClassName(tableName,filterStr) + "> implements " + tableToClassName(tableName,filterStr) + "Dao {\r\n";

			if (primaryKeyType.equals("Long")) {
				daoImplContent = daoImplContent + "@Override\r\n";
				daoImplContent = daoImplContent + "public " + tableToClassName(tableName,filterStr) + " get(long " + columnList.get(0).getColumnName() + ") {\r\n";
				daoImplContent = daoImplContent + "return this.getSqlSession().selectOne(getStatementPrefix() + \".get\", " + columnList.get(0).getColumnName() + ");\r\n";
				daoImplContent = daoImplContent + "}\r\n";
			}

			daoImplContent = daoImplContent + "@Override\r\n";
			daoImplContent = daoImplContent + "public List<" + tableToClassName(tableName,filterStr) + "> getList() {";
			daoImplContent = daoImplContent + "List<" + tableToClassName(tableName,filterStr) + "> resultList = this.getSqlSession().selectList(getStatementPrefix() + \".getList\");\r\n";
			daoImplContent = daoImplContent + "if (resultList == null) {\r\n";
			daoImplContent = daoImplContent + "resultList = Collections.emptyList();\r\n";
			daoImplContent = daoImplContent + "}\r\n";
			daoImplContent = daoImplContent + "return resultList;\r\n";
			daoImplContent = daoImplContent + "}\r\n";

			daoImplContent = daoImplContent + "@Override\r\n";
			daoImplContent = daoImplContent + "public int getCount() {\r\n";
			daoImplContent = daoImplContent + "Integer result = (Integer) this.getSqlSession().selectOne(getStatementPrefix() + \".getCount\");\r\n";
			daoImplContent = daoImplContent + "return result != null ? result.intValue() : 0;\r\n";
			daoImplContent = daoImplContent + "}\r\n";
			daoImplContent = daoImplContent + "}\r\n";
			if (fileBooleanArr[2]) {
				FileUtil.createFile(eclipseWorkspaceProjectPath + "/src/main/java/" + daoImplPackage.replace(".", "/") + "/" + tableToClassName(tableName,filterStr) + "DaoImpl.java", daoImplContent, WebConstant.CHARSET_UTF8);
			}
			// ////////////////////////////////////////////////////////
			String daoContent = "package " + daoPackage + ";\r\n\r\n";
			daoContent = daoContent + "import java.util.List;\r\n";
			daoContent = daoContent + "import com.cifpay.starframework.dao.CommonDao;\r\n";
			daoContent = daoContent + "import " + modelPackage + "." + tableToClassName(tableName,filterStr) + ";\r\n\r\n";
			daoContent = daoContent + "public interface " + tableToClassName(tableName,filterStr) + "Dao extends CommonDao<" + tableToClassName(tableName,filterStr) + "> {\r\n";
			if (primaryKeyType.equals("Long")) {
				daoContent = daoContent + "public " + tableToClassName(tableName,filterStr) + " get(long " + columnList.get(0).getColumnName() + ");\r\n";
			}
			daoContent = daoContent + "public List<" + tableToClassName(tableName,filterStr) + "> getList();\r\n";
			daoContent = daoContent + "public int getCount();\r\n";
			daoContent = daoContent + "}\r\n";
			if (fileBooleanArr[3]) {
				FileUtil.createFile(eclipseWorkspaceProjectPath + "/src/main/java/" + daoPackage.replace(".", "/") + "/" + tableToClassName(tableName,filterStr) + "Dao.java", daoContent, WebConstant.CHARSET_UTF8);
			}
			// ////////////////////////////////////////////////////////
			String serviceImplContent = "package " + serviceImplPackage + ";\r\n\r\n";
			serviceImplContent = serviceImplContent + "import java.util.List;\r\n";
			serviceImplContent = serviceImplContent + "import org.springframework.beans.factory.annotation.Autowired;\r\n";
			serviceImplContent = serviceImplContent + "import org.springframework.stereotype.Service;\r\n";
			serviceImplContent = serviceImplContent + "import " + daoPackage + "." + tableToClassName(tableName,filterStr) + "Dao;\r\n";
			serviceImplContent = serviceImplContent + "import " + modelPackage + "." + tableToClassName(tableName,filterStr) + ";\r\n";
			serviceImplContent = serviceImplContent + "import " + servicePackage + "." + tableToClassName(tableName,filterStr) + "Service;\r\n";
			serviceImplContent = serviceImplContent + "import com.cifpay.starframework.model.ServiceResult;\r\n";

			serviceImplContent = serviceImplContent + "@Service(\"" + StringUtils.toFirstLowerCase(tableToClassName(tableName,filterStr)) + "Service\")\r\n";
			serviceImplContent = serviceImplContent + "public class " + tableToClassName(tableName,filterStr) + "ServiceImpl implements " + tableToClassName(tableName,filterStr) + "Service {\r\n";

			serviceImplContent = serviceImplContent + "@Autowired\r\n";
			serviceImplContent = serviceImplContent + "private " + tableToClassName(tableName,filterStr) + "Dao " + StringUtils.toFirstLowerCase(tableToClassName(tableName,filterStr)) + "Dao;\r\n";

			serviceImplContent = serviceImplContent + "@Override\r\n";
			serviceImplContent = serviceImplContent + "public " + tableToClassName(tableName,filterStr) + " get(" + (primaryKeyType.equals("Long") ? "long" : "int") + " " + columnList.get(0).getColumnName() + ") {\r\n";
			serviceImplContent = serviceImplContent + "return " + StringUtils.toFirstLowerCase(tableToClassName(tableName,filterStr)) + "Dao.get(" + columnList.get(0).getColumnName() + ");\r\n";
			serviceImplContent = serviceImplContent + "}\r\n";

			serviceImplContent = serviceImplContent + "@Override\r\n";
			serviceImplContent = serviceImplContent + "public ServiceResult<String> add(" + tableToClassName(tableName,filterStr) + " " + StringUtils.toFirstLowerCase(tableToClassName(tableName,filterStr)) + ") {\r\n";
			serviceImplContent = serviceImplContent + "ServiceResult<String> serviceResult = new ServiceResult<String>();\r\n";
			serviceImplContent = serviceImplContent + "int result = " + StringUtils.toFirstLowerCase(tableToClassName(tableName,filterStr)) + "Dao.add(" + StringUtils.toFirstLowerCase(tableToClassName(tableName,filterStr)) + ");\r\n";
			serviceImplContent = serviceImplContent + "if (result == 1) {\r\n";
			serviceImplContent = serviceImplContent + "	serviceResult.setCode(resultCode.get(\"common.sucess\"));\r\n";
			serviceImplContent = serviceImplContent + "} else {\r\n";
			serviceImplContent = serviceImplContent + " serviceResult.setCode(resultCode.get(\"common.fail\"));\r\n";
			serviceImplContent = serviceImplContent + "}\r\n";
			serviceImplContent = serviceImplContent + "return serviceResult;\r\n";
			serviceImplContent = serviceImplContent + "}\r\n";

			serviceImplContent = serviceImplContent + "@Override\r\n";
			serviceImplContent = serviceImplContent + "public ServiceResult<String> addSelective(" + tableToClassName(tableName,filterStr) + " " + StringUtils.toFirstLowerCase(tableToClassName(tableName,filterStr)) + ") {\r\n";
			serviceImplContent = serviceImplContent + "ServiceResult<String> serviceResult = new ServiceResult<String>();\r\n";
			serviceImplContent = serviceImplContent + "int result = " + StringUtils.toFirstLowerCase(tableToClassName(tableName,filterStr)) + "Dao.addSelective(" + StringUtils.toFirstLowerCase(tableToClassName(tableName,filterStr)) + ");\r\n";
			serviceImplContent = serviceImplContent + "if (result == 1) {\r\n";
			serviceImplContent = serviceImplContent + "	serviceResult.setCode(resultCode.get(\"common.sucess\"));\r\n";
			serviceImplContent = serviceImplContent + "} else {\r\n";
			serviceImplContent = serviceImplContent + " serviceResult.setCode(resultCode.get(\"common.fail\"));\r\n";
			serviceImplContent = serviceImplContent + "}\r\n";
			serviceImplContent = serviceImplContent + "return serviceResult;\r\n";
			serviceImplContent = serviceImplContent + "}\r\n";

			serviceImplContent = serviceImplContent + "@Override\r\n";
			serviceImplContent = serviceImplContent + "public ServiceResult<String> update(" + tableToClassName(tableName,filterStr) + " " + StringUtils.toFirstLowerCase(tableToClassName(tableName,filterStr)) + ") {\r\n";
			serviceImplContent = serviceImplContent + "ServiceResult<String> serviceResult = new ServiceResult<String>();\r\n";
			serviceImplContent = serviceImplContent + "int result = " + StringUtils.toFirstLowerCase(tableToClassName(tableName,filterStr)) + "Dao.update(" + StringUtils.toFirstLowerCase(tableToClassName(tableName,filterStr)) + ");\r\n";
			serviceImplContent = serviceImplContent + "if (result == 1) {\r\n";
			serviceImplContent = serviceImplContent + "	serviceResult.setCode(resultCode.get(\"common.sucess\"));\r\n";
			serviceImplContent = serviceImplContent + "} else {\r\n";
			serviceImplContent = serviceImplContent + " serviceResult.setCode(resultCode.get(\"common.fail\"));\r\n";
			serviceImplContent = serviceImplContent + "}\r\n";
			serviceImplContent = serviceImplContent + "return serviceResult;\r\n";
			serviceImplContent = serviceImplContent + "}\r\n";

			serviceImplContent = serviceImplContent + "@Override\r\n";
			serviceImplContent = serviceImplContent + "public ServiceResult<String> updateSelective(" + tableToClassName(tableName,filterStr) + " " + StringUtils.toFirstLowerCase(tableToClassName(tableName,filterStr)) + ") {\r\n";
			serviceImplContent = serviceImplContent + "ServiceResult<String> serviceResult = new ServiceResult<String>();\r\n";
			serviceImplContent = serviceImplContent + "int result = " + StringUtils.toFirstLowerCase(tableToClassName(tableName,filterStr)) + "Dao.updateSelective(" + StringUtils.toFirstLowerCase(tableToClassName(tableName,filterStr)) + ");\r\n";
			serviceImplContent = serviceImplContent + "if (result == 1) {\r\n";
			serviceImplContent = serviceImplContent + "	serviceResult.setCode(resultCode.get(\"common.sucess\"));\r\n";
			serviceImplContent = serviceImplContent + "} else {\r\n";
			serviceImplContent = serviceImplContent + " serviceResult.setCode(resultCode.get(\"common.fail\"));\r\n";
			serviceImplContent = serviceImplContent + "}\r\n";
			serviceImplContent = serviceImplContent + "return serviceResult;\r\n";
			serviceImplContent = serviceImplContent + "}\r\n";

			serviceImplContent = serviceImplContent + "@Override\r\n";
			serviceImplContent = serviceImplContent + "public ServiceResult<String> delete(" + tableToClassName(tableName,filterStr) + " " + StringUtils.toFirstLowerCase(tableToClassName(tableName,filterStr)) + ") {\r\n";
			serviceImplContent = serviceImplContent + "ServiceResult<String> serviceResult = new ServiceResult<String>();\r\n";
			serviceImplContent = serviceImplContent + "int result = " + StringUtils.toFirstLowerCase(tableToClassName(tableName,filterStr)) + "Dao.delete(" + StringUtils.toFirstLowerCase(tableToClassName(tableName,filterStr)) + ");\r\n";
			serviceImplContent = serviceImplContent + "if (result == 1) {\r\n";
			serviceImplContent = serviceImplContent + "	serviceResult.setCode(resultCode.get(\"common.sucess\"));\r\n";
			serviceImplContent = serviceImplContent + "} else {\r\n";
			serviceImplContent = serviceImplContent + " serviceResult.setCode(resultCode.get(\"common.fail\"));\r\n";
			serviceImplContent = serviceImplContent + "}\r\n";
			serviceImplContent = serviceImplContent + "return serviceResult;\r\n";
			serviceImplContent = serviceImplContent + "}\r\n";

			serviceImplContent = serviceImplContent + "@Override\r\n";
			serviceImplContent = serviceImplContent + "public List<" + tableToClassName(tableName,filterStr) + "> getList() {\r\n";
			serviceImplContent = serviceImplContent + "return " + StringUtils.toFirstLowerCase(tableToClassName(tableName,filterStr)) + "Dao.getList();\r\n";
			serviceImplContent = serviceImplContent + "}\r\n";

			serviceImplContent = serviceImplContent + "@Override\r\n";
			serviceImplContent = serviceImplContent + "public int getCount() {\r\n";
			serviceImplContent = serviceImplContent + "return " + StringUtils.toFirstLowerCase(tableToClassName(tableName,filterStr)) + "Dao.getCount();\r\n";
			serviceImplContent = serviceImplContent + "}\r\n";
			serviceImplContent = serviceImplContent + "}\r\n";
			if (fileBooleanArr[4]) {
				FileUtil.createFile(eclipseWorkspaceProjectPath + "/src/main/java/" + serviceImplPackage.replace(".", "/") + "/" + tableToClassName(tableName,filterStr) + "ServiceImpl.java", serviceImplContent, WebConstant.CHARSET_UTF8);
			}
			// ////////////////////////////////////////////////
			String serviceContent = "package " + servicePackage + ";\r\n\r\n";
			serviceContent = serviceContent + "import java.util.List;\r\n";
			serviceContent = serviceContent + "import com.cifpay.starframework.service.CommonService;\r\n";
			serviceContent = serviceContent + "import " + modelPackage + "." + tableToClassName(tableName,filterStr) + ";\r\n\r\n";
			serviceContent = serviceContent + "public interface " + tableToClassName(tableName,filterStr) + "Service extends CommonService<" + tableToClassName(tableName,filterStr) + "> {\r\n";
			serviceContent = serviceContent + "public " + tableToClassName(tableName,filterStr) + " get(" + (primaryKeyType.equals("Long") ? "long" : "int") + " " + columnList.get(0).getColumnName() + ");\r\n";
			serviceContent = serviceContent + "public List<" + tableToClassName(tableName,filterStr) + "> getList();\r\n";
			serviceContent = serviceContent + "public int getCount();\r\n";
			serviceContent = serviceContent + "}\r\n";
			if (fileBooleanArr[5]) {
				FileUtil.createFile(eclipseWorkspaceProjectPath + "/src/main/java/" + servicePackage.replace(".", "/") + "/" + tableToClassName(tableName,filterStr) + "Service.java", serviceContent, WebConstant.CHARSET_UTF8);
			}
			////System.out.println("tableNamesArr[" + i + "]=" + tableNamesArr[i] + " end\r\n\r\n");
		}
	}
}
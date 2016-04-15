package com.common.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;


/**
 * properties配置文件工具类
 * @Title:    PropertiesUtil
 * Company:   cifpay
 * Copyright: Copyright(C) 2013
 * @Version   1.0
 * @author    chenbin
 * @date:     2015年6月3日 
 * @time:     上午11:17:14
 * @Description:
 */
public class PropertiesUtil {
	private static final Logger LOG = LogManager.getLogger(PropertiesUtil.class);

	private PropertiesUtil() {
	}

	public static Properties getProperties(String filePath) {
		Properties properties = new Properties();
		InputStreamReader inputStream = null;
		try {
			inputStream = new InputStreamReader(new FileInputStream(filePath), WebConstant.CHARSET_UTF8);
			properties.load(inputStream);
			return properties;
		} catch (Exception e) {
			LOG.error(e, e);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
					inputStream = null;
				} catch (Exception e) {
					LOG.error(e.getMessage(), e);
				}
			}
		}
		return null;
	}

	public static void save(Properties propertie, String filePath, String description) {
		if (propertie == null) {
			throw new NullPointerException("propertie == null");
		}
		OutputStreamWriter outputStream = null;
		try {
			outputStream = new OutputStreamWriter(new FileOutputStream(filePath), WebConstant.CHARSET_UTF8);
			propertie.store(outputStream, description);
		} catch (Exception e) {
			throw new MCLException(e.getMessage(), e);
		} finally {
			if (outputStream != null) {
				try {
					outputStream.close();
					outputStream = null;
				} catch (Exception e) {
					LOG.error(e.getMessage(), e);
				}
			}
		}
	}
}

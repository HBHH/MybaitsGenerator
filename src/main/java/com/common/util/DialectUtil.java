package com.common.util;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DialectUtil {
	private static final Logger LOG = LoggerFactory.getLogger(DialectUtil.class);

	private static final Map<String, Dialect> DIALECT_MAP = new ConcurrentHashMap<String, Dialect>();
	private static Dialect dialect = null;

	static {
		DIALECT_MAP.put("mysql", new Mysql5Dialect());
		DIALECT_MAP.put("oracle", new OracleDialect());
		if (LOG.isInfoEnabled()) {
			Iterator<Entry<String, Dialect>> iterator = DIALECT_MAP.entrySet().iterator();
			LOG.info("DIALECT_MAP:");
			while (iterator.hasNext()) {
				Entry<String, Dialect> entry = iterator.next();
				LOG.info("key=" + entry.getKey() + " value=" + entry.getValue().getClass().getName());
			}
		}
	}

	private DialectUtil() {
	}

	public static int getPageOffset(int pageNo, int pageSize) {
		if (pageNo < 1) {
			pageNo = 1;
		}
		if (pageSize < 1) {
			pageSize = 10;
		}
		if (pageSize > Constant.PAGE_SIZE_MAX) {
			pageSize = Constant.PAGE_SIZE_MAX;
		}
		int offset = (pageNo - 1) * pageSize;
		return offset;
	}

	public static Dialect getDialect() {
		if (dialect == null) {
			if (Constant.DATABASE_DRIVER_CLASS_NAME.toLowerCase().indexOf("mysql") > -1) {
				dialect = DIALECT_MAP.get("mysql");
			} else if (Constant.DATABASE_DRIVER_CLASS_NAME.toLowerCase().indexOf("oracle") > -1) {
				dialect = DIALECT_MAP.get("oracle");
			} else {
				dialect = new Mysql5Dialect();
			}
		}
		return dialect;
	}
}
package com.common.util;

import java.util.List;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.session.Configuration;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class Mysql5Dialect extends Dialect {
	private static final Logger LOG = LogManager.getLogger(Mysql5Dialect.class);

	@Override
	public String getBoundSqlForPage(String sql) {
		return sql + " LIMIT ?,?";
	}

	@Override
	public void addParameterToBoundSql(Configuration configuration, BoundSql boundSql, int offset, int limit) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("addParameterToBoundSql(Configuration configuration:" + configuration + ", BoundSql boundSql:" + boundSql + ", int offset:" + offset + ", int limit:" + limit + ") start");
		}
		if (configuration == null || boundSql == null) {
			return;
		}
		List<ParameterMapping> parameterMappingsList = boundSql.getParameterMappings();
		ParameterMapping offsetParameterMapping = new ParameterMapping.Builder(configuration, "offset", Integer.class).build();
		parameterMappingsList.add(offsetParameterMapping);
		ParameterMapping limitParameterMapping = new ParameterMapping.Builder(configuration, "limit", Integer.class).build();
		parameterMappingsList.add(limitParameterMapping);

		if (LOG.isDebugEnabled()) {
			LOG.debug("parameterMappingsList=" + parameterMappingsList);
		}
		/*
		 * @SuppressWarnings("unchecked") Map<String, Object> parameterObjectMap
		 * = (Map<String, Object>) boundSql.getParameterObject();
		 * parameterObjectMap.put("offset", offset);
		 * parameterObjectMap.put("limit", limit); if (LOG.isDebugEnabled()) {
		 * LOG.debug("parameterObjectMap=" + parameterObjectMap); }
		 */
		boundSql.setAdditionalParameter("offset", offset);
		boundSql.setAdditionalParameter("limit", limit);
		if (LOG.isDebugEnabled()) {
			LOG.debug("addParameterToBoundSql end");
		}
	}
}
package com.common.util;

import java.util.List;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.session.Configuration;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class OracleDialect extends Dialect {
	private static final Logger LOG = LogManager.getLogger(OracleDialect.class);

	@Override
	public String getBoundSqlForPage(String sql) {
		// SELECT * FROM (
		// SELECT rownum rownum_ FROM (
		// SELECT * FROM t_crane_info ORDER BY crane_id
		// ) where rownum<=?
		// ) WHERE rownum_ >?
		StringBuffer sqlSb = new StringBuffer("SELECT * FROM ( SELECT  rownum rownum_,a.* FROM (");
		sqlSb.append(sql);
		sqlSb.append(" ) a WHERE rownum<=? ) WHERE rownum_ >?");
		return sqlSb.toString();
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
		ParameterMapping limitParameterMapping = new ParameterMapping.Builder(configuration, "limit", Integer.class).build();
		parameterMappingsList.add(limitParameterMapping);
		ParameterMapping offsetParameterMapping = new ParameterMapping.Builder(configuration, "offset", Integer.class).build();
		parameterMappingsList.add(offsetParameterMapping);
		boundSql.setAdditionalParameter("offset", offset);
		boundSql.setAdditionalParameter("limit", limit);
		if (LOG.isDebugEnabled()) {
			LOG.debug("addParameterToBoundSql end");
		}
	}
}
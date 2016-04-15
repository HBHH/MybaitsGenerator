package com.common.util.dao;

import java.util.List;
import java.util.Map;

public interface CommonDao<T> {
	public T get(int id);

	public int add(T t);

	public int addSelective(T t);

	public int update(T t);

	public int updateSelective(T t);

	public int delete(T t);

	public List<T> getList(Map<String, Object> map, int pageNo, int pageSize);

	public int getCount(Map<String, Object> map);
	
	public List<T> getObjectListBySelectSqlId(Map<String, Object> map, int pageNo, int pageSize,String selectSqlId);

	public int getObjectCountBySelecSqltId(Map<String, Object> map,String selectSqlId);
}

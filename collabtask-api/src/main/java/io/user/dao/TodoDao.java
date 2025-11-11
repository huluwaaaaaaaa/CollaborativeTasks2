/**
 * Copyright (c) 2018 COLLAB-TASK All rights reserved.
 *
 * https://www.collabtask.io
 *
 * 版权所有，侵权必究！
 */

package io.user.dao;

import io.user.common.dao.BaseDao;
import io.user.entity.TodoEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * TODO DAO
 * 
 * @author System
 */
@Mapper
public interface TodoDao extends BaseDao<TodoEntity> {
	
	/**
	 * 查询我的TODO列表（包含共享给我的）
	 * 
	 * v1.1优化：支持查询共享的TODO
	 */
	List<TodoEntity> selectMyTodosPage(
		@Param("userId") Long userId,
		@Param("keyword") String keyword,
		@Param("status") String status,
		@Param("priority") String priority,
		@Param("teamId") Long teamId,
		@Param("dueDateStart") Date dueDateStart,
		@Param("dueDateEnd") Date dueDateEnd,
		@Param("orderBy") String orderBy,
		@Param("orderDirection") String orderDirection,
		@Param("offset") Long offset,
		@Param("limit") Integer limit
	);
	
	/**
	 * 统计我的TODO数量（包含共享的）
	 */
	Long countMyTodos(
		@Param("userId") Long userId,
		@Param("keyword") String keyword,
		@Param("status") String status,
		@Param("priority") String priority,
		@Param("teamId") Long teamId,
		@Param("dueDateStart") Date dueDateStart,
		@Param("dueDateEnd") Date dueDateEnd
	);
}


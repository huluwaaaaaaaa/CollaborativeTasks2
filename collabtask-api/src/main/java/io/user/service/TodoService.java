/**
 * Copyright (c) 2018 COLLAB-TASK All rights reserved.
 *
 * https://www.collabtask.io
 *
 * 版权所有，侵权必究！
 */

package io.user.service;

import io.user.common.service.BaseService;
import io.user.common.page.PageData;
import io.user.dto.TodoCreateDTO;
import io.user.dto.TodoQueryDTO;
import io.user.dto.TodoUpdateDTO;
import io.user.dto.TodoVO;
import io.user.entity.TodoEntity;

/**
 * TODO Service
 * 
 * @author System
 */
public interface TodoService extends BaseService<TodoEntity> {

	/**
	 * 创建 TODO
	 * @param dto 创建请求
	 * @param userId 创建者ID
	 * @return TodoVO
	 */
	TodoVO createTodo(TodoCreateDTO dto, Long userId);
	
	/**
	 * 获取 TODO 列表（分页、筛选、排序）
	 * @param dto 查询条件
	 * @param userId 当前用户ID
	 * @return 分页数据
	 */
	PageData<TodoVO> getTodoList(TodoQueryDTO dto, Long userId);
	
	/**
	 * 获取 TODO 详情
	 * @param id TODO ID
	 * @param userId 当前用户ID
	 * @return TodoVO
	 */
	TodoVO getTodoById(Long id, Long userId);
	
	/**
	 * 更新 TODO
	 * @param id TODO ID
	 * @param dto 更新请求
	 * @param userId 当前用户ID
	 * @return TodoVO
	 */
	TodoVO updateTodo(Long id, TodoUpdateDTO dto, Long userId);
	
	/**
	 * 完成 TODO
	 * @param id TODO ID
	 * @param userId 当前用户ID
	 * @return TodoVO
	 */
	TodoVO completeTodo(Long id, Long userId);
	
	/**
	 * 删除 TODO
	 * @param id TODO ID
	 * @param userId 当前用户ID
	 */
	void deleteTodo(Long id, Long userId);

}


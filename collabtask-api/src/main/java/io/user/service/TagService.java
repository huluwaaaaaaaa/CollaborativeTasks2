/**
 * Copyright (c) 2018 COLLAB-TASK All rights reserved.
 *
 * https://www.collabtask.io
 *
 * 版权所有，侵权必究！
 */

package io.user.service;

import io.user.dto.*;

import java.util.List;

/**
 * 标签管理 Service
 *
 * @author System
 */
public interface TagService {
	
	/**
	 * 创建标签
	 */
	TagVO createTag(TagCreateDTO dto, Long userId);
	
	/**
	 * 获取我的标签列表
	 */
	List<TagVO> getMyTags(Long userId);
	
	/**
	 * 获取标签详情
	 */
	TagVO getTagById(Long id, Long userId);
	
	/**
	 * 更新标签
	 */
	TagVO updateTag(Long id, TagUpdateDTO dto, Long userId);
	
	/**
	 * 删除标签
	 */
	void deleteTag(Long id, Long userId);
	
	/**
	 * 给 TODO 添加标签
	 */
	void addTagToTodo(Long todoId, Long tagId, Long userId);
	
	/**
	 * 从 TODO 移除标签
	 */
	void removeTagFromTodo(Long todoId, Long tagId, Long userId);
	
	/**
	 * 获取 TODO 的所有标签
	 */
	List<TagVO> getTodoTags(Long todoId, Long userId);
}


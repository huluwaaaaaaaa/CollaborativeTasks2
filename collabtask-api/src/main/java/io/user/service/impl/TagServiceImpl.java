/**
 * Copyright (c) 2018 COLLAB-TASK All rights reserved.
 *
 * https://www.collabtask.io
 *
 * 版权所有，侵权必究！
 */

package io.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.user.common.exception.RenException;
import io.user.common.service.impl.BaseServiceImpl;
import io.user.dao.*;
import io.user.dto.*;
import io.user.entity.*;
import io.user.common.annotation.Idempotent;
import io.user.service.TagService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 标签管理 Service 实现
 *
 * @author System
 */
@Service
@AllArgsConstructor
public class TagServiceImpl extends BaseServiceImpl<TagDao, TagEntity> implements TagService {
	
	private final TagDao tagDao;
	private final TodoTagDao todoTagDao;
	private final TodoDao todoDao;
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	@Idempotent(timeout = 300)  // v1.2: 幂等性控制
	public TagVO createTag(TagCreateDTO dto, Long userId) {
		// 检查标签名是否已存在
		QueryWrapper<TagEntity> wrapper = new QueryWrapper<>();
		wrapper.eq("user_id", userId).eq("name", dto.getName());
		if (tagDao.selectCount(wrapper) > 0) {
			throw new RenException("标签名称已存在");
		}
		
		// 创建标签
		TagEntity tag = new TagEntity();
		tag.setName(dto.getName());
		tag.setColor(dto.getColor() != null ? dto.getColor() : "#999999");
		tag.setUserId(userId);
		tag.setCreateDate(new Date());
		tagDao.insert(tag);
		
		return convertToVO(tag);
	}
	
	@Override
	public List<TagVO> getMyTags(Long userId) {
		QueryWrapper<TagEntity> wrapper = new QueryWrapper<>();
		wrapper.eq("user_id", userId).orderByDesc("create_date");
		List<TagEntity> tags = tagDao.selectList(wrapper);
		
		return tags.stream()
			.map(this::convertToVO)
			.collect(Collectors.toList());
	}
	
	@Override
	public TagVO getTagById(Long id, Long userId) {
		TagEntity tag = tagDao.selectById(id);
		if (tag == null) {
			throw new RenException("标签不存在");
		}
		
		// 检查权限
		if (!tag.getUserId().equals(userId)) {
			throw new RenException("无权限查看该标签");
		}
		
		return convertToVO(tag);
	}
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public TagVO updateTag(Long id, TagUpdateDTO dto, Long userId) {
		TagEntity tag = tagDao.selectById(id);
		if (tag == null) {
			throw new RenException("标签不存在");
		}
		
		// 检查权限
		if (!tag.getUserId().equals(userId)) {
			throw new RenException("无权限修改该标签");
		}
		
		// 检查标签名是否已存在
		if (dto.getName() != null && !dto.getName().equals(tag.getName())) {
			QueryWrapper<TagEntity> wrapper = new QueryWrapper<>();
			wrapper.eq("user_id", userId).eq("name", dto.getName());
			if (tagDao.selectCount(wrapper) > 0) {
				throw new RenException("标签名称已存在");
			}
		}
		
		// 更新
		if (dto.getName() != null) {
			tag.setName(dto.getName());
		}
		if (dto.getColor() != null) {
			tag.setColor(dto.getColor());
		}
		tag.setUpdateDate(new Date());
		tagDao.updateById(tag);
		
		return convertToVO(tag);
	}
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteTag(Long id, Long userId) {
		TagEntity tag = tagDao.selectById(id);
		if (tag == null) {
			throw new RenException("标签不存在");
		}
		
		// 检查权限
		if (!tag.getUserId().equals(userId)) {
			throw new RenException("无权限删除该标签");
		}
		
		// 删除关联关系
		QueryWrapper<TodoTagEntity> wrapper = new QueryWrapper<>();
		wrapper.eq("tag_id", id);
		todoTagDao.delete(wrapper);
		
		// 删除标签
		tagDao.deleteById(id);
	}
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void addTagToTodo(Long todoId, Long tagId, Long userId) {
		// 检查 TODO 是否存在且有权限
		TodoEntity todo = todoDao.selectById(todoId);
		if (todo == null) {
			throw new RenException("TODO 不存在");
		}
		if (!todo.getUserId().equals(userId)) {
			throw new RenException("无权限操作该 TODO");
		}
		
		// 检查标签是否存在且有权限
		TagEntity tag = tagDao.selectById(tagId);
		if (tag == null) {
			throw new RenException("标签不存在");
		}
		if (!tag.getUserId().equals(userId)) {
			throw new RenException("无权限使用该标签");
		}
		
		// 检查是否已添加
		QueryWrapper<TodoTagEntity> wrapper = new QueryWrapper<>();
		wrapper.eq("todo_id", todoId).eq("tag_id", tagId);
		if (todoTagDao.selectCount(wrapper) > 0) {
			throw new RenException("该标签已添加");
		}
		
		// 添加关联
		TodoTagEntity todoTag = new TodoTagEntity();
		todoTag.setTodoId(todoId);
		todoTag.setTagId(tagId);
		todoTag.setCreateDate(new Date());
		todoTagDao.insert(todoTag);
	}
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void removeTagFromTodo(Long todoId, Long tagId, Long userId) {
		// 检查 TODO 是否存在且有权限
		TodoEntity todo = todoDao.selectById(todoId);
		if (todo == null) {
			throw new RenException("TODO 不存在");
		}
		if (!todo.getUserId().equals(userId)) {
			throw new RenException("无权限操作该 TODO");
		}
		
		// 删除关联
		QueryWrapper<TodoTagEntity> wrapper = new QueryWrapper<>();
		wrapper.eq("todo_id", todoId).eq("tag_id", tagId);
		todoTagDao.delete(wrapper);
	}
	
	@Override
	public List<TagVO> getTodoTags(Long todoId, Long userId) {
		// 检查 TODO 是否存在且有权限
		TodoEntity todo = todoDao.selectById(todoId);
		if (todo == null) {
			throw new RenException("TODO 不存在");
		}
		if (!todo.getUserId().equals(userId)) {
			throw new RenException("无权限查看该 TODO");
		}
		
		// 查询关联的标签ID
		QueryWrapper<TodoTagEntity> wrapper = new QueryWrapper<>();
		wrapper.eq("todo_id", todoId);
		List<TodoTagEntity> todoTags = todoTagDao.selectList(wrapper);
		
		if (todoTags.isEmpty()) {
			return List.of();
		}
		
		List<Long> tagIds = todoTags.stream()
			.map(TodoTagEntity::getTagId)
			.collect(Collectors.toList());
		
		// 查询标签信息
		List<TagEntity> tags = tagDao.selectBatchIds(tagIds);
		
		return tags.stream()
			.map(this::convertToVO)
			.collect(Collectors.toList());
	}
	
	/**
	 * 转换为 VO
	 */
	private TagVO convertToVO(TagEntity tag) {
		TagVO vo = new TagVO();
		vo.setId(tag.getId());
		vo.setName(tag.getName());
		vo.setColor(tag.getColor());
		vo.setCreateDate(tag.getCreateDate());
		
		// 查询使用次数
		QueryWrapper<TodoTagEntity> wrapper = new QueryWrapper<>();
		wrapper.eq("tag_id", tag.getId());
		Long count = todoTagDao.selectCount(wrapper);
		vo.setUsageCount(count.intValue());
		
		return vo;
	}
}


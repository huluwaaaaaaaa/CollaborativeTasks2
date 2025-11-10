/**
 * Copyright (c) 2018 COLLAB-TASK All rights reserved.
 *
 * https://www.collabtask.io
 *
 * 版权所有，侵权必究！
 */

package io.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.user.common.exception.ErrorCode;
import io.user.common.exception.RenException;
import io.user.common.service.impl.BaseServiceImpl;
import io.user.common.page.PageData;
import io.user.dao.TodoDao;
import io.user.dao.UserDao;
import io.user.dto.TodoCreateDTO;
import io.user.dto.TodoQueryDTO;
import io.user.dto.TodoUpdateDTO;
import io.user.dto.TodoVO;
import io.user.entity.TodoEntity;
import io.user.entity.UserEntity;
import io.user.service.TodoService;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TODO Service 实现
 *
 * @author System
 */
@Service
@AllArgsConstructor
public class TodoServiceImpl extends BaseServiceImpl<TodoDao, TodoEntity> implements TodoService {
	
	private final TodoDao todoDao;
	private final UserDao userDao;
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public TodoVO createTodo(TodoCreateDTO dto, Long userId) {
		// 创建 TODO 实体
		TodoEntity todo = new TodoEntity();
		todo.setName(dto.getName());
		todo.setDescription(dto.getDescription());
		todo.setDueDate(dto.getDueDate());
		todo.setPriority(StringUtils.isNotBlank(dto.getPriority()) ? dto.getPriority() : "MEDIUM");
		todo.setStatus("NOT_STARTED");
		todo.setUserId(userId);
		todo.setTeamId(dto.getTeamId());
		todo.setCreateDate(new Date());
		todo.setUpdateDate(new Date());
		
		// 保存到数据库
		todoDao.insert(todo);
		
		// TODO: 自动授予 OWNER 权限（ACL 功能开发后添加）
		// aclManagementService.grantPermission(userId, "TODO", todo.getId(), "OWNER");
		
		// 转换为 VO
		return convertToVO(todo);
	}
	
	@Override
	public PageData<TodoVO> getTodoList(TodoQueryDTO dto, Long userId) {
		// 构建查询条件
		LambdaQueryWrapper<TodoEntity> wrapper = new LambdaQueryWrapper<>();
		
		// 只查询自己创建的 TODO（暂不考虑权限，后续通过 ACL 扩展）
		wrapper.eq(TodoEntity::getUserId, userId);
		
		// 关键词搜索
		if (StringUtils.isNotBlank(dto.getKeyword())) {
			wrapper.and(w -> w.like(TodoEntity::getName, dto.getKeyword())
							 .or()
							 .like(TodoEntity::getDescription, dto.getKeyword()));
		}
		
		// 状态筛选
		if (StringUtils.isNotBlank(dto.getStatus())) {
			wrapper.eq(TodoEntity::getStatus, dto.getStatus());
		}
		
		// 优先级筛选
		if (StringUtils.isNotBlank(dto.getPriority())) {
			wrapper.eq(TodoEntity::getPriority, dto.getPriority());
		}
		
		// 团队筛选
		if (dto.getTeamId() != null) {
			wrapper.eq(TodoEntity::getTeamId, dto.getTeamId());
		}
		
		// 截止日期范围
		if (dto.getDueDateStart() != null) {
			wrapper.ge(TodoEntity::getDueDate, dto.getDueDateStart());
		}
		if (dto.getDueDateEnd() != null) {
			wrapper.le(TodoEntity::getDueDate, dto.getDueDateEnd());
		}
		
		// 排序
		String orderBy = StringUtils.isNotBlank(dto.getOrderBy()) ? dto.getOrderBy() : "create_date";
		boolean isAsc = "asc".equalsIgnoreCase(dto.getOrderDirection());
		
		switch (orderBy) {
			case "due_date":
				wrapper.orderBy(true, isAsc, TodoEntity::getDueDate);
				break;
			case "priority":
				wrapper.orderBy(true, isAsc, TodoEntity::getPriority);
				break;
			case "status":
				wrapper.orderBy(true, isAsc, TodoEntity::getStatus);
				break;
			default:
				wrapper.orderBy(true, isAsc, TodoEntity::getCreateDate);
		}
		
		// 分页查询
		Page<TodoEntity> page = new Page<>(dto.getPage(), dto.getLimit());
		IPage<TodoEntity> result = todoDao.selectPage(page, wrapper);
		
		// 转换为 VO
		List<TodoVO> voList = result.getRecords().stream()
				.map(this::convertToVO)
				.collect(Collectors.toList());
		
		return new PageData<>(voList, result.getTotal());
	}
	
	@Override
	public TodoVO getTodoById(Long id, Long userId) {
		TodoEntity todo = todoDao.selectById(id);
		
		if (todo == null) {
			throw new RenException("记录不存在");
		}
		
		// TODO: 权限检查（ACL 功能开发后添加）
		// if (!permissionService.hasPermission(userId, "TODO", id, "VIEW")) {
		//     throw new RenException(ErrorCode.FORBIDDEN);
		// }
		
		// 简单检查：只能查看自己的 TODO（暂不考虑权限）
		if (!todo.getUserId().equals(userId)) {
			throw new RenException("无权限查看此 TODO");
		}
		
		return convertToVO(todo);
	}
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public TodoVO updateTodo(Long id, TodoUpdateDTO dto, Long userId) {
		TodoEntity todo = todoDao.selectById(id);
		
		if (todo == null) {
			throw new RenException("记录不存在");
		}
		
		// TODO: 权限检查（ACL 功能开发后添加）
		// if (!permissionService.hasPermission(userId, "TODO", id, "EDIT")) {
		//     throw new RenException(ErrorCode.FORBIDDEN);
		// }
		
		// 简单检查：只能编辑自己的 TODO
		if (!todo.getUserId().equals(userId)) {
			throw new RenException("无权限编辑此 TODO");
		}
		
		// 更新字段
		if (StringUtils.isNotBlank(dto.getName())) {
			todo.setName(dto.getName());
		}
		if (dto.getDescription() != null) {
			todo.setDescription(dto.getDescription());
		}
		if (dto.getDueDate() != null) {
			todo.setDueDate(dto.getDueDate());
		}
		if (StringUtils.isNotBlank(dto.getStatus())) {
			todo.setStatus(dto.getStatus());
			// 如果状态改为 COMPLETED，记录完成时间
			if ("COMPLETED".equals(dto.getStatus()) && todo.getCompletedAt() == null) {
				todo.setCompletedAt(new Date());
			}
		}
		if (StringUtils.isNotBlank(dto.getPriority())) {
			todo.setPriority(dto.getPriority());
		}
		
		todo.setUpdateDate(new Date());
		
		// 保存
		todoDao.updateById(todo);
		
		return convertToVO(todo);
	}
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public TodoVO completeTodo(Long id, Long userId) {
		TodoEntity todo = todoDao.selectById(id);
		
		if (todo == null) {
			throw new RenException("记录不存在");
		}
		
		// 简单检查：只能完成自己的 TODO
		if (!todo.getUserId().equals(userId)) {
			throw new RenException("无权限操作此 TODO");
		}
		
		// 已完成则不重复操作
		if ("COMPLETED".equals(todo.getStatus())) {
			throw new RenException("TODO 已经完成");
		}
		
		// 设置为完成状态
		todo.setStatus("COMPLETED");
		todo.setCompletedAt(new Date());
		todo.setUpdateDate(new Date());
		
		todoDao.updateById(todo);
		
		return convertToVO(todo);
	}
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteTodo(Long id, Long userId) {
		TodoEntity todo = todoDao.selectById(id);
		
		if (todo == null) {
			throw new RenException("记录不存在");
		}
		
		// TODO: 权限检查（ACL 功能开发后添加）
		// if (!permissionService.hasPermission(userId, "TODO", id, "DELETE")) {
		//     throw new RenException(ErrorCode.FORBIDDEN);
		// }
		
		// 简单检查：只能删除自己的 TODO
		if (!todo.getUserId().equals(userId)) {
			throw new RenException("无权限删除此 TODO");
		}
		
		// 删除
		todoDao.deleteById(id);
		
		// TODO: 清理关联数据（标签关联、权限记录）
		// todoTagDao.delete(new LambdaQueryWrapper<TodoTagEntity>().eq(TodoTagEntity::getTodoId, id));
		// aclManagementService.revokeAllPermissions("TODO", id);
	}
	
	// ==================== 辅助方法 ====================
	
	/**
	 * 转换为 VO
	 */
	private TodoVO convertToVO(TodoEntity entity) {
		if (entity == null) {
			return null;
		}
		
		TodoVO vo = new TodoVO();
		vo.setId(entity.getId());
		vo.setName(entity.getName());
		vo.setDescription(entity.getDescription());
		vo.setDueDate(entity.getDueDate());
		vo.setStatus(entity.getStatus());
		vo.setPriority(entity.getPriority());
		vo.setUserId(entity.getUserId());
		vo.setTeamId(entity.getTeamId());
		vo.setCompletedAt(entity.getCompletedAt());
		vo.setCreateDate(entity.getCreateDate());
		vo.setUpdateDate(entity.getUpdateDate());
		
		// 查询创建者用户名
		UserEntity user = userDao.selectById(entity.getUserId());
		if (user != null) {
			vo.setUsername(user.getUsername());
		}
		
		// TODO: 查询团队名称（团队功能开发后添加）
		// if (entity.getTeamId() != null) {
		//     TeamEntity team = teamDao.selectById(entity.getTeamId());
		//     if (team != null) {
		//         vo.setTeamName(team.getName());
		//     }
		// }
		
		return vo;
	}
}


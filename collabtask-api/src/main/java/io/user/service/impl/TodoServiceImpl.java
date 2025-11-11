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
import io.user.common.annotation.DistributedLock;
import io.user.common.annotation.Idempotent;
import io.user.enums.PermissionCode;
import io.user.enums.ResourceType;
import io.user.enums.TodoPriority;
import io.user.enums.TodoStatus;
import io.user.service.AclPermissionService;
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
	private final AclPermissionService aclPermissionService;
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	@Idempotent(timeout = 300)  // v1.2: 幂等性控制（5分钟内防重复提交）
	public TodoVO createTodo(TodoCreateDTO dto, Long userId) {
		// 创建 TODO 实体
		TodoEntity todo = new TodoEntity();
		todo.setName(dto.getName());
		todo.setDescription(dto.getDescription());
		todo.setDueDate(dto.getDueDate());
		todo.setPriority(StringUtils.isNotBlank(dto.getPriority()) ? dto.getPriority() : TodoPriority.MEDIUM.getCode());
		todo.setStatus(TodoStatus.NOT_STARTED.getCode());
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
		// v1.1优化：查询自己创建的 + 共享给我的TODO
		// 使用自定义SQL联表查询ACL权限表
		
		// 计算偏移量
		long offset = (dto.getPage() - 1) * dto.getLimit();
		
		// 调用自定义查询方法
		List<TodoEntity> todoList = todoDao.selectMyTodosPage(
			userId,
			dto.getKeyword(),
			dto.getStatus(),
			dto.getPriority(),
			dto.getTeamId(),
			dto.getDueDateStart(),
			dto.getDueDateEnd(),
			StringUtils.isNotBlank(dto.getOrderBy()) ? dto.getOrderBy() : "create_date",
			dto.getOrderDirection(),
			offset,
			dto.getLimit()
		);
		
		// 统计总数
		Long total = todoDao.countMyTodos(
			userId,
			dto.getKeyword(),
			dto.getStatus(),
			dto.getPriority(),
			dto.getTeamId(),
			dto.getDueDateStart(),
			dto.getDueDateEnd()
		);
		
		// 转换为 VO
		List<TodoVO> voList = todoList.stream()
				.map(this::convertToVO)
				.collect(Collectors.toList());
		
		return new PageData<>(voList, total);
	}
	
	@Override
	public TodoVO getTodoById(Long id, Long userId) {
		TodoEntity todo = todoDao.selectById(id);
		
		if (todo == null) {
			throw new RenException("记录不存在");
		}
		
		// v1.1: ACL权限检查（支持TODO共享）
		// 检查是否是创建者，或者有VIEW权限
		boolean isOwner = todo.getUserId().equals(userId);
		boolean hasPermission = aclPermissionService.hasPermission(
			userId, ResourceType.TODO.getCode(), id, PermissionCode.VIEW.getCode()
		);
		
		if (!isOwner && !hasPermission) {
			throw new RenException("无权限查看此 TODO");
		}
		
		return convertToVO(todo);
	}
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	// @DistributedLock(key = "'todo:edit:' + #id", waitTime = 3, leaseTime = 10)  // v1.2: 分布式锁（暂时注释）
	public TodoVO updateTodo(Long id, TodoUpdateDTO dto, Long userId) {
		TodoEntity todo = todoDao.selectById(id);
		
		if (todo == null) {
			throw new RenException("记录不存在");
		}
		
		// v1.1: ACL权限检查（支持TODO共享）
		// 检查是否是创建者，或者有EDIT权限
		boolean isOwner = todo.getUserId().equals(userId);
		boolean hasPermission = aclPermissionService.hasPermission(
			userId, ResourceType.TODO.getCode(), id, PermissionCode.EDIT.getCode()
		);
		
		if (!isOwner && !hasPermission) {
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
		if (TodoStatus.COMPLETED.getCode().equals(todo.getStatus())) {
			throw new RenException("TODO 已经完成");
		}
		
		// 设置为完成状态
		todo.setStatus(TodoStatus.COMPLETED.getCode());
		todo.setCompletedAt(new Date());
		todo.setUpdateDate(new Date());
		
		todoDao.updateById(todo);
		
		return convertToVO(todo);
	}
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	// @DistributedLock(key = "'todo:delete:' + #id", waitTime = 3, leaseTime = 10)  // v1.2: 分布式锁（暂时注释）
	public void deleteTodo(Long id, Long userId) {
		TodoEntity todo = todoDao.selectById(id);
		
		if (todo == null) {
			throw new RenException("记录不存在");
		}
		
		// v1.1: ACL权限检查
		// 只有创建者可以删除（即使有EDIT权限也不能删除）
		if (!todo.getUserId().equals(userId)) {
			throw new RenException("只有创建者可以删除 TODO");
		}
		
		// 删除TODO
		todoDao.deleteById(id);
		
		// 数据库外键级联删除会自动清理：
		// - tb_todo_tags 中的关联记录（ON DELETE CASCADE）
		// - tb_acl_access_control 中的权限记录（ON DELETE CASCADE）
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


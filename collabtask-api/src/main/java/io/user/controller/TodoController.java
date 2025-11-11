/**
 * Copyright (c) 2018 COLLAB-TASK All rights reserved.
 *
 * https://www.collabtask.io
 *
 * 版权所有，侵权必究！
 */

package io.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.user.common.annotation.Login;
import io.user.common.page.PageData;
import io.user.common.utils.Result;
import io.user.common.validator.ValidatorUtils;
import io.user.dto.TodoCreateDTO;
import io.user.dto.TodoQueryDTO;
import io.user.dto.TodoShareDTO;
import io.user.dto.TodoUpdateDTO;
import io.user.dto.TodoVO;
import io.user.enums.PermissionCode;
import io.user.enums.ResourceType;
import io.user.service.AclPermissionService;
import io.user.service.TodoService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * TODO 控制器
 *
 * @author System
 */
@RestController
@RequestMapping("/api/todos")
@Tag(name = "TODO 管理")
@AllArgsConstructor
public class TodoController {
	
	private final TodoService todoService;
	private final AclPermissionService aclPermissionService;
	
	/**
	 * 创建 TODO
	 */
	@Login
	@PostMapping
	@Operation(summary = "创建 TODO")
	public Result<TodoVO> create(
		@RequestBody TodoCreateDTO dto,
		@Parameter(hidden = true) @RequestAttribute("userId") Long userId
	) {
		// 表单校验
		ValidatorUtils.validateEntity(dto);
		
		// 创建 TODO
		TodoVO vo = todoService.createTodo(dto, userId);
		
		return new Result<TodoVO>().ok(vo);
	}
	
	/**
	 * 获取 TODO 列表（分页、筛选、排序）
	 */
	@Login
	@GetMapping
	@Operation(summary = "获取 TODO 列表")
	public Result<PageData<TodoVO>> list(
		@ModelAttribute TodoQueryDTO dto,
		@Parameter(hidden = true) @RequestAttribute("userId") Long userId
	) {
		// 获取列表
		PageData<TodoVO> page = todoService.getTodoList(dto, userId);
		
		return new Result<PageData<TodoVO>>().ok(page);
	}
	
	/**
	 * 获取 TODO 详情
	 */
	@Login
	@GetMapping("/{id}")
	@Operation(summary = "获取 TODO 详情")
	public Result<TodoVO> getById(
		@PathVariable Long id,
		@Parameter(hidden = true) @RequestAttribute("userId") Long userId
	) {
		TodoVO vo = todoService.getTodoById(id, userId);
		return new Result<TodoVO>().ok(vo);
	}
	
	/**
	 * 更新 TODO
	 */
	@Login
	@PutMapping("/{id}")
	@Operation(summary = "更新 TODO")
	public Result<TodoVO> update(
		@PathVariable Long id,
		@RequestBody TodoUpdateDTO dto,
		@Parameter(hidden = true) @RequestAttribute("userId") Long userId
	) {
		// 更新
		TodoVO vo = todoService.updateTodo(id, dto, userId);
		
		return new Result<TodoVO>().ok(vo);
	}
	
	/**
	 * 完成 TODO
	 */
	@Login
	@PatchMapping("/{id}/complete")
	@Operation(summary = "完成 TODO")
	public Result<TodoVO> complete(
		@PathVariable Long id,
		@Parameter(hidden = true) @RequestAttribute("userId") Long userId
	) {
		TodoVO vo = todoService.completeTodo(id, userId);
		return new Result<TodoVO>().ok(vo);
	}
	
	/**
	 * 删除 TODO
	 */
	@Login
	@DeleteMapping("/{id}")
	@Operation(summary = "删除 TODO")
	public Result delete(
		@PathVariable Long id,
		@Parameter(hidden = true) @RequestAttribute("userId") Long userId
	) {
		todoService.deleteTodo(id, userId);
		return new Result().ok("删除成功");
	}
	
	/**
	 * 共享 TODO（v1.1新增）
	 */
	@Login
	@PostMapping("/{id}/share")
	@Operation(summary = "共享 TODO 给其他用户")
	public Result shareTodo(
		@PathVariable Long id,
		@RequestBody TodoShareDTO dto,
		@Parameter(hidden = true) @RequestAttribute("userId") Long userId
	) {
		// 验证
		ValidatorUtils.validateEntity(dto);
		
		// 检查是否是TODO的所有者
		TodoVO todo = todoService.getTodoById(id, userId);
		if (!todo.getUserId().equals(userId)) {
			throw new io.user.common.exception.RenException("只有创建者可以共享 TODO");
		}
		
		// 授予权限
		PermissionCode permissionCode = "VIEW".equals(dto.getPermission()) 
			? PermissionCode.VIEW 
			: PermissionCode.EDIT;
		aclPermissionService.grantPermission(
			dto.getTargetUserId(),
			ResourceType.TODO.getCode(),
			id,
			permissionCode.getCode(),
			userId
		);
		
		return new Result().ok("共享成功");
	}
	
	/**
	 * 取消共享 TODO（v1.1新增）
	 */
	@Login
	@DeleteMapping("/{id}/share/{targetUserId}")
	@Operation(summary = "取消共享 TODO")
	public Result unshareTodo(
		@PathVariable Long id,
		@PathVariable Long targetUserId,
		@Parameter(hidden = true) @RequestAttribute("userId") Long userId
	) {
		// 检查是否是TODO的所有者
		TodoVO todo = todoService.getTodoById(id, userId);
		if (!todo.getUserId().equals(userId)) {
			throw new io.user.common.exception.RenException("只有创建者可以取消共享");
		}
		
		// 撤销所有权限（VIEW + EDIT）
		aclPermissionService.revokePermission(targetUserId, ResourceType.TODO.getCode(), id, PermissionCode.VIEW.getCode());
		aclPermissionService.revokePermission(targetUserId, ResourceType.TODO.getCode(), id, PermissionCode.EDIT.getCode());
		
		return new Result().ok("取消共享成功");
	}
}


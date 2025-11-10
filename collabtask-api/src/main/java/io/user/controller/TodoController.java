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
import io.user.dto.TodoUpdateDTO;
import io.user.dto.TodoVO;
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
}


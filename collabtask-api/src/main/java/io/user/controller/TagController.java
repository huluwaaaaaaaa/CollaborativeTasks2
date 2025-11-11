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
import io.user.common.utils.Result;
import io.user.dto.*;
import io.user.service.TagService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 标签管理 Controller
 *
 * @author System
 */
@RestController
@RequestMapping("/api/tags")
@Tag(name = "标签管理")
@AllArgsConstructor
public class TagController {
	
	private final TagService tagService;
	
	@PostMapping
	@Login
	@Operation(summary = "创建标签")
	public Result<TagVO> createTag(
		@Valid @RequestBody TagCreateDTO dto,
		@Parameter(hidden = true) @RequestAttribute("userId") Long userId
	) {
		TagVO result = tagService.createTag(dto, userId);
		return new Result<TagVO>().ok(result);
	}
	
	@GetMapping
	@Login
	@Operation(summary = "获取我的标签列表")
	public Result<List<TagVO>> getMyTags(
		@Parameter(hidden = true) @RequestAttribute("userId") Long userId
	) {
		List<TagVO> result = tagService.getMyTags(userId);
		return new Result<List<TagVO>>().ok(result);
	}
	
	@GetMapping("/{id}")
	@Login
	@Operation(summary = "获取标签详情")
	public Result<TagVO> getTagById(
		@PathVariable Long id,
		@Parameter(hidden = true) @RequestAttribute("userId") Long userId
	) {
		TagVO result = tagService.getTagById(id, userId);
		return new Result<TagVO>().ok(result);
	}
	
	@PutMapping("/{id}")
	@Login
	@Operation(summary = "更新标签")
	public Result<TagVO> updateTag(
		@PathVariable Long id,
		@Valid @RequestBody TagUpdateDTO dto,
		@Parameter(hidden = true) @RequestAttribute("userId") Long userId
	) {
		TagVO result = tagService.updateTag(id, dto, userId);
		return new Result<TagVO>().ok(result);
	}
	
	@DeleteMapping("/{id}")
	@Login
	@Operation(summary = "删除标签")
	public Result<Void> deleteTag(
		@PathVariable Long id,
		@Parameter(hidden = true) @RequestAttribute("userId") Long userId
	) {
		tagService.deleteTag(id, userId);
		return new Result<Void>().ok(null);
	}
	
	@PostMapping("/todos/{todoId}/tags/{tagId}")
	@Login
	@Operation(summary = "给 TODO 添加标签")
	public Result<Void> addTagToTodo(
		@PathVariable Long todoId,
		@PathVariable Long tagId,
		@Parameter(hidden = true) @RequestAttribute("userId") Long userId
	) {
		tagService.addTagToTodo(todoId, tagId, userId);
		return new Result<Void>().ok(null);
	}
	
	@DeleteMapping("/todos/{todoId}/tags/{tagId}")
	@Login
	@Operation(summary = "从 TODO 移除标签")
	public Result<Void> removeTagFromTodo(
		@PathVariable Long todoId,
		@PathVariable Long tagId,
		@Parameter(hidden = true) @RequestAttribute("userId") Long userId
	) {
		tagService.removeTagFromTodo(todoId, tagId, userId);
		return new Result<Void>().ok(null);
	}
	
	@GetMapping("/todos/{todoId}")
	@Login
	@Operation(summary = "获取 TODO 的所有标签")
	public Result<List<TagVO>> getTodoTags(
		@PathVariable Long todoId,
		@Parameter(hidden = true) @RequestAttribute("userId") Long userId
	) {
		List<TagVO> result = tagService.getTodoTags(todoId, userId);
		return new Result<List<TagVO>>().ok(result);
	}
}


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
import io.user.dto.*;
import io.user.service.TeamService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 团队管理 Controller
 *
 * @author System
 */
@RestController
@RequestMapping("/api/teams")
@Tag(name = "团队管理")
@AllArgsConstructor
public class TeamController {
	
	private final TeamService teamService;
	
	@PostMapping
	@Login
	@Operation(summary = "创建团队")
	public Result<TeamVO> createTeam(
		@Valid @RequestBody TeamCreateDTO dto,
		@Parameter(hidden = true) @RequestAttribute("userId") Long userId
	) {
		TeamVO result = teamService.createTeam(dto, userId);
		return new Result<TeamVO>().ok(result);
	}
	
	@GetMapping
	@Login
	@Operation(summary = "获取我的团队列表")
	public Result<List<TeamVO>> getMyTeams(
		@Parameter(hidden = true) @RequestAttribute("userId") Long userId
	) {
		List<TeamVO> result = teamService.getMyTeams(userId);
		return new Result<List<TeamVO>>().ok(result);
	}
	
	@GetMapping("/{id}")
	@Login
	@Operation(summary = "获取团队详情")
	public Result<TeamVO> getTeamById(
		@PathVariable Long id,
		@Parameter(hidden = true) @RequestAttribute("userId") Long userId
	) {
		TeamVO result = teamService.getTeamById(id, userId);
		return new Result<TeamVO>().ok(result);
	}
	
	@PutMapping("/{id}")
	@Login
	@Operation(summary = "更新团队")
	public Result<TeamVO> updateTeam(
		@PathVariable Long id,
		@Valid @RequestBody TeamUpdateDTO dto,
		@Parameter(hidden = true) @RequestAttribute("userId") Long userId
	) {
		TeamVO result = teamService.updateTeam(id, dto, userId);
		return new Result<TeamVO>().ok(result);
	}
	
	@DeleteMapping("/{id}")
	@Login
	@Operation(summary = "删除团队")
	public Result<Void> deleteTeam(
		@PathVariable Long id,
		@Parameter(hidden = true) @RequestAttribute("userId") Long userId
	) {
		teamService.deleteTeam(id, userId);
		return new Result<Void>().ok(null);
	}
	
	@PostMapping("/{teamId}/members/{memberId}")
	@Login
	@Operation(summary = "添加团队成员")
	public Result<Void> addMember(
		@PathVariable Long teamId,
		@PathVariable Long memberId,
		@Parameter(hidden = true) @RequestAttribute("userId") Long userId
	) {
		teamService.addMember(teamId, memberId, userId);
		return new Result<Void>().ok(null);
	}
	
	@DeleteMapping("/{teamId}/members/{memberId}")
	@Login
	@Operation(summary = "移除团队成员")
	public Result<Void> removeMember(
		@PathVariable Long teamId,
		@PathVariable Long memberId,
		@Parameter(hidden = true) @RequestAttribute("userId") Long userId
	) {
		teamService.removeMember(teamId, memberId, userId);
		return new Result<Void>().ok(null);
	}
	
	@GetMapping("/{teamId}/members")
	@Login
	@Operation(summary = "获取团队成员列表")
	public Result<List<TeamMemberVO>> getTeamMembers(
		@PathVariable Long teamId,
		@Parameter(hidden = true) @RequestAttribute("userId") Long userId
	) {
		List<TeamMemberVO> result = teamService.getTeamMembers(teamId, userId);
		return new Result<List<TeamMemberVO>>().ok(result);
	}
	
	@GetMapping("/{teamId}/todos")
	@Login
	@Operation(summary = "获取团队的 TODO 列表")
	public Result<PageData<TodoVO>> getTeamTodos(
		@PathVariable Long teamId,
		TodoQueryDTO query,
		@Parameter(hidden = true) @RequestAttribute("userId") Long userId
	) {
		PageData<TodoVO> result = teamService.getTeamTodos(teamId, query, userId);
		return new Result<PageData<TodoVO>>().ok(result);
	}
}


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
import io.user.common.page.PageData;
import io.user.common.service.impl.BaseServiceImpl;
import io.user.dao.*;
import io.user.dto.*;
import io.user.common.annotation.Idempotent;
import io.user.entity.*;
import io.user.enums.PermissionCode;
import io.user.enums.ResourceType;
import io.user.service.TeamService;
import io.user.service.TodoService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 团队管理 Service 实现
 *
 * @author System
 */
@Service
@AllArgsConstructor
public class TeamServiceImpl extends BaseServiceImpl<TeamDao, TeamEntity> implements TeamService {
	
	private final TeamDao teamDao;
	private final TeamMemberDao teamMemberDao;
	private final UserDao userDao;
	private final TodoService todoService;
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	@Idempotent(timeout = 300)  // v1.2: 幂等性控制
	public TeamVO createTeam(TeamCreateDTO dto, Long userId) {
		// 1. 创建团队
		TeamEntity team = new TeamEntity();
		team.setName(dto.getName());
		team.setDescription(dto.getDescription());
		team.setOwnerId(userId);
		team.setCreateDate(new Date());
		teamDao.insert(team);
		
		// 2. 自动添加创建者为成员
		TeamMemberEntity member = new TeamMemberEntity();
		member.setTeamId(team.getId());
		member.setUserId(userId);
		member.setCreateDate(new Date());
		teamMemberDao.insert(member);
		
		// 3. 返回 VO
		return convertToVO(team, userId);
	}
	
	@Override
	public List<TeamVO> getMyTeams(Long userId) {
		// 查询我加入的所有团队
		QueryWrapper<TeamMemberEntity> wrapper = new QueryWrapper<>();
		wrapper.eq("user_id", userId);
		List<TeamMemberEntity> members = teamMemberDao.selectList(wrapper);
		
		List<Long> teamIds = members.stream()
			.map(TeamMemberEntity::getTeamId)
			.collect(Collectors.toList());
		
		if (teamIds.isEmpty()) {
			return new ArrayList<>();
		}
		
		// 查询团队信息
		List<TeamEntity> teams = teamDao.selectBatchIds(teamIds);
		return teams.stream()
			.map(team -> convertToVO(team, userId))
			.collect(Collectors.toList());
	}
	
	@Override
	public TeamVO getTeamById(Long id, Long userId) {
		// 检查权限：是否是团队成员
		if (!isTeamMember(id, userId)) {
			throw new RenException("无权限查看该团队");
		}
		
		TeamEntity team = teamDao.selectById(id);
		if (team == null) {
			throw new RenException("团队不存在");
		}
		
		return convertToVO(team, userId);
	}
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	@io.user.common.annotation.RequirePermission(
		resourceType = ResourceType.TEAM,
		permission = PermissionCode.EDIT,
		checkOwner = true  // ⭐ 自动检查 OWNER（Team的owner字段是ownerId）
	)
	// @DistributedLock(key = "'team:edit:' + #id", waitTime = 3, leaseTime = 10)  // v1.2: 分布式锁（暂时注释）
	public TeamVO updateTeam(Long id, TeamUpdateDTO dto, Long userId) {
		// ✅ 切面已自动检查权限（OWNER）
		
		TeamEntity team = teamDao.selectById(id);
		if (team == null) {
			throw new RenException("团队不存在");
		}
		
		// 更新
		if (dto.getName() != null) {
			team.setName(dto.getName());
		}
		if (dto.getDescription() != null) {
			team.setDescription(dto.getDescription());
		}
		team.setUpdateDate(new Date());
		teamDao.updateById(team);
		
		return convertToVO(team, userId);
	}
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	@io.user.common.annotation.RequirePermission(
		resourceType = ResourceType.TEAM,
		permission = PermissionCode.DELETE,
		checkOwner = true  // ⭐ 自动检查 OWNER
	)
	public void deleteTeam(Long id, Long userId) {
		// ✅ 切面已自动检查权限（OWNER）
		
		TeamEntity team = teamDao.selectById(id);
		if (team == null) {
			throw new RenException("团队不存在");
		}
		
		// 删除团队成员
		QueryWrapper<TeamMemberEntity> wrapper = new QueryWrapper<>();
		wrapper.eq("team_id", id);
		teamMemberDao.delete(wrapper);
		
		// 删除团队
		teamDao.deleteById(id);
	}
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void addMember(Long teamId, Long memberId, Long userId) {
		TeamEntity team = teamDao.selectById(teamId);
		if (team == null) {
			throw new RenException("团队不存在");
		}
		
		// 检查权限：只有所有者可以添加成员
		if (!team.getOwnerId().equals(userId)) {
			throw new RenException("只有团队所有者可以添加成员");
		}
		
		// 检查用户是否存在
		UserEntity user = userDao.selectById(memberId);
		if (user == null) {
			throw new RenException("用户不存在");
		}
		
		// 检查是否已经是成员
		QueryWrapper<TeamMemberEntity> wrapper = new QueryWrapper<>();
		wrapper.eq("team_id", teamId).eq("user_id", memberId);
		if (teamMemberDao.selectCount(wrapper) > 0) {
			throw new RenException("用户已经是团队成员");
		}
		
		// 添加成员
		TeamMemberEntity member = new TeamMemberEntity();
		member.setTeamId(teamId);
		member.setUserId(memberId);
		member.setCreateDate(new Date());
		teamMemberDao.insert(member);
	}
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void removeMember(Long teamId, Long memberId, Long userId) {
		TeamEntity team = teamDao.selectById(teamId);
		if (team == null) {
			throw new RenException("团队不存在");
		}
		
		// 检查权限：只有所有者可以移除成员
		if (!team.getOwnerId().equals(userId)) {
			throw new RenException("只有团队所有者可以移除成员");
		}
		
		// 不能移除所有者
		if (memberId.equals(team.getOwnerId())) {
			throw new RenException("不能移除团队所有者");
		}
		
		// 移除成员
		QueryWrapper<TeamMemberEntity> wrapper = new QueryWrapper<>();
		wrapper.eq("team_id", teamId).eq("user_id", memberId);
		teamMemberDao.delete(wrapper);
	}
	
	@Override
	public List<TeamMemberVO> getTeamMembers(Long teamId, Long userId) {
		// 检查权限：是否是团队成员
		if (!isTeamMember(teamId, userId)) {
			throw new RenException("无权限查看该团队成员");
		}
		
		TeamEntity team = teamDao.selectById(teamId);
		if (team == null) {
			throw new RenException("团队不存在");
		}
		
		// 查询成员
		QueryWrapper<TeamMemberEntity> wrapper = new QueryWrapper<>();
		wrapper.eq("team_id", teamId);
		List<TeamMemberEntity> members = teamMemberDao.selectList(wrapper);
		
		// 查询用户信息
		List<Long> userIds = members.stream()
			.map(TeamMemberEntity::getUserId)
			.collect(Collectors.toList());
		
		Map<Long, UserEntity> userMap = userDao.selectBatchIds(userIds).stream()
			.collect(Collectors.toMap(UserEntity::getId, u -> u));
		
		// 转换为 VO
		return members.stream().map(member -> {
			TeamMemberVO vo = new TeamMemberVO();
			vo.setId(member.getId());
			vo.setUserId(member.getUserId());
			vo.setJoinDate(member.getCreateDate());
			vo.setIsOwner(member.getUserId().equals(team.getOwnerId()));
			
			UserEntity user = userMap.get(member.getUserId());
			if (user != null) {
				vo.setUsername(user.getUsername());
			}
			return vo;
		}).collect(Collectors.toList());
	}
	
	@Override
	public PageData<TodoVO> getTeamTodos(Long teamId, TodoQueryDTO query, Long userId) {
		// 检查权限：是否是团队成员
		if (!isTeamMember(teamId, userId)) {
			throw new RenException("无权限查看该团队的 TODO");
		}
		
		// 设置团队 ID 过滤
		query.setTeamId(teamId);
		
		// 使用 TodoService 的分页查询
		return todoService.getTodoList(query, userId);
	}
	
	/**
	 * 检查是否是团队成员
	 */
	private boolean isTeamMember(Long teamId, Long userId) {
		QueryWrapper<TeamMemberEntity> wrapper = new QueryWrapper<>();
		wrapper.eq("team_id", teamId).eq("user_id", userId);
		return teamMemberDao.selectCount(wrapper) > 0;
	}
	
	/**
	 * 转换为 VO
	 */
	private TeamVO convertToVO(TeamEntity team, Long currentUserId) {
		TeamVO vo = new TeamVO();
		vo.setId(team.getId());
		vo.setName(team.getName());
		vo.setDescription(team.getDescription());
		vo.setOwnerId(team.getOwnerId());
		vo.setCreateDate(team.getCreateDate());
		vo.setUpdateDate(team.getUpdateDate());
		
		// 查询所有者用户名
		UserEntity owner = userDao.selectById(team.getOwnerId());
		if (owner != null) {
			vo.setOwnerName(owner.getUsername());
		}
		
		// 查询成员数量
		QueryWrapper<TeamMemberEntity> wrapper = new QueryWrapper<>();
		wrapper.eq("team_id", team.getId());
		Long memberCount = teamMemberDao.selectCount(wrapper);
		vo.setMemberCount(memberCount.intValue());
		
		return vo;
	}
}


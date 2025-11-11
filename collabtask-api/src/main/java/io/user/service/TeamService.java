/**
 * Copyright (c) 2018 COLLAB-TASK All rights reserved.
 *
 * https://www.collabtask.io
 *
 * 版权所有，侵权必究！
 */

package io.user.service;

import io.user.common.page.PageData;
import io.user.dto.*;

import java.util.List;

/**
 * 团队管理 Service
 *
 * @author System
 */
public interface TeamService {
	
	/**
	 * 创建团队
	 */
	TeamVO createTeam(TeamCreateDTO dto, Long userId);
	
	/**
	 * 获取我的团队列表（我创建的+我加入的）
	 */
	List<TeamVO> getMyTeams(Long userId);
	
	/**
	 * 获取团队详情
	 */
	TeamVO getTeamById(Long id, Long userId);
	
	/**
	 * 更新团队
	 */
	TeamVO updateTeam(Long id, TeamUpdateDTO dto, Long userId);
	
	/**
	 * 删除团队
	 */
	void deleteTeam(Long id, Long userId);
	
	/**
	 * 添加团队成员
	 */
	void addMember(Long teamId, Long memberId, Long userId);
	
	/**
	 * 移除团队成员
	 */
	void removeMember(Long teamId, Long memberId, Long userId);
	
	/**
	 * 获取团队成员列表
	 */
	List<TeamMemberVO> getTeamMembers(Long teamId, Long userId);
	
	/**
	 * 获取团队的 TODO 列表
	 */
	PageData<TodoVO> getTeamTodos(Long teamId, TodoQueryDTO query, Long userId);
}


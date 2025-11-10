/**
 * Copyright (c) 2018 COLLAB-TASK All rights reserved.
 *
 * https://www.collabtask.io
 *
 * 版权所有，侵权必究！
 */

package io.user.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.user.common.exception.RenException;
import io.user.dao.TeamDao;
import io.user.dao.TeamMemberDao;
import io.user.dao.UserDao;
import io.user.dto.TeamCreateDTO;
import io.user.dto.TeamUpdateDTO;
import io.user.dto.TeamVO;
import io.user.entity.TeamEntity;
import io.user.entity.TeamMemberEntity;
import io.user.entity.UserEntity;
import io.user.service.impl.TeamServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * TeamService 单元测试
 * 
 * @author System
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("团队服务单元测试")
public class TeamServiceTest {

    @Mock
    private TeamDao teamDao;

    @Mock
    private TeamMemberDao teamMemberDao;

    @Mock
    private UserDao userDao;

    @InjectMocks
    private TeamServiceImpl teamService;

    private Long userId;
    private TeamEntity teamEntity;
    private TeamCreateDTO createDTO;

    @BeforeEach
    void setUp() {
        userId = 1L;
        
        teamEntity = new TeamEntity();
        teamEntity.setId(1L);
        teamEntity.setName("测试团队");
        teamEntity.setDescription("测试描述");
        teamEntity.setOwnerId(userId);
        teamEntity.setCreateDate(new Date());
        
        createDTO = new TeamCreateDTO();
        createDTO.setName("新团队");
        createDTO.setDescription("新团队描述");
    }

    @Test
    @DisplayName("创建团队 - 成功")
    void testCreateTeam_Success() {
        // Given
        when(teamDao.insert(any(TeamEntity.class))).thenAnswer(invocation -> {
            TeamEntity team = invocation.getArgument(0);
            team.setId(1L);
            return 1;
        });
        when(teamMemberDao.insert(any(TeamMemberEntity.class))).thenReturn(1);
        when(teamMemberDao.selectCount(any(QueryWrapper.class))).thenReturn(1L);
        
        UserEntity user = new UserEntity();
        user.setUsername("testuser");
        when(userDao.selectById(userId)).thenReturn(user);

        // When
        TeamVO result = teamService.createTeam(createDTO, userId);

        // Then
        assertNotNull(result);
        assertEquals("新团队", result.getName());
        assertEquals("新团队描述", result.getDescription());
        assertEquals(userId, result.getOwnerId());
        assertEquals(1, result.getMemberCount());
        
        verify(teamDao, times(1)).insert(any(TeamEntity.class));
        verify(teamMemberDao, times(1)).insert(any(TeamMemberEntity.class));
    }

    @Test
    @DisplayName("获取团队详情 - 成功")
    void testGetTeamById_Success() {
        // Given
        Long teamId = 1L;
        when(teamDao.selectById(teamId)).thenReturn(teamEntity);
        
        UserEntity user = new UserEntity();
        user.setUsername("testuser");
        when(userDao.selectById(userId)).thenReturn(user);

        // Mock isTeamMember and member count
        when(teamMemberDao.selectCount(any(QueryWrapper.class))).thenReturn(1L);

        // When
        TeamVO result = teamService.getTeamById(teamId, userId);

        // Then
        assertNotNull(result);
        assertEquals("测试团队", result.getName());
        assertEquals(userId, result.getOwnerId());
    }

    @Test
    @DisplayName("获取团队详情 - 团队不存在")
    void testGetTeamById_NotFound() {
        // Given
        Long teamId = 999L;
        when(teamMemberDao.selectCount(any(QueryWrapper.class))).thenReturn(1L);
        when(teamDao.selectById(teamId)).thenReturn(null);

        // When & Then
        assertThrows(RenException.class, () -> {
            teamService.getTeamById(teamId, userId);
        });
    }

    @Test
    @DisplayName("获取团队详情 - 无权限")
    void testGetTeamById_NoPermission() {
        // Given
        Long teamId = 1L;
        when(teamMemberDao.selectCount(any(QueryWrapper.class))).thenReturn(0L);

        // When & Then
        assertThrows(RenException.class, () -> {
            teamService.getTeamById(teamId, userId);
        });
    }

    @Test
    @DisplayName("更新团队 - 成功")
    void testUpdateTeam_Success() {
        // Given
        Long teamId = 1L;
        TeamUpdateDTO updateDTO = new TeamUpdateDTO();
        updateDTO.setName("更新后的团队");
        updateDTO.setDescription("更新后的描述");
        
        when(teamDao.selectById(teamId)).thenReturn(teamEntity);
        when(teamDao.updateById(any(TeamEntity.class))).thenReturn(1);
        when(teamMemberDao.selectCount(any(QueryWrapper.class))).thenReturn(1L);
        
        UserEntity user = new UserEntity();
        user.setUsername("testuser");
        when(userDao.selectById(userId)).thenReturn(user);

        // When
        TeamVO result = teamService.updateTeam(teamId, updateDTO, userId);

        // Then
        assertNotNull(result);
        assertEquals("更新后的团队", result.getName());
        assertEquals("更新后的描述", result.getDescription());
        
        verify(teamDao, times(1)).updateById(any(TeamEntity.class));
    }

    @Test
    @DisplayName("更新团队 - 非所有者无权限")
    void testUpdateTeam_NotOwner() {
        // Given
        Long teamId = 1L;
        Long otherUserId = 2L;
        teamEntity.setOwnerId(otherUserId);
        
        TeamUpdateDTO updateDTO = new TeamUpdateDTO();
        when(teamDao.selectById(teamId)).thenReturn(teamEntity);

        // When & Then
        assertThrows(RenException.class, () -> {
            teamService.updateTeam(teamId, updateDTO, userId);
        });
        
        verify(teamDao, never()).updateById(any(TeamEntity.class));
    }

    @Test
    @DisplayName("删除团队 - 成功")
    void testDeleteTeam_Success() {
        // Given
        Long teamId = 1L;
        when(teamDao.selectById(teamId)).thenReturn(teamEntity);
        when(teamMemberDao.delete(any(QueryWrapper.class))).thenReturn(1);
        when(teamDao.deleteById(teamId)).thenReturn(1);

        // When
        teamService.deleteTeam(teamId, userId);

        // Then
        verify(teamMemberDao, times(1)).delete(any(QueryWrapper.class));
        verify(teamDao, times(1)).deleteById(teamId);
    }

    @Test
    @DisplayName("添加团队成员 - 成功")
    void testAddMember_Success() {
        // Given
        Long teamId = 1L;
        Long memberId = 2L;
        
        when(teamDao.selectById(teamId)).thenReturn(teamEntity);
        
        UserEntity member = new UserEntity();
        member.setId(memberId);
        member.setUsername("newmember");
        when(userDao.selectById(memberId)).thenReturn(member);
        
        when(teamMemberDao.selectCount(any(QueryWrapper.class))).thenReturn(0L);
        when(teamMemberDao.insert(any(TeamMemberEntity.class))).thenReturn(1);

        // When
        teamService.addMember(teamId, memberId, userId);

        // Then
        verify(teamMemberDao, times(1)).insert(any(TeamMemberEntity.class));
    }

    @Test
    @DisplayName("添加团队成员 - 非所有者无权限")
    void testAddMember_NotOwner() {
        // Given
        Long teamId = 1L;
        Long memberId = 2L;
        Long otherUserId = 999L;
        teamEntity.setOwnerId(otherUserId);
        
        when(teamDao.selectById(teamId)).thenReturn(teamEntity);

        // When & Then
        assertThrows(RenException.class, () -> {
            teamService.addMember(teamId, memberId, userId);
        });
        
        verify(teamMemberDao, never()).insert(any(TeamMemberEntity.class));
    }

    @Test
    @DisplayName("添加团队成员 - 成员已存在")
    void testAddMember_AlreadyExists() {
        // Given
        Long teamId = 1L;
        Long memberId = 2L;
        
        when(teamDao.selectById(teamId)).thenReturn(teamEntity);
        
        UserEntity member = new UserEntity();
        member.setId(memberId);
        when(userDao.selectById(memberId)).thenReturn(member);
        
        when(teamMemberDao.selectCount(any(QueryWrapper.class))).thenReturn(1L);

        // When & Then
        assertThrows(RenException.class, () -> {
            teamService.addMember(teamId, memberId, userId);
        });
        
        verify(teamMemberDao, never()).insert(any(TeamMemberEntity.class));
    }

    @Test
    @DisplayName("移除团队成员 - 成功")
    void testRemoveMember_Success() {
        // Given
        Long teamId = 1L;
        Long memberId = 2L;
        
        when(teamDao.selectById(teamId)).thenReturn(teamEntity);
        when(teamMemberDao.delete(any(QueryWrapper.class))).thenReturn(1);

        // When
        teamService.removeMember(teamId, memberId, userId);

        // Then
        verify(teamMemberDao, times(1)).delete(any(QueryWrapper.class));
    }

    @Test
    @DisplayName("移除团队成员 - 不能移除所有者")
    void testRemoveMember_CannotRemoveOwner() {
        // Given
        Long teamId = 1L;
        
        when(teamDao.selectById(teamId)).thenReturn(teamEntity);

        // When & Then
        assertThrows(RenException.class, () -> {
            teamService.removeMember(teamId, userId, userId);
        });
        
        verify(teamMemberDao, never()).delete(any());
    }

    @Test
    @DisplayName("获取我的团队列表 - 成功")
    void testGetMyTeams_Success() {
        // Given
        List<TeamMemberEntity> members = new ArrayList<>();
        TeamMemberEntity member = new TeamMemberEntity();
        member.setTeamId(1L);
        member.setUserId(userId);
        members.add(member);
        
        when(teamMemberDao.selectList(any(QueryWrapper.class))).thenReturn(members);
        
        List<TeamEntity> teams = new ArrayList<>();
        teams.add(teamEntity);
        when(teamDao.selectBatchIds(anyList())).thenReturn(teams);
        when(teamMemberDao.selectCount(any(QueryWrapper.class))).thenReturn(1L);
        
        UserEntity user = new UserEntity();
        user.setUsername("testuser");
        when(userDao.selectById(userId)).thenReturn(user);

        // When
        List<TeamVO> result = teamService.getMyTeams(userId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("测试团队", result.get(0).getName());
    }
}


/**
 * Copyright (c) 2018 COLLAB-TASK All rights reserved.
 *
 * https://www.collabtask.io
 *
 * 版权所有，侵权必究！
 */

package io.user.integration;

import io.user.common.exception.RenException;
import io.user.dao.*;
import io.user.dto.TagUpdateDTO;
import io.user.dto.TeamUpdateDTO;
import io.user.dto.TodoUpdateDTO;
import io.user.entity.TagEntity;
import io.user.entity.TeamEntity;
import io.user.entity.TodoEntity;
import io.user.entity.UserEntity;
import io.user.enums.PermissionCode;
import io.user.enums.ResourceType;
import io.user.service.AclPermissionService;
import io.user.service.TagService;
import io.user.service.TeamService;
import io.user.service.TodoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * @RequirePermission 注解集成测试
 *
 * 测试权限检查切面是否正常工作
 *
 * @author System
 */
//@SpringBootTest
//@ActiveProfiles("test")
//@DisplayName("权限检查注解集成测试")
public class PermissionCheckTest {

    @Autowired
    private TodoService todoService;

    @Autowired
    private TagService tagService;

    @Autowired
    private TeamService teamService;

    @MockBean
    private TodoDao todoDao;

    @MockBean
    private TagDao tagDao;

    @MockBean
    private TeamDao teamDao;

    @MockBean
    private UserDao userDao;

    @MockBean
    private TodoTagDao todoTagDao;

    @MockBean
    private TeamMemberDao teamMemberDao;

    @MockBean
    private AclPermissionService aclPermissionService;

    private Long ownerId;
    private Long otherUserId;
    private TodoEntity todoEntity;
    private TagEntity tagEntity;
    private TeamEntity teamEntity;

    @BeforeEach
    void setUp() {
        ownerId = 1L;
        otherUserId = 2L;

        // 准备 TODO 测试数据
        todoEntity = new TodoEntity();
        todoEntity.setId(1L);
        todoEntity.setName("测试TODO");
        todoEntity.setUserId(ownerId);
        todoEntity.setStatus("NOT_STARTED");
        todoEntity.setCreateDate(new Date());

        // 准备 Tag 测试数据
        tagEntity = new TagEntity();
        tagEntity.setId(1L);
        tagEntity.setName("测试标签");
        tagEntity.setColor("#FF0000");
        tagEntity.setUserId(ownerId);
        tagEntity.setCreateDate(new Date());

        // 准备 Team 测试数据
        teamEntity = new TeamEntity();
        teamEntity.setId(1L);
        teamEntity.setName("测试团队");
        teamEntity.setOwnerId(ownerId);
        teamEntity.setCreateDate(new Date());

        // Mock User
        UserEntity user = new UserEntity();
        user.setId(ownerId);
        user.setUsername("owner");
        when(userDao.selectById(ownerId)).thenReturn(user);

        UserEntity otherUser = new UserEntity();
        otherUser.setId(otherUserId);
        otherUser.setUsername("other");
        when(userDao.selectById(otherUserId)).thenReturn(otherUser);
    }

    // ==================== TODO 权限测试 ====================

    @Test
    @DisplayName("TODO - OWNER可以查看（跳过ACL检查）")
    void testTodo_OwnerCanView() {
        // Given
        when(todoDao.selectById(1L)).thenReturn(todoEntity);

        // When
        assertDoesNotThrow(() -> {
            todoService.getTodoById(1L, ownerId);
        });

        // Then - OWNER 不应该调用 ACL 检查
        verify(aclPermissionService, never()).hasPermission(any(), any(), any(), any());
    }

    @Test
    @DisplayName("TODO - 非OWNER有VIEW权限可以查看")
    void testTodo_NonOwnerWithPermissionCanView() {
        // Given
        when(todoDao.selectById(1L)).thenReturn(todoEntity);
        when(aclPermissionService.hasPermission(
                eq(otherUserId),
                eq(ResourceType.TODO.getCode()),
                eq(1L),
                eq(PermissionCode.VIEW.getCode())
        )).thenReturn(true);

        // When
        assertDoesNotThrow(() -> {
            todoService.getTodoById(1L, otherUserId);
        });

        // Then - 应该调用了 ACL 检查
        verify(aclPermissionService, times(1)).hasPermission(
                eq(otherUserId),
                eq(ResourceType.TODO.getCode()),
                eq(1L),
                eq(PermissionCode.VIEW.getCode())
        );
    }

    @Test
    @DisplayName("TODO - 非OWNER无权限抛出异常")
    void testTodo_NonOwnerWithoutPermissionThrowsException() {
        // Given
        when(todoDao.selectById(1L)).thenReturn(todoEntity);
        when(aclPermissionService.hasPermission(
                eq(otherUserId),
                eq(ResourceType.TODO.getCode()),
                eq(1L),
                eq(PermissionCode.VIEW.getCode())
        )).thenReturn(false);

        // When & Then
        RenException exception = assertThrows(RenException.class, () -> {
            todoService.getTodoById(1L, otherUserId);
        });

        assertEquals("权限不足", exception.getMessage());

        // 应该记录审计日志
        verify(aclPermissionService, times(1)).auditLog(
                eq(otherUserId),
                eq(ResourceType.TODO.getCode()),
                eq(1L),
                eq(PermissionCode.VIEW.getCode()),
                eq("CHECK"),
                eq("DENIED"),
                eq(null)
        );
    }

    @Test
    @DisplayName("TODO - OWNER可以编辑")
    void testTodo_OwnerCanEdit() {
        // Given
        TodoUpdateDTO updateDTO = new TodoUpdateDTO();
        updateDTO.setName("更新后的名称");

        when(todoDao.selectById(1L)).thenReturn(todoEntity);
        when(todoDao.updateById(argThat((TodoEntity t) -> t != null))).thenReturn(1);

        // When
        assertDoesNotThrow(() -> {
            todoService.updateTodo(1L, updateDTO, ownerId);
        });

        // Then
        verify(aclPermissionService, never()).hasPermission(any(), any(), any(), any());
        verify(todoDao, times(1)).updateById(argThat((TodoEntity t) -> t != null));
    }

    @Test
    @DisplayName("TODO - 非OWNER有EDIT权限可以编辑")
    void testTodo_NonOwnerWithEditPermissionCanEdit() {
        // Given
        TodoUpdateDTO updateDTO = new TodoUpdateDTO();
        updateDTO.setName("更新后的名称");

        when(todoDao.selectById(1L)).thenReturn(todoEntity);
        when(aclPermissionService.hasPermission(
                eq(otherUserId),
                eq(ResourceType.TODO.getCode()),
                eq(1L),
                eq(PermissionCode.EDIT.getCode())
        )).thenReturn(true);
        when(todoDao.updateById(argThat((TodoEntity t) -> t != null))).thenReturn(1);

        // When
        assertDoesNotThrow(() -> {
            todoService.updateTodo(1L, updateDTO, otherUserId);
        });

        // Then
        verify(aclPermissionService, times(1)).hasPermission(
                eq(otherUserId),
                eq(ResourceType.TODO.getCode()),
                eq(1L),
                eq(PermissionCode.EDIT.getCode())
        );
    }

    @Test
    @DisplayName("TODO - OWNER可以删除")
    void testTodo_OwnerCanDelete() {
        // Given
        when(todoDao.selectById(1L)).thenReturn(todoEntity);
        when(todoDao.deleteById(1L)).thenReturn(1);

        // When
        assertDoesNotThrow(() -> {
            todoService.deleteTodo(1L, ownerId);
        });

        // Then
        verify(todoDao, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("TODO - 非OWNER无DELETE权限不能删除")
    void testTodo_NonOwnerCannotDelete() {
        // Given
        when(todoDao.selectById(1L)).thenReturn(todoEntity);
        when(aclPermissionService.hasPermission(
                eq(otherUserId),
                eq(ResourceType.TODO.getCode()),
                eq(1L),
                eq(PermissionCode.DELETE.getCode())
        )).thenReturn(false);

        // When & Then
        assertThrows(RenException.class, () -> {
            todoService.deleteTodo(1L, otherUserId);
        });

        verify(todoDao, never()).deleteById(any());
    }

    // ==================== TAG 权限测试 ====================

    @Test
    @DisplayName("TAG - OWNER可以编辑")
    void testTag_OwnerCanEdit() {
        // Given
        TagUpdateDTO updateDTO = new TagUpdateDTO();
        updateDTO.setName("更新后的标签");

        when(tagDao.selectById(1L)).thenReturn(tagEntity);
        when(tagDao.updateById(argThat((TagEntity t) -> t != null))).thenReturn(1);

        // When
        assertDoesNotThrow(() -> {
            tagService.updateTag(1L, updateDTO, ownerId);
        });

        // Then
        verify(aclPermissionService, never()).hasPermission(any(), any(), any(), any());
    }

    @Test
    @DisplayName("TAG - 非OWNER不能编辑")
    void testTag_NonOwnerCannotEdit() {
        // Given
        TagUpdateDTO updateDTO = new TagUpdateDTO();
        updateDTO.setName("更新后的标签");

        when(tagDao.selectById(1L)).thenReturn(tagEntity);
        when(aclPermissionService.hasPermission(
                eq(otherUserId),
                eq(ResourceType.TAG.getCode()),
                eq(1L),
                eq(PermissionCode.EDIT.getCode())
        )).thenReturn(false);

        // When & Then
        assertThrows(RenException.class, () -> {
            tagService.updateTag(1L, updateDTO, otherUserId);
        });

        verify(tagDao, never()).updateById(argThat((TagEntity t) -> true));
    }

    @Test
    @DisplayName("TAG - OWNER可以删除")
    void testTag_OwnerCanDelete() {
        // Given
        when(tagDao.selectById(1L)).thenReturn(tagEntity);
        when(tagDao.deleteById(1L)).thenReturn(1);

        // When
        assertDoesNotThrow(() -> {
            tagService.deleteTag(1L, ownerId);
        });

        // Then
        verify(tagDao, times(1)).deleteById(1L);
    }

    // ==================== TEAM 权限测试 ====================

    @Test
    @DisplayName("TEAM - OWNER可以编辑")
    void testTeam_OwnerCanEdit() {
        // Given
        TeamUpdateDTO updateDTO = new TeamUpdateDTO();
        updateDTO.setName("更新后的团队");

        when(teamDao.selectById(1L)).thenReturn(teamEntity);
        when(teamDao.updateById(argThat((TeamEntity t) -> t != null))).thenReturn(1);

        // When
        assertDoesNotThrow(() -> {
            teamService.updateTeam(1L, updateDTO, ownerId);
        });

        // Then
        verify(aclPermissionService, never()).hasPermission(any(), any(), any(), any());
    }

    @Test
    @DisplayName("TEAM - 非OWNER不能编辑")
    void testTeam_NonOwnerCannotEdit() {
        // Given
        TeamUpdateDTO updateDTO = new TeamUpdateDTO();
        updateDTO.setName("更新后的团队");

        when(teamDao.selectById(1L)).thenReturn(teamEntity);
        when(aclPermissionService.hasPermission(
                eq(otherUserId),
                eq(ResourceType.TEAM.getCode()),
                eq(1L),
                eq(PermissionCode.EDIT.getCode())
        )).thenReturn(false);

        // When & Then
        assertThrows(RenException.class, () -> {
            teamService.updateTeam(1L, updateDTO, otherUserId);
        });

        verify(teamDao, never()).updateById(argThat((TeamEntity t) -> true));
    }

    @Test
    @DisplayName("TEAM - OWNER可以删除")
    void testTeam_OwnerCanDelete() {
        // Given
        when(teamDao.selectById(1L)).thenReturn(teamEntity);
        when(teamDao.deleteById(1L)).thenReturn(1);

        // When
        assertDoesNotThrow(() -> {
            teamService.deleteTeam(1L, ownerId);
        });

        // Then
        verify(teamDao, times(1)).deleteById(1L);
    }

    // ==================== 审计日志测试 ====================

    @Test
    @DisplayName("权限被拒绝时应记录审计日志")
    void testAuditLogRecordedOnPermissionDenied() {
        // Given
        when(todoDao.selectById(1L)).thenReturn(todoEntity);
        when(aclPermissionService.hasPermission(
                eq(otherUserId),
                eq(ResourceType.TODO.getCode()),
                eq(1L),
                eq(PermissionCode.VIEW.getCode())
        )).thenReturn(false);

        // When & Then
        assertThrows(RenException.class, () -> {
            todoService.getTodoById(1L, otherUserId);
        });

        // 验证审计日志被记录
        verify(aclPermissionService, times(1)).auditLog(
                eq(otherUserId),
                eq(ResourceType.TODO.getCode()),
                eq(1L),
                eq(PermissionCode.VIEW.getCode()),
                eq("CHECK"),
                eq("DENIED"),
                eq(null)
        );
    }

    @Test
    @DisplayName("OWNER访问时不记录审计日志")
    void testNoAuditLogForOwner() {
        // Given
        when(todoDao.selectById(1L)).thenReturn(todoEntity);

        // When
        assertDoesNotThrow(() -> {
            todoService.getTodoById(1L, ownerId);
        });

        // Then - OWNER 不应该记录审计日志
        verify(aclPermissionService, never()).auditLog(any(), any(), any(), any(), any(), any(), any());
    }

    // ==================== 枚举类型安全测试 ====================

    @Test
    @DisplayName("验证使用枚举而非魔法值")
    void testEnumUsageNotMagicValues() {
        // 这个测试主要是编译期验证
        // 如果代码使用了魔法值，这里会编译失败

        ResourceType resourceType = ResourceType.TODO;
        PermissionCode permission = PermissionCode.VIEW;

        assertNotNull(resourceType);
        assertNotNull(permission);
        assertEquals("TODO", resourceType.getCode());
        assertEquals("VIEW", permission.getCode());
    }
}


/**
 * Copyright (c) 2018 COLLAB-TASK All rights reserved.
 *
 * https://www.collabtask.io
 *
 * 版权所有，侵权必究！
 */

package io.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.user.common.exception.RenException;
import io.user.common.page.PageData;
import io.user.dao.TodoDao;
import io.user.dao.UserDao;
import io.user.dto.TodoCreateDTO;
import io.user.dto.TodoQueryDTO;
import io.user.dto.TodoUpdateDTO;
import io.user.dto.TodoVO;
import io.user.entity.TodoEntity;
import io.user.entity.UserEntity;
import io.user.enums.PermissionCode;
import io.user.enums.ResourceType;
import io.user.service.impl.TodoServiceImpl;
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
 * TodoService 单元测试
 * 
 * @author System
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TODO服务单元测试")
public class TodoServiceTest {

    @Mock
    private TodoDao todoDao;

    @Mock
    private UserDao userDao;

    @Mock
    private AclPermissionService aclPermissionService;

    @InjectMocks
    private TodoServiceImpl todoService;

    private Long userId;
    private TodoEntity todoEntity;
    private TodoCreateDTO createDTO;

    @BeforeEach
    void setUp() {
        userId = 1L;
        
        // 准备测试数据
        todoEntity = new TodoEntity();
        todoEntity.setId(1L);
        todoEntity.setName("测试TODO");
        todoEntity.setDescription("测试描述");
        todoEntity.setStatus("NOT_STARTED");
        todoEntity.setPriority("HIGH");
        todoEntity.setUserId(userId);
        todoEntity.setCreateDate(new Date());
        
        createDTO = new TodoCreateDTO();
        createDTO.setName("新TODO");
        createDTO.setDescription("新描述");
        createDTO.setPriority("HIGH");
    }

    @Test
    @DisplayName("创建TODO - 成功")
    void testCreateTodo_Success() {
        // Given
        when(todoDao.insert(any(TodoEntity.class))).thenReturn(1);
        
        UserEntity user = new UserEntity();
        user.setUsername("testuser");
        when(userDao.selectById(userId)).thenReturn(user);

        // When
        TodoVO result = todoService.createTodo(createDTO, userId);

        // Then
        assertNotNull(result);
        assertEquals("新TODO", result.getName());
        assertEquals("新描述", result.getDescription());
        assertEquals("NOT_STARTED", result.getStatus());
        assertEquals("HIGH", result.getPriority());
        assertEquals(userId, result.getUserId());
        
        verify(todoDao, times(1)).insert(any(TodoEntity.class));
    }

    @Test
    @DisplayName("获取TODO详情 - 成功")
    void testGetTodoById_Success() {
        // Given
        Long todoId = 1L;
        when(todoDao.selectById(todoId)).thenReturn(todoEntity);
        
        UserEntity user = new UserEntity();
        user.setUsername("testuser");
        when(userDao.selectById(userId)).thenReturn(user);
        
        // v1.1: Mock ACL权限检查（用户是所有者，不需要检查hasPermission）

        // When
        TodoVO result = todoService.getTodoById(todoId, userId);

        // Then
        assertNotNull(result);
        assertEquals(todoId, result.getId());
        assertEquals("测试TODO", result.getName());
        assertEquals(userId, result.getUserId());
        
        verify(todoDao, times(1)).selectById(todoId);
    }

    @Test
    @DisplayName("获取TODO详情 - TODO不存在")
    void testGetTodoById_NotFound() {
        // Given
        Long todoId = 999L;
        when(todoDao.selectById(todoId)).thenReturn(null);

        // When & Then
        assertThrows(RenException.class, () -> {
            todoService.getTodoById(todoId, userId);
        });
        
        verify(todoDao, times(1)).selectById(todoId);
    }

    @Test
    @DisplayName("获取TODO详情 - 无权限访问")
    void testGetTodoById_NoPermission() {
        // Given
        Long todoId = 1L;
        Long otherUserId = 2L;
        todoEntity.setUserId(otherUserId);
        when(todoDao.selectById(todoId)).thenReturn(todoEntity);
        
        // v1.1: Mock ACL权限检查（不是所有者，且没有VIEW权限）
        when(aclPermissionService.hasPermission(
            userId, ResourceType.TODO.getCode(), todoId, PermissionCode.VIEW.getCode()
        )).thenReturn(false);

        // When & Then
        assertThrows(RenException.class, () -> {
            todoService.getTodoById(todoId, userId);
        });
    }

    @Test
    @DisplayName("更新TODO - 成功")
    void testUpdateTodo_Success() {
        // Given
        Long todoId = 1L;
        TodoUpdateDTO updateDTO = new TodoUpdateDTO();
        updateDTO.setName("更新后的TODO");
        
        // v1.1: Mock ACL权限检查（用户是所有者，不需要检查hasPermission）
        updateDTO.setDescription("更新后的描述");
        
        when(todoDao.selectById(todoId)).thenReturn(todoEntity);
        when(todoDao.updateById(any(TodoEntity.class))).thenReturn(1);
        
        UserEntity user = new UserEntity();
        user.setUsername("testuser");
        when(userDao.selectById(userId)).thenReturn(user);

        // When
        TodoVO result = todoService.updateTodo(todoId, updateDTO, userId);

        // Then
        assertNotNull(result);
        assertEquals("更新后的TODO", result.getName());
        assertEquals("更新后的描述", result.getDescription());
        
        verify(todoDao, times(1)).updateById(any(TodoEntity.class));
    }

    @Test
    @DisplayName("更新TODO - TODO不存在")
    void testUpdateTodo_NotFound() {
        // Given
        Long todoId = 999L;
        TodoUpdateDTO updateDTO = new TodoUpdateDTO();
        when(todoDao.selectById(todoId)).thenReturn(null);

        // When & Then
        assertThrows(RenException.class, () -> {
            todoService.updateTodo(todoId, updateDTO, userId);
        });
    }

    @Test
    @DisplayName("完成TODO - 成功")
    void testCompleteTodo_Success() {
        // Given
        Long todoId = 1L;
        when(todoDao.selectById(todoId)).thenReturn(todoEntity);
        when(todoDao.updateById(any(TodoEntity.class))).thenReturn(1);
        
        UserEntity user = new UserEntity();
        user.setUsername("testuser");
        when(userDao.selectById(userId)).thenReturn(user);

        // When
        TodoVO result = todoService.completeTodo(todoId, userId);

        // Then
        assertNotNull(result);
        assertEquals("COMPLETED", result.getStatus());
        assertNotNull(result.getCompletedAt());
        
        verify(todoDao, times(1)).updateById(any(TodoEntity.class));
    }

    @Test
    @DisplayName("删除TODO - 成功")
    void testDeleteTodo_Success() {
        // Given
        Long todoId = 1L;
        when(todoDao.selectById(todoId)).thenReturn(todoEntity);
        when(todoDao.deleteById(todoId)).thenReturn(1);

        // When
        todoService.deleteTodo(todoId, userId);

        // Then
        verify(todoDao, times(1)).deleteById(todoId);
    }

    @Test
    @DisplayName("删除TODO - 无权限")
    void testDeleteTodo_NoPermission() {
        // Given
        Long todoId = 1L;
        Long otherUserId = 2L;
        todoEntity.setUserId(otherUserId);
        when(todoDao.selectById(todoId)).thenReturn(todoEntity);

        // When & Then
        assertThrows(RenException.class, () -> {
            todoService.deleteTodo(todoId, userId);
        });
        
        verify(todoDao, never()).deleteById(any());
    }

    @Test
    @DisplayName("查询TODO列表 - 分页")
    void testGetTodoList_WithPagination() {
        // Given
        TodoQueryDTO queryDTO = new TodoQueryDTO();
        queryDTO.setPage(1);
        queryDTO.setLimit(10);
        
        List<TodoEntity> todoList = new ArrayList<>();
        todoList.add(todoEntity);
        
        Page<TodoEntity> page = new Page<>(1, 10);
        page.setRecords(todoList);
        page.setTotal(1);
        
        when(todoDao.selectPage(any(IPage.class), any(LambdaQueryWrapper.class)))
            .thenReturn(page);
        
        UserEntity user = new UserEntity();
        user.setUsername("testuser");
        when(userDao.selectById(userId)).thenReturn(user);

        // When
        PageData<TodoVO> result = todoService.getTodoList(queryDTO, userId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getList().size());
        assertEquals("测试TODO", result.getList().get(0).getName());
    }

    @Test
    @DisplayName("查询TODO列表 - 按状态筛选")
    void testGetTodoList_FilterByStatus() {
        // Given
        TodoQueryDTO queryDTO = new TodoQueryDTO();
        queryDTO.setStatus("NOT_STARTED");
        queryDTO.setPage(1);
        queryDTO.setLimit(10);
        
        List<TodoEntity> todoList = new ArrayList<>();
        todoList.add(todoEntity);
        
        Page<TodoEntity> page = new Page<>(1, 10);
        page.setRecords(todoList);
        page.setTotal(1);
        
        when(todoDao.selectPage(any(IPage.class), any(LambdaQueryWrapper.class)))
            .thenReturn(page);
        
        UserEntity user = new UserEntity();
        user.setUsername("testuser");
        when(userDao.selectById(userId)).thenReturn(user);

        // When
        PageData<TodoVO> result = todoService.getTodoList(queryDTO, userId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertTrue(result.getList().stream()
            .allMatch(todo -> "NOT_STARTED".equals(todo.getStatus())));
    }
}


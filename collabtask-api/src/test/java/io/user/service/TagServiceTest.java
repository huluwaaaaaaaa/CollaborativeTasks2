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
import io.user.dao.TagDao;
import io.user.dao.TodoDao;
import io.user.dao.TodoTagDao;
import io.user.dto.TagCreateDTO;
import io.user.dto.TagUpdateDTO;
import io.user.dto.TagVO;
import io.user.entity.TagEntity;
import io.user.entity.TodoEntity;
import io.user.entity.TodoTagEntity;
import io.user.service.impl.TagServiceImpl;
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
 * TagService 单元测试
 * 
 * @author System
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("标签服务单元测试")
public class TagServiceTest {

    @Mock
    private TagDao tagDao;

    @Mock
    private TodoTagDao todoTagDao;

    @Mock
    private TodoDao todoDao;

    @InjectMocks
    private TagServiceImpl tagService;

    private Long userId;
    private TagEntity tagEntity;
    private TagCreateDTO createDTO;

    @BeforeEach
    void setUp() {
        userId = 1L;
        
        tagEntity = new TagEntity();
        tagEntity.setId(1L);
        tagEntity.setName("紧急");
        tagEntity.setColor("#FF0000");
        tagEntity.setUserId(userId);
        tagEntity.setCreateDate(new Date());
        
        createDTO = new TagCreateDTO();
        createDTO.setName("新标签");
        createDTO.setColor("#00FF00");
    }

    @Test
    @DisplayName("创建标签 - 成功")
    void testCreateTag_Success() {
        // Given
        when(tagDao.selectCount(any(QueryWrapper.class))).thenReturn(0L);
        when(tagDao.insert(any(TagEntity.class))).thenAnswer(invocation -> {
            TagEntity tag = invocation.getArgument(0);
            tag.setId(1L);
            return 1;
        });
        when(todoTagDao.selectCount(any(QueryWrapper.class))).thenReturn(0L);

        // When
        TagVO result = tagService.createTag(createDTO, userId);

        // Then
        assertNotNull(result);
        assertEquals("新标签", result.getName());
        assertEquals("#00FF00", result.getColor());
        assertEquals(0, result.getUsageCount());
        
        verify(tagDao, times(1)).insert(any(TagEntity.class));
    }

    @Test
    @DisplayName("创建标签 - 标签名已存在")
    void testCreateTag_NameExists() {
        // Given
        when(tagDao.selectCount(any(QueryWrapper.class))).thenReturn(1L);

        // When & Then
        assertThrows(RenException.class, () -> {
            tagService.createTag(createDTO, userId);
        });
        
        verify(tagDao, never()).insert(any(TagEntity.class));
    }

    @Test
    @DisplayName("获取标签列表 - 成功")
    void testGetMyTags_Success() {
        // Given
        List<TagEntity> tags = new ArrayList<>();
        tags.add(tagEntity);
        
        when(tagDao.selectList(any(QueryWrapper.class))).thenReturn(tags);
        when(todoTagDao.selectCount(any(QueryWrapper.class))).thenReturn(5L);

        // When
        List<TagVO> result = tagService.getMyTags(userId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("紧急", result.get(0).getName());
        assertEquals(5, result.get(0).getUsageCount());
    }

    @Test
    @DisplayName("获取标签详情 - 成功")
    void testGetTagById_Success() {
        // Given
        Long tagId = 1L;
        when(tagDao.selectById(tagId)).thenReturn(tagEntity);
        when(todoTagDao.selectCount(any(QueryWrapper.class))).thenReturn(3L);

        // When
        TagVO result = tagService.getTagById(tagId, userId);

        // Then
        assertNotNull(result);
        assertEquals("紧急", result.getName());
        assertEquals("#FF0000", result.getColor());
        assertEquals(3, result.getUsageCount());
    }

    @Test
    @DisplayName("获取标签详情 - 标签不存在")
    void testGetTagById_NotFound() {
        // Given
        Long tagId = 999L;
        when(tagDao.selectById(tagId)).thenReturn(null);

        // When & Then
        assertThrows(RenException.class, () -> {
            tagService.getTagById(tagId, userId);
        });
    }

    @Test
    @DisplayName("获取标签详情 - 无权限")
    void testGetTagById_NoPermission() {
        // Given
        Long tagId = 1L;
        Long otherUserId = 2L;
        tagEntity.setUserId(otherUserId);
        when(tagDao.selectById(tagId)).thenReturn(tagEntity);

        // When & Then
        assertThrows(RenException.class, () -> {
            tagService.getTagById(tagId, userId);
        });
    }

    @Test
    @DisplayName("更新标签 - 成功")
    void testUpdateTag_Success() {
        // Given
        Long tagId = 1L;
        TagUpdateDTO updateDTO = new TagUpdateDTO();
        updateDTO.setName("更新后的标签");
        updateDTO.setColor("#0000FF");
        
        when(tagDao.selectById(tagId)).thenReturn(tagEntity);
        when(tagDao.selectCount(any(QueryWrapper.class))).thenReturn(0L);
        when(tagDao.updateById(any(TagEntity.class))).thenReturn(1);
        when(todoTagDao.selectCount(any(QueryWrapper.class))).thenReturn(2L);

        // When
        TagVO result = tagService.updateTag(tagId, updateDTO, userId);

        // Then
        assertNotNull(result);
        assertEquals("更新后的标签", result.getName());
        assertEquals("#0000FF", result.getColor());
        
        verify(tagDao, times(1)).updateById(any(TagEntity.class));
    }

    @Test
    @DisplayName("删除标签 - 成功")
    void testDeleteTag_Success() {
        // Given
        Long tagId = 1L;
        when(tagDao.selectById(tagId)).thenReturn(tagEntity);
        when(todoTagDao.delete(any(QueryWrapper.class))).thenReturn(1);
        when(tagDao.deleteById(tagId)).thenReturn(1);

        // When
        tagService.deleteTag(tagId, userId);

        // Then
        verify(todoTagDao, times(1)).delete(any(QueryWrapper.class));
        verify(tagDao, times(1)).deleteById(tagId);
    }

    @Test
    @DisplayName("给TODO添加标签 - 成功")
    void testAddTagToTodo_Success() {
        // Given
        Long todoId = 1L;
        Long tagId = 1L;
        
        TodoEntity todo = new TodoEntity();
        todo.setId(todoId);
        todo.setUserId(userId);
        when(todoDao.selectById(todoId)).thenReturn(todo);
        
        when(tagDao.selectById(tagId)).thenReturn(tagEntity);
        when(todoTagDao.selectCount(any(QueryWrapper.class))).thenReturn(0L);
        when(todoTagDao.insert(any(TodoTagEntity.class))).thenReturn(1);

        // When
        tagService.addTagToTodo(todoId, tagId, userId);

        // Then
        verify(todoTagDao, times(1)).insert(any(TodoTagEntity.class));
    }

    @Test
    @DisplayName("给TODO添加标签 - TODO不存在")
    void testAddTagToTodo_TodoNotFound() {
        // Given
        Long todoId = 999L;
        Long tagId = 1L;
        when(todoDao.selectById(todoId)).thenReturn(null);

        // When & Then
        assertThrows(RenException.class, () -> {
            tagService.addTagToTodo(todoId, tagId, userId);
        });
        
        verify(todoTagDao, never()).insert(any(TodoTagEntity.class));
    }

    @Test
    @DisplayName("给TODO添加标签 - 标签已存在")
    void testAddTagToTodo_TagAlreadyExists() {
        // Given
        Long todoId = 1L;
        Long tagId = 1L;
        
        TodoEntity todo = new TodoEntity();
        todo.setId(todoId);
        todo.setUserId(userId);
        when(todoDao.selectById(todoId)).thenReturn(todo);
        
        when(tagDao.selectById(tagId)).thenReturn(tagEntity);
        when(todoTagDao.selectCount(any(QueryWrapper.class))).thenReturn(1L);

        // When & Then
        assertThrows(RenException.class, () -> {
            tagService.addTagToTodo(todoId, tagId, userId);
        });
        
        verify(todoTagDao, never()).insert(any(TodoTagEntity.class));
    }

    @Test
    @DisplayName("从TODO移除标签 - 成功")
    void testRemoveTagFromTodo_Success() {
        // Given
        Long todoId = 1L;
        Long tagId = 1L;
        
        TodoEntity todo = new TodoEntity();
        todo.setId(todoId);
        todo.setUserId(userId);
        when(todoDao.selectById(todoId)).thenReturn(todo);
        when(todoTagDao.delete(any(QueryWrapper.class))).thenReturn(1);

        // When
        tagService.removeTagFromTodo(todoId, tagId, userId);

        // Then
        verify(todoTagDao, times(1)).delete(any(QueryWrapper.class));
    }

    @Test
    @DisplayName("获取TODO的标签 - 成功")
    void testGetTodoTags_Success() {
        // Given
        Long todoId = 1L;
        
        TodoEntity todo = new TodoEntity();
        todo.setId(todoId);
        todo.setUserId(userId);
        when(todoDao.selectById(todoId)).thenReturn(todo);
        
        List<TodoTagEntity> todoTags = new ArrayList<>();
        TodoTagEntity todoTag = new TodoTagEntity();
        todoTag.setTodoId(todoId);
        todoTag.setTagId(1L);
        todoTags.add(todoTag);
        when(todoTagDao.selectList(any(QueryWrapper.class))).thenReturn(todoTags);
        
        List<TagEntity> tags = new ArrayList<>();
        tags.add(tagEntity);
        when(tagDao.selectBatchIds(anyList())).thenReturn(tags);
        when(todoTagDao.selectCount(any(QueryWrapper.class))).thenReturn(2L);

        // When
        List<TagVO> result = tagService.getTodoTags(todoId, userId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("紧急", result.get(0).getName());
    }
}


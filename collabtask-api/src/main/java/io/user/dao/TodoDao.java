/**
 * Copyright (c) 2018 COLLAB-TASK All rights reserved.
 *
 * https://www.collabtask.io
 *
 * 版权所有，侵权必究！
 */

package io.user.dao;

import io.user.common.dao.BaseDao;
import io.user.entity.TodoEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * TODO DAO
 * 
 * @author System
 */
@Mapper
public interface TodoDao extends BaseDao<TodoEntity> {
    // MyBatis-Plus 自动提供基础 CRUD 方法
    // 复杂查询可在 TodoDao.xml 中定义
}


/**
 * Copyright (c) 2018 COLLAB-TASK All rights reserved.
 *
 * https://www.collabtask.io
 *
 * 版权所有，侵权必究！
 */

package io.user.dao;

import io.user.common.dao.BaseDao;
import io.user.entity.TodoTagEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * TODO-标签关联 DAO
 * 
 * @author System
 */
@Mapper
public interface TodoTagDao extends BaseDao<TodoTagEntity> {
}


/**
 * Copyright (c) 2018 COLLAB-TASK All rights reserved.
 *
 * https://www.collabtask.io
 *
 * 版权所有，侵权必究！
 */

package io.user.dao;

import io.user.common.dao.BaseDao;
import io.user.entity.TeamMemberEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 团队成员 DAO
 * 
 * @author System
 */
@Mapper
public interface TeamMemberDao extends BaseDao<TeamMemberEntity> {
}


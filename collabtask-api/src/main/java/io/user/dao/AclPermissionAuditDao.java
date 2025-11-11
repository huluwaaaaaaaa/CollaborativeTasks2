/**
 * Copyright (c) 2018 COLLAB-TASK All rights reserved.
 *
 * https://www.collabtask.io
 *
 * 版权所有，侵权必究！
 */

package io.user.dao;

import io.user.common.dao.BaseDao;
import io.user.entity.AclPermissionAuditEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * ACL 权限审计 DAO
 * 
 * @author System
 */
@Mapper
public interface AclPermissionAuditDao extends BaseDao<AclPermissionAuditEntity> {
}


/**
 * Copyright (c) 2018 COLLAB-TASK All rights reserved.
 *
 * https://www.collabtask.io
 *
 * 版权所有，侵权必究！
 */

package io.user.dao;

import io.user.common.dao.BaseDao;
import io.user.entity.AclAccessControlEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * ACL 访问控制 DAO
 * 
 * @author System
 */
@Mapper
public interface AclAccessControlDao extends BaseDao<AclAccessControlEntity> {
}


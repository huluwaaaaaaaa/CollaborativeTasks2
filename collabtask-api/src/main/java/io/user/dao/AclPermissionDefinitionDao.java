/**
 * Copyright (c) 2018 COLLAB-TASK All rights reserved.
 *
 * https://www.collabtask.io
 *
 * 版权所有，侵权必究！
 */

package io.user.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.user.entity.AclPermissionDefinitionEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * ACL权限定义 DAO
 *
 * @author System
 */
@Mapper
public interface AclPermissionDefinitionDao extends BaseMapper<AclPermissionDefinitionEntity> {
	
}


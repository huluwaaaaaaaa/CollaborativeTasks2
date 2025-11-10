/**
 * Copyright (c) 2018 COLLAB-TASK All rights reserved.
 *
 * https://www.collabtask.io
 *
 * 版权所有，侵权必究！
 */

package io.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.user.dao.AclAccessControlDao;
import io.user.dao.AclPermissionAuditDao;
import io.user.entity.AclAccessControlEntity;
import io.user.entity.AclPermissionAuditEntity;
import io.user.service.AclPermissionService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * ACL 权限检查 Service 实现
 *
 * @author System
 */
@Service
@AllArgsConstructor
@Slf4j
public class AclPermissionServiceImpl implements AclPermissionService {
	
	private final AclAccessControlDao aclAccessControlDao;
	private final AclPermissionAuditDao aclPermissionAuditDao;
	
	@Override
	public boolean hasPermission(Long userId, String resourceType, Long resourceId, String permissionCode) {
		// 查询 ACL 表
		QueryWrapper<AclAccessControlEntity> wrapper = new QueryWrapper<>();
		wrapper.eq("user_id", userId)
			.eq("resource_type", resourceType)
			.eq("resource_id", resourceId)
			.eq("permission_code", permissionCode);
		
		// 检查是否过期
		wrapper.and(w -> w.isNull("expires_at").or().gt("expires_at", new Date()));
		
		Long count = aclAccessControlDao.selectCount(wrapper);
		return count > 0;
	}
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void grantPermission(Long targetUserId, String resourceType, Long resourceId, String permissionCode, Long grantedBy) {
		// 检查是否已存在
		QueryWrapper<AclAccessControlEntity> wrapper = new QueryWrapper<>();
		wrapper.eq("user_id", targetUserId)
			.eq("resource_type", resourceType)
			.eq("resource_id", resourceId)
			.eq("permission_code", permissionCode);
		
		if (aclAccessControlDao.selectCount(wrapper) > 0) {
			log.warn("权限已存在，跳过授权：userId={}, resourceType={}, resourceId={}, permissionCode={}", 
				targetUserId, resourceType, resourceId, permissionCode);
			return;
		}
		
		// 创建权限
		AclAccessControlEntity acl = new AclAccessControlEntity();
		acl.setUserId(targetUserId);
		acl.setResourceType(resourceType);
		acl.setResourceId(resourceId);
		acl.setPermissionCode(permissionCode);
		acl.setGrantedBy(grantedBy);
		acl.setGrantDate(new Date());
		// expiresAt 为 null 表示永久有效
		
		aclAccessControlDao.insert(acl);
		
		log.info("授权成功：userId={}, resourceType={}, resourceId={}, permissionCode={}", 
			targetUserId, resourceType, resourceId, permissionCode);
	}
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void revokePermission(Long targetUserId, String resourceType, Long resourceId, String permissionCode) {
		QueryWrapper<AclAccessControlEntity> wrapper = new QueryWrapper<>();
		wrapper.eq("user_id", targetUserId)
			.eq("resource_type", resourceType)
			.eq("resource_id", resourceId)
			.eq("permission_code", permissionCode);
		
		aclAccessControlDao.delete(wrapper);
		
		log.info("撤销权限成功：userId={}, resourceType={}, resourceId={}, permissionCode={}", 
			targetUserId, resourceType, resourceId, permissionCode);
	}
	
	@Override
	public void auditLog(Long userId, String resourceType, Long resourceId, String permissionCode, String action, String result, String ipAddress) {
		AclPermissionAuditEntity audit = new AclPermissionAuditEntity();
		audit.setUserId(userId);
		audit.setResourceType(resourceType);
		audit.setResourceId(resourceId);
		audit.setPermissionCode(permissionCode);
		audit.setAction(action);
		audit.setResult(result);
		audit.setIpAddress(ipAddress);
		audit.setCreateDate(new Date());
		
		aclPermissionAuditDao.insert(audit);
	}
}


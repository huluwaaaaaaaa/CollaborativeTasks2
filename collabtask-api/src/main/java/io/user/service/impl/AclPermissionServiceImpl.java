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
import io.user.dao.AclPermissionDefinitionDao;
import io.user.entity.AclAccessControlEntity;
import io.user.entity.AclPermissionAuditEntity;
import io.user.entity.AclPermissionDefinitionEntity;
import io.user.enums.SubjectType;
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
	private final AclPermissionDefinitionDao aclPermissionDefinitionDao;
	
	@Override
	public boolean hasPermission(Long userId, String resourceType, Long resourceId, String permissionCode) {
		// 查询ACL表（用户是主体）
		QueryWrapper<AclAccessControlEntity> wrapper = new QueryWrapper<>();
		wrapper.eq("subject_type", SubjectType.USER.getCode())
			.eq("subject_id", userId)
			.eq("resource_type", resourceType)
			.eq("resource_id", resourceId);
		
		// 通过permission_code关联查询permission_id
		// 先查询permission_id
		QueryWrapper<AclPermissionDefinitionEntity> permWrapper = new QueryWrapper<>();
		permWrapper.eq("resource_type", resourceType)
			.eq("permission_code", permissionCode)
			.eq("is_active", 1);
		AclPermissionDefinitionEntity permDef = aclPermissionDefinitionDao.selectOne(permWrapper);
		
		if (permDef == null) {
			return false; // 权限定义不存在
		}
		
		wrapper.eq("permission_id", permDef.getId().longValue());
		wrapper.eq("is_active", 1);
		
		// 检查是否过期
		wrapper.and(w -> w.isNull("expires_at").or().gt("expires_at", new Date()));
		
		Long count = aclAccessControlDao.selectCount(wrapper);
		return count > 0;
	}
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void grantPermission(Long targetUserId, String resourceType, Long resourceId, String permissionCode, Long grantedBy) {
		// 先查询permission_id
		QueryWrapper<AclPermissionDefinitionEntity> permWrapper = new QueryWrapper<>();
		permWrapper.eq("resource_type", resourceType)
			.eq("permission_code", permissionCode)
			.eq("is_active", 1);
		AclPermissionDefinitionEntity permDef = aclPermissionDefinitionDao.selectOne(permWrapper);
		
		if (permDef == null) {
			log.error("权限定义不存在：resourceType={}, permissionCode={}", resourceType, permissionCode);
			throw new RuntimeException("权限定义不存在");
		}
		
		// 检查是否已存在
		QueryWrapper<AclAccessControlEntity> wrapper = new QueryWrapper<>();
		wrapper.eq("subject_type", SubjectType.USER.getCode())
			.eq("subject_id", targetUserId)
			.eq("resource_type", resourceType)
			.eq("resource_id", resourceId)
			.eq("permission_id", permDef.getId().longValue())
			.eq("is_active", 1);
		
		if (aclAccessControlDao.selectCount(wrapper) > 0) {
			log.warn("权限已存在，跳过授权：userId={}, resourceType={}, resourceId={}, permissionCode={}", 
				targetUserId, resourceType, resourceId, permissionCode);
			return;
		}
		
		// 创建权限
		AclAccessControlEntity acl = new AclAccessControlEntity();
		acl.setSubjectType(SubjectType.USER.getCode());
		acl.setSubjectId(targetUserId);
		acl.setResourceType(resourceType);
		acl.setResourceId(resourceId);
		acl.setPermissionId(permDef.getId().longValue());
		// acl.setSourceType("SHARED");  // 数据库表中无此字段
		acl.setGrantedBy(grantedBy);
		acl.setGrantedAt(new Date());
		acl.setIsActive(1);
		// expiresAt 为 null 表示永久有效
		
		aclAccessControlDao.insert(acl);
		
		log.info("授权成功：userId={}, resourceType={}, resourceId={}, permissionCode={}, permissionId={}", 
			targetUserId, resourceType, resourceId, permissionCode, permDef.getId());
	}
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void revokePermission(Long targetUserId, String resourceType, Long resourceId, String permissionCode) {
		// 先查询permission_id
		QueryWrapper<AclPermissionDefinitionEntity> permWrapper = new QueryWrapper<>();
		permWrapper.eq("resource_type", resourceType)
			.eq("permission_code", permissionCode)
			.eq("is_active", 1);
		AclPermissionDefinitionEntity permDef = aclPermissionDefinitionDao.selectOne(permWrapper);
		
		if (permDef == null) {
			log.warn("权限定义不存在，跳过撤销：resourceType={}, permissionCode={}", resourceType, permissionCode);
			return;
		}
		
		QueryWrapper<AclAccessControlEntity> wrapper = new QueryWrapper<>();
		wrapper.eq("subject_type", SubjectType.USER.getCode())
			.eq("subject_id", targetUserId)
			.eq("resource_type", resourceType)
			.eq("resource_id", resourceId)
			.eq("permission_id", permDef.getId().longValue());
		
		aclAccessControlDao.delete(wrapper);
		
		log.info("撤销权限成功：userId={}, resourceType={}, resourceId={}, permissionCode={}", 
			targetUserId, resourceType, resourceId, permissionCode);
	}
	
	@Override
	public void auditLog(Long userId, String resourceType, Long resourceId, String permissionCode, String action, String result, String ipAddress) {
		// 注意：数据库表结构与接口参数不完全匹配，进行字段映射
		AclPermissionAuditEntity audit = new AclPermissionAuditEntity();
		audit.setSubjectType(SubjectType.USER.getCode());
		audit.setSubjectId(userId);
		audit.setResourceType(resourceType);
		audit.setResourceId(resourceId);
		audit.setPermissionCode(permissionCode);
		audit.setAction(action);
		audit.setOperatorId(userId);
		audit.setReason(result);  // result映射到reason字段
		audit.setIpAddress(ipAddress);
		audit.setCreateDate(new Date());
		
		aclPermissionAuditDao.insert(audit);
		
		log.info("权限审计：userId={}, resourceType={}, resourceId={}, permissionCode={}, action={}", 
			userId, resourceType, resourceId, permissionCode, action);
	}
}


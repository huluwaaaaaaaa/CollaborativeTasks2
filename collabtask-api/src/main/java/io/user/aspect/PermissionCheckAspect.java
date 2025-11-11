/**
 * Copyright (c) 2018 COLLAB-TASK All rights reserved.
 *
 * https://www.collabtask.io
 *
 * 版权所有，侵权必究！
 */

package io.user.aspect;

import io.user.common.annotation.RequirePermission;
import io.user.common.exception.RenException;
import io.user.dao.TodoDao;
import io.user.dao.TagDao;
import io.user.dao.TeamDao;
import io.user.entity.TodoEntity;
import io.user.entity.TagEntity;
import io.user.entity.TeamEntity;
import io.user.enums.ResourceType;
import io.user.service.AclPermissionService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 权限检查切面（v2.0 重构版）
 * 
 * 变更说明：
 * 1. 使用方法参数名称获取参数（需要编译器保留参数名）
 * 2. 支持 OWNER 自动检查
 * 3. 自动记录审计日志
 * 
 * @author System
 */
@Aspect
@Component
@AllArgsConstructor
@Slf4j
public class PermissionCheckAspect {
	
	private final AclPermissionService aclPermissionService;
	private final TodoDao todoDao;
	private final TagDao tagDao;
	private final TeamDao teamDao;
	
	@Around("@annotation(io.user.common.annotation.RequirePermission)")
	public Object around(ProceedingJoinPoint point) throws Throwable {
		MethodSignature signature = (MethodSignature) point.getSignature();
		Method method = signature.getMethod();
		RequirePermission annotation = method.getAnnotation(RequirePermission.class);
		
		// ⭐ 使用 MethodSignature.getParameterNames() 获取参数名
		String[] parameterNames = signature.getParameterNames();
		Object[] args = point.getArgs();
		
		Long resourceId = null;
		Long userId = null;
		
		// 查找 resourceId 和 userId
		for (int i = 0; i < parameterNames.length; i++) {
			if (parameterNames[i].equals(annotation.resourceIdParam())) {
				resourceId = (Long) args[i];
			}
			if (parameterNames[i].equals(annotation.userIdParam())) {
				userId = (Long) args[i];
			}
		}
		
		if (resourceId == null || userId == null) {
			log.error("权限检查失败：未找到 resourceId={} 或 userId={}", resourceId, userId);
			throw new RenException("权限检查参数错误");
		}
		
		// 获取枚举的 code 值
		String resourceTypeCode = annotation.resourceType().getCode();
		String permissionCode = annotation.permission().getCode();
		
		// ⭐ 步骤1：检查是否是 OWNER
		if (annotation.checkOwner()) {
			boolean isOwner = checkOwner(resourceTypeCode, resourceId, userId);
			if (isOwner) {
				log.debug("用户是 OWNER，跳过 ACL 权限检查：userId={}, resourceType={}, resourceId={}", 
					userId, resourceTypeCode, resourceId);
				return point.proceed();
			}
		}
		
		// ⭐ 步骤2：检查 ACL 权限
		boolean hasPermission = aclPermissionService.hasPermission(
			userId, 
			resourceTypeCode, 
			resourceId, 
			permissionCode
		);
		
		if (!hasPermission) {
			log.warn("权限不足：userId={}, resourceType={}, resourceId={}, permission={}", 
				userId, resourceTypeCode, resourceId, permissionCode);
			
			// ⭐ 记录审计日志（权限被拒绝）
			aclPermissionService.auditLog(
				userId, 
				resourceTypeCode, 
				resourceId, 
				permissionCode, 
				"CHECK", 
				"DENIED", 
				null
			);
			
			throw new RenException("权限不足");
		}
		
		log.debug("ACL 权限检查通过：userId={}, resourceType={}, resourceId={}, permission={}", 
			userId, resourceTypeCode, resourceId, permissionCode);
		
		// 执行方法
		return point.proceed();
	}
	
	/**
	 * 检查是否是资源的 OWNER
	 * 
	 * @param resourceTypeCode 资源类型代码（使用枚举的 code 值）
	 * @param resourceId 资源ID
	 * @param userId 用户ID
	 * @return true=是OWNER，false=不是OWNER
	 */
	private boolean checkOwner(String resourceTypeCode, Long resourceId, Long userId) {
		try {
			// ⭐ 使用枚举值进行判断（避免魔法值）
			if (ResourceType.TODO.getCode().equals(resourceTypeCode)) {
				TodoEntity todo = todoDao.selectById(resourceId);
				return todo != null && todo.getUserId().equals(userId);
			}
			
			if (ResourceType.TAG.getCode().equals(resourceTypeCode)) {
				TagEntity tag = tagDao.selectById(resourceId);
				return tag != null && tag.getUserId().equals(userId);
			}
			
			if (ResourceType.TEAM.getCode().equals(resourceTypeCode)) {
				TeamEntity team = teamDao.selectById(resourceId);
				return team != null && team.getOwnerId().equals(userId);
			}
			
			log.warn("未知的资源类型：{}", resourceTypeCode);
			return false;
			
		} catch (Exception e) {
			log.error("检查 OWNER 失败：resourceType={}, resourceId={}", resourceTypeCode, resourceId, e);
			return false;
		}
	}
}


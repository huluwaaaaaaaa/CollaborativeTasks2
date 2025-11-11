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
import io.user.service.AclPermissionService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * 权限检查切面
 * 
 * @author System
 */
@Aspect
@Component
@AllArgsConstructor
@Slf4j
public class PermissionCheckAspect {
	
	private final AclPermissionService aclPermissionService;
	
	@Around("@annotation(io.user.common.annotation.RequirePermission)")
	public Object around(ProceedingJoinPoint point) throws Throwable {
		MethodSignature signature = (MethodSignature) point.getSignature();
		Method method = signature.getMethod();
		RequirePermission annotation = method.getAnnotation(RequirePermission.class);
		
		// 获取参数值
		Parameter[] parameters = method.getParameters();
		Object[] args = point.getArgs();
		
		Long resourceId = null;
		Long userId = null;
		
		// 查找 resourceId 和 userId
		for (int i = 0; i < parameters.length; i++) {
			String paramName = parameters[i].getName();
			if (paramName.equals(annotation.resourceIdParam())) {
				resourceId = (Long) args[i];
			}
			if (paramName.equals(annotation.userIdParam())) {
				userId = (Long) args[i];
			}
		}
		
		if (resourceId == null || userId == null) {
			log.error("权限检查失败：未找到 resourceId 或 userId");
			throw new RenException("权限检查参数错误");
		}
		
		// 检查权限
		boolean hasPermission = aclPermissionService.hasPermission(
			userId, 
			annotation.resourceType(), 
			resourceId, 
			annotation.permissionCode()
		);
		
		if (!hasPermission) {
			log.warn("权限不足：userId={}, resourceType={}, resourceId={}, permissionCode={}", 
				userId, annotation.resourceType(), resourceId, annotation.permissionCode());
			
			// 记录审计日志
			aclPermissionService.auditLog(
				userId, 
				annotation.resourceType(), 
				resourceId, 
				annotation.permissionCode(), 
				"CHECK", 
				"DENIED", 
				null
			);
			
			throw new RenException("权限不足");
		}
		
		log.debug("权限检查通过：userId={}, resourceType={}, resourceId={}, permissionCode={}", 
			userId, annotation.resourceType(), resourceId, annotation.permissionCode());
		
		// 执行方法
		return point.proceed();
	}
}


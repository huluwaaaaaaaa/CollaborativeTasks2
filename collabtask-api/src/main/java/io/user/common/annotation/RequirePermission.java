/**
 * Copyright (c) 2018 COLLAB-TASK All rights reserved.
 *
 * https://www.collabtask.io
 *
 * 版权所有，侵权必究！
 */

package io.user.common.annotation;

import io.user.enums.PermissionCode;
import io.user.enums.ResourceType;

import java.lang.annotation.*;

/**
 * 权限检查注解（v2.1 枚举版）
 * 
 * 功能：
 * 1. 自动检查用户是否是资源的 OWNER
 * 2. 如果不是 OWNER，检查 ACL 权限
 * 3. 自动记录审计日志
 * 
 * v2.1 变更：使用枚举替代魔法值
 * 
 * 使用示例：
 * @RequirePermission(
 *     resourceType = ResourceType.TODO, 
 *     permission = PermissionCode.EDIT,
 *     checkOwner = true  // 自动检查 OWNER
 * )
 * public TodoVO updateTodo(Long id, TodoUpdateDTO dto, Long userId) {
 *     // 业务逻辑（切面已自动检查权限）
 * }
 * 
 * @author System
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {
	
	/**
	 * ⭐ 资源类型（使用枚举，避免魔法值）
	 */
	ResourceType resourceType();
	
	/**
	 * ⭐ 权限代码（使用枚举，避免魔法值）
	 */
	PermissionCode permission();
	
	/**
	 * 资源ID参数名（默认为 "id"）
	 */
	String resourceIdParam() default "id";
	
	/**
	 * 用户ID参数名（默认为 "userId"）
	 */
	String userIdParam() default "userId";
	
	/**
	 * ⭐ 是否检查 OWNER（默认 true）
	 * 
	 * true：先检查用户是否是资源的 OWNER，如果是则直接通过
	 * false：只检查 ACL 权限，不检查 OWNER
	 */
	boolean checkOwner() default true;
}


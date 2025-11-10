/**
 * Copyright (c) 2018 COLLAB-TASK All rights reserved.
 *
 * https://www.collabtask.io
 *
 * 版权所有，侵权必究！
 */

package io.user.common.annotation;

import java.lang.annotation.*;

/**
 * 权限检查注解
 * 
 * 使用示例：
 * @RequirePermission(resourceType = "TODO", permissionCode = "TODO_EDIT")
 * public void updateTodo(Long id, TodoUpdateDTO dto, Long userId) {
 *     // 业务逻辑
 * }
 * 
 * @author System
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {
	
	/**
	 * 资源类型（TODO, TEAM, TAG等）
	 */
	String resourceType();
	
	/**
	 * 权限代码（TODO_VIEW, TODO_EDIT, TODO_DELETE等）
	 */
	String permissionCode();
	
	/**
	 * 资源ID参数名（默认为 "id"）
	 */
	String resourceIdParam() default "id";
	
	/**
	 * 用户ID参数名（默认为 "userId"）
	 */
	String userIdParam() default "userId";
}


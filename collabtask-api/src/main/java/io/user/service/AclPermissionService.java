/**
 * Copyright (c) 2018 COLLAB-TASK All rights reserved.
 *
 * https://www.collabtask.io
 *
 * 版权所有，侵权必究！
 */

package io.user.service;

/**
 * ACL 权限检查 Service
 *
 * @author System
 */
public interface AclPermissionService {
	
	/**
	 * 检查用户是否有权限
	 * 
	 * @param userId 用户ID
	 * @param resourceType 资源类型（TODO, TEAM, TAG等）
	 * @param resourceId 资源ID
	 * @param permissionCode 权限代码（TODO_VIEW, TODO_EDIT等）
	 * @return true=有权限, false=无权限
	 */
	boolean hasPermission(Long userId, String resourceType, Long resourceId, String permissionCode);
	
	/**
	 * 授予权限
	 * 
	 * @param targetUserId 被授权用户ID
	 * @param resourceType 资源类型
	 * @param resourceId 资源ID
	 * @param permissionCode 权限代码
	 * @param grantedBy 授权人ID
	 */
	void grantPermission(Long targetUserId, String resourceType, Long resourceId, String permissionCode, Long grantedBy);
	
	/**
	 * 撤销权限
	 * 
	 * @param targetUserId 被撤销用户ID
	 * @param resourceType 资源类型
	 * @param resourceId 资源ID
	 * @param permissionCode 权限代码
	 */
	void revokePermission(Long targetUserId, String resourceType, Long resourceId, String permissionCode);
	
	/**
	 * 记录权限审计日志
	 * 
	 * @param userId 用户ID
	 * @param resourceType 资源类型
	 * @param resourceId 资源ID
	 * @param permissionCode 权限代码
	 * @param action 操作（GRANT, REVOKE, CHECK等）
	 * @param result 结果（SUCCESS, DENIED等）
	 * @param ipAddress IP地址
	 */
	void auditLog(Long userId, String resourceType, Long resourceId, String permissionCode, String action, String result, String ipAddress);
}


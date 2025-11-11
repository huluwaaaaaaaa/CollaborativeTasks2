/**
 * Copyright (c) 2018 COLLAB-TASK All rights reserved.
 *
 * https://www.collabtask.io
 *
 * 版权所有，侵权必究！
 */

package io.user.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 权限代码枚举
 * 
 * 对应数据库表：tb_acl_permission_definitions.permission_code
 *
 * @author System
 * @since v1.1
 */
@Getter
@AllArgsConstructor
public enum PermissionCode {
	
	/** 查看权限 */
	VIEW("VIEW", "查看"),
	
	/** 编辑权限 */
	EDIT("EDIT", "编辑"),
	
	/** 删除权限 */
	DELETE("DELETE", "删除"),
	
	/** 分享权限 */
	SHARE("SHARE", "分享"),
	
	/** 所有者权限（最高权限） */
	OWNER("OWNER", "所有者");
	
	/** 权限代码（数据库存储值） */
	private final String code;
	
	/** 权限名称 */
	private final String name;
	
	/**
	 * 根据code获取枚举
	 */
	public static PermissionCode fromCode(String code) {
		for (PermissionCode permission : values()) {
			if (permission.code.equals(code)) {
				return permission;
			}
		}
		throw new IllegalArgumentException("未知的权限代码: " + code);
	}
}


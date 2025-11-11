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
 * ACL操作类型枚举
 * 
 * 用于权限审计日志
 *
 * @author System
 * @since v1.1
 */
@Getter
@AllArgsConstructor
public enum AclAction {
	
	/** 授予权限 */
	GRANT("GRANT", "授予"),
	
	/** 撤销权限 */
	REVOKE("REVOKE", "撤销"),
	
	/** 检查权限 */
	CHECK("CHECK", "检查"),
	
	/** 拒绝访问 */
	DENY("DENY", "拒绝");
	
	/** 操作代码 */
	private final String code;
	
	/** 操作名称 */
	private final String name;
}


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
 * ACL主体类型枚举
 * 
 * 对应数据库表：tb_acl_access_control.subject_type
 *
 * @author System
 * @since v1.1
 */
@Getter
@AllArgsConstructor
public enum SubjectType {
	
	/** 用户主体 */
	USER("USER", "用户"),
	
	/** 团队主体 */
	TEAM("TEAM", "团队");
	
	/** 主体类型代码 */
	private final String code;
	
	/** 主体类型名称 */
	private final String name;
	
	/**
	 * 根据code获取枚举
	 */
	public static SubjectType fromCode(String code) {
		for (SubjectType type : values()) {
			if (type.code.equals(code)) {
				return type;
			}
		}
		throw new IllegalArgumentException("未知的主体类型: " + code);
	}
}


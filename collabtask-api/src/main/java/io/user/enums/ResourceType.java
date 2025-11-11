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
 * 资源类型枚举
 * 
 * 对应ACL权限系统中的资源类型
 *
 * @author System
 * @since v1.1
 */
@Getter
@AllArgsConstructor
public enum ResourceType {
	
	/** TODO资源 */
	TODO("TODO", "待办事项"),
	
	/** 团队资源 */
	TEAM("TEAM", "团队"),
	
	/** 标签资源 */
	TAG("TAG", "标签");
	
	/** 资源类型代码 */
	private final String code;
	
	/** 资源类型名称 */
	private final String name;
	
	/**
	 * 根据code获取枚举
	 */
	public static ResourceType fromCode(String code) {
		for (ResourceType type : values()) {
			if (type.code.equals(code)) {
				return type;
			}
		}
		throw new IllegalArgumentException("未知的资源类型: " + code);
	}
}


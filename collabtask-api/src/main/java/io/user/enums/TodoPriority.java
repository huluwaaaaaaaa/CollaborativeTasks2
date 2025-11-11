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
 * TODO优先级枚举
 * 
 * 对应数据库表：tb_todos.priority
 *
 * @author System
 */
@Getter
@AllArgsConstructor
public enum TodoPriority {
	
	/** 低优先级 */
	LOW("LOW", "低"),
	
	/** 中优先级 */
	MEDIUM("MEDIUM", "中"),
	
	/** 高优先级 */
	HIGH("HIGH", "高"),
	
	/** 紧急 */
	URGENT("URGENT", "紧急");
	
	/** 优先级代码 */
	private final String code;
	
	/** 优先级名称 */
	private final String name;
	
	/**
	 * 根据code获取枚举
	 */
	public static TodoPriority fromCode(String code) {
		for (TodoPriority priority : values()) {
			if (priority.code.equals(code)) {
				return priority;
			}
		}
		throw new IllegalArgumentException("未知的TODO优先级: " + code);
	}
}


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
 * TODO状态枚举
 * 
 * 对应数据库表：tb_todos.status
 *
 * @author System
 */
@Getter
@AllArgsConstructor
public enum TodoStatus {
	
	/** 未开始 */
	NOT_STARTED("NOT_STARTED", "未开始"),
	
	/** 进行中 */
	IN_PROGRESS("IN_PROGRESS", "进行中"),
	
	/** 已完成 */
	COMPLETED("COMPLETED", "已完成");
	
	/** 状态代码 */
	private final String code;
	
	/** 状态名称 */
	private final String name;
	
	/**
	 * 根据code获取枚举
	 */
	public static TodoStatus fromCode(String code) {
		for (TodoStatus status : values()) {
			if (status.code.equals(code)) {
				return status;
			}
		}
		throw new IllegalArgumentException("未知的TODO状态: " + code);
	}
}


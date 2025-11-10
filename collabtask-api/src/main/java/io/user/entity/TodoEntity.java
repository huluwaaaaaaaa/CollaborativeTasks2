/**
 * Copyright (c) 2018 COLLAB-TASK All rights reserved.
 *
 * https://www.collabtask.io
 *
 * 版权所有，侵权必究！
 */

package io.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * TODO 实体类
 * 
 * 对应数据库表：tb_todos
 * 字段检查状态：✅ 已与数据库表结构对照验证（11个字段）
 * 
 * @author System
 * @since 2025-11-10
 */
@Data
@TableName("tb_todos")
public class TodoEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * TODO ID（主键）
	 * 数据库字段：id BIGINT NOT NULL AUTO_INCREMENT
	 */
	@TableId(type = IdType.AUTO)
	private Long id;
	
	/**
	 * TODO 名称
	 * 数据库字段：name VARCHAR(200) NOT NULL
	 */
	@TableField("name")
	private String name;
	
	/**
	 * TODO 描述（Markdown）
	 * 数据库字段：description TEXT DEFAULT NULL
	 */
	@TableField("description")
	private String description;
	
	/**
	 * 截止日期
	 * 数据库字段：due_date DATE DEFAULT NULL
	 */
	@TableField("due_date")
	private Date dueDate;
	
	/**
	 * 状态：NOT_STARTED/IN_PROGRESS/COMPLETED
	 * 数据库字段：status VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED'
	 */
	@TableField("status")
	private String status;
	
	/**
	 * 优先级：LOW/MEDIUM/HIGH/URGENT
	 * 数据库字段：priority VARCHAR(20) DEFAULT 'MEDIUM'
	 */
	@TableField("priority")
	private String priority;
	
	/**
	 * 创建者ID
	 * 数据库字段：user_id BIGINT NOT NULL
	 */
	@TableField("user_id")
	private Long userId;
	
	/**
	 * 所属团队ID（NULL=个人TODO）
	 * 数据库字段：team_id BIGINT DEFAULT NULL
	 */
	@TableField("team_id")
	private Long teamId;
	
	/**
	 * 完成时间
	 * 数据库字段：completed_at DATETIME DEFAULT NULL
	 */
	@TableField("completed_at")
	private Date completedAt;
	
	/**
	 * 创建时间
	 * 数据库字段：create_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
	 */
	@TableField("create_date")
	private Date createDate;
	
	/**
	 * 更新时间
	 * 数据库字段：update_date DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP
	 */
	@TableField("update_date")
	private Date updateDate;
}


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
 * TODO-标签关联实体类
 * 
 * 对应数据库表：tb_todo_tags
 * 
 * @author System
 * @since 2025-11-10
 */
@Data
@TableName("tb_todo_tags")
public class TodoTagEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * TODO ID（联合主键之一）
	 */
	@TableId(type = IdType.NONE)
	@TableField("todo_id")
	private Long todoId;
	
	/**
	 * 标签ID（联合主键之一）
	 */
	@TableField("tag_id")
	private Long tagId;
	
	/**
	 * 创建时间
	 */
	@TableField("create_date")
	private Date createDate;
}


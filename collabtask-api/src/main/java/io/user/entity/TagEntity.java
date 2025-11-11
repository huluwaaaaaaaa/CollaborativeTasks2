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
 * 标签实体类
 * 
 * 对应数据库表：tb_tags
 * 
 * @author System
 * @since 2025-11-10
 */
@Data
@TableName("tb_tags")
public class TagEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 标签ID（主键）
	 */
	@TableId(type = IdType.AUTO)
	private Long id;
	
	/**
	 * 标签名称
	 */
	@TableField("name")
	private String name;
	
	/**
	 * 标签颜色（Hex格式）
	 */
	@TableField("color")
	private String color;
	
	/**
	 * 用户ID
	 */
	@TableField("user_id")
	private Long userId;
	
	/**
	 * 创建时间
	 */
	@TableField("create_date")
	private Date createDate;
	
	/**
	 * 更新时间
	 */
	@TableField("update_date")
	private Date updateDate;
}


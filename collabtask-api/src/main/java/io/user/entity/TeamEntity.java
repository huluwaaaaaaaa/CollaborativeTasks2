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
 * 团队实体类
 * 
 * 对应数据库表：tb_teams
 * 字段检查状态：✅ 已与数据库表结构对照验证（6个字段）
 * 
 * @author System
 * @since 2025-11-10
 */
@Data
@TableName("tb_teams")
public class TeamEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 团队ID（主键）
	 * 数据库字段：id BIGINT NOT NULL AUTO_INCREMENT
	 */
	@TableId(type = IdType.AUTO)
	private Long id;
	
	/**
	 * 团队名称
	 * 数据库字段：name VARCHAR(100) NOT NULL
	 */
	@TableField("name")
	private String name;
	
	/**
	 * 团队描述
	 * 数据库字段：description VARCHAR(500) DEFAULT NULL
	 */
	@TableField("description")
	private String description;
	
	/**
	 * 创建者ID（所有者）
	 * 数据库字段：owner_id BIGINT NOT NULL
	 */
	@TableField("owner_id")
	private Long ownerId;
	
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


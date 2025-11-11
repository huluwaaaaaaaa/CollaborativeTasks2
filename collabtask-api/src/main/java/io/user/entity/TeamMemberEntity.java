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
 * 团队成员实体类
 * 
 * 对应数据库表：tb_team_members
 * 字段检查状态：✅ 已与数据库表结构对照验证（4个字段）
 * 
 * @author System
 * @since 2025-11-10
 */
@Data
@TableName("tb_team_members")
public class TeamMemberEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 成员ID（主键）
	 * 数据库字段：id BIGINT NOT NULL AUTO_INCREMENT
	 */
	@TableId(type = IdType.AUTO)
	private Long id;
	
	/**
	 * 团队ID
	 * 数据库字段：team_id BIGINT NOT NULL
	 */
	@TableField("team_id")
	private Long teamId;
	
	/**
	 * 用户ID
	 * 数据库字段：user_id BIGINT NOT NULL
	 */
	@TableField("user_id")
	private Long userId;
	
	/**
	 * 创建时间（加入时间）
	 * 数据库字段：create_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
	 */
	@TableField("create_date")
	private Date createDate;
}


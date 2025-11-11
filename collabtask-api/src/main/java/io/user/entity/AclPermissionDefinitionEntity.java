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
 * ACL 权限定义实体类
 * 
 * 对应数据库表：tb_acl_permission_definitions
 * 
 * @author System
 * @since 2025-11-10
 */
@Data
@TableName("tb_acl_permission_definitions")
public class AclPermissionDefinitionEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@TableId(type = IdType.AUTO)
	private Integer id;
	
	@TableField("resource_type")
	private String resourceType;
	
	@TableField("permission_code")
	private String permissionCode;
	
	@TableField("permission_name")
	private String permissionName;
	
	@TableField("description")
	private String description;
	
	@TableField("level")
	private Integer level;
	
	// @TableField("implies")  // 数据库表中无此字段，暂时注释
	// private String implies;
	
	@TableField("is_active")
	private Integer isActive;
	
	@TableField("create_date")
	private Date createDate;
	
	// @TableField("update_date")  // 数据库表中无此字段
	// private Date updateDate;
}


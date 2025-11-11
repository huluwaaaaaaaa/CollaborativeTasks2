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
 * ACL 访问控制实体类
 * 
 * 对应数据库表：tb_acl_access_control
 * 
 * @author System
 * @since 2025-11-10
 */
@Data
@TableName("tb_acl_access_control")
public class AclAccessControlEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@TableId(type = IdType.AUTO)
	private Long id;
	
	@TableField("subject_type")
	private String subjectType;
	
	@TableField("subject_id")
	private Long subjectId;
	
	@TableField("resource_type")
	private String resourceType;
	
	@TableField("resource_id")
	private Long resourceId;
	
	@TableField("permission_id")
	private Long permissionId;
	
	// @TableField("source_type")  // 数据库表中无此字段
	// private String sourceType;
	
	// @TableField("source_id")  // 数据库表中无此字段
	// private Long sourceId;
	
	@TableField("granted_by")
	private Long grantedBy;
	
	@TableField("granted_at")
	private Date grantedAt;
	
	@TableField("expires_at")
	private Date expiresAt;
	
	@TableField("is_active")
	private Integer isActive;
	
	@TableField("revoked_by")
	private Long revokedBy;
	
	@TableField("revoked_at")
	private Date revokedAt;
	
	@TableField("revoke_reason")
	private String revokeReason;
	
	@TableField("create_date")
	private Date createDate;
	
	// @TableField("update_date")  // 数据库表中无此字段
	// private Date updateDate;
}


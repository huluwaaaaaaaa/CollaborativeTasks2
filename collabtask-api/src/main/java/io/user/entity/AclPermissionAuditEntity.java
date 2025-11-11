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
 * ACL 权限审计日志实体类
 * 
 * 对应数据库表：tb_acl_permission_audit
 * 
 * @author System
 * @since 2025-11-10
 */
@Data
@TableName("tb_acl_permission_audit")
public class AclPermissionAuditEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@TableId(type = IdType.AUTO)
	private Long id;
	
	@TableField("acl_id")
	private Long aclId;
	
	@TableField("action")
	private String action;
	
	@TableField("subject_type")
	private String subjectType;
	
	@TableField("subject_id")
	private Long subjectId;
	
	@TableField("resource_type")
	private String resourceType;
	
	@TableField("resource_id")
	private Long resourceId;
	
	@TableField("permission_code")
	private String permissionCode;
	
	@TableField("operator_id")
	private Long operatorId;
	
	@TableField("reason")
	private String reason;
	
	@TableField("ip_address")
	private String ipAddress;
	
	@TableField("user_agent")
	private String userAgent;
	
	@TableField("create_date")
	private Date createDate;
}


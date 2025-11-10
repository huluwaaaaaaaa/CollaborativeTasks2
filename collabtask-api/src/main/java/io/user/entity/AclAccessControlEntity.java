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
	
	@TableField("user_id")
	private Long userId;
	
	@TableField("resource_type")
	private String resourceType;
	
	@TableField("resource_id")
	private Long resourceId;
	
	@TableField("permission_code")
	private String permissionCode;
	
	@TableField("granted_by")
	private Long grantedBy;
	
	@TableField("grant_date")
	private Date grantDate;
	
	@TableField("expires_at")
	private Date expiresAt;
}


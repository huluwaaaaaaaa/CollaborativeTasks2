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
 * ACL 用户角色关联实体类
 * 
 * 对应数据库表：tb_acl_user_roles
 * 
 * @author System
 * @since 2025-11-10
 */
@Data
@TableName("tb_acl_user_roles")
public class AclUserRoleEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@TableId(type = IdType.AUTO)
	private Long id;
	
	@TableField("user_id")
	private Long userId;
	
	@TableField("role_code")
	private String roleCode;
	
	@TableField("create_date")
	private Date createDate;
}


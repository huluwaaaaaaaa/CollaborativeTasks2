/**
 * Copyright (c) 2018 COLLAB-TASK All rights reserved.
 *
 * https://www.collabtask.io
 *
 * 版权所有，侵权必究！
 */

package io.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 团队成员响应 VO
 *
 * @author System
 */
@Data
@Schema(description = "团队成员响应")
public class TeamMemberVO implements Serializable {
	private static final long serialVersionUID = 1L;

	@Schema(description = "成员ID", example = "1")
	private Long id;
	
	@Schema(description = "用户ID", example = "1067246875900000001")
	private Long userId;
	
	@Schema(description = "用户名", example = "john")
	private String username;
	
	@Schema(description = "全名", example = "John Doe")
	private String fullName;
	
	@Schema(description = "头像URL")
	private String avatarUrl;
	
	@Schema(description = "是否为团队所有者", example = "false")
	private Boolean isOwner;
	
	@Schema(description = "加入时间", example = "2025-11-10 10:00:00")
	private Date joinDate;

}


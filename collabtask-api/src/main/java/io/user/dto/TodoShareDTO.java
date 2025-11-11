/**
 * Copyright (c) 2018 COLLAB-TASK All rights reserved.
 *
 * https://www.collabtask.io
 *
 * 版权所有，侵权必究！
 */

package io.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * TODO共享请求 DTO
 *
 * @author System
 * @since v1.1
 */
@Data
@Schema(description = "TODO共享请求")
public class TodoShareDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	@NotNull(message = "被共享用户ID不能为空")
	@Schema(description = "被共享用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1067246875900000002")
	private Long targetUserId;
	
	@NotBlank(message = "权限类型不能为空")
	@Schema(description = "权限类型：VIEW（仅查看）, EDIT（可编辑）", 
		requiredMode = Schema.RequiredMode.REQUIRED, 
		example = "VIEW",
		allowableValues = {"VIEW", "EDIT"})
	private String permission;

}


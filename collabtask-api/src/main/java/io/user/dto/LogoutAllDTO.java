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

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 登出所有设备请求 DTO
 *
 * @author System
 */
@Data
@Schema(description = "登出所有设备请求")
public class LogoutAllDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	@NotBlank(message = "Refresh Token 不能为空")
	@Schema(description = "Refresh Token", requiredMode = Schema.RequiredMode.REQUIRED)
	private String refreshToken;

}


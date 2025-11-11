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
import lombok.Data;

import java.io.Serializable;

/**
 * 创建团队请求 DTO
 *
 * @author System
 */
@Data
@Schema(description = "创建团队请求")
public class TeamCreateDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	@NotBlank(message = "团队名称不能为空")
	@Schema(description = "团队名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "开发团队")
	private String name;
	
	@Schema(description = "团队描述", example = "负责后端开发")
	private String description;

}


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

/**
 * 更新团队请求 DTO
 *
 * @author System
 */
@Data
@Schema(description = "更新团队请求")
public class TeamUpdateDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	@Schema(description = "团队名称", example = "开发团队（已更新）")
	private String name;
	
	@Schema(description = "团队描述", example = "负责全栈开发")
	private String description;

}


/**
 * Copyright (c) 2018 COLLAB-TASK All rights reserved.
 *
 * https://www.collabtask.io
 *
 * 版权所有，侵权必究！
 */

package io.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serializable;

/**
 * 更新标签请求 DTO
 *
 * @author System
 */
@Data
@Schema(description = "更新标签请求")
public class TagUpdateDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	@Schema(description = "标签名称", example = "紧急")
	private String name;
	
	@Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "颜色格式不正确")
	@Schema(description = "标签颜色（Hex格式）", example = "#FF0000")
	private String color;

}


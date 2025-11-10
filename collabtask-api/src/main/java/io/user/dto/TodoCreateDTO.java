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
import java.util.Date;

/**
 * 创建 TODO 请求 DTO
 *
 * @author System
 */
@Data
@Schema(description = "创建 TODO 请求")
public class TodoCreateDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	@NotBlank(message = "TODO 名称不能为空")
	@Schema(description = "TODO 名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "完成项目文档")
	private String name;
	
	@Schema(description = "TODO 描述（支持 Markdown）", example = "需要完成以下内容：\n1. 系统设计\n2. API 文档")
	private String description;
	
	@Schema(description = "截止日期", example = "2025-11-15")
	private Date dueDate;
	
	@Schema(description = "优先级：LOW/MEDIUM/HIGH/URGENT", example = "HIGH")
	private String priority;
	
	@Schema(description = "所属团队ID（NULL=个人TODO）", example = "123")
	private Long teamId;

}


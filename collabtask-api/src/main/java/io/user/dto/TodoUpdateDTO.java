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
 * 更新 TODO 请求 DTO
 *
 * @author System
 */
@Data
@Schema(description = "更新 TODO 请求")
public class TodoUpdateDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	@Schema(description = "TODO 名称", example = "完成项目文档（已修改）")
	private String name;
	
	@Schema(description = "TODO 描述", example = "更新后的描述")
	private String description;
	
	@Schema(description = "截止日期", example = "2025-11-20")
	private Date dueDate;
	
	@Schema(description = "状态：NOT_STARTED/IN_PROGRESS/COMPLETED", example = "IN_PROGRESS")
	private String status;
	
	@Schema(description = "优先级：LOW/MEDIUM/HIGH/URGENT", example = "URGENT")
	private String priority;

}


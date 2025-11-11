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
 * TODO 查询请求 DTO
 *
 * @author System
 */
@Data
@Schema(description = "TODO 查询请求")
public class TodoQueryDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	@Schema(description = "关键词搜索（名称或描述）", example = "项目")
	private String keyword;
	
	@Schema(description = "状态筛选：NOT_STARTED/IN_PROGRESS/COMPLETED", example = "IN_PROGRESS")
	private String status;
	
	@Schema(description = "优先级筛选：LOW/MEDIUM/HIGH/URGENT", example = "HIGH")
	private String priority;
	
	@Schema(description = "团队ID筛选", example = "123")
	private Long teamId;
	
	@Schema(description = "标签ID筛选", example = "456")
	private Long tagId;
	
	@Schema(description = "截止日期开始", example = "2025-11-01")
	private Date dueDateStart;
	
	@Schema(description = "截止日期结束", example = "2025-11-30")
	private Date dueDateEnd;
	
	@Schema(description = "排序字段：create_date/due_date/priority/status", example = "due_date")
	private String orderBy;
	
	@Schema(description = "排序方向：asc/desc", example = "desc")
	private String orderDirection;
	
	@Schema(description = "页码（从1开始）", example = "1")
	private Integer page = 1;
	
	@Schema(description = "每页数量", example = "20")
	private Integer limit = 20;

}


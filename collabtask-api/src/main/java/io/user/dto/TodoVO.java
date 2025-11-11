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
 * TODO 响应 VO
 *
 * @author System
 */
@Data
@Schema(description = "TODO 响应")
public class TodoVO implements Serializable {
	private static final long serialVersionUID = 1L;

	@Schema(description = "TODO ID", example = "1")
	private Long id;
	
	@Schema(description = "TODO 名称", example = "完成项目文档")
	private String name;
	
	@Schema(description = "TODO 描述", example = "详细描述...")
	private String description;
	
	@Schema(description = "截止日期", example = "2025-11-15")
	private Date dueDate;
	
	@Schema(description = "状态", example = "IN_PROGRESS")
	private String status;
	
	@Schema(description = "优先级", example = "HIGH")
	private String priority;
	
	@Schema(description = "创建者ID", example = "1067246875900000001")
	private Long userId;
	
	@Schema(description = "创建者用户名", example = "john")
	private String username;
	
	@Schema(description = "所属团队ID", example = "123")
	private Long teamId;
	
	@Schema(description = "所属团队名称", example = "开发团队")
	private String teamName;
	
	@Schema(description = "完成时间", example = "2025-11-10 10:00:00")
	private Date completedAt;
	
	@Schema(description = "创建时间", example = "2025-11-10 09:00:00")
	private Date createDate;
	
	@Schema(description = "更新时间", example = "2025-11-10 10:30:00")
	private Date updateDate;

}


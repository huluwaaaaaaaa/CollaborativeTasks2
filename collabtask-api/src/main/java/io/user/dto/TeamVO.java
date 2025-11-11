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
 * 团队响应 VO
 *
 * @author System
 */
@Data
@Schema(description = "团队响应")
public class TeamVO implements Serializable {
	private static final long serialVersionUID = 1L;

	@Schema(description = "团队ID", example = "1")
	private Long id;
	
	@Schema(description = "团队名称", example = "开发团队")
	private String name;
	
	@Schema(description = "团队描述", example = "负责后端开发")
	private String description;
	
	@Schema(description = "创建者ID", example = "1067246875900000001")
	private Long ownerId;
	
	@Schema(description = "创建者用户名", example = "mark")
	private String ownerName;
	
	@Schema(description = "成员数量", example = "5")
	private Integer memberCount;
	
	@Schema(description = "创建时间", example = "2025-11-10 10:00:00")
	private Date createDate;
	
	@Schema(description = "更新时间", example = "2025-11-10 11:00:00")
	private Date updateDate;

}


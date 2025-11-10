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
 * 标签响应 VO
 *
 * @author System
 */
@Data
@Schema(description = "标签响应")
public class TagVO implements Serializable {
	private static final long serialVersionUID = 1L;

	@Schema(description = "标签ID", example = "1")
	private Long id;
	
	@Schema(description = "标签名称", example = "重要")
	private String name;
	
	@Schema(description = "标签颜色", example = "#FF5733")
	private String color;
	
	@Schema(description = "使用次数", example = "5")
	private Integer usageCount;
	
	@Schema(description = "创建时间", example = "2025-11-10 10:00:00")
	private Date createDate;

}


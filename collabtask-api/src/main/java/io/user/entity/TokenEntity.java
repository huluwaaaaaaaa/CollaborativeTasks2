/**
 * Copyright (c) 2018 COLLAB-TASK All rights reserved.
 *
 * https://www.collabtask.io
 *
 * 版权所有，侵权必究！
 */

package io.user.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户Token（v2.0 - 支持 Refresh Token、登出、多设备管理）
 * 
 * v2.0 变更：
 * 1. 表名改为 tb_tokens（复数）
 * 2. 新增 refresh_token 字段（Refresh Token，7天有效）
 * 3. 新增 device_type、device_id、ip_address（设备管理）
 * 4. 新增 is_revoked 字段（支持登出）
 * 5. 新增 refresh_expires_at（Refresh Token 过期时间）
 * 6. 新增 create_date（创建时间）
 * 7. expireDate 改名为 expiresAt（保持向后兼容）
 * 
 * @author Mark sunlightcs@gmail.com
 */
@Data
@TableName("tb_tokens")
public class TokenEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@TableId
	private Long id;
	
	/**
	 * 用户ID
	 */
	private Long userId;
	
	/**
	 * Access Token（JWT，2小时有效）
	 */
	private String token;
	
	/**
	 * Refresh Token（用于刷新 Access Token，7天有效）
	 * v2.0 新增
	 */
	private String refreshToken;
	
	/**
	 * 设备类型：WEB/IOS/ANDROID
	 * v2.0 新增
	 */
	private String deviceType;
	
	/**
	 * 设备ID（唯一标识）
	 * v2.0 新增
	 */
	private String deviceId;
	
	/**
	 * IP地址
	 * v2.0 新增
	 */
	private String ipAddress;
	
	/**
	 * Access Token 过期时间
	 * v2.0 改名（原 expireDate）
	 */
	private Date expiresAt;
	
	/**
	 * Refresh Token 过期时间
	 * v2.0 新增
	 */
	private Date refreshExpiresAt;
	
	/**
	 * 是否已撤销：0=否，1=是
	 * v2.0 新增（用于登出功能）
	 */
	private Byte isRevoked;
	
	/**
	 * 创建时间
	 * v2.0 新增
	 */
	private Date createDate;
	
	/**
	 * 更新时间
	 */
	private Date updateDate;
	
	// ==================== 向后兼容（废弃但保留） ====================
	
	/**
	 * @deprecated 使用 expiresAt 替代
	 */
	@Deprecated
	public Date getExpireDate() {
		return this.expiresAt;
	}
	
	/**
	 * @deprecated 使用 setExpiresAt 替代
	 */
	@Deprecated
	public void setExpireDate(Date expireDate) {
		this.expiresAt = expireDate;
	}

}
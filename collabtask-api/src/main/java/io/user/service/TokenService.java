/**
 * Copyright (c) 2018 COLLAB-TASK All rights reserved.
 *
 * https://www.collabtask.io
 *
 * 版权所有，侵权必究！
 */

package io.user.service;

import io.user.common.service.BaseService;
import io.user.entity.TokenEntity;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 用户Token Service
 * 
 * v2.0 变更：新增 Refresh Token、登出、撤销检查等功能
 * 
 * @author Mark sunlightcs@gmail.com
 */
public interface TokenService extends BaseService<TokenEntity> {

	/**
	 * 根据 Access Token 查询
	 * @param token Access Token
	 * @return TokenEntity
	 */
	TokenEntity getByToken(String token);

	/**
	 * 生成 token（v2.0 - 支持设备信息和 Refresh Token）
	 * @param userId  用户ID
	 * @param request HTTP 请求（用于提取设备信息和IP）
	 * @return        返回 token 信息
	 */
	TokenEntity createToken(Long userId, HttpServletRequest request);

	/**
	 * 设置 token 过期（废弃，使用 revokeToken 替代）
	 * @param userId 用户ID
	 * @deprecated 使用 revokeToken 替代
	 */
	@Deprecated
	void expireToken(Long userId);
	
	// ==================== v2.0 新增方法 ====================
	
	/**
	 * 刷新 Access Token
	 * @param refreshToken Refresh Token
	 * @return TokenEntity（新的 Access Token）
	 */
	TokenEntity refreshAccessToken(String refreshToken);
	
	/**
	 * 撤销 Token（登出）
	 * @param token Access Token
	 * @param userId 用户ID
	 * @return boolean 成功与否
	 */
	boolean revokeToken(String token, Long userId);
	
	/**
	 * 撤销所有 Token（登出所有设备）
	 * @param refreshToken Refresh Token
	 * @param userId 用户ID
	 * @return int 撤销的数量
	 */
	int revokeAllTokensByRefreshToken(String refreshToken, Long userId);
	
	/**
	 * 检查 Token 是否被撤销（带 Redis 缓存）
	 * @param token Access Token
	 * @return boolean true=已撤销
	 */
	boolean isTokenRevoked(String token);
	
	/**
	 * 清理过期 Token（定时任务调用）
	 * @return int 清理的数量
	 */
	int cleanupExpiredTokens();

}

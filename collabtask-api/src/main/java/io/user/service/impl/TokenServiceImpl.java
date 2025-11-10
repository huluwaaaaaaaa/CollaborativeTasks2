/**
 * Copyright (c) 2018 COLLAB-TASK All rights reserved.
 *
 * https://www.collabtask.io
 *
 * 版权所有，侵权必究！
 */

package io.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import io.user.common.service.impl.BaseServiceImpl;
import io.user.dao.TokenDao;
import io.user.entity.TokenEntity;
import io.user.service.TokenService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


@Service
public class TokenServiceImpl extends BaseServiceImpl<TokenDao, TokenEntity> implements TokenService {

    @Resource
	private TokenDao tokenDao;
	
	@Autowired(required = false)
	private RedisTemplate<String, Object> redisTemplate;

	/**
	 * Access Token 过期时间：2小时
	 */
	private final static int ACCESS_TOKEN_EXPIRE = 3600 * 2;
	
	/**
	 * Refresh Token 过期时间：7天
	 */
	private final static int REFRESH_TOKEN_EXPIRE = 3600 * 24 * 7;

	@Override
	public TokenEntity getByToken(String token) {
		return baseDao.getByToken(token);
	}

	@Override
	public TokenEntity createToken(Long userId, HttpServletRequest request) {
		// 当前时间
		Date now = new Date();
		
		// 生成 Access Token（2小时）
		String token = generateToken();
		
		// 生成 Refresh Token（7天）
		String refreshToken = UUID.randomUUID().toString().replace("-", "");
		
		// 提取设备信息
		String deviceType = extractDeviceType(request);
		String deviceId = extractDeviceId(request);
		String ipAddress = getClientIP(request);
		
		// 创建 Token 记录
		TokenEntity tokenEntity = new TokenEntity();
		tokenEntity.setUserId(userId);
		tokenEntity.setToken(token);
		tokenEntity.setRefreshToken(refreshToken);
		tokenEntity.setDeviceType(deviceType);
		tokenEntity.setDeviceId(deviceId);
		tokenEntity.setIpAddress(ipAddress);
		
		// 设置过期时间
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(now);
		calendar.add(Calendar.HOUR, 2);  // Access Token 2小时
		tokenEntity.setExpiresAt(calendar.getTime());
		
		calendar.setTime(now);
		calendar.add(Calendar.DAY_OF_MONTH, 7);  // Refresh Token 7天
		tokenEntity.setRefreshExpiresAt(calendar.getTime());
		
		tokenEntity.setIsRevoked((byte) 0);
		tokenEntity.setCreateDate(now);
		tokenEntity.setUpdateDate(now);
		
		// 保存到数据库
		this.insert(tokenEntity);
		
		return tokenEntity;
	}

	@Override
	@Deprecated
	public void expireToken(Long userId){
		Date now = new Date();

		LambdaUpdateWrapper<TokenEntity> updateWrapper = new LambdaUpdateWrapper<>();
		updateWrapper.eq(TokenEntity::getUserId, userId);
		updateWrapper.set(TokenEntity::getExpiresAt, now);
		updateWrapper.set(TokenEntity::getUpdateDate, now);

		tokenDao.update(updateWrapper);
	}
	
	// ==================== v2.0 新增方法 ====================
	
	@Override
	public TokenEntity refreshAccessToken(String refreshToken) {
		// 查询 Refresh Token
		TokenEntity oldToken = tokenDao.getByRefreshToken(refreshToken);
		if (oldToken == null) {
			throw new RuntimeException("Refresh Token 不存在或已撤销");
		}
		
		// 检查是否过期
		if (oldToken.getRefreshExpiresAt().before(new Date())) {
			throw new RuntimeException("Refresh Token 已过期，请重新登录");
		}
		
		// 生成新的 Access Token
		String newAccessToken = generateToken();
		Date now = new Date();
		
		// 创建新记录（复用同一个 Refresh Token）
		TokenEntity newToken = new TokenEntity();
		newToken.setUserId(oldToken.getUserId());
		newToken.setToken(newAccessToken);
		newToken.setRefreshToken(refreshToken);  // 复用
		newToken.setDeviceType(oldToken.getDeviceType());
		newToken.setDeviceId(oldToken.getDeviceId());
		newToken.setIpAddress(oldToken.getIpAddress());
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(now);
		calendar.add(Calendar.HOUR, 2);  // Access Token 2小时
		newToken.setExpiresAt(calendar.getTime());
		
		newToken.setRefreshExpiresAt(oldToken.getRefreshExpiresAt());  // 复用
		newToken.setIsRevoked((byte) 0);
		newToken.setCreateDate(now);
		newToken.setUpdateDate(now);
		
		this.insert(newToken);
		
		return newToken;
	}
	
	@Override
	public boolean revokeToken(String token, Long userId) {
		LambdaUpdateWrapper<TokenEntity> wrapper = new LambdaUpdateWrapper<>();
		wrapper.eq(TokenEntity::getToken, token)
			   .eq(TokenEntity::getUserId, userId)
			   .eq(TokenEntity::getIsRevoked, 0)
			   .set(TokenEntity::getIsRevoked, 1)
			   .set(TokenEntity::getUpdateDate, new Date());
		
		int updated = tokenDao.update(wrapper);
		
		// 写入 Redis 黑名单
		if (updated > 0 && redisTemplate != null) {
			String cacheKey = "token:revoked:" + token;
			redisTemplate.opsForValue().set(cacheKey, true, 5, TimeUnit.MINUTES);
		}
		
		return updated > 0;
	}
	
	@Override
	public int revokeAllTokensByRefreshToken(String refreshToken, Long userId) {
		// 查询所有有效 Token
		LambdaQueryWrapper<TokenEntity> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(TokenEntity::getRefreshToken, refreshToken)
					.eq(TokenEntity::getUserId, userId)
					.eq(TokenEntity::getIsRevoked, 0);
		List<TokenEntity> tokens = tokenDao.selectList(queryWrapper);
		
		// 批量撤销
		LambdaUpdateWrapper<TokenEntity> updateWrapper = new LambdaUpdateWrapper<>();
		updateWrapper.eq(TokenEntity::getRefreshToken, refreshToken)
					 .eq(TokenEntity::getUserId, userId)
					 .eq(TokenEntity::getIsRevoked, 0)
					 .set(TokenEntity::getIsRevoked, 1)
					 .set(TokenEntity::getUpdateDate, new Date());
		
		int updated = tokenDao.update(updateWrapper);
		
		// 批量写入 Redis 黑名单
		if (redisTemplate != null) {
			tokens.forEach(token -> {
				String cacheKey = "token:revoked:" + token.getToken();
				redisTemplate.opsForValue().set(cacheKey, true, 5, TimeUnit.MINUTES);
			});
		}
		
		return updated;
	}
	
	@Override
	public boolean isTokenRevoked(String token) {
		// 先查 Redis 缓存
		if (redisTemplate != null) {
			String cacheKey = "token:revoked:" + token;
			Boolean cached = (Boolean) redisTemplate.opsForValue().get(cacheKey);
			if (cached != null) {
				return cached;
			}
		}
		
		// 查数据库
		TokenEntity tokenEntity = tokenDao.getByToken(token);
		if (tokenEntity == null) {
			return false;
		}
		
		boolean isRevoked = tokenEntity.getIsRevoked() != null && tokenEntity.getIsRevoked() == 1;
		
		// 如果已撤销，写入缓存
		if (isRevoked && redisTemplate != null) {
			String cacheKey = "token:revoked:" + token;
			redisTemplate.opsForValue().set(cacheKey, true, 5, TimeUnit.MINUTES);
		}
		
		return isRevoked;
	}
	
	@Override
	public int cleanupExpiredTokens() {
		// 删除 Refresh Token 过期超过7天的记录
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_MONTH, -7);
		Date sevenDaysAgo = calendar.getTime();
		
		LambdaQueryWrapper<TokenEntity> wrapper = new LambdaQueryWrapper<>();
		wrapper.lt(TokenEntity::getRefreshExpiresAt, sevenDaysAgo);
		
		return tokenDao.delete(wrapper);
	}
	
	// ==================== 辅助方法 ====================

	private String generateToken(){
		return UUID.randomUUID().toString().replace("-", "");
	}
	
	private String extractDeviceType(HttpServletRequest request) {
		String userAgent = request.getHeader("User-Agent");
		if (StringUtils.isBlank(userAgent)) {
			return "UNKNOWN";
		}
		
		userAgent = userAgent.toLowerCase();
		if (userAgent.contains("iphone") || userAgent.contains("ipad")) {
			return "IOS";
		} else if (userAgent.contains("android")) {
			return "ANDROID";
		} else {
			return "WEB";
		}
	}
	
	private String extractDeviceId(HttpServletRequest request) {
		String deviceId = request.getHeader("X-Device-Id");
		return StringUtils.isNotBlank(deviceId) ? deviceId : UUID.randomUUID().toString();
	}
	
	private String getClientIP(HttpServletRequest request) {
		String ip = request.getHeader("X-Forwarded-For");
		if (StringUtils.isNotBlank(ip) && !"unknown".equalsIgnoreCase(ip)) {
			int index = ip.indexOf(',');
			return index != -1 ? ip.substring(0, index) : ip;
		}
		
		ip = request.getHeader("X-Real-IP");
		if (StringUtils.isNotBlank(ip) && !"unknown".equalsIgnoreCase(ip)) {
			return ip;
		}
		
		return request.getRemoteAddr();
	}
}

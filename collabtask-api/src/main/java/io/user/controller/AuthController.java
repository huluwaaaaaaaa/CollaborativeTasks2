/**
 * Copyright (c) 2018 COLLAB-TASK All rights reserved.
 * <p>
 * https://www.collabtask.io
 * <p>
 * 版权所有，侵权必究！
 */

package io.user.controller;

import io.user.annotation.Login;
import io.user.common.utils.Result;
import io.user.common.validator.ValidatorUtils;
import io.user.dto.LogoutAllDTO;
import io.user.dto.RefreshTokenDTO;
import io.user.entity.TokenEntity;
import io.user.service.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器（v2.0 新增）
 * 
 * 新增接口：
 * 1. POST /api/auth/refresh - 刷新 Token
 * 2. POST /api/auth/logout-all - 登出所有设备
 *
 * @author System
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "认证管理")
@AllArgsConstructor
public class AuthController {
	
	private final TokenService tokenService;
	
	/**
	 * 刷新 Token（v2.0 新增）⭐
	 * 
	 * 用途：Access Token 过期后，使用 Refresh Token 获取新的 Access Token
	 */
	@PostMapping("/refresh")
	@Operation(summary = "刷新 Token")
	public Result<Map<String, Object>> refresh(@RequestBody RefreshTokenDTO dto) {
		// 表单校验
		ValidatorUtils.validateEntity(dto);
		
		try {
			// 刷新 Access Token
			TokenEntity newToken = tokenService.refreshAccessToken(dto.getRefreshToken());
			
			// 返回新的 Access Token
			Map<String, Object> map = new HashMap<>(2);
			map.put("token", newToken.getToken());
			map.put("expire", newToken.getExpiresAt().getTime() - System.currentTimeMillis());
			
			return new Result<Map<String, Object>>().ok(map);
		} catch (Exception e) {
			return new Result<Map<String, Object>>().error(e.getMessage());
		}
	}
	
	/**
	 * 登出所有设备（v2.0 新增）⭐
	 * 
	 * 用途：撤销所有使用该 Refresh Token 的 Access Token
	 */
	@Login
	@PostMapping("/logout-all")
	@Operation(summary = "登出所有设备")
	public Result logoutAll(
		@Parameter(hidden = true) @RequestAttribute("userId") Long userId,
		@RequestBody LogoutAllDTO dto
	) {
		// 表单校验
		ValidatorUtils.validateEntity(dto);
		
		try {
			// 批量撤销 Token
			int count = tokenService.revokeAllTokensByRefreshToken(dto.getRefreshToken(), userId);
			
			return new Result().ok("已登出 " + count + " 个设备");
		} catch (Exception e) {
			return new Result().error(e.getMessage());
		}
	}
	
}


/**
 * Copyright (c) 2018 COLLAB-TASK All rights reserved.
 *
 * https://www.collabtask.io
 *
 * 版权所有，侵权必究！
 */

package io.user.service;

import io.user.common.service.BaseService;
import io.user.entity.UserEntity;
import io.user.dto.LoginDTO;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 用户
 * 
 * @author Mark sunlightcs@gmail.com
 */
public interface UserService extends BaseService<UserEntity> {

	UserEntity getByMobile(String mobile);

	UserEntity getUserByUserId(Long userId);

	/**
	 * 用户登录（v2.0 - 添加 HttpServletRequest 参数）
	 * @param dto    登录表单
	 * @param request HTTP 请求（用于提取设备信息和IP）
	 * @return        返回登录信息（包含 refreshToken）
	 */
	Map<String, Object> login(LoginDTO dto, HttpServletRequest request);
}

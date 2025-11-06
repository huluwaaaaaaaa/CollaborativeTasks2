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
	 * 用户登录
	 * @param dto    登录表单
	 * @return        返回登录信息
	 */
	Map<String, Object> login(LoginDTO dto);
}

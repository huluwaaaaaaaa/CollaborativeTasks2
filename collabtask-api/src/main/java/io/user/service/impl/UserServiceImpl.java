/**
 * Copyright (c) 2018 COLLAB-TASK All rights reserved.
 * <p>
 * https://www.collabtask.io
 * <p>
 * 版权所有，侵权必究！
 */

package io.user.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import io.user.common.exception.ErrorCode;
import io.user.common.exception.RenException;
import io.user.common.service.impl.BaseServiceImpl;
import io.user.common.validator.AssertUtils;
import io.user.dao.UserDao;
import io.user.dto.LoginDTO;
import io.user.entity.TokenEntity;
import io.user.entity.UserEntity;
import io.user.service.TokenService;
import io.user.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Service
@AllArgsConstructor
public class UserServiceImpl extends BaseServiceImpl<UserDao, UserEntity> implements UserService {
    private final TokenService tokenService;

    @Override
    public UserEntity getByMobile(String mobile) {
        return baseDao.getUserByMobile(mobile);
    }

    @Override
    public UserEntity getUserByUserId(Long userId) {
        return baseDao.getUserByUserId(userId);
    }

    @Override
    public Map<String, Object> login(LoginDTO dto, HttpServletRequest request) {
        UserEntity user = getByMobile(dto.getMobile());
        AssertUtils.isNull(user, ErrorCode.ACCOUNT_PASSWORD_ERROR);

        //密码错误
        if (!user.getPassword().equals(DigestUtil.sha256Hex(dto.getPassword()))) {
            throw new RenException(ErrorCode.ACCOUNT_PASSWORD_ERROR);
        }

        //获取登录 token（v2.0 - 传入 request 参数）
        TokenEntity tokenEntity = tokenService.createToken(user.getId(), request);

        // v2.0 - 返回 refreshToken
        Map<String, Object> map = new HashMap<>(4);
        map.put("token", tokenEntity.getToken());
        map.put("refreshToken", tokenEntity.getRefreshToken());  // ⭐ 新增
        map.put("expire", tokenEntity.getExpiresAt().getTime() - System.currentTimeMillis());
        map.put("userId", user.getId());  // ⭐ 新增

        return map;
    }

}
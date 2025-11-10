/**
 * Copyright (c) 2024 COLLAB-TASK All rights reserved.
 *
 * https://www.collabtask.io
 *
 * 版权所有，侵权必究！
 */

package io.user.controller;

import cn.hutool.core.util.StrUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.user.common.utils.Result;
import io.user.entity.TokenEntity;
import io.user.entity.UserEntity;
import io.user.service.TokenService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Token验证接口（供Gateway调用）
 *
 * @author Gateway Team
 */
@RestController
@RequestMapping("/token")
@Tag(name = "Token验证")
public class TokenController {

    @Resource
    private TokenService tokenService;

    @GetMapping("/validate")
    @Operation(summary = "验证Token", description = "供Gateway调用，验证token是否有效")
    public Result<TokenValidateVO> validate(HttpServletRequest request) {
        // 从header中获取token
        String token = request.getHeader("token");
        
        // 如果header中不存在token，则从参数中获取
        if (StrUtil.isBlank(token)) {
            token = request.getParameter("token");
        }

        // token为空
        if (StrUtil.isBlank(token)) {
            return new Result<TokenValidateVO>().error("token不能为空");

        }

        // 查询token信息
        TokenEntity tokenEntity = tokenService.getByToken(token);
        
        TokenValidateVO vo = new TokenValidateVO();
        vo.setToken(token);
        
        if (tokenEntity == null) {
            vo.setValid(false);
            return new Result<TokenValidateVO>().ok(vo);
        }

        // 检查token是否过期（v2.0 - 使用 expiresAt）
        if (tokenEntity.getExpiresAt().getTime() < System.currentTimeMillis()) {
            vo.setValid(false);
            return new Result<TokenValidateVO>().ok(vo);
        }
        
        // v2.0 - 检查是否被撤销 ⭐
        if (tokenEntity.getIsRevoked() != null && tokenEntity.getIsRevoked() == 1) {
            vo.setValid(false);
            return new Result<TokenValidateVO>().ok(vo);
        }

        // token有效
        vo.setValid(true);
        vo.setUserId(tokenEntity.getUserId());
        
        return new Result<TokenValidateVO>().ok(vo);

    }
    
    /**
     * 检查 Token 是否被撤销（v2.0 新增）⭐
     * 
     * 供 Gateway 调用
     */
    @GetMapping("/check-revoked")
    @Operation(summary = "检查 Token 是否被撤销", description = "供Gateway调用，检查token是否已登出")
    public boolean checkRevoked(HttpServletRequest request) {
        // 从参数中获取 token
        String token = request.getParameter("token");
        
        if (StrUtil.isBlank(token)) {
            return false;
        }
        
        // 调用 Service 检查（带 Redis 缓存）
        return tokenService.isTokenRevoked(token);
    }

    /**
     * Token验证响应VO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenValidateVO {
        /**
         * 用户ID
         */
        private Long userId;
        
        /**
         * token
         */
        private String token;
        
        /**
         * 是否有效
         */
        private Boolean valid;
    }

}


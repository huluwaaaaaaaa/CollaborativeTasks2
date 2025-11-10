/**
 * Copyright (c) 2018 COLLAB-TASK All rights reserved.
 * <p>
 * https://www.collabtask.io
 * <p>
 * 版权所有，侵权必究！
 */

package io.user.controller;


import io.user.common.annotation.Login;
import io.user.common.utils.Result;
import io.user.common.validator.ValidatorUtils;
import io.user.dto.LoginDTO;
import io.user.service.TokenService;
import io.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 登录接口
 *
 * @author Mark sunlightcs@gmail.com
 */
@RestController
@RequestMapping("/api")
@Tag(name = "登录接口")
@AllArgsConstructor
public class ApiLoginController {
    private final UserService userService;
    private final TokenService tokenService;


    @PostMapping("login")
    @Operation(summary = "登录")
    public Result<Map<String, Object>> login(@RequestBody LoginDTO dto, HttpServletRequest request) {
        //表单校验
        ValidatorUtils.validateEntity(dto);

        //用户登录（v2.0 - 传入 request 参数）
        Map<String, Object> map = userService.login(dto, request);

        return new Result().ok(map);
    }

    @Login
    @PostMapping("logout")
    @Operation(summary = "退出")
    public Result logout(@Parameter(hidden = true) @RequestAttribute("userId") Long userId, HttpServletRequest request) {
        // v2.0 - 使用 revokeToken 替代 expireToken
        String token = request.getHeader("token");
        if (token == null || token.isEmpty()) {
            return new Result().error("token不能为空");
        }
        tokenService.revokeToken(token, userId);
        return new Result().ok("登出成功");
    }

}

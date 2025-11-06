/**
 * Copyright (c) 2024 COLLAB-TASK All rights reserved.
 *
 * https://www.collabtask.io
 *
 * 版权所有，侵权必究！
 */

package io.user.gateway.filter;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import io.user.gateway.common.Result;
import io.user.gateway.config.GatewayAuthProperties;
import io.user.gateway.service.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * 认证全局过滤器
 * 用于token验证和权限控制
 * 
 * @author Gateway Team
 */
@Slf4j
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    @Autowired
    private GatewayAuthProperties authProperties;

    @Autowired
    private TokenService tokenService;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        
        log.debug("请求路径: {}, 认证开关: {}", path, authProperties.getEnabled());

        // 如果未开启认证，直接放行
        if (!authProperties.getEnabled()) {
            log.debug("认证功能未开启，直接放行");
            return chain.filter(exchange);
        }

        // 检查是否在白名单中
        if (isSkipUrl(path)) {
            log.debug("请求路径在白名单中，直接放行: {}", path);
            return chain.filter(exchange);
        }

        // 从header中获取token
        String token = request.getHeaders().getFirst("token");
        
        // 如果header中不存在token，则从参数中获取
        if (StrUtil.isBlank(token)) {
            token = request.getQueryParams().getFirst("token");
        }

        // token为空
        if (StrUtil.isBlank(token)) {
            log.warn("请求缺少token, 路径: {}", path);
            return unauthorized(exchange.getResponse(), "token不能为空");
        }

        String finalToken = token;
        // 验证token
        return tokenService.validateToken(token)
                .flatMap(userId -> {
                    if (userId != null) {
                        log.debug("Token验证通过: {}, userId: {}", finalToken, userId);
                        // Token有效，将userId添加到header中传递给下游服务
                        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                                .header("X-User-Id", String.valueOf(userId))
                                .build();
                        return chain.filter(exchange.mutate().request(mutatedRequest).build());
                    } else {
                        log.warn("Token验证失败或已过期: {}", finalToken);
                        return unauthorized(exchange.getResponse(), "token无效或已过期");
                    }
                })
                .switchIfEmpty(unauthorized(exchange.getResponse(), "token验证失败"));
    }

    /**
     * 检查URL是否在白名单中
     */
    private boolean isSkipUrl(String path) {
        if (authProperties.getSkipUrls() == null || authProperties.getSkipUrls().isEmpty()) {
            return false;
        }
        
        for (String pattern : authProperties.getSkipUrls()) {
            if (pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 返回未授权错误
     */
    private Mono<Void> unauthorized(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        
        Result<Object> result = Result.error(HttpStatus.UNAUTHORIZED.value(), message);
        String body = JSON.toJSONString(result);
        
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        // 设置较高优先级，确保在路由之前执行
        return -100;
    }

}


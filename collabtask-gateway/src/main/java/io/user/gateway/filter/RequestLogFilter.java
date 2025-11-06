/**
 * Copyright (c) 2024 COLLAB-TASK All rights reserved.
 *
 * https://www.collabtask.io
 *
 * 版权所有，侵权必究！
 */

package io.user.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 请求日志过滤器
 * 记录请求信息
 * 
 * @author Gateway Team
 */
@Slf4j
@Component
public class RequestLogFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // 记录请求信息
        log.info("请求方法: {}, 请求路径: {}, 远程地址: {}", 
                request.getMethod(), 
                request.getURI().getPath(),
                request.getRemoteAddress());
        
        long startTime = System.currentTimeMillis();
        
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            long endTime = System.currentTimeMillis();
            log.info("请求路径: {}, 耗时: {}ms", 
                    request.getURI().getPath(), 
                    (endTime - startTime));
        }));
    }

    @Override
    public int getOrder() {
        // 设置较低优先级，在认证过滤器之后执行
        return -50;
    }

}


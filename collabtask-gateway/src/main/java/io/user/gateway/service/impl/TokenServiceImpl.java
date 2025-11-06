/**
 * Copyright (c) 2024 COLLAB-TASK All rights reserved.
 *
 * https://www.collabtask.io
 *
 * 版权所有，侵权必究！
 */

package io.user.gateway.service.impl;

import io.user.gateway.config.GatewayAuthProperties;
import io.user.gateway.entity.TokenValidateResponse;
import io.user.gateway.service.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Token服务实现类
 * 通过HTTP调用API服务验证token
 * 
 * @author Gateway Team
 */
@Slf4j
@Service
public class TokenServiceImpl implements TokenService {

    @Autowired
    private WebClient webClient;

    @Autowired
    private GatewayAuthProperties authProperties;

    @Override
    public Mono<Long> validateToken(String token) {
        String url = authProperties.getApiServiceUrl() + "/collabtask-api/token/validate";
        
        log.debug("调用API服务验证token: {}, URL: {}", token, url);
        
        return webClient.get()
                .uri(url)
                .header("token", token)
                .retrieve()
                .bodyToMono(TokenValidateResponse.class)
                .map(response -> {
                    if (response.getCode() == 0 && response.getData() != null) {
                        TokenValidateResponse.TokenInfo tokenInfo = response.getData();
                        if (tokenInfo.getValid() != null && tokenInfo.getValid()) {
                            log.debug("Token验证通过, userId: {}", tokenInfo.getUserId());
                            return tokenInfo.getUserId();
                        }
                    }
                    log.warn("Token验证失败: {}", response.getMsg());
                    return null;
                })
                .onErrorResume(error -> {
                    log.error("调用API服务验证token失败: {}", error.getMessage(), error);
                    return Mono.just(null);
                })
                .defaultIfEmpty(null);
    }

}

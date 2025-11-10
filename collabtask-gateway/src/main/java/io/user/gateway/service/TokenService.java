/**
 * Copyright (c) 2024 COLLAB-TASK All rights reserved.
 *
 * https://www.collabtask.io
 *
 * 版权所有，侵权必究！
 */

package io.user.gateway.service;

import reactor.core.publisher.Mono;

/**
 * Token服务接口
 * 
 * v2.0 变更：新增 Token 撤销检查
 * 
 * @author Gateway Team
 */
public interface TokenService {

    /**
     * 验证token是否有效
     * 
     * @param token token值
     * @return 用户ID，如果token无效则返回null
     */
    Mono<Long> validateToken(String token);
    
    /**
     * 检查 Token 是否被撤销（v2.0 新增）⭐
     * 
     * @param token Access Token
     * @return true=已撤销，false=未撤销
     */
    Mono<Boolean> isTokenRevoked(String token);

}


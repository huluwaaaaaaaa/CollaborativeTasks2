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

}


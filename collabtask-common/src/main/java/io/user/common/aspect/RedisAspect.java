/**
 * Copyright (c) 2018 COLLAB-TASK All rights reserved.
 * <p>
 * https://www.collabtask.io
 * <p>
 * 版权所有，侵权必究！
 */

package io.user.common.aspect;

import io.user.common.exception.ErrorCode;
import io.user.common.exception.RenException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Redis切面处理类
 *
 * @author Mark sunlightcs@gmail.com
 */
@Aspect
@Component
public class RedisAspect {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    /**
     * 是否开启redis缓存  true开启   false关闭
     */
    @Value("${collabtask.redis.open: false}")
    private boolean open;

    @Around("execution(* io.user.common.redis.RedisUtils.*(..))")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        Object result = null;
        if (open) {
            try {
                result = point.proceed();
            } catch (Exception e) {
                logger.error("redis error", e);
                throw new RenException(ErrorCode.REDIS_ERROR);
            }
        }
        return result;
    }
}

/**
 * Copyright (c) 2018 COLLAB-TASK All rights reserved.
 *
 * https://www.collabtask.io
 *
 * 版权所有，侵权必究！
 */

package io.user.aspect;

import io.user.common.annotation.Idempotent;
import io.user.common.exception.RenException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * 幂等性切面
 * 
 * @author System
 */
@Aspect
@Component
@AllArgsConstructor
@Slf4j
public class IdempotentAspect {
	
	private final RedisTemplate<String, String> redisTemplate;
	
	@Around("@annotation(io.user.common.annotation.Idempotent)")
	public Object around(ProceedingJoinPoint point) throws Throwable {
		MethodSignature signature = (MethodSignature) point.getSignature();
		Method method = signature.getMethod();
		Idempotent annotation = method.getAnnotation(Idempotent.class);
		
		// 获取请求信息
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		if (attributes == null) {
			log.warn("无法获取请求信息，跳过幂等性检查");
			return point.proceed();
		}
		
		HttpServletRequest request = attributes.getRequest();
		String userId = (String) request.getAttribute("userId");
		String uri = request.getRequestURI();
		String methodName = method.getName();
		
		// 构建幂等key
		String idempotentKey = String.format("idempotent:%s:%s:%s", userId, uri, methodName);
		
		// 检查是否已执行
		Boolean success = redisTemplate.opsForValue().setIfAbsent(
			idempotentKey, 
			"1", 
			annotation.timeout(), 
			TimeUnit.SECONDS
		);
		
		if (Boolean.FALSE.equals(success)) {
			log.warn("重复请求：key={}", idempotentKey);
			throw new RenException("请勿重复提交");
		}
		
		try {
			log.debug("幂等性检查通过：key={}", idempotentKey);
			// 执行方法
			return point.proceed();
		} catch (Exception e) {
			// 如果执行失败，删除幂等key，允许重试
			redisTemplate.delete(idempotentKey);
			throw e;
		}
	}
}


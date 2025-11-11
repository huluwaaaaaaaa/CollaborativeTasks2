/**
 * Copyright (c) 2018 COLLAB-TASK All rights reserved.
 *
 * https://www.collabtask.io
 *
 * 版权所有，侵权必究！
 */

package io.user.aspect;

import io.user.common.annotation.DistributedLock;
import io.user.common.exception.RenException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁切面
 * 
 * @author System
 */
@Aspect
@Component
@AllArgsConstructor
@Slf4j
public class DistributedLockAspect {
	
	private final RedissonClient redissonClient;
	private final DefaultParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
	private final ExpressionParser parser = new SpelExpressionParser();
	
	@Around("@annotation(io.user.common.annotation.DistributedLock)")
	public Object around(ProceedingJoinPoint point) throws Throwable {
		MethodSignature signature = (MethodSignature) point.getSignature();
		Method method = signature.getMethod();
		DistributedLock annotation = method.getAnnotation(DistributedLock.class);
		
		// 解析锁的key
		String lockKey = parseLockKey(annotation.key(), method, point.getArgs());
		
		RLock lock = redissonClient.getLock(lockKey);
		boolean acquired = false;
		
		try {
			// 尝试获取锁
			acquired = lock.tryLock(annotation.waitTime(), annotation.leaseTime(), TimeUnit.SECONDS);
			
			if (!acquired) {
				log.warn("获取分布式锁失败：lockKey={}", lockKey);
				throw new RenException("操作过于频繁，请稍后重试");
			}
			
			log.debug("获取分布式锁成功：lockKey={}", lockKey);
			
			// 执行方法
			return point.proceed();
			
		} catch (InterruptedException e) {
			log.error("获取分布式锁被中断：lockKey={}", lockKey, e);
			Thread.currentThread().interrupt();
			throw new RenException("系统繁忙，请稍后重试");
		} finally {
			if (acquired) {
				lock.unlock();
				log.debug("释放分布式锁：lockKey={}", lockKey);
			}
		}
	}
	
	/**
	 * 解析锁的key（支持SpEL表达式）
	 */
	private String parseLockKey(String keyExpression, Method method, Object[] args) {
		// 获取参数名
		String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);
		if (parameterNames == null) {
			return keyExpression;
		}
		
		// 构建SpEL上下文
		EvaluationContext context = new StandardEvaluationContext();
		for (int i = 0; i < parameterNames.length; i++) {
			context.setVariable(parameterNames[i], args[i]);
		}
		
		// 解析表达式
		Expression expression = parser.parseExpression(keyExpression);
		Object value = expression.getValue(context);
		
		return value != null ? value.toString() : keyExpression;
	}
}


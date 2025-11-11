/**
 * Copyright (c) 2018 COLLAB-TASK All rights reserved.
 *
 * https://www.collabtask.io
 *
 * 版权所有，侵权必究！
 */

package io.user.common.annotation;

import java.lang.annotation.*;

/**
 * 分布式锁注解
 * 
 * 使用示例：
 * @DistributedLock(key = "'todo:edit:' + #id", waitTime = 3, leaseTime = 10)
 * public void updateTodo(Long id, TodoUpdateDTO dto) {
 *     // 业务逻辑
 * }
 * 
 * @author System
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributedLock {
	
	/**
	 * 锁的key（支持SpEL表达式）
	 */
	String key();
	
	/**
	 * 尝试获取锁的等待时间（秒）
	 */
	long waitTime() default 3;
	
	/**
	 * 锁的过期时间（秒）
	 */
	long leaseTime() default 10;
}


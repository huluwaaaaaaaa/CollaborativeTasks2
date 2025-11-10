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
 * 幂等性注解
 * 
 * 使用示例：
 * @Idempotent(timeout = 300)
 * public TodoVO createTodo(TodoCreateDTO dto, Long userId) {
 *     // 业务逻辑
 * }
 * 
 * @author System
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Idempotent {
	
	/**
	 * 幂等时间窗口（秒）
	 */
	long timeout() default 300;
}


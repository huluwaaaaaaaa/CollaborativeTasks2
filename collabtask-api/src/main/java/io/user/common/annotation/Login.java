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
 * 登录认证注解
 * 
 * 用于标记需要登录认证的接口
 * 
 * @author System
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Login {
}


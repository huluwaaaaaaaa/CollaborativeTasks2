/**
 * Copyright (c) 2024 COLLAB-TASK All rights reserved.
 *
 * https://www.collabtask.io
 *
 * 版权所有，侵权必究！
 */

package io.user.gateway.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一响应结果
 * 
 * @author Gateway Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    
    /**
     * 状态码
     */
    private int code;
    
    /**
     * 提示信息
     */
    private String msg;
    
    /**
     * 数据
     */
    private T data;

    public static <T> Result<T> error(int code, String msg) {
        return new Result<>(code, msg, null);
    }

    public static <T> Result<T> error(String msg) {
        return error(500, msg);
    }

    public static <T> Result<T> ok() {
        return new Result<>(0, "success", null);
    }

    public static <T> Result<T> ok(T data) {
        return new Result<>(0, "success", data);
    }

}


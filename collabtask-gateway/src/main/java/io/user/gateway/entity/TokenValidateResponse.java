/**
 * Copyright (c) 2024 COLLAB-TASK All rights reserved.
 *
 * https://www.collabtask.io
 *
 * 版权所有，侵权必究！
 */

package io.user.gateway.entity;

import lombok.Data;

/**
 * Token验证响应
 * 
 * @author Gateway Team
 */
@Data
public class TokenValidateResponse {
    
    /**
     * 状态码
     */
    private Integer code;
    
    /**
     * 提示信息
     */
    private String msg;
    
    /**
     * 数据
     */
    private TokenInfo data;

    @Data
    public static class TokenInfo {
        /**
         * 用户ID
         */
        private Long userId;
        
        /**
         * token
         */
        private String token;
        
        /**
         * 是否有效
         */
        private Boolean valid;
    }

}


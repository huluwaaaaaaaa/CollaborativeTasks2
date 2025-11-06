/**
 * Copyright (c) 2024 COLLAB-TASK All rights reserved.
 *
 * https://www.collabtask.io
 *
 * 版权所有，侵权必究！
 */

package io.user.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Gateway认证配置属性
 * 
 * @author Gateway Team
 */
@Data
@Component
@ConfigurationProperties(prefix = "gateway.auth")
public class GatewayAuthProperties {

    /**
     * 是否开启认证
     */
    private Boolean enabled = false;

    /**
     * API服务地址（用于token验证）
     */
    private String apiServiceUrl = "http://collabtask-api";

    /**
     * 跳过认证的URL列表（白名单）
     */
    private List<String> skipUrls = new ArrayList<>();

}


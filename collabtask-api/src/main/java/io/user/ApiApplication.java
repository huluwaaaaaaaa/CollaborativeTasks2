/**
 * Copyright (c) 2018 COLLAB-TASK All rights reserved.
 *
 * https://www.collabtask.io
 *
 * 版权所有，侵权必究！
 */

package io.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * collabtask-api
 *
 * @author Mark sunlightcs@gmail.com
 */
@EnableDiscoveryClient
@SpringBootApplication
public class ApiApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		// 禁用Nacos客户端的默认日志配置（避免与logback-spring.xml冲突）
		System.setProperty("nacos.logging.default.config.enabled", "false");
		
		SpringApplication.run(ApiApplication.class, args);
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(ApiApplication.class);
	}
}

/**
 * Copyright (c) 2018 COLLAB-TASK All rights reserved.
 *
 * https://www.collabtask.io
 *
 * 版权所有，侵权必究！
 */

package io.user.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson 配置
 * 
 * @author System
 */
@Configuration
public class RedissonConfig {
	
	@Value("${spring.data.redis.host:localhost}")
	private String host;
	
	@Value("${spring.data.redis.port:6379}")
	private Integer port;
	
	@Value("${spring.data.redis.password:}")
	private String password;
	
	@Value("${spring.data.redis.database:0}")
	private Integer database;
	
	@Bean
	public RedissonClient redissonClient() {
		Config config = new Config();
		
		String address = String.format("redis://%s:%d", host, port);
		config.useSingleServer()
			.setAddress(address)
			.setDatabase(database)
			.setConnectionMinimumIdleSize(5)
			.setConnectionPoolSize(10)
			.setIdleConnectionTimeout(10000)
			.setConnectTimeout(3000)
			.setTimeout(3000);
		
		// 如果配置了密码
		if (password != null && !password.isEmpty()) {
			config.useSingleServer().setPassword(password);
		}
		
		return Redisson.create(config);
	}
}


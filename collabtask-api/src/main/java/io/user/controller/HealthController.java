/**
 * Copyright (c) 2018 COLLAB-TASK All rights reserved.
 *
 * https://www.collabtask.io
 *
 * 版权所有，侵权必究！
 */

package io.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.user.common.utils.Result;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查/探活接口
 *
 * @author System
 */
@RestController
@RequestMapping("/api")
@Tag(name = "健康检查")
@AllArgsConstructor
public class HealthController {
	
	private final JdbcTemplate jdbcTemplate;
	private final RedisTemplate<String, Object> redisTemplate;
	
	/**
	 * 健康检查（简单）
	 * 用于K8s/Docker的健康探针
	 */
	@GetMapping("/health")
	@Operation(summary = "健康检查（简单）")
	public Result<String> health() {
		return new Result<String>().ok("ok");
	}
	
	/**
	 * 存活探针（Liveness Probe）
	 * 检查应用是否存活，失败则重启
	 */
	@GetMapping("/liveness")
	@Operation(summary = "存活探针")
	public Result<Map<String, Object>> liveness() {
		Map<String, Object> data = new HashMap<>();
		data.put("status", "UP");
		data.put("service", "collabtask-api");
		data.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
		return new Result<Map<String, Object>>().ok(data);
	}
	
	/**
	 * 就绪探针（Readiness Probe）
	 * 检查应用是否就绪，未就绪则不转发流量
	 */
	@GetMapping("/readiness")
	@Operation(summary = "就绪探针")
	public Result<Map<String, Object>> readiness() {
		Map<String, Object> data = new HashMap<>();
		Map<String, String> dependencies = new HashMap<>();
		
		try {
			// 检查数据库连接
			jdbcTemplate.queryForObject("SELECT 1", Integer.class);
			dependencies.put("database", "UP");
		} catch (Exception e) {
			dependencies.put("database", "DOWN");
			data.put("status", "DOWN");
			data.put("dependencies", dependencies);
			data.put("error", "数据库连接失败: " + e.getMessage());
			data.put("service", "collabtask-api");
			data.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
			Result<Map<String, Object>> result = new Result<>();
			result.error("服务未就绪");
			result.setData(data);
			return result;
		}
		
		try {
			// 检查Redis连接
			redisTemplate.opsForValue().get("health_check");
			dependencies.put("redis", "UP");
		} catch (Exception e) {
			dependencies.put("redis", "DOWN");
			// Redis失败不影响就绪状态（可选依赖）
		}
		
		data.put("status", "UP");
		data.put("service", "collabtask-api");
		data.put("dependencies", dependencies);
		data.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
		
		return new Result<Map<String, Object>>().ok(data);
	}
	
	/**
	 * 详细健康检查
	 * 包含版本信息、依赖状态等
	 */
	@GetMapping("/health/detail")
	@Operation(summary = "详细健康检查")
	public Result<Map<String, Object>> healthDetail() {
		Map<String, Object> data = new HashMap<>();
		Map<String, Object> components = new HashMap<>();
		
		// 应用信息
		data.put("service", "collabtask-api");
		data.put("version", "1.0.0");
		data.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
		data.put("uptime", getUptime());
		
		// 数据库检查
		Map<String, Object> dbHealth = new HashMap<>();
		try {
			Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
			dbHealth.put("status", "UP");
			dbHealth.put("type", "MySQL");
			
			// 获取数据库连接数
			try {
				Integer connections = jdbcTemplate.queryForObject(
					"SELECT COUNT(*) FROM information_schema.PROCESSLIST", Integer.class);
				dbHealth.put("connections", connections);
			} catch (Exception ignored) {
			}
		} catch (Exception e) {
			dbHealth.put("status", "DOWN");
			dbHealth.put("error", e.getMessage());
		}
		components.put("database", dbHealth);
		
		// Redis检查
		Map<String, Object> redisHealth = new HashMap<>();
		try {
			redisTemplate.opsForValue().set("health_check", "test", 5, java.util.concurrent.TimeUnit.SECONDS);
			redisTemplate.opsForValue().get("health_check");
			redisHealth.put("status", "UP");
			redisHealth.put("type", "Redis");
		} catch (Exception e) {
			redisHealth.put("status", "DOWN");
			redisHealth.put("error", e.getMessage());
		}
		components.put("redis", redisHealth);
		
		// 内存信息
		Runtime runtime = Runtime.getRuntime();
		Map<String, Object> memory = new HashMap<>();
		memory.put("total", runtime.totalMemory() / 1024 / 1024 + "MB");
		memory.put("free", runtime.freeMemory() / 1024 / 1024 + "MB");
		memory.put("used", (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024 + "MB");
		memory.put("max", runtime.maxMemory() / 1024 / 1024 + "MB");
		components.put("memory", memory);
		
		data.put("components", components);
		
		// 判断整体状态
		boolean allUp = components.values().stream()
			.filter(c -> c instanceof Map)
			.map(c -> (Map<?, ?>) c)
			.allMatch(c -> "UP".equals(c.get("status")));
		
		data.put("status", allUp ? "UP" : "DEGRADED");
		
		return new Result<Map<String, Object>>().ok(data);
	}
	
	/**
	 * 获取应用运行时间
	 */
	private String getUptime() {
		long uptime = java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime();
		long seconds = uptime / 1000;
		long minutes = seconds / 60;
		long hours = minutes / 60;
		long days = hours / 24;
		
		return String.format("%d天%d小时%d分钟", days, hours % 24, minutes % 60);
	}
}


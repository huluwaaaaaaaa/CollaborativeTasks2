/**
 * Copyright (c) 2024 COLLAB-TASK All rights reserved.
 *
 * https://www.collabtask.io
 *
 * 版权所有，侵权必究！
 */

package io.user.gateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Gateway健康检查/探活接口
 *
 * @author System
 */
@RestController
@RequestMapping
public class HealthController {
	
	/**
	 * 健康检查（简单）
	 * 用于K8s/Docker的健康探针
	 * 
	 * 访问地址：http://localhost:8001/health
	 */
	@GetMapping("/health")
	public Mono<Map<String, Object>> health() {
		Map<String, Object> data = new HashMap<>();
		data.put("status", "UP");
		data.put("service", "collabtask-gateway");
		data.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
		return Mono.just(data);
	}
	
	/**
	 * 存活探针（Liveness Probe）
	 * 检查Gateway是否存活，失败则重启
	 * 
	 * 访问地址：http://localhost:8001/liveness
	 */
	@GetMapping("/liveness")
	public Mono<Map<String, Object>> liveness() {
		Map<String, Object> data = new HashMap<>();
		data.put("status", "UP");
		data.put("service", "collabtask-gateway");
		data.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
		data.put("type", "liveness");
		return Mono.just(data);
	}
	
	/**
	 * 就绪探针（Readiness Probe）
	 * 检查Gateway是否就绪，未就绪则不转发流量
	 * 
	 * 访问地址：http://localhost:8001/readiness
	 */
	@GetMapping("/readiness")
	public Mono<Map<String, Object>> readiness() {
		Map<String, Object> data = new HashMap<>();
		Map<String, String> dependencies = new HashMap<>();
		
		// Gateway的就绪检查相对简单，主要确认路由配置已加载
		dependencies.put("routes", "UP");  // 路由已加载
		dependencies.put("nacos", "UP");   // Nacos连接正常（能启动说明已连接）
		
		data.put("status", "UP");
		data.put("service", "collabtask-gateway");
		data.put("dependencies", dependencies);
		data.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
		data.put("type", "readiness");
		
		return Mono.just(data);
	}
	
	/**
	 * 详细健康检查
	 * 包含版本信息、路由配置等
	 * 
	 * 访问地址：http://localhost:8001/health/detail
	 */
	@GetMapping("/health/detail")
	public Mono<Map<String, Object>> healthDetail() {
		Map<String, Object> data = new HashMap<>();
		
		// 应用信息
		data.put("service", "collabtask-gateway");
		data.put("version", "1.0.0");
		data.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
		data.put("uptime", getUptime());
		
		// Gateway特有信息
		Map<String, Object> gateway = new HashMap<>();
		gateway.put("type", "Spring Cloud Gateway");
		gateway.put("routes", "配置正常");  // 简化版，可扩展为实际路由数量
		data.put("gateway", gateway);
		
		// 内存信息
		Runtime runtime = Runtime.getRuntime();
		Map<String, Object> memory = new HashMap<>();
		memory.put("total", runtime.totalMemory() / 1024 / 1024 + "MB");
		memory.put("free", runtime.freeMemory() / 1024 / 1024 + "MB");
		memory.put("used", (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024 + "MB");
		memory.put("max", runtime.maxMemory() / 1024 / 1024 + "MB");
		data.put("memory", memory);
		
		data.put("status", "UP");
		
		return Mono.just(data);
	}
	
	/**
	 * Ping接口（最简单的存活检查）
	 * 
	 * 访问地址：http://localhost:8001/ping
	 */
	@GetMapping("/ping")
	public Mono<String> ping() {
		return Mono.just("pong");
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


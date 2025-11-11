/**
 * Copyright (c) 2018 COLLAB-TASK All rights reserved.
 *
 * https://www.collabtask.io
 *
 * 版权所有，侵权必究！
 */

package io.user.schedule;

import io.user.service.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Token 清理定时任务
 * 
 * 功能：每天凌晨2点清理过期超过7天的 Token
 * 
 * @author System
 */
@Slf4j
@Component
public class TokenCleanupTask {
	
	@Autowired
	private TokenService tokenService;
	
	/**
	 * 清理过期 Token
	 * 
	 * 执行时间：每天凌晨 2:00
	 * Cron 表达式：0 0 2 * * ?
	 */
	@Scheduled(cron = "0 0 2 * * ?")
	public void cleanupExpiredTokens() {
		log.info("开始清理过期 Token...");
		
		try {
			int count = tokenService.cleanupExpiredTokens();
			log.info("清理过期 Token 完成，删除数量：{}", count);
		} catch (Exception e) {
			log.error("清理过期 Token 失败", e);
		}
	}
}


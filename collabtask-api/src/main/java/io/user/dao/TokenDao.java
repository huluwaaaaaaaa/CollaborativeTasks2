/**
 * Copyright (c) 2018 COLLAB-TASK All rights reserved.
 *
 * https://www.collabtask.io
 *
 * 版权所有，侵权必究！
 */

package io.user.dao;

import io.user.common.dao.BaseDao;
import io.user.entity.TokenEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Token DAO
 * 
 * v2.0 变更：新增 Refresh Token 相关查询方法
 * 
 * @author Mark sunlightcs@gmail.com
 */
@Mapper
public interface TokenDao extends BaseDao<TokenEntity> {
    
    /**
     * 根据 Access Token 查询
     * @param token Access Token
     * @return TokenEntity
     */
    TokenEntity getByToken(String token);

    /**
     * 根据用户ID查询（获取最新的一条）
     * @param userId 用户ID
     * @return TokenEntity
     */
    TokenEntity getByUserId(Long userId);
    
    /**
     * 根据 Refresh Token 查询最新记录（v2.0 新增）
     * @param refreshToken Refresh Token
     * @return TokenEntity
     */
    TokenEntity getByRefreshToken(String refreshToken);
}

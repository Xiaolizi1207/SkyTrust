package com.skytrust.service;

/**
 * 令牌黑名单服务接口
 * 用于管理失效的JWT令牌（登出、令牌撤销等场景）
 *
 * @author SkyTrust Team
 */
public interface TokenBlacklistService {

    /**
     * 将令牌加入黑名单
     *
     * @param token JWT令牌
     * @param expirationTime 令牌过期时间（毫秒时间戳）
     */
    void addToBlacklist(String token, long expirationTime);

    /**
     * 检查令牌是否在黑名单中
     *
     * @param token JWT令牌
     * @return 是否在黑名单中
     */
    boolean isBlacklisted(String token);

    /**
     * 清理过期的黑名单条目（定时任务调用）
     */
    void cleanupExpiredTokens();

    /**
     * 根据用户名移除所有相关令牌（强制下线）
     *
     * @param username 用户名
     */
    void removeAllByUsername(String username);
}
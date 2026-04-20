package com.skytrust.service.impl;

import com.skytrust.service.TokenBlacklistService;
import com.skytrust.common.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 令牌黑名单服务实现（基于Redis）
 *
 * @author SkyTrust Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private static final String BLACKLIST_KEY_PREFIX = "jwt:blacklist:";
    private static final String USER_TOKENS_KEY_PREFIX = "jwt:user_tokens:";

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void addToBlacklist(String token, long expirationTime) {
        if (token == null || token.trim().isEmpty()) {
            return;
        }

        try {
            // 计算剩余过期时间（秒）
            long currentTime = System.currentTimeMillis();
            long ttlSeconds = Math.max(1, (expirationTime - currentTime) / 1000);

            // 将令牌加入黑名单，设置过期时间
            String blacklistKey = BLACKLIST_KEY_PREFIX + token;
            redisTemplate.opsForValue().set(blacklistKey, "1", ttlSeconds, TimeUnit.SECONDS);

            // 提取用户名并添加到用户令牌集合
            try {
                String username = SecurityUtil.extractUsername(token);
                if (username != null && !username.trim().isEmpty()) {
                    String userTokensKey = USER_TOKENS_KEY_PREFIX + username;
                    // 将令牌添加到用户令牌集合
                    redisTemplate.opsForSet().add(userTokensKey, token);
                    // 为用户令牌集合设置过期时间（7天，避免无限增长）
                    redisTemplate.expire(userTokensKey, 7, TimeUnit.DAYS);
                    log.debug("令牌已加入黑名单并关联用户: {}, 剩余TTL: {}秒", username, ttlSeconds);
                }
            } catch (Exception e) {
                log.debug("无法提取用户名，跳过用户令牌关联: {}", e.getMessage());
            }

            log.debug("令牌已加入黑名单，剩余TTL: {}秒", ttlSeconds);
        } catch (Exception e) {
            log.error("将令牌加入黑名单失败: {}", token, e);
        }
    }

    @Override
    public boolean isBlacklisted(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }

        try {
            String blacklistKey = BLACKLIST_KEY_PREFIX + token;
            Boolean exists = redisTemplate.hasKey(blacklistKey);
            return exists != null && exists;
        } catch (Exception e) {
            log.error("检查令牌黑名单失败: {}", token, e);
            return false; // 如果Redis故障，允许令牌通过（避免服务不可用）
        }
    }

    @Override
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
    public void cleanupExpiredTokens() {
        try {
            // Redis会自动清理过期的key，这里只需要记录日志
            log.info("令牌黑名单清理任务执行完成（Redis自动过期）");
        } catch (Exception e) {
            log.error("清理黑名单令牌失败", e);
        }
    }

    @Override
    public void removeAllByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return;
        }

        try {
            String userTokensKey = USER_TOKENS_KEY_PREFIX + username;
            // 获取该用户的所有令牌
            Set<String> tokens = redisTemplate.opsForSet().members(userTokensKey);
            if (tokens != null && !tokens.isEmpty()) {
                log.info("强制下线用户: {}, 找到 {} 个令牌", username, tokens.size());
                for (String token : tokens) {
                    try {
                        // 从令牌中提取过期时间
                        Date expiration = SecurityUtil.extractExpiration(token);
                        long expirationTime = expiration.getTime();
                        // 将令牌加入黑名单（如果尚未加入）
                        addToBlacklist(token, expirationTime);
                    } catch (Exception e) {
                        log.debug("无法处理令牌，跳过: {}", e.getMessage());
                    }
                }
                // 删除用户令牌集合
                redisTemplate.delete(userTokensKey);
                log.info("用户 {} 的所有令牌已加入黑名单，用户令牌集合已删除", username);
            } else {
                log.info("用户 {} 没有找到有效的令牌集合", username);
            }
        } catch (Exception e) {
            log.error("强制下线用户失败: {}", username, e);
        }
    }
}
package com.skytrust.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT配置属性
 *
 * @author SkyTrust Team
 */
@Data
@ConfigurationProperties(prefix = "skytrust.jwt")
public class JwtProperties {

    /**
     * JWT签名密钥
     */
    private String secret = "skytrust-secret-key-2026-blockchain-drone-rental-platform";

    /**
     * 访问令牌过期时间（秒），默认2小时
     */
    private long expiration = 7200;

    /**
     * 刷新令牌过期时间（秒），默认7天
     */
    private long refreshExpiration = 604800;
}

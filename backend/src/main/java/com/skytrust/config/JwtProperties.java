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
     * JWT签名密钥 — 必须通过环境变量 JWT_SECRET 注入
     * 默认值仅用于开发环境，生产环境启动时若为空则 SecurityUtil.init() 会抛异常拒绝启动
     */
    private String secret = "";

    /**
     * 访问令牌过期时间（秒），默认2小时
     */
    private long expiration = 7200;

    /**
     * 刷新令牌过期时间（秒），默认7天
     */
    private long refreshExpiration = 604800;
}

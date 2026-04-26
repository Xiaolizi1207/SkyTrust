package com.skytrust.config;

import com.skytrust.common.utils.SecurityUtil;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * JWT配置初始化器
 * 将application.yml中的JWT配置加载到SecurityUtil工具类中
 *
 * @author SkyTrust Team
 */
@Configuration
public class JwtConfig {

    private final JwtProperties jwtProperties;

    public JwtConfig(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @PostConstruct
    public void initSecurityUtil() {
        SecurityUtil.init(
                jwtProperties.getSecret(),
                jwtProperties.getExpiration() * 1000,
                jwtProperties.getRefreshExpiration() * 1000
        );
    }
}

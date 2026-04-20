package com.skytrust.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.util.Arrays;

/**
 * 跨域配置
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    // @Value("${skytrust.cors.allowed-origins:*}")
    private String allowedOrigins = "*";

    // @Value("${skytrust.cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS}")
    private String allowedMethods = "GET,POST,PUT,DELETE,OPTIONS";

    // @Value("${skytrust.cors.allowed-headers:*}")
    private String allowedHeaders = "*";

    // @Value("${skytrust.cors.allow-credentials:false}")
    private boolean allowCredentials = false;

    // @Value("${skytrust.cors.max-age:3600}")
    private long maxAge = 3600;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 处理允许的来源
        String[] originPatterns;
        if ("*".equals(allowedOrigins)) {
            // 通配符模式
            originPatterns = new String[]{"*"};
        } else {
            // 分割多个来源
            originPatterns = StringUtils.tokenizeToStringArray(allowedOrigins, ",");
        }

        // 处理方法
        String[] methods = StringUtils.tokenizeToStringArray(allowedMethods, ",");

        // 处理请求头
        String[] headers;
        if ("*".equals(allowedHeaders)) {
            headers = new String[]{"*"};
        } else {
            headers = StringUtils.tokenizeToStringArray(allowedHeaders, ",");
        }

        // 配置CORS
        registry.addMapping("/**") // 所有接口
                .allowedOriginPatterns(originPatterns) // 支持通配符，Spring Boot 2.4.0以上使用allowedOriginPatterns代替allowedOrigins
                .allowedMethods(methods)
                .allowedHeaders(headers)
                .allowCredentials(allowCredentials)
                .maxAge(maxAge);
    }

    /**
     * CORS配置源Bean，供Spring Security使用
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        // 处理允许的来源
        String[] originPatterns;
        if ("*".equals(allowedOrigins)) {
            originPatterns = new String[]{"*"};
        } else {
            originPatterns = StringUtils.tokenizeToStringArray(allowedOrigins, ",");
        }

        // 处理方法
        String[] methods = StringUtils.tokenizeToStringArray(allowedMethods, ",");

        // 处理请求头
        String[] headers;
        if ("*".equals(allowedHeaders)) {
            headers = new String[]{"*"};
        } else {
            headers = StringUtils.tokenizeToStringArray(allowedHeaders, ",");
        }

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList(originPatterns));
        configuration.setAllowedMethods(Arrays.asList(methods));
        configuration.setAllowedHeaders(Arrays.asList(headers));
        configuration.setAllowCredentials(allowCredentials);
        configuration.setMaxAge(maxAge);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
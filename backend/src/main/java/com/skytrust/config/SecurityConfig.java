package com.skytrust.config;

import com.skytrust.config.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security配置（Spring Boot 2.7 兼容版）
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(@Lazy JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                // 禁用CSRF（JWT无状态，不需要CSRF防护）
                .csrf().disable()
                // 启用CORS（使用CorsConfig中的配置）
                .cors().and()
                // 会话管理：无状态（不使用Session）
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                // 授权配置
                .authorizeRequests()
                // 公开访问的端点（登录、注册、刷新、文档等）
                .antMatchers(
                        "/api/auth/login",
                        "/api/auth/register",
                        "/api/auth/refresh",
                        "/api/users/register",
                        "/api/users/login",
                        "/api/users/refresh-token",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/doc.html",
                        "/favicon.ico"
                ).permitAll()
                // 管理员专属端点
                .antMatchers("/api/admin/**").hasRole("ADMIN")
                // 飞行员专属端点
                .antMatchers("/api/pilot/**").hasRole("PILOT")
                // 其他所有请求都需要认证
                .anyRequest().authenticated()
                .and()
                // 添加JWT认证过滤器（在UsernamePasswordAuthenticationFilter之前执行）
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }
}
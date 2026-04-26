package com.skytrust.config.filter;

import com.skytrust.common.utils.SecurityUtil;
import com.skytrust.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT认证过滤器（Spring Boot 2.7 兼容版）
 * 用于验证请求中的JWT令牌并设置Spring Security上下文
 *
 * @author SkyTrust Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final UserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        String username = null;
        String token = null;

        // 检查Authorization头是否存在且以Bearer开头
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7); // 去掉"Bearer "前缀
            try {
                username = SecurityUtil.extractUsername(token);
            } catch (Exception e) {
                log.debug("JWT令牌解析失败: {}", e.getMessage());
            }
        }

        // 如果用户名不为空且当前上下文中没有认证信息
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                // 验证令牌是否有效
                if (SecurityUtil.validateToken(token)) {
                    // 检查令牌类型是否为access（防止refresh token被滥用）
                    String tokenType = SecurityUtil.extractType(token);
                    if (!"access".equals(tokenType)) {
                        log.debug("JWT令牌类型不是access，拒绝认证: {}", tokenType);
                        filterChain.doFilter(request, response);
                        return;
                    }

                    // 检查令牌是否在黑名单中
                    if (tokenBlacklistService.isBlacklisted(token)) {
                        log.debug("JWT令牌已被加入黑名单: {}", token.substring(0, Math.min(token.length(), 20)) + "...");
                        filterChain.doFilter(request, response);
                        return;
                    }

                    // 加载用户详情
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    // 创建认证令牌
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 设置认证信息到Security上下文
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    log.debug("用户认证成功: {}", username);
                } else {
                    log.debug("JWT令牌无效: {}", token);
                }
            } catch (Exception e) {
                log.error("用户认证失败: {}", e.getMessage(), e);
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        // 不需要JWT认证的路径（适配SpringDoc/Knife4j）
        return path.startsWith("/api/auth/login") ||
                path.startsWith("/api/auth/register") ||
                path.startsWith("/api/auth/refresh") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/doc.html") ||
                path.startsWith("/favicon.ico");
    }
}
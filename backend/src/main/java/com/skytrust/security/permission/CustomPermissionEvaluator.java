package com.skytrust.security.permission;

import com.skytrust.service.RoleMenuService;
import com.skytrust.service.UserRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Collection;

/**
 * 自定义权限评估器
 * 支持Spring Security的hasPermission表达式
 *
 * @author SkyTrust Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomPermissionEvaluator implements PermissionEvaluator {

    private final UserRoleService userRoleService;
    private final RoleMenuService roleMenuService;

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        log.debug("检查权限: targetDomainObject={}, permission={}", targetDomainObject, permission);

        // 如果用户未认证，拒绝访问
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String username = authentication.getName();
        log.debug("用户: {}", username);

        // 支持多种权限检查方式
        if (permission instanceof String) {
            String perm = (String) permission;
            return checkPermission(authentication, perm);
        }

        return false;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        log.debug("检查权限: targetId={}, targetType={}, permission={}", targetId, targetType, permission);

        // 对于基于ID的权限检查，可以扩展实现数据级权限
        // 例如：检查用户是否有权访问特定ID的资源
        if (permission instanceof String) {
            String perm = (String) permission;
            return checkPermission(authentication, perm);
        }

        return false;
    }

    /**
     * 检查用户是否拥有指定权限
     */
    private boolean checkPermission(Authentication authentication, String permission) {
        // 权限标识格式：sys:user:query, sys:device:edit等
        // 支持通配符：sys:user:*

        // 1. 检查直接权限
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        for (GrantedAuthority authority : authorities) {
            String authorityString = authority.getAuthority();

            // 如果是权限标识（不以ROLE_开头）
            if (!authorityString.startsWith("ROLE_")) {
                // 精确匹配
                if (authorityString.equals(permission)) {
                    return true;
                }

                // 通配符匹配（如sys:user:* 匹配 sys:user:query, sys:user:edit等）
                if (authorityString.endsWith(":*")) {
                    String prefix = authorityString.substring(0, authorityString.length() - 1); // 去掉最后一个字符，变成"sys:user:"
                    if (permission.startsWith(prefix)) {
                        return true;
                    }
                }
            }
        }

        // 2. 检查角色权限（如果权限标识以ROLE_开头）
        if (permission.startsWith("ROLE_")) {
            for (GrantedAuthority authority : authorities) {
                if (authority.getAuthority().equals(permission)) {
                    return true;
                }
            }
        }

        log.debug("权限检查失败: permission={}, authorities={}", permission, authorities);
        return false;
    }

    /**
     * 检查用户是否拥有指定角色
     */
    public boolean hasRole(Authentication authentication, String role) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String roleWithPrefix = role.startsWith("ROLE_") ? role : "ROLE_" + role.toUpperCase();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        for (GrantedAuthority authority : authorities) {
            if (authority.getAuthority().equals(roleWithPrefix)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 检查用户是否拥有任意指定角色
     */
    public boolean hasAnyRole(Authentication authentication, String... roles) {
        for (String role : roles) {
            if (hasRole(authentication, role)) {
                return true;
            }
        }
        return false;
    }
}
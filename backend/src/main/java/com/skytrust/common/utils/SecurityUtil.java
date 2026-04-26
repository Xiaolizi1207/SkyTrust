package com.skytrust.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * 安全工具类
 * 包含JWT、加密、安全上下文等工具方法
 *
 * @author SkyTrust Team
 */
public class SecurityUtil {

    private static String jwtSecret = "skytrust-secret-key-2026-blockchain-drone-rental-platform";
    private static long jwtExpiration = 2 * 60 * 60 * 1000L; // 2小时
    private static long jwtRefreshExpiration = 7 * 24 * 60 * 60 * 1000L; // 7天

    private static SecretKey jwtKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

    private SecurityUtil() {
        // 工具类，防止实例化
    }

    /**
     * 初始化JWT配置（启动时由JwtConfig调用，从application.yml加载）
     */
    public static void init(String secret, long expirationMs, long refreshExpirationMs) {
        jwtSecret = secret;
        jwtExpiration = expirationMs;
        jwtRefreshExpiration = refreshExpirationMs;
        jwtKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // ==================== JWT相关方法 ====================

    /**
     * 生成JWT Token
     *
     * @param username 用户名
     * @return JWT Token
     */
    public static String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", username);
        return createToken(claims, username, jwtExpiration);
    }

    /**
     * 生成JWT Token（带额外声明）
     *
     * @param username 用户名
     * @param claims   额外声明
     * @return JWT Token
     */
    public static String generateToken(String username, Map<String, Object> claims) {
        if (claims == null) {
            claims = new HashMap<>();
        }
        claims.put("username", username);
        return createToken(claims, username, jwtExpiration);
    }

    /**
     * 生成刷新Token
     *
     * @param username 用户名
     * @return 刷新Token
     */
    public static String generateRefreshToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", username);
        claims.put("type", "refresh");
        return createToken(claims, username, jwtRefreshExpiration);
    }

    /**
     * 创建Token
     */
    private static String createToken(Map<String, Object> claims, String subject, long expiration) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(jwtKey)
                .compact();
    }

    /**
     * 从Token中提取用户名
     *
     * @param token JWT Token
     * @return 用户名
     */
    public static String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * 从Token中提取令牌类型
     *
     * @param token JWT Token
     * @return 令牌类型（access/refresh）
     */
    public static String extractType(String token) {
        return extractClaim(token, claims -> claims.get("type", String.class));
    }

    /**
     * 从Token中提取过期时间
     *
     * @param token JWT Token
     * @return 过期时间
     */
    public static Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * 从Token中提取指定声明
     *
     * @param token          JWT Token
     * @param claimsResolver 声明解析器
     * @param <T>            声明类型
     * @return 声明值
     */
    public static <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 从Token中提取所有声明
     *
     * @param token JWT Token
     * @return Claims
     */
    public static Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(jwtKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 验证Token是否过期
     *
     * @param token JWT Token
     * @return 是否过期
     */
    public static Boolean isTokenExpired(String token) {
        final Date expiration = extractExpiration(token);
        return expiration.before(new Date());
    }

    /**
     * 验证Token是否有效
     *
     * @param token    JWT Token
     * @param username 用户名
     * @return 是否有效
     */
    public static Boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    /**
     * 验证Token是否有效（不验证用户名）
     *
     * @param token JWT Token
     * @return 是否有效
     */
    public static Boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 验证刷新Token是否有效
     *
     * @param token JWT Token
     * @return 是否有效
     */
    public static Boolean validateRefreshToken(String token) {
        try {
            if (isTokenExpired(token)) {
                return false;
            }
            String type = extractType(token);
            return "refresh".equals(type);
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== 加密相关方法 ====================

    /**
     * 简单的密码加密（实际项目中应使用BCryptPasswordEncoder等）
     *
     * @param password 原始密码
     * @return 加密后的密码
     */
    public static String encryptPassword(String password) {
        if (StringUtil.isEmpty(password)) {
            return null;
        }
        // 注意：这里只是简单示例，实际项目应使用BCryptPasswordEncoder
        org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder =
            new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        return encoder.encode(password);
    }

    /**
     * 验证密码（简单示例）
     *
     * @param rawPassword     原始密码
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    public static boolean matchesPassword(String rawPassword, String encodedPassword) {
        if (StringUtil.isEmpty(rawPassword) || StringUtil.isEmpty(encodedPassword)) {
            return false;
        }
        // 注意：这里只是简单示例，实际项目应使用BCryptPasswordEncoder
        org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder =
            new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        return encoder.matches(rawPassword, encodedPassword);
    }

    /**
     * MD5加密
     *
     * @param input 输入字符串
     * @return MD5加密结果
     */
    public static String md5(String input) {
        if (StringUtil.isEmpty(input)) {
            return null;
        }
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : array) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5加密失败", e);
        }
    }

    /**
     * SHA-256加密
     *
     * @param input 输入字符串
     * @return SHA-256加密结果
     */
    public static String sha256(String input) {
        if (StringUtil.isEmpty(input)) {
            return null;
        }
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] array = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : array) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256加密失败", e);
        }
    }

    /**
     * Base64编码
     *
     * @param input 输入字符串
     * @return Base64编码结果
     */
    public static String base64Encode(String input) {
        if (StringUtil.isEmpty(input)) {
            return null;
        }
        return java.util.Base64.getEncoder().encodeToString(input.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Base64解码
     *
     * @param input Base64字符串
     * @return 解码结果
     */
    public static String base64Decode(String input) {
        if (StringUtil.isEmpty(input)) {
            return null;
        }
        return new String(java.util.Base64.getDecoder().decode(input), StandardCharsets.UTF_8);
    }

    // ==================== Spring Security上下文相关方法 ====================

    /**
     * 获取当前认证信息
     *
     * @return Authentication
     */
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * 获取当前登录用户名
     *
     * @return 用户名
     */
    public static String getCurrentUsername() {
        Authentication authentication = getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }

    /**
     * 获取当前登录用户ID
     *
     * @return 用户ID
     */
    public static Long getCurrentUserId() {
        Authentication authentication = getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof com.skytrust.security.user.CustomUserDetails) {
            return ((com.skytrust.security.user.CustomUserDetails) principal).getUserId();
        }
        // 如果不是CustomUserDetails，回退到原来的逻辑
        String username = getCurrentUsername();
        if (StringUtil.isEmpty(username)) {
            return null;
        }
        try {
            // 假设用户ID是username的一部分，如"user_123"
            if (username.startsWith("user_")) {
                return Long.parseLong(username.substring(5));
            }
            return Long.parseLong(username);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 获取当前登录用户详情
     *
     * @return UserDetails
     */
    public static UserDetails getCurrentUserDetails() {
        Authentication authentication = getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            return (UserDetails) principal;
        }
        return null;
    }

    /**
     * 检查当前用户是否有指定角色
     *
     * @param role 角色
     * @return 是否有该角色
     */
    public static boolean hasRole(String role) {
        Authentication authentication = getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
    }

    /**
     * 检查当前用户是否有管理员角色
     *
     * @return 是否是管理员
     */
    public static boolean isAdmin() {
        return hasRole("ADMIN");
    }

    /**
     * 检查当前用户是否有普通用户角色
     *
     * @return 是否是普通用户
     */
    public static boolean isUser() {
        return hasRole("USER");
    }

    /**
     * 检查当前用户是否有指定权限标识
     *
     * @param permission 权限标识，如sys:user:query
     * @return 是否有该权限
     */
    public static boolean hasPermission(String permission) {
        Authentication authentication = getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> {
                    String authorityString = authority.getAuthority();
                    // 精确匹配
                    if (authorityString.equals(permission)) {
                        return true;
                    }
                    // 通配符匹配（如sys:user:* 匹配 sys:user:query）
                    if (authorityString.endsWith(":*")) {
                        String prefix = authorityString.substring(0, authorityString.length() - 1);
                        if (permission.startsWith(prefix)) {
                            return true;
                        }
                    }
                    return false;
                });
    }

    /**
     * 检查当前用户是否有任意指定权限标识
     *
     * @param permissions 权限标识列表
     * @return 是否有任意权限
     */
    public static boolean hasAnyPermission(String... permissions) {
        for (String permission : permissions) {
            if (hasPermission(permission)) {
                return true;
            }
        }
        return false;
    }

    // ==================== 其他安全工具方法 ====================

    /**
     * 生成随机盐值
     *
     * @param length 盐值长度
     * @return 随机盐值
     */
    public static String generateSalt(int length) {
        if (length <= 0) {
            return "";
        }
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }

    /**
     * 生成API密钥
     *
     * @param length 密钥长度
     * @return API密钥
     */
    public static String generateApiKey(int length) {
        if (length <= 0) {
            return "";
        }
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(index));
        }
        return "sk_" + sb.toString();
    }

    /**
     * 生成安全的随机数
     *
     * @param min 最小值
     * @param max 最大值
     * @return 随机数
     */
    public static int secureRandom(int min, int max) {
        if (min >= max) {
            throw new IllegalArgumentException("max必须大于min");
        }
        java.security.SecureRandom random = new java.security.SecureRandom();
        return random.nextInt(max - min + 1) + min;
    }

    /**
     * 生成验证码（6位数字）
     *
     * @return 验证码
     */
    public static String generateVerificationCode() {
        java.security.SecureRandom random = new java.security.SecureRandom();
        int code = random.nextInt(900000) + 100000; // 100000-999999
        return String.valueOf(code);
    }

    /**
     * 生成UUID
     *
     * @return UUID
     */
    public static String generateUuid() {
        return java.util.UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成短UUID（8位）
     *
     * @return 短UUID
     */
    public static String generateShortUuid() {
        return generateUuid().substring(0, 8);
    }
}
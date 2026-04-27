package com.skytrust.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.skytrust.common.Result;
import com.skytrust.common.ResultCode;
import com.skytrust.common.utils.CaptchaUtil;
import com.skytrust.common.utils.SecurityUtil;
import com.skytrust.dto.LoginDTO;
import com.skytrust.entity.User;
import com.skytrust.enums.LoginLogTypeEnum;
import com.skytrust.enums.UserStatusEnum;
import com.skytrust.exception.BusinessException;
import com.skytrust.mapper.UserMapper;
import com.skytrust.service.AuthService;
import com.skytrust.service.LoginLogService;
import com.skytrust.service.RoleMenuService;
import com.skytrust.service.TokenBlacklistService;
import com.skytrust.service.UserRoleService;
import com.skytrust.service.UserService;
import com.skytrust.security.user.CustomUserDetails;
import com.skytrust.vo.CaptchaVO;
import com.skytrust.vo.LoginVO;
import com.skytrust.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现类
 *
 * @author SkyTrust Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final TokenBlacklistService tokenBlacklistService;
    private final UserRoleService userRoleService;
    private final RoleMenuService roleMenuService;
    private final StringRedisTemplate stringRedisTemplate;
    private final LoginLogService loginLogService;

    @Override
    public Result<CaptchaVO> getCaptcha() {
        // 生成验证码文本（4位字符，排除易混淆的0/O/1/I等）
        String captchaText = CaptchaUtil.generateText(4);
        // 生成唯一标识
        String captchaKey = UUID.randomUUID().toString().replace("-", "");
        // 生成Base64图片
        CaptchaUtil.CaptchaResult captchaResult = CaptchaUtil.generate(captchaText, 130, 40);
        // 存入Redis，5分钟有效
        stringRedisTemplate.opsForValue().set(
                "captcha:" + captchaKey,
                captchaText,
                5,
                TimeUnit.MINUTES
        );
        return Result.success(new CaptchaVO(captchaKey, captchaResult.getImage()));
    }

    /** 最大登录失败次数 */
    private static final int MAX_FAIL_ATTEMPTS = 5;
    /** 锁定时间（分钟） */
    private static final int LOCK_DURATION_MINUTES = 15;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<LoginVO> login(LoginDTO loginDTO, HttpServletRequest request) {
        log.info("用户登录: {}", loginDTO.getUsername());

        String ipAddress = getIpAddress(request);
        String userAgent = request.getHeader("User-Agent");

        // 1. 参数验证
        if (loginDTO.getUsername() == null || loginDTO.getPassword() == null) {
            return Result.error(ResultCode.PARAM_ERROR, "用户名或密码不能为空");
        }

        String username = loginDTO.getUsername().trim();

        // 2. 检查账户是否被锁定
        try {
            String lockKey = "login:lock:" + username;
            Boolean isLocked = stringRedisTemplate.hasKey(lockKey);
            if (Boolean.TRUE.equals(isLocked)) {
                loginLogService.recordLogin(null, username, LoginLogTypeEnum.LOGIN_LOCKED.getCode(),
                        ipAddress, userAgent, "账户被锁定，登录失败次数过多");
                log.warn("账户已被锁定: {}", username);
                return Result.error(ResultCode.USER_LOCKED, "账户已被锁定，请" + LOCK_DURATION_MINUTES + "分钟后再试");
            }
        } catch (Exception e) {
            log.warn("Redis连接异常，跳过登录锁定检查: {}", e.getMessage());
        }

        // 3. 验证图形验证码
        if (loginDTO.getCaptchaKey() != null && loginDTO.getCaptchaCode() != null) {
            try {
                String captchaKey = "captcha:" + loginDTO.getCaptchaKey();
                String storedCaptcha = stringRedisTemplate.opsForValue().get(captchaKey);
                if (storedCaptcha == null) {
                    return Result.error(ResultCode.PARAM_ERROR, "验证码已过期，请重新获取");
                }
                if (!storedCaptcha.equalsIgnoreCase(loginDTO.getCaptchaCode())) {
                    stringRedisTemplate.delete(captchaKey);
                    return Result.error(ResultCode.PARAM_ERROR, "验证码错误");
                }
                stringRedisTemplate.delete(captchaKey);
            } catch (Exception e) {
                log.warn("Redis连接异常，跳过验证码校验: {}", e.getMessage());
            }
        }

        // 4. 查询用户（支持用户名、手机号、邮箱登录）
        User user = userService.getUserByUsername(username);
        if (user == null) {
            user = userService.getUserByPhone(username);
        }
        if (user == null) {
            user = userService.getUserByEmail(username);
        }
        if (user == null) {
            loginLogService.recordLogin(null, username, LoginLogTypeEnum.LOGIN_FAIL.getCode(),
                    ipAddress, userAgent, "用户不存在");
            return Result.error(ResultCode.USER_NOT_EXIST, "用户不存在");
        }

        // 5. 检查用户状态
        if (UserStatusEnum.DISABLED.getCode().equals(user.getStatus())) {
            loginLogService.recordLogin(user.getId(), username, LoginLogTypeEnum.LOGIN_FAIL.getCode(),
                    ipAddress, userAgent, "用户已被禁用");
            return Result.error(ResultCode.USER_DISABLED, "用户已被禁用");
        }

        // 6. 验证密码
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            handleLoginFail(username, user.getId(), ipAddress, userAgent);
            return Result.error(ResultCode.PASSWORD_ERROR, "密码错误");
        }

        // 7. 登录成功，清除失败计数
        clearLoginFailCount(username);

        // 8. 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        userService.updateById(user);

        // 9. 生成令牌
        String accessToken = generateAccessToken(user.getUsername());
        String refreshToken = generateRefreshToken(user.getUsername());

        // 10. 记录登录成功日志
        loginLogService.recordLogin(user.getId(), username, LoginLogTypeEnum.LOGIN_SUCCESS.getCode(),
                ipAddress, userAgent, null);

        // 11. 构建响应
        LoginVO loginVO = new LoginVO();
        loginVO.setAccessToken(accessToken);
        loginVO.setRefreshToken(refreshToken);
        loginVO.setExpiresIn(7200L);
        loginVO.setUser(convertToVO(user));

        log.info("用户登录成功: {}", user.getUsername());
        return Result.success(loginVO, "登录成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<LoginVO> register(String username, String password, String phone, String email) {
        log.info("用户注册: {}", username);

        try {
            // 1. 委托UserService创建用户（包含校验、重名检查、默认值设置、密码加密）
            User user = new User();
            user.setUsername(username);
            user.setPassword(password);
            user.setPhone(phone);
            user.setEmail(email);

            Result<User> regResult = userService.register(user);
            if (!regResult.isSuccess()) {
                return Result.error(regResult.getCode(), regResult.getMessage());
            }
            user = regResult.getData();

            // 2. 生成令牌（自动登录）
            String accessToken = generateAccessToken(user.getUsername());
            String refreshToken = generateRefreshToken(user.getUsername());

            LoginVO loginVO = new LoginVO();
            loginVO.setAccessToken(accessToken);
            loginVO.setRefreshToken(refreshToken);
            loginVO.setExpiresIn(7200L);
            loginVO.setUser(convertToVO(user));

            log.info("用户注册成功: {}, ID: {}", user.getUsername(), user.getId());
            return Result.success(loginVO, "注册成功");
        } catch (BusinessException e) {
            log.warn("用户注册失败: {}", e.getMessage());
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @Override
    public Result<LoginVO> refreshToken(String refreshToken) {
        log.info("刷新令牌");

        try {
            // 1. 验证刷新令牌（检查过期和type=refresh）
            if (!SecurityUtil.validateRefreshToken(refreshToken)) {
                return Result.error(ResultCode.TOKEN_INVALID, "刷新令牌无效");
            }

            // 1.5 检查刷新令牌是否在黑名单中
            if (tokenBlacklistService.isBlacklisted(refreshToken)) {
                return Result.error(ResultCode.TOKEN_INVALID, "刷新令牌已被撤销");
            }

            // 2. 提取用户名
            String username = SecurityUtil.extractUsername(refreshToken);
            if (username == null) {
                return Result.error(ResultCode.TOKEN_INVALID, "刷新令牌无效");
            }

            // 3. 检查用户是否存在
            User user = userService.getUserByUsername(username);
            if (user == null) {
                return Result.error(ResultCode.USER_NOT_EXIST, "用户不存在");
            }

            // 4. 检查用户状态
            if (UserStatusEnum.DISABLED.getCode().equals(user.getStatus())) {
                return Result.error(ResultCode.USER_DISABLED, "用户已被禁用");
            }

            // 5. 生成新的令牌
            String newAccessToken = generateAccessToken(username);
            String newRefreshToken = generateRefreshToken(username);

            // 6. 构建响应
            LoginVO loginVO = new LoginVO();
            loginVO.setAccessToken(newAccessToken);
            loginVO.setRefreshToken(newRefreshToken);
            loginVO.setExpiresIn(7200L);
            loginVO.setUser(convertToVO(user));

            log.info("令牌刷新成功: {}", username);
            return Result.success(loginVO, "令牌刷新成功");
        } catch (Exception e) {
            log.error("刷新令牌失败", e);
            return Result.error(ResultCode.TOKEN_INVALID, "刷新令牌失败");
        }
    }

    @Override
    public String validateToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        String token = authHeader.substring(7); // 去掉"Bearer "前缀
        try {
            // 验证令牌有效性
            if (!SecurityUtil.validateToken(token)) {
                return null;
            }

            // 检查令牌是否在黑名单中
            if (tokenBlacklistService.isBlacklisted(token)) {
                log.debug("令牌已被加入黑名单: {}", token.substring(0, Math.min(token.length(), 20)) + "...");
                return null;
            }

            return SecurityUtil.extractUsername(token);
        } catch (Exception e) {
            log.debug("Token验证失败", e);
            return null;
        }
    }

    @Override
    public String generateAccessToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "access");
        return SecurityUtil.generateToken(username, claims);
    }

    @Override
    public String generateRefreshToken(String username) {
        return SecurityUtil.generateRefreshToken(username);
    }

    @Override
    public boolean logout(String token) {
        log.info("用户注销，令牌: {}", token.substring(0, Math.min(token.length(), 20)) + "...");

        try {
            // 提取令牌过期时间
            Date expiration = SecurityUtil.extractExpiration(token);
            long expirationTime = expiration.getTime();

            // 将令牌加入黑名单
            tokenBlacklistService.addToBlacklist(token, expirationTime);
            log.info("令牌已加入黑名单，过期时间: {}", expiration);
            return true;
        } catch (Exception e) {
            log.error("注销失败，令牌加入黑名单异常: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("加载用户详情: {}", username);

        // 查询用户（支持用户名、手机号、邮箱）
        User user = userService.getUserByUsername(username);
        if (user == null) {
            user = userService.getUserByPhone(username);
        }
        if (user == null) {
            user = userService.getUserByEmail(username);
        }
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        // 检查用户状态
        if (UserStatusEnum.DISABLED.getCode().equals(user.getStatus())) {
            throw new UsernameNotFoundException("用户已被禁用: " + username);
        }

        // 构建权限列表
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        // 1. 添加角色权限
        String role = user.getRole();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));

        // 2. 获取用户的权限标识（通过角色）
        List<String> perms = roleMenuService.getPermsByUserId(user.getId());
        for (String perm : perms) {
            if (perm != null && !perm.trim().isEmpty()) {
                authorities.add(new SimpleGrantedAuthority(perm));
            }
        }

        // 3. 获取用户的角色代码（用于hasRole检查）
        List<String> roleCodes = userRoleService.getRoleCodesByUserId(user.getId());
        for (String roleCode : roleCodes) {
            if (roleCode != null && !roleCode.trim().isEmpty()) {
                // 确保角色代码以ROLE_前缀添加（如果还没有）
                String roleAuthority = roleCode.startsWith("ROLE_") ? roleCode : "ROLE_" + roleCode.toUpperCase();
                authorities.add(new SimpleGrantedAuthority(roleAuthority));
            }
        }

        return new CustomUserDetails(user, authorities);
    }

    /**
     * 将User实体转换为UserVO
     */
    private UserVO convertToVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    /**
     * 处理登录失败：增加失败计数，超出阈值则锁定账户
     */
    private void handleLoginFail(String username, Long userId, String ipAddress, String userAgent) {
        try {
            String failCountKey = "login:fail:count:" + username;
            Long failCount = stringRedisTemplate.opsForValue().increment(failCountKey);
            if (failCount != null && failCount == 1) {
                // 首次失败，设置TTL防止计数长期累积
                stringRedisTemplate.expire(failCountKey, 30, TimeUnit.MINUTES);
            }
            if (failCount != null && failCount >= MAX_FAIL_ATTEMPTS) {
                // 锁定账户
                String lockKey = "login:lock:" + username;
                stringRedisTemplate.opsForValue().set(lockKey, "locked", LOCK_DURATION_MINUTES, TimeUnit.MINUTES);
                stringRedisTemplate.delete(failCountKey);
                loginLogService.recordLogin(userId, username, LoginLogTypeEnum.LOGIN_LOCKED.getCode(),
                        ipAddress, userAgent, "登录失败次数过多，账户已锁定");
                log.warn("账户因登录失败次数过多已被锁定: {}, 失败次数: {}", username, failCount);
            } else {
                loginLogService.recordLogin(userId, username, LoginLogTypeEnum.LOGIN_FAIL.getCode(),
                        ipAddress, userAgent, "密码错误");
            }
        } catch (Exception e) {
            log.error("处理登录失败记录时异常: {}", e.getMessage(), e);
        }
    }

    /**
     * 清除登录失败计数
     */
    private void clearLoginFailCount(String username) {
        try {
            String failCountKey = "login:fail:count:" + username;
            stringRedisTemplate.delete(failCountKey);
        } catch (Exception e) {
            log.warn("清除登录失败计数时异常: {}", e.getMessage());
        }
    }

    /**
     * 获取客户端真实IP地址
     */
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理的情况，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
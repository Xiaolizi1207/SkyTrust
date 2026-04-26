package com.skytrust.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.skytrust.common.Result;
import com.skytrust.common.ResultCode;
import com.skytrust.common.utils.SecurityUtil;
import com.skytrust.dto.LoginDTO;
import com.skytrust.entity.User;
import com.skytrust.enums.UserStatusEnum;
import com.skytrust.exception.BusinessException;
import com.skytrust.mapper.UserMapper;
import com.skytrust.service.AuthService;
import com.skytrust.service.RoleMenuService;
import com.skytrust.service.TokenBlacklistService;
import com.skytrust.service.UserRoleService;
import com.skytrust.service.UserService;
import com.skytrust.security.user.CustomUserDetails;
import com.skytrust.vo.LoginVO;
import com.skytrust.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<LoginVO> login(LoginDTO loginDTO) {
        log.info("用户登录: {}", loginDTO.getUsername());

        // 1. 参数验证
        if (loginDTO.getUsername() == null || loginDTO.getPassword() == null) {
            return Result.error(ResultCode.PARAM_ERROR, "用户名或密码不能为空");
        }

        // 2. 查询用户（支持用户名、手机号、邮箱登录）
        User user = userService.getUserByUsername(loginDTO.getUsername());
        if (user == null) {
            // 尝试手机号登录
            user = userService.getUserByPhone(loginDTO.getUsername());
        }
        if (user == null) {
            // 尝试邮箱登录
            user = userService.getUserByEmail(loginDTO.getUsername());
        }
        if (user == null) {
            return Result.error(ResultCode.USER_NOT_EXIST, "用户不存在");
        }

        // 3. 检查用户状态
        if (UserStatusEnum.DISABLED.getCode().equals(user.getStatus())) {
            return Result.error(ResultCode.USER_DISABLED, "用户已被禁用");
        }

        // 4. 验证密码
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            return Result.error(ResultCode.PASSWORD_ERROR, "密码错误");
        }

        // 5. 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        userService.updateById(user);

        // 6. 生成令牌
        String accessToken = generateAccessToken(user.getUsername());
        String refreshToken = generateRefreshToken(user.getUsername());

        // 7. 构建响应
        LoginVO loginVO = new LoginVO();
        loginVO.setAccessToken(accessToken);
        loginVO.setRefreshToken(refreshToken);
        loginVO.setExpiresIn(7200L); // 2小时
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
}
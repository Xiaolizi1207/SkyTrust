package com.skytrust.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.skytrust.common.Result;
import com.skytrust.common.ResultCode;
import com.skytrust.common.utils.CaptchaUtil;
import com.skytrust.common.utils.PasswordPolicyUtil;
import com.skytrust.common.utils.SecurityUtil;
import com.skytrust.dto.CodeLoginDTO;
import com.skytrust.dto.DecryptPhoneDTO;
import com.skytrust.dto.ForgotPasswordDTO;
import com.skytrust.dto.LoginDTO;
import com.skytrust.dto.ResetPasswordDTO;
import com.skytrust.dto.SendCodeDTO;
import com.skytrust.dto.WechatLoginDTO;
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
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Base64;

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
    private final PasswordPolicyUtil passwordPolicyUtil;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Result<CaptchaVO> getCaptcha() {
        try {
            // 生成验证码文本（4位字符，排除易混淆的0/O/1/I等）
            String captchaText = CaptchaUtil.generateText(4);
            // 生成唯一标识
            String captchaKey = UUID.randomUUID().toString().replace("-", "");
            // 生成Base64图片
            CaptchaUtil.CaptchaResult captchaResult = CaptchaUtil.generate(captchaText, 130, 40);
            // 存入Redis，3分钟有效
            stringRedisTemplate.opsForValue().set(
                    "captcha:" + captchaKey,
                    captchaText,
                    3,
                    TimeUnit.MINUTES
            );
            return Result.success(new CaptchaVO(captchaKey, captchaResult.getImage()));
        } catch (Exception e) {
            log.error("获取验证码失败: {}", e.getMessage(), e);
            return Result.error("获取验证码失败，请稍后重试");
        }
    }

    @Override
    public Result<Void> sendVerificationCode(SendCodeDTO dto) {
        String target = dto.getPhone() != null ? dto.getPhone() : dto.getEmail();
        log.info("发送验证码: target=[{}]", target);

        try {
            // 生成6位数字验证码
            String code = String.format("%06d", (int) (Math.random() * 1000000));

            // 存入Redis，3分钟有效
            String redisKey = "verify:code:" + target;
            stringRedisTemplate.opsForValue().set(redisKey, code, 3, TimeUnit.MINUTES);

            log.info("验证码已存入Redis: key=[{}], code=[{}], ttl=3min", redisKey, code);
            System.out.println("========== 验证码 [" + code + "] 已发送至: " + target + " ==========");

            return Result.success("验证码已发送");
        } catch (Exception e) {
            log.error("发送验证码失败: {}", e.getMessage(), e);
            return Result.error("验证码发送失败，请稍后重试");
        }
    }

    @Override
    public Result<LoginVO> codeLogin(CodeLoginDTO dto, HttpServletRequest request) {
        String target = dto.getPhone() != null ? dto.getPhone() : dto.getEmail();
        log.info("验证码登录: target=[{}], code=[{}]", target, dto.getCode());

        String ipAddress = getIpAddress(request);
        String userAgent = request.getHeader("User-Agent");

        // 1. 验证手机号或邮箱不能为空
        if (target == null || target.trim().isEmpty()) {
            return Result.error(ResultCode.PARAM_ERROR, "手机号或邮箱不能为空");
        }

        // 2. 验证验证码
        try {
            String redisKey = "verify:code:" + target;
            log.info("从Redis读取验证码: key=[{}]", redisKey);
            String storedCode = stringRedisTemplate.opsForValue().get(redisKey);
            log.info("Redis查询结果: storedCode=[{}]", storedCode);
            if (storedCode == null) {
                return Result.error("验证码已过期，请重新获取");
            }
            if (!storedCode.equals(dto.getCode())) {
                return Result.error("验证码错误");
            }
            // 验证成功后删除验证码
            stringRedisTemplate.delete(redisKey);
        } catch (Exception e) {
            log.warn("Redis连接异常，验证码校验失败: {}", e.getMessage());
            return Result.error("验证码校验失败，请稍后重试");
        }

        // 3. 查询用户（先按手机号查，再按邮箱查）
        User user = null;
        if (dto.getPhone() != null) {
            user = userService.getUserByPhone(dto.getPhone());
        }
        if (user == null && dto.getEmail() != null) {
            user = userService.getUserByEmail(dto.getEmail());
        }
        if (user == null) {
            loginLogService.recordLogin(null, target, LoginLogTypeEnum.LOGIN_FAIL.getCode(),
                    ipAddress, userAgent, "用户不存在");
            return Result.error(ResultCode.USER_NOT_EXIST, "该账号未注册");
        }

        // 4. 检查用户状态
        if (UserStatusEnum.DISABLED.getCode().equals(user.getStatus())) {
            loginLogService.recordLogin(user.getId(), target, LoginLogTypeEnum.LOGIN_FAIL.getCode(),
                    ipAddress, userAgent, "用户已被禁用");
            return Result.error(ResultCode.USER_DISABLED, "用户已被禁用");
        }

        // 5. 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        userService.updateById(user);

        // 6. 生成令牌
        String accessToken = generateAccessToken(user.getUsername());
        String refreshToken = generateRefreshToken(user.getUsername());

        // 7. 记录登录成功日志
        loginLogService.recordLogin(user.getId(), target, LoginLogTypeEnum.LOGIN_SUCCESS.getCode(),
                ipAddress, userAgent, "验证码登录成功");

        // 8. 构建响应
        LoginVO loginVO = new LoginVO();
        loginVO.setAccessToken(accessToken);
        loginVO.setRefreshToken(refreshToken);
        loginVO.setExpiresIn(7200L);
        loginVO.setUser(convertToVO(user));

        log.info("验证码登录成功: {}", user.getUsername());
        return Result.success(loginVO, "登录成功");
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

    /**
     * 用户注册（无邀请码，委托到5-arg版本）
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<LoginVO> register(String username, String password, String phone, String email) {
        return register(username, password, phone, email, null);
    }

    /**
     * 用户注册（带邀请码）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<LoginVO> register(String username, String password, String phone, String email, String inviteCode) {
        log.info("用户注册: {}, inviteCode={}", username, inviteCode);

        // 邀请码非空时记录日志（后续可对接邀请码校验）
        if (inviteCode != null && !inviteCode.isEmpty()) {
            log.info("使用邀请码注册: code=[{}], username=[{}]", inviteCode, username);
        }

        return doRegister(username, password, phone, email);
    }

    /**
     * 注册核心逻辑（提取为私有方法，供有/无邀请码版本复用）
     */
    private Result<LoginVO> doRegister(String username, String password, String phone, String email) {
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
        } catch (Exception e) {
            log.error("注册出现系统异常", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
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
    public Result<Void> forgotPassword(ForgotPasswordDTO dto) {
        String email = dto.getEmail();
        log.info("发送密码重置验证码: email=[{}]", email);

        // 1. 校验邮箱是否存在
        User user = userService.getUserByEmail(email);
        if (user == null) {
            return Result.error(ResultCode.USER_NOT_EXIST, "该邮箱未注册");
        }

        // 2. 检查用户状态
        if (UserStatusEnum.DISABLED.getCode().equals(user.getStatus())) {
            return Result.error(ResultCode.USER_DISABLED, "用户已被禁用");
        }

        try {
            // 3. 生成6位数字验证码
            String code = String.format("%06d", (int) (Math.random() * 1000000));

            // 4. 存入Redis，3分钟有效（使用不同key前缀）
            String redisKey = "reset:code:" + email;
            stringRedisTemplate.opsForValue().set(redisKey, code, 3, TimeUnit.MINUTES);

            log.info("密码重置验证码已存入Redis: key=[{}], code=[{}], ttl=3min", redisKey, code);
            System.out.println("========== 密码重置验证码 [" + code + "] 已发送至: " + email + " ==========");

            return Result.success("验证码已发送到邮箱");
        } catch (Exception e) {
            log.error("发送密码重置验证码失败: {}", e.getMessage(), e);
            return Result.error("验证码发送失败，请稍后重试");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> resetPassword(ResetPasswordDTO dto) {
        log.info("重置密码: email=[{}]", dto.getEmail());

        // 1. 验证两次密码一致
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            return Result.error("新密码和确认密码不一致");
        }

        // 2. 验证验证码
        try {
            String redisKey = "reset:code:" + dto.getEmail();
            String storedCode = stringRedisTemplate.opsForValue().get(redisKey);
            if (storedCode == null) {
                return Result.error("验证码已过期，请重新获取");
            }
            if (!storedCode.equals(dto.getCode())) {
                return Result.error("验证码错误");
            }
            // 验证成功后删除验证码
            stringRedisTemplate.delete(redisKey);
        } catch (Exception e) {
            log.warn("Redis连接异常，验证码校验失败: {}", e.getMessage());
            return Result.error("验证码校验失败，请稍后重试");
        }

        // 3. 查询用户
        User user = userService.getUserByEmail(dto.getEmail());
        if (user == null) {
            return Result.error(ResultCode.USER_NOT_EXIST, "用户不存在");
        }

        // 4. 检查用户状态
        if (UserStatusEnum.DISABLED.getCode().equals(user.getStatus())) {
            return Result.error(ResultCode.USER_DISABLED, "用户已被禁用");
        }

        // 5. 密码强度校验
        String passwordMsg = passwordPolicyUtil.validateAndGetMessage(dto.getNewPassword(), user.getUsername());
        if (passwordMsg != null) {
            return Result.error(ResultCode.PARAM_ERROR, passwordMsg);
        }

        // 6. 用BCrypt加密新密码
        String encodedPassword = passwordEncoder.encode(dto.getNewPassword());
        user.setPassword(encodedPassword);
        userService.updateById(user);

        // 7. 记录密码重置日志
        loginLogService.recordLogin(user.getId(), user.getUsername(),
                LoginLogTypeEnum.PASSWORD_RESET.getCode(),
                null, null, "密码重置成功");

        log.info("密码重置成功: {}", user.getUsername());
        return Result.success("密码重置成功");
    }

    // ==================== 微信小程序登录 ====================

    /** 微信AppID（从配置文件读取，此处提供默认值） */
    private String getWxAppId() {
        return "wx_your_appid_here";
    }

    /** 微信AppSecret */
    private String getWxAppSecret() {
        return "your_appsecret_here";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<LoginVO> wechatLogin(WechatLoginDTO dto, HttpServletRequest request) {
        String code = dto.getCode();
        String ipAddress = getIpAddress(request);
        String userAgent = request.getHeader("User-Agent");

        log.info("微信一键登录: code=[{}]", code);

        try {
            // 1. 调用微信接口 code2Session 换取 openid 和 session_key
            String wxUrl = String.format(
                    "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                    getWxAppId(), getWxAppSecret(), code);

            String wxResponse = restTemplate.getForObject(wxUrl, String.class);
            log.debug("微信 code2Session 响应: {}", wxResponse);

            JsonNode jsonNode = objectMapper.readTree(wxResponse);
            if (jsonNode.has("errcode") && jsonNode.get("errcode").asInt() != 0) {
                String errMsg = jsonNode.has("errmsg") ? jsonNode.get("errmsg").asText() : "未知错误";
                log.error("微信 code2Session 失败: {}", errMsg);
                loginLogService.recordLogin(null, null, LoginLogTypeEnum.LOGIN_FAIL.getCode(),
                        ipAddress, userAgent, "微信登录失败: " + errMsg);
                return Result.error("微信授权失败: " + errMsg);
            }

            String openid = jsonNode.get("openid").asText();
            String sessionKey = jsonNode.has("session_key") ? jsonNode.get("session_key").asText() : null;
            String unionid = jsonNode.has("unionid") ? jsonNode.get("unionid").asText() : null;

            log.info("微信 openid: {}, unionid: {}", openid, unionid);

            // 2. 缓存 session_key 到 Redis（5分钟有效），供后续手机号解密使用
            if (sessionKey != null) {
                stringRedisTemplate.opsForValue().set("wx:session:" + openid, sessionKey, 5, TimeUnit.MINUTES);
            }

            // 3. 通过 openid 查找已绑定用户（openid 存储在 remark 或 wallet_address 字段中）
            //    实际项目建议在 User 表增加 wechat_openid 字段，此处使用 remark 字段做模拟映射
            User user = userMapper.selectByOpenid(openid);
            if (user == null) {
                // 尝试通过 unionid 查找
                if (unionid != null) {
                    user = userMapper.selectByOpenid(unionid);
                }
            }

            // 4. 新用户自动注册
            if (user == null) {
                user = new User();
                String autoUsername = "wx_" + openid.substring(0, 12);
                user.setUsername(autoUsername);
                user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString())); // 随机密码
                user.setStatus(UserStatusEnum.ENABLED.getCode());
                user.setRole("user");
                user.setCreditScore(80); // 新用户默认信用分

                // 将 openid 存入 remark 字段（临时方案，建议后续增加 wechat_openid 字段）
                if (unionid != null) {
                    user.setRemark("openid:" + openid + ",unionid:" + unionid);
                } else {
                    user.setRemark("openid:" + openid);
                }

                Result<User> regResult = userService.register(user);
                if (!regResult.isSuccess()) {
                    return Result.error(regResult.getCode(), regResult.getMessage());
                }
                user = regResult.getData();
                log.info("微信新用户自动注册: {}, openid: {}", autoUsername, openid);
            }

            // 5. 如果前端传了 encryptedData + iv，解密用户信息（昵称、头像等）
            if (dto.getEncryptedData() != null && dto.getIv() != null && sessionKey != null) {
                try {
                    String decryptedData = decryptWxData(dto.getEncryptedData(), sessionKey, dto.getIv());
                    log.info("解密微信用户信息: {}", decryptedData);
                    JsonNode userInfo = objectMapper.readTree(decryptedData);
                    if (userInfo.has("nickName") && user.getRealName() == null) {
                        user.setRealName(userInfo.get("nickName").asText());
                    }
                    if (userInfo.has("avatarUrl") && user.getAvatar() == null) {
                        user.setAvatar(userInfo.get("avatarUrl").asText());
                    }
                    userService.updateById(user);
                } catch (Exception e) {
                    log.warn("解密微信用户信息失败（不影响登录）: {}", e.getMessage());
                }
            }

            // 6. 检查用户状态
            if (UserStatusEnum.DISABLED.getCode().equals(user.getStatus())) {
                loginLogService.recordLogin(user.getId(), user.getUsername(),
                        LoginLogTypeEnum.LOGIN_FAIL.getCode(),
                        ipAddress, userAgent, "用户已被禁用");
                return Result.error(ResultCode.USER_DISABLED, "用户已被禁用");
            }

            // 7. 更新最后登录时间
            user.setLastLoginTime(LocalDateTime.now());
            userService.updateById(user);

            // 8. 生成 JWT 令牌
            String accessToken = generateAccessToken(user.getUsername());
            String refreshToken = generateRefreshToken(user.getUsername());

            // 9. 记录登录日志
            loginLogService.recordLogin(user.getId(), user.getUsername(),
                    LoginLogTypeEnum.LOGIN_SUCCESS.getCode(),
                    ipAddress, userAgent, "微信登录成功");

            // 10. 构建响应
            LoginVO loginVO = new LoginVO();
            loginVO.setAccessToken(accessToken);
            loginVO.setRefreshToken(refreshToken);
            loginVO.setExpiresIn(7200L);
            loginVO.setUser(convertToVO(user));

            log.info("微信登录成功: {}", user.getUsername());
            return Result.success(loginVO, "登录成功");
        } catch (Exception e) {
            log.error("微信登录异常: {}", e.getMessage(), e);
            loginLogService.recordLogin(null, null, LoginLogTypeEnum.LOGIN_FAIL.getCode(),
                    ipAddress, userAgent, "微信登录系统异常");
            return Result.error("微信登录失败，请稍后重试");
        }
    }

    @Override
    public Result<String> decryptPhone(DecryptPhoneDTO dto) {
        log.info("解密微信手机号: encryptedData length={}", dto.getEncryptedData().length());

        try {
            // 1. 获取 session_key：优先使用请求中的，否则尝试从 Redis 根据 openid 获取
            String sessionKey = dto.getSessionKey();

            if (sessionKey == null || sessionKey.trim().isEmpty()) {
                // 尝试从 Redis 读取（需要 openid 作为 key）
                // 实际场景中 session_key 由前端 localStorage 保存，此处保留 Redis 兜底逻辑
                return Result.error("缺少 session_key，请先完成微信登录获取");
            }

            // 2. AES-128-CBC 解密
            String decryptedData = decryptWxData(dto.getEncryptedData(), sessionKey, dto.getIv());
            log.debug("解密手机号原始数据: {}", decryptedData);

            // 3. 解析 JSON 获取 purePhoneNumber
            JsonNode jsonNode = objectMapper.readTree(decryptedData);
            if (!jsonNode.has("purePhoneNumber")) {
                return Result.error("解密失败：未获取到手机号");
            }

            String phone = jsonNode.get("purePhoneNumber").asText();
            log.info("微信手机号解密成功: {}", phone);
            return Result.success(phone, "解密成功");
        } catch (Exception e) {
            log.error("微信手机号解密失败: {}", e.getMessage(), e);
            return Result.error("手机号解密失败: " + e.getMessage());
        }
    }

    /**
     * 微信加密数据解密（AES-128-CBC）
     *
     * @param encryptedData Base64编码的加密数据
     * @param sessionKey    会话密钥
     * @param iv            加密初始向量
     * @return 解密后的 JSON 字符串
     */
    private String decryptWxData(String encryptedData, String sessionKey, String iv) throws Exception {
        byte[] sessionKeyBytes = Base64.getDecoder().decode(sessionKey);
        byte[] ivBytes = Base64.getDecoder().decode(iv);
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);

        AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(sessionKeyBytes, "AES");
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        byte[] decrypted = cipher.doFinal(encryptedBytes);

        return new String(decrypted, StandardCharsets.UTF_8);
    }

    // ==================== 密码登录 ====================
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
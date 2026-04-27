package com.skytrust.controller;

import com.skytrust.common.Result;
import com.skytrust.common.utils.CaptchaUtil;
import com.skytrust.dto.LoginDTO;
import com.skytrust.dto.RefreshTokenDTO;
import com.skytrust.dto.RegisterDTO;
import com.skytrust.service.AuthService;
import com.skytrust.vo.CaptchaVO;
import com.skytrust.vo.LoginVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 认证控制器
 * 处理用户登录、注册、令牌刷新等认证相关接口
 *
 * @author SkyTrust Team
 */
@Slf4j
@Tag(name = "认证管理", description = "用户登录、注册、令牌刷新等接口")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 获取图形验证码
     */
    @Operation(summary = "获取图形验证码")
    @GetMapping("/captcha")
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

    /**
     * 用户登录
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        return authService.login(loginDTO);
    }

    /**
     * 用户注册
     */
    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<LoginVO> register(@Valid @RequestBody RegisterDTO registerDTO) {
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            return Result.error("密码和确认密码不一致");
        }

        return authService.register(
                registerDTO.getUsername(),
                registerDTO.getPassword(),
                registerDTO.getPhone(),
                registerDTO.getEmail()
        );
    }

    /**
     * 刷新访问令牌
     */
    @Operation(summary = "刷新访问令牌")
    @PostMapping("/refresh")
    public Result<LoginVO> refreshToken(@Valid @RequestBody RefreshTokenDTO refreshTokenDTO) {
        return authService.refreshToken(refreshTokenDTO.getRefreshToken());
    }

    /**
     * 用户注销
     */
    @Operation(summary = "用户注销")
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Result.error("未提供有效的令牌");
        }

        String token = authHeader.substring(7);
        boolean success = authService.logout(token);
        if (success) {
            return Result.success("注销成功");
        } else {
            return Result.error("注销失败");
        }
    }

    /**
     * 获取当前用户信息
     */
    @Operation(summary = "获取当前用户信息")
    @GetMapping("/me")
    public Result<String> getCurrentUser(HttpServletRequest request) {
        String username = authService.validateToken(request);
        if (username == null) {
            return Result.error("用户未登录");
        }
        return Result.success("当前用户: " + username);
    }
}
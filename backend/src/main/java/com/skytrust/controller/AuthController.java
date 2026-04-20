package com.skytrust.controller;

import com.skytrust.common.Result;
import com.skytrust.dto.LoginDTO;
import com.skytrust.dto.RegisterDTO;
import com.skytrust.service.AuthService;
import com.skytrust.vo.LoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 认证控制器
 * 处理用户登录、注册、令牌刷新等认证相关接口
 *
 * @author SkyTrust Team
 */
@Slf4j
@Api(tags = "认证管理", description = "用户登录、注册、令牌刷新等接口")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     */
    @ApiOperation("用户登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        return authService.login(loginDTO);
    }

    /**
     * 用户注册
     */
    @ApiOperation("用户注册")
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
    @ApiOperation("刷新访问令牌")
    @PostMapping("/refresh")
    public Result<LoginVO> refreshToken(@RequestHeader("Authorization") String refreshToken) {
        if (refreshToken != null && refreshToken.startsWith("Bearer ")) {
            refreshToken = refreshToken.substring(7);
        }
        return authService.refreshToken(refreshToken);
    }

    /**
     * 用户注销
     */
    @ApiOperation("用户注销")
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
    @ApiOperation("获取当前用户信息")
    @GetMapping("/me")
    public Result<String> getCurrentUser(HttpServletRequest request) {
        String username = authService.validateToken(request);
        if (username == null) {
            return Result.error("用户未登录");
        }
        return Result.success("当前用户: " + username);
    }
}
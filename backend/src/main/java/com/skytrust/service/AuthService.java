package com.skytrust.service;

import com.skytrust.common.Result;
import com.skytrust.dto.LoginDTO;
import com.skytrust.vo.CaptchaVO;
import com.skytrust.vo.LoginVO;
import org.springframework.security.core.userdetails.UserDetailsService;
import javax.servlet.http.HttpServletRequest;

/**
 * 认证服务接口
 * 负责用户登录、注册、令牌刷新等认证相关操作
 *
 * @author SkyTrust Team
 */
public interface AuthService extends UserDetailsService {

    /**
     * 获取图形验证码
     *
     * @return 验证码信息（包含captchaKey和Base64图片数据）
     */
    Result<CaptchaVO> getCaptcha();

    /**
     * 用户登录认证
     *
     * @param loginDTO 登录参数
     * @param request  HTTP请求（用于获取IP和User-Agent）
     * @return 登录结果（包含Token和用户信息）
     */
    Result<LoginVO> login(LoginDTO loginDTO, HttpServletRequest request);

    /**
     * 用户注册
     *
     * @param username 用户名
     * @param password 密码
     * @param phone    手机号
     * @param email    邮箱（可选）
     * @return 注册结果
     */
    Result<LoginVO> register(String username, String password, String phone, String email);

    /**
     * 刷新访问令牌
     *
     * @param refreshToken 刷新令牌
     * @return 新的令牌信息
     */
    Result<LoginVO> refreshToken(String refreshToken);

    /**
     * 从请求中提取并验证Token，返回用户名
     *
     * @param request HTTP请求
     * @return 用户名（如果Token有效）
     */
    String validateToken(HttpServletRequest request);

    /**
     * 生成访问令牌
     *
     * @param username 用户名
     * @return 访问令牌
     */
    String generateAccessToken(String username);

    /**
     * 生成刷新令牌
     *
     * @param username 用户名
     * @return 刷新令牌
     */
    String generateRefreshToken(String username);

    /**
     * 注销用户（使令牌失效）
     *
     * @param token 访问令牌
     * @return 是否成功
     */
    boolean logout(String token);
}
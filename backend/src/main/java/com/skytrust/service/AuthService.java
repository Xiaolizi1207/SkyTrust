package com.skytrust.service;

import com.skytrust.common.Result;
import com.skytrust.dto.CodeLoginDTO;
import com.skytrust.dto.DecryptPhoneDTO;
import com.skytrust.dto.ForgotPasswordDTO;
import com.skytrust.dto.LoginDTO;
import com.skytrust.dto.ResetPasswordDTO;
import com.skytrust.dto.SendCodeDTO;
import com.skytrust.dto.WechatLoginDTO;
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
     * 用户登录认证（密码登录）
     *
     * @param loginDTO 登录参数
     * @param request  HTTP请求（用于获取IP和User-Agent）
     * @return 登录结果（包含Token和用户信息）
     */
    Result<LoginVO> login(LoginDTO loginDTO, HttpServletRequest request);

    /**
     * 发送验证码（到手机或邮箱）
     *
     * @param dto 手机号或邮箱
     * @return 发送结果
     */
    Result<Void> sendVerificationCode(SendCodeDTO dto);

    /**
     * 验证码登录
     *
     * @param dto     手机号/邮箱 + 验证码
     * @param request HTTP请求
     * @return 登录结果
     */
    Result<LoginVO> codeLogin(CodeLoginDTO dto, HttpServletRequest request);

    /**
     * 用户注册
     *
     * @param username 用户名
     * @param password 密码
     * @param phone    手机号
     * @param email    邮箱（可选）
     * @param inviteCode 邀请码（可选）
     * @return 注册结果
     */
    Result<LoginVO> register(String username, String password, String phone, String email, String inviteCode);

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

    /**
     * 发送密码重置验证码到邮箱
     *
     * @param dto 邮箱信息
     * @return 发送结果
     */
    Result<Void> forgotPassword(ForgotPasswordDTO dto);

    /**
     * 验证验证码并重置密码
     *
     * @param dto 重置密码信息
     * @return 重置结果
     */
    Result<Void> resetPassword(ResetPasswordDTO dto);

    /**
     * 微信一键登录
     * 通过微信临时code换取openid，查找或创建用户，返回JWT
     *
     * @param dto     微信登录参数（code + 可选encryptedData/iv）
     * @param request HTTP请求（用于记录登录日志）
     * @return 登录结果（包含Token和用户信息）
     */
    Result<LoginVO> wechatLogin(WechatLoginDTO dto, HttpServletRequest request);

    /**
     * 解密微信手机号
     * 使用session_key解密微信返回的加密手机号数据
     *
     * @param dto 解密参数（encryptedData + iv + sessionKey）
     * @return 解密后的手机号
     */
    Result<String> decryptPhone(DecryptPhoneDTO dto);
}
package com.skytrust.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

/**
 * 登录返回视图对象
 *
 * @author SkyTrust Team
 */
@Data
@Schema(description = "登录返回视图对象")
public class LoginVO {

    @Schema(description = "访问令牌", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    @Schema(description = "令牌类型", example = "Bearer")
    private String tokenType = "Bearer";

    @Schema(description = "过期时间（秒）", example = "7200")
    private Long expiresIn;

    @Schema(description = "刷新令牌", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String refreshToken;

    @Schema(description = "用户信息")
    private UserVO user;
}
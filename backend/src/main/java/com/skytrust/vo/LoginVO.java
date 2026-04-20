package com.skytrust.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 登录返回视图对象
 *
 * @author SkyTrust Team
 */
@Data
@ApiModel(description = "登录返回视图对象")
public class LoginVO {

    @ApiModelProperty(value = "访问令牌", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    @ApiModelProperty(value = "令牌类型", example = "Bearer")
    private String tokenType = "Bearer";

    @ApiModelProperty(value = "过期时间（秒）", example = "7200")
    private Long expiresIn;

    @ApiModelProperty(value = "刷新令牌", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String refreshToken;

    @ApiModelProperty(value = "用户信息")
    private UserVO user;
}
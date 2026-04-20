package com.skytrust.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录数据传输对象
 *
 * @author SkyTrust Team
 */
@Data
@ApiModel(description = "登录数据传输对象")
public class LoginDTO {

    @ApiModelProperty(value = "用户名或手机号", example = "john_doe", required = true)
    @NotBlank(message = "用户名或手机号不能为空")
    private String username;

    @ApiModelProperty(value = "密码", example = "Password123!", required = true)
    @NotBlank(message = "密码不能为空")
    private String password;

    @ApiModelProperty(value = "验证码（可选）", example = "123456")
    private String captcha;

    @ApiModelProperty(value = "记住我", example = "false")
    private Boolean rememberMe = false;
}
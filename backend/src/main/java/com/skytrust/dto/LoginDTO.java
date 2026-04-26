package com.skytrust.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录数据传输对象
 *
 * @author SkyTrust Team
 */
@Data
@Schema(description = "登录数据传输对象")
public class LoginDTO {

    @Schema(description = "用户名或手机号", example = "john_doe", required = true)
    @NotBlank(message = "用户名或手机号不能为空")
    private String username;

    @Schema(description = "密码", example = "Password123!", required = true)
    @NotBlank(message = "密码不能为空")
    private String password;
}
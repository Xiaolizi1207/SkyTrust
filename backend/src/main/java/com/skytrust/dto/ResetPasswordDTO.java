package com.skytrust.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 重置密码数据传输对象
 *
 * @author SkyTrust Team
 */
@Data
@Schema(description = "重置密码DTO")
public class ResetPasswordDTO {

    @Schema(description = "邮箱", example = "user@example.com", required = true)
    @NotBlank(message = "邮箱不能为空")
    private String email;

    @Schema(description = "验证码", example = "827364", required = true)
    @NotBlank(message = "验证码不能为空")
    private String code;

    @Schema(description = "新密码", example = "NewPass123!", required = true)
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 50, message = "密码长度需在6-50个字符之间")
    private String newPassword;

    @Schema(description = "确认新密码", example = "NewPass123!", required = true)
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;
}

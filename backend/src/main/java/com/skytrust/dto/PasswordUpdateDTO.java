package com.skytrust.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Data;

/**
 * 密码更新数据传输对象
 *
 * @author SkyTrust Team
 */
@Data
@Schema(description = "密码更新数据传输对象")
public class PasswordUpdateDTO {

    @Schema(description = "旧密码", example = "OldPassword123!", required = true)
    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;

    @Schema(description = "新密码", example = "NewPassword123!", required = true)
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 100, message = "新密码长度必须在6-100个字符之间")
    private String newPassword;
}
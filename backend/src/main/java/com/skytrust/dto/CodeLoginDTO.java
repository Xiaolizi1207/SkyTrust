package com.skytrust.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 验证码登录请求DTO
 */
@Data
@Schema(description = "验证码登录请求")
public class CodeLoginDTO {

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "邮箱", example = "user@example.com")
    private String email;

    @Schema(description = "验证码", example = "827364", required = true)
    @NotBlank(message = "验证码不能为空")
    private String code;
}

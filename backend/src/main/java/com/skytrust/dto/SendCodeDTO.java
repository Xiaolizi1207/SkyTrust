package com.skytrust.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.AssertTrue;

/**
 * 发送验证码请求DTO
 */
@Data
@Schema(description = "发送验证码请求")
public class SendCodeDTO {

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "邮箱", example = "user@example.com")
    private String email;

    @AssertTrue(message = "手机号或邮箱至少填一个")
    public boolean isValid() {
        return (phone != null && !phone.trim().isEmpty())
                || (email != null && !email.trim().isEmpty());
    }
}

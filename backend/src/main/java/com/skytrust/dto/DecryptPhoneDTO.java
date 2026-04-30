package com.skytrust.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 微信手机号解密请求DTO
 *
 * @author SkyTrust Team
 */
@Data
@Schema(description = "微信手机号解密请求")
public class DecryptPhoneDTO {

    @Schema(description = "手机号加密数据", required = true)
    @NotBlank(message = "加密数据不能为空")
    private String encryptedData;

    @Schema(description = "加密初始向量", required = true)
    @NotBlank(message = "加密向量不能为空")
    private String iv;

    @Schema(description = "微信会话密钥（session_key）")
    private String sessionKey;
}

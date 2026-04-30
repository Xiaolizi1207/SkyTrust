package com.skytrust.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 微信一键登录请求DTO
 *
 * @author SkyTrust Team
 */
@Data
@Schema(description = "微信一键登录请求")
public class WechatLoginDTO {

    @Schema(description = "微信登录临时code（wx.login返回）", required = true)
    @NotBlank(message = "微信code不能为空")
    private String code;

    @Schema(description = "用户敏感数据密文（getUserInfo返回的encryptedData）")
    private String encryptedData;

    @Schema(description = "加密算法的初始向量（getUserInfo返回的iv）")
    private String iv;
}

package com.skytrust.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 刷新令牌请求
 *
 * @author SkyTrust Team
 */
@Data
@Schema(description = "刷新令牌请求")
public class RefreshTokenDTO {

    @NotBlank(message = "刷新令牌不能为空")
    @Schema(description = "刷新令牌", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...", required = true)
    private String refreshToken;
}

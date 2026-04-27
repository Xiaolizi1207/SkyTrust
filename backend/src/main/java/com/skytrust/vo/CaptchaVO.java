package com.skytrust.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 图形验证码视图对象
 *
 * @author SkyTrust Team
 */
@Data
@AllArgsConstructor
@Schema(description = "图形验证码视图对象")
public class CaptchaVO {

    @Schema(description = "验证码唯一标识", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    private String captchaKey;

    @Schema(description = "验证码图片Base64数据", example = "data:image/png;base64,iVBORw0KGgo...")
    private String captchaImage;
}

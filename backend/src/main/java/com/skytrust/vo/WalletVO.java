package com.skytrust.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "钱包余额视图")
public class WalletVO {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "钱包余额（元）")
    private BigDecimal balance;
}

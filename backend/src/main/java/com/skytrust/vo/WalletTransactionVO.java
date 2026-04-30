package com.skytrust.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "钱包交易记录视图")
public class WalletTransactionVO {

    @Schema(description = "交易ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "交易类型")
    private Integer type;

    @Schema(description = "交易类型文本")
    private String typeText;

    @Schema(description = "交易金额")
    private BigDecimal amount;

    @Schema(description = "交易前余额")
    private BigDecimal balanceBefore;

    @Schema(description = "交易后余额")
    private BigDecimal balanceAfter;

    @Schema(description = "交易描述")
    private String description;

    @Schema(description = "关联订单ID")
    private Long orderId;

    @Schema(description = "交易状态")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}

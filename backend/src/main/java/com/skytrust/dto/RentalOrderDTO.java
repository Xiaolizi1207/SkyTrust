package com.skytrust.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 租赁订单数据传输对象（用于创建和更新订单）
 *
 * @author SkyTrust Team
 */
@Data
@Schema(description = "租赁订单数据传输对象")
public class RentalOrderDTO {

    @Schema(description = "用户ID", example = "1", required = true)
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @Schema(description = "设备ID", example = "1", required = true)
    @NotNull(message = "设备ID不能为空")
    private Long deviceId;

    @Schema(description = "租赁开始时间", example = "2023-10-01T10:00:00", required = true)
    @NotNull(message = "租赁开始时间不能为空")
    @Future(message = "租赁开始时间必须是将来的时间")
    private LocalDateTime startTime;

    @Schema(description = "租赁结束时间", example = "2023-10-01T12:00:00", required = true)
    @NotNull(message = "租赁结束时间不能为空")
    @Future(message = "租赁结束时间必须是将来的时间")
    private LocalDateTime endTime;

    @Schema(description = "保险费用（元）", example = "50.00", required = true)
    @NotNull(message = "保险费用不能为空")
    @DecimalMin(value = "0.0", message = "保险费用不能小于0")
    @Digits(integer = 10, fraction = 2, message = "保险费用格式不正确")
    private BigDecimal insuranceFee;

    @Schema(description = "其他费用（元）", example = "0.00")
    @DecimalMin(value = "0.0", message = "其他费用不能小于0")
    @Digits(integer = 10, fraction = 2, message = "其他费用格式不正确")
    private BigDecimal otherFee = BigDecimal.ZERO;

    @Schema(description = "支付方式（alipay-支付宝，wechat-微信，wallet-钱包）", example = "alipay", required = true)
    @NotNull(message = "支付方式不能为空")
    @Size(max = 20, message = "支付方式长度不能超过20个字符")
    private String paymentMethod;

    @Schema(description = "智能合约地址", example = "0x742d35Cc6634C0532925a3b844Bc9e60F6433cdb")
    @Size(max = 100, message = "智能合约地址长度不能超过100个字符")
    private String contractAddress;

    @Schema(description = "备注", example = "希望设备电量充足")
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;
}
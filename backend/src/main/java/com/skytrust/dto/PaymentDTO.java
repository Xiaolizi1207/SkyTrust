package com.skytrust.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

/**
 * 支付数据传输对象（用于创建支付记录）
 *
 * @author SkyTrust Team
 */
@Data
@ApiModel(description = "支付数据传输对象")
public class PaymentDTO {

    @ApiModelProperty(value = "关联订单ID", example = "1", required = true)
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @ApiModelProperty(value = "用户ID", example = "1", required = true)
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @ApiModelProperty(value = "支付金额（元）", example = "450.00", required = true)
    @NotNull(message = "支付金额不能为空")
    @DecimalMin(value = "0.01", message = "支付金额必须大于0")
    @Digits(integer = 10, fraction = 2, message = "支付金额格式不正确")
    private BigDecimal amount;

    @ApiModelProperty(value = "支付方式（alipay-支付宝，wechat-微信，wallet-钱包）", example = "alipay", required = true)
    @NotBlank(message = "支付方式不能为空")
    @Size(max = 20, message = "支付方式长度不能超过20个字符")
    private String paymentMethod;

    @ApiModelProperty(value = "第三方支付平台交易号", example = "2023100122001401010123456789")
    @Size(max = 100, message = "交易号长度不能超过100个字符")
    private String transactionId;

    @ApiModelProperty(value = "支付备注", example = "租赁订单支付")
    @Size(max = 500, message = "支付备注长度不能超过500个字符")
    private String remark;

    @ApiModelProperty(value = "回调通知URL", example = "https://api.example.com/payment/notify")
    @Size(max = 500, message = "回调通知URL长度不能超过500个字符")
    private String notifyUrl;
}
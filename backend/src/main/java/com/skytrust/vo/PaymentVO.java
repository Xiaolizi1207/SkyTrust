package com.skytrust.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付记录视图对象（用于返回支付信息）
 *
 * @author SkyTrust Team
 */
@Data
@ApiModel(description = "支付记录视图对象")
public class PaymentVO {

    @ApiModelProperty(value = "支付记录ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "支付订单号", example = "PAY202310010001")
    private String transactionNo;

    @ApiModelProperty(value = "关联订单ID", example = "1")
    private Long orderId;

    @ApiModelProperty(value = "用户ID", example = "1")
    private Long userId;

    @ApiModelProperty(value = "支付金额（元）", example = "450.00")
    private BigDecimal amount;

    @ApiModelProperty(value = "支付状态（0-待支付，1-支付成功，2-支付失败，3-已退款）", example = "1")
    private Integer status;

    @ApiModelProperty(value = "支付方式（alipay-支付宝，wechat-微信，wallet-钱包）", example = "alipay")
    private String paymentMethod;

    @ApiModelProperty(value = "第三方支付平台交易号", example = "2023100122001401010123456789")
    private String transactionId;

    @ApiModelProperty(value = "支付完成时间", example = "2023-10-01T10:05:00")
    private LocalDateTime payTime;

    @ApiModelProperty(value = "支付备注", example = "租赁订单支付")
    private String remark;

    @ApiModelProperty(value = "回调通知URL", example = "https://api.example.com/payment/notify")
    private String notifyUrl;

    @ApiModelProperty(value = "回调状态（0-未回调，1-回调成功，2-回调失败）", example = "1")
    private Integer notifyStatus;

    @ApiModelProperty(value = "回调时间", example = "2023-10-01T10:06:00")
    private LocalDateTime notifyTime;

    @ApiModelProperty(value = "回调响应内容", example = "SUCCESS")
    private String notifyResponse;

    @ApiModelProperty(value = "创建时间", example = "2023-10-01T10:00:00")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间", example = "2023-10-01T10:06:00")
    private LocalDateTime updateTime;
}
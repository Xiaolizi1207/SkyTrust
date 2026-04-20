package com.skytrust.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 租赁订单视图对象（用于返回订单信息）
 *
 * @author SkyTrust Team
 */
@Data
@ApiModel(description = "租赁订单视图对象")
public class RentalOrderVO {

    @ApiModelProperty(value = "订单ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "订单号", example = "RO202310010001")
    private String orderNo;

    @ApiModelProperty(value = "用户ID", example = "1")
    private Long userId;

    @ApiModelProperty(value = "设备ID", example = "1")
    private Long deviceId;

    @ApiModelProperty(value = "租赁开始时间", example = "2023-10-01T10:00:00")
    private LocalDateTime startTime;

    @ApiModelProperty(value = "租赁结束时间", example = "2023-10-01T12:00:00")
    private LocalDateTime endTime;

    @ApiModelProperty(value = "实际开始时间", example = "2023-10-01T10:05:00")
    private LocalDateTime actualStartTime;

    @ApiModelProperty(value = "实际结束时间", example = "2023-10-01T11:55:00")
    private LocalDateTime actualEndTime;

    @ApiModelProperty(value = "租赁时长（小时）", example = "2.0")
    private BigDecimal duration;

    @ApiModelProperty(value = "租赁费用（元）", example = "400.00")
    private BigDecimal rentalFee;

    @ApiModelProperty(value = "保险费用（元）", example = "50.00")
    private BigDecimal insuranceFee;

    @ApiModelProperty(value = "其他费用（元）", example = "0.00")
    private BigDecimal otherFee;

    @ApiModelProperty(value = "总费用（元）", example = "450.00")
    private BigDecimal totalFee;

    @ApiModelProperty(value = "实际支付金额（元）", example = "450.00")
    private BigDecimal actualPayment;

    @ApiModelProperty(value = "支付状态（0-未支付，1-已支付，2-支付失败，3-已退款）", example = "1")
    private Integer paymentStatus;

    @ApiModelProperty(value = "订单状态（0-待开始，1-进行中，2-已完成，3-已取消，4-异常）", example = "1")
    private Integer orderStatus;

    @ApiModelProperty(value = "支付时间", example = "2023-10-01T09:55:00")
    private LocalDateTime paymentTime;

    @ApiModelProperty(value = "支付方式（alipay-支付宝，wechat-微信，wallet-钱包）", example = "alipay")
    private String paymentMethod;

    @ApiModelProperty(value = "交易号", example = "2023100122001401010123456789")
    private String transactionId;

    @ApiModelProperty(value = "智能合约地址", example = "0x742d35Cc6634C0532925a3b844Bc9e60F6433cdb")
    private String contractAddress;

    @ApiModelProperty(value = "区块链交易哈希", example = "0x1234567890abcdef...")
    private String blockchainTxHash;

    @ApiModelProperty(value = "用户评价（1-5星）", example = "5")
    private Integer userRating;

    @ApiModelProperty(value = "用户评价内容", example = "非常好用！")
    private String userComment;

    @ApiModelProperty(value = "备注", example = "希望设备电量充足")
    private String remark;

    @ApiModelProperty(value = "创建时间", example = "2023-10-01T09:50:00")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间", example = "2023-10-01T11:55:00")
    private LocalDateTime updateTime;
}
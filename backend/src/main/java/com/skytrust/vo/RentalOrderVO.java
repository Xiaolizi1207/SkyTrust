package com.skytrust.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 租赁订单视图对象（用于返回订单信息）
 *
 * @author SkyTrust Team
 */
@Data
@Schema(description = "租赁订单视图对象")
public class RentalOrderVO {

    @Schema(description = "订单ID", example = "1")
    private Long id;

    @Schema(description = "订单号", example = "RO202310010001")
    private String orderNo;

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "设备ID", example = "1")
    private Long deviceId;

    @Schema(description = "租赁开始时间", example = "2023-10-01T10:00:00")
    private LocalDateTime startTime;

    @Schema(description = "租赁结束时间", example = "2023-10-01T12:00:00")
    private LocalDateTime endTime;

    @Schema(description = "实际开始时间", example = "2023-10-01T10:05:00")
    private LocalDateTime actualStartTime;

    @Schema(description = "实际结束时间", example = "2023-10-01T11:55:00")
    private LocalDateTime actualEndTime;

    @Schema(description = "租赁时长（小时）", example = "2.0")
    private BigDecimal duration;

    @Schema(description = "租赁费用（元）", example = "400.00")
    private BigDecimal rentalFee;

    @Schema(description = "保险费用（元）", example = "50.00")
    private BigDecimal insuranceFee;

    @Schema(description = "其他费用（元）", example = "0.00")
    private BigDecimal otherFee;

    @Schema(description = "总费用（元）", example = "450.00")
    private BigDecimal totalFee;

    @Schema(description = "实际支付金额（元）", example = "450.00")
    private BigDecimal actualPayment;

    @Schema(description = "支付状态（0-未支付，1-已支付，2-支付失败，3-已退款）", example = "1")
    private Integer paymentStatus;

    @Schema(description = "订单状态（0-待开始，1-进行中，2-已完成，3-已取消，4-异常）", example = "1")
    private Integer orderStatus;

    @Schema(description = "支付时间", example = "2023-10-01T09:55:00")
    private LocalDateTime paymentTime;

    @Schema(description = "支付方式（alipay-支付宝，wechat-微信，wallet-钱包）", example = "alipay")
    private String paymentMethod;

    @Schema(description = "交易号", example = "2023100122001401010123456789")
    private String transactionId;

    @Schema(description = "智能合约地址", example = "0x742d35Cc6634C0532925a3b844Bc9e60F6433cdb")
    private String contractAddress;

    @Schema(description = "区块链交易哈希", example = "0x1234567890abcdef...")
    private String blockchainTxHash;

    @Schema(description = "用户评价（1-5星）", example = "5")
    private Integer userRating;

    @Schema(description = "用户评价内容", example = "非常好用！")
    private String userComment;

    @Schema(description = "备注", example = "希望设备电量充足")
    private String remark;

    @Schema(description = "创建时间", example = "2023-10-01T09:50:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2023-10-01T11:55:00")
    private LocalDateTime updateTime;
}
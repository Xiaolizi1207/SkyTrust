package com.skytrust.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 租赁订单实体类
 *
 * @author SkyTrust Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("rental_order")
public class RentalOrder extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 订单号（唯一）
     */
    @TableField(value = "order_no")
    private String orderNo;

    /**
     * 用户ID（关联用户表）
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 设备ID（关联设备表）
     */
    @TableField(value = "device_id")
    private Long deviceId;

    /**
     * 租赁开始时间
     */
    @TableField(value = "start_time")
    private LocalDateTime startTime;

    /**
     * 租赁结束时间
     */
    @TableField(value = "end_time")
    private LocalDateTime endTime;

    /**
     * 实际开始时间
     */
    @TableField(value = "actual_start_time")
    private LocalDateTime actualStartTime;

    /**
     * 实际结束时间
     */
    @TableField(value = "actual_end_time")
    private LocalDateTime actualEndTime;

    /**
     * 租赁时长（小时）
     */
    @TableField(value = "duration")
    private BigDecimal duration;

    /**
     * 租赁费用（元）
     */
    @TableField(value = "rental_fee")
    private BigDecimal rentalFee;

    /**
     * 保险费用（元）
     */
    @TableField(value = "insurance_fee")
    private BigDecimal insuranceFee;

    /**
     * 其他费用（元）
     */
    @TableField(value = "other_fee")
    private BigDecimal otherFee;

    /**
     * 总费用（元）
     */
    @TableField(value = "total_fee")
    private BigDecimal totalFee;

    /**
     * 实际支付金额（元）
     */
    @TableField(value = "actual_payment")
    private BigDecimal actualPayment;

    /**
     * 支付状态（0-未支付，1-已支付，2-支付失败，3-已退款）
     */
    @TableField(value = "payment_status")
    private Integer paymentStatus;

    /**
     * 订单状态（0-待开始，1-进行中，2-已完成，3-已取消，4-异常）
     */
    @TableField(value = "order_status")
    private Integer orderStatus;

    /**
     * 支付时间
     */
    @TableField(value = "payment_time")
    private LocalDateTime paymentTime;

    /**
     * 支付方式（alipay-支付宝，wechat-微信，wallet-钱包）
     */
    @TableField(value = "payment_method")
    private String paymentMethod;

    /**
     * 交易号（第三方支付平台）
     */
    @TableField(value = "transaction_id")
    private String transactionId;

    /**
     * 智能合约地址
     */
    @TableField(value = "contract_address")
    private String contractAddress;

    /**
     * 区块链交易哈希
     */
    @TableField(value = "blockchain_tx_hash")
    private String blockchainTxHash;

    /**
     * 用户评价（1-5星）
     */
    @TableField(value = "user_rating")
    private Integer userRating;

    /**
     * 用户评价内容
     */
    @TableField(value = "user_comment")
    private String userComment;

    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;

    /**
     * 押金金额（元）
     */
    @TableField(value = "deposit_amount")
    private BigDecimal depositAmount;

    /**
     * 取消原因
     */
    @TableField(value = "cancel_reason")
    private String cancelReason;

    /**
     * 取消时间
     */
    @TableField(value = "cancel_time")
    private LocalDateTime cancelTime;

    // 别名方法，用于向后兼容
    public Integer getStatus() {
        return orderStatus;
    }

    public void setStatus(Integer status) {
        this.orderStatus = status;
    }

    public BigDecimal getTotalAmount() {
        return totalFee;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalFee = totalAmount;
    }
}
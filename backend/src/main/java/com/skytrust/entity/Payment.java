package com.skytrust.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付记录实体类
 *
 * @author SkyTrust Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("payment")
public class Payment extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 支付订单号（唯一）
     */
    @TableField(value = "payment_no")
    private String transactionNo;

    /**
     * 关联订单ID（rental_order表）
     */
    @TableField(value = "order_id")
    private Long orderId;

    /**
     * 用户ID（关联用户表）
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 支付金额（元）
     */
    @TableField(value = "amount")
    private BigDecimal amount;

    /**
     * 支付状态（0-待支付，1-支付成功，2-支付失败，3-已退款）
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 支付方式（alipay-支付宝，wechat-微信，wallet-钱包）
     */
    @TableField(value = "payment_method")
    private String paymentMethod;

    /**
     * 第三方支付平台交易号
     */
    @TableField(value = "transaction_id")
    private String transactionId;

    /**
     * 支付完成时间
     */
    @TableField(value = "pay_time")
    private LocalDateTime payTime;

    /**
     * 支付备注
     */
    @TableField(value = "remark")
    private String remark;

    /**
     * 回调通知URL
     */
    @TableField(value = "notify_url")
    private String notifyUrl;

    /**
     * 回调状态（0-未回调，1-回调成功，2-回调失败）
     */
    @TableField(value = "notify_status")
    private Integer notifyStatus;

    /**
     * 回调时间
     */
    @TableField(value = "notify_time")
    private LocalDateTime notifyTime;

    /**
     * 回调响应内容
     */
    @TableField(value = "notify_response")
    private String notifyResponse;

    // Getter and Setter methods
    public String getTransactionNo() {
        return transactionNo;
    }

    public void setTransactionNo(String transactionNo) {
        this.transactionNo = transactionNo;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public LocalDateTime getPayTime() {
        return payTime;
    }

    public void setPayTime(LocalDateTime payTime) {
        this.payTime = payTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }

    public Integer getNotifyStatus() {
        return notifyStatus;
    }

    public void setNotifyStatus(Integer notifyStatus) {
        this.notifyStatus = notifyStatus;
    }

    public LocalDateTime getNotifyTime() {
        return notifyTime;
    }

    public void setNotifyTime(LocalDateTime notifyTime) {
        this.notifyTime = notifyTime;
    }

    public String getNotifyResponse() {
        return notifyResponse;
    }

    public void setNotifyResponse(String notifyResponse) {
        this.notifyResponse = notifyResponse;
    }
}
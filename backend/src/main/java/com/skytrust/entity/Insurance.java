package com.skytrust.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 保险记录实体类
 *
 * @author SkyTrust Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("insurance")
public class Insurance extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 保险单号（唯一）
     */
    @TableField(value = "insurance_no")
    private String insuranceNo;

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
     * 设备ID（关联设备表）
     */
    @TableField(value = "device_id")
    private Long deviceId;

    /**
     * 保险类型（0-基础保险，1-高级保险，2-全险）
     */
    @TableField(value = "insurance_type")
    private Integer insuranceType;

    /**
     * 保险金额（元）
     */
    @TableField(value = "amount")
    private BigDecimal amount;

    /**
     * 保险费率（百分比）
     */
    @TableField(value = "rate")
    private BigDecimal rate;

    /**
     * 保险开始时间
     */
    @TableField(value = "start_time")
    private LocalDateTime startTime;

    /**
     * 保险结束时间
     */
    @TableField(value = "end_time")
    private LocalDateTime endTime;

    /**
     * 保险状态（0-未生效，1-生效中，2-已过期，3-已理赔）
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 理赔金额（元）
     */
    @TableField(value = "claim_amount")
    private BigDecimal claimAmount;

    /**
     * 理赔时间
     */
    @TableField(value = "claim_time")
    private LocalDateTime claimTime;

    /**
     * 理赔原因
     */
    @TableField(value = "claim_reason")
    private String claimReason;

    /**
     * 理赔状态（0-未理赔，1-申请中，2-已赔付，3-已拒绝）
     */
    @TableField(value = "claim_status")
    private Integer claimStatus;

    /**
     * 保险条款JSON
     */
    @TableField(value = "terms")
    private String terms;

    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;

    // Getter and Setter methods
    public String getInsuranceNo() {
        return insuranceNo;
    }

    public void setInsuranceNo(String insuranceNo) {
        this.insuranceNo = insuranceNo;
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

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public Integer getInsuranceType() {
        return insuranceType;
    }

    public void setInsuranceType(Integer insuranceType) {
        this.insuranceType = insuranceType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public BigDecimal getClaimAmount() {
        return claimAmount;
    }

    public void setClaimAmount(BigDecimal claimAmount) {
        this.claimAmount = claimAmount;
    }

    public LocalDateTime getClaimTime() {
        return claimTime;
    }

    public void setClaimTime(LocalDateTime claimTime) {
        this.claimTime = claimTime;
    }

    public String getClaimReason() {
        return claimReason;
    }

    public void setClaimReason(String claimReason) {
        this.claimReason = claimReason;
    }

    public Integer getClaimStatus() {
        return claimStatus;
    }

    public void setClaimStatus(Integer claimStatus) {
        this.claimStatus = claimStatus;
    }

    public String getTerms() {
        return terms;
    }

    public void setTerms(String terms) {
        this.terms = terms;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    /**
     * 获取保单号（insuranceNo的别名）
     */
    public String getPolicyNo() {
        return insuranceNo;
    }

    /**
     * 设置保单号（insuranceNo的别名）
     */
    public void setPolicyNo(String policyNo) {
        this.insuranceNo = policyNo;
    }
}
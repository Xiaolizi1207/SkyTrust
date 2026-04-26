package com.skytrust.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 保险记录视图对象（用于返回保险信息）
 *
 * @author SkyTrust Team
 */
@Data
@Schema(description = "保险记录视图对象")
public class InsuranceVO {

    @Schema(description = "保险记录ID", example = "1")
    private Long id;

    @Schema(description = "保险单号（唯一）", example = "INS202310010001")
    private String insuranceNo;

    @Schema(description = "关联订单ID", example = "1")
    private Long orderId;

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "设备ID", example = "1")
    private Long deviceId;

    @Schema(description = "保险类型（0-基础保险，1-高级保险，2-全险）", example = "1")
    private Integer insuranceType;

    @Schema(description = "保险金额（元）", example = "500.00")
    private BigDecimal amount;

    @Schema(description = "保险费率（百分比）", example = "5.00")
    private BigDecimal rate;

    @Schema(description = "保险开始时间", example = "2023-10-01T10:00:00")
    private LocalDateTime startTime;

    @Schema(description = "保险结束时间", example = "2023-12-01T10:00:00")
    private LocalDateTime endTime;

    @Schema(description = "保险状态（0-未生效，1-生效中，2-已过期，3-已理赔）", example = "1")
    private Integer status;

    @Schema(description = "理赔金额（元）", example = "200.00")
    private BigDecimal claimAmount;

    @Schema(description = "理赔时间", example = "2023-11-01T14:30:00")
    private LocalDateTime claimTime;

    @Schema(description = "理赔原因", example = "设备损坏")
    private String claimReason;

    @Schema(description = "理赔状态（0-未理赔，1-申请中，2-已赔付，3-已拒绝）", example = "2")
    private Integer claimStatus;

    @Schema(description = "保险条款JSON", example = "{\"coverage\": \"设备损坏\", \"deductible\": 100}")
    private String terms;

    @Schema(description = "备注", example = "租赁设备保险")
    private String remark;

    @Schema(description = "创建时间", example = "2023-10-01T10:00:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2023-11-01T14:30:00")
    private LocalDateTime updateTime;
}
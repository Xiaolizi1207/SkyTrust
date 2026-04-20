package com.skytrust.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 保险记录视图对象（用于返回保险信息）
 *
 * @author SkyTrust Team
 */
@Data
@ApiModel(description = "保险记录视图对象")
public class InsuranceVO {

    @ApiModelProperty(value = "保险记录ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "保险单号（唯一）", example = "INS202310010001")
    private String insuranceNo;

    @ApiModelProperty(value = "关联订单ID", example = "1")
    private Long orderId;

    @ApiModelProperty(value = "用户ID", example = "1")
    private Long userId;

    @ApiModelProperty(value = "设备ID", example = "1")
    private Long deviceId;

    @ApiModelProperty(value = "保险类型（0-基础保险，1-高级保险，2-全险）", example = "1")
    private Integer insuranceType;

    @ApiModelProperty(value = "保险金额（元）", example = "500.00")
    private BigDecimal amount;

    @ApiModelProperty(value = "保险费率（百分比）", example = "5.00")
    private BigDecimal rate;

    @ApiModelProperty(value = "保险开始时间", example = "2023-10-01T10:00:00")
    private LocalDateTime startTime;

    @ApiModelProperty(value = "保险结束时间", example = "2023-12-01T10:00:00")
    private LocalDateTime endTime;

    @ApiModelProperty(value = "保险状态（0-未生效，1-生效中，2-已过期，3-已理赔）", example = "1")
    private Integer status;

    @ApiModelProperty(value = "理赔金额（元）", example = "200.00")
    private BigDecimal claimAmount;

    @ApiModelProperty(value = "理赔时间", example = "2023-11-01T14:30:00")
    private LocalDateTime claimTime;

    @ApiModelProperty(value = "理赔原因", example = "设备损坏")
    private String claimReason;

    @ApiModelProperty(value = "理赔状态（0-未理赔，1-申请中，2-已赔付，3-已拒绝）", example = "2")
    private Integer claimStatus;

    @ApiModelProperty(value = "保险条款JSON", example = "{\"coverage\": \"设备损坏\", \"deductible\": 100}")
    private String terms;

    @ApiModelProperty(value = "备注", example = "租赁设备保险")
    private String remark;

    @ApiModelProperty(value = "创建时间", example = "2023-10-01T10:00:00")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间", example = "2023-11-01T14:30:00")
    private LocalDateTime updateTime;
}
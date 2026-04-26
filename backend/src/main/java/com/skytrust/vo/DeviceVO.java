package com.skytrust.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 设备视图对象（用于返回设备信息）
 *
 * @author SkyTrust Team
 */
@Data
@Schema(description = "设备视图对象")
public class DeviceVO {

    @Schema(description = "设备ID", example = "1")
    private Long id;

    @Schema(description = "设备名称", example = "大疆无人机 Mavic 3")
    private String deviceName;

    @Schema(description = "设备型号", example = "Mavic 3 Pro")
    private String model;

    @Schema(description = "设备序列号", example = "DJI20231001001")
    private String serialNumber;

    @Schema(description = "设备状态（0-离线，1-在线，2-飞行中，3-维修中，4-已报废）", example = "1")
    private Integer status;

    @Schema(description = "当前纬度", example = "39.9042")
    private BigDecimal latitude;

    @Schema(description = "当前经度", example = "116.4074")
    private BigDecimal longitude;

    @Schema(description = "当前高度（米）", example = "100.5")
    private BigDecimal altitude;

    @Schema(description = "电池电量（百分比）", example = "85")
    private Integer batteryLevel;

    @Schema(description = "飞行速度（米/秒）", example = "15.5")
    private BigDecimal speed;

    @Schema(description = "飞行总时长（小时）", example = "150.5")
    private BigDecimal totalFlightHours;

    @Schema(description = "设备所有者ID", example = "1")
    private Long ownerId;

    @Schema(description = "租赁价格（元/小时）", example = "200.00")
    private BigDecimal rentalPrice;

    @Schema(description = "保险费用（元/次）", example = "50.00")
    private BigDecimal insuranceFee;

    @Schema(description = "设备描述", example = "这是一台高性能无人机")
    private String description;

    @Schema(description = "设备图片URL（多个用逗号分隔）", example = "https://example.com/image1.jpg,https://example.com/image2.jpg")
    private String images;

    @Schema(description = "设备规格JSON", example = "{\"camera\":\"4K\",\"range\":\"10km\"}")
    private String specifications;

    @Schema(description = "最后上线时间", example = "2023-10-01 12:00:00")
    private LocalDateTime lastOnlineTime;

    @Schema(description = "最后维护时间", example = "2023-10-01 12:00:00")
    private LocalDateTime lastMaintenanceTime;

    @Schema(description = "备注", example = "设备保养良好")
    private String remark;

    @Schema(description = "创建时间", example = "2023-10-01 12:00:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2023-10-01 12:00:00")
    private LocalDateTime updateTime;
}
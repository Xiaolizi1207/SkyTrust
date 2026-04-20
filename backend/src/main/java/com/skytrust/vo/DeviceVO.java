package com.skytrust.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 设备视图对象（用于返回设备信息）
 *
 * @author SkyTrust Team
 */
@Data
@ApiModel(description = "设备视图对象")
public class DeviceVO {

    @ApiModelProperty(value = "设备ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "设备名称", example = "大疆无人机 Mavic 3")
    private String deviceName;

    @ApiModelProperty(value = "设备型号", example = "Mavic 3 Pro")
    private String model;

    @ApiModelProperty(value = "设备序列号", example = "DJI20231001001")
    private String serialNumber;

    @ApiModelProperty(value = "设备状态（0-离线，1-在线，2-飞行中，3-维修中，4-已报废）", example = "1")
    private Integer status;

    @ApiModelProperty(value = "当前纬度", example = "39.9042")
    private BigDecimal latitude;

    @ApiModelProperty(value = "当前经度", example = "116.4074")
    private BigDecimal longitude;

    @ApiModelProperty(value = "当前高度（米）", example = "100.5")
    private BigDecimal altitude;

    @ApiModelProperty(value = "电池电量（百分比）", example = "85")
    private Integer batteryLevel;

    @ApiModelProperty(value = "飞行速度（米/秒）", example = "15.5")
    private BigDecimal speed;

    @ApiModelProperty(value = "飞行总时长（小时）", example = "150.5")
    private BigDecimal totalFlightHours;

    @ApiModelProperty(value = "设备所有者ID", example = "1")
    private Long ownerId;

    @ApiModelProperty(value = "租赁价格（元/小时）", example = "200.00")
    private BigDecimal rentalPrice;

    @ApiModelProperty(value = "保险费用（元/次）", example = "50.00")
    private BigDecimal insuranceFee;

    @ApiModelProperty(value = "设备描述", example = "这是一台高性能无人机")
    private String description;

    @ApiModelProperty(value = "设备图片URL（多个用逗号分隔）", example = "https://example.com/image1.jpg,https://example.com/image2.jpg")
    private String images;

    @ApiModelProperty(value = "设备规格JSON", example = "{\"camera\":\"4K\",\"range\":\"10km\"}")
    private String specifications;

    @ApiModelProperty(value = "最后上线时间", example = "2023-10-01 12:00:00")
    private LocalDateTime lastOnlineTime;

    @ApiModelProperty(value = "最后维护时间", example = "2023-10-01 12:00:00")
    private LocalDateTime lastMaintenanceTime;

    @ApiModelProperty(value = "备注", example = "设备保养良好")
    private String remark;

    @ApiModelProperty(value = "创建时间", example = "2023-10-01 12:00:00")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间", example = "2023-10-01 12:00:00")
    private LocalDateTime updateTime;
}
package com.skytrust.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 设备位置轨迹视图对象（用于返回设备轨迹信息）
 *
 * @author SkyTrust Team
 */
@Data
@ApiModel(description = "设备位置轨迹视图对象")
public class DeviceTrackVO {

    @ApiModelProperty(value = "轨迹记录ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "设备ID", example = "1")
    private Long deviceId;

    @ApiModelProperty(value = "订单ID（关联租赁订单表，可为空）", example = "1")
    private Long orderId;

    @ApiModelProperty(value = "纬度", example = "39.9042")
    private BigDecimal latitude;

    @ApiModelProperty(value = "经度", example = "116.4074")
    private BigDecimal longitude;

    @ApiModelProperty(value = "高度（米）", example = "100.0")
    private BigDecimal altitude;

    @ApiModelProperty(value = "速度（米/秒）", example = "10.5")
    private BigDecimal speed;

    @ApiModelProperty(value = "方向角度（0-360度）", example = "45.0")
    private BigDecimal heading;

    @ApiModelProperty(value = "电池电量（百分比）", example = "85")
    private Integer batteryLevel;

    @ApiModelProperty(value = "信号强度（0-100）", example = "75")
    private Integer signalStrength;

    @ApiModelProperty(value = "卫星数量", example = "12")
    private Integer satelliteCount;

    @ApiModelProperty(value = "定位精度（米）", example = "5.0")
    private BigDecimal accuracy;

    @ApiModelProperty(value = "位置来源（gps-卫星定位，network-网络定位，fused-融合定位）", example = "gps")
    private String source;

    @ApiModelProperty(value = "记录时间", example = "2023-10-01T10:00:00")
    private LocalDateTime recordTime;

    @ApiModelProperty(value = "是否为异常位置（0-正常，1-异常）", example = "false")
    private Boolean abnormal;

    @ApiModelProperty(value = "异常原因", example = "超出飞行区域")
    private String abnormalReason;

    @ApiModelProperty(value = "备注", example = "正常飞行轨迹")
    private String remark;

    @ApiModelProperty(value = "创建时间", example = "2023-10-01T10:00:00")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间", example = "2023-10-01T10:00:00")
    private LocalDateTime updateTime;
}
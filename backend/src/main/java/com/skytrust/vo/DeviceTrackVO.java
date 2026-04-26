package com.skytrust.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 设备位置轨迹视图对象（用于返回设备轨迹信息）
 *
 * @author SkyTrust Team
 */
@Data
@Schema(description = "设备位置轨迹视图对象")
public class DeviceTrackVO {

    @Schema(description = "轨迹记录ID", example = "1")
    private Long id;

    @Schema(description = "设备ID", example = "1")
    private Long deviceId;

    @Schema(description = "订单ID（关联租赁订单表，可为空）", example = "1")
    private Long orderId;

    @Schema(description = "纬度", example = "39.9042")
    private BigDecimal latitude;

    @Schema(description = "经度", example = "116.4074")
    private BigDecimal longitude;

    @Schema(description = "高度（米）", example = "100.0")
    private BigDecimal altitude;

    @Schema(description = "速度（米/秒）", example = "10.5")
    private BigDecimal speed;

    @Schema(description = "方向角度（0-360度）", example = "45.0")
    private BigDecimal heading;

    @Schema(description = "电池电量（百分比）", example = "85")
    private Integer batteryLevel;

    @Schema(description = "信号强度（0-100）", example = "75")
    private Integer signalStrength;

    @Schema(description = "卫星数量", example = "12")
    private Integer satelliteCount;

    @Schema(description = "定位精度（米）", example = "5.0")
    private BigDecimal accuracy;

    @Schema(description = "位置来源（gps-卫星定位，network-网络定位，fused-融合定位）", example = "gps")
    private String source;

    @Schema(description = "记录时间", example = "2023-10-01T10:00:00")
    private LocalDateTime recordTime;

    @Schema(description = "是否为异常位置（0-正常，1-异常）", example = "false")
    private Boolean abnormal;

    @Schema(description = "异常原因", example = "超出飞行区域")
    private String abnormalReason;

    @Schema(description = "备注", example = "正常飞行轨迹")
    private String remark;

    @Schema(description = "创建时间", example = "2023-10-01T10:00:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2023-10-01T10:00:00")
    private LocalDateTime updateTime;
}
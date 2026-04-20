package com.skytrust.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 设备位置轨迹数据传输对象（用于创建设备轨迹记录）
 *
 * @author SkyTrust Team
 */
@Data
@ApiModel(description = "设备位置轨迹数据传输对象")
public class DeviceTrackDTO {

    @ApiModelProperty(value = "设备ID", example = "1", required = true)
    @NotNull(message = "设备ID不能为空")
    private Long deviceId;

    @ApiModelProperty(value = "订单ID（关联租赁订单表，可为空）", example = "1")
    private Long orderId;

    @ApiModelProperty(value = "纬度", example = "39.9042", required = true)
    @NotNull(message = "纬度不能为空")
    @DecimalMin(value = "-90.0", message = "纬度范围必须在-90到90之间")
    @DecimalMax(value = "90.0", message = "纬度范围必须在-90到90之间")
    @Digits(integer = 3, fraction = 6, message = "纬度格式不正确")
    private BigDecimal latitude;

    @ApiModelProperty(value = "经度", example = "116.4074", required = true)
    @NotNull(message = "经度不能为空")
    @DecimalMin(value = "-180.0", message = "经度范围必须在-180到180之间")
    @DecimalMax(value = "180.0", message = "经度范围必须在-180到180之间")
    @Digits(integer = 3, fraction = 6, message = "经度格式不正确")
    private BigDecimal longitude;

    @ApiModelProperty(value = "高度（米）", example = "100.0")
    @DecimalMin(value = "0.0", message = "高度不能为负数")
    @Digits(integer = 5, fraction = 2, message = "高度格式不正确")
    private BigDecimal altitude;

    @ApiModelProperty(value = "速度（米/秒）", example = "10.5")
    @DecimalMin(value = "0.0", message = "速度不能为负数")
    @Digits(integer = 4, fraction = 2, message = "速度格式不正确")
    private BigDecimal speed;

    @ApiModelProperty(value = "方向角度（0-360度）", example = "45.0")
    @DecimalMin(value = "0.0", message = "方向角度不能小于0")
    @DecimalMax(value = "360.0", message = "方向角度不能大于360")
    @Digits(integer = 3, fraction = 2, message = "方向角度格式不正确")
    private BigDecimal heading;

    @ApiModelProperty(value = "电池电量（百分比）", example = "85")
    @Min(value = 0, message = "电池电量必须在0-100之间")
    @Max(value = 100, message = "电池电量必须在0-100之间")
    private Integer batteryLevel;

    @ApiModelProperty(value = "信号强度（0-100）", example = "75")
    @Min(value = 0, message = "信号强度必须在0-100之间")
    @Max(value = 100, message = "信号强度必须在0-100之间")
    private Integer signalStrength;

    @ApiModelProperty(value = "卫星数量", example = "12")
    @Min(value = 0, message = "卫星数量不能为负数")
    private Integer satelliteCount;

    @ApiModelProperty(value = "定位精度（米）", example = "5.0")
    @DecimalMin(value = "0.0", message = "定位精度不能为负数")
    @Digits(integer = 3, fraction = 2, message = "定位精度格式不正确")
    private BigDecimal accuracy;

    @ApiModelProperty(value = "位置来源（gps-卫星定位，network-网络定位，fused-融合定位）", example = "gps")
    @Size(max = 20, message = "位置来源长度不能超过20个字符")
    private String source;

    @ApiModelProperty(value = "记录时间", example = "2023-10-01T10:00:00", required = true)
    @NotNull(message = "记录时间不能为空")
    private LocalDateTime recordTime;

    @ApiModelProperty(value = "是否为异常位置（0-正常，1-异常）", example = "false")
    private Boolean abnormal;

    @ApiModelProperty(value = "异常原因", example = "超出飞行区域")
    @Size(max = 500, message = "异常原因长度不能超过500个字符")
    private String abnormalReason;

    @ApiModelProperty(value = "备注", example = "正常飞行轨迹")
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;
}
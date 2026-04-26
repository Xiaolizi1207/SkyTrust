package com.skytrust.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

/**
 * 设备数据传输对象（用于创建和更新设备）
 *
 * @author SkyTrust Team
 */
@Data
@Schema(description = "设备数据传输对象")
public class DeviceDTO {

    @Schema(description = "设备名称", example = "大疆无人机 Mavic 3", required = true)
    @NotBlank(message = "设备名称不能为空")
    @Size(max = 100, message = "设备名称长度不能超过100个字符")
    private String deviceName;

    @Schema(description = "设备型号", example = "Mavic 3 Pro", required = true)
    @NotBlank(message = "设备型号不能为空")
    @Size(max = 50, message = "设备型号长度不能超过50个字符")
    private String model;

    @Schema(description = "设备序列号（唯一）", example = "DJI20231001001", required = true)
    @NotBlank(message = "设备序列号不能为空")
    @Size(max = 100, message = "设备序列号长度不能超过100个字符")
    private String serialNumber;

    @Schema(description = "设备状态（0-离线，1-在线，2-飞行中，3-维修中，4-已报废）", example = "1", required = true)
    @NotNull(message = "设备状态不能为空")
    private Integer status;

    @Schema(description = "当前纬度", example = "39.9042")
    @DecimalMin(value = "-90.0", message = "纬度不能小于-90")
    @DecimalMax(value = "90.0", message = "纬度不能大于90")
    @Digits(integer = 3, fraction = 6, message = "纬度格式不正确")
    private BigDecimal latitude;

    @Schema(description = "当前经度", example = "116.4074")
    @DecimalMin(value = "-180.0", message = "经度不能小于-180")
    @DecimalMax(value = "180.0", message = "经度不能大于180")
    @Digits(integer = 3, fraction = 6, message = "经度格式不正确")
    private BigDecimal longitude;

    @Schema(description = "当前高度（米）", example = "100.5")
    @DecimalMin(value = "0.0", message = "高度不能小于0")
    @Digits(integer = 5, fraction = 2, message = "高度格式不正确")
    private BigDecimal altitude;

    @Schema(description = "电池电量（百分比）", example = "85")
    private Integer batteryLevel;

    @Schema(description = "飞行速度（米/秒）", example = "15.5")
    @DecimalMin(value = "0.0", message = "飞行速度不能小于0")
    @Digits(integer = 5, fraction = 2, message = "飞行速度格式不正确")
    private BigDecimal speed;

    @Schema(description = "飞行总时长（小时）", example = "150.5")
    @DecimalMin(value = "0.0", message = "飞行总时长不能小于0")
    @Digits(integer = 10, fraction = 2, message = "飞行总时长格式不正确")
    private BigDecimal totalFlightHours;

    @Schema(description = "设备所有者ID", example = "1", required = true)
    @NotNull(message = "设备所有者ID不能为空")
    private Long ownerId;

    @Schema(description = "租赁价格（元/小时）", example = "200.00", required = true)
    @NotNull(message = "租赁价格不能为空")
    @DecimalMin(value = "0.0", message = "租赁价格不能小于0")
    @Digits(integer = 10, fraction = 2, message = "租赁价格格式不正确")
    private BigDecimal rentalPrice;

    @Schema(description = "保险费用（元/次）", example = "50.00", required = true)
    @NotNull(message = "保险费用不能为空")
    @DecimalMin(value = "0.0", message = "保险费用不能小于0")
    @Digits(integer = 10, fraction = 2, message = "保险费用格式不正确")
    private BigDecimal insuranceFee;

    @Schema(description = "设备描述", example = "这是一台高性能无人机")
    @Size(max = 1000, message = "设备描述长度不能超过1000个字符")
    private String description;

    @Schema(description = "设备图片URL（多个用逗号分隔）", example = "https://example.com/image1.jpg,https://example.com/image2.jpg")
    @Size(max = 2000, message = "设备图片URL长度不能超过2000个字符")
    private String images;

    @Schema(description = "设备规格JSON", example = "{\"camera\":\"4K\",\"range\":\"10km\"}")
    @Size(max = 2000, message = "设备规格JSON长度不能超过2000个字符")
    private String specifications;

    @Schema(description = "备注", example = "设备保养良好")
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;
}
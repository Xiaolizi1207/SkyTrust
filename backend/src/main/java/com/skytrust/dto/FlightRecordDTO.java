package com.skytrust.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 飞行记录数据传输对象（用于创建飞行记录）
 *
 * @author SkyTrust Team
 */
@Data
@ApiModel(description = "飞行记录数据传输对象")
public class FlightRecordDTO {

    @ApiModelProperty(value = "订单ID", example = "1", required = true)
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @ApiModelProperty(value = "设备ID", example = "1", required = true)
    @NotNull(message = "设备ID不能为空")
    private Long deviceId;

    @ApiModelProperty(value = "用户ID", example = "1", required = true)
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @ApiModelProperty(value = "飞行开始时间", example = "2023-10-01T10:00:00", required = true)
    @NotNull(message = "飞行开始时间不能为空")
    private LocalDateTime startTime;

    @ApiModelProperty(value = "飞行结束时间", example = "2023-10-01T11:00:00", required = true)
    @NotNull(message = "飞行结束时间不能为空")
    private LocalDateTime endTime;

    @ApiModelProperty(value = "起飞地点纬度", example = "39.9042", required = true)
    @NotNull(message = "起飞地点纬度不能为空")
    @DecimalMin(value = "-90.0", message = "纬度范围必须在-90到90之间")
    @DecimalMax(value = "90.0", message = "纬度范围必须在-90到90之间")
    @Digits(integer = 3, fraction = 6, message = "纬度格式不正确")
    private BigDecimal takeoffLatitude;

    @ApiModelProperty(value = "起飞地点经度", example = "116.4074", required = true)
    @NotNull(message = "起飞地点经度不能为空")
    @DecimalMin(value = "-180.0", message = "经度范围必须在-180到180之间")
    @DecimalMax(value = "180.0", message = "经度范围必须在-180到180之间")
    @Digits(integer = 3, fraction = 6, message = "经度格式不正确")
    private BigDecimal takeoffLongitude;

    @ApiModelProperty(value = "降落地点纬度", example = "39.9045", required = true)
    @NotNull(message = "降落地点纬度不能为空")
    @DecimalMin(value = "-90.0", message = "纬度范围必须在-90到90之间")
    @DecimalMax(value = "90.0", message = "纬度范围必须在-90到90之间")
    @Digits(integer = 3, fraction = 6, message = "纬度格式不正确")
    private BigDecimal landingLatitude;

    @ApiModelProperty(value = "降落地点经度", example = "116.4078", required = true)
    @NotNull(message = "降落地点经度不能为空")
    @DecimalMin(value = "-180.0", message = "经度范围必须在-180到180之间")
    @DecimalMax(value = "180.0", message = "经度范围必须在-180到180之间")
    @Digits(integer = 3, fraction = 6, message = "经度格式不正确")
    private BigDecimal landingLongitude;

    @ApiModelProperty(value = "天气状况JSON", example = "{\"temperature\": 25, \"windSpeed\": 5}")
    @Size(max = 1000, message = "天气状况数据长度不能超过1000个字符")
    private String weatherInfo;

    @ApiModelProperty(value = "飞行路径数据（JSON格式的轨迹点数组）", example = "[{\"lat\": 39.9042, \"lng\": 116.4074}]")
    @Size(max = 10000, message = "飞行路径数据长度不能超过10000个字符")
    private String flightPath;

    @ApiModelProperty(value = "飞行参数JSON（如风速、温度等）", example = "{\"windSpeed\": 5, \"temperature\": 25}")
    @Size(max = 2000, message = "飞行参数数据长度不能超过2000个字符")
    private String flightParams;

    @ApiModelProperty(value = "异常事件记录JSON", example = "[{\"time\": \"2023-10-01T10:30:00\", \"event\": \"strong_wind\"}]")
    @Size(max = 2000, message = "异常事件记录长度不能超过2000个字符")
    private String incidents;

    @ApiModelProperty(value = "AI分析结果JSON", example = "{\"flight_quality\": \"good\", \"risk_level\": \"low\"}")
    @Size(max = 2000, message = "AI分析结果长度不能超过2000个字符")
    private String aiAnalysis;

    @ApiModelProperty(value = "飞行评分（1-10分）", example = "8")
    @Min(value = 1, message = "飞行评分必须在1-10之间")
    @Max(value = 10, message = "飞行评分必须在1-10之间")
    private Integer flightScore;

    @ApiModelProperty(value = "是否违规（0-否，1-是）", example = "false")
    private Boolean violation;

    @ApiModelProperty(value = "违规原因", example = "超出飞行高度限制")
    @Size(max = 500, message = "违规原因长度不能超过500个字符")
    private String violationReason;

    @ApiModelProperty(value = "备注", example = "正常飞行记录")
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;
}
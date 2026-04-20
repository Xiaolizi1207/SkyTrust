package com.skytrust.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 飞行记录视图对象（用于返回飞行记录信息）
 *
 * @author SkyTrust Team
 */
@Data
@ApiModel(description = "飞行记录视图对象")
public class FlightRecordVO {

    @ApiModelProperty(value = "飞行记录ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "飞行记录编号（唯一）", example = "FL202310010001")
    private String flightNo;

    @ApiModelProperty(value = "订单ID", example = "1")
    private Long orderId;

    @ApiModelProperty(value = "设备ID", example = "1")
    private Long deviceId;

    @ApiModelProperty(value = "用户ID", example = "1")
    private Long userId;

    @ApiModelProperty(value = "飞行开始时间", example = "2023-10-01T10:00:00")
    private LocalDateTime startTime;

    @ApiModelProperty(value = "飞行结束时间", example = "2023-10-01T11:00:00")
    private LocalDateTime endTime;

    @ApiModelProperty(value = "实际开始时间", example = "2023-10-01T10:01:00")
    private LocalDateTime actualStartTime;

    @ApiModelProperty(value = "实际结束时间", example = "2023-10-01T11:02:00")
    private LocalDateTime actualEndTime;

    @ApiModelProperty(value = "飞行时长（分钟）", example = "61")
    private Integer duration;

    @ApiModelProperty(value = "飞行距离（米）", example = "1500.5")
    private BigDecimal distance;

    @ApiModelProperty(value = "最大高度（米）", example = "120.0")
    private BigDecimal maxAltitude;

    @ApiModelProperty(value = "最大速度（米/秒）", example = "15.5")
    private BigDecimal maxSpeed;

    @ApiModelProperty(value = "平均速度（米/秒）", example = "10.2")
    private BigDecimal avgSpeed;

    @ApiModelProperty(value = "起飞地点纬度", example = "39.9042")
    private BigDecimal takeoffLatitude;

    @ApiModelProperty(value = "起飞地点经度", example = "116.4074")
    private BigDecimal takeoffLongitude;

    @ApiModelProperty(value = "降落地点纬度", example = "39.9045")
    private BigDecimal landingLatitude;

    @ApiModelProperty(value = "降落地点经度", example = "116.4078")
    private BigDecimal landingLongitude;

    @ApiModelProperty(value = "飞行状态（0-准备中，1-飞行中，2-已完成，3-已取消，4-异常终止）", example = "2")
    private Integer flightStatus;

    @ApiModelProperty(value = "天气状况JSON", example = "{\"temperature\": 25, \"windSpeed\": 5}")
    private String weatherInfo;

    @ApiModelProperty(value = "飞行路径数据（JSON格式的轨迹点数组）", example = "[{\"lat\": 39.9042, \"lng\": 116.4074}]")
    private String flightPath;

    @ApiModelProperty(value = "飞行参数JSON（如风速、温度等）", example = "{\"windSpeed\": 5, \"temperature\": 25}")
    private String flightParams;

    @ApiModelProperty(value = "异常事件记录JSON", example = "[{\"time\": \"2023-10-01T10:30:00\", \"event\": \"strong_wind\"}]")
    private String incidents;

    @ApiModelProperty(value = "AI分析结果JSON", example = "{\"flight_quality\": \"good\", \"risk_level\": \"low\"}")
    private String aiAnalysis;

    @ApiModelProperty(value = "飞行评分（1-10分）", example = "8")
    private Integer flightScore;

    @ApiModelProperty(value = "是否违规（0-否，1-是）", example = "false")
    private Boolean violation;

    @ApiModelProperty(value = "违规原因", example = "超出飞行高度限制")
    private String violationReason;

    @ApiModelProperty(value = "备注", example = "正常飞行记录")
    private String remark;

    @ApiModelProperty(value = "创建时间", example = "2023-10-01T10:00:00")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间", example = "2023-10-01T11:02:00")
    private LocalDateTime updateTime;
}
package com.skytrust.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 飞行记录实体类
 *
 * @author SkyTrust Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("flight_record")
public class FlightRecord extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 飞行记录编号（唯一）
     */
    @TableField(value = "flight_no")
    private String flightNo;

    /**
     * 订单ID（关联租赁订单表）
     */
    @TableField(value = "order_id")
    private Long orderId;

    /**
     * 设备ID（关联设备表）
     */
    @TableField(value = "device_id")
    private Long deviceId;

    /**
     * 用户ID（关联用户表）
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 飞行开始时间
     */
    @TableField(value = "start_time")
    private LocalDateTime startTime;

    /**
     * 飞行结束时间
     */
    @TableField(value = "end_time")
    private LocalDateTime endTime;

    /**
     * 实际开始时间
     */
    @TableField(value = "actual_start_time")
    private LocalDateTime actualStartTime;

    /**
     * 实际结束时间
     */
    @TableField(value = "actual_end_time")
    private LocalDateTime actualEndTime;

    /**
     * 飞行时长（分钟）
     */
    @TableField(value = "duration")
    private Integer duration;

    /**
     * 飞行距离（米）
     */
    @TableField(value = "distance")
    private BigDecimal distance;

    /**
     * 最大高度（米）
     */
    @TableField(value = "max_altitude")
    private BigDecimal maxAltitude;

    /**
     * 最大速度（米/秒）
     */
    @TableField(value = "max_speed")
    private BigDecimal maxSpeed;

    /**
     * 平均速度（米/秒）
     */
    @TableField(value = "avg_speed")
    private BigDecimal avgSpeed;

    /**
     * 起飞地点纬度
     */
    @TableField(value = "takeoff_latitude")
    private BigDecimal takeoffLatitude;

    /**
     * 起飞地点经度
     */
    @TableField(value = "takeoff_longitude")
    private BigDecimal takeoffLongitude;

    /**
     * 降落地点纬度
     */
    @TableField(value = "landing_latitude")
    private BigDecimal landingLatitude;

    /**
     * 降落地点经度
     */
    @TableField(value = "landing_longitude")
    private BigDecimal landingLongitude;

    /**
     * 飞行状态（0-准备中，1-飞行中，2-已完成，3-已取消，4-异常终止）
     */
    @TableField(value = "flight_status")
    private Integer flightStatus;

    /**
     * 天气状况JSON
     */
    @TableField(value = "weather_info")
    private String weatherInfo;

    /**
     * 飞行路径数据（JSON格式的轨迹点数组）
     */
    @TableField(value = "flight_path")
    private String flightPath;

    /**
     * 飞行参数JSON（如风速、温度等）
     */
    @TableField(value = "flight_params")
    private String flightParams;

    /**
     * 异常事件记录JSON
     */
    @TableField(value = "incidents")
    private String incidents;

    /**
     * AI分析结果JSON
     */
    @TableField(value = "ai_analysis")
    private String aiAnalysis;

    /**
     * 飞行评分（1-10分）
     */
    @TableField(value = "flight_score")
    private Integer flightScore;

    /**
     * 是否违规（0-否，1-是）
     */
    @TableField(value = "is_violation")
    private Boolean violation;

    /**
     * 违规原因
     */
    @TableField(value = "violation_reason")
    private String violationReason;

    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;
}
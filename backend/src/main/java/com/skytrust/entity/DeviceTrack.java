package com.skytrust.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 设备位置轨迹实体类
 *
 * @author SkyTrust Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("device_track")
public class DeviceTrack extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 设备ID（关联设备表）
     */
    @TableField(value = "device_id")
    private Long deviceId;

    /**
     * 订单ID（关联租赁订单表，可为空）
     */
    @TableField(value = "order_id")
    private Long orderId;

    /**
     * 纬度
     */
    @TableField(value = "latitude")
    private BigDecimal latitude;

    /**
     * 经度
     */
    @TableField(value = "longitude")
    private BigDecimal longitude;

    /**
     * 高度（米）
     */
    @TableField(value = "altitude")
    private BigDecimal altitude;

    /**
     * 速度（米/秒）
     */
    @TableField(value = "speed")
    private BigDecimal speed;

    /**
     * 方向角度（0-360度）
     */
    @TableField(value = "heading")
    private BigDecimal heading;

    /**
     * 电池电量（百分比）
     */
    @TableField(value = "battery_level")
    private Integer batteryLevel;

    /**
     * 信号强度（0-100）
     */
    @TableField(value = "signal_strength")
    private Integer signalStrength;

    /**
     * 卫星数量
     */
    @TableField(value = "satellite_count")
    private Integer satelliteCount;

    /**
     * 定位精度（米）
     */
    @TableField(value = "accuracy")
    private BigDecimal accuracy;

    /**
     * 位置来源（gps-卫星定位，network-网络定位，fused-融合定位）
     */
    @TableField(value = "source")
    private String source;

    /**
     * 记录时间
     */
    @TableField(value = "record_time")
    private LocalDateTime recordTime;

    /**
     * 是否为异常位置（0-正常，1-异常）
     */
    @TableField(value = "is_abnormal")
    private Boolean abnormal;

    /**
     * 异常原因
     */
    @TableField(value = "abnormal_reason")
    private String abnormalReason;

    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;

    // Getter and Setter methods
    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public BigDecimal getAltitude() {
        return altitude;
    }

    public void setAltitude(BigDecimal altitude) {
        this.altitude = altitude;
    }

    public BigDecimal getSpeed() {
        return speed;
    }

    public void setSpeed(BigDecimal speed) {
        this.speed = speed;
    }

    public BigDecimal getHeading() {
        return heading;
    }

    public void setHeading(BigDecimal heading) {
        this.heading = heading;
    }

    public Integer getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(Integer batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public Integer getSignalStrength() {
        return signalStrength;
    }

    public void setSignalStrength(Integer signalStrength) {
        this.signalStrength = signalStrength;
    }

    public Integer getSatelliteCount() {
        return satelliteCount;
    }

    public void setSatelliteCount(Integer satelliteCount) {
        this.satelliteCount = satelliteCount;
    }

    public BigDecimal getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(BigDecimal accuracy) {
        this.accuracy = accuracy;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public LocalDateTime getRecordTime() {
        return recordTime;
    }

    public void setRecordTime(LocalDateTime recordTime) {
        this.recordTime = recordTime;
    }

    public Boolean getAbnormal() {
        return abnormal;
    }

    public void setAbnormal(Boolean abnormal) {
        this.abnormal = abnormal;
    }

    public String getAbnormalReason() {
        return abnormalReason;
    }

    public void setAbnormalReason(String abnormalReason) {
        this.abnormalReason = abnormalReason;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
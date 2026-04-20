package com.skytrust.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 设备实体类（无人机）
 *
 * @author SkyTrust Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("device")
public class Device extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 设备名称
     */
    @TableField(value = "device_name")
    private String deviceName;

    /**
     * 设备型号
     */
    @TableField(value = "model")
    private String model;

    /**
     * 设备序列号（唯一）
     */
    @TableField(value = "serial_number")
    private String serialNumber;

    /**
     * 设备状态（0-离线，1-在线，2-飞行中，3-维修中，4-已报废）
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 当前纬度
     */
    @TableField(value = "latitude")
    private BigDecimal latitude;

    /**
     * 当前经度
     */
    @TableField(value = "longitude")
    private BigDecimal longitude;

    /**
     * 当前高度（米）
     */
    @TableField(value = "altitude")
    private BigDecimal altitude;

    /**
     * 电池电量（百分比）
     */
    @TableField(value = "battery_level")
    private Integer batteryLevel;

    /**
     * 飞行速度（米/秒）
     */
    @TableField(value = "speed")
    private BigDecimal speed;

    /**
     * 飞行总时长（小时）
     */
    @TableField(value = "total_flight_hours")
    private BigDecimal totalFlightHours;

    /**
     * 设备所有者ID（关联用户表）
     */
    @TableField(value = "owner_id")
    private Long ownerId;

    /**
     * 租赁价格（元/小时）
     */
    @TableField(value = "rental_price")
    private BigDecimal rentalPrice;

    /**
     * 保险费用（元/次）
     */
    @TableField(value = "insurance_fee")
    private BigDecimal insuranceFee;

    /**
     * 设备描述
     */
    @TableField(value = "description")
    private String description;

    /**
     * 设备图片URL（多个用逗号分隔）
     */
    @TableField(value = "images")
    private String images;

    /**
     * 设备规格JSON
     */
    @TableField(value = "specifications")
    private String specifications;

    /**
     * 最后上线时间
     */
    @TableField(value = "last_online_time")
    private LocalDateTime lastOnlineTime;

    /**
     * 最后维护时间
     */
    @TableField(value = "last_maintenance_time")
    private LocalDateTime lastMaintenanceTime;

    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;

    // Getter and Setter methods
    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
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

    public Integer getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(Integer batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public BigDecimal getSpeed() {
        return speed;
    }

    public void setSpeed(BigDecimal speed) {
        this.speed = speed;
    }

    public BigDecimal getTotalFlightHours() {
        return totalFlightHours;
    }

    public void setTotalFlightHours(BigDecimal totalFlightHours) {
        this.totalFlightHours = totalFlightHours;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public BigDecimal getRentalPrice() {
        return rentalPrice;
    }

    public void setRentalPrice(BigDecimal rentalPrice) {
        this.rentalPrice = rentalPrice;
    }

    public BigDecimal getInsuranceFee() {
        return insuranceFee;
    }

    public void setInsuranceFee(BigDecimal insuranceFee) {
        this.insuranceFee = insuranceFee;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public String getSpecifications() {
        return specifications;
    }

    public void setSpecifications(String specifications) {
        this.specifications = specifications;
    }

    public LocalDateTime getLastOnlineTime() {
        return lastOnlineTime;
    }

    public void setLastOnlineTime(LocalDateTime lastOnlineTime) {
        this.lastOnlineTime = lastOnlineTime;
    }

    public LocalDateTime getLastMaintenanceTime() {
        return lastMaintenanceTime;
    }

    public void setLastMaintenanceTime(LocalDateTime lastMaintenanceTime) {
        this.lastMaintenanceTime = lastMaintenanceTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
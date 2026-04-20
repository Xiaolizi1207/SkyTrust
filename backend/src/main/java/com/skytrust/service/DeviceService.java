package com.skytrust.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.skytrust.entity.Device;
import com.skytrust.common.Result;

import java.math.BigDecimal;
import java.util.List;

/**
 * 设备服务接口
 *
 * @author SkyTrust Team
 */
public interface DeviceService extends IService<Device> {

    /**
     * 添加新设备
     *
     * @param device 设备信息
     * @return 添加结果
     */
    Result<Device> addDevice(Device device);

    /**
     * 更新设备状态
     *
     * @param deviceId 设备ID
     * @param status   状态（0-离线，1-在线，2-飞行中，3-维修中，4-已报废）
     * @return 是否成功
     */
    boolean updateDeviceStatus(Long deviceId, Integer status);

    /**
     * 更新设备位置信息
     *
     * @param deviceId  设备ID
     * @param latitude  纬度
     * @param longitude 经度
     * @param altitude  高度
     * @return 是否成功
     */
    boolean updateDeviceLocation(Long deviceId, BigDecimal latitude, BigDecimal longitude, BigDecimal altitude);

    /**
     * 更新设备电量
     *
     * @param deviceId     设备ID
     * @param batteryLevel 电池电量（百分比）
     * @return 是否成功
     */
    boolean updateDeviceBattery(Long deviceId, Integer batteryLevel);

    /**
     * 更新设备飞行数据
     *
     * @param deviceId           设备ID
     * @param speed              飞行速度（米/秒）
     * @param totalFlightHours   飞行总时长（小时）
     * @return 是否成功
     */
    boolean updateDeviceFlightData(Long deviceId, BigDecimal speed, BigDecimal totalFlightHours);

    /**
     * 更新设备最后上线时间
     *
     * @param deviceId 设备ID
     * @return 是否成功
     */
    boolean updateDeviceLastOnlineTime(Long deviceId);

    /**
     * 更新设备最后维护时间
     *
     * @param deviceId 设备ID
     * @return 是否成功
     */
    boolean updateDeviceLastMaintenanceTime(Long deviceId);

    /**
     * 根据序列号查询设备
     *
     * @param serialNumber 序列号
     * @return 设备信息
     */
    Device getDeviceBySerialNumber(String serialNumber);

    /**
     * 根据所有者查询设备
     *
     * @param ownerId 所有者ID
     * @return 设备列表
     */
    List<Device> getDevicesByOwner(Long ownerId);

    /**
     * 根据状态查询设备
     *
     * @param status 状态
     * @return 设备列表
     */
    List<Device> getDevicesByStatus(Integer status);

    /**
     * 查询可用设备（在线且未飞行）
     *
     * @return 可用设备列表
     */
    List<Device> getAvailableDevices();

    /**
     * 查询附近设备
     *
     * @param latitude   纬度
     * @param longitude  经度
     * @param radius     半径（公里）
     * @return 附近设备列表
     */
    List<Device> getNearbyDevices(BigDecimal latitude, BigDecimal longitude, BigDecimal radius);

    /**
     * 查询设备分页列表
     *
     * @param page          当前页
     * @param size          每页大小
     * @param deviceName    设备名称（模糊查询）
     * @param model         设备型号（模糊查询）
     * @param serialNumber  序列号（模糊查询）
     * @param status        状态
     * @param minPrice      最低租赁价格
     * @param maxPrice      最高租赁价格
     * @param minBattery    最低电量
     * @param orderBy       排序字段
     * @return 分页结果
     */
    IPage<Device> pageDevices(Integer page, Integer size,
                             String deviceName, String model, String serialNumber,
                             Integer status, BigDecimal minPrice, BigDecimal maxPrice,
                             Integer minBattery, String orderBy);

    /**
     * 检查设备是否可用（在线、电量充足、未飞行）
     *
     * @param deviceId 设备ID
     * @return 是否可用
     */
    boolean isDeviceAvailable(Long deviceId);

    /**
     * 检查序列号是否已存在
     *
     * @param serialNumber 序列号
     * @return 是否存在
     */
    boolean isSerialNumberExists(String serialNumber);

    /**
     * 批量更新设备状态
     *
     * @param deviceIds 设备ID列表
     * @param status    状态
     * @return 是否成功
     */
    boolean batchUpdateDeviceStatus(List<Long> deviceIds, Integer status);

    /**
     * 批量更新设备所有者
     *
     * @param deviceIds 设备ID列表
     * @param ownerId   所有者ID
     * @return 是否成功
     */
    boolean batchUpdateDeviceOwner(List<Long> deviceIds, Long ownerId);

    /**
     * 设备维护
     *
     * @param deviceId 设备ID
     * @param remark   维护备注
     * @return 是否成功
     */
    boolean maintainDevice(Long deviceId, String remark);

    /**
     * 设备报废
     *
     * @param deviceId 设备ID
     * @param remark   报废原因
     * @return 是否成功
     */
    boolean scrapDevice(Long deviceId, String remark);

    /**
     * 统计设备数量
     *
     * @return 各状态设备数量统计
     */
    DeviceStatistics getDeviceStatistics();

    /**
     * 设备统计信息
     */
    class DeviceStatistics {
        private Long totalDevices;      // 总设备数
        private Long onlineDevices;     // 在线设备数
        private Long flyingDevices;     // 飞行中设备数
        private Long maintenanceDevices;// 维修中设备数
        private Long scrappedDevices;   // 已报废设备数

        // 构造函数
        public DeviceStatistics() {
        }

        public DeviceStatistics(Long totalDevices, Long onlineDevices, Long flyingDevices,
                               Long maintenanceDevices, Long scrappedDevices) {
            this.totalDevices = totalDevices;
            this.onlineDevices = onlineDevices;
            this.flyingDevices = flyingDevices;
            this.maintenanceDevices = maintenanceDevices;
            this.scrappedDevices = scrappedDevices;
        }

        // Getter and Setter methods
        public Long getTotalDevices() {
            return totalDevices;
        }

        public void setTotalDevices(Long totalDevices) {
            this.totalDevices = totalDevices;
        }

        public Long getOnlineDevices() {
            return onlineDevices;
        }

        public void setOnlineDevices(Long onlineDevices) {
            this.onlineDevices = onlineDevices;
        }

        public Long getFlyingDevices() {
            return flyingDevices;
        }

        public void setFlyingDevices(Long flyingDevices) {
            this.flyingDevices = flyingDevices;
        }

        public Long getMaintenanceDevices() {
            return maintenanceDevices;
        }

        public void setMaintenanceDevices(Long maintenanceDevices) {
            this.maintenanceDevices = maintenanceDevices;
        }

        public Long getScrappedDevices() {
            return scrappedDevices;
        }

        public void setScrappedDevices(Long scrappedDevices) {
            this.scrappedDevices = scrappedDevices;
        }
    }
}
package com.skytrust.service;

import com.skytrust.entity.DeviceTrack;

/**
 * 设备轨迹服务接口
 *
 * @author SkyTrust Team
 */
public interface DeviceTrackService extends IService<DeviceTrack> {

    /**
     * 根据设备ID查询轨迹记录
     *
     * @param deviceId 设备ID
     * @return 轨迹记录列表
     */
    java.util.List<DeviceTrack> getByDeviceId(Long deviceId);

    /**
     * 根据设备ID和时间范围查询轨迹记录
     *
     * @param deviceId 设备ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 轨迹记录列表
     */
    java.util.List<DeviceTrack> getByDeviceIdAndTimeRange(Long deviceId, java.time.LocalDateTime startTime, java.time.LocalDateTime endTime);

    /**
     * 获取设备最新位置
     *
     * @param deviceId 设备ID
     * @return 最新轨迹记录
     */
    DeviceTrack getLatestByDeviceId(Long deviceId);
}
package com.skytrust.service;

import com.skytrust.entity.FlightRecord;

/**
 * 飞行记录服务接口
 *
 * @author SkyTrust Team
 */
public interface FlightRecordService extends IService<FlightRecord> {

    /**
     * 根据设备ID查询飞行记录
     *
     * @param deviceId 设备ID
     * @return 飞行记录列表
     */
    java.util.List<FlightRecord> getByDeviceId(Long deviceId);

    /**
     * 根据订单ID查询飞行记录
     *
     * @param orderId 订单ID
     * @return 飞行记录
     */
    FlightRecord getByOrderId(Long orderId);

    /**
     * 根据用户ID查询飞行记录
     *
     * @param userId 用户ID
     * @return 飞行记录列表
     */
    java.util.List<FlightRecord> getByUserId(Long userId);
}
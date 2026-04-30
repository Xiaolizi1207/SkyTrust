package com.skytrust.service;

import com.skytrust.entity.RentalOrder;

/**
 * 租赁订单服务接口
 *
 * @author SkyTrust Team
 */
public interface RentalOrderService extends IService<RentalOrder> {

    /**
     * 根据订单号查询订单
     *
     * @param orderNo 订单号
     * @return 订单信息
     */
    RentalOrder getByOrderNo(String orderNo);

    /**
     * 根据用户ID查询订单
     *
     * @param userId 用户ID
     * @return 订单列表
     */
    java.util.List<RentalOrder> getByUserId(Long userId);

    /**
     * 根据设备ID查询订单
     *
     * @param deviceId 设备ID
     * @return 订单列表
     */
    java.util.List<RentalOrder> getByDeviceId(Long deviceId);

    /**
     * 根据状态查询订单
     *
     * @param status 订单状态
     * @return 订单列表
     */
    java.util.List<RentalOrder> getByStatus(Integer status);

    /**
     * 创建租赁订单
     *
     * @param order 订单信息
     * @return 创建结果
     */
    com.skytrust.common.Result<RentalOrder> createOrder(RentalOrder order);

    /**
     * 取消订单
     *
     * @param orderId 订单ID
     * @param reason  取消原因
     * @return 是否成功
     */
    boolean cancelOrder(Long orderId, String reason);

    /**
     * 完成订单
     *
     * @param orderId 订单ID
     * @return 是否成功
     */
    boolean completeOrder(Long orderId);

    /**
     * 续租订单
     *
     * @param orderId 订单ID
     * @param days    续租天数
     * @return 续租结果
     */
    com.skytrust.common.Result<RentalOrder> renewOrder(Long orderId, Integer days);

    /**
     * 生成订单号
     *
     * @return 订单号
     */
    String generateOrderNo();
}
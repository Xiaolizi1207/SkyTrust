package com.skytrust.service;

import com.skytrust.entity.Insurance;

/**
 * 保险服务接口
 *
 * @author SkyTrust Team
 */
public interface InsuranceService extends IService<Insurance> {

    /**
     * 根据订单ID查询保险记录
     *
     * @param orderId 订单ID
     * @return 保险记录
     */
    Insurance getByOrderId(Long orderId);

    /**
     * 根据保单号查询保险记录
     *
     * @param policyNo 保单号
     * @return 保险记录
     */
    Insurance getByPolicyNo(String policyNo);
}
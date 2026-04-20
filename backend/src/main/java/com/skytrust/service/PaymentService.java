package com.skytrust.service;

import com.skytrust.entity.Payment;

/**
 * 支付服务接口
 *
 * @author SkyTrust Team
 */
public interface PaymentService extends IService<Payment> {

    /**
     * 根据订单ID查询支付记录
     *
     * @param orderId 订单ID
     * @return 支付记录
     */
    Payment getByOrderId(Long orderId);

    /**
     * 根据交易号查询支付记录
     *
     * @param transactionNo 交易号
     * @return 支付记录
     */
    Payment getByTransactionNo(String transactionNo);

    /**
     * 创建支付记录
     *
     * @param payment 支付信息
     * @return 创建结果
     */
    com.skytrust.common.Result<Payment> createPayment(Payment payment);

    /**
     * 更新支付状态
     *
     * @param paymentId 支付记录ID
     * @param status    支付状态
     * @param remark    备注
     * @return 是否成功
     */
    boolean updatePaymentStatus(Long paymentId, Integer status, String remark);

    /**
     * 生成交易号
     *
     * @return 交易号
     */
    String generateTransactionNo();
}
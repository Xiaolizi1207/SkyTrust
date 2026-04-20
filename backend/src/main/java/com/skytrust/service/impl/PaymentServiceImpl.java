package com.skytrust.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.skytrust.common.Result;
import com.skytrust.common.ResultCode;
import com.skytrust.common.utils.DateUtil;
import com.skytrust.common.utils.StringUtil;
import com.skytrust.entity.Payment;
import com.skytrust.exception.BusinessException;
import com.skytrust.mapper.PaymentMapper;
import com.skytrust.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 支付服务实现类
 *
 * @author SkyTrust Team
 */
@Slf4j
@Service
public class PaymentServiceImpl extends BaseService<PaymentMapper, Payment> implements PaymentService {

    @Override
    public Payment getByOrderId(Long orderId) {
        if (orderId == null) {
            return null;
        }
        LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Payment::getOrderId, orderId)
                .orderByDesc(Payment::getCreateTime);
        return getOne(wrapper);
    }

    @Override
    public Payment getByTransactionNo(String transactionNo) {
        if (StringUtil.isEmpty(transactionNo)) {
            return null;
        }
        LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Payment::getTransactionNo, transactionNo);
        return getOne(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Payment> createPayment(Payment payment) {
        log.info("创建支付记录");

        // 验证必填字段
        if (payment.getOrderId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "订单ID不能为空");
        }
        if (payment.getAmount() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "支付金额不能为空");
        }
        if (StringUtil.isEmpty(payment.getPaymentMethod())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "支付方式不能为空");
        }

        // 生成交易号
        if (StringUtil.isEmpty(payment.getTransactionNo())) {
            payment.setTransactionNo(generateTransactionNo());
        }

        // 设置默认值
        payment.setId(null);
        if (payment.getStatus() == null) {
            payment.setStatus(0); // 待支付
        }
        if (payment.getPayTime() == null && payment.getStatus() == 1) {
            payment.setPayTime(LocalDateTime.now()); // 已支付则设置支付时间
        }

        // 保存支付记录
        boolean success = super.save(payment);
        if (!success) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "支付记录创建失败");
        }

        log.info("支付记录创建成功: {}, ID: {}", payment.getTransactionNo(), payment.getId());
        return Result.success(payment, "支付记录创建成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePaymentStatus(Long paymentId, Integer status, String remark) {
        log.info("更新支付状态: {}, status: {}", paymentId, status);

        if (paymentId == null || status == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "参数错误");
        }

        Payment payment = getById(paymentId);
        if (payment == null) {
            throw new BusinessException(ResultCode.PAYMENT_NOT_EXIST, "支付记录不存在");
        }

        payment.setStatus(status);
        if (StringUtil.isNotEmpty(remark)) {
            payment.setRemark(remark);
        }

        // 如果状态变为已支付，设置支付时间
        if (status == 1 && payment.getPayTime() == null) {
            payment.setPayTime(LocalDateTime.now());
        }

        return updateById(payment);
    }

    @Override
    public String generateTransactionNo() {
        return DateUtil.generateOrderNo("PAY");
    }

    @Override
    public String validate(Payment entity) {
        String parentValidation = super.validate(entity);
        if (parentValidation != null) {
            return parentValidation;
        }

        // 验证支付逻辑
        if (entity.getAmount() != null) {
            if (entity.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                return "支付金额必须大于0";
            }
        }

        if (entity.getStatus() != null) {
            if (entity.getStatus() < 0 || entity.getStatus() > 2) {
                return "支付状态无效 (0-待支付,1-已支付,2-支付失败)";
            }
        }

        if (StringUtil.isNotEmpty(entity.getPaymentMethod())) {
            String method = entity.getPaymentMethod();
            if (!"alipay".equals(method) && !"wechat".equals(method) && !"bank".equals(method) && !"blockchain".equals(method)) {
                return "支付方式无效 (alipay/wechat/bank/blockchain)";
            }
        }

        return null;
    }
}
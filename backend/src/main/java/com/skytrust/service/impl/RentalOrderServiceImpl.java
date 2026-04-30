package com.skytrust.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.skytrust.common.Result;
import com.skytrust.common.ResultCode;
import com.skytrust.common.utils.DateUtil;
import com.skytrust.common.utils.StringUtil;
import com.skytrust.entity.RentalOrder;
import com.skytrust.exception.BusinessException;
import com.skytrust.mapper.RentalOrderMapper;
import com.skytrust.service.RentalOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 租赁订单服务实现类
 *
 * @author SkyTrust Team
 */
@Slf4j
@Service
public class RentalOrderServiceImpl extends BaseService<RentalOrderMapper, RentalOrder> implements RentalOrderService {

    @Override
    public RentalOrder getByOrderNo(String orderNo) {
        if (StringUtil.isEmpty(orderNo)) {
            return null;
        }
        LambdaQueryWrapper<RentalOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RentalOrder::getOrderNo, orderNo);
        return getOne(wrapper);
    }

    @Override
    public List<RentalOrder> getByUserId(Long userId) {
        if (userId == null) {
            return listAll();
        }
        LambdaQueryWrapper<RentalOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RentalOrder::getUserId, userId)
                .orderByDesc(RentalOrder::getCreateTime);
        return list(wrapper);
    }

    @Override
    public List<RentalOrder> getByDeviceId(Long deviceId) {
        if (deviceId == null) {
            return listAll();
        }
        LambdaQueryWrapper<RentalOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RentalOrder::getDeviceId, deviceId)
                .orderByDesc(RentalOrder::getCreateTime);
        return list(wrapper);
    }

    @Override
    public List<RentalOrder> getByStatus(Integer status) {
        if (status == null) {
            return listAll();
        }
        LambdaQueryWrapper<RentalOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RentalOrder::getStatus, status)
                .orderByDesc(RentalOrder::getCreateTime);
        return list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<RentalOrder> createOrder(RentalOrder order) {
        log.info("创建租赁订单");

        // 验证必填字段
        if (order.getUserId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "用户ID不能为空");
        }
        if (order.getDeviceId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "设备ID不能为空");
        }
        if (order.getStartTime() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "租赁开始时间不能为空");
        }
        if (order.getEndTime() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "租赁结束时间不能为空");
        }
        if (order.getTotalAmount() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "订单总金额不能为空");
        }

        // 生成订单号
        if (StringUtil.isEmpty(order.getOrderNo())) {
            order.setOrderNo(generateOrderNo());
        }

        // 设置默认值
        order.setId(null);
        if (order.getStatus() == null) {
            order.setStatus(0); // 待支付
        }
        if (order.getPaymentStatus() == null) {
            order.setPaymentStatus(0); // 未支付
        }

        // 保存订单
        boolean success = super.save(order);
        if (!success) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "订单创建失败");
        }

        log.info("租赁订单创建成功: {}, ID: {}", order.getOrderNo(), order.getId());
        return Result.success(order, "订单创建成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelOrder(Long orderId, String reason) {
        log.info("取消订单: {}, reason: {}", orderId, reason);

        if (orderId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "订单ID不能为空");
        }

        RentalOrder order = getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST, "订单不存在");
        }

        // 检查订单状态是否可以取消
        Integer status = order.getStatus();
        if (status != null && status >= 2) { // 2-已确认，3-进行中，4-已完成
            throw new BusinessException(ResultCode.ORDER_CANNOT_CANCEL, "订单已确认，无法取消");
        }

        // 更新订单状态为已取消
        order.setStatus(5); // 5-已取消
        order.setCancelReason(reason);
        order.setCancelTime(LocalDateTime.now());

        return updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean completeOrder(Long orderId) {
        log.info("完成订单: {}", orderId);

        if (orderId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "订单ID不能为空");
        }

        RentalOrder order = getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST, "订单不存在");
        }

        // 检查订单状态是否可以完成
        Integer status = order.getStatus();
        if (status != 3) { // 3-进行中
            throw new BusinessException(ResultCode.ORDER_CANNOT_COMPLETE, "订单未在进行中，无法完成");
        }

        // 更新订单状态为已完成
        order.setStatus(4); // 4-已完成
        order.setActualEndTime(LocalDateTime.now());

        return updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<RentalOrder> renewOrder(Long orderId, Integer days) {
        log.info("续租订单: orderId={}, days={}", orderId, days);

        if (orderId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "订单ID不能为空");
        }
        if (days == null || days <= 0 || days > 30) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "续租天数必须在1-30之间");
        }

        RentalOrder order = getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST, "订单不存在");
        }

        // 只有进行中(status=3)的订单可以续租
        Integer status = order.getStatus();
        if (status == null || status != 3) {
            throw new BusinessException(ResultCode.ORDER_CANNOT_CANCEL, "仅进行中的订单可以续租");
        }

        // 计算日均费用作为续租单价
        java.math.BigDecimal totalFee = order.getTotalFee() != null ? order.getTotalFee() : java.math.BigDecimal.ZERO;
        java.math.BigDecimal dailyRate;
        if (order.getDuration() != null && order.getDuration().compareTo(java.math.BigDecimal.ZERO) > 0) {
            dailyRate = totalFee.divide(order.getDuration(), 2, java.math.RoundingMode.HALF_UP);
        } else {
            // fallback: use rentalFee + insuranceFee as daily rate
            dailyRate = (order.getRentalFee() != null ? order.getRentalFee() : java.math.BigDecimal.ZERO)
                    .add(order.getInsuranceFee() != null ? order.getInsuranceFee() : java.math.BigDecimal.ZERO);
        }
        java.math.BigDecimal additionalFee = dailyRate.multiply(java.math.BigDecimal.valueOf(days));

        // 更新订单
        order.setEndTime(order.getEndTime().plusDays(days));
        order.setDuration(order.getDuration() != null
                ? order.getDuration().add(java.math.BigDecimal.valueOf(days))
                : java.math.BigDecimal.valueOf(days));
        order.setTotalFee(totalFee.add(additionalFee));
        if (order.getActualPayment() != null) {
            order.setActualPayment(order.getActualPayment().add(additionalFee));
        }

        boolean updated = updateById(order);
        if (!updated) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "续租失败，请重试");
        }

        log.info("订单续租成功: orderId={}, 新增{}天, 新结束时间={}", orderId, days, order.getEndTime());
        return Result.success(order, "续租成功");
    }

    @Override
    public String generateOrderNo() {
        return DateUtil.generateOrderNo("RO");
    }

    @Override
    public String validate(RentalOrder entity) {
        String parentValidation = super.validate(entity);
        if (parentValidation != null) {
            return parentValidation;
        }

        // 验证订单逻辑
        if (entity.getStartTime() != null && entity.getEndTime() != null) {
            if (entity.getEndTime().isBefore(entity.getStartTime())) {
                return "租赁结束时间不能早于开始时间";
            }
        }

        if (entity.getTotalAmount() != null) {
            if (entity.getTotalAmount().compareTo(java.math.BigDecimal.ZERO) < 0) {
                return "订单总金额不能为负数";
            }
        }

        if (entity.getDepositAmount() != null) {
            if (entity.getDepositAmount().compareTo(java.math.BigDecimal.ZERO) < 0) {
                return "押金金额不能为负数";
            }
        }

        if (entity.getStatus() != null) {
            if (entity.getStatus() < 0 || entity.getStatus() > 5) {
                return "订单状态无效 (0-待支付,1-已支付,2-已确认,3-进行中,4-已完成,5-已取消)";
            }
        }

        if (entity.getPaymentStatus() != null) {
            if (entity.getPaymentStatus() < 0 || entity.getPaymentStatus() > 2) {
                return "支付状态无效 (0-未支付,1-已支付,2-支付失败)";
            }
        }

        return null;
    }
}
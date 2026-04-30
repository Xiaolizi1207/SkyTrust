package com.skytrust.controller;

import com.skytrust.common.Result;
import com.skytrust.common.ResultCode;
import com.skytrust.dto.PaymentDTO;
import com.skytrust.entity.Payment;
import com.skytrust.service.PaymentService;
import com.skytrust.vo.PaymentVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 支付控制器
 *
 * @author SkyTrust Team
 */
@Tag(name = "支付管理", description = "支付记录管理接口")
@Validated
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * 创建支付记录
     */
    @Operation(summary = "创建支付记录")
    @PostMapping
    public Result<PaymentVO> createPayment(@Valid @RequestBody PaymentDTO paymentDTO) {
        // 转换DTO为实体
        Payment payment = new Payment();
        BeanUtils.copyProperties(paymentDTO, payment);

        // 调用Service创建支付记录
        var result = paymentService.createPayment(payment);
        if (!result.isSuccess()) {
            return Result.error(result.getCode(), result.getMessage());
        }

        PaymentVO paymentVO = convertToVO(result.getData());
        return Result.success(paymentVO, "支付记录创建成功");
    }

    /**
     * 更新支付记录信息
     */
    @Operation(summary = "更新支付记录信息")
    @PutMapping("/{id}")
    public Result<PaymentVO> updatePayment(
            @Parameter(description = "支付记录ID", required = true) @PathVariable Long id,
            @Valid @RequestBody PaymentDTO paymentDTO) {
        Payment payment = paymentService.getById(id);
        if (payment == null) {
            return Result.error(ResultCode.DATA_NOT_EXIST.getCode(), "支付记录不存在");
        }

        // 更新字段（排除不能修改的字段）
        BeanUtils.copyProperties(paymentDTO, payment, "id", "transactionNo", "createTime", "updateTime");

        boolean updated = paymentService.updateById(payment);
        if (!updated) {
            return Result.error("支付记录更新失败");
        }

        PaymentVO paymentVO = convertToVO(payment);
        return Result.success(paymentVO, "支付记录更新成功");
    }

    /**
     * 获取支付记录详情
     */
    @Operation(summary = "获取支付记录详情")
    @GetMapping("/{id}")
    public Result<PaymentVO> getPaymentById(@Parameter(description = "支付记录ID", required = true) @PathVariable Long id) {
        Payment payment = paymentService.getById(id);
        if (payment == null) {
            return Result.error(ResultCode.DATA_NOT_EXIST.getCode(), "支付记录不存在");
        }
        return Result.success(convertToVO(payment));
    }

    /**
     * 删除支付记录（逻辑删除）
     */
    @Operation(summary = "删除支付记录")
    @DeleteMapping("/{id}")
    public Result<Void> deletePayment(@Parameter(description = "支付记录ID", required = true) @PathVariable Long id) {
        boolean deleted = paymentService.logicRemoveById(id);
        if (!deleted) {
            return Result.error("支付记录删除失败");
        }
        return Result.success("支付记录删除成功");
    }

    /**
     * 分页查询支付记录列表
     */
    @Operation(summary = "分页查询支付记录列表")
    @GetMapping
    public Result<List<PaymentVO>> getPaymentList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "订单ID") @RequestParam(required = false) Long orderId,
            @Parameter(description = "支付状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "交易号") @RequestParam(required = false) String transactionNo) {

        // 简化处理：使用Service的list方法
        List<Payment> payments = paymentService.list();

        // 应用过滤条件
        List<Payment> filteredPayments = payments.stream()
                .filter(payment -> userId == null || payment.getUserId().equals(userId))
                .filter(payment -> orderId == null || payment.getOrderId().equals(orderId))
                .filter(payment -> status == null || payment.getStatus().equals(status))
                .filter(payment -> transactionNo == null || payment.getTransactionNo().contains(transactionNo))
                .skip((page - 1) * (long) size)
                .limit(size)
                .collect(Collectors.toList());

        List<PaymentVO> paymentVOs = filteredPayments.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return Result.success(paymentVOs);
    }

    /**
     * 根据订单ID查询支付记录
     */
    @Operation(summary = "根据订单ID查询支付记录")
    @GetMapping("/order/{orderId}")
    public Result<PaymentVO> getPaymentByOrderId(@Parameter(description = "订单ID", required = true) @PathVariable Long orderId) {
        Payment payment = paymentService.getByOrderId(orderId);
        if (payment == null) {
            return Result.error(ResultCode.DATA_NOT_EXIST.getCode(), "支付记录不存在");
        }
        return Result.success(convertToVO(payment));
    }

    /**
     * 根据交易号查询支付记录
     */
    @Operation(summary = "根据交易号查询支付记录")
    @GetMapping("/transaction/{transactionNo}")
    public Result<PaymentVO> getPaymentByTransactionNo(@Parameter(description = "交易号", required = true) @PathVariable String transactionNo) {
        Payment payment = paymentService.getByTransactionNo(transactionNo);
        if (payment == null) {
            return Result.error(ResultCode.DATA_NOT_EXIST.getCode(), "支付记录不存在");
        }
        return Result.success(convertToVO(payment));
    }

    /**
     * 更新支付状态
     */
    @Operation(summary = "更新支付状态")
    @PostMapping("/{id}/status")
    public Result<Void> updatePaymentStatus(
            @Parameter(description = "支付记录ID", required = true) @PathVariable Long id,
            @Parameter(description = "支付状态", required = true) @RequestParam Integer status,
            @Parameter(description = "备注") @RequestParam(required = false) String remark) {

        boolean updated = paymentService.updatePaymentStatus(id, status, remark);
        if (!updated) {
            return Result.error("支付状态更新失败");
        }
        return Result.success("支付状态更新成功");
    }

    /**
     * 根据订单ID查询支付状态
     */
    @Operation(summary = "根据订单ID查询支付状态")
    @GetMapping("/status/{orderId}")
    public Result<PaymentVO> getPaymentStatusByOrderId(@Parameter(description = "订单ID", required = true) @PathVariable Long orderId) {
        Payment payment = paymentService.getByOrderId(orderId);
        if (payment == null) {
            // 不存在支付记录时返回待支付状态，不报错
            PaymentVO emptyVO = new PaymentVO();
            emptyVO.setOrderId(orderId);
            emptyVO.setStatus(0);
            return Result.success(emptyVO);
        }
        return Result.success(convertToVO(payment));
    }

    /**
     * 生成交易号
     */
    @Operation(summary = "生成交易号")
    @GetMapping("/generate-transaction-no")
    public Result<String> generateTransactionNo() {
        String transactionNo = paymentService.generateTransactionNo();
        return Result.success(transactionNo);
    }

    /**
     * 将Payment实体转换为PaymentVO
     */
    private PaymentVO convertToVO(Payment payment) {
        if (payment == null) {
            return null;
        }
        PaymentVO paymentVO = new PaymentVO();
        BeanUtils.copyProperties(payment, paymentVO);
        return paymentVO;
    }
}
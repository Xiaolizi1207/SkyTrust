package com.skytrust.controller;

import com.skytrust.common.Result;
import com.skytrust.common.ResultCode;
import com.skytrust.common.utils.StringUtil;
import com.skytrust.dto.RentalOrderDTO;
import com.skytrust.entity.Device;
import com.skytrust.entity.RentalOrder;
import com.skytrust.service.DeviceService;
import com.skytrust.service.RentalOrderService;
import com.skytrust.vo.RentalOrderVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 租赁订单控制器
 *
 * @author SkyTrust Team
 */
@Tag(name = "租赁订单管理", description = "无人机租赁订单管理接口")
@Validated
@RestController
@RequestMapping("/api/orders")
public class RentalOrderController {

    private final RentalOrderService rentalOrderService;
    private final DeviceService deviceService;

    public RentalOrderController(RentalOrderService rentalOrderService, DeviceService deviceService) {
        this.rentalOrderService = rentalOrderService;
        this.deviceService = deviceService;
    }

    /**
     * 创建租赁订单
     */
    @Operation(summary = "创建租赁订单")
    @PostMapping
    public Result<RentalOrderVO> createOrder(@Valid @RequestBody RentalOrderDTO orderDTO) {
        // 转换DTO为实体
        RentalOrder order = new RentalOrder();
        BeanUtils.copyProperties(orderDTO, order);

        // 调用Service创建订单
        var result = rentalOrderService.createOrder(order);
        if (!result.isSuccess()) {
            return Result.error(result.getCode(), result.getMessage());
        }

        RentalOrderVO orderVO = convertToVO(result.getData());
        return Result.success(orderVO, "订单创建成功");
    }

    /**
     * 更新订单信息
     */
    @Operation(summary = "更新订单信息")
    @PutMapping("/{id}")
    public Result<RentalOrderVO> updateOrder(
            @Parameter(description = "订单ID", required = true) @PathVariable Long id,
            @Valid @RequestBody RentalOrderDTO orderDTO) {
        RentalOrder order = rentalOrderService.getById(id);
        if (order == null) {
            return Result.error(ResultCode.DATA_NOT_EXIST.getCode(), "订单不存在");
        }

        // 更新字段（排除不能修改的字段）
        BeanUtils.copyProperties(orderDTO, order, "id", "orderNo", "createTime", "updateTime");

        boolean updated = rentalOrderService.updateById(order);
        if (!updated) {
            return Result.error("订单更新失败");
        }

        RentalOrderVO orderVO = convertToVO(order);
        return Result.success(orderVO, "订单更新成功");
    }

    /**
     * 获取订单详情
     */
    @Operation(summary = "获取订单详情")
    @GetMapping("/{id}")
    public Result<RentalOrderVO> getOrderById(@Parameter(description = "订单ID", required = true) @PathVariable Long id) {
        RentalOrder order = rentalOrderService.getById(id);
        if (order == null) {
            return Result.error(ResultCode.DATA_NOT_EXIST.getCode(), "订单不存在");
        }
        return Result.success(convertToVO(order));
    }

    /**
     * 删除订单（逻辑删除）
     */
    @Operation(summary = "删除订单")
    @DeleteMapping("/{id}")
    public Result<Void> deleteOrder(@Parameter(description = "订单ID", required = true) @PathVariable Long id) {
        boolean deleted = rentalOrderService.logicRemoveById(id);
        if (!deleted) {
            return Result.error("订单删除失败");
        }
        return Result.success("订单删除成功");
    }

    /**
     * 分页查询订单列表
     */
    @Operation(summary = "分页查询订单列表")
    @GetMapping
    public Result<List<RentalOrderVO>> getOrderList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "设备ID") @RequestParam(required = false) Long deviceId,
            @Parameter(description = "订单状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "订单号") @RequestParam(required = false) String orderNo) {

        // 简化处理：使用Service的分页查询（这里先简单实现）
        List<RentalOrder> orders = rentalOrderService.list();

        // 应用过滤条件
        List<RentalOrder> filteredOrders = orders.stream()
                .filter(order -> userId == null || order.getUserId().equals(userId))
                .filter(order -> deviceId == null || order.getDeviceId().equals(deviceId))
                .filter(order -> status == null || order.getOrderStatus().equals(status))
                .filter(order -> orderNo == null || order.getOrderNo().contains(orderNo))
                .skip((page - 1) * (long) size)
                .limit(size)
                .collect(Collectors.toList());

        List<RentalOrderVO> orderVOs = filteredOrders.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return Result.success(orderVOs);
    }

    /**
     * 根据用户ID查询订单
     */
    @Operation(summary = "根据用户ID查询订单")
    @GetMapping("/user/{userId}")
    public Result<List<RentalOrderVO>> getOrdersByUserId(@Parameter(description = "用户ID", required = true) @PathVariable Long userId) {
        List<RentalOrder> orders = rentalOrderService.getByUserId(userId);
        List<RentalOrderVO> orderVOs = orders.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        return Result.success(orderVOs);
    }

    /**
     * 根据设备ID查询订单
     */
    @Operation(summary = "根据设备ID查询订单")
    @GetMapping("/device/{deviceId}")
    public Result<List<RentalOrderVO>> getOrdersByDeviceId(@Parameter(description = "设备ID", required = true) @PathVariable Long deviceId) {
        List<RentalOrder> orders = rentalOrderService.getByDeviceId(deviceId);
        List<RentalOrderVO> orderVOs = orders.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        return Result.success(orderVOs);
    }

    /**
     * 根据状态查询订单
     */
    @Operation(summary = "根据状态查询订单")
    @GetMapping("/status/{status}")
    public Result<List<RentalOrderVO>> getOrdersByStatus(@Parameter(description = "订单状态", required = true) @PathVariable Integer status) {
        List<RentalOrder> orders = rentalOrderService.getByStatus(status);
        List<RentalOrderVO> orderVOs = orders.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        return Result.success(orderVOs);
    }

    /**
     * 根据订单号查询订单
     */
    @Operation(summary = "根据订单号查询订单")
    @GetMapping("/orderNo/{orderNo}")
    public Result<RentalOrderVO> getOrderByOrderNo(@Parameter(description = "订单号", required = true) @PathVariable String orderNo) {
        RentalOrder order = rentalOrderService.getByOrderNo(orderNo);
        if (order == null) {
            return Result.error(ResultCode.DATA_NOT_EXIST.getCode(), "订单不存在");
        }
        return Result.success(convertToVO(order));
    }

    /**
     * 取消订单
     */
    @Operation(summary = "取消订单")
    @PostMapping("/{id}/cancel")
    public Result<Void> cancelOrder(
            @Parameter(description = "订单ID", required = true) @PathVariable Long id,
            @Parameter(description = "取消原因", required = true) @RequestParam String reason) {

        boolean canceled = rentalOrderService.cancelOrder(id, reason);
        if (!canceled) {
            return Result.error("订单取消失败");
        }
        return Result.success("订单取消成功");
    }

    /**
     * 完成订单
     */
    @Operation(summary = "完成订单")
    @PostMapping("/{id}/complete")
    public Result<Void> completeOrder(@Parameter(description = "订单ID", required = true) @PathVariable Long id) {
        boolean completed = rentalOrderService.completeOrder(id);
        if (!completed) {
            return Result.error("订单完成失败");
        }
        return Result.success("订单完成成功");
    }

    /**
     * 续租订单
     */
    @Operation(summary = "续租订单")
    @PutMapping("/{id}/renew")
    public Result<RentalOrderVO> renewOrder(
            @Parameter(description = "订单ID", required = true) @PathVariable Long id,
            @RequestBody Map<String, Integer> body) {
        Integer days = body.get("days");
        if (days == null || days <= 0 || days > 30) {
            return Result.error(ResultCode.PARAM_ERROR.getCode(), "续租天数必须在1-30之间");
        }
        var result = rentalOrderService.renewOrder(id, days);
        if (!result.isSuccess()) {
            return Result.error(result.getCode(), result.getMessage());
        }
        return Result.success(convertToVO(result.getData()), "续租成功");
    }

    /**
     * 生成订单号
     */
    @Operation(summary = "生成订单号")
    @GetMapping("/generate-order-no")
    public Result<String> generateOrderNo() {
        String orderNo = rentalOrderService.generateOrderNo();
        return Result.success(orderNo);
    }

    /**
     * 将RentalOrder实体转换为RentalOrderVO
     */
    private RentalOrderVO convertToVO(RentalOrder order) {
        if (order == null) {
            return null;
        }
        RentalOrderVO orderVO = new RentalOrderVO();
        BeanUtils.copyProperties(order, orderVO);

        // 前端兼容字段：status 映射 (后端0-5 → 前端0-3)
        Integer backendStatus = order.getStatus();
        if (backendStatus != null) {
            if (backendStatus == 5) {
                orderVO.setStatus(3); // 已取消
            } else if (backendStatus >= 4) {
                orderVO.setStatus(2); // 已完成
            } else if (backendStatus >= 3) {
                orderVO.setStatus(1); // 进行中
            } else {
                orderVO.setStatus(0); // 待支付
            }
        }

        // 前端兼容字段：payStatus 直接映射
        orderVO.setPayStatus(order.getPaymentStatus());
        // 前端兼容字段：totalAmount
        orderVO.setTotalAmount(order.getTotalAmount());
        // 前端兼容字段：dailyInsuranceFee
        orderVO.setDailyInsuranceFee(order.getInsuranceFee());

        // 设备关联字段
        if (order.getDeviceId() != null) {
            Device device = deviceService.getById(order.getDeviceId());
            if (device != null) {
                orderVO.setDeviceName(device.getDeviceName());
                orderVO.setDeviceModel(device.getModel());
                orderVO.setRentalPrice(device.getRentalPrice());
                if (StringUtil.isNotEmpty(device.getImages())) {
                    String firstImage = device.getImages().split(",")[0].trim();
                    orderVO.setDeviceImage(firstImage);
                }
            }
        }

        // 计算租赁天数
        if (order.getStartTime() != null && order.getEndTime() != null) {
            long days = ChronoUnit.DAYS.between(order.getStartTime(), order.getEndTime());
            orderVO.setTotalDays((int) Math.max(1, days));
        }

        return orderVO;
    }
}
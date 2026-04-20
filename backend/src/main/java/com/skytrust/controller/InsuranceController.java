package com.skytrust.controller;

import com.skytrust.common.Result;
import com.skytrust.common.ResultCode;
import com.skytrust.dto.InsuranceDTO;
import com.skytrust.entity.Insurance;
import com.skytrust.service.InsuranceService;
import com.skytrust.vo.InsuranceVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 保险控制器
 *
 * @author SkyTrust Team
 */
@Api(tags = "保险管理", description = "保险记录管理接口")
@Validated
@RestController
@RequestMapping("/api/insurances")
public class InsuranceController {

    private final InsuranceService insuranceService;

    public InsuranceController(InsuranceService insuranceService) {
        this.insuranceService = insuranceService;
    }

    /**
     * 创建保险记录
     */
    @ApiOperation(value = "创建保险记录")
    @PostMapping
    public Result<InsuranceVO> createInsurance(@Valid @RequestBody InsuranceDTO insuranceDTO) {
        // 转换DTO为实体
        Insurance insurance = new Insurance();
        BeanUtils.copyProperties(insuranceDTO, insurance);

        // 保存保险记录
        boolean saved = insuranceService.save(insurance);
        if (!saved) {
            return Result.error("保险记录创建失败");
        }

        InsuranceVO insuranceVO = convertToVO(insurance);
        return Result.success(insuranceVO, "保险记录创建成功");
    }

    /**
     * 更新保险记录信息
     */
    @ApiOperation(value = "更新保险记录信息")
    @PutMapping("/{id}")
    public Result<InsuranceVO> updateInsurance(
            @ApiParam(value = "保险记录ID", required = true) @PathVariable Long id,
            @Valid @RequestBody InsuranceDTO insuranceDTO) {
        Insurance insurance = insuranceService.getById(id);
        if (insurance == null) {
            return Result.error(ResultCode.DATA_NOT_EXIST.getCode(), "保险记录不存在");
        }

        // 更新字段（排除不能修改的字段）
        BeanUtils.copyProperties(insuranceDTO, insurance, "id", "insuranceNo", "createTime", "updateTime");

        boolean updated = insuranceService.updateById(insurance);
        if (!updated) {
            return Result.error("保险记录更新失败");
        }

        InsuranceVO insuranceVO = convertToVO(insurance);
        return Result.success(insuranceVO, "保险记录更新成功");
    }

    /**
     * 获取保险记录详情
     */
    @ApiOperation(value = "获取保险记录详情")
    @GetMapping("/{id}")
    public Result<InsuranceVO> getInsuranceById(@ApiParam(value = "保险记录ID", required = true) @PathVariable Long id) {
        Insurance insurance = insuranceService.getById(id);
        if (insurance == null) {
            return Result.error(ResultCode.DATA_NOT_EXIST.getCode(), "保险记录不存在");
        }
        return Result.success(convertToVO(insurance));
    }

    /**
     * 删除保险记录（逻辑删除）
     */
    @ApiOperation(value = "删除保险记录")
    @DeleteMapping("/{id}")
    public Result<Void> deleteInsurance(@ApiParam(value = "保险记录ID", required = true) @PathVariable Long id) {
        boolean deleted = insuranceService.logicRemoveById(id);
        if (!deleted) {
            return Result.error("保险记录删除失败");
        }
        return Result.success("保险记录删除成功");
    }

    /**
     * 分页查询保险记录列表
     */
    @ApiOperation(value = "分页查询保险记录列表")
    @GetMapping
    public Result<List<InsuranceVO>> getInsuranceList(
            @ApiParam(value = "页码") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam(value = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @ApiParam(value = "用户ID") @RequestParam(required = false) Long userId,
            @ApiParam(value = "订单ID") @RequestParam(required = false) Long orderId,
            @ApiParam(value = "设备ID") @RequestParam(required = false) Long deviceId,
            @ApiParam(value = "保险类型") @RequestParam(required = false) Integer insuranceType,
            @ApiParam(value = "保险状态") @RequestParam(required = false) Integer insuranceStatus) {

        // 简化处理：使用Service的list方法
        List<Insurance> insurances = insuranceService.list();

        // 应用过滤条件
        List<Insurance> filteredInsurances = insurances.stream()
                .filter(insurance -> userId == null || insurance.getUserId().equals(userId))
                .filter(insurance -> orderId == null || insurance.getOrderId().equals(orderId))
                .filter(insurance -> deviceId == null || insurance.getDeviceId().equals(deviceId))
                .filter(insurance -> insuranceType == null || insurance.getInsuranceType().equals(insuranceType))
                .filter(insurance -> insuranceStatus == null || insurance.getStatus().equals(insuranceStatus))
                .skip((page - 1) * (long) size)
                .limit(size)
                .collect(Collectors.toList());

        List<InsuranceVO> insuranceVOs = filteredInsurances.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return Result.success(insuranceVOs);
    }

    /**
     * 根据订单ID查询保险记录
     */
    @ApiOperation(value = "根据订单ID查询保险记录")
    @GetMapping("/order/{orderId}")
    public Result<InsuranceVO> getInsuranceByOrderId(@ApiParam(value = "订单ID", required = true) @PathVariable Long orderId) {
        Insurance insurance = insuranceService.getByOrderId(orderId);
        if (insurance == null) {
            return Result.error(ResultCode.DATA_NOT_EXIST.getCode(), "保险记录不存在");
        }
        return Result.success(convertToVO(insurance));
    }

    /**
     * 根据保单号查询保险记录
     */
    @ApiOperation(value = "根据保单号查询保险记录")
    @GetMapping("/policy/{policyNo}")
    public Result<InsuranceVO> getInsuranceByPolicyNo(@ApiParam(value = "保单号", required = true) @PathVariable String policyNo) {
        Insurance insurance = insuranceService.getByPolicyNo(policyNo);
        if (insurance == null) {
            return Result.error(ResultCode.DATA_NOT_EXIST.getCode(), "保险记录不存在");
        }
        return Result.success(convertToVO(insurance));
    }

    /**
     * 将Insurance实体转换为InsuranceVO
     */
    private InsuranceVO convertToVO(Insurance insurance) {
        if (insurance == null) {
            return null;
        }
        InsuranceVO insuranceVO = new InsuranceVO();
        BeanUtils.copyProperties(insurance, insuranceVO);
        return insuranceVO;
    }
}
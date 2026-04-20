package com.skytrust.controller;

import com.skytrust.common.Result;
import com.skytrust.common.ResultCode;
import com.skytrust.dto.FlightRecordDTO;
import com.skytrust.entity.FlightRecord;
import com.skytrust.service.FlightRecordService;
import com.skytrust.vo.FlightRecordVO;
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
 * 飞行记录控制器
 *
 * @author SkyTrust Team
 */
@Api(tags = "飞行记录管理", description = "飞行记录管理接口")
@Validated
@RestController
@RequestMapping("/api/flight-records")
public class FlightRecordController {

    private final FlightRecordService flightRecordService;

    public FlightRecordController(FlightRecordService flightRecordService) {
        this.flightRecordService = flightRecordService;
    }

    /**
     * 创建飞行记录
     */
    @ApiOperation(value = "创建飞行记录")
    @PostMapping
    public Result<FlightRecordVO> createFlightRecord(@Valid @RequestBody FlightRecordDTO flightRecordDTO) {
        // 转换DTO为实体
        FlightRecord flightRecord = new FlightRecord();
        BeanUtils.copyProperties(flightRecordDTO, flightRecord);

        // 保存飞行记录
        boolean saved = flightRecordService.save(flightRecord);
        if (!saved) {
            return Result.error("飞行记录创建失败");
        }

        FlightRecordVO flightRecordVO = convertToVO(flightRecord);
        return Result.success(flightRecordVO, "飞行记录创建成功");
    }

    /**
     * 更新飞行记录信息
     */
    @ApiOperation(value = "更新飞行记录信息")
    @PutMapping("/{id}")
    public Result<FlightRecordVO> updateFlightRecord(
            @ApiParam(value = "飞行记录ID", required = true) @PathVariable Long id,
            @Valid @RequestBody FlightRecordDTO flightRecordDTO) {
        FlightRecord flightRecord = flightRecordService.getById(id);
        if (flightRecord == null) {
            return Result.error(ResultCode.DATA_NOT_EXIST.getCode(), "飞行记录不存在");
        }

        // 更新字段（排除不能修改的字段）
        BeanUtils.copyProperties(flightRecordDTO, flightRecord, "id", "flightNo", "createTime", "updateTime");

        boolean updated = flightRecordService.updateById(flightRecord);
        if (!updated) {
            return Result.error("飞行记录更新失败");
        }

        FlightRecordVO flightRecordVO = convertToVO(flightRecord);
        return Result.success(flightRecordVO, "飞行记录更新成功");
    }

    /**
     * 获取飞行记录详情
     */
    @ApiOperation(value = "获取飞行记录详情")
    @GetMapping("/{id}")
    public Result<FlightRecordVO> getFlightRecordById(@ApiParam(value = "飞行记录ID", required = true) @PathVariable Long id) {
        FlightRecord flightRecord = flightRecordService.getById(id);
        if (flightRecord == null) {
            return Result.error(ResultCode.DATA_NOT_EXIST.getCode(), "飞行记录不存在");
        }
        return Result.success(convertToVO(flightRecord));
    }

    /**
     * 删除飞行记录（逻辑删除）
     */
    @ApiOperation(value = "删除飞行记录")
    @DeleteMapping("/{id}")
    public Result<Void> deleteFlightRecord(@ApiParam(value = "飞行记录ID", required = true) @PathVariable Long id) {
        boolean deleted = flightRecordService.logicRemoveById(id);
        if (!deleted) {
            return Result.error("飞行记录删除失败");
        }
        return Result.success("飞行记录删除成功");
    }

    /**
     * 分页查询飞行记录列表
     */
    @ApiOperation(value = "分页查询飞行记录列表")
    @GetMapping
    public Result<List<FlightRecordVO>> getFlightRecordList(
            @ApiParam(value = "页码") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam(value = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @ApiParam(value = "用户ID") @RequestParam(required = false) Long userId,
            @ApiParam(value = "设备ID") @RequestParam(required = false) Long deviceId,
            @ApiParam(value = "订单ID") @RequestParam(required = false) Long orderId,
            @ApiParam(value = "飞行状态") @RequestParam(required = false) Integer flightStatus,
            @ApiParam(value = "是否违规") @RequestParam(required = false) Boolean violation) {

        // 简化处理：使用Service的list方法
        List<FlightRecord> flightRecords = flightRecordService.list();

        // 应用过滤条件
        List<FlightRecord> filteredFlightRecords = flightRecords.stream()
                .filter(record -> userId == null || record.getUserId().equals(userId))
                .filter(record -> deviceId == null || record.getDeviceId().equals(deviceId))
                .filter(record -> orderId == null || record.getOrderId().equals(orderId))
                .filter(record -> flightStatus == null || record.getFlightStatus().equals(flightStatus))
                .filter(record -> violation == null || record.getViolation().equals(violation))
                .skip((page - 1) * (long) size)
                .limit(size)
                .collect(Collectors.toList());

        List<FlightRecordVO> flightRecordVOs = filteredFlightRecords.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return Result.success(flightRecordVOs);
    }

    /**
     * 根据设备ID查询飞行记录
     */
    @ApiOperation(value = "根据设备ID查询飞行记录")
    @GetMapping("/device/{deviceId}")
    public Result<List<FlightRecordVO>> getFlightRecordsByDeviceId(@ApiParam(value = "设备ID", required = true) @PathVariable Long deviceId) {
        List<FlightRecord> flightRecords = flightRecordService.getByDeviceId(deviceId);
        List<FlightRecordVO> flightRecordVOs = flightRecords.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        return Result.success(flightRecordVOs);
    }

    /**
     * 根据订单ID查询飞行记录
     */
    @ApiOperation(value = "根据订单ID查询飞行记录")
    @GetMapping("/order/{orderId}")
    public Result<FlightRecordVO> getFlightRecordByOrderId(@ApiParam(value = "订单ID", required = true) @PathVariable Long orderId) {
        FlightRecord flightRecord = flightRecordService.getByOrderId(orderId);
        if (flightRecord == null) {
            return Result.error(ResultCode.DATA_NOT_EXIST.getCode(), "飞行记录不存在");
        }
        return Result.success(convertToVO(flightRecord));
    }

    /**
     * 根据用户ID查询飞行记录
     */
    @ApiOperation(value = "根据用户ID查询飞行记录")
    @GetMapping("/user/{userId}")
    public Result<List<FlightRecordVO>> getFlightRecordsByUserId(@ApiParam(value = "用户ID", required = true) @PathVariable Long userId) {
        List<FlightRecord> flightRecords = flightRecordService.getByUserId(userId);
        List<FlightRecordVO> flightRecordVOs = flightRecords.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        return Result.success(flightRecordVOs);
    }

    /**
     * 将FlightRecord实体转换为FlightRecordVO
     */
    private FlightRecordVO convertToVO(FlightRecord flightRecord) {
        if (flightRecord == null) {
            return null;
        }
        FlightRecordVO flightRecordVO = new FlightRecordVO();
        BeanUtils.copyProperties(flightRecord, flightRecordVO);
        return flightRecordVO;
    }
}
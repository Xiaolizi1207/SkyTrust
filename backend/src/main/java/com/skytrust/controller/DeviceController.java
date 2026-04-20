package com.skytrust.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.skytrust.common.Result;
import com.skytrust.common.ResultCode;
import com.skytrust.dto.DeviceDTO;
import com.skytrust.entity.Device;
import com.skytrust.enums.DeviceStatusEnum;
import com.skytrust.service.DeviceService;
import com.skytrust.vo.DeviceVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 设备控制器
 *
 * @author SkyTrust Team
 */
@Api(tags = "设备管理", description = "无人机设备管理接口")
@Validated
@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    /**
     * 创建设备
     */
    @ApiOperation(value = "创建设备")
    @PostMapping
    public Result<DeviceVO> createDevice(@Valid @RequestBody DeviceDTO deviceDTO) {
        // 检查序列号是否已存在
        if (deviceService.isSerialNumberExists(deviceDTO.getSerialNumber())) {
            return Result.error(ResultCode.DATA_DUPLICATE.getCode(), "设备序列号已存在");
        }

        // 转换DTO为实体
        Device device = new Device();
        BeanUtils.copyProperties(deviceDTO, device);

        // 调用Service添加设备
        var result = deviceService.addDevice(device);
        if (!result.isSuccess()) {
            return Result.error(result.getCode(), result.getMessage());
        }

        DeviceVO deviceVO = convertToVO(result.getData());
        return Result.success(deviceVO, "设备创建成功");
    }

    /**
     * 更新设备信息
     */
    @ApiOperation(value = "更新设备信息")
    @PutMapping("/{id}")
    public Result<DeviceVO> updateDevice(
            @ApiParam(value = "设备ID", required = true) @PathVariable Long id,
            @Valid @RequestBody DeviceDTO deviceDTO) {
        Device device = deviceService.getById(id);
        if (device == null) {
            return Result.error(ResultCode.DATA_NOT_EXIST.getCode(), "设备不存在");
        }

        // 检查序列号是否已被其他设备使用
        if (!device.getSerialNumber().equals(deviceDTO.getSerialNumber())) {
            if (deviceService.isSerialNumberExists(deviceDTO.getSerialNumber())) {
                return Result.error(ResultCode.DATA_DUPLICATE.getCode(), "设备序列号已被其他设备使用");
            }
        }

        // 更新字段
        BeanUtils.copyProperties(deviceDTO, device, "id", "createTime", "updateTime");

        boolean updated = deviceService.updateById(device);
        if (!updated) {
            return Result.error("设备更新失败");
        }

        DeviceVO deviceVO = convertToVO(device);
        return Result.success(deviceVO, "设备更新成功");
    }

    /**
     * 获取设备详情
     */
    @ApiOperation(value = "获取设备详情")
    @GetMapping("/{id}")
    public Result<DeviceVO> getDeviceById(@ApiParam(value = "设备ID", required = true) @PathVariable Long id) {
        Device device = deviceService.getById(id);
        if (device == null) {
            return Result.error(ResultCode.DATA_NOT_EXIST.getCode(), "设备不存在");
        }
        return Result.success(convertToVO(device));
    }

    /**
     * 删除设备（逻辑删除）
     */
    @ApiOperation(value = "删除设备")
    @DeleteMapping("/{id}")
    public Result<Void> deleteDevice(@ApiParam(value = "设备ID", required = true) @PathVariable Long id) {
        boolean deleted = deviceService.logicRemoveById(id);
        if (!deleted) {
            return Result.error("设备删除失败");
        }
        return Result.success("设备删除成功");
    }

    /**
     * 分页查询设备列表
     */
    @ApiOperation(value = "分页查询设备列表")
    @GetMapping
    public Result<List<DeviceVO>> getDeviceList(
            @ApiParam(value = "页码", defaultValue = "1") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam(value = "每页大小", defaultValue = "10") @RequestParam(defaultValue = "10") Integer size,
            @ApiParam(value = "设备名称") @RequestParam(required = false) String deviceName,
            @ApiParam(value = "设备型号") @RequestParam(required = false) String model,
            @ApiParam(value = "序列号") @RequestParam(required = false) String serialNumber,
            @ApiParam(value = "设备状态") @RequestParam(required = false) Integer status,
            @ApiParam(value = "最低租赁价格") @RequestParam(required = false) BigDecimal minPrice,
            @ApiParam(value = "最高租赁价格") @RequestParam(required = false) BigDecimal maxPrice,
            @ApiParam(value = "最低电量") @RequestParam(required = false) Integer minBattery,
            @ApiParam(value = "排序字段") @RequestParam(required = false) String orderBy) {

        IPage<Device> devicePage = deviceService.pageDevices(page, size, deviceName, model, serialNumber,
                status, minPrice, maxPrice, minBattery, orderBy);

        List<DeviceVO> deviceVOs = devicePage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return Result.success(deviceVOs);
    }

    /**
     * 更新设备状态
     */
    @ApiOperation(value = "更新设备状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateDeviceStatus(
            @ApiParam(value = "设备ID", required = true) @PathVariable Long id,
            @ApiParam(value = "状态（0-离线，1-在线，2-飞行中，3-维修中，4-已报废）", required = true) @RequestParam Integer status) {

        if (!DeviceStatusEnum.isValid(status)) {
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "无效的设备状态");
        }

        boolean updated = deviceService.updateDeviceStatus(id, status);
        if (!updated) {
            return Result.error("设备状态更新失败");
        }
        return Result.success("设备状态更新成功");
    }

    /**
     * 更新设备位置
     */
    @ApiOperation(value = "更新设备位置")
    @PutMapping("/{id}/location")
    public Result<Void> updateDeviceLocation(
            @ApiParam(value = "设备ID", required = true) @PathVariable Long id,
            @ApiParam(value = "纬度", required = true) @RequestParam BigDecimal latitude,
            @ApiParam(value = "经度", required = true) @RequestParam BigDecimal longitude,
            @ApiParam(value = "高度（米）") @RequestParam(required = false) BigDecimal altitude) {

        boolean updated = deviceService.updateDeviceLocation(id, latitude, longitude, altitude);
        if (!updated) {
            return Result.error("设备位置更新失败");
        }
        return Result.success("设备位置更新成功");
    }

    /**
     * 更新设备电量
     */
    @ApiOperation(value = "更新设备电量")
    @PutMapping("/{id}/battery")
    public Result<Void> updateDeviceBattery(
            @ApiParam(value = "设备ID", required = true) @PathVariable Long id,
            @ApiParam(value = "电池电量（百分比）", required = true) @RequestParam Integer batteryLevel) {

        if (batteryLevel < 0 || batteryLevel > 100) {
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "电池电量必须在0-100之间");
        }

        boolean updated = deviceService.updateDeviceBattery(id, batteryLevel);
        if (!updated) {
            return Result.error("设备电量更新失败");
        }
        return Result.success("设备电量更新成功");
    }

    /**
     * 更新设备飞行数据
     */
    @ApiOperation(value = "更新设备飞行数据")
    @PutMapping("/{id}/flight-data")
    public Result<Void> updateDeviceFlightData(
            @ApiParam(value = "设备ID", required = true) @PathVariable Long id,
            @ApiParam(value = "飞行速度（米/秒）", required = true) @RequestParam BigDecimal speed,
            @ApiParam(value = "飞行总时长（小时）", required = true) @RequestParam BigDecimal totalFlightHours) {

        if (speed.compareTo(BigDecimal.ZERO) < 0) {
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "飞行速度不能为负数");
        }
        if (totalFlightHours.compareTo(BigDecimal.ZERO) < 0) {
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "飞行总时长不能为负数");
        }

        boolean updated = deviceService.updateDeviceFlightData(id, speed, totalFlightHours);
        if (!updated) {
            return Result.error("设备飞行数据更新失败");
        }
        return Result.success("设备飞行数据更新成功");
    }

    /**
     * 获取可用设备列表
     */
    @ApiOperation(value = "获取可用设备列表")
    @GetMapping("/available")
    public Result<List<DeviceVO>> getAvailableDevices() {
        List<Device> devices = deviceService.getAvailableDevices();
        List<DeviceVO> deviceVOs = devices.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        return Result.success(deviceVOs);
    }

    /**
     * 获取附近设备
     */
    @ApiOperation(value = "获取附近设备")
    @GetMapping("/nearby")
    public Result<List<DeviceVO>> getNearbyDevices(
            @ApiParam(value = "纬度", required = true) @RequestParam BigDecimal latitude,
            @ApiParam(value = "经度", required = true) @RequestParam BigDecimal longitude,
            @ApiParam(value = "半径（公里）", defaultValue = "10") @RequestParam(defaultValue = "10") BigDecimal radius) {

        List<Device> devices = deviceService.getNearbyDevices(latitude, longitude, radius);
        List<DeviceVO> deviceVOs = devices.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        return Result.success(deviceVOs);
    }

    /**
     * 检查设备是否可用
     */
    @ApiOperation(value = "检查设备是否可用")
    @GetMapping("/{id}/available")
    public Result<Boolean> checkDeviceAvailable(@ApiParam(value = "设备ID", required = true) @PathVariable Long id) {
        boolean available = deviceService.isDeviceAvailable(id);
        return Result.success(available);
    }

    /**
     * 设备维护
     */
    @ApiOperation(value = "设备维护")
    @PostMapping("/{id}/maintain")
    public Result<Void> maintainDevice(
            @ApiParam(value = "设备ID", required = true) @PathVariable Long id,
            @ApiParam(value = "维护备注", required = true) @RequestParam String remark) {

        boolean maintained = deviceService.maintainDevice(id, remark);
        if (!maintained) {
            return Result.error("设备维护失败");
        }
        return Result.success("设备维护成功");
    }

    /**
     * 设备报废
     */
    @ApiOperation(value = "设备报废")
    @PostMapping("/{id}/scrap")
    public Result<Void> scrapDevice(
            @ApiParam(value = "设备ID", required = true) @PathVariable Long id,
            @ApiParam(value = "报废原因", required = true) @RequestParam String remark) {

        boolean scrapped = deviceService.scrapDevice(id, remark);
        if (!scrapped) {
            return Result.error("设备报废失败");
        }
        return Result.success("设备报废成功");
    }

    /**
     * 获取设备统计信息
     */
    @ApiOperation(value = "获取设备统计信息")
    @GetMapping("/statistics")
    public Result<DeviceService.DeviceStatistics> getDeviceStatistics() {
        DeviceService.DeviceStatistics statistics = deviceService.getDeviceStatistics();
        return Result.success(statistics);
    }

    /**
     * 将Device实体转换为DeviceVO
     */
    private DeviceVO convertToVO(Device device) {
        if (device == null) {
            return null;
        }
        DeviceVO deviceVO = new DeviceVO();
        BeanUtils.copyProperties(device, deviceVO);
        return deviceVO;
    }
}
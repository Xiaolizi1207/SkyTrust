package com.skytrust.controller;

import com.skytrust.common.Result;
import com.skytrust.common.ResultCode;
import com.skytrust.dto.DeviceTrackDTO;
import com.skytrust.entity.DeviceTrack;
import com.skytrust.service.DeviceTrackService;
import com.skytrust.vo.DeviceTrackVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 设备轨迹控制器
 *
 * @author SkyTrust Team
 */
@Tag(name = "设备轨迹管理", description = "设备位置轨迹管理接口")
@Validated
@RestController
@RequestMapping("/api/device-tracks")
public class DeviceTrackController {

    private final DeviceTrackService deviceTrackService;

    public DeviceTrackController(DeviceTrackService deviceTrackService) {
        this.deviceTrackService = deviceTrackService;
    }

    /**
     * 创建设备轨迹记录
     */
    @Operation(summary = "创建设备轨迹记录")
    @PostMapping
    public Result<DeviceTrackVO> createDeviceTrack(@Valid @RequestBody DeviceTrackDTO deviceTrackDTO) {
        // 转换DTO为实体
        DeviceTrack deviceTrack = new DeviceTrack();
        BeanUtils.copyProperties(deviceTrackDTO, deviceTrack);

        // 保存轨迹记录
        boolean saved = deviceTrackService.save(deviceTrack);
        if (!saved) {
            return Result.error("设备轨迹记录创建失败");
        }

        DeviceTrackVO deviceTrackVO = convertToVO(deviceTrack);
        return Result.success(deviceTrackVO, "设备轨迹记录创建成功");
    }

    /**
     * 更新设备轨迹记录信息
     */
    @Operation(summary = "更新设备轨迹记录信息")
    @PutMapping("/{id}")
    public Result<DeviceTrackVO> updateDeviceTrack(
            @Parameter(description = "轨迹记录ID", required = true) @PathVariable Long id,
            @Valid @RequestBody DeviceTrackDTO deviceTrackDTO) {
        DeviceTrack deviceTrack = deviceTrackService.getById(id);
        if (deviceTrack == null) {
            return Result.error(ResultCode.DATA_NOT_EXIST.getCode(), "设备轨迹记录不存在");
        }

        // 更新字段（排除不能修改的字段）
        BeanUtils.copyProperties(deviceTrackDTO, deviceTrack, "id", "createTime", "updateTime");

        boolean updated = deviceTrackService.updateById(deviceTrack);
        if (!updated) {
            return Result.error("设备轨迹记录更新失败");
        }

        DeviceTrackVO deviceTrackVO = convertToVO(deviceTrack);
        return Result.success(deviceTrackVO, "设备轨迹记录更新成功");
    }

    /**
     * 获取设备轨迹记录详情
     */
    @Operation(summary = "获取设备轨迹记录详情")
    @GetMapping("/{id}")
    public Result<DeviceTrackVO> getDeviceTrackById(@Parameter(description = "轨迹记录ID", required = true) @PathVariable Long id) {
        DeviceTrack deviceTrack = deviceTrackService.getById(id);
        if (deviceTrack == null) {
            return Result.error(ResultCode.DATA_NOT_EXIST.getCode(), "设备轨迹记录不存在");
        }
        return Result.success(convertToVO(deviceTrack));
    }

    /**
     * 删除设备轨迹记录（逻辑删除）
     */
    @Operation(summary = "删除设备轨迹记录")
    @DeleteMapping("/{id}")
    public Result<Void> deleteDeviceTrack(@Parameter(description = "轨迹记录ID", required = true) @PathVariable Long id) {
        boolean deleted = deviceTrackService.logicRemoveById(id);
        if (!deleted) {
            return Result.error("设备轨迹记录删除失败");
        }
        return Result.success("设备轨迹记录删除成功");
    }

    /**
     * 分页查询设备轨迹记录列表
     */
    @Operation(summary = "分页查询设备轨迹记录列表")
    @GetMapping
    public Result<List<DeviceTrackVO>> getDeviceTrackList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "设备ID") @RequestParam(required = false) Long deviceId,
            @Parameter(description = "开始时间") @RequestParam(required = false) LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) LocalDateTime endTime) {

        // 简化处理：使用Service的list方法
        List<DeviceTrack> deviceTracks = deviceTrackService.list();

        // 应用过滤条件
        List<DeviceTrack> filteredDeviceTracks = deviceTracks.stream()
                .filter(track -> deviceId == null || track.getDeviceId().equals(deviceId))
                .filter(track -> startTime == null || !track.getRecordTime().isBefore(startTime))
                .filter(track -> endTime == null || !track.getRecordTime().isAfter(endTime))
                .skip((page - 1) * (long) size)
                .limit(size)
                .collect(Collectors.toList());

        List<DeviceTrackVO> deviceTrackVOs = filteredDeviceTracks.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return Result.success(deviceTrackVOs);
    }

    /**
     * 根据设备ID查询轨迹记录
     */
    @Operation(summary = "根据设备ID查询轨迹记录")
    @GetMapping("/device/{deviceId}")
    public Result<List<DeviceTrackVO>> getDeviceTracksByDeviceId(@Parameter(description = "设备ID", required = true) @PathVariable Long deviceId) {
        List<DeviceTrack> deviceTracks = deviceTrackService.getByDeviceId(deviceId);
        List<DeviceTrackVO> deviceTrackVOs = deviceTracks.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        return Result.success(deviceTrackVOs);
    }

    /**
     * 根据设备ID和时间范围查询轨迹记录
     */
    @Operation(summary = "根据设备ID和时间范围查询轨迹记录")
    @GetMapping("/device/{deviceId}/time-range")
    public Result<List<DeviceTrackVO>> getDeviceTracksByTimeRange(
            @Parameter(description = "设备ID", required = true) @PathVariable Long deviceId,
            @Parameter(description = "开始时间", required = true) @RequestParam LocalDateTime startTime,
            @Parameter(description = "结束时间", required = true) @RequestParam LocalDateTime endTime) {
        List<DeviceTrack> deviceTracks = deviceTrackService.getByDeviceIdAndTimeRange(deviceId, startTime, endTime);
        List<DeviceTrackVO> deviceTrackVOs = deviceTracks.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        return Result.success(deviceTrackVOs);
    }

    /**
     * 获取设备最新位置
     */
    @Operation(summary = "获取设备最新位置")
    @GetMapping("/device/{deviceId}/latest")
    public Result<DeviceTrackVO> getLatestDeviceTrack(@Parameter(description = "设备ID", required = true) @PathVariable Long deviceId) {
        DeviceTrack deviceTrack = deviceTrackService.getLatestByDeviceId(deviceId);
        if (deviceTrack == null) {
            return Result.error(ResultCode.DATA_NOT_EXIST.getCode(), "设备轨迹记录不存在");
        }
        return Result.success(convertToVO(deviceTrack));
    }

    /**
     * 将DeviceTrack实体转换为DeviceTrackVO
     */
    private DeviceTrackVO convertToVO(DeviceTrack deviceTrack) {
        if (deviceTrack == null) {
            return null;
        }
        DeviceTrackVO deviceTrackVO = new DeviceTrackVO();
        BeanUtils.copyProperties(deviceTrack, deviceTrackVO);
        return deviceTrackVO;
    }
}
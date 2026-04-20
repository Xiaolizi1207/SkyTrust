package com.skytrust.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.skytrust.common.Result;
import com.skytrust.common.ResultCode;
import com.skytrust.common.utils.DateUtil;
import com.skytrust.common.utils.StringUtil;
import com.skytrust.entity.Device;
import com.skytrust.exception.BusinessException;
import com.skytrust.mapper.DeviceMapper;
import com.skytrust.service.DeviceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 设备服务实现类
 *
 * @author SkyTrust Team
 */
@Slf4j
@Service
public class DeviceServiceImpl extends BaseService<DeviceMapper, Device> implements DeviceService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Device> addDevice(Device device) {
        log.info("添加新设备: {}", device.getDeviceName());

        // 1. 验证必填字段
        if (StringUtil.isEmpty(device.getDeviceName())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "设备名称不能为空");
        }
        if (StringUtil.isEmpty(device.getSerialNumber())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "设备序列号不能为空");
        }
        if (device.getRentalPrice() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "租赁价格不能为空");
        }

        // 2. 检查序列号是否已存在
        if (isSerialNumberExists(device.getSerialNumber())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "设备序列号已存在");
        }

        // 3. 设置默认值
        device.setId(null); // 确保ID为null
        if (device.getStatus() == null) {
            device.setStatus(0); // 默认离线
        }
        if (device.getBatteryLevel() == null) {
            device.setBatteryLevel(100); // 默认满电
        }
        if (device.getTotalFlightHours() == null) {
            device.setTotalFlightHours(BigDecimal.ZERO); // 默认飞行时长0
        }
        if (device.getSpeed() == null) {
            device.setSpeed(BigDecimal.ZERO); // 默认速度0
        }
        if (device.getInsuranceFee() == null) {
            device.setInsuranceFee(BigDecimal.ZERO); // 默认保险费用0
        }

        // 4. 保存设备
        boolean success = super.save(device);
        if (!success) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "设备添加失败");
        }

        log.info("设备添加成功: {}, ID: {}", device.getDeviceName(), device.getId());
        return Result.success(device, "设备添加成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDeviceStatus(Long deviceId, Integer status) {
        log.info("更新设备状态: {}, status: {}", deviceId, status);

        // 验证参数
        if (deviceId == null || status == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "参数错误");
        }
        if (status < 0 || status > 4) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "状态值无效 (0-离线,1-在线,2-飞行中,3-维修中,4-已报废)");
        }

        Device device = getById(deviceId);
        if (device == null) {
            throw new BusinessException(ResultCode.DEVICE_NOT_EXIST, "设备不存在");
        }

        // 如果状态变为在线，更新最后上线时间
        if (status == 1 && device.getStatus() != 1) {
            device.setLastOnlineTime(LocalDateTime.now());
        }

        device.setStatus(status);
        return updateById(device);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDeviceLocation(Long deviceId, BigDecimal latitude, BigDecimal longitude, BigDecimal altitude) {
        log.info("更新设备位置: {}, lat: {}, lng: {}, alt: {}", deviceId, latitude, longitude, altitude);

        if (deviceId == null || latitude == null || longitude == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "参数错误");
        }

        Device device = getById(deviceId);
        if (device == null) {
            throw new BusinessException(ResultCode.DEVICE_NOT_EXIST, "设备不存在");
        }

        device.setLatitude(latitude);
        device.setLongitude(longitude);
        if (altitude != null) {
            device.setAltitude(altitude);
        }

        return updateById(device);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDeviceBattery(Long deviceId, Integer batteryLevel) {
        log.info("更新设备电量: {}, battery: {}%", deviceId, batteryLevel);

        if (deviceId == null || batteryLevel == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "参数错误");
        }
        if (batteryLevel < 0 || batteryLevel > 100) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "电量范围 0-100%");
        }

        Device device = getById(deviceId);
        if (device == null) {
            throw new BusinessException(ResultCode.DEVICE_NOT_EXIST, "设备不存在");
        }

        device.setBatteryLevel(batteryLevel);
        return updateById(device);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDeviceFlightData(Long deviceId, BigDecimal speed, BigDecimal totalFlightHours) {
        log.info("更新设备飞行数据: {}, speed: {}, hours: {}", deviceId, speed, totalFlightHours);

        if (deviceId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "设备ID不能为空");
        }

        Device device = getById(deviceId);
        if (device == null) {
            throw new BusinessException(ResultCode.DEVICE_NOT_EXIST, "设备不存在");
        }

        if (speed != null && speed.compareTo(BigDecimal.ZERO) >= 0) {
            device.setSpeed(speed);
        }
        if (totalFlightHours != null && totalFlightHours.compareTo(BigDecimal.ZERO) >= 0) {
            device.setTotalFlightHours(totalFlightHours);
        }

        return updateById(device);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDeviceLastOnlineTime(Long deviceId) {
        if (deviceId == null) {
            return false;
        }

        Device device = getById(deviceId);
        if (device == null) {
            return false;
        }

        device.setLastOnlineTime(LocalDateTime.now());
        return updateById(device);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDeviceLastMaintenanceTime(Long deviceId) {
        if (deviceId == null) {
            return false;
        }

        Device device = getById(deviceId);
        if (device == null) {
            return false;
        }

        device.setLastMaintenanceTime(LocalDateTime.now());
        return updateById(device);
    }

    @Override
    public Device getDeviceBySerialNumber(String serialNumber) {
        if (StringUtil.isEmpty(serialNumber)) {
            return null;
        }
        LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Device::getSerialNumber, serialNumber);
        return getOne(wrapper);
    }

    @Override
    public List<Device> getDevicesByOwner(Long ownerId) {
        if (ownerId == null) {
            return listAll();
        }
        LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Device::getOwnerId, ownerId);
        return list(wrapper);
    }

    @Override
    public List<Device> getDevicesByStatus(Integer status) {
        if (status == null) {
            return listAll();
        }
        LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Device::getStatus, status);
        return list(wrapper);
    }

    @Override
    public List<Device> getAvailableDevices() {
        LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Device::getStatus, 1) // 在线
                .ge(Device::getBatteryLevel, 20) // 电量大于等于20%
                .orderByDesc(Device::getBatteryLevel); // 按电量降序
        return list(wrapper);
    }

    @Override
    public List<Device> getNearbyDevices(BigDecimal latitude, BigDecimal longitude, BigDecimal radius) {
        // TODO: 实现附近设备查询（需要空间查询或距离计算）
        // 这里简单返回所有在线设备
        log.warn("附近设备查询功能未完全实现，返回所有在线设备");
        return getDevicesByStatus(1);
    }

    @Override
    public IPage<Device> pageDevices(Integer page, Integer size,
                                    String deviceName, String model, String serialNumber,
                                    Integer status, BigDecimal minPrice, BigDecimal maxPrice,
                                    Integer minBattery, String orderBy) {
        Page<Device> pageObj = getPage(page, size);
        QueryWrapper<Device> wrapper = new QueryWrapper<>();

        if (StringUtil.isNotEmpty(deviceName)) {
            wrapper.like(getColumn("deviceName"), deviceName);
        }
        if (StringUtil.isNotEmpty(model)) {
            wrapper.like(getColumn("model"), model);
        }
        if (StringUtil.isNotEmpty(serialNumber)) {
            wrapper.like(getColumn("serialNumber"), serialNumber);
        }
        if (status != null) {
            wrapper.eq(getColumn("status"), status);
        }
        if (minPrice != null) {
            wrapper.ge(getColumn("rentalPrice"), minPrice);
        }
        if (maxPrice != null) {
            wrapper.le(getColumn("rentalPrice"), maxPrice);
        }
        if (minBattery != null) {
            wrapper.ge(getColumn("batteryLevel"), minBattery);
        }

        applyOrderBy(wrapper, orderBy);
        return page(pageObj, wrapper);
    }

    @Override
    public boolean isDeviceAvailable(Long deviceId) {
        if (deviceId == null) {
            return false;
        }

        Device device = getById(deviceId);
        if (device == null) {
            return false;
        }

        // 在线、电量充足（>=20%）、未飞行
        return device.getStatus() != null && device.getStatus() == 1 &&
                device.getBatteryLevel() != null && device.getBatteryLevel() >= 20;
    }

    @Override
    public boolean isSerialNumberExists(String serialNumber) {
        if (StringUtil.isEmpty(serialNumber)) {
            return false;
        }
        LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Device::getSerialNumber, serialNumber);
        return count(wrapper) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchUpdateDeviceStatus(List<Long> deviceIds, Integer status) {
        log.info("批量更新设备状态: {}, status: {}", deviceIds, status);

        if (deviceIds == null || deviceIds.isEmpty() || status == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "参数错误");
        }
        if (status < 0 || status > 4) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "状态值无效 (0-离线,1-在线,2-飞行中,3-维修中,4-已报废)");
        }

        boolean success = true;
        for (Long deviceId : deviceIds) {
            if (!updateDeviceStatus(deviceId, status)) {
                success = false;
                log.error("更新设备状态失败: {}", deviceId);
            }
        }
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchUpdateDeviceOwner(List<Long> deviceIds, Long ownerId) {
        log.info("批量更新设备所有者: {}, ownerId: {}", deviceIds, ownerId);

        if (deviceIds == null || deviceIds.isEmpty() || ownerId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "参数错误");
        }

        boolean success = true;
        for (Long deviceId : deviceIds) {
            Device device = getById(deviceId);
            if (device == null) {
                log.error("设备不存在: {}", deviceId);
                success = false;
                continue;
            }

            device.setOwnerId(ownerId);
            if (!updateById(device)) {
                success = false;
                log.error("更新设备所有者失败: {}", deviceId);
            }
        }
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean maintainDevice(Long deviceId, String remark) {
        log.info("设备维护: {}, remark: {}", deviceId, remark);

        if (deviceId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "设备ID不能为空");
        }

        Device device = getById(deviceId);
        if (device == null) {
            throw new BusinessException(ResultCode.DEVICE_NOT_EXIST, "设备不存在");
        }

        // 更新状态为维修中
        device.setStatus(3);
        device.setLastMaintenanceTime(LocalDateTime.now());
        if (StringUtil.isNotEmpty(remark)) {
            device.setRemark(remark);
        }

        return updateById(device);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean scrapDevice(Long deviceId, String remark) {
        log.info("设备报废: {}, remark: {}", deviceId, remark);

        if (deviceId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "设备ID不能为空");
        }

        Device device = getById(deviceId);
        if (device == null) {
            throw new BusinessException(ResultCode.DEVICE_NOT_EXIST, "设备不存在");
        }

        // 更新状态为已报废
        device.setStatus(4);
        if (StringUtil.isNotEmpty(remark)) {
            device.setRemark(remark);
        }

        return updateById(device);
    }

    @Override
    public DeviceStatistics getDeviceStatistics() {
        DeviceStatistics statistics = new DeviceStatistics();

        // 统计总设备数
        statistics.setTotalDevices(count());

        // 统计各状态设备数
        for (int status = 0; status <= 4; status++) {
            LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Device::getStatus, status);
            long count = count(wrapper);

            switch (status) {
                case 0:
                    // 离线设备数（不在统计中）
                    break;
                case 1:
                    statistics.setOnlineDevices(count);
                    break;
                case 2:
                    statistics.setFlyingDevices(count);
                    break;
                case 3:
                    statistics.setMaintenanceDevices(count);
                    break;
                case 4:
                    statistics.setScrappedDevices(count);
                    break;
            }
        }

        return statistics;
    }

    @Override
    public String validate(Device entity) {
        // 调用父类验证
        String parentValidation = super.validate(entity);
        if (parentValidation != null) {
            return parentValidation;
        }

        // 自定义设备验证逻辑
        if (StringUtil.isNotEmpty(entity.getDeviceName())) {
            if (entity.getDeviceName().length() < 2 || entity.getDeviceName().length() > 100) {
                return "设备名称长度需在2-100个字符之间";
            }
        }

        if (StringUtil.isNotEmpty(entity.getSerialNumber())) {
            if (entity.getSerialNumber().length() < 5 || entity.getSerialNumber().length() > 50) {
                return "设备序列号长度需在5-50个字符之间";
            }
        }

        if (entity.getStatus() != null) {
            if (entity.getStatus() < 0 || entity.getStatus() > 4) {
                return "设备状态无效 (0-离线,1-在线,2-飞行中,3-维修中,4-已报废)";
            }
        }

        if (entity.getBatteryLevel() != null) {
            if (entity.getBatteryLevel() < 0 || entity.getBatteryLevel() > 100) {
                return "电池电量范围 0-100%";
            }
        }

        if (entity.getRentalPrice() != null) {
            if (entity.getRentalPrice().compareTo(BigDecimal.ZERO) < 0) {
                return "租赁价格不能为负数";
            }
        }

        if (entity.getInsuranceFee() != null) {
            if (entity.getInsuranceFee().compareTo(BigDecimal.ZERO) < 0) {
                return "保险费用不能为负数";
            }
        }

        if (entity.getTotalFlightHours() != null) {
            if (entity.getTotalFlightHours().compareTo(BigDecimal.ZERO) < 0) {
                return "飞行总时长不能为负数";
            }
        }

        if (entity.getSpeed() != null) {
            if (entity.getSpeed().compareTo(BigDecimal.ZERO) < 0) {
                return "飞行速度不能为负数";
            }
        }

        return null; // 验证通过
    }
}
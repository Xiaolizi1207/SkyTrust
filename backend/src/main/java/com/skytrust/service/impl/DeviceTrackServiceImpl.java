package com.skytrust.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.skytrust.entity.DeviceTrack;
import com.skytrust.mapper.DeviceTrackMapper;
import com.skytrust.service.DeviceTrackService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 设备轨迹服务实现类
 *
 * @author SkyTrust Team
 */
@Service
public class DeviceTrackServiceImpl extends BaseService<DeviceTrackMapper, DeviceTrack> implements DeviceTrackService {

    @Override
    public List<DeviceTrack> getByDeviceId(Long deviceId) {
        if (deviceId == null) {
            return listAll();
        }
        LambdaQueryWrapper<DeviceTrack> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DeviceTrack::getDeviceId, deviceId)
                .orderByDesc(DeviceTrack::getRecordTime);
        return list(wrapper);
    }

    @Override
    public List<DeviceTrack> getByDeviceIdAndTimeRange(Long deviceId, LocalDateTime startTime, LocalDateTime endTime) {
        if (deviceId == null) {
            return listAll();
        }
        LambdaQueryWrapper<DeviceTrack> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DeviceTrack::getDeviceId, deviceId);
        if (startTime != null) {
            wrapper.ge(DeviceTrack::getRecordTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(DeviceTrack::getRecordTime, endTime);
        }
        wrapper.orderByAsc(DeviceTrack::getRecordTime);
        return list(wrapper);
    }

    @Override
    public DeviceTrack getLatestByDeviceId(Long deviceId) {
        if (deviceId == null) {
            return null;
        }
        LambdaQueryWrapper<DeviceTrack> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DeviceTrack::getDeviceId, deviceId)
                .orderByDesc(DeviceTrack::getRecordTime)
                .last("LIMIT 1");
        return getOne(wrapper);
    }
}
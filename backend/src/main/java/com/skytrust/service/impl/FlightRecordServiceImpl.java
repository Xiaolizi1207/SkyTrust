package com.skytrust.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.skytrust.entity.FlightRecord;
import com.skytrust.mapper.FlightRecordMapper;
import com.skytrust.service.FlightRecordService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 飞行记录服务实现类
 *
 * @author SkyTrust Team
 */
@Service
public class FlightRecordServiceImpl extends BaseService<FlightRecordMapper, FlightRecord> implements FlightRecordService {

    @Override
    public List<FlightRecord> getByDeviceId(Long deviceId) {
        if (deviceId == null) {
            return listAll();
        }
        LambdaQueryWrapper<FlightRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FlightRecord::getDeviceId, deviceId)
                .orderByDesc(FlightRecord::getStartTime);
        return list(wrapper);
    }

    @Override
    public FlightRecord getByOrderId(Long orderId) {
        if (orderId == null) {
            return null;
        }
        LambdaQueryWrapper<FlightRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FlightRecord::getOrderId, orderId);
        return getOne(wrapper);
    }

    @Override
    public List<FlightRecord> getByUserId(Long userId) {
        if (userId == null) {
            return listAll();
        }
        LambdaQueryWrapper<FlightRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FlightRecord::getUserId, userId)
                .orderByDesc(FlightRecord::getStartTime);
        return list(wrapper);
    }
}
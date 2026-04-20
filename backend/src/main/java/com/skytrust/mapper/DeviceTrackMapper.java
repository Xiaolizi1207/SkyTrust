package com.skytrust.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.skytrust.entity.DeviceTrack;
import org.apache.ibatis.annotations.Mapper;

/**
 * 设备位置轨迹Mapper接口
 *
 * @author SkyTrust Team
 */
@Mapper
public interface DeviceTrackMapper extends BaseMapper<DeviceTrack> {

    // 可以在此添加自定义SQL方法
    // 例如：根据设备ID和时间范围查询轨迹
    // @Select("SELECT * FROM device_track WHERE device_id = #{deviceId} AND record_time BETWEEN #{startTime} AND #{endTime} ORDER BY record_time ASC")
    // List<DeviceTrack> selectByDeviceIdAndTimeRange(@Param("deviceId") Long deviceId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}
package com.skytrust.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.skytrust.entity.Device;
import org.apache.ibatis.annotations.Mapper;

/**
 * 设备Mapper接口
 *
 * @author SkyTrust Team
 */
@Mapper
public interface DeviceMapper extends BaseMapper<Device> {

    // 可以在此添加自定义SQL方法
    // 例如：根据状态查询设备列表
    // @Select("SELECT * FROM device WHERE status = #{status} ORDER BY create_time DESC")
    // List<Device> selectByStatus(@Param("status") Integer status);
}
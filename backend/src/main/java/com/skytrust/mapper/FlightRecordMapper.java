package com.skytrust.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.skytrust.entity.FlightRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 飞行记录Mapper接口
 *
 * @author SkyTrust Team
 */
@Mapper
public interface FlightRecordMapper extends BaseMapper<FlightRecord> {

    // 可以在此添加自定义SQL方法
    // 例如：根据订单ID查询飞行记录
    // @Select("SELECT * FROM flight_record WHERE order_id = #{orderId}")
    // FlightRecord selectByOrderId(@Param("orderId") Long orderId);
}
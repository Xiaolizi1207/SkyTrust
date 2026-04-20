package com.skytrust.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.skytrust.entity.Insurance;
import org.apache.ibatis.annotations.Mapper;

/**
 * 保险记录Mapper接口
 *
 * @author SkyTrust Team
 */
@Mapper
public interface InsuranceMapper extends BaseMapper<Insurance> {

    // 可以在此添加自定义SQL方法
    // 例如：根据订单ID查询保险记录
    // @Select("SELECT * FROM insurance WHERE order_id = #{orderId}")
    // Insurance selectByOrderId(@Param("orderId") Long orderId);
}
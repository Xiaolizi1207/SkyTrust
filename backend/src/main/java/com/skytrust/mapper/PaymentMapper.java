package com.skytrust.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.skytrust.entity.Payment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付记录Mapper接口
 *
 * @author SkyTrust Team
 */
@Mapper
public interface PaymentMapper extends BaseMapper<Payment> {

    // 可以在此添加自定义SQL方法
    // 例如：根据支付状态查询支付记录
    // @Select("SELECT * FROM payment WHERE status = #{status} ORDER BY create_time DESC")
    // List<Payment> selectByStatus(@Param("status") Integer status);
}
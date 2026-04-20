package com.skytrust.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.skytrust.entity.RentalOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 租赁订单Mapper接口
 *
 * @author SkyTrust Team
 */
@Mapper
public interface RentalOrderMapper extends BaseMapper<RentalOrder> {

    // 可以在此添加自定义SQL方法
    // 例如：根据用户ID查询订单列表
    // @Select("SELECT * FROM rental_order WHERE user_id = #{userId} ORDER BY create_time DESC")
    // List<RentalOrder> selectByUserId(@Param("userId") Long userId);
}
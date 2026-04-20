package com.skytrust.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.skytrust.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper接口
 *
 * @author SkyTrust Team
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    // 可以在此添加自定义SQL方法
    // 例如：根据用户名查询用户
    // @Select("SELECT * FROM user WHERE username = #{username}")
    // User selectByUsername(@Param("username") String username);
}
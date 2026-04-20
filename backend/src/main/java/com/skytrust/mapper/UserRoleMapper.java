package com.skytrust.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.skytrust.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户角色关联Mapper接口
 *
 * @author SkyTrust Team
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {

    // 可以在此添加自定义SQL方法
    // 例如：根据用户ID查询角色ID列表
    // @Select("SELECT role_id FROM sys_user_role WHERE user_id = #{userId}")
    // List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);
}
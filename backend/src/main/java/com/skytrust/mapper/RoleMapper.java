package com.skytrust.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.skytrust.entity.Role;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色Mapper接口
 *
 * @author SkyTrust Team
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    // 可以在此添加自定义SQL方法
    // 例如：根据角色代码查询角色
    // @Select("SELECT * FROM sys_role WHERE role_code = #{roleCode}")
    // Role selectByRoleCode(@Param("roleCode") String roleCode);
}
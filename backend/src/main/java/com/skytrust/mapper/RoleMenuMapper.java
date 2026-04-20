package com.skytrust.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.skytrust.entity.RoleMenu;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色菜单关联Mapper接口
 *
 * @author SkyTrust Team
 */
@Mapper
public interface RoleMenuMapper extends BaseMapper<RoleMenu> {

    // 可以在此添加自定义SQL方法
    // 例如：根据角色ID查询菜单ID列表
    // @Select("SELECT menu_id FROM sys_role_menu WHERE role_id = #{roleId}")
    // List<Long> selectMenuIdsByRoleId(@Param("roleId") Long roleId);
}
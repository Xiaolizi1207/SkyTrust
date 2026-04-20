package com.skytrust.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.skytrust.entity.Menu;
import org.apache.ibatis.annotations.Mapper;

/**
 * 菜单Mapper接口
 *
 * @author SkyTrust Team
 */
@Mapper
public interface MenuMapper extends BaseMapper<Menu> {

    // 可以在此添加自定义SQL方法
    // 例如：查询所有启用的菜单
    // @Select("SELECT * FROM sys_menu WHERE status = 1 AND deleted = 0 ORDER BY sort_order ASC")
    // List<Menu> selectAllEnabledMenus();
}
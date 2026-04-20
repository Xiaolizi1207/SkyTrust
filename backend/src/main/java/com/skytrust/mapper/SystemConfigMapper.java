package com.skytrust.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.skytrust.entity.SystemConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统配置Mapper接口
 *
 * @author SkyTrust Team
 */
@Mapper
public interface SystemConfigMapper extends BaseMapper<SystemConfig> {

    // 可以在此添加自定义SQL方法
    // 例如：根据配置键查询配置值
    // @Select("SELECT config_value FROM system_config WHERE config_key = #{configKey}")
    // String selectValueByKey(@Param("configKey") String configKey);
}
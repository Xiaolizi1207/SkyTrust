package com.skytrust.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.skytrust.entity.DictType;
import org.apache.ibatis.annotations.Mapper;

/**
 * 字典类型Mapper接口
 *
 * @author SkyTrust Team
 */
@Mapper
public interface DictTypeMapper extends BaseMapper<DictType> {

    // 可以在此添加自定义SQL方法
    // 例如：根据类型编码查询字典类型
    // @Select("SELECT * FROM dict_type WHERE type_code = #{typeCode}")
    // DictType selectByTypeCode(@Param("typeCode") String typeCode);
}
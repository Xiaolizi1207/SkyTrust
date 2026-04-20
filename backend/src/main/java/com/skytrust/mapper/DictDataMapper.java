package com.skytrust.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.skytrust.entity.DictData;
import org.apache.ibatis.annotations.Mapper;

/**
 * 字典数据Mapper接口
 *
 * @author SkyTrust Team
 */
@Mapper
public interface DictDataMapper extends BaseMapper<DictData> {

    // 可以在此添加自定义SQL方法
    // 例如：根据字典类型编码查询字典数据列表
    // @Select("SELECT * FROM dict_data WHERE dict_type = #{dictType} AND status = 1 ORDER BY sort_order")
    // List<DictData> selectByDictType(@Param("dictType") String dictType);
}
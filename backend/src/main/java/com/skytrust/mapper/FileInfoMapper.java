package com.skytrust.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.skytrust.entity.FileInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文件信息Mapper接口
 *
 * @author SkyTrust Team
 */
@Mapper
public interface FileInfoMapper extends BaseMapper<FileInfo> {

    // 可以在此添加自定义SQL方法
    // 例如：根据业务类型和业务ID查询文件
    // @Select("SELECT * FROM file_info WHERE biz_type = #{bizType} AND biz_id = #{bizId} AND deleted = 0")
    // List<FileInfo> selectByBizTypeAndId(@Param("bizType") String bizType, @Param("bizId") Long bizId);
}
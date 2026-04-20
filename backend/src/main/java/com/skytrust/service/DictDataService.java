package com.skytrust.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.skytrust.entity.DictData;

import java.util.List;

/**
 * 字典数据服务接口
 *
 * @author SkyTrust Team
 */
public interface DictDataService extends com.baomidou.mybatisplus.extension.service.IService<DictData> {

    /**
     * 创建字典数据
     *
     * @param dictData 字典数据实体
     * @return 是否成功
     */
    boolean createDictData(DictData dictData);

    /**
     * 更新字典数据
     *
     * @param dictData 字典数据实体
     * @return 是否成功
     */
    boolean updateDictData(DictData dictData);

    /**
     * 删除字典数据（逻辑删除）
     *
     * @param id 字典数据ID
     * @return 是否成功
     */
    boolean deleteDictData(Long id);

    /**
     * 批量删除字典数据
     *
     * @param ids 字典数据ID列表
     * @return 是否成功
     */
    boolean batchDeleteDictDatas(List<Long> ids);

    /**
     * 根据ID获取字典数据
     *
     * @param id 字典数据ID
     * @return 字典数据实体
     */
    DictData getDictDataById(Long id);

    /**
     * 根据字典类型编码获取字典数据列表
     *
     * @param dictType 字典类型编码
     * @return 字典数据列表
     */
    List<DictData> getDictDataByType(String dictType);

    /**
     * 根据字典类型编码获取启用的字典数据列表
     *
     * @param dictType 字典类型编码
     * @return 字典数据列表
     */
    List<DictData> getEnabledDictDataByType(String dictType);

    /**
     * 获取字典数据列表（分页）
     *
     * @param page       页码
     * @param size       每页大小
     * @param dictLabel  字典标签（模糊查询）
     * @param dictValue  字典值（模糊查询）
     * @param dictType   字典类型编码
     * @param status     状态
     * @param orderBy    排序字段
     * @return 分页结果
     */
    IPage<DictData> getDictDataList(Integer page, Integer size,
                                    String dictLabel, String dictValue,
                                    String dictType, Integer status, String orderBy);

    /**
     * 检查字典标签和值在同一个字典类型下是否已存在
     *
     * @param dictLabel 字典标签
     * @param dictValue 字典值
     * @param dictType  字典类型编码
     * @return 是否存在
     */
    boolean isDictLabelExists(String dictLabel, String dictValue, String dictType);

    /**
     * 更新字典数据状态
     *
     * @param id     字典数据ID
     * @param status 状态（0-禁用，1-启用）
     * @return 是否成功
     */
    boolean updateDictDataStatus(Long id, Integer status);

    /**
     * 批量更新字典数据状态
     *
     * @param ids    字典数据ID列表
     * @param status 状态（0-禁用，1-启用）
     * @return 是否成功
     */
    boolean batchUpdateDictDataStatus(List<Long> ids, Integer status);

    /**
     * 设置字典数据为默认
     *
     * @param id 字典数据ID
     * @return 是否成功
     */
    boolean setDictDataAsDefault(Long id);

    /**
     * 根据字典类型编码和字典值获取字典数据
     *
     * @param dictType  字典类型编码
     * @param dictValue 字典值
     * @return 字典数据实体
     */
    DictData getDictDataByTypeAndValue(String dictType, String dictValue);

    /**
     * 根据字典类型编码和字典标签获取字典数据
     *
     * @param dictType  字典类型编码
     * @param dictLabel 字典标签
     * @return 字典数据实体
     */
    DictData getDictDataByTypeAndLabel(String dictType, String dictLabel);
}
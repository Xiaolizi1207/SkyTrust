package com.skytrust.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.skytrust.entity.DictType;

import java.util.List;

/**
 * 字典类型服务接口
 *
 * @author SkyTrust Team
 */
public interface DictTypeService extends com.baomidou.mybatisplus.extension.service.IService<DictType> {

    /**
     * 创建字典类型
     *
     * @param dictType 字典类型实体
     * @return 是否成功
     */
    boolean createDictType(DictType dictType);

    /**
     * 更新字典类型
     *
     * @param dictType 字典类型实体
     * @return 是否成功
     */
    boolean updateDictType(DictType dictType);

    /**
     * 删除字典类型（逻辑删除）
     *
     * @param id 字典类型ID
     * @return 是否成功
     */
    boolean deleteDictType(Long id);

    /**
     * 批量删除字典类型
     *
     * @param ids 字典类型ID列表
     * @return 是否成功
     */
    boolean batchDeleteDictTypes(List<Long> ids);

    /**
     * 根据ID获取字典类型
     *
     * @param id 字典类型ID
     * @return 字典类型实体
     */
    DictType getDictTypeById(Long id);

    /**
     * 根据类型编码获取字典类型
     *
     * @param typeCode 类型编码
     * @return 字典类型实体
     */
    DictType getDictTypeByCode(String typeCode);

    /**
     * 获取字典类型列表（分页）
     *
     * @param page       页码
     * @param size       每页大小
     * @param typeCode   类型编码（模糊查询）
     * @param typeName   类型名称（模糊查询）
     * @param status     状态
     * @param orderBy    排序字段
     * @return 分页结果
     */
    IPage<DictType> getDictTypeList(Integer page, Integer size,
                                    String typeCode, String typeName,
                                    Integer status, String orderBy);

    /**
     * 获取所有启用的字典类型列表
     *
     * @return 字典类型列表
     */
    List<DictType> getAllEnabledDictTypes();

    /**
     * 检查类型编码是否已存在
     *
     * @param typeCode 类型编码
     * @return 是否存在
     */
    boolean isTypeCodeExists(String typeCode);

    /**
     * 更新字典类型状态
     *
     * @param id     字典类型ID
     * @param status 状态（0-禁用，1-启用）
     * @return 是否成功
     */
    boolean updateDictTypeStatus(Long id, Integer status);

    /**
     * 批量更新字典类型状态
     *
     * @param ids    字典类型ID列表
     * @param status 状态（0-禁用，1-启用）
     * @return 是否成功
     */
    boolean batchUpdateDictTypeStatus(List<Long> ids, Integer status);
}
package com.skytrust.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.skytrust.common.utils.StringUtil;
import com.skytrust.entity.DictType;
import com.skytrust.mapper.DictTypeMapper;
import com.skytrust.service.DictTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 字典类型服务实现类
 *
 * @author SkyTrust Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DictTypeServiceImpl extends BaseService<DictTypeMapper, DictType> implements DictTypeService {

    private final DictTypeMapper dictTypeMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createDictType(DictType dictType) {
        log.info("创建字典类型: typeCode={}, typeName={}", dictType.getTypeCode(), dictType.getTypeName());

        // 检查类型编码是否已存在
        if (isTypeCodeExists(dictType.getTypeCode())) {
            throw new IllegalArgumentException("字典类型编码已存在: " + dictType.getTypeCode());
        }

        // 设置创建时间和更新时间
        dictType.setCreateTime(LocalDateTime.now());
        dictType.setUpdateTime(LocalDateTime.now());
        dictType.setDeleted(false);

        return dictTypeMapper.insert(dictType) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDictType(DictType dictType) {
        log.info("更新字典类型: id={}, typeCode={}", dictType.getId(), dictType.getTypeCode());

        DictType existingDictType = dictTypeMapper.selectById(dictType.getId());
        if (existingDictType == null) {
            throw new IllegalArgumentException("字典类型不存在: id=" + dictType.getId());
        }

        // 如果类型编码有变化，检查新编码是否已存在
        if (!existingDictType.getTypeCode().equals(dictType.getTypeCode())) {
            if (isTypeCodeExists(dictType.getTypeCode())) {
                throw new IllegalArgumentException("字典类型编码已存在: " + dictType.getTypeCode());
            }
        }

        dictType.setUpdateTime(LocalDateTime.now());

        return dictTypeMapper.updateById(dictType) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteDictType(Long id) {
        log.info("删除字典类型: id={}", id);

        DictType dictType = dictTypeMapper.selectById(id);
        if (dictType == null) {
            throw new IllegalArgumentException("字典类型不存在: id=" + id);
        }

        // 检查是否有字典数据关联
        // TODO: 可以添加检查，如果有字典数据关联则不允许删除，或者级联删除

        // 逻辑删除
        dictType.setDeleted(true);
        dictType.setUpdateTime(LocalDateTime.now());

        return dictTypeMapper.updateById(dictType) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDeleteDictTypes(List<Long> ids) {
        log.info("批量删除字典类型: ids={}", ids);

        boolean success = true;
        for (Long id : ids) {
            try {
                if (!deleteDictType(id)) {
                    success = false;
                }
            } catch (Exception e) {
                log.error("删除字典类型失败: id={}", id, e);
                success = false;
            }
        }
        return success;
    }

    @Override
    public DictType getDictTypeById(Long id) {
        log.debug("根据ID获取字典类型: id={}", id);
        return dictTypeMapper.selectById(id);
    }

    @Override
    public DictType getDictTypeByCode(String typeCode) {
        log.debug("根据类型编码获取字典类型: typeCode={}", typeCode);

        LambdaQueryWrapper<DictType> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DictType::getTypeCode, typeCode);
        wrapper.eq(DictType::getDeleted, false);
        return dictTypeMapper.selectOne(wrapper);
    }

    @Override
    public IPage<DictType> getDictTypeList(Integer page, Integer size,
                                           String typeCode, String typeName,
                                           Integer status, String orderBy) {
        log.debug("获取字典类型列表: page={}, size={}, typeCode={}, typeName={}, status={}",
                page, size, typeCode, typeName, status);

        Page<DictType> pageObj = getPage(page, size);
        LambdaQueryWrapper<DictType> wrapper = new LambdaQueryWrapper<>();

        // 构建查询条件
        if (StringUtil.isNotEmpty(typeCode)) {
            wrapper.like(DictType::getTypeCode, typeCode);
        }
        if (StringUtil.isNotEmpty(typeName)) {
            wrapper.like(DictType::getTypeName, typeName);
        }
        if (status != null) {
            wrapper.eq(DictType::getStatus, status);
        }
        wrapper.eq(DictType::getDeleted, false);

        // 应用排序
        applyOrderBy(wrapper, orderBy);

        return dictTypeMapper.selectPage(pageObj, wrapper);
    }

    @Override
    public List<DictType> getAllEnabledDictTypes() {
        log.debug("获取所有启用的字典类型列表");

        LambdaQueryWrapper<DictType> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DictType::getStatus, 1);
        wrapper.eq(DictType::getDeleted, false);
        wrapper.orderByAsc(DictType::getSortOrder);

        return dictTypeMapper.selectList(wrapper);
    }

    @Override
    public boolean isTypeCodeExists(String typeCode) {
        if (StringUtil.isEmpty(typeCode)) {
            return false;
        }

        LambdaQueryWrapper<DictType> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DictType::getTypeCode, typeCode);
        wrapper.eq(DictType::getDeleted, false);
        return dictTypeMapper.selectCount(wrapper) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDictTypeStatus(Long id, Integer status) {
        log.info("更新字典类型状态: id={}, status={}", id, status);

        DictType dictType = dictTypeMapper.selectById(id);
        if (dictType == null) {
            throw new IllegalArgumentException("字典类型不存在: id=" + id);
        }

        dictType.setStatus(status);
        dictType.setUpdateTime(LocalDateTime.now());

        return dictTypeMapper.updateById(dictType) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchUpdateDictTypeStatus(List<Long> ids, Integer status) {
        log.info("批量更新字典类型状态: ids={}, status={}", ids, status);

        boolean success = true;
        for (Long id : ids) {
            try {
                if (!updateDictTypeStatus(id, status)) {
                    success = false;
                }
            } catch (Exception e) {
                log.error("更新字典类型状态失败: id={}", id, e);
                success = false;
            }
        }
        return success;
    }

    @Override
    public String validate(DictType dictType) {
        // 调用父类的基础验证
        String baseValidation = super.validate(dictType);
        if (baseValidation != null) {
            return baseValidation;
        }

        // 验证类型编码
        if (StringUtil.isEmpty(dictType.getTypeCode())) {
            return "字典类型编码不能为空";
        }

        // 验证类型名称
        if (StringUtil.isEmpty(dictType.getTypeName())) {
            return "字典类型名称不能为空";
        }

        // 验证状态
        if (dictType.getStatus() != null && dictType.getStatus() != 0 && dictType.getStatus() != 1) {
            return "状态值必须是0或1";
        }

        return null;
    }
}
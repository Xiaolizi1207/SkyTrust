package com.skytrust.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.skytrust.common.utils.StringUtil;
import com.skytrust.entity.DictData;
import com.skytrust.mapper.DictDataMapper;
import com.skytrust.service.DictDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 字典数据服务实现类
 *
 * @author SkyTrust Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DictDataServiceImpl extends BaseService<DictDataMapper, DictData> implements DictDataService {

    private final DictDataMapper dictDataMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createDictData(DictData dictData) {
        log.info("创建字典数据: dictLabel={}, dictValue={}, dictType={}",
                dictData.getDictLabel(), dictData.getDictValue(), dictData.getDictType());

        // 检查字典标签和值在同一个字典类型下是否已存在
        if (isDictLabelExists(dictData.getDictLabel(), dictData.getDictValue(), dictData.getDictType())) {
            throw new IllegalArgumentException("字典标签或值已存在于该字典类型下");
        }

        // 设置创建时间和更新时间
        dictData.setCreateTime(LocalDateTime.now());
        dictData.setUpdateTime(LocalDateTime.now());
        dictData.setDeleted(false);

        // 如果是默认项，需要将该字典类型下的其他项设置为非默认
        if (dictData.getIsDefault() != null && dictData.getIsDefault() == 1) {
            clearDefaultForDictType(dictData.getDictType());
        }

        return dictDataMapper.insert(dictData) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDictData(DictData dictData) {
        log.info("更新字典数据: id={}, dictLabel={}", dictData.getId(), dictData.getDictLabel());

        DictData existingDictData = dictDataMapper.selectById(dictData.getId());
        if (existingDictData == null) {
            throw new IllegalArgumentException("字典数据不存在: id=" + dictData.getId());
        }

        // 如果字典类型、标签或值有变化，检查是否已存在
        if (!existingDictData.getDictType().equals(dictData.getDictType()) ||
                !existingDictData.getDictLabel().equals(dictData.getDictLabel()) ||
                !existingDictData.getDictValue().equals(dictData.getDictValue())) {
            if (isDictLabelExists(dictData.getDictLabel(), dictData.getDictValue(), dictData.getDictType())) {
                throw new IllegalArgumentException("字典标签或值已存在于该字典类型下");
            }
        }

        // 如果是默认项，需要将该字典类型下的其他项设置为非默认
        if (dictData.getIsDefault() != null && dictData.getIsDefault() == 1) {
            clearDefaultForDictType(dictData.getDictType());
        }

        dictData.setUpdateTime(LocalDateTime.now());

        return dictDataMapper.updateById(dictData) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteDictData(Long id) {
        log.info("删除字典数据: id={}", id);

        DictData dictData = dictDataMapper.selectById(id);
        if (dictData == null) {
            throw new IllegalArgumentException("字典数据不存在: id=" + id);
        }

        // 逻辑删除
        dictData.setDeleted(true);
        dictData.setUpdateTime(LocalDateTime.now());

        return dictDataMapper.updateById(dictData) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDeleteDictDatas(List<Long> ids) {
        log.info("批量删除字典数据: ids={}", ids);

        boolean success = true;
        for (Long id : ids) {
            try {
                if (!deleteDictData(id)) {
                    success = false;
                }
            } catch (Exception e) {
                log.error("删除字典数据失败: id={}", id, e);
                success = false;
            }
        }
        return success;
    }

    @Override
    public DictData getDictDataById(Long id) {
        log.debug("根据ID获取字典数据: id={}", id);
        return dictDataMapper.selectById(id);
    }

    @Override
    public List<DictData> getDictDataByType(String dictType) {
        log.debug("根据字典类型编码获取字典数据列表: dictType={}", dictType);

        LambdaQueryWrapper<DictData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DictData::getDictType, dictType);
        wrapper.eq(DictData::getDeleted, false);
        wrapper.orderByAsc(DictData::getSortOrder);

        return dictDataMapper.selectList(wrapper);
    }

    @Override
    public List<DictData> getEnabledDictDataByType(String dictType) {
        log.debug("根据字典类型编码获取启用的字典数据列表: dictType={}", dictType);

        LambdaQueryWrapper<DictData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DictData::getDictType, dictType);
        wrapper.eq(DictData::getStatus, 1);
        wrapper.eq(DictData::getDeleted, false);
        wrapper.orderByAsc(DictData::getSortOrder);

        return dictDataMapper.selectList(wrapper);
    }

    @Override
    public IPage<DictData> getDictDataList(Integer page, Integer size,
                                           String dictLabel, String dictValue,
                                           String dictType, Integer status, String orderBy) {
        log.debug("获取字典数据列表: page={}, size={}, dictLabel={}, dictValue={}, dictType={}, status={}",
                page, size, dictLabel, dictValue, dictType, status);

        Page<DictData> pageObj = getPage(page, size);
        QueryWrapper<DictData> wrapper = new QueryWrapper<>();

        // 构建查询条件
        if (StringUtil.isNotEmpty(dictLabel)) {
            wrapper.like(getColumn("dictLabel"), dictLabel);
        }
        if (StringUtil.isNotEmpty(dictValue)) {
            wrapper.like(getColumn("dictValue"), dictValue);
        }
        if (StringUtil.isNotEmpty(dictType)) {
            wrapper.eq(getColumn("dictType"), dictType);
        }
        if (status != null) {
            wrapper.eq(getColumn("status"), status);
        }
        wrapper.eq(getColumn("deleted"), false);

        // 应用排序
        applyOrderBy(wrapper, orderBy);

        return dictDataMapper.selectPage(pageObj, wrapper);
    }

    @Override
    public boolean isDictLabelExists(String dictLabel, String dictValue, String dictType) {
        if (StringUtil.isEmpty(dictLabel) || StringUtil.isEmpty(dictType)) {
            return false;
        }

        LambdaQueryWrapper<DictData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DictData::getDictType, dictType);
        wrapper.and(qw -> qw.eq(DictData::getDictLabel, dictLabel).or().eq(DictData::getDictValue, dictValue));
        wrapper.eq(DictData::getDeleted, false);
        return dictDataMapper.selectCount(wrapper) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDictDataStatus(Long id, Integer status) {
        log.info("更新字典数据状态: id={}, status={}", id, status);

        DictData dictData = dictDataMapper.selectById(id);
        if (dictData == null) {
            throw new IllegalArgumentException("字典数据不存在: id=" + id);
        }

        dictData.setStatus(status);
        dictData.setUpdateTime(LocalDateTime.now());

        return dictDataMapper.updateById(dictData) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchUpdateDictDataStatus(List<Long> ids, Integer status) {
        log.info("批量更新字典数据状态: ids={}, status={}", ids, status);

        boolean success = true;
        for (Long id : ids) {
            try {
                if (!updateDictDataStatus(id, status)) {
                    success = false;
                }
            } catch (Exception e) {
                log.error("更新字典数据状态失败: id={}", id, e);
                success = false;
            }
        }
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setDictDataAsDefault(Long id) {
        log.info("设置字典数据为默认: id={}", id);

        DictData dictData = dictDataMapper.selectById(id);
        if (dictData == null) {
            throw new IllegalArgumentException("字典数据不存在: id=" + id);
        }

        // 先将该字典类型下的所有项设置为非默认
        clearDefaultForDictType(dictData.getDictType());

        // 设置该项为默认
        dictData.setIsDefault(1);
        dictData.setUpdateTime(LocalDateTime.now());

        return dictDataMapper.updateById(dictData) > 0;
    }

    @Override
    public DictData getDictDataByTypeAndValue(String dictType, String dictValue) {
        log.debug("根据字典类型编码和字典值获取字典数据: dictType={}, dictValue={}", dictType, dictValue);

        LambdaQueryWrapper<DictData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DictData::getDictType, dictType);
        wrapper.eq(DictData::getDictValue, dictValue);
        wrapper.eq(DictData::getDeleted, false);
        wrapper.eq(DictData::getStatus, 1);

        return dictDataMapper.selectOne(wrapper);
    }

    @Override
    public DictData getDictDataByTypeAndLabel(String dictType, String dictLabel) {
        log.debug("根据字典类型编码和字典标签获取字典数据: dictType={}, dictLabel={}", dictType, dictLabel);

        LambdaQueryWrapper<DictData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DictData::getDictType, dictType);
        wrapper.eq(DictData::getDictLabel, dictLabel);
        wrapper.eq(DictData::getDeleted, false);
        wrapper.eq(DictData::getStatus, 1);

        return dictDataMapper.selectOne(wrapper);
    }

    @Override
    public String validate(DictData dictData) {
        // 调用父类的基础验证
        String baseValidation = super.validate(dictData);
        if (baseValidation != null) {
            return baseValidation;
        }

        // 验证字典标签
        if (StringUtil.isEmpty(dictData.getDictLabel())) {
            return "字典标签不能为空";
        }

        // 验证字典值
        if (StringUtil.isEmpty(dictData.getDictValue())) {
            return "字典值不能为空";
        }

        // 验证字典类型编码
        if (StringUtil.isEmpty(dictData.getDictType())) {
            return "字典类型编码不能为空";
        }

        // 验证状态
        if (dictData.getStatus() != null && dictData.getStatus() != 0 && dictData.getStatus() != 1) {
            return "状态值必须是0或1";
        }

        // 验证是否默认
        if (dictData.getIsDefault() != null && dictData.getIsDefault() != 0 && dictData.getIsDefault() != 1) {
            return "是否默认值必须是0或1";
        }

        return null;
    }

    /**
     * 清除指定字典类型下的所有字典数据的默认标志
     *
     * @param dictType 字典类型编码
     */
    private void clearDefaultForDictType(String dictType) {
        LambdaQueryWrapper<DictData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DictData::getDictType, dictType);
        wrapper.eq(DictData::getDeleted, false);
        wrapper.eq(DictData::getIsDefault, 1);

        DictData dictData = new DictData();
        dictData.setIsDefault(0);
        dictData.setUpdateTime(LocalDateTime.now());

        dictDataMapper.update(dictData, wrapper);
    }
}
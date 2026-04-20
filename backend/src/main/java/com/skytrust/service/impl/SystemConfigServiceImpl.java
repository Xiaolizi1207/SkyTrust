package com.skytrust.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.skytrust.entity.SystemConfig;
import com.skytrust.mapper.SystemConfigMapper;
import com.skytrust.service.SystemConfigService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统配置服务实现类
 *
 * @author SkyTrust Team
 */
@Service
public class SystemConfigServiceImpl extends BaseService<SystemConfigMapper, SystemConfig> implements SystemConfigService {

    @Override
    public SystemConfig getByConfigKey(String configKey) {
        if (configKey == null || configKey.trim().isEmpty()) {
            return null;
        }
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getConfigKey, configKey.trim());
        return getOne(wrapper);
    }

    @Override
    public List<SystemConfig> getByConfigType(Integer configType) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        if (configType != null) {
            wrapper.eq(SystemConfig::getConfigType, configType);
        }
        wrapper.orderByAsc(SystemConfig::getSortOrder)
                .orderByAsc(SystemConfig::getConfigKey);
        return list(wrapper);
    }

    @Override
    public List<SystemConfig> getByConfigGroup(String configGroup) {
        if (configGroup == null || configGroup.trim().isEmpty()) {
            return listAll();
        }
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getConfigGroup, configGroup.trim())
                .orderByAsc(SystemConfig::getSortOrder)
                .orderByAsc(SystemConfig::getConfigKey);
        return list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateConfigValue(String configKey, String configValue, Long lastModifiedBy) {
        if (configKey == null || configKey.trim().isEmpty()) {
            return false;
        }

        SystemConfig config = getByConfigKey(configKey);
        if (config == null) {
            // 配置不存在，创建新配置（默认可修改）
            config = new SystemConfig();
            config.setConfigKey(configKey.trim());
            config.setConfigValue(configValue);
            config.setConfigType(0); // 默认系统配置
            config.setModifiable(true);
            config.setLastModifiedTime(LocalDateTime.now());
            config.setLastModifiedBy(lastModifiedBy);
            return save(config);
        }

        // 检查是否可修改
        if (config.getModifiable() != null && !config.getModifiable()) {
            throw new IllegalArgumentException("配置[" + configKey + "]不可修改");
        }

        // 更新配置值
        config.setConfigValue(configValue);
        config.setLastModifiedTime(LocalDateTime.now());
        config.setLastModifiedBy(lastModifiedBy);
        return updateById(config);
    }

    @Override
    public List<String> getAllConfigGroups() {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(SystemConfig::getConfigGroup)
                .isNotNull(SystemConfig::getConfigGroup)
                .ne(SystemConfig::getConfigGroup, "")
                .groupBy(SystemConfig::getConfigGroup)
                .orderByAsc(SystemConfig::getConfigGroup);

        return list(wrapper).stream()
                .map(SystemConfig::getConfigGroup)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteConfig(String configKey) {
        if (configKey == null || configKey.trim().isEmpty()) {
            return false;
        }

        SystemConfig config = getByConfigKey(configKey);
        if (config == null) {
            return false;
        }

        // 检查是否可删除（可修改的配置才允许删除）
        if (config.getModifiable() != null && !config.getModifiable()) {
            throw new IllegalArgumentException("配置[" + configKey + "]不可删除");
        }

        return removeById(config.getId());
    }
}
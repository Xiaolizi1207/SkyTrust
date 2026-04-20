package com.skytrust.service;

import com.skytrust.entity.SystemConfig;

/**
 * 系统配置服务接口
 *
 * @author SkyTrust Team
 */
public interface SystemConfigService extends IService<SystemConfig> {

    /**
     * 根据配置键查询配置
     *
     * @param configKey 配置键
     * @return 配置信息
     */
    SystemConfig getByConfigKey(String configKey);

    /**
     * 根据配置类型查询配置列表
     *
     * @param configType 配置类型（0-系统配置，1-业务配置，2-区块链配置，3-AI配置，4-物联网配置）
     * @return 配置列表
     */
    java.util.List<SystemConfig> getByConfigType(Integer configType);

    /**
     * 根据配置分组查询配置列表
     *
     * @param configGroup 配置分组
     * @return 配置列表
     */
    java.util.List<SystemConfig> getByConfigGroup(String configGroup);

    /**
     * 根据配置键更新配置值
     *
     * @param configKey 配置键
     * @param configValue 配置值
     * @param lastModifiedBy 最后修改人
     * @return 是否更新成功
     */
    boolean updateConfigValue(String configKey, String configValue, Long lastModifiedBy);

    /**
     * 获取所有配置分组
     *
     * @return 分组列表
     */
    java.util.List<String> getAllConfigGroups();

    /**
     * 根据配置键删除配置（仅删除可修改的配置）
     *
     * @param configKey 配置键
     * @return 是否删除成功
     */
    boolean deleteConfig(String configKey);
}
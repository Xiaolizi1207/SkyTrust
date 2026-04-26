package com.skytrust.controller;

import com.skytrust.common.Result;
import com.skytrust.common.ResultCode;
import com.skytrust.dto.SystemConfigDTO;
import com.skytrust.entity.SystemConfig;
import com.skytrust.service.SystemConfigService;
import com.skytrust.vo.SystemConfigVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统配置控制器
 *
 * @author SkyTrust Team
 */
@Tag(name = "系统配置管理", description = "系统配置管理接口")
@Validated
@RestController
@RequestMapping("/api/system-configs")
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    public SystemConfigController(SystemConfigService systemConfigService) {
        this.systemConfigService = systemConfigService;
    }

    /**
     * 创建系统配置
     */
    @Operation(summary = "创建系统配置")
    @PostMapping
    public Result<SystemConfigVO> createSystemConfig(@Valid @RequestBody SystemConfigDTO configDTO) {
        // 检查配置键是否已存在
        SystemConfig existingConfig = systemConfigService.getByConfigKey(configDTO.getConfigKey());
        if (existingConfig != null) {
            return Result.error(ResultCode.DATA_DUPLICATE.getCode(), "配置键已存在");
        }

        // 转换DTO为实体
        SystemConfig config = new SystemConfig();
        BeanUtils.copyProperties(configDTO, config);

        // 保存配置
        boolean saved = systemConfigService.save(config);
        if (!saved) {
            return Result.error("系统配置创建失败");
        }

        SystemConfigVO configVO = convertToVO(config);
        return Result.success(configVO, "系统配置创建成功");
    }

    /**
     * 更新系统配置信息
     */
    @Operation(summary = "更新系统配置信息")
    @PutMapping("/{id}")
    public Result<SystemConfigVO> updateSystemConfig(
            @Parameter(description = "配置ID", required = true) @PathVariable Long id,
            @Valid @RequestBody SystemConfigDTO configDTO,
            @Parameter(description = "最后修改人ID", required = true) @RequestParam Long lastModifiedBy) {
        SystemConfig config = systemConfigService.getById(id);
        if (config == null) {
            return Result.error(ResultCode.DATA_NOT_EXIST.getCode(), "系统配置不存在");
        }

        // 检查配置是否可修改
        if (config.getModifiable() != null && !config.getModifiable()) {
            return Result.error("该配置不可修改");
        }

        // 检查配置键是否已被其他配置使用
        if (!config.getConfigKey().equals(configDTO.getConfigKey())) {
            SystemConfig existingConfig = systemConfigService.getByConfigKey(configDTO.getConfigKey());
            if (existingConfig != null && !existingConfig.getId().equals(id)) {
                return Result.error(ResultCode.DATA_DUPLICATE.getCode(), "配置键已被其他配置使用");
            }
        }

        // 更新字段
        BeanUtils.copyProperties(configDTO, config, "id", "createTime", "updateTime");
        config.setLastModifiedBy(lastModifiedBy);

        boolean updated = systemConfigService.updateById(config);
        if (!updated) {
            return Result.error("系统配置更新失败");
        }

        SystemConfigVO configVO = convertToVO(config);
        return Result.success(configVO, "系统配置更新成功");
    }

    /**
     * 获取系统配置详情
     */
    @Operation(summary = "获取系统配置详情")
    @GetMapping("/{id}")
    public Result<SystemConfigVO> getSystemConfigById(@Parameter(description = "配置ID", required = true) @PathVariable Long id) {
        SystemConfig config = systemConfigService.getById(id);
        if (config == null) {
            return Result.error(ResultCode.DATA_NOT_EXIST.getCode(), "系统配置不存在");
        }
        return Result.success(convertToVO(config));
    }

    /**
     * 删除系统配置（逻辑删除）
     */
    @Operation(summary = "删除系统配置")
    @DeleteMapping("/{id}")
    public Result<Void> deleteSystemConfig(@Parameter(description = "配置ID", required = true) @PathVariable Long id) {
        boolean deleted = systemConfigService.logicRemoveById(id);
        if (!deleted) {
            return Result.error("系统配置删除失败");
        }
        return Result.success("系统配置删除成功");
    }

    /**
     * 分页查询系统配置列表
     */
    @Operation(summary = "分页查询系统配置列表")
    @GetMapping
    public Result<List<SystemConfigVO>> getSystemConfigList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "配置类型") @RequestParam(required = false) Integer configType,
            @Parameter(description = "配置分组") @RequestParam(required = false) String configGroup,
            @Parameter(description = "配置键") @RequestParam(required = false) String configKey,
            @Parameter(description = "是否可修改") @RequestParam(required = false) Boolean modifiable) {

        // 简化处理：使用Service的list方法
        List<SystemConfig> configs = systemConfigService.list();

        // 应用过滤条件
        List<SystemConfig> filteredConfigs = configs.stream()
                .filter(config -> configType == null || config.getConfigType().equals(configType))
                .filter(config -> configGroup == null || config.getConfigGroup().contains(configGroup))
                .filter(config -> configKey == null || config.getConfigKey().contains(configKey))
                .filter(config -> modifiable == null || config.getModifiable().equals(modifiable))
                .skip((page - 1) * (long) size)
                .limit(size)
                .collect(Collectors.toList());

        List<SystemConfigVO> configVOs = filteredConfigs.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return Result.success(configVOs);
    }

    /**
     * 根据配置键查询配置
     */
    @Operation(summary = "根据配置键查询配置")
    @GetMapping("/key/{configKey}")
    public Result<SystemConfigVO> getSystemConfigByKey(@Parameter(description = "配置键", required = true) @PathVariable String configKey) {
        SystemConfig config = systemConfigService.getByConfigKey(configKey);
        if (config == null) {
            return Result.error(ResultCode.DATA_NOT_EXIST.getCode(), "系统配置不存在");
        }
        return Result.success(convertToVO(config));
    }

    /**
     * 根据配置类型查询配置列表
     */
    @Operation(summary = "根据配置类型查询配置列表")
    @GetMapping("/type/{configType}")
    public Result<List<SystemConfigVO>> getSystemConfigsByType(@Parameter(description = "配置类型", required = true) @PathVariable Integer configType) {
        List<SystemConfig> configs = systemConfigService.getByConfigType(configType);
        List<SystemConfigVO> configVOs = configs.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        return Result.success(configVOs);
    }

    /**
     * 根据配置分组查询配置列表
     */
    @Operation(summary = "根据配置分组查询配置列表")
    @GetMapping("/group/{configGroup}")
    public Result<List<SystemConfigVO>> getSystemConfigsByGroup(@Parameter(description = "配置分组", required = true) @PathVariable String configGroup) {
        List<SystemConfig> configs = systemConfigService.getByConfigGroup(configGroup);
        List<SystemConfigVO> configVOs = configs.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        return Result.success(configVOs);
    }

    /**
     * 根据配置键更新配置值
     */
    @Operation(summary = "根据配置键更新配置值")
    @PutMapping("/key/{configKey}/value")
    public Result<Void> updateConfigValueByKey(
            @Parameter(description = "配置键", required = true) @PathVariable String configKey,
            @Parameter(description = "配置值", required = true) @RequestParam String configValue,
            @Parameter(description = "最后修改人ID", required = true) @RequestParam Long lastModifiedBy) {

        boolean updated = systemConfigService.updateConfigValue(configKey, configValue, lastModifiedBy);
        if (!updated) {
            return Result.error("系统配置更新失败");
        }
        return Result.success("系统配置更新成功");
    }

    /**
     * 获取所有配置分组
     */
    @Operation(summary = "获取所有配置分组")
    @GetMapping("/groups")
    public Result<List<String>> getAllConfigGroups() {
        List<String> groups = systemConfigService.getAllConfigGroups();
        return Result.success(groups);
    }

    /**
     * 根据配置键删除配置
     */
    @Operation(summary = "根据配置键删除配置")
    @DeleteMapping("/key/{configKey}")
    public Result<Void> deleteConfigByKey(@Parameter(description = "配置键", required = true) @PathVariable String configKey) {
        boolean deleted = systemConfigService.deleteConfig(configKey);
        if (!deleted) {
            return Result.error("系统配置删除失败");
        }
        return Result.success("系统配置删除成功");
    }

    /**
     * 将SystemConfig实体转换为SystemConfigVO
     */
    private SystemConfigVO convertToVO(SystemConfig config) {
        if (config == null) {
            return null;
        }
        SystemConfigVO configVO = new SystemConfigVO();
        BeanUtils.copyProperties(config, configVO);
        return configVO;
    }
}
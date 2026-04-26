package com.skytrust.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.*;
import lombok.Data;

/**
 * 系统配置数据传输对象（用于创建或更新系统配置）
 *
 * @author SkyTrust Team
 */
@Data
@Schema(description = "系统配置数据传输对象")
public class SystemConfigDTO {

    @Schema(description = "配置键（唯一）", example = "app.name", required = true)
    @NotBlank(message = "配置键不能为空")
    @Size(max = 100, message = "配置键长度不能超过100个字符")
    private String configKey;

    @Schema(description = "配置值", example = "SkyTrust无人机租赁平台", required = true)
    @NotBlank(message = "配置值不能为空")
    @Size(max = 2000, message = "配置值长度不能超过2000个字符")
    private String configValue;

    @Schema(description = "配置类型（0-系统配置，1-业务配置，2-区块链配置，3-AI配置，4-物联网配置）", example = "0", required = true)
    @NotNull(message = "配置类型不能为空")
    private Integer configType;

    @Schema(description = "配置描述", example = "应用名称配置")
    @Size(max = 500, message = "配置描述长度不能超过500个字符")
    private String description;

    @Schema(description = "是否可修改（0-否，1-是）", example = "true", required = true)
    @NotNull(message = "是否可修改不能为空")
    private Boolean modifiable;

    @Schema(description = "配置分组", example = "app")
    @Size(max = 50, message = "配置分组长度不能超过50个字符")
    private String configGroup;

    @Schema(description = "排序号", example = "1", required = true)
    @NotNull(message = "排序号不能为空")
    private Integer sortOrder;

    @Schema(description = "备注", example = "系统基础配置")
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;
}
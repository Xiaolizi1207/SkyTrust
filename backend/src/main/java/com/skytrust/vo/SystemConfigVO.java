package com.skytrust.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 系统配置视图对象（用于返回系统配置信息）
 *
 * @author SkyTrust Team
 */
@Data
@Schema(description = "系统配置视图对象")
public class SystemConfigVO {

    @Schema(description = "配置ID", example = "1")
    private Long id;

    @Schema(description = "配置键（唯一）", example = "app.name")
    private String configKey;

    @Schema(description = "配置值", example = "SkyTrust无人机租赁平台")
    private String configValue;

    @Schema(description = "配置类型（0-系统配置，1-业务配置，2-区块链配置，3-AI配置，4-物联网配置）", example = "0")
    private Integer configType;

    @Schema(description = "配置描述", example = "应用名称配置")
    private String description;

    @Schema(description = "是否可修改（0-否，1-是）", example = "true")
    private Boolean modifiable;

    @Schema(description = "配置分组", example = "app")
    private String configGroup;

    @Schema(description = "排序号", example = "1")
    private Integer sortOrder;

    @Schema(description = "最后修改时间", example = "2023-10-01T10:00:00")
    private LocalDateTime lastModifiedTime;

    @Schema(description = "最后修改人", example = "1")
    private Long lastModifiedBy;

    @Schema(description = "备注", example = "系统基础配置")
    private String remark;

    @Schema(description = "创建时间", example = "2023-10-01T10:00:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2023-10-01T10:00:00")
    private LocalDateTime updateTime;
}
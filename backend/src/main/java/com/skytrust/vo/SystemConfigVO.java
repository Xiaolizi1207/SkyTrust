package com.skytrust.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 系统配置视图对象（用于返回系统配置信息）
 *
 * @author SkyTrust Team
 */
@Data
@ApiModel(description = "系统配置视图对象")
public class SystemConfigVO {

    @ApiModelProperty(value = "配置ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "配置键（唯一）", example = "app.name")
    private String configKey;

    @ApiModelProperty(value = "配置值", example = "SkyTrust无人机租赁平台")
    private String configValue;

    @ApiModelProperty(value = "配置类型（0-系统配置，1-业务配置，2-区块链配置，3-AI配置，4-物联网配置）", example = "0")
    private Integer configType;

    @ApiModelProperty(value = "配置描述", example = "应用名称配置")
    private String description;

    @ApiModelProperty(value = "是否可修改（0-否，1-是）", example = "true")
    private Boolean modifiable;

    @ApiModelProperty(value = "配置分组", example = "app")
    private String configGroup;

    @ApiModelProperty(value = "排序号", example = "1")
    private Integer sortOrder;

    @ApiModelProperty(value = "最后修改时间", example = "2023-10-01T10:00:00")
    private LocalDateTime lastModifiedTime;

    @ApiModelProperty(value = "最后修改人", example = "1")
    private Long lastModifiedBy;

    @ApiModelProperty(value = "备注", example = "系统基础配置")
    private String remark;

    @ApiModelProperty(value = "创建时间", example = "2023-10-01T10:00:00")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间", example = "2023-10-01T10:00:00")
    private LocalDateTime updateTime;
}
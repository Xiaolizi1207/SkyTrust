package com.skytrust.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 系统配置实体类
 *
 * @author SkyTrust Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("system_config")
public class SystemConfig extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 配置键（唯一）
     */
    @TableField(value = "config_key")
    private String configKey;

    /**
     * 配置值
     */
    @TableField(value = "config_value")
    private String configValue;

    /**
     * 配置类型（0-系统配置，1-业务配置，2-区块链配置，3-AI配置，4-物联网配置）
     */
    @TableField(value = "config_type")
    private Integer configType;

    /**
     * 配置描述
     */
    @TableField(value = "description")
    private String description;

    /**
     * 是否可修改（0-否，1-是）
     */
    @TableField(value = "modifiable")
    private Boolean modifiable;

    /**
     * 最后修改时间
     */
    @TableField(value = "last_modified_time")
    private LocalDateTime lastModifiedTime;

    /**
     * 最后修改人
     */
    @TableField(value = "last_modified_by")
    private Long lastModifiedBy;

    /**
     * 配置分组
     */
    @TableField(value = "config_group")
    private String configGroup;

    /**
     * 排序号
     */
    @TableField(value = "sort_order")
    private Integer sortOrder;

    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;
}
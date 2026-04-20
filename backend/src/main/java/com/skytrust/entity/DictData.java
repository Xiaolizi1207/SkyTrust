package com.skytrust.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典数据实体类
 *
 * @author SkyTrust Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dict_data")
public class DictData extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 字典标签（显示文本）
     */
    @TableField("dict_label")
    private String dictLabel;

    /**
     * 字典值（存储值）
     */
    @TableField("dict_value")
    private String dictValue;

    /**
     * 字典类型编码（关联dict_type.type_code）
     */
    @TableField("dict_type")
    private String dictType;

    /**
     * 状态（0-禁用，1-启用）
     */
    @TableField("status")
    private Integer status = 1;

    /**
     * 排序（数字越小越靠前）
     */
    @TableField("sort_order")
    private Integer sortOrder = 0;

    /**
     * CSS样式类（前端显示用）
     */
    @TableField("css_class")
    private String cssClass;

    /**
     * 列表样式（前端显示用）
     */
    @TableField("list_class")
    private String listClass;

    /**
     * 是否默认（0-否，1-是）
     */
    @TableField("is_default")
    private Integer isDefault = 0;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;
}
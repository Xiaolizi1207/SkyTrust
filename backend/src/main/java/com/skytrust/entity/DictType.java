package com.skytrust.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典类型实体类
 *
 * @author SkyTrust Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dict_type")
public class DictType extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 字典类型编码（唯一）
     */
    @TableField("type_code")
    private String typeCode;

    /**
     * 字典类型名称
     */
    @TableField("type_name")
    private String typeName;

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
     * 备注
     */
    @TableField("remark")
    private String remark;
}
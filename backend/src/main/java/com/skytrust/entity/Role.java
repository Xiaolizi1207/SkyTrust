package com.skytrust.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.skytrust.enums.UserStatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色实体类
 *
 * @author SkyTrust Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class Role extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 角色代码（唯一），与UserRoleEnum保持兼容
     */
    @TableField("role_code")
    private String roleCode;

    /**
     * 角色名称
     */
    @TableField("role_name")
    private String roleName;

    /**
     * 角色描述
     */
    @TableField("description")
    private String description;

    /**
     * 状态（0-禁用，1-启用）
     */
    @TableField("status")
    private Integer status = UserStatusEnum.ENABLED.getCode();

    /**
     * 排序（数字越小越靠前）
     */
    @TableField("sort_order")
    private Integer sortOrder = 0;
}
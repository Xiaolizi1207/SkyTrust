package com.skytrust.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.skytrust.enums.MenuStatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;
import java.util.ArrayList;

/**
 * 菜单实体类
 *
 * @author SkyTrust Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
public class Menu extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 父菜单ID（0表示根菜单）
     */
    @TableField("parent_id")
    private Long parentId = 0L;

    /**
     * 菜单名称
     */
    @TableField("menu_name")
    private String menuName;

    /**
     * 菜单代码（唯一），用于权限标识
     */
    @TableField("menu_code")
    private String menuCode;

    /**
     * 前端路由路径
     */
    @TableField("menu_path")
    private String menuPath;

    /**
     * 组件路径
     */
    @TableField("component")
    private String component;

    /**
     * 菜单图标
     */
    @TableField("icon")
    private String icon;

    /**
     * 菜单类型（1-目录，2-菜单，3-按钮）
     */
    @TableField("menu_type")
    private Integer menuType;

    /**
     * 状态（0-禁用，1-启用）
     */
    @TableField("status")
    private Integer status = MenuStatusEnum.ENABLED.getCode();

    /**
     * 排序（数字越小越靠前）
     */
    @TableField("sort_order")
    private Integer sortOrder = 0;

    /**
     * 权限标识（如sys:user:query）
     */
    @TableField("perms")
    private String perms;

    /**
     * 是否外链（0-否，1-是）
     */
    @TableField("is_external")
    private Integer isExternal = 0;

    /**
     * 外链地址
     */
    @TableField("external_url")
    private String externalUrl;

    /**
     * 是否缓存（0-否，1-是）
     */
    @TableField("is_cache")
    private Integer isCache = 0;

    /**
     * 是否可见（0-隐藏，1-显示）
     */
    @TableField("is_visible")
    private Integer isVisible = 1;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 子菜单列表（非数据库字段，用于树形结构）
     */
    @TableField(exist = false)
    private List<Menu> children = new ArrayList<>();
}
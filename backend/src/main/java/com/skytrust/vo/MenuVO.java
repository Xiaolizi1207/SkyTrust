package com.skytrust.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.skytrust.enums.MenuStatusEnum;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 菜单视图对象
 *
 * @author SkyTrust Team
 */
@Data
@ApiModel(description = "菜单视图对象")
public class MenuVO {

    @ApiModelProperty(value = "菜单ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "父菜单ID（0表示根菜单）", example = "0")
    private Long parentId = 0L;

    @ApiModelProperty(value = "菜单名称", example = "用户管理")
    private String menuName;

    @ApiModelProperty(value = "菜单代码（唯一）", example = "system:user")
    private String menuCode;

    @ApiModelProperty(value = "前端路由路径", example = "/system/user")
    private String menuPath;

    @ApiModelProperty(value = "组件路径", example = "system/user/index")
    private String component;

    @ApiModelProperty(value = "菜单图标", example = "user")
    private String icon;

    @ApiModelProperty(value = "菜单类型（1-目录，2-菜单，3-按钮）", example = "2")
    private Integer menuType;

    @ApiModelProperty(value = "状态（0-禁用，1-启用）", example = "1")
    private Integer status = MenuStatusEnum.ENABLED.getCode();

    @ApiModelProperty(value = "排序（数字越小越靠前）", example = "0")
    private Integer sortOrder = 0;

    @ApiModelProperty(value = "权限标识（如sys:user:query）", example = "system:user:query")
    private String perms;

    @ApiModelProperty(value = "是否外链（0-否，1-是）", example = "0")
    private Integer isExternal = 0;

    @ApiModelProperty(value = "外链地址", example = "https://example.com")
    private String externalUrl;

    @ApiModelProperty(value = "是否缓存（0-否，1-是）", example = "0")
    private Integer isCache = 0;

    @ApiModelProperty(value = "是否可见（0-隐藏，1-显示）", example = "1")
    private Integer isVisible = 1;

    @ApiModelProperty(value = "备注", example = "用户管理菜单")
    private String remark;

    @ApiModelProperty(value = "创建时间", example = "2023-10-01 12:00:00")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间", example = "2023-10-01 12:00:00")
    private LocalDateTime updateTime;
}
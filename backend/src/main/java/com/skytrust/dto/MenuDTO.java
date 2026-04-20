package com.skytrust.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import com.skytrust.enums.MenuStatusEnum;
import lombok.Data;

/**
 * 菜单数据传输对象
 *
 * @author SkyTrust Team
 */
@Data
@ApiModel(description = "菜单数据传输对象")
public class MenuDTO {

    @ApiModelProperty(value = "父菜单ID（0表示根菜单）", example = "0")
    private Long parentId = 0L;

    @ApiModelProperty(value = "菜单名称", example = "用户管理", required = true)
    @NotBlank(message = "菜单名称不能为空")
    @Size(min = 1, max = 100, message = "菜单名称长度必须在1-100个字符之间")
    private String menuName;

    @ApiModelProperty(value = "菜单代码（唯一）", example = "system:user", required = true)
    @NotBlank(message = "菜单代码不能为空")
    @Size(min = 1, max = 100, message = "菜单代码长度必须在1-100个字符之间")
    private String menuCode;

    @ApiModelProperty(value = "前端路由路径", example = "/system/user")
    @Size(max = 200, message = "前端路由路径长度不能超过200个字符")
    private String menuPath;

    @ApiModelProperty(value = "组件路径", example = "system/user/index")
    @Size(max = 200, message = "组件路径长度不能超过200个字符")
    private String component;

    @ApiModelProperty(value = "菜单图标", example = "user")
    @Size(max = 100, message = "菜单图标长度不能超过100个字符")
    private String icon;

    @ApiModelProperty(value = "菜单类型（1-目录，2-菜单，3-按钮）", example = "2", required = true)
    @NotNull(message = "菜单类型不能为空")
    private Integer menuType;

    @ApiModelProperty(value = "状态（0-禁用，1-启用）", example = "1")
    private Integer status = MenuStatusEnum.ENABLED.getCode();

    @ApiModelProperty(value = "排序（数字越小越靠前）", example = "0")
    private Integer sortOrder = 0;

    @ApiModelProperty(value = "权限标识（如sys:user:query）", example = "system:user:query")
    @Size(max = 100, message = "权限标识长度不能超过100个字符")
    private String perms;

    @ApiModelProperty(value = "是否外链（0-否，1-是）", example = "0")
    private Integer isExternal = 0;

    @ApiModelProperty(value = "外链地址", example = "https://example.com")
    @Size(max = 500, message = "外链地址长度不能超过500个字符")
    private String externalUrl;

    @ApiModelProperty(value = "是否缓存（0-否，1-是）", example = "0")
    private Integer isCache = 0;

    @ApiModelProperty(value = "是否可见（0-隐藏，1-显示）", example = "1")
    private Integer isVisible = 1;

    @ApiModelProperty(value = "备注", example = "用户管理菜单")
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;
}
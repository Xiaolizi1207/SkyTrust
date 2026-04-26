package com.skytrust.dto;

import io.swagger.v3.oas.annotations.media.Schema;

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
@Schema(description = "菜单数据传输对象")
public class MenuDTO {

    @Schema(description = "父菜单ID（0表示根菜单）", example = "0")
    private Long parentId = 0L;

    @Schema(description = "菜单名称", example = "用户管理", required = true)
    @NotBlank(message = "菜单名称不能为空")
    @Size(min = 1, max = 100, message = "菜单名称长度必须在1-100个字符之间")
    private String menuName;

    @Schema(description = "菜单代码（唯一）", example = "system:user", required = true)
    @NotBlank(message = "菜单代码不能为空")
    @Size(min = 1, max = 100, message = "菜单代码长度必须在1-100个字符之间")
    private String menuCode;

    @Schema(description = "前端路由路径", example = "/system/user")
    @Size(max = 200, message = "前端路由路径长度不能超过200个字符")
    private String menuPath;

    @Schema(description = "组件路径", example = "system/user/index")
    @Size(max = 200, message = "组件路径长度不能超过200个字符")
    private String component;

    @Schema(description = "菜单图标", example = "user")
    @Size(max = 100, message = "菜单图标长度不能超过100个字符")
    private String icon;

    @Schema(description = "菜单类型（1-目录，2-菜单，3-按钮）", example = "2", required = true)
    @NotNull(message = "菜单类型不能为空")
    private Integer menuType;

    @Schema(description = "状态（0-禁用，1-启用）", example = "1")
    private Integer status = MenuStatusEnum.ENABLED.getCode();

    @Schema(description = "排序（数字越小越靠前）", example = "0")
    private Integer sortOrder = 0;

    @Schema(description = "权限标识（如sys:user:query）", example = "system:user:query")
    @Size(max = 100, message = "权限标识长度不能超过100个字符")
    private String perms;

    @Schema(description = "是否外链（0-否，1-是）", example = "0")
    private Integer isExternal = 0;

    @Schema(description = "外链地址", example = "https://example.com")
    @Size(max = 500, message = "外链地址长度不能超过500个字符")
    private String externalUrl;

    @Schema(description = "是否缓存（0-否，1-是）", example = "0")
    private Integer isCache = 0;

    @Schema(description = "是否可见（0-隐藏，1-显示）", example = "1")
    private Integer isVisible = 1;

    @Schema(description = "备注", example = "用户管理菜单")
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;
}
package com.skytrust.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import com.skytrust.enums.MenuStatusEnum;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 菜单视图对象
 *
 * @author SkyTrust Team
 */
@Data
@Schema(description = "菜单视图对象")
public class MenuVO {

    @Schema(description = "菜单ID", example = "1")
    private Long id;

    @Schema(description = "父菜单ID（0表示根菜单）", example = "0")
    private Long parentId = 0L;

    @Schema(description = "菜单名称", example = "用户管理")
    private String menuName;

    @Schema(description = "菜单代码（唯一）", example = "system:user")
    private String menuCode;

    @Schema(description = "前端路由路径", example = "/system/user")
    private String menuPath;

    @Schema(description = "组件路径", example = "system/user/index")
    private String component;

    @Schema(description = "菜单图标", example = "user")
    private String icon;

    @Schema(description = "菜单类型（1-目录，2-菜单，3-按钮）", example = "2")
    private Integer menuType;

    @Schema(description = "状态（0-禁用，1-启用）", example = "1")
    private Integer status = MenuStatusEnum.ENABLED.getCode();

    @Schema(description = "排序（数字越小越靠前）", example = "0")
    private Integer sortOrder = 0;

    @Schema(description = "权限标识（如sys:user:query）", example = "system:user:query")
    private String perms;

    @Schema(description = "是否外链（0-否，1-是）", example = "0")
    private Integer isExternal = 0;

    @Schema(description = "外链地址", example = "https://example.com")
    private String externalUrl;

    @Schema(description = "是否缓存（0-否，1-是）", example = "0")
    private Integer isCache = 0;

    @Schema(description = "是否可见（0-隐藏，1-显示）", example = "1")
    private Integer isVisible = 1;

    @Schema(description = "备注", example = "用户管理菜单")
    private String remark;

    @Schema(description = "创建时间", example = "2023-10-01 12:00:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2023-10-01 12:00:00")
    private LocalDateTime updateTime;
}
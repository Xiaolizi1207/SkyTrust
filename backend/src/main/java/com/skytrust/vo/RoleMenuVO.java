package com.skytrust.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 角色菜单关联视图对象
 *
 * @author SkyTrust Team
 */
@Data
@Schema(description = "角色菜单关联视图对象")
public class RoleMenuVO {

    @Schema(description = "关联ID", example = "1")
    private Long id;

    @Schema(description = "角色ID", example = "1")
    private Long roleId;

    @Schema(description = "菜单ID", example = "1")
    private Long menuId;

    @Schema(description = "创建时间", example = "2023-10-01 12:00:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2023-10-01 12:00:00")
    private LocalDateTime updateTime;
}
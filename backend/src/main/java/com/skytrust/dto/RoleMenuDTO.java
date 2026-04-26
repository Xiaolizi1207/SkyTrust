package com.skytrust.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * 角色菜单关联数据传输对象
 *
 * @author SkyTrust Team
 */
@Data
@Schema(description = "角色菜单关联数据传输对象")
public class RoleMenuDTO {

    @Schema(description = "角色ID", example = "1", required = true)
    @NotNull(message = "角色ID不能为空")
    private Long roleId;

    @Schema(description = "菜单ID", example = "1", required = true)
    @NotNull(message = "菜单ID不能为空")
    private Long menuId;
}
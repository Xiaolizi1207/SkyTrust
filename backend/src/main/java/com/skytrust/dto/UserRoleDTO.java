package com.skytrust.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户角色关联数据传输对象
 *
 * @author SkyTrust Team
 */
@Data
@Schema(description = "用户角色关联数据传输对象")
public class UserRoleDTO {

    @Schema(description = "用户ID", example = "1", required = true)
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @Schema(description = "角色ID", example = "1", required = true)
    @NotNull(message = "角色ID不能为空")
    private Long roleId;
}
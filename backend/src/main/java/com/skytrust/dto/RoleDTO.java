package com.skytrust.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import com.skytrust.enums.UserStatusEnum;
import lombok.Data;

/**
 * 角色数据传输对象
 *
 * @author SkyTrust Team
 */
@Data
@Schema(description = "角色数据传输对象")
public class RoleDTO {

    @Schema(description = "角色代码（唯一）", example = "admin", required = true)
    @NotBlank(message = "角色代码不能为空")
    @Size(min = 1, max = 50, message = "角色代码长度必须在1-50个字符之间")
    private String roleCode;

    @Schema(description = "角色名称", example = "管理员", required = true)
    @NotBlank(message = "角色名称不能为空")
    @Size(min = 1, max = 100, message = "角色名称长度必须在1-100个字符之间")
    private String roleName;

    @Schema(description = "角色描述", example = "系统管理员，拥有所有权限")
    @Size(max = 500, message = "角色描述长度不能超过500个字符")
    private String description;

    @Schema(description = "状态（0-禁用，1-启用）", example = "1")
    private Integer status = UserStatusEnum.ENABLED.getCode();

    @Schema(description = "排序（数字越小越靠前）", example = "0")
    private Integer sortOrder = 0;
}
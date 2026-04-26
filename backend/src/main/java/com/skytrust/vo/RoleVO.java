package com.skytrust.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import com.skytrust.enums.UserStatusEnum;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 角色视图对象
 *
 * @author SkyTrust Team
 */
@Data
@Schema(description = "角色视图对象")
public class RoleVO {

    @Schema(description = "角色ID", example = "1")
    private Long id;

    @Schema(description = "角色代码（唯一）", example = "admin")
    private String roleCode;

    @Schema(description = "角色名称", example = "管理员")
    private String roleName;

    @Schema(description = "角色描述", example = "系统管理员，拥有所有权限")
    private String description;

    @Schema(description = "状态（0-禁用，1-启用）", example = "1")
    private Integer status = UserStatusEnum.ENABLED.getCode();

    @Schema(description = "排序（数字越小越靠前）", example = "0")
    private Integer sortOrder = 0;

    @Schema(description = "创建时间", example = "2023-10-01 12:00:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2023-10-01 12:00:00")
    private LocalDateTime updateTime;
}
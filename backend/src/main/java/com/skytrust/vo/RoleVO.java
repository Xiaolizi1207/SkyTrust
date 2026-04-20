package com.skytrust.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.skytrust.enums.UserStatusEnum;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 角色视图对象
 *
 * @author SkyTrust Team
 */
@Data
@ApiModel(description = "角色视图对象")
public class RoleVO {

    @ApiModelProperty(value = "角色ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "角色代码（唯一）", example = "admin")
    private String roleCode;

    @ApiModelProperty(value = "角色名称", example = "管理员")
    private String roleName;

    @ApiModelProperty(value = "角色描述", example = "系统管理员，拥有所有权限")
    private String description;

    @ApiModelProperty(value = "状态（0-禁用，1-启用）", example = "1")
    private Integer status = UserStatusEnum.ENABLED.getCode();

    @ApiModelProperty(value = "排序（数字越小越靠前）", example = "0")
    private Integer sortOrder = 0;

    @ApiModelProperty(value = "创建时间", example = "2023-10-01 12:00:00")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间", example = "2023-10-01 12:00:00")
    private LocalDateTime updateTime;
}
package com.skytrust.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户角色关联数据传输对象
 *
 * @author SkyTrust Team
 */
@Data
@ApiModel(description = "用户角色关联数据传输对象")
public class UserRoleDTO {

    @ApiModelProperty(value = "用户ID", example = "1", required = true)
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @ApiModelProperty(value = "角色ID", example = "1", required = true)
    @NotNull(message = "角色ID不能为空")
    private Long roleId;
}
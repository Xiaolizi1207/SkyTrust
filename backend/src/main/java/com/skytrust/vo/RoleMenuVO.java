package com.skytrust.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 角色菜单关联视图对象
 *
 * @author SkyTrust Team
 */
@Data
@ApiModel(description = "角色菜单关联视图对象")
public class RoleMenuVO {

    @ApiModelProperty(value = "关联ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "角色ID", example = "1")
    private Long roleId;

    @ApiModelProperty(value = "菜单ID", example = "1")
    private Long menuId;

    @ApiModelProperty(value = "创建时间", example = "2023-10-01 12:00:00")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间", example = "2023-10-01 12:00:00")
    private LocalDateTime updateTime;
}
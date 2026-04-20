package com.skytrust.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户视图对象（用于返回用户信息）
 *
 * @author SkyTrust Team
 */
@Data
@ApiModel(description = "用户视图对象")
public class UserVO {

    @ApiModelProperty(value = "用户ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "用户名", example = "john_doe")
    private String username;

    @ApiModelProperty(value = "手机号", example = "13800138000")
    private String phone;

    @ApiModelProperty(value = "邮箱", example = "john@example.com")
    private String email;

    @ApiModelProperty(value = "真实姓名", example = "张三")
    private String realName;

    @ApiModelProperty(value = "身份证号", example = "110101199001011234")
    private String idCard;

    @ApiModelProperty(value = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatar;

    @ApiModelProperty(value = "用户状态（0-禁用，1-启用）", example = "1")
    private Integer status;

    @ApiModelProperty(value = "用户角色（admin-管理员，user-普通用户，pilot-飞行员）", example = "user")
    private String role;

    @ApiModelProperty(value = "信用评分（0-100）", example = "80")
    private Integer creditScore;

    @ApiModelProperty(value = "钱包地址（区块链地址）", example = "0x742d35Cc6634C0532925a3b844Bc9e60F6433cdb")
    private String walletAddress;

    @ApiModelProperty(value = "最后登录时间", example = "2023-10-01 12:00:00")
    private LocalDateTime lastLoginTime;

    @ApiModelProperty(value = "备注", example = "测试用户")
    private String remark;

    @ApiModelProperty(value = "创建时间", example = "2023-10-01 12:00:00")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间", example = "2023-10-01 12:00:00")
    private LocalDateTime updateTime;
}
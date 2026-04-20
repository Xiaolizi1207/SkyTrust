package com.skytrust.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;
import lombok.Data;

/**
 * 用户数据传输对象（用于创建和更新用户）
 *
 * @author SkyTrust Team
 */
@Data
@ApiModel(description = "用户数据传输对象")
public class UserDTO {

    @ApiModelProperty(value = "用户名（唯一）", example = "john_doe", required = true)
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;

    @ApiModelProperty(value = "密码（创建用户时必填，更新时可选）", example = "Password123!")
    @Size(min = 6, max = 100, message = "密码长度必须在6-100个字符之间")
    private String password;

    @ApiModelProperty(value = "手机号（唯一）", example = "13800138000", required = true)
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @ApiModelProperty(value = "邮箱", example = "john@example.com")
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;

    @ApiModelProperty(value = "真实姓名", example = "张三")
    @Size(max = 50, message = "真实姓名长度不能超过50个字符")
    private String realName;

    @ApiModelProperty(value = "身份证号", example = "110101199001011234")
    @Pattern(regexp = "^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$",
            message = "身份证号格式不正确")
    private String idCard;

    @ApiModelProperty(value = "头像URL", example = "https://example.com/avatar.jpg")
    @Size(max = 500, message = "头像URL长度不能超过500个字符")
    private String avatar;

    @ApiModelProperty(value = "用户状态（0-禁用，1-启用）", example = "1")
    private Integer status;

    @ApiModelProperty(value = "用户角色（admin-管理员，user-普通用户，pilot-飞行员）", example = "user")
    @Pattern(regexp = "^(admin|user|pilot)$", message = "用户角色只能是admin、user或pilot")
    private String role;

    @ApiModelProperty(value = "信用评分（0-100）", example = "80")
    private Integer creditScore;

    @ApiModelProperty(value = "钱包地址（区块链地址）", example = "0x742d35Cc6634C0532925a3b844Bc9e60F6433cdb")
    @Size(max = 100, message = "钱包地址长度不能超过100个字符")
    private String walletAddress;

    @ApiModelProperty(value = "备注", example = "测试用户")
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;
}
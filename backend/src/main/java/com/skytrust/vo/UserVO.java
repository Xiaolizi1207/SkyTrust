package com.skytrust.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户视图对象（用于返回用户信息）
 *
 * @author SkyTrust Team
 */
@Data
@Schema(description = "用户视图对象")
public class UserVO {

    @Schema(description = "用户ID", example = "1")
    private Long id;

    @Schema(description = "用户名", example = "john_doe")
    private String username;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "邮箱", example = "john@example.com")
    private String email;

    @Schema(description = "真实姓名", example = "张三")
    private String realName;

    @Schema(description = "身份证号", example = "110101199001011234")
    private String idCard;

    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatar;

    @Schema(description = "用户状态（0-禁用，1-启用）", example = "1")
    private Integer status;

    @Schema(description = "用户角色（admin-管理员，user-普通用户，pilot-飞行员）", example = "user")
    private String role;

    @Schema(description = "信用评分（0-100）", example = "80")
    private Integer creditScore;

    @Schema(description = "钱包地址（区块链地址）", example = "0x742d35Cc6634C0532925a3b844Bc9e60F6433cdb")
    private String walletAddress;

    @Schema(description = "钱包余额（元）", example = "1000.00")
    private java.math.BigDecimal balance;

    @Schema(description = "最后登录时间", example = "2023-10-01 12:00:00")
    private LocalDateTime lastLoginTime;

    @Schema(description = "备注", example = "测试用户")
    private String remark;

    @Schema(description = "创建时间", example = "2023-10-01 12:00:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2023-10-01 12:00:00")
    private LocalDateTime updateTime;
}
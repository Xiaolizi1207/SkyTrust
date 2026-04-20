package com.skytrust.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户实体类
 *
 * @author SkyTrust Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user")
public class User extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 用户名（唯一）
     */
    @TableField(value = "username")
    private String username;

    /**
     * 密码（加密存储）
     */
    @TableField(value = "password")
    private String password;

    /**
     * 手机号（唯一）
     */
    @TableField(value = "phone")
    private String phone;

    /**
     * 邮箱
     */
    @TableField(value = "email")
    private String email;

    /**
     * 真实姓名
     */
    @TableField(value = "real_name")
    private String realName;

    /**
     * 身份证号
     */
    @TableField(value = "id_card")
    private String idCard;

    /**
     * 头像URL
     */
    @TableField(value = "avatar")
    private String avatar;

    /**
     * 用户状态（0-禁用，1-启用）
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 用户角色（admin-管理员，user-普通用户，pilot-飞行员）
     */
    @TableField(value = "role")
    private String role;

    /**
     * 信用评分（0-100）
     */
    @TableField(value = "credit_score")
    private Integer creditScore;

    /**
     * 钱包地址（区块链地址）
     */
    @TableField(value = "wallet_address")
    private String walletAddress;

    /**
     * 最后登录时间
     */
    @TableField(value = "last_login_time")
    private LocalDateTime lastLoginTime;

    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;
}
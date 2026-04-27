package com.skytrust.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 登录日志实体类
 *
 * @author SkyTrust Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("login_log")
public class LoginLog extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 登录用户名
     */
    @TableField(value = "username")
    private String username;

    /**
     * 日志类型（0-登录成功，1-登录失败，2-登录锁定，3-退出登录，4-令牌刷新，5-密码重置）
     */
    @TableField(value = "log_type")
    private Integer logType;

    /**
     * IP地址
     */
    @TableField(value = "ip_address")
    private String ipAddress;

    /**
     * IP所在地址
     */
    @TableField(value = "ip_location")
    private String ipLocation;

    /**
     * 用户代理（浏览器/客户端信息）
     */
    @TableField(value = "user_agent")
    private String userAgent;

    /**
     * 设备信息
     */
    @TableField(value = "device_info")
    private String deviceInfo;

    /**
     * 登录时间
     */
    @TableField(value = "login_time")
    private LocalDateTime loginTime;

    /**
     * 失败原因
     */
    @TableField(value = "fail_reason")
    private String failReason;
}

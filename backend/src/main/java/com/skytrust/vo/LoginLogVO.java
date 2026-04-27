package com.skytrust.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录日志视图对象
 *
 * @author SkyTrust Team
 */
@Data
@Schema(description = "登录日志视图对象")
public class LoginLogVO {

    @Schema(description = "日志ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "日志类型（0-登录成功，1-登录失败，2-登录锁定，3-退出登录）")
    private Integer logType;

    @Schema(description = "日志类型描述")
    private String logTypeDesc;

    @Schema(description = "IP地址")
    private String ipAddress;

    @Schema(description = "IP所在地址")
    private String ipLocation;

    @Schema(description = "设备信息")
    private String deviceInfo;

    @Schema(description = "用户代理")
    private String userAgent;

    @Schema(description = "登录时间")
    private LocalDateTime loginTime;

    @Schema(description = "失败原因")
    private String failReason;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}

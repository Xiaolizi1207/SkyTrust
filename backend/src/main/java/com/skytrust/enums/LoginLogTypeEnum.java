package com.skytrust.enums;

import lombok.Getter;

/**
 * 登录日志类型枚举
 *
 * @author SkyTrust Team
 */
@Getter
public enum LoginLogTypeEnum {

    /**
     * 登录成功
     */
    LOGIN_SUCCESS(0, "登录成功"),

    /**
     * 登录失败（密码错误）
     */
    LOGIN_FAIL(1, "登录失败"),

    /**
     * 登录锁定
     */
    LOGIN_LOCKED(2, "登录锁定"),

    /**
     * 退出登录
     */
    LOGOUT(3, "退出登录"),

    /**
     * 令牌刷新
     */
    TOKEN_REFRESH(4, "令牌刷新"),

    /**
     * 密码重置
     */
    PASSWORD_RESET(5, "密码重置");

    private final Integer code;
    private final String description;

    LoginLogTypeEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static String getDescriptionByCode(Integer code) {
        if (code == null) return "";
        for (LoginLogTypeEnum type : values()) {
            if (type.code.equals(code)) {
                return type.description;
            }
        }
        return "";
    }
}

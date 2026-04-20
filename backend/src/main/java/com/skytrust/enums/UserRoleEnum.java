package com.skytrust.enums;

import lombok.Getter;

/**
 * 用户角色枚举
 *
 * @author SkyTrust Team
 */
@Getter
public enum UserRoleEnum {

    ADMIN("admin", "管理员"),
    USER("user", "普通用户"),
    PILOT("pilot", "飞行员");

    private final String code;
    private final String description;

    UserRoleEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据code获取枚举
     */
    public static UserRoleEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (UserRoleEnum role : values()) {
            if (role.getCode().equals(code)) {
                return role;
            }
        }
        return null;
    }

    /**
     * 根据code获取描述
     */
    public static String getDescriptionByCode(String code) {
        UserRoleEnum role = getByCode(code);
        return role != null ? role.getDescription() : "未知";
    }

    /**
     * 验证code是否有效
     */
    public static boolean isValid(String code) {
        return getByCode(code) != null;
    }
}
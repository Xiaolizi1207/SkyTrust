package com.skytrust.enums;

import lombok.Getter;

/**
 * 菜单状态枚举
 *
 * @author SkyTrust Team
 */
@Getter
public enum MenuStatusEnum {

    DISABLED(0, "禁用"),
    ENABLED(1, "启用");

    private final Integer code;
    private final String description;

    MenuStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据code获取枚举
     */
    public static MenuStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (MenuStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 根据code获取描述
     */
    public static String getDescriptionByCode(Integer code) {
        MenuStatusEnum status = getByCode(code);
        return status != null ? status.getDescription() : "未知";
    }

    /**
     * 验证code是否有效
     */
    public static boolean isValid(Integer code) {
        return getByCode(code) != null;
    }
}
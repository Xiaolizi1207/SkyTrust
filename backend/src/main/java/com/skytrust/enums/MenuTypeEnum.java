package com.skytrust.enums;

import lombok.Getter;

/**
 * 菜单类型枚举
 *
 * @author SkyTrust Team
 */
@Getter
public enum MenuTypeEnum {

    DIRECTORY(1, "目录"),
    MENU(2, "菜单"),
    BUTTON(3, "按钮");

    private final Integer code;
    private final String description;

    MenuTypeEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据code获取枚举
     */
    public static MenuTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (MenuTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 根据code获取描述
     */
    public static String getDescriptionByCode(Integer code) {
        MenuTypeEnum type = getByCode(code);
        return type != null ? type.getDescription() : "未知";
    }

    /**
     * 验证code是否有效
     */
    public static boolean isValid(Integer code) {
        return getByCode(code) != null;
    }
}
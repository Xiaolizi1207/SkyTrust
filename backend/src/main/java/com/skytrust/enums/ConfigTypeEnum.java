package com.skytrust.enums;

import lombok.Getter;

/**
 * 配置类型枚举
 *
 * @author SkyTrust Team
 */
@Getter
public enum ConfigTypeEnum {

    SYSTEM(0, "系统配置"),
    BUSINESS(1, "业务配置"),
    BLOCKCHAIN(2, "区块链配置"),
    AI(3, "AI配置"),
    IOT(4, "物联网配置");

    private final Integer code;
    private final String description;

    ConfigTypeEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据code获取枚举
     */
    public static ConfigTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ConfigTypeEnum type : values()) {
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
        ConfigTypeEnum type = getByCode(code);
        return type != null ? type.getDescription() : "未知";
    }

    /**
     * 验证code是否有效
     */
    public static boolean isValid(Integer code) {
        return getByCode(code) != null;
    }
}
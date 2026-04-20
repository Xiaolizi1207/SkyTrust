package com.skytrust.enums;

import lombok.Getter;

/**
 * 合约状态枚举
 *
 * @author SkyTrust Team
 */
@Getter
public enum ContractStatusEnum {

    INACTIVE(0, "未激活"),
    ACTIVE(1, "已激活"),
    SUSPENDED(2, "已暂停"),
    TERMINATED(3, "已终止");

    private final Integer code;
    private final String description;

    ContractStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据code获取枚举
     */
    public static ContractStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ContractStatusEnum status : values()) {
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
        ContractStatusEnum status = getByCode(code);
        return status != null ? status.getDescription() : "未知";
    }

    /**
     * 验证code是否有效
     */
    public static boolean isValid(Integer code) {
        return getByCode(code) != null;
    }
}
package com.skytrust.enums;

import lombok.Getter;

/**
 * 合约类型枚举
 *
 * @author SkyTrust Team
 */
@Getter
public enum ContractTypeEnum {

    RENTAL(0, "租赁合约"),
    PAYMENT(1, "支付合约"),
    INSURANCE(2, "保险合约"),
    DEVICE(3, "设备合约");

    private final Integer code;
    private final String description;

    ContractTypeEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据code获取枚举
     */
    public static ContractTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ContractTypeEnum type : values()) {
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
        ContractTypeEnum type = getByCode(code);
        return type != null ? type.getDescription() : "未知";
    }

    /**
     * 验证code是否有效
     */
    public static boolean isValid(Integer code) {
        return getByCode(code) != null;
    }
}
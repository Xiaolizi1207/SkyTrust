package com.skytrust.enums;

import lombok.Getter;

/**
 * 支付状态枚举
 *
 * @author SkyTrust Team
 */
@Getter
public enum PaymentStatusEnum {

    UNPAID(0, "未支付"),
    PAID(1, "已支付"),
    FAILED(2, "支付失败"),
    REFUNDED(3, "已退款");

    private final Integer code;
    private final String description;

    PaymentStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据code获取枚举
     */
    public static PaymentStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (PaymentStatusEnum status : values()) {
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
        PaymentStatusEnum status = getByCode(code);
        return status != null ? status.getDescription() : "未知";
    }

    /**
     * 验证code是否有效
     */
    public static boolean isValid(Integer code) {
        return getByCode(code) != null;
    }
}
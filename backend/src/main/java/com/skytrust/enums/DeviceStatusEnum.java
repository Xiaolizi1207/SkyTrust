package com.skytrust.enums;

import lombok.Getter;

/**
 * 设备状态枚举
 *
 * @author SkyTrust Team
 */
@Getter
public enum DeviceStatusEnum {

    OFFLINE(0, "离线"),
    ONLINE(1, "在线"),
    FLYING(2, "飞行中"),
    MAINTENANCE(3, "维修中"),
    SCRAPPED(4, "已报废");

    private final Integer code;
    private final String description;

    DeviceStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据code获取枚举
     */
    public static DeviceStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (DeviceStatusEnum status : values()) {
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
        DeviceStatusEnum status = getByCode(code);
        return status != null ? status.getDescription() : "未知";
    }

    /**
     * 验证code是否有效
     */
    public static boolean isValid(Integer code) {
        return getByCode(code) != null;
    }
}
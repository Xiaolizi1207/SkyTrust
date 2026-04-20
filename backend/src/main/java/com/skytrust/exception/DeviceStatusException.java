package com.skytrust.exception;

import com.skytrust.common.ResultCode;

/**
 * 设备状态异常
 */
public class DeviceStatusException extends BusinessException {

    public DeviceStatusException(String message) {
        super(ResultCode.DEVICE_STATUS_ERROR.getCode(), message);
    }

    public DeviceStatusException(String message, Throwable cause) {
        super(ResultCode.DEVICE_STATUS_ERROR.getCode(), message, cause);
    }

    public DeviceStatusException(ResultCode resultCode, String message) {
        super(resultCode, message);
    }
}
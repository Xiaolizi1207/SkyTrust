package com.skytrust.exception;

import com.skytrust.common.ResultCode;

/**
 * 物联网连接异常
 */
public class IoTException extends BusinessException {

    public IoTException(String message) {
        super(ResultCode.IOT_CONNECTION_ERROR.getCode(), message);
    }

    public IoTException(String message, Throwable cause) {
        super(ResultCode.IOT_CONNECTION_ERROR.getCode(), message, cause);
    }

    public IoTException(ResultCode resultCode, String message) {
        super(resultCode, message);
    }
}
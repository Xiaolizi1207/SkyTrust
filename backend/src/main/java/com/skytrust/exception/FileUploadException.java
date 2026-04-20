package com.skytrust.exception;

import com.skytrust.common.ResultCode;

/**
 * 文件上传异常
 */
public class FileUploadException extends BusinessException {

    public FileUploadException(String message) {
        super(ResultCode.FILE_UPLOAD_ERROR.getCode(), message);
    }

    public FileUploadException(String message, Throwable cause) {
        super(ResultCode.FILE_UPLOAD_ERROR.getCode(), message, cause);
    }

    public FileUploadException(ResultCode resultCode, String message) {
        super(resultCode, message);
    }
}
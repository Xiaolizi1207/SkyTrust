package com.skytrust.exception;

import com.skytrust.common.ResultCode;

/**
 * 区块链操作异常
 */
public class BlockchainException extends BusinessException {

    public BlockchainException(String message) {
        super(ResultCode.BLOCKCHAIN_ERROR.getCode(), message);
    }

    public BlockchainException(String message, Throwable cause) {
        super(ResultCode.BLOCKCHAIN_ERROR.getCode(), message, cause);
    }

    public BlockchainException(ResultCode resultCode, String message) {
        super(resultCode, message);
    }
}
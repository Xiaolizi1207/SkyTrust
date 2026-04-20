package com.skytrust.exception;

import com.skytrust.common.ResultCode;

/**
 * 智能合约执行异常
 */
public class ContractExecutionException extends BusinessException {

    public ContractExecutionException(String message) {
        super(ResultCode.CONTRACT_EXECUTION_ERROR.getCode(), message);
    }

    public ContractExecutionException(String message, Throwable cause) {
        super(ResultCode.CONTRACT_EXECUTION_ERROR.getCode(), message, cause);
    }

    public ContractExecutionException(ResultCode resultCode, String message) {
        super(resultCode, message);
    }
}
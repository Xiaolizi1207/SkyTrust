package com.skytrust.service;

import com.skytrust.common.Result;
import com.skytrust.entity.WalletTransaction;

import java.math.BigDecimal;
import java.util.List;

/**
 * 钱包交易服务接口
 */
public interface WalletTransactionService extends IService<WalletTransaction> {

    /**
     * 获取用户余额
     */
    BigDecimal getBalance(Long userId);

    /**
     * 获取用户交易记录（分页）
     */
    List<WalletTransaction> getByUserId(Long userId, Integer page, Integer size);

    /**
     * 充值
     */
    Result<WalletTransaction> recharge(Long userId, BigDecimal amount, String description);
}

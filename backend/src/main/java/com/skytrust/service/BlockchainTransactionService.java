package com.skytrust.service;

import com.skytrust.entity.BlockchainTransaction;

/**
 * 区块链交易服务接口
 *
 * @author SkyTrust Team
 */
public interface BlockchainTransactionService extends IService<BlockchainTransaction> {

    /**
     * 根据交易哈希查询交易记录
     *
     * @param txHash 交易哈希
     * @return 交易记录
     */
    BlockchainTransaction getByTxHash(String txHash);

    /**
     * 根据订单ID查询交易记录
     *
     * @param orderId 订单ID
     * @return 交易记录
     */
    BlockchainTransaction getByOrderId(Long orderId);

    /**
     * 根据合约地址查询交易记录
     *
     * @param contractAddress 合约地址
     * @return 交易记录列表
     */
    java.util.List<BlockchainTransaction> getByContractAddress(String contractAddress);
}
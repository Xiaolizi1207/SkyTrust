package com.skytrust.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.skytrust.common.utils.StringUtil;
import com.skytrust.entity.BlockchainTransaction;
import com.skytrust.mapper.BlockchainTransactionMapper;
import com.skytrust.service.BlockchainTransactionService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 区块链交易服务实现类
 *
 * @author SkyTrust Team
 */
@Service
public class BlockchainTransactionServiceImpl extends BaseService<BlockchainTransactionMapper, BlockchainTransaction>
        implements BlockchainTransactionService {

    @Override
    public BlockchainTransaction getByTxHash(String txHash) {
        if (StringUtil.isEmpty(txHash)) {
            return null;
        }
        LambdaQueryWrapper<BlockchainTransaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlockchainTransaction::getTxHash, txHash);
        return getOne(wrapper);
    }

    @Override
    public BlockchainTransaction getByOrderId(Long orderId) {
        if (orderId == null) {
            return null;
        }
        LambdaQueryWrapper<BlockchainTransaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlockchainTransaction::getBusinessId, orderId)
               .eq(BlockchainTransaction::getBusinessType, "order");
        return getOne(wrapper);
    }

    @Override
    public List<BlockchainTransaction> getByContractAddress(String contractAddress) {
        if (StringUtil.isEmpty(contractAddress)) {
            return listAll();
        }
        LambdaQueryWrapper<BlockchainTransaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlockchainTransaction::getContractAddress, contractAddress)
                .orderByDesc(BlockchainTransaction::getCreateTime);
        return list(wrapper);
    }
}
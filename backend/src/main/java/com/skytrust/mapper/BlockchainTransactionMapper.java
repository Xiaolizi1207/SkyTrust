package com.skytrust.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.skytrust.entity.BlockchainTransaction;
import org.apache.ibatis.annotations.Mapper;

/**
 * 区块链交易记录Mapper接口
 *
 * @author SkyTrust Team
 */
@Mapper
public interface BlockchainTransactionMapper extends BaseMapper<BlockchainTransaction> {

    // 可以在此添加自定义SQL方法
    // 例如：根据交易哈希查询交易记录
    // @Select("SELECT * FROM blockchain_transaction WHERE tx_hash = #{txHash}")
    // BlockchainTransaction selectByTxHash(@Param("txHash") String txHash);
}
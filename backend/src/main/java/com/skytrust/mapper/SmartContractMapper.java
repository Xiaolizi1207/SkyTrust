package com.skytrust.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.skytrust.entity.SmartContract;
import org.apache.ibatis.annotations.Mapper;

/**
 * 智能合约Mapper接口
 *
 * @author SkyTrust Team
 */
@Mapper
public interface SmartContractMapper extends BaseMapper<SmartContract> {

    // 可以在此添加自定义SQL方法
    // 例如：根据合约地址查询合约信息
    // @Select("SELECT * FROM smart_contract WHERE contract_address = #{contractAddress}")
    // SmartContract selectByContractAddress(@Param("contractAddress") String contractAddress);
}
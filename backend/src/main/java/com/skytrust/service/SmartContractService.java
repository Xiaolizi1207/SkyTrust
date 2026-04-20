package com.skytrust.service;

import com.skytrust.entity.SmartContract;

/**
 * 智能合约服务接口
 *
 * @author SkyTrust Team
 */
public interface SmartContractService extends IService<SmartContract> {

    /**
     * 根据合约地址查询合约
     *
     * @param contractAddress 合约地址
     * @return 合约信息
     */
    SmartContract getByContractAddress(String contractAddress);

    /**
     * 根据类型查询合约
     *
     * @param contractType 合约类型
     * @return 合约列表
     */
    java.util.List<SmartContract> getByContractType(String contractType);

    /**
     * 获取最新部署的合约
     *
     * @return 最新合约
     */
    SmartContract getLatestContract();
}
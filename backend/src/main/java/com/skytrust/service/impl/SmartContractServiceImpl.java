package com.skytrust.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.skytrust.entity.SmartContract;
import com.skytrust.mapper.SmartContractMapper;
import com.skytrust.service.SmartContractService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 智能合约服务实现类
 *
 * @author SkyTrust Team
 */
@Service
public class SmartContractServiceImpl extends BaseService<SmartContractMapper, SmartContract> implements SmartContractService {

    @Override
    public SmartContract getByContractAddress(String contractAddress) {
        if (contractAddress == null || contractAddress.trim().isEmpty()) {
            return null;
        }
        LambdaQueryWrapper<SmartContract> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SmartContract::getContractAddress, contractAddress.trim());
        return getOne(wrapper);
    }

    @Override
    public List<SmartContract> getByContractType(String contractType) {
        if (contractType == null || contractType.trim().isEmpty()) {
            return listAll();
        }
        LambdaQueryWrapper<SmartContract> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SmartContract::getContractType, contractType.trim())
                .orderByDesc(SmartContract::getDeployTime);
        return list(wrapper);
    }

    @Override
    public SmartContract getLatestContract() {
        LambdaQueryWrapper<SmartContract> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(SmartContract::getDeployTime)
                .last("LIMIT 1");
        return getOne(wrapper);
    }
}
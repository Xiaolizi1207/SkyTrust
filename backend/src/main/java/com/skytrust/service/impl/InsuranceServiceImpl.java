package com.skytrust.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.skytrust.common.utils.StringUtil;
import com.skytrust.entity.Insurance;
import com.skytrust.mapper.InsuranceMapper;
import com.skytrust.service.InsuranceService;
import org.springframework.stereotype.Service;

/**
 * 保险服务实现类
 *
 * @author SkyTrust Team
 */
@Service
public class InsuranceServiceImpl extends BaseService<InsuranceMapper, Insurance> implements InsuranceService {

    @Override
    public Insurance getByOrderId(Long orderId) {
        if (orderId == null) {
            return null;
        }
        LambdaQueryWrapper<Insurance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Insurance::getOrderId, orderId);
        return getOne(wrapper);
    }

    @Override
    public Insurance getByPolicyNo(String policyNo) {
        if (StringUtil.isEmpty(policyNo)) {
            return null;
        }
        LambdaQueryWrapper<Insurance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Insurance::getPolicyNo, policyNo);
        return getOne(wrapper);
    }
}
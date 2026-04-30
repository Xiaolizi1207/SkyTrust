package com.skytrust.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.skytrust.common.Result;
import com.skytrust.common.ResultCode;
import com.skytrust.entity.User;
import com.skytrust.entity.WalletTransaction;
import com.skytrust.exception.BusinessException;
import com.skytrust.mapper.WalletTransactionMapper;
import com.skytrust.service.UserService;
import com.skytrust.service.WalletTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletTransactionServiceImpl
        extends BaseService<WalletTransactionMapper, WalletTransaction>
        implements WalletTransactionService {

    private final UserService userService;

    @Override
    public BigDecimal getBalance(Long userId) {
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST, "用户不存在");
        }
        return user.getBalance() != null ? user.getBalance() : BigDecimal.ZERO;
    }

    @Override
    public List<WalletTransaction> getByUserId(Long userId, Integer page, Integer size) {
        LambdaQueryWrapper<WalletTransaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WalletTransaction::getUserId, userId)
                .orderByDesc(WalletTransaction::getCreateTime);
        if (page != null && size != null && page > 0 && size > 0) {
            long offset = (long) (page - 1) * size;
            return list(wrapper.last("LIMIT " + offset + "," + size));
        }
        return list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<WalletTransaction> recharge(Long userId, BigDecimal amount, String description) {
        log.info("钱包充值: userId={}, amount={}", userId, amount);

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "充值金额必须大于0");
        }

        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST, "用户不存在");
        }

        BigDecimal balanceBefore = user.getBalance() != null ? user.getBalance() : BigDecimal.ZERO;
        BigDecimal balanceAfter = balanceBefore.add(amount);

        user.setBalance(balanceAfter);
        userService.updateById(user);

        WalletTransaction tx = new WalletTransaction();
        tx.setUserId(userId);
        tx.setType(0); // 充值
        tx.setAmount(amount);
        tx.setBalanceBefore(balanceBefore);
        tx.setBalanceAfter(balanceAfter);
        tx.setDescription(description != null ? description : "钱包充值");
        tx.setStatus(0); // 成功
        tx.setCreateTime(LocalDateTime.now());
        tx.setUpdateTime(LocalDateTime.now());
        save(tx);

        log.info("充值成功: userId={}, balanceBefore={}, balanceAfter={}", userId, balanceBefore, balanceAfter);
        return Result.success(tx, "充值成功");
    }
}

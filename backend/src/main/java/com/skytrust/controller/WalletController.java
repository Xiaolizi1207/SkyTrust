package com.skytrust.controller;

import com.skytrust.common.Result;
import com.skytrust.common.utils.SecurityUtil;
import com.skytrust.dto.RechargeDTO;
import com.skytrust.entity.WalletTransaction;
import com.skytrust.service.WalletTransactionService;
import com.skytrust.vo.WalletTransactionVO;
import com.skytrust.vo.WalletVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Tag(name = "钱包管理", description = "钱包余额及交易记录接口")
@Validated
@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletTransactionService walletTransactionService;

    private static final String[] TYPE_TEXTS = {"充值", "消费", "退款", "提现"};

    @Operation(summary = "获取钱包余额")
    @GetMapping("/balance")
    public Result<WalletVO> getBalance() {
        Long userId = SecurityUtil.getCurrentUserId();
        BigDecimal balance = walletTransactionService.getBalance(userId);
        WalletVO vo = new WalletVO();
        vo.setUserId(userId);
        vo.setBalance(balance);
        return Result.success(vo);
    }

    @Operation(summary = "获取交易记录")
    @GetMapping("/transactions")
    public Result<List<WalletTransactionVO>> getTransactions(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = SecurityUtil.getCurrentUserId();
        List<WalletTransaction> list = walletTransactionService.getByUserId(userId, page, size);
        List<WalletTransactionVO> voList = new ArrayList<>();
        for (WalletTransaction tx : list) {
            WalletTransactionVO vo = new WalletTransactionVO();
            vo.setId(tx.getId());
            vo.setUserId(tx.getUserId());
            vo.setType(tx.getType());
            if (tx.getType() != null && tx.getType() >= 0 && tx.getType() < TYPE_TEXTS.length) {
                vo.setTypeText(TYPE_TEXTS[tx.getType()]);
            }
            vo.setAmount(tx.getAmount());
            vo.setBalanceBefore(tx.getBalanceBefore());
            vo.setBalanceAfter(tx.getBalanceAfter());
            vo.setDescription(tx.getDescription());
            vo.setOrderId(tx.getOrderId());
            vo.setStatus(tx.getStatus());
            vo.setCreateTime(tx.getCreateTime());
            voList.add(vo);
        }
        return Result.success(voList);
    }

    @Operation(summary = "钱包充值")
    @PostMapping("/recharge")
    public Result<WalletVO> recharge(@Valid @RequestBody RechargeDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();
        var result = walletTransactionService.recharge(userId, dto.getAmount(), dto.getDescription());
        if (!result.isSuccess()) {
            return Result.error(result.getCode(), result.getMessage());
        }
        WalletVO vo = new WalletVO();
        vo.setUserId(userId);
        vo.setBalance(result.getData().getBalanceAfter());
        return Result.success(vo, "充值成功");
    }
}

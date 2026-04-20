package com.skytrust.controller;

import com.skytrust.common.Result;
import com.skytrust.common.ResultCode;
import com.skytrust.dto.BlockchainTransactionDTO;
import com.skytrust.entity.BlockchainTransaction;
import com.skytrust.service.BlockchainTransactionService;
import com.skytrust.vo.BlockchainTransactionVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 区块链交易控制器
 *
 * @author SkyTrust Team
 */
@Api(tags = "区块链交易管理", description = "区块链交易记录管理接口")
@Validated
@RestController
@RequestMapping("/api/blockchain-transactions")
public class BlockchainTransactionController {

    private final BlockchainTransactionService blockchainTransactionService;

    public BlockchainTransactionController(BlockchainTransactionService blockchainTransactionService) {
        this.blockchainTransactionService = blockchainTransactionService;
    }

    /**
     * 创建区块链交易记录
     */
    @ApiOperation(value = "创建区块链交易记录")
    @PostMapping
    public Result<BlockchainTransactionVO> createTransaction(@Valid @RequestBody BlockchainTransactionDTO transactionDTO) {
        // 转换DTO为实体
        BlockchainTransaction transaction = new BlockchainTransaction();
        BeanUtils.copyProperties(transactionDTO, transaction);

        // 保存交易记录
        boolean saved = blockchainTransactionService.save(transaction);
        if (!saved) {
            return Result.error("区块链交易记录创建失败");
        }

        BlockchainTransactionVO transactionVO = convertToVO(transaction);
        return Result.success(transactionVO, "区块链交易记录创建成功");
    }

    /**
     * 更新区块链交易记录信息
     */
    @ApiOperation(value = "更新区块链交易记录信息")
    @PutMapping("/{id}")
    public Result<BlockchainTransactionVO> updateTransaction(
            @ApiParam(value = "交易记录ID", required = true) @PathVariable Long id,
            @Valid @RequestBody BlockchainTransactionDTO transactionDTO) {
        BlockchainTransaction transaction = blockchainTransactionService.getById(id);
        if (transaction == null) {
            return Result.error(ResultCode.DATA_NOT_EXIST.getCode(), "区块链交易记录不存在");
        }

        // 更新字段（排除不能修改的字段）
        BeanUtils.copyProperties(transactionDTO, transaction, "id", "txHash", "createTime", "updateTime");

        boolean updated = blockchainTransactionService.updateById(transaction);
        if (!updated) {
            return Result.error("区块链交易记录更新失败");
        }

        BlockchainTransactionVO transactionVO = convertToVO(transaction);
        return Result.success(transactionVO, "区块链交易记录更新成功");
    }

    /**
     * 获取区块链交易记录详情
     */
    @ApiOperation(value = "获取区块链交易记录详情")
    @GetMapping("/{id}")
    public Result<BlockchainTransactionVO> getTransactionById(@ApiParam(value = "交易记录ID", required = true) @PathVariable Long id) {
        BlockchainTransaction transaction = blockchainTransactionService.getById(id);
        if (transaction == null) {
            return Result.error(ResultCode.DATA_NOT_EXIST.getCode(), "区块链交易记录不存在");
        }
        return Result.success(convertToVO(transaction));
    }

    /**
     * 删除区块链交易记录（逻辑删除）
     */
    @ApiOperation(value = "删除区块链交易记录")
    @DeleteMapping("/{id}")
    public Result<Void> deleteTransaction(@ApiParam(value = "交易记录ID", required = true) @PathVariable Long id) {
        boolean deleted = blockchainTransactionService.logicRemoveById(id);
        if (!deleted) {
            return Result.error("区块链交易记录删除失败");
        }
        return Result.success("区块链交易记录删除成功");
    }

    /**
     * 分页查询区块链交易记录列表
     */
    @ApiOperation(value = "分页查询区块链交易记录列表")
    @GetMapping
    public Result<List<BlockchainTransactionVO>> getTransactionList(
            @ApiParam(value = "页码", defaultValue = "1") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam(value = "每页大小", defaultValue = "10") @RequestParam(defaultValue = "10") Integer size,
            @ApiParam(value = "智能合约地址") @RequestParam(required = false) String contractAddress,
            @ApiParam(value = "订单ID") @RequestParam(required = false) Long orderId,
            @ApiParam(value = "交易类型") @RequestParam(required = false) Integer transactionType,
            @ApiParam(value = "交易状态") @RequestParam(required = false) Integer transactionStatus,
            @ApiParam(value = "交易哈希") @RequestParam(required = false) String txHash) {

        // 简化处理：使用Service的list方法
        List<BlockchainTransaction> transactions = blockchainTransactionService.list();

        // 应用过滤条件
        List<BlockchainTransaction> filteredTransactions = transactions.stream()
                .filter(transaction -> contractAddress == null ||
                        (transaction.getContractAddress() != null && transaction.getContractAddress().contains(contractAddress)))
                .filter(transaction -> orderId == null ||
                        (transaction.getBusinessId() != null && transaction.getBusinessId().equals(orderId)))
                .filter(transaction -> transactionType == null ||
                        (transaction.getTxType() != null && transaction.getTxType().equals(transactionType)))
                .filter(transaction -> transactionStatus == null ||
                        (transaction.getStatus() != null && transaction.getStatus().equals(transactionStatus)))
                .filter(transaction -> txHash == null ||
                        (transaction.getTxHash() != null && transaction.getTxHash().contains(txHash)))
                .skip((page - 1) * (long) size)
                .limit(size)
                .collect(Collectors.toList());

        List<BlockchainTransactionVO> transactionVOs = filteredTransactions.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return Result.success(transactionVOs);
    }

    /**
     * 根据交易哈希查询交易记录
     */
    @ApiOperation(value = "根据交易哈希查询交易记录")
    @GetMapping("/tx-hash/{txHash}")
    public Result<BlockchainTransactionVO> getTransactionByTxHash(@ApiParam(value = "交易哈希", required = true) @PathVariable String txHash) {
        BlockchainTransaction transaction = blockchainTransactionService.getByTxHash(txHash);
        if (transaction == null) {
            return Result.error(ResultCode.DATA_NOT_EXIST.getCode(), "区块链交易记录不存在");
        }
        return Result.success(convertToVO(transaction));
    }

    /**
     * 根据订单ID查询交易记录
     */
    @ApiOperation(value = "根据订单ID查询交易记录")
    @GetMapping("/order/{orderId}")
    public Result<BlockchainTransactionVO> getTransactionByOrderId(@ApiParam(value = "订单ID", required = true) @PathVariable Long orderId) {
        BlockchainTransaction transaction = blockchainTransactionService.getByOrderId(orderId);
        if (transaction == null) {
            return Result.error(ResultCode.DATA_NOT_EXIST.getCode(), "区块链交易记录不存在");
        }
        return Result.success(convertToVO(transaction));
    }

    /**
     * 根据合约地址查询交易记录
     */
    @ApiOperation(value = "根据合约地址查询交易记录")
    @GetMapping("/contract/{contractAddress}")
    public Result<List<BlockchainTransactionVO>> getTransactionsByContractAddress(@ApiParam(value = "合约地址", required = true) @PathVariable String contractAddress) {
        List<BlockchainTransaction> transactions = blockchainTransactionService.getByContractAddress(contractAddress);
        List<BlockchainTransactionVO> transactionVOs = transactions.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        return Result.success(transactionVOs);
    }

    /**
     * 将BlockchainTransaction实体转换为BlockchainTransactionVO
     */
    private BlockchainTransactionVO convertToVO(BlockchainTransaction transaction) {
        if (transaction == null) {
            return null;
        }
        BlockchainTransactionVO transactionVO = new BlockchainTransactionVO();
        BeanUtils.copyProperties(transaction, transactionVO);
        return transactionVO;
    }
}
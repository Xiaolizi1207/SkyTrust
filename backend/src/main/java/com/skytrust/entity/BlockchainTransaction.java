package com.skytrust.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 区块链交易记录实体类
 *
 * @author SkyTrust Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("blockchain_transaction")
public class BlockchainTransaction extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 交易哈希（唯一）
     */
    @TableField(value = "tx_hash")
    private String txHash;

    /**
     * 智能合约地址
     */
    @TableField(value = "contract_address")
    private String contractAddress;

    /**
     * 区块号
     */
    @TableField(value = "block_number")
    private Long blockNumber;

    /**
     * 区块哈希
     */
    @TableField(value = "block_hash")
    private String blockHash;

    /**
     * 交易发起方地址
     */
    @TableField(value = "from_address")
    private String fromAddress;

    /**
     * 交易接收方地址
     */
    @TableField(value = "to_address")
    private String toAddress;

    /**
     * 交易金额（ETH/代币数量）
     */
    @TableField(value = "amount")
    private BigDecimal amount;

    /**
     * 交易gas费用
     */
    @TableField(value = "gas_fee")
    private BigDecimal gasFee;

    /**
     * 交易状态（0-待确认，1-已确认，2-失败）
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 确认时间
     */
    @TableField(value = "confirm_time")
    private LocalDateTime confirmTime;

    /**
     * 交易类型（0-租赁合约创建，1-支付，2-保险，3-设备状态更新）
     */
    @TableField(value = "tx_type")
    private Integer txType;

    /**
     * 关联业务ID（如订单ID、支付ID等）
     */
    @TableField(value = "business_id")
    private Long businessId;

    /**
     * 关联业务类型（order-订单，payment-支付，insurance-保险）
     */
    @TableField(value = "business_type")
    private String businessType;

    /**
     * 交易输入数据（JSON格式）
     */
    @TableField(value = "input_data")
    private String inputData;

    /**
     * 交易收据（JSON格式）
     */
    @TableField(value = "receipt")
    private String receipt;

    /**
     * 网络ID（链ID）
     */
    @TableField(value = "chain_id")
    private Integer chainId;

    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;

    // Getter and Setter methods
    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public Long getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(Long blockNumber) {
        this.blockNumber = blockNumber;
    }

    public String getBlockHash() {
        return blockHash;
    }

    public void setBlockHash(String blockHash) {
        this.blockHash = blockHash;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public String getToAddress() {
        return toAddress;
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getGasFee() {
        return gasFee;
    }

    public void setGasFee(BigDecimal gasFee) {
        this.gasFee = gasFee;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getConfirmTime() {
        return confirmTime;
    }

    public void setConfirmTime(LocalDateTime confirmTime) {
        this.confirmTime = confirmTime;
    }

    public Integer getTxType() {
        return txType;
    }

    public void setTxType(Integer txType) {
        this.txType = txType;
    }

    public Long getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Long businessId) {
        this.businessId = businessId;
    }

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    public String getInputData() {
        return inputData;
    }

    public void setInputData(String inputData) {
        this.inputData = inputData;
    }

    public String getReceipt() {
        return receipt;
    }

    public void setReceipt(String receipt) {
        this.receipt = receipt;
    }

    public Integer getChainId() {
        return chainId;
    }

    public void setChainId(Integer chainId) {
        this.chainId = chainId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
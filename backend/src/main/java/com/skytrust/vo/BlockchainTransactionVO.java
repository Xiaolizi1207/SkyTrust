package com.skytrust.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 区块链交易记录视图对象（用于返回交易信息）
 *
 * @author SkyTrust Team
 */
@Data
@ApiModel(description = "区块链交易记录视图对象")
public class BlockchainTransactionVO {

    @ApiModelProperty(value = "交易记录ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "交易哈希（唯一）", example = "0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef")
    private String txHash;

    @ApiModelProperty(value = "智能合约地址", example = "0x742d35Cc6634C0532925a3b844Bc9e")
    private String contractAddress;

    @ApiModelProperty(value = "区块号", example = "12345678")
    private Long blockNumber;

    @ApiModelProperty(value = "区块哈希", example = "0xabcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890")
    private String blockHash;

    @ApiModelProperty(value = "交易发起方地址", example = "0x742d35Cc6634C0532925a3b844Bc9e")
    private String fromAddress;

    @ApiModelProperty(value = "交易接收方地址", example = "0x742d35Cc6634C0532925a3b844Bc9e")
    private String toAddress;

    @ApiModelProperty(value = "交易金额（ETH/代币数量）", example = "1.5")
    private BigDecimal amount;

    @ApiModelProperty(value = "交易gas费用", example = "0.001")
    private BigDecimal gasFee;

    @ApiModelProperty(value = "交易状态（0-待确认，1-已确认，2-失败）", example = "1")
    private Integer status;

    @ApiModelProperty(value = "确认时间", example = "2023-10-01T10:05:00")
    private LocalDateTime confirmTime;

    @ApiModelProperty(value = "交易类型（0-租赁合约创建，1-支付，2-保险，3-设备状态更新）", example = "1")
    private Integer txType;

    @ApiModelProperty(value = "关联业务ID（如订单ID、支付ID等）", example = "1")
    private Long businessId;

    @ApiModelProperty(value = "关联业务类型（order-订单，payment-支付，insurance-保险）", example = "payment")
    private String businessType;

    @ApiModelProperty(value = "交易输入数据（JSON格式）", example = "{\"method\": \"transfer\", \"params\": [\"0x...\", \"100\"]}")
    private String inputData;

    @ApiModelProperty(value = "交易收据（JSON格式）", example = "{\"status\": \"0x1\", \"gasUsed\": \"21000\"}")
    private String receipt;

    @ApiModelProperty(value = "网络ID（链ID）", example = "1")
    private Integer chainId;

    @ApiModelProperty(value = "备注", example = "租赁订单支付交易")
    private String remark;

    @ApiModelProperty(value = "创建时间", example = "2023-10-01T10:00:00")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间", example = "2023-10-01T10:05:00")
    private LocalDateTime updateTime;
}
package com.skytrust.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 区块链交易记录数据传输对象（用于创建交易记录）
 *
 * @author SkyTrust Team
 */
@Data
@Schema(description = "区块链交易记录数据传输对象")
public class BlockchainTransactionDTO {

    @Schema(description = "智能合约地址", example = "0x742d35Cc6634C0532925a3b844Bc9e", required = true)
    @NotBlank(message = "智能合约地址不能为空")
    @Size(max = 100, message = "智能合约地址长度不能超过100个字符")
    private String contractAddress;

    @Schema(description = "交易发起方地址", example = "0x742d35Cc6634C0532925a3b844Bc9e", required = true)
    @NotBlank(message = "发起方地址不能为空")
    @Size(max = 100, message = "发起方地址长度不能超过100个字符")
    private String fromAddress;

    @Schema(description = "交易接收方地址", example = "0x742d35Cc6634C0532925a3b844Bc9e", required = true)
    @NotBlank(message = "接收方地址不能为空")
    @Size(max = 100, message = "接收方地址长度不能超过100个字符")
    private String toAddress;

    @Schema(description = "交易金额（ETH/代币数量）", example = "1.5", required = true)
    @NotNull(message = "交易金额不能为空")
    @DecimalMin(value = "0.00000001", message = "交易金额必须大于0")
    @Digits(integer = 10, fraction = 8, message = "交易金额格式不正确")
    private BigDecimal amount;

    @Schema(description = "交易gas费用", example = "0.001", required = true)
    @NotNull(message = "gas费用不能为空")
    @DecimalMin(value = "0.00000001", message = "gas费用必须大于0")
    @Digits(integer = 10, fraction = 8, message = "gas费用格式不正确")
    private BigDecimal gasFee;

    @Schema(description = "交易类型（0-租赁合约创建，1-支付，2-保险，3-设备状态更新）", example = "1", required = true)
    @NotNull(message = "交易类型不能为空")
    private Integer txType;

    @Schema(description = "关联业务ID（如订单ID、支付ID等）", example = "1", required = true)
    @NotNull(message = "关联业务ID不能为空")
    private Long businessId;

    @Schema(description = "关联业务类型（order-订单，payment-支付，insurance-保险）", example = "payment", required = true)
    @NotBlank(message = "关联业务类型不能为空")
    @Size(max = 50, message = "关联业务类型长度不能超过50个字符")
    private String businessType;

    @Schema(description = "交易输入数据（JSON格式）", example = "{\"method\": \"transfer\", \"params\": [\"0x...\", \"100\"]}")
    @Size(max = 2000, message = "交易输入数据长度不能超过2000个字符")
    private String inputData;

    @Schema(description = "网络ID（链ID）", example = "1", required = true)
    @NotNull(message = "网络ID不能为空")
    private Integer chainId;

    @Schema(description = "备注", example = "租赁订单支付交易")
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;
}
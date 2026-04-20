package com.skytrust.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 智能合约数据传输对象（用于创建智能合约记录）
 *
 * @author SkyTrust Team
 */
@Data
@ApiModel(description = "智能合约数据传输对象")
public class SmartContractDTO {

    @ApiModelProperty(value = "合约名称", example = "租赁合约V1", required = true)
    @NotBlank(message = "合约名称不能为空")
    @Size(max = 100, message = "合约名称长度不能超过100个字符")
    private String contractName;

    @ApiModelProperty(value = "合约地址（唯一）", example = "0x742d35Cc6634C0532925a3b844Bc9e", required = true)
    @NotBlank(message = "合约地址不能为空")
    @Size(max = 100, message = "合约地址长度不能超过100个字符")
    private String contractAddress;

    @ApiModelProperty(value = "合约ABI（JSON格式）", example = "[{\"inputs\":[],\"name\":\"getBalance\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}]}]", required = true)
    @NotBlank(message = "合约ABI不能为空")
    @Size(max = 10000, message = "合约ABI长度不能超过10000个字符")
    private String contractAbi;

    @ApiModelProperty(value = "合约字节码", example = "0x608060405234801561001057600080fd5b5060", required = true)
    @NotBlank(message = "合约字节码不能为空")
    @Size(max = 10000, message = "合约字节码长度不能超过10000个字符")
    private String contractBytecode;

    @ApiModelProperty(value = "合约类型（0-租赁合约，1-支付合约，2-保险合约，3-设备合约）", example = "0", required = true)
    @NotNull(message = "合约类型不能为空")
    private Integer contractType;

    @ApiModelProperty(value = "合约版本", example = "1.0.0", required = true)
    @NotBlank(message = "合约版本不能为空")
    @Size(max = 20, message = "合约版本长度不能超过20个字符")
    private String version;

    @ApiModelProperty(value = "部署交易哈希", example = "0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef", required = true)
    @NotBlank(message = "部署交易哈希不能为空")
    @Size(max = 100, message = "部署交易哈希长度不能超过100个字符")
    private String deployTxHash;

    @ApiModelProperty(value = "部署者地址", example = "0x742d35Cc6634C0532925a3b844Bc9e", required = true)
    @NotBlank(message = "部署者地址不能为空")
    @Size(max = 100, message = "部署者地址长度不能超过100个字符")
    private String deployerAddress;

    @ApiModelProperty(value = "部署网络ID（链ID）", example = "1", required = true)
    @NotNull(message = "部署网络ID不能为空")
    private Integer chainId;

    @ApiModelProperty(value = "部署时间", example = "2023-10-01T10:00:00", required = true)
    @NotNull(message = "部署时间不能为空")
    private LocalDateTime deployTime;

    @ApiModelProperty(value = "合约状态（0-未激活，1-已激活，2-已暂停，3-已终止）", example = "1", required = true)
    @NotNull(message = "合约状态不能为空")
    private Integer contractStatus;

    @ApiModelProperty(value = "是否可升级（0-否，1-是）", example = "true", required = true)
    @NotNull(message = "是否可升级不能为空")
    private Boolean upgradable;

    @ApiModelProperty(value = "升级合约地址", example = "0x742d35Cc6634C0532925a3b844Bc9e")
    @Size(max = 100, message = "升级合约地址长度不能超过100个字符")
    private String upgradeAddress;

    @ApiModelProperty(value = "合约拥有者地址", example = "0x742d35Cc6634C0532925a3b844Bc9e", required = true)
    @NotBlank(message = "合约拥有者地址不能为空")
    @Size(max = 100, message = "合约拥有者地址长度不能超过100个字符")
    private String ownerAddress;

    @ApiModelProperty(value = "合约描述", example = "用于无人机租赁的智能合约")
    @Size(max = 500, message = "合约描述长度不能超过500个字符")
    private String description;

    @ApiModelProperty(value = "备注", example = "主网部署合约")
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;
}
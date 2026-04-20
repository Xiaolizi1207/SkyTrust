package com.skytrust.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 智能合约实体类
 *
 * @author SkyTrust Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("smart_contract")
public class SmartContract extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 合约名称
     */
    @TableField(value = "contract_name")
    private String contractName;

    /**
     * 合约地址（唯一）
     */
    @TableField(value = "contract_address")
    private String contractAddress;

    /**
     * 合约ABI（JSON格式）
     */
    @TableField(value = "contract_abi")
    private String contractAbi;

    /**
     * 合约字节码
     */
    @TableField(value = "contract_bytecode")
    private String contractBytecode;

    /**
     * 合约类型（0-租赁合约，1-支付合约，2-保险合约，3-设备合约）
     */
    @TableField(value = "contract_type")
    private Integer contractType;

    /**
     * 合约版本
     */
    @TableField(value = "version")
    private String version;

    /**
     * 部署交易哈希
     */
    @TableField(value = "deploy_tx_hash")
    private String deployTxHash;

    /**
     * 部署者地址
     */
    @TableField(value = "deployer_address")
    private String deployerAddress;

    /**
     * 部署网络ID（链ID）
     */
    @TableField(value = "chain_id")
    private Integer chainId;

    /**
     * 部署时间
     */
    @TableField(value = "deploy_time")
    private LocalDateTime deployTime;

    /**
     * 合约状态（0-未激活，1-已激活，2-已暂停，3-已终止）
     */
    @TableField(value = "contract_status")
    private Integer contractStatus;

    /**
     * 是否可升级（0-否，1-是）
     */
    @TableField(value = "upgradable")
    private Boolean upgradable;

    /**
     * 升级合约地址
     */
    @TableField(value = "upgrade_address")
    private String upgradeAddress;

    /**
     * 合约拥有者地址
     */
    @TableField(value = "owner_address")
    private String ownerAddress;

    /**
     * 合约描述
     */
    @TableField(value = "description")
    private String description;

    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;

    // Getter methods
    public String getContractName() {
        return contractName;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public Integer getContractType() {
        return contractType;
    }

    public Integer getContractStatus() {
        return contractStatus;
    }

    // Setter methods
    public void setContractName(String contractName) {
        this.contractName = contractName;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public void setContractType(Integer contractType) {
        this.contractType = contractType;
    }

    public void setContractStatus(Integer contractStatus) {
        this.contractStatus = contractStatus;
    }
}
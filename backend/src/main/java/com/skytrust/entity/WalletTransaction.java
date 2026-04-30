package com.skytrust.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 钱包交易记录实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wallet_transaction")
public class WalletTransaction extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableField(value = "user_id")
    private Long userId;

    /** 交易类型: 0-充值, 1-消费, 2-退款, 3-提现 */
    @TableField(value = "type")
    private Integer type;

    @TableField(value = "amount")
    private BigDecimal amount;

    @TableField(value = "balance_before")
    private BigDecimal balanceBefore;

    @TableField(value = "balance_after")
    private BigDecimal balanceAfter;

    @TableField(value = "description")
    private String description;

    @TableField(value = "order_id")
    private Long orderId;

    /** 交易状态: 0-成功, 1-失败, 2-处理中 */
    @TableField(value = "status")
    private Integer status;

    @TableField(value = "remark")
    private String remark;
}

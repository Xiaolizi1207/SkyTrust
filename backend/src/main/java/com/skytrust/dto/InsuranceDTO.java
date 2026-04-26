package com.skytrust.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 保险记录数据传输对象（用于创建保险记录）
 *
 * @author SkyTrust Team
 */
@Data
@Schema(description = "保险记录数据传输对象")
public class InsuranceDTO {

    @Schema(description = "关联订单ID", example = "1", required = true)
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @Schema(description = "用户ID", example = "1", required = true)
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @Schema(description = "设备ID", example = "1", required = true)
    @NotNull(message = "设备ID不能为空")
    private Long deviceId;

    @Schema(description = "保险类型（0-基础保险，1-高级保险，2-全险）", example = "1", required = true)
    @NotNull(message = "保险类型不能为空")
    private Integer insuranceType;

    @Schema(description = "保险金额（元）", example = "500.00", required = true)
    @NotNull(message = "保险金额不能为空")
    @DecimalMin(value = "0.01", message = "保险金额必须大于0")
    @Digits(integer = 10, fraction = 2, message = "保险金额格式不正确")
    private BigDecimal amount;

    @Schema(description = "保险费率（百分比）", example = "5.00", required = true)
    @NotNull(message = "保险费率不能为空")
    @DecimalMin(value = "0.01", message = "保险费率必须大于0")
    @Digits(integer = 3, fraction = 2, message = "保险费率格式不正确")
    private BigDecimal rate;

    @Schema(description = "保险开始时间", example = "2023-10-01T10:00:00", required = true)
    @NotNull(message = "保险开始时间不能为空")
    private LocalDateTime startTime;

    @Schema(description = "保险结束时间", example = "2023-12-01T10:00:00", required = true)
    @NotNull(message = "保险结束时间不能为空")
    private LocalDateTime endTime;

    @Schema(description = "保险条款JSON", example = "{\"coverage\": \"设备损坏\", \"deductible\": 100}")
    @Size(max = 2000, message = "保险条款长度不能超过2000个字符")
    private String terms;

    @Schema(description = "备注", example = "租赁设备保险")
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;
}
package com.skytrust.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Data;

/**
 * 字典数据传输对象
 *
 * @author SkyTrust Team
 */
@Data
@Schema(description = "字典数据传输对象")
public class DictDataDTO {

    @Schema(description = "字典标签（显示文本）", example = "男", required = true)
    @NotBlank(message = "字典标签不能为空")
    @Size(min = 1, max = 100, message = "字典标签长度必须在1-100个字符之间")
    private String dictLabel;

    @Schema(description = "字典值（存储值）", example = "male", required = true)
    @NotBlank(message = "字典值不能为空")
    @Size(min = 1, max = 100, message = "字典值长度必须在1-100个字符之间")
    private String dictValue;

    @Schema(description = "字典类型编码", example = "gender", required = true)
    @NotBlank(message = "字典类型编码不能为空")
    @Size(min = 1, max = 50, message = "字典类型编码长度必须在1-50个字符之间")
    private String dictType;

    @Schema(description = "状态（0-禁用，1-启用）", example = "1")
    private Integer status = 1;

    @Schema(description = "排序（数字越小越靠前）", example = "0")
    private Integer sortOrder = 0;

    @Schema(description = "CSS样式类（前端显示用）", example = "badge badge-success")
    @Size(max = 100, message = "CSS样式类长度不能超过100个字符")
    private String cssClass;

    @Schema(description = "列表样式（前端显示用）", example = "success")
    @Size(max = 50, message = "列表样式长度不能超过50个字符")
    private String listClass;

    @Schema(description = "是否默认（0-否，1-是）", example = "0")
    private Integer isDefault = 0;

    @Schema(description = "备注", example = "男性")
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;
}
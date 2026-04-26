package com.skytrust.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Data;

/**
 * 字典类型数据传输对象
 *
 * @author SkyTrust Team
 */
@Data
@Schema(description = "字典类型数据传输对象")
public class DictTypeDTO {

    @Schema(description = "字典类型编码（唯一）", example = "gender", required = true)
    @NotBlank(message = "字典类型编码不能为空")
    @Size(min = 1, max = 50, message = "字典类型编码长度必须在1-50个字符之间")
    private String typeCode;

    @Schema(description = "字典类型名称", example = "性别", required = true)
    @NotBlank(message = "字典类型名称不能为空")
    @Size(min = 1, max = 100, message = "字典类型名称长度必须在1-100个字符之间")
    private String typeName;

    @Schema(description = "状态（0-禁用，1-启用）", example = "1")
    private Integer status = 1;

    @Schema(description = "排序（数字越小越靠前）", example = "0")
    private Integer sortOrder = 0;

    @Schema(description = "备注", example = "性别字典")
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;
}
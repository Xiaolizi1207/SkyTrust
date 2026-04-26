package com.skytrust.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 字典类型视图对象
 *
 * @author SkyTrust Team
 */
@Data
@Schema(description = "字典类型视图对象")
public class DictTypeVO {

    @Schema(description = "字典类型ID", example = "1")
    private Long id;

    @Schema(description = "字典类型编码（唯一）", example = "gender")
    private String typeCode;

    @Schema(description = "字典类型名称", example = "性别")
    private String typeName;

    @Schema(description = "状态（0-禁用，1-启用）", example = "1")
    private Integer status = 1;

    @Schema(description = "排序（数字越小越靠前）", example = "0")
    private Integer sortOrder = 0;

    @Schema(description = "备注", example = "性别字典")
    private String remark;

    @Schema(description = "创建时间", example = "2023-10-01 12:00:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2023-10-01 12:00:00")
    private LocalDateTime updateTime;
}
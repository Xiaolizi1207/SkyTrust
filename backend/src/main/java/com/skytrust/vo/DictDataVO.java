package com.skytrust.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 字典数据视图对象
 *
 * @author SkyTrust Team
 */
@Data
@Schema(description = "字典数据视图对象")
public class DictDataVO {

    @Schema(description = "字典数据ID", example = "1")
    private Long id;

    @Schema(description = "字典标签（显示文本）", example = "男")
    private String dictLabel;

    @Schema(description = "字典值（存储值）", example = "male")
    private String dictValue;

    @Schema(description = "字典类型编码", example = "gender")
    private String dictType;

    @Schema(description = "状态（0-禁用，1-启用）", example = "1")
    private Integer status = 1;

    @Schema(description = "排序（数字越小越靠前）", example = "0")
    private Integer sortOrder = 0;

    @Schema(description = "CSS样式类（前端显示用）", example = "badge badge-success")
    private String cssClass;

    @Schema(description = "列表样式（前端显示用）", example = "success")
    private String listClass;

    @Schema(description = "是否默认（0-否，1-是）", example = "0")
    private Integer isDefault = 0;

    @Schema(description = "备注", example = "男性")
    private String remark;

    @Schema(description = "创建时间", example = "2023-10-01 12:00:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2023-10-01 12:00:00")
    private LocalDateTime updateTime;
}
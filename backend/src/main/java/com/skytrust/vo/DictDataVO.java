package com.skytrust.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 字典数据视图对象
 *
 * @author SkyTrust Team
 */
@Data
@ApiModel(description = "字典数据视图对象")
public class DictDataVO {

    @ApiModelProperty(value = "字典数据ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "字典标签（显示文本）", example = "男")
    private String dictLabel;

    @ApiModelProperty(value = "字典值（存储值）", example = "male")
    private String dictValue;

    @ApiModelProperty(value = "字典类型编码", example = "gender")
    private String dictType;

    @ApiModelProperty(value = "状态（0-禁用，1-启用）", example = "1")
    private Integer status = 1;

    @ApiModelProperty(value = "排序（数字越小越靠前）", example = "0")
    private Integer sortOrder = 0;

    @ApiModelProperty(value = "CSS样式类（前端显示用）", example = "badge badge-success")
    private String cssClass;

    @ApiModelProperty(value = "列表样式（前端显示用）", example = "success")
    private String listClass;

    @ApiModelProperty(value = "是否默认（0-否，1-是）", example = "0")
    private Integer isDefault = 0;

    @ApiModelProperty(value = "备注", example = "男性")
    private String remark;

    @ApiModelProperty(value = "创建时间", example = "2023-10-01 12:00:00")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间", example = "2023-10-01 12:00:00")
    private LocalDateTime updateTime;
}
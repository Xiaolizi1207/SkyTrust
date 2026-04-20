package com.skytrust.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传数据传输对象
 *
 * @author SkyTrust Team
 */
@Data
@ApiModel(description = "文件上传数据传输对象")
public class FileUploadDTO {

    @ApiModelProperty(value = "上传的文件", required = true)
    private MultipartFile file;

    @ApiModelProperty(value = "业务类型", example = "DEVICE_IMAGE")
    @Size(max = 50, message = "业务类型长度不能超过50个字符")
    private String bizType;

    @ApiModelProperty(value = "业务ID", example = "1001")
    private Long bizId;

    @ApiModelProperty(value = "文件描述", example = "设备图片")
    @Size(max = 200, message = "文件描述长度不能超过200个字符")
    private String description;

    @ApiModelProperty(value = "是否公开", example = "true")
    private Boolean isPublic = true;
}
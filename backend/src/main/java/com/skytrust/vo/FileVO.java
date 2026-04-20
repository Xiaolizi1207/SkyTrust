package com.skytrust.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 文件信息视图对象
 *
 * @author SkyTrust Team
 */
@Data
@ApiModel(description = "文件信息视图对象")
public class FileVO {

    @ApiModelProperty(value = "文件ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "原始文件名", example = "document.pdf")
    private String originalName;

    @ApiModelProperty(value = "存储文件名", example = "20260405_12345678.pdf")
    private String storedName;

    @ApiModelProperty(value = "文件路径", example = "/uploads/2026/04/05/20260405_12345678.pdf")
    private String filePath;

    @ApiModelProperty(value = "文件大小（字节）", example = "102400")
    private Long fileSize;

    @ApiModelProperty(value = "文件类型", example = "application/pdf")
    private String fileType;

    @ApiModelProperty(value = "文件扩展名", example = "pdf")
    private String extension;

    @ApiModelProperty(value = "上传用户ID", example = "1")
    private Long uploadUserId;

    @ApiModelProperty(value = "上传用户名", example = "john_doe")
    private String uploadUsername;

    @ApiModelProperty(value = "业务类型", example = "DEVICE_IMAGE")
    private String bizType;

    @ApiModelProperty(value = "业务ID", example = "1001")
    private Long bizId;

    @ApiModelProperty(value = "文件描述", example = "设备图片")
    private String description;

    @ApiModelProperty(value = "MD5哈希值", example = "e10adc3949ba59abbe56e057f20f883e")
    private String md5Hash;

    @ApiModelProperty(value = "SHA-256哈希值", example = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")
    private String sha256Hash;

    @ApiModelProperty(value = "上传时间", example = "2026-04-05 10:30:00")
    private LocalDateTime uploadTime;

    @ApiModelProperty(value = "下载次数", example = "5")
    private Integer downloadCount = 0;

    @ApiModelProperty(value = "是否公开", example = "true")
    private Boolean isPublic = true;

    @ApiModelProperty(value = "文件状态", example = "1")
    private Integer status = 1; // 1-正常, 0-已删除, 2-隐藏

    @ApiModelProperty(value = "创建时间", example = "2026-04-05 10:30:00")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间", example = "2026-04-05 10:30:00")
    private LocalDateTime updateTime;
}
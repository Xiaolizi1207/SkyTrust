package com.skytrust.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 文件信息视图对象
 *
 * @author SkyTrust Team
 */
@Data
@Schema(description = "文件信息视图对象")
public class FileVO {

    @Schema(description = "文件ID", example = "1")
    private Long id;

    @Schema(description = "原始文件名", example = "document.pdf")
    private String originalName;

    @Schema(description = "存储文件名", example = "20260405_12345678.pdf")
    private String storedName;

    @Schema(description = "文件路径", example = "/uploads/2026/04/05/20260405_12345678.pdf")
    private String filePath;

    @Schema(description = "文件大小（字节）", example = "102400")
    private Long fileSize;

    @Schema(description = "文件类型", example = "application/pdf")
    private String fileType;

    @Schema(description = "文件扩展名", example = "pdf")
    private String extension;

    @Schema(description = "上传用户ID", example = "1")
    private Long uploadUserId;

    @Schema(description = "上传用户名", example = "john_doe")
    private String uploadUsername;

    @Schema(description = "业务类型", example = "DEVICE_IMAGE")
    private String bizType;

    @Schema(description = "业务ID", example = "1001")
    private Long bizId;

    @Schema(description = "文件描述", example = "设备图片")
    private String description;

    @Schema(description = "MD5哈希值", example = "e10adc3949ba59abbe56e057f20f883e")
    private String md5Hash;

    @Schema(description = "SHA-256哈希值", example = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")
    private String sha256Hash;

    @Schema(description = "上传时间", example = "2026-04-05 10:30:00")
    private LocalDateTime uploadTime;

    @Schema(description = "下载次数", example = "5")
    private Integer downloadCount = 0;

    @Schema(description = "是否公开", example = "true")
    private Boolean isPublic = true;

    @Schema(description = "文件状态", example = "1")
    private Integer status = 1; // 1-正常, 0-已删除, 2-隐藏

    @Schema(description = "创建时间", example = "2026-04-05 10:30:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2026-04-05 10:30:00")
    private LocalDateTime updateTime;
}
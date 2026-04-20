package com.skytrust.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 文件信息实体类
 *
 * @author SkyTrust Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("file_info")
public class FileInfo extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 原始文件名
     */
    @TableField("original_name")
    private String originalName;

    /**
     * 存储文件名（唯一）
     */
    @TableField("stored_name")
    private String storedName;

    /**
     * 文件路径（相对路径）
     */
    @TableField("file_path")
    private String filePath;

    /**
     * 文件大小（字节）
     */
    @TableField("file_size")
    private Long fileSize;

    /**
     * 文件类型（MIME类型）
     */
    @TableField("file_type")
    private String fileType;

    /**
     * 文件扩展名
     */
    @TableField("extension")
    private String extension;

    /**
     * 上传用户ID
     */
    @TableField("upload_user_id")
    private Long uploadUserId;

    /**
     * 上传用户名
     */
    @TableField("upload_username")
    private String uploadUsername;

    /**
     * 业务类型（如：DEVICE_IMAGE, USER_AVATAR, DOCUMENT等）
     */
    @TableField("biz_type")
    private String bizType;

    /**
     * 业务ID（关联的业务记录ID）
     */
    @TableField("biz_id")
    private Long bizId;

    /**
     * 文件描述
     */
    @TableField("description")
    private String description;

    /**
     * MD5哈希值（用于文件去重和校验）
     */
    @TableField("md5_hash")
    private String md5Hash;

    /**
     * SHA-256哈希值
     */
    @TableField("sha256_hash")
    private String sha256Hash;

    /**
     * 上传时间
     */
    @TableField("upload_time")
    private LocalDateTime uploadTime;

    /**
     * 下载次数
     */
    @TableField("download_count")
    private Integer downloadCount = 0;

    /**
     * 是否公开
     */
    @TableField("is_public")
    private Boolean isPublic = true;

    /**
     * 文件状态（1-正常，0-已删除，2-隐藏）
     */
    @TableField("status")
    private Integer status = 1;

    /**
     * 最后下载时间
     */
    @TableField("last_download_time")
    private LocalDateTime lastDownloadTime;

    /**
     * 访问权限（JSON格式，如：{"roles":["admin","user"], "users":[1,2,3]}）
     */
    @TableField("access_permission")
    private String accessPermission;

    /**
     * 存储类型（local-本地存储，oss-对象存储，cos-腾讯云存储等）
     */
    @TableField("storage_type")
    private String storageType = "local";

    /**
     * 存储配置（JSON格式，存储相关配置信息）
     */
    @TableField("storage_config")
    private String storageConfig;
}
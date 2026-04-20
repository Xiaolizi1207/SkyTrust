package com.skytrust.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.skytrust.dto.FileUploadDTO;
import com.skytrust.entity.FileInfo;
import com.skytrust.vo.FileVO;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 文件上传服务接口
 *
 * @author SkyTrust Team
 */
public interface FileUploadService extends com.baomidou.mybatisplus.extension.service.IService<FileInfo> {

    /**
     * 上传单个文件
     *
     * @param fileUploadDTO 文件上传DTO
     * @return 文件信息VO
     */
    FileVO uploadFile(FileUploadDTO fileUploadDTO);

    /**
     * 上传多个文件
     *
     * @param files      文件数组
     * @param bizType    业务类型
     * @param bizId      业务ID
     * @param description 文件描述
     * @param isPublic   是否公开
     * @return 文件信息VO列表
     */
    List<FileVO> uploadMultipleFiles(MultipartFile[] files, String bizType, Long bizId, String description, Boolean isPublic);

    /**
     * 根据文件ID下载文件
     *
     * @param id       文件ID
     * @param response HttpServletResponse
     */
    void downloadFile(Long id, HttpServletResponse response);

    /**
     * 根据文件路径下载文件
     *
     * @param filePath     文件路径
     * @param originalName 原始文件名（可选）
     * @param response     HttpServletResponse
     */
    void downloadFileByPath(String filePath, String originalName, HttpServletResponse response);

    /**
     * 获取文件信息
     *
     * @param id 文件ID
     * @return 文件信息VO
     */
    FileVO getFileInfo(Long id);

    /**
     * 删除文件（逻辑删除）
     *
     * @param id 文件ID
     * @return 是否成功
     */
    boolean deleteFile(Long id);

    /**
     * 批量删除文件
     *
     * @param fileIds 文件ID列表
     * @return 是否成功
     */
    boolean batchDeleteFiles(List<Long> fileIds);

    /**
     * 获取文件列表（分页）
     *
     * @param page         页码
     * @param size         每页大小
     * @param originalName 原始文件名（模糊查询）
     * @param bizType      业务类型
     * @param bizId        业务ID
     * @param uploadUserId 上传用户ID
     * @param isPublic     是否公开
     * @param status       状态
     * @param startTime    开始时间
     * @param endTime      结束时间
     * @param orderBy      排序字段
     * @return 分页结果
     */
    IPage<FileVO> getFileList(Integer page, Integer size,
                              String originalName, String bizType, Long bizId,
                              Long uploadUserId, Boolean isPublic, Integer status,
                              String startTime, String endTime, String orderBy);

    /**
     * 检查文件是否存在
     *
     * @param filePath 文件路径
     * @return 是否存在
     */
    boolean checkFileExists(String filePath);

    /**
     * 获取文件大小
     *
     * @param filePath 文件路径
     * @return 文件大小（字节）
     */
    long getFileSize(String filePath);

    /**
     * 根据文件哈希值查找重复文件
     *
     * @param md5Hash MD5哈希值
     * @return 文件信息列表
     */
    List<FileInfo> findDuplicateFilesByMd5(String md5Hash);

    /**
     * 根据文件哈希值查找重复文件
     *
     * @param sha256Hash SHA256哈希值
     * @return 文件信息列表
     */
    List<FileInfo> findDuplicateFilesBySha256(String sha256Hash);

    /**
     * 更新文件下载次数
     *
     * @param id 文件ID
     * @return 是否成功
     */
    boolean updateDownloadCount(Long id);

    /**
     * 根据业务类型和业务ID查询文件
     *
     * @param bizType 业务类型
     * @param bizId   业务ID
     * @return 文件信息列表
     */
    List<FileInfo> getFilesByBizTypeAndId(String bizType, Long bizId);

    /**
     * 根据上传用户ID查询文件
     *
     * @param uploadUserId 上传用户ID
     * @return 文件信息列表
     */
    List<FileInfo> getFilesByUploadUserId(Long uploadUserId);

    /**
     * 恢复已删除的文件
     *
     * @param id 文件ID
     * @return 是否成功
     */
    boolean restoreFile(Long id);

    /**
     * 批量恢复已删除的文件
     *
     * @param fileIds 文件ID列表
     * @return 是否成功
     */
    boolean batchRestoreFiles(List<Long> fileIds);

    /**
     * 物理删除文件（删除文件记录和实际文件）
     *
     * @param id 文件ID
     * @return 是否成功
     */
    boolean physicalDeleteFile(Long id);

    /**
     * 批量物理删除文件
     *
     * @param fileIds 文件ID列表
     * @return 是否成功
     */
    boolean batchPhysicalDeleteFiles(List<Long> fileIds);

    /**
     * 生成文件访问URL
     *
     * @param id 文件ID
     * @return 访问URL
     */
    String generateFileUrl(Long id);

    /**
     * 生成文件访问URL（带过期时间）
     *
     * @param id        文件ID
     * @param expireSeconds 过期时间（秒）
     * @return 访问URL
     */
    String generateFileUrlWithExpire(Long id, Long expireSeconds);
}
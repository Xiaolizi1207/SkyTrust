package com.skytrust.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.skytrust.common.ResultCode;
import com.skytrust.common.utils.*;
import com.skytrust.dto.FileUploadDTO;
import com.skytrust.entity.FileInfo;
import com.skytrust.entity.User;
import com.skytrust.exception.BusinessException;
import com.skytrust.exception.FileUploadException;
import com.skytrust.mapper.FileInfoMapper;
import com.skytrust.service.FileUploadService;
import com.skytrust.service.UserService;
import com.skytrust.vo.FileVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传服务实现类
 *
 * @author SkyTrust Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadServiceImpl extends BaseService<FileInfoMapper, FileInfo> implements FileUploadService {

    @Value("${skytrust.upload.path:${user.home}/skytrust/uploads}")
    private String uploadBasePath;

    @Value("${skytrust.upload.max-size:104857600}")
    private String maxFileSize; // 100MB默认值

    @Value("${skytrust.upload.allowed-types:jpg,jpeg,png,gif,pdf,doc,docx,xls,xlsx}")
    private String allowedTypes;

    private final UserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileVO uploadFile(FileUploadDTO fileUploadDTO) {
        MultipartFile multipartFile = fileUploadDTO.getFile();
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new FileUploadException("上传的文件不能为空");
        }

        // 1. 验证文件大小
        validateFileSize(multipartFile.getSize());

        // 2. 验证文件类型
        validateFileType(multipartFile.getOriginalFilename());

        // 3. 获取当前用户信息
        User currentUser = getCurrentUser();

        try {
            // 4. 生成存储路径和文件名
            String originalFilename = multipartFile.getOriginalFilename();
            String fileExtension = FileUtil.getFileExtension(originalFilename);
            String storedFilename = FileUtil.generateUniqueFileName(originalFilename);
            String datePath = FileUtil.generateDatePath();
            String relativePath = Paths.get(datePath, storedFilename).toString();
            String fullPath = Paths.get(uploadBasePath, relativePath).toString();

            // 5. 创建目录
            createDirectoryIfNotExists(fullPath);

            // 6. 计算文件哈希值
            byte[] fileBytes = multipartFile.getBytes();
            String md5Hash = calculateMd5(fileBytes);
            String sha256Hash = calculateSha256(fileBytes);

            // 7. 检查重复文件
            List<FileInfo> duplicateFiles = findDuplicateFilesByMd5(md5Hash);
            if (!duplicateFiles.isEmpty()) {
                log.info("检测到重复文件: MD5={}, 原始文件={}", md5Hash, originalFilename);
                // 返回已存在的文件信息
                FileInfo existingFile = duplicateFiles.get(0);
                return convertToVO(existingFile);
            }

            // 8. 保存文件到磁盘
            Path targetPath = Paths.get(fullPath);
            multipartFile.transferTo(targetPath.toFile());

            // 9. 保存文件信息到数据库
            FileInfo fileInfo = new FileInfo();
            fileInfo.setOriginalName(originalFilename);
            fileInfo.setStoredName(storedFilename);
            fileInfo.setFilePath(relativePath);
            fileInfo.setFileSize(multipartFile.getSize());
            fileInfo.setFileType(multipartFile.getContentType());
            fileInfo.setExtension(fileExtension);
            fileInfo.setUploadUserId(currentUser != null ? currentUser.getId() : null);
            fileInfo.setUploadUsername(currentUser != null ? currentUser.getUsername() : "system");
            fileInfo.setBizType(fileUploadDTO.getBizType());
            fileInfo.setBizId(fileUploadDTO.getBizId());
            fileInfo.setDescription(fileUploadDTO.getDescription());
            fileInfo.setMd5Hash(md5Hash);
            fileInfo.setSha256Hash(sha256Hash);
            fileInfo.setUploadTime(LocalDateTime.now());
            fileInfo.setIsPublic(fileUploadDTO.getIsPublic() != null ? fileUploadDTO.getIsPublic() : true);
            fileInfo.setStatus(1); // 正常状态
            fileInfo.setDownloadCount(0);
            fileInfo.setStorageType("local");

            boolean saved = save(fileInfo);
            if (!saved) {
                throw new FileUploadException("文件信息保存失败");
            }

            log.info("文件上传成功: ID={}, 原始文件名={}, 存储路径={}",
                    fileInfo.getId(), originalFilename, relativePath);

            return convertToVO(fileInfo);
        } catch (IOException e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            throw new FileUploadException("文件上传失败: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("文件上传异常: {}", e.getMessage(), e);
            throw new FileUploadException("文件上传异常: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<FileVO> uploadMultipleFiles(MultipartFile[] files, String bizType, Long bizId,
                                            String description, Boolean isPublic) {
        if (files == null || files.length == 0) {
            throw new FileUploadException("请选择要上传的文件");
        }

        List<FileVO> fileVOs = new ArrayList<>();
        for (MultipartFile file : files) {
            try {
                FileUploadDTO fileUploadDTO = new FileUploadDTO();
                fileUploadDTO.setFile(file);
                fileUploadDTO.setBizType(bizType);
                fileUploadDTO.setBizId(bizId);
                fileUploadDTO.setDescription(description);
                fileUploadDTO.setIsPublic(isPublic);

                FileVO fileVO = uploadFile(fileUploadDTO);
                fileVOs.add(fileVO);
            } catch (Exception e) {
                log.error("多文件上传中单个文件上传失败: {}", file.getOriginalFilename(), e);
                // 继续处理其他文件，不中断整个上传过程
            }
        }

        if (fileVOs.isEmpty()) {
            throw new FileUploadException("所有文件上传失败");
        }

        return fileVOs;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void downloadFile(Long id, HttpServletResponse response) {
        FileInfo fileInfo = getById(id);
        if (fileInfo == null) {
            throw new FileUploadException("文件不存在");
        }

        if (Boolean.TRUE.equals(fileInfo.getDeleted())) {
            throw new FileUploadException("文件已被删除");
        }

        if (fileInfo.getStatus() != 1) {
            throw new FileUploadException("文件状态异常");
        }

        // 权限检查：非公开文件需要权限验证
        if (Boolean.FALSE.equals(fileInfo.getIsPublic())) {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                throw new FileUploadException("无权限访问此文件");
            }
            // TODO: 更详细的权限检查
        }

        String filePath = Paths.get(uploadBasePath, fileInfo.getFilePath()).toString();
        downloadFileInternal(filePath, fileInfo.getOriginalName(), response);

        // 更新下载次数
        updateDownloadCount(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void downloadFileByPath(String filePath, String originalName, HttpServletResponse response) {
        if (StringUtil.isEmpty(filePath)) {
            throw new FileUploadException("文件路径不能为空");
        }

        String fullPath = Paths.get(uploadBasePath, filePath).toString();
        downloadFileInternal(fullPath, originalName, response);
    }

    @Override
    public FileVO getFileInfo(Long id) {
        FileInfo fileInfo = getById(id);
        if (fileInfo == null) {
            throw new FileUploadException("文件不存在");
        }

        if (Boolean.TRUE.equals(fileInfo.getDeleted())) {
            throw new FileUploadException("文件已被删除");
        }

        // 权限检查
        if (Boolean.FALSE.equals(fileInfo.getIsPublic())) {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                throw new FileUploadException("无权限查看此文件信息");
            }
        }

        return convertToVO(fileInfo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteFile(Long id) {
        FileInfo fileInfo = getById(id);
        if (fileInfo == null) {
            throw new FileUploadException("文件不存在");
        }

        // 权限检查：只有管理员或文件上传者可以删除
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new FileUploadException("无权限删除文件");
        }

        boolean isAdmin = SecurityUtil.isAdmin();
        boolean isOwner = currentUser.getId().equals(fileInfo.getUploadUserId());
        if (!isAdmin && !isOwner) {
            throw new FileUploadException("无权限删除此文件");
        }

        // 逻辑删除
        fileInfo.setDeleted(true);
        fileInfo.setStatus(0); // 设置为已删除状态
        fileInfo.setUpdateTime(LocalDateTime.now());

        boolean updated = updateById(fileInfo);
        if (updated) {
            log.info("文件逻辑删除成功: ID={}, 文件名={}", id, fileInfo.getOriginalName());
        }

        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDeleteFiles(List<Long> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) {
            throw new FileUploadException("文件ID列表不能为空");
        }

        boolean allSuccess = true;
        for (Long fileId : fileIds) {
            try {
                boolean deleted = deleteFile(fileId);
                if (!deleted) {
                    allSuccess = false;
                    log.warn("批量删除文件中单个文件删除失败: ID={}", fileId);
                }
            } catch (Exception e) {
                allSuccess = false;
                log.error("批量删除文件异常: ID={}", fileId, e);
            }
        }

        return allSuccess;
    }

    @Override
    public IPage<FileVO> getFileList(Integer page, Integer size,
                                     String originalName, String bizType, Long bizId,
                                     Long uploadUserId, Boolean isPublic, Integer status,
                                     String startTime, String endTime, String orderBy) {
        // 构建查询条件
        LambdaQueryWrapper<FileInfo> queryWrapper = new LambdaQueryWrapper<>();

        // 未删除的文件
        queryWrapper.eq(FileInfo::getDeleted, false);

        if (StringUtil.isNotEmpty(originalName)) {
            queryWrapper.like(FileInfo::getOriginalName, originalName);
        }

        if (StringUtil.isNotEmpty(bizType)) {
            queryWrapper.eq(FileInfo::getBizType, bizType);
        }

        if (bizId != null) {
            queryWrapper.eq(FileInfo::getBizId, bizId);
        }

        if (uploadUserId != null) {
            queryWrapper.eq(FileInfo::getUploadUserId, uploadUserId);
        }

        if (isPublic != null) {
            queryWrapper.eq(FileInfo::getIsPublic, isPublic);
        }

        if (status != null) {
            queryWrapper.eq(FileInfo::getStatus, status);
        }

        // 权限检查：非管理员只能查看公开文件或自己上传的文件
        User currentUser = getCurrentUser();
        if (currentUser != null && !SecurityUtil.isAdmin()) {
            queryWrapper.and(wrapper -> wrapper
                    .eq(FileInfo::getIsPublic, true)
                    .or()
                    .eq(FileInfo::getUploadUserId, currentUser.getId())
            );
        }

        // 时间范围查询
        if (StringUtil.isNotEmpty(startTime)) {
            LocalDateTime start = DateUtil.parseDateTime(startTime, "yyyy-MM-dd HH:mm:ss");
            if (start != null) {
                queryWrapper.ge(FileInfo::getUploadTime, start);
            }
        }

        if (StringUtil.isNotEmpty(endTime)) {
            LocalDateTime end = DateUtil.parseDateTime(endTime, "yyyy-MM-dd HH:mm:ss");
            if (end != null) {
                queryWrapper.le(FileInfo::getUploadTime, end);
            }
        }

        // 排序
        if (StringUtil.isNotEmpty(orderBy)) {
            // TODO: 实现更复杂的排序逻辑
            if (orderBy.startsWith("-")) {
                queryWrapper.orderByDesc(FileInfo::getUploadTime);
            } else {
                queryWrapper.orderByAsc(FileInfo::getUploadTime);
            }
        } else {
            queryWrapper.orderByDesc(FileInfo::getUploadTime);
        }

        // 分页查询
        Page<FileInfo> pageParam = new Page<>(page, size);
        IPage<FileInfo> filePage = page(pageParam, queryWrapper);

        // 转换为VO
        return filePage.convert(this::convertToVO);
    }

    @Override
    public boolean checkFileExists(String filePath) {
        if (StringUtil.isEmpty(filePath)) {
            return false;
        }

        String fullPath = Paths.get(uploadBasePath, filePath).toString();
        File file = new File(fullPath);
        return file.exists() && file.isFile();
    }

    @Override
    public long getFileSize(String filePath) {
        if (StringUtil.isEmpty(filePath)) {
            return 0;
        }

        String fullPath = Paths.get(uploadBasePath, filePath).toString();
        File file = new File(fullPath);
        if (file.exists() && file.isFile()) {
            return file.length();
        }

        return 0;
    }

    @Override
    public List<FileInfo> findDuplicateFilesByMd5(String md5Hash) {
        if (StringUtil.isEmpty(md5Hash)) {
            return new ArrayList<>();
        }

        LambdaQueryWrapper<FileInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FileInfo::getMd5Hash, md5Hash)
                .eq(FileInfo::getDeleted, false)
                .eq(FileInfo::getStatus, 1);

        return list(queryWrapper);
    }

    @Override
    public List<FileInfo> findDuplicateFilesBySha256(String sha256Hash) {
        if (StringUtil.isEmpty(sha256Hash)) {
            return new ArrayList<>();
        }

        LambdaQueryWrapper<FileInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FileInfo::getSha256Hash, sha256Hash)
                .eq(FileInfo::getDeleted, false)
                .eq(FileInfo::getStatus, 1);

        return list(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDownloadCount(Long id) {
        FileInfo fileInfo = getById(id);
        if (fileInfo == null) {
            return false;
        }

        fileInfo.setDownloadCount(fileInfo.getDownloadCount() + 1);
        fileInfo.setLastDownloadTime(LocalDateTime.now());

        return updateById(fileInfo);
    }

    @Override
    public List<FileInfo> getFilesByBizTypeAndId(String bizType, Long bizId) {
        LambdaQueryWrapper<FileInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FileInfo::getBizType, bizType)
                .eq(FileInfo::getBizId, bizId)
                .eq(FileInfo::getDeleted, false)
                .eq(FileInfo::getStatus, 1)
                .orderByDesc(FileInfo::getUploadTime);

        return list(queryWrapper);
    }

    @Override
    public List<FileInfo> getFilesByUploadUserId(Long uploadUserId) {
        LambdaQueryWrapper<FileInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FileInfo::getUploadUserId, uploadUserId)
                .eq(FileInfo::getDeleted, false)
                .eq(FileInfo::getStatus, 1)
                .orderByDesc(FileInfo::getUploadTime);

        return list(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean restoreFile(Long id) {
        FileInfo fileInfo = getById(id);
        if (fileInfo == null) {
            throw new FileUploadException("文件不存在");
        }

        if (!Boolean.TRUE.equals(fileInfo.getDeleted())) {
            return true; // 未删除，不需要恢复
        }

        // 权限检查
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new FileUploadException("无权限恢复文件");
        }

        boolean isAdmin = SecurityUtil.isAdmin();
        boolean isOwner = currentUser.getId().equals(fileInfo.getUploadUserId());
        if (!isAdmin && !isOwner) {
            throw new FileUploadException("无权限恢复此文件");
        }

        fileInfo.setDeleted(false);
        fileInfo.setStatus(1); // 恢复为正常状态
        fileInfo.setUpdateTime(LocalDateTime.now());

        boolean updated = updateById(fileInfo);
        if (updated) {
            log.info("文件恢复成功: ID={}, 文件名={}", id, fileInfo.getOriginalName());
        }

        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchRestoreFiles(List<Long> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) {
            throw new FileUploadException("文件ID列表不能为空");
        }

        boolean allSuccess = true;
        for (Long fileId : fileIds) {
            try {
                boolean restored = restoreFile(fileId);
                if (!restored) {
                    allSuccess = false;
                    log.warn("批量恢复文件中单个文件恢复失败: ID={}", fileId);
                }
            } catch (Exception e) {
                allSuccess = false;
                log.error("批量恢复文件异常: ID={}", fileId, e);
            }
        }

        return allSuccess;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean physicalDeleteFile(Long id) {
        FileInfo fileInfo = getById(id);
        if (fileInfo == null) {
            throw new FileUploadException("文件不存在");
        }

        // 权限检查：只有管理员可以物理删除
        User currentUser = getCurrentUser();
        if (currentUser == null || !SecurityUtil.isAdmin()) {
            throw new FileUploadException("无权限物理删除文件");
        }

        try {
            // 删除物理文件
            String filePath = Paths.get(uploadBasePath, fileInfo.getFilePath()).toString();
            File file = new File(filePath);
            if (file.exists()) {
                boolean fileDeleted = file.delete();
                if (!fileDeleted) {
                    log.warn("物理文件删除失败: {}", filePath);
                }
            }

            // 删除数据库记录
            boolean dbDeleted = removeById(id);
            if (dbDeleted) {
                log.info("文件物理删除成功: ID={}, 文件名={}, 文件路径={}",
                        id, fileInfo.getOriginalName(), fileInfo.getFilePath());
            }

            return dbDeleted;
        } catch (Exception e) {
            log.error("物理删除文件失败: ID={}", id, e);
            throw new FileUploadException("物理删除文件失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchPhysicalDeleteFiles(List<Long> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) {
            throw new FileUploadException("文件ID列表不能为空");
        }

        boolean allSuccess = true;
        for (Long fileId : fileIds) {
            try {
                boolean deleted = physicalDeleteFile(fileId);
                if (!deleted) {
                    allSuccess = false;
                    log.warn("批量物理删除文件中单个文件删除失败: ID={}", fileId);
                }
            } catch (Exception e) {
                allSuccess = false;
                log.error("批量物理删除文件异常: ID={}", fileId, e);
            }
        }

        return allSuccess;
    }

    @Override
    public String generateFileUrl(Long id) {
        FileInfo fileInfo = getById(id);
        if (fileInfo == null) {
            throw new FileUploadException("文件不存在");
        }

        if (Boolean.TRUE.equals(fileInfo.getDeleted()) || fileInfo.getStatus() != 1) {
            throw new FileUploadException("文件不可用");
        }

        // 简单的URL生成，实际项目中可能需要更复杂的逻辑
        return "/api/files/download/" + id;
    }

    @Override
    public String generateFileUrlWithExpire(Long id, Long expireSeconds) {
        // TODO: 实现带过期时间的URL生成
        // 实际项目中可能需要使用签名URL技术
        return generateFileUrl(id) + "?expire=" + expireSeconds;
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 验证文件大小
     */
    /**
     * 验证文件大小
     */
    private void validateFileSize(long fileSize) {
        if (fileSize <= 0) {
            throw new FileUploadException("文件大小无效");
        }

        // 把配置的字符串 100MB 转成 long
        long maxSize = Long.parseLong(maxFileSize.replace("MB", "").trim()) * 1024 * 1024;

        if (fileSize > maxSize) {
            throw new FileUploadException(ResultCode.FILE_SIZE_EXCEEDED,
                    "文件大小超出限制，最大允许" + (maxSize / 1024 / 1024) + "MB");
        }
    }

    /**
     * 验证文件类型
     */
    private void validateFileType(String fileName) {
        if (StringUtil.isEmpty(fileName)) {
            throw new FileUploadException("文件名不能为空");
        }

        String fileExtension = FileUtil.getFileExtension(fileName);
        if (StringUtil.isEmpty(fileExtension)) {
            throw new FileUploadException("不支持无扩展名的文件");
        }

        // 检查是否在允许的文件类型列表中
        String[] allowedExtensions = allowedTypes.split(",");
        boolean isAllowed = false;
        for (String allowedExt : allowedExtensions) {
            if (allowedExt.trim().equalsIgnoreCase(fileExtension)) {
                isAllowed = true;
                break;
            }
        }

        if (!isAllowed) {
            throw new FileUploadException(ResultCode.FILE_TYPE_NOT_SUPPORTED,
                    "不支持的文件类型: " + fileExtension + "，允许的类型: " + allowedTypes);
        }
    }

    /**
     * 获取当前用户
     */
    private User getCurrentUser() {
        try {
            String username = SecurityUtil.getCurrentUsername();
            if (username == null) {
                return null;
            }
            return userService.getUserByUsername(username);
        } catch (Exception e) {
            log.debug("获取当前用户失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 创建目录（如果不存在）
     */
    private void createDirectoryIfNotExists(String filePath) {
        File file = new File(filePath);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            boolean created = parentDir.mkdirs();
            if (!created) {
                throw new FileUploadException("创建目录失败: " + parentDir.getAbsolutePath());
            }
        }
    }

    /**
     * 计算MD5哈希值
     */
    private String calculateMd5(byte[] bytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(bytes);
            return bytesToHex(digest);
        } catch (Exception e) {
            log.warn("计算MD5哈希值失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 计算SHA-256哈希值
     */
    private String calculateSha256(byte[] bytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(bytes);
            return bytesToHex(digest);
        } catch (Exception e) {
            log.warn("计算SHA-256哈希值失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 字节数组转十六进制字符串
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * 内部文件下载方法
     */
    private void downloadFileInternal(String filePath, String originalName, HttpServletResponse response) {
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            throw new FileUploadException("文件不存在或不是有效文件");
        }

        try (InputStream inputStream = new FileInputStream(file);
             OutputStream outputStream = response.getOutputStream()) {

            // 设置响应头
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"" + encodeFileName(originalName != null ? originalName : file.getName()) + "\"");
            response.setHeader("Content-Length", String.valueOf(file.length()));

            // 传输文件
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();

        } catch (IOException e) {
            log.error("文件下载失败: {}", filePath, e);
            throw new FileUploadException("文件下载失败: " + e.getMessage(), e);
        }
    }

    /**
     * 编码文件名（防止中文乱码）
     */
    private String encodeFileName(String fileName) {
        try {
            return java.net.URLEncoder.encode(fileName, "UTF-8").replace("+", "%20");
        } catch (Exception e) {
            return fileName;
        }
    }

    /**
     * 将FileInfo转换为FileVO
     */
    private FileVO convertToVO(FileInfo fileInfo) {
        if (fileInfo == null) {
            return null;
        }

        FileVO fileVO = new FileVO();
        BeanUtils.copyProperties(fileInfo, fileVO);
        return fileVO;
    }
}
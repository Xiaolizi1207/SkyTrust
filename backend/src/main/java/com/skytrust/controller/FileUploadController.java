package com.skytrust.controller;

import com.skytrust.common.Result;
import com.skytrust.common.ResultCode;
import com.skytrust.common.utils.FileUtil;
import com.skytrust.common.utils.SecurityUtil;
import com.skytrust.dto.FileUploadDTO;
import com.skytrust.entity.FileInfo;
import com.skytrust.exception.FileUploadException;
import com.skytrust.service.FileUploadService;
import com.skytrust.vo.FileVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件上传控制器
 *
 * @author SkyTrust Team
 */
@Slf4j
@Api(tags = "文件上传管理", description = "文件上传、下载、管理等接口")
@Validated
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileUploadService fileUploadService;

    /**
     * 单文件上传
     */
    @ApiOperation(value = "单文件上传")
    @PostMapping("/upload")
    public Result<FileVO> uploadFile(
            @ApiParam(value = "上传的文件", required = true) @RequestParam("file") MultipartFile file,
            @ApiParam(value = "业务类型") @RequestParam(required = false) String bizType,
            @ApiParam(value = "业务ID") @RequestParam(required = false) Long bizId,
            @ApiParam(value = "文件描述") @RequestParam(required = false) String description,
            @ApiParam(value = "是否公开") @RequestParam(required = false, defaultValue = "true") Boolean isPublic) {

        log.info("单文件上传: 文件名={}, 大小={}字节, 业务类型={}, 业务ID={}",
                file.getOriginalFilename(), file.getSize(), bizType, bizId);

        try {
            FileUploadDTO fileUploadDTO = new FileUploadDTO();
            fileUploadDTO.setFile(file);
            fileUploadDTO.setBizType(bizType);
            fileUploadDTO.setBizId(bizId);
            fileUploadDTO.setDescription(description);
            fileUploadDTO.setIsPublic(isPublic);

            FileVO fileVO = fileUploadService.uploadFile(fileUploadDTO);
            return Result.success(fileVO, "文件上传成功");
        } catch (FileUploadException e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            return Result.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("文件上传异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.FILE_UPLOAD_ERROR, "文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 多文件上传
     */
    @ApiOperation(value = "多文件上传")
    @PostMapping("/upload-multiple")
    public Result<List<FileVO>> uploadMultipleFiles(
            @ApiParam(value = "上传的文件列表", required = true) @RequestParam("files") MultipartFile[] files,
            @ApiParam(value = "业务类型") @RequestParam(required = false) String bizType,
            @ApiParam(value = "业务ID") @RequestParam(required = false) Long bizId,
            @ApiParam(value = "文件描述") @RequestParam(required = false) String description,
            @ApiParam(value = "是否公开") @RequestParam(required = false, defaultValue = "true") Boolean isPublic) {

        log.info("多文件上传: 文件数量={}, 业务类型={}, 业务ID={}", files.length, bizType, bizId);

        try {
            List<FileVO> fileVOs = fileUploadService.uploadMultipleFiles(files, bizType, bizId, description, isPublic);
            return Result.success(fileVOs, "多文件上传成功");
        } catch (FileUploadException e) {
            log.error("多文件上传失败: {}", e.getMessage(), e);
            return Result.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("多文件上传异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.FILE_UPLOAD_ERROR, "多文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 根据文件ID下载文件
     */
    @ApiOperation(value = "下载文件")
    @GetMapping("/download/{id}")
    public void downloadFile(
            @ApiParam(value = "文件ID", required = true) @PathVariable Long id,
            HttpServletResponse response) {

        log.info("下载文件: 文件ID={}", id);

        try {
            fileUploadService.downloadFile(id, response);
        } catch (Exception e) {
            log.error("下载文件失败: 文件ID={}", id, e);
            throw new FileUploadException("下载文件失败: " + e.getMessage());
        }
    }

    /**
     * 根据文件路径下载文件
     */
    @ApiOperation(value = "根据路径下载文件")
    @GetMapping("/download-by-path")
    public void downloadFileByPath(
            @ApiParam(value = "文件路径", required = true) @RequestParam String filePath,
            @ApiParam(value = "原始文件名") @RequestParam(required = false) String originalName,
            HttpServletResponse response) {

        log.info("根据路径下载文件: 文件路径={}", filePath);

        try {
            fileUploadService.downloadFileByPath(filePath, originalName, response);
        } catch (Exception e) {
            log.error("根据路径下载文件失败: 文件路径={}", filePath, e);
            throw new FileUploadException("下载文件失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件信息
     */
    @ApiOperation(value = "获取文件信息")
    @GetMapping("/{id}")
    public Result<FileVO> getFileInfo(
            @ApiParam(value = "文件ID", required = true) @PathVariable Long id) {

        log.info("获取文件信息: 文件ID={}", id);

        try {
            FileVO fileVO = fileUploadService.getFileInfo(id);
            return Result.success(fileVO);
        } catch (FileUploadException e) {
            log.error("获取文件信息失败: {}", e.getMessage(), e);
            return Result.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("获取文件信息异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.FILE_UPLOAD_ERROR, "获取文件信息失败: " + e.getMessage());
        }
    }

    /**
     * 删除文件（逻辑删除）
     */
    @ApiOperation(value = "删除文件")
    @DeleteMapping("/{id}")
    public Result<Void> deleteFile(
            @ApiParam(value = "文件ID", required = true) @PathVariable Long id) {

        log.info("删除文件: 文件ID={}", id);

        try {
            boolean deleted = fileUploadService.deleteFile(id);
            if (deleted) {
                return Result.success("文件删除成功");
            } else {
                return Result.error(ResultCode.FILE_UPLOAD_ERROR, "文件删除失败");
            }
        } catch (FileUploadException e) {
            log.error("删除文件失败: {}", e.getMessage(), e);
            return Result.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("删除文件异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.FILE_UPLOAD_ERROR, "文件删除失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除文件
     */
    @ApiOperation(value = "批量删除文件")
    @DeleteMapping("/batch")
    public Result<Void> batchDeleteFiles(
            @ApiParam(value = "文件ID列表", required = true) @RequestBody List<Long> fileIds) {

        log.info("批量删除文件: 文件数量={}", fileIds.size());

        try {
            boolean deleted = fileUploadService.batchDeleteFiles(fileIds);
            if (deleted) {
                return Result.success("批量文件删除成功");
            } else {
                return Result.error(ResultCode.FILE_UPLOAD_ERROR, "批量文件删除失败");
            }
        } catch (FileUploadException e) {
            log.error("批量删除文件失败: {}", e.getMessage(), e);
            return Result.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("批量删除文件异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.FILE_UPLOAD_ERROR, "批量文件删除失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件列表（分页）
     */
    @ApiOperation(value = "获取文件列表（分页）")
    @GetMapping
    public Result<com.baomidou.mybatisplus.core.metadata.IPage<FileVO>> getFileList(
            @ApiParam(value = "页码") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam(value = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @ApiParam(value = "原始文件名（模糊查询）") @RequestParam(required = false) String originalName,
            @ApiParam(value = "业务类型") @RequestParam(required = false) String bizType,
            @ApiParam(value = "业务ID") @RequestParam(required = false) Long bizId,
            @ApiParam(value = "上传用户ID") @RequestParam(required = false) Long uploadUserId,
            @ApiParam(value = "是否公开") @RequestParam(required = false) Boolean isPublic,
            @ApiParam(value = "状态") @RequestParam(required = false) Integer status,
            @ApiParam(value = "开始时间") @RequestParam(required = false) String startTime,
            @ApiParam(value = "结束时间") @RequestParam(required = false) String endTime,
            @ApiParam(value = "排序字段") @RequestParam(required = false) String orderBy) {

        log.info("获取文件列表: page={}, size={}, bizType={}, bizId={}", page, size, bizType, bizId);

        try {
            com.baomidou.mybatisplus.core.metadata.IPage<FileVO> filePage = fileUploadService.getFileList(
                    page, size, originalName, bizType, bizId, uploadUserId, isPublic, status,
                    startTime, endTime, orderBy);
            return Result.success(filePage);
        } catch (Exception e) {
            log.error("获取文件列表异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.FILE_UPLOAD_ERROR, "获取文件列表失败: " + e.getMessage());
        }
    }

    /**
     * 检查文件是否存在
     */
    @ApiOperation(value = "检查文件是否存在")
    @GetMapping("/check-exists")
    public Result<Boolean> checkFileExists(
            @ApiParam(value = "文件路径", required = true) @RequestParam String filePath) {

        log.info("检查文件是否存在: 文件路径={}", filePath);

        try {
            boolean exists = fileUploadService.checkFileExists(filePath);
            return Result.success(exists);
        } catch (Exception e) {
            log.error("检查文件是否存在异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.FILE_UPLOAD_ERROR, "检查文件失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件大小
     */
    @ApiOperation(value = "获取文件大小")
    @GetMapping("/size")
    public Result<Long> getFileSize(
            @ApiParam(value = "文件路径", required = true) @RequestParam String filePath) {

        log.info("获取文件大小: 文件路径={}", filePath);

        try {
            long fileSize = fileUploadService.getFileSize(filePath);
            return Result.success(fileSize);
        } catch (Exception e) {
            log.error("获取文件大小异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.FILE_UPLOAD_ERROR, "获取文件大小失败: " + e.getMessage());
        }
    }
}
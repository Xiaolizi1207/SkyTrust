package com.skytrust.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.skytrust.common.Result;
import com.skytrust.common.ResultCode;
import com.skytrust.dto.DictTypeDTO;
import com.skytrust.entity.DictType;
import com.skytrust.service.DictTypeService;
import com.skytrust.vo.DictTypeVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 字典类型控制器
 *
 * @author SkyTrust Team
 */
@Slf4j
@Api(tags = "字典类型管理", description = "字典类型的增删改查等接口")
@Validated
@RestController
@RequestMapping("/api/dict-types")
@RequiredArgsConstructor
public class DictTypeController {

    private final DictTypeService dictTypeService;

    /**
     * 创建字典类型
     */
    @ApiOperation(value = "创建字典类型")
    @PostMapping
    public Result<DictTypeVO> createDictType(@Valid @RequestBody DictTypeDTO dictTypeDTO) {
        log.info("创建字典类型请求: typeCode={}, typeName={}", dictTypeDTO.getTypeCode(), dictTypeDTO.getTypeName());

        try {
            // DTO转实体
            DictType dictType = new DictType();
            BeanUtils.copyProperties(dictTypeDTO, dictType);

            // 调用服务创建
            boolean success = dictTypeService.createDictType(dictType);
            if (!success) {
                return Result.error(ResultCode.SYSTEM_ERROR, "创建字典类型失败");
            }

            // 实体转VO
            DictTypeVO dictTypeVO = convertToVO(dictType);
            return Result.success(dictTypeVO, "创建字典类型成功");
        } catch (IllegalArgumentException e) {
            log.error("创建字典类型参数错误: {}", e.getMessage(), e);
            return Result.error(ResultCode.PARAM_ERROR, e.getMessage());
        } catch (Exception e) {
            log.error("创建字典类型异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "创建字典类型异常: " + e.getMessage());
        }
    }

    /**
     * 更新字典类型
     */
    @ApiOperation(value = "更新字典类型")
    @PutMapping("/{id}")
    public Result<DictTypeVO> updateDictType(
            @ApiParam(value = "字典类型ID", required = true) @PathVariable Long id,
            @Valid @RequestBody DictTypeDTO dictTypeDTO) {
        log.info("更新字典类型请求: id={}, typeCode={}", id, dictTypeDTO.getTypeCode());

        try {
            // 先查询现有字典类型
            DictType dictType = dictTypeService.getDictTypeById(id);
            if (dictType == null) {
                return Result.error(ResultCode.PARAM_ERROR, "字典类型不存在");
            }

            // 更新字段
            BeanUtils.copyProperties(dictTypeDTO, dictType);
            dictType.setId(id);

            // 调用服务更新
            boolean success = dictTypeService.updateDictType(dictType);
            if (!success) {
                return Result.error(ResultCode.SYSTEM_ERROR, "更新字典类型失败");
            }

            // 重新查询获取最新数据
            DictType updatedDictType = dictTypeService.getDictTypeById(id);
            DictTypeVO dictTypeVO = convertToVO(updatedDictType);
            return Result.success(dictTypeVO, "更新字典类型成功");
        } catch (IllegalArgumentException e) {
            log.error("更新字典类型参数错误: {}", e.getMessage(), e);
            return Result.error(ResultCode.PARAM_ERROR, e.getMessage());
        } catch (Exception e) {
            log.error("更新字典类型异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "更新字典类型异常: " + e.getMessage());
        }
    }

    /**
     * 删除字典类型（逻辑删除）
     */
    @ApiOperation(value = "删除字典类型")
    @DeleteMapping("/{id}")
    public Result<Void> deleteDictType(@ApiParam(value = "字典类型ID", required = true) @PathVariable Long id) {
        log.info("删除字典类型请求: id={}", id);

        try {
            boolean success = dictTypeService.deleteDictType(id);
            if (!success) {
                return Result.error(ResultCode.SYSTEM_ERROR, "删除字典类型失败");
            }
            return Result.success("删除字典类型成功");
        } catch (IllegalArgumentException e) {
            log.error("删除字典类型参数错误: {}", e.getMessage(), e);
            return Result.error(ResultCode.PARAM_ERROR, e.getMessage());
        } catch (Exception e) {
            log.error("删除字典类型异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "删除字典类型异常: " + e.getMessage());
        }
    }

    /**
     * 批量删除字典类型
     */
    @ApiOperation(value = "批量删除字典类型")
    @DeleteMapping("/batch")
    public Result<Void> batchDeleteDictTypes(@ApiParam(value = "字典类型ID列表", required = true) @RequestBody List<Long> ids) {
        log.info("批量删除字典类型请求: ids={}", ids);

        try {
            boolean success = dictTypeService.batchDeleteDictTypes(ids);
            if (!success) {
                return Result.error(ResultCode.SYSTEM_ERROR, "批量删除字典类型失败");
            }
            return Result.success("批量删除字典类型成功");
        } catch (Exception e) {
            log.error("批量删除字典类型异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "批量删除字典类型异常: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取字典类型详情
     */
    @ApiOperation(value = "获取字典类型详情")
    @GetMapping("/{id}")
    public Result<DictTypeVO> getDictTypeById(@ApiParam(value = "字典类型ID", required = true) @PathVariable Long id) {
        log.info("获取字典类型详情请求: id={}", id);

        try {
            DictType dictType = dictTypeService.getDictTypeById(id);
            if (dictType == null) {
                return Result.error(ResultCode.PARAM_ERROR, "字典类型不存在");
            }

            DictTypeVO dictTypeVO = convertToVO(dictType);
            return Result.success(dictTypeVO);
        } catch (Exception e) {
            log.error("获取字典类型详情异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "获取字典类型详情异常: " + e.getMessage());
        }
    }

    /**
     * 根据类型编码获取字典类型详情
     */
    @ApiOperation(value = "根据类型编码获取字典类型详情")
    @GetMapping("/code/{typeCode}")
    public Result<DictTypeVO> getDictTypeByCode(@ApiParam(value = "字典类型编码", required = true) @PathVariable String typeCode) {
        log.info("根据类型编码获取字典类型详情请求: typeCode={}", typeCode);

        try {
            DictType dictType = dictTypeService.getDictTypeByCode(typeCode);
            if (dictType == null) {
                return Result.error(ResultCode.PARAM_ERROR, "字典类型不存在");
            }

            DictTypeVO dictTypeVO = convertToVO(dictType);
            return Result.success(dictTypeVO);
        } catch (Exception e) {
            log.error("根据类型编码获取字典类型详情异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "根据类型编码获取字典类型详情异常: " + e.getMessage());
        }
    }

    /**
     * 获取字典类型列表（分页）
     */
    @ApiOperation(value = "获取字典类型列表（分页）")
    @GetMapping
    public Result<IPage<DictTypeVO>> getDictTypeList(
            @ApiParam(value = "页码", defaultValue = "1") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam(value = "每页大小", defaultValue = "10") @RequestParam(defaultValue = "10") Integer size,
            @ApiParam(value = "类型编码（模糊查询）") @RequestParam(required = false) String typeCode,
            @ApiParam(value = "类型名称（模糊查询）") @RequestParam(required = false) String typeName,
            @ApiParam(value = "状态（0-禁用，1-启用）") @RequestParam(required = false) Integer status,
            @ApiParam(value = "排序字段") @RequestParam(required = false) String orderBy) {
        log.info("获取字典类型列表请求: page={}, size={}, typeCode={}, typeName={}, status={}",
                page, size, typeCode, typeName, status);

        try {
            IPage<DictType> dictTypePage = dictTypeService.getDictTypeList(page, size, typeCode, typeName, status, orderBy);
            IPage<DictTypeVO> dictTypeVOPage = dictTypePage.convert(this::convertToVO);
            return Result.success(dictTypeVOPage);
        } catch (Exception e) {
            log.error("获取字典类型列表异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "获取字典类型列表异常: " + e.getMessage());
        }
    }

    /**
     * 获取所有启用的字典类型列表
     */
    @ApiOperation(value = "获取所有启用的字典类型列表")
    @GetMapping("/enabled")
    public Result<List<DictTypeVO>> getAllEnabledDictTypes() {
        log.info("获取所有启用的字典类型列表请求");

        try {
            List<DictType> dictTypes = dictTypeService.getAllEnabledDictTypes();
            List<DictTypeVO> dictTypeVOs = dictTypes.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            return Result.success(dictTypeVOs);
        } catch (Exception e) {
            log.error("获取所有启用的字典类型列表异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "获取所有启用的字典类型列表异常: " + e.getMessage());
        }
    }

    /**
     * 更新字典类型状态
     */
    @ApiOperation(value = "更新字典类型状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateDictTypeStatus(
            @ApiParam(value = "字典类型ID", required = true) @PathVariable Long id,
            @ApiParam(value = "状态（0-禁用，1-启用）", required = true) @RequestParam Integer status) {
        log.info("更新字典类型状态请求: id={}, status={}", id, status);

        try {
            boolean success = dictTypeService.updateDictTypeStatus(id, status);
            if (!success) {
                return Result.error(ResultCode.SYSTEM_ERROR, "更新字典类型状态失败");
            }
            return Result.success("更新字典类型状态成功");
        } catch (IllegalArgumentException e) {
            log.error("更新字典类型状态参数错误: {}", e.getMessage(), e);
            return Result.error(ResultCode.PARAM_ERROR, e.getMessage());
        } catch (Exception e) {
            log.error("更新字典类型状态异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "更新字典类型状态异常: " + e.getMessage());
        }
    }

    /**
     * 批量更新字典类型状态
     */
    @ApiOperation(value = "批量更新字典类型状态")
    @PutMapping("/batch/status")
    public Result<Void> batchUpdateDictTypeStatus(
            @ApiParam(value = "字典类型ID列表", required = true) @RequestBody List<Long> ids,
            @ApiParam(value = "状态（0-禁用，1-启用）", required = true) @RequestParam Integer status) {
        log.info("批量更新字典类型状态请求: ids={}, status={}", ids, status);

        try {
            boolean success = dictTypeService.batchUpdateDictTypeStatus(ids, status);
            if (!success) {
                return Result.error(ResultCode.SYSTEM_ERROR, "批量更新字典类型状态失败");
            }
            return Result.success("批量更新字典类型状态成功");
        } catch (Exception e) {
            log.error("批量更新字典类型状态异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "批量更新字典类型状态异常: " + e.getMessage());
        }
    }

    /**
     * 检查类型编码是否已存在
     */
    @ApiOperation(value = "检查类型编码是否已存在")
    @GetMapping("/check-type-code")
    public Result<Boolean> checkTypeCodeExists(@ApiParam(value = "类型编码", required = true) @RequestParam String typeCode) {
        log.info("检查类型编码是否已存在请求: typeCode={}", typeCode);

        try {
            boolean exists = dictTypeService.isTypeCodeExists(typeCode);
            return Result.success(exists);
        } catch (Exception e) {
            log.error("检查类型编码是否已存在异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "检查类型编码是否已存在异常: " + e.getMessage());
        }
    }

    /**
     * 将DictType实体转换为DictTypeVO
     */
    private DictTypeVO convertToVO(DictType dictType) {
        if (dictType == null) {
            return null;
        }
        DictTypeVO dictTypeVO = new DictTypeVO();
        BeanUtils.copyProperties(dictType, dictTypeVO);
        return dictTypeVO;
    }
}
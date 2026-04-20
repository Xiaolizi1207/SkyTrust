package com.skytrust.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.skytrust.common.Result;
import com.skytrust.common.ResultCode;
import com.skytrust.dto.DictDataDTO;
import com.skytrust.entity.DictData;
import com.skytrust.service.DictDataService;
import com.skytrust.vo.DictDataVO;
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
 * 字典数据控制器
 *
 * @author SkyTrust Team
 */
@Slf4j
@Api(tags = "字典数据管理", description = "字典数据的增删改查等接口")
@Validated
@RestController
@RequestMapping("/api/dict-datas")
@RequiredArgsConstructor
public class DictDataController {

    private final DictDataService dictDataService;

    /**
     * 创建字典数据
     */
    @ApiOperation(value = "创建字典数据")
    @PostMapping
    public Result<DictDataVO> createDictData(@Valid @RequestBody DictDataDTO dictDataDTO) {
        log.info("创建字典数据请求: dictLabel={}, dictValue={}, dictType={}",
                dictDataDTO.getDictLabel(), dictDataDTO.getDictValue(), dictDataDTO.getDictType());

        try {
            // DTO转实体
            DictData dictData = new DictData();
            BeanUtils.copyProperties(dictDataDTO, dictData);

            // 调用服务创建
            boolean success = dictDataService.createDictData(dictData);
            if (!success) {
                return Result.error(ResultCode.SYSTEM_ERROR, "创建字典数据失败");
            }

            // 实体转VO
            DictDataVO dictDataVO = convertToVO(dictData);
            return Result.success(dictDataVO, "创建字典数据成功");
        } catch (IllegalArgumentException e) {
            log.error("创建字典数据参数错误: {}", e.getMessage(), e);
            return Result.error(ResultCode.PARAM_ERROR, e.getMessage());
        } catch (Exception e) {
            log.error("创建字典数据异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "创建字典数据异常: " + e.getMessage());
        }
    }

    /**
     * 更新字典数据
     */
    @ApiOperation(value = "更新字典数据")
    @PutMapping("/{id}")
    public Result<DictDataVO> updateDictData(
            @ApiParam(value = "字典数据ID", required = true) @PathVariable Long id,
            @Valid @RequestBody DictDataDTO dictDataDTO) {
        log.info("更新字典数据请求: id={}, dictLabel={}", id, dictDataDTO.getDictLabel());

        try {
            // 先查询现有字典数据
            DictData dictData = dictDataService.getDictDataById(id);
            if (dictData == null) {
                return Result.error(ResultCode.PARAM_ERROR, "字典数据不存在");
            }

            // 更新字段
            BeanUtils.copyProperties(dictDataDTO, dictData);
            dictData.setId(id);

            // 调用服务更新
            boolean success = dictDataService.updateDictData(dictData);
            if (!success) {
                return Result.error(ResultCode.SYSTEM_ERROR, "更新字典数据失败");
            }

            // 重新查询获取最新数据
            DictData updatedDictData = dictDataService.getDictDataById(id);
            DictDataVO dictDataVO = convertToVO(updatedDictData);
            return Result.success(dictDataVO, "更新字典数据成功");
        } catch (IllegalArgumentException e) {
            log.error("更新字典数据参数错误: {}", e.getMessage(), e);
            return Result.error(ResultCode.PARAM_ERROR, e.getMessage());
        } catch (Exception e) {
            log.error("更新字典数据异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "更新字典数据异常: " + e.getMessage());
        }
    }

    /**
     * 删除字典数据（逻辑删除）
     */
    @ApiOperation(value = "删除字典数据")
    @DeleteMapping("/{id}")
    public Result<Void> deleteDictData(@ApiParam(value = "字典数据ID", required = true) @PathVariable Long id) {
        log.info("删除字典数据请求: id={}", id);

        try {
            boolean success = dictDataService.deleteDictData(id);
            if (!success) {
                return Result.error(ResultCode.SYSTEM_ERROR, "删除字典数据失败");
            }
            return Result.success("删除字典数据成功");
        } catch (IllegalArgumentException e) {
            log.error("删除字典数据参数错误: {}", e.getMessage(), e);
            return Result.error(ResultCode.PARAM_ERROR, e.getMessage());
        } catch (Exception e) {
            log.error("删除字典数据异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "删除字典数据异常: " + e.getMessage());
        }
    }

    /**
     * 批量删除字典数据
     */
    @ApiOperation(value = "批量删除字典数据")
    @DeleteMapping("/batch")
    public Result<Void> batchDeleteDictDatas(@ApiParam(value = "字典数据ID列表", required = true) @RequestBody List<Long> ids) {
        log.info("批量删除字典数据请求: ids={}", ids);

        try {
            boolean success = dictDataService.batchDeleteDictDatas(ids);
            if (!success) {
                return Result.error(ResultCode.SYSTEM_ERROR, "批量删除字典数据失败");
            }
            return Result.success("批量删除字典数据成功");
        } catch (Exception e) {
            log.error("批量删除字典数据异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "批量删除字典数据异常: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取字典数据详情
     */
    @ApiOperation(value = "获取字典数据详情")
    @GetMapping("/{id}")
    public Result<DictDataVO> getDictDataById(@ApiParam(value = "字典数据ID", required = true) @PathVariable Long id) {
        log.info("获取字典数据详情请求: id={}", id);

        try {
            DictData dictData = dictDataService.getDictDataById(id);
            if (dictData == null) {
                return Result.error(ResultCode.PARAM_ERROR, "字典数据不存在");
            }

            DictDataVO dictDataVO = convertToVO(dictData);
            return Result.success(dictDataVO);
        } catch (Exception e) {
            log.error("获取字典数据详情异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "获取字典数据详情异常: " + e.getMessage());
        }
    }

    /**
     * 根据字典类型编码获取字典数据列表
     */
    @ApiOperation(value = "根据字典类型编码获取字典数据列表")
    @GetMapping("/type/{dictType}")
    public Result<List<DictDataVO>> getDictDataByType(@ApiParam(value = "字典类型编码", required = true) @PathVariable String dictType) {
        log.info("根据字典类型编码获取字典数据列表请求: dictType={}", dictType);

        try {
            List<DictData> dictDatas = dictDataService.getDictDataByType(dictType);
            List<DictDataVO> dictDataVOs = dictDatas.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            return Result.success(dictDataVOs);
        } catch (Exception e) {
            log.error("根据字典类型编码获取字典数据列表异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "根据字典类型编码获取字典数据列表异常: " + e.getMessage());
        }
    }

    /**
     * 根据字典类型编码获取启用的字典数据列表
     */
    @ApiOperation(value = "根据字典类型编码获取启用的字典数据列表")
    @GetMapping("/type/{dictType}/enabled")
    public Result<List<DictDataVO>> getEnabledDictDataByType(@ApiParam(value = "字典类型编码", required = true) @PathVariable String dictType) {
        log.info("根据字典类型编码获取启用的字典数据列表请求: dictType={}", dictType);

        try {
            List<DictData> dictDatas = dictDataService.getEnabledDictDataByType(dictType);
            List<DictDataVO> dictDataVOs = dictDatas.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            return Result.success(dictDataVOs);
        } catch (Exception e) {
            log.error("根据字典类型编码获取启用的字典数据列表异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "根据字典类型编码获取启用的字典数据列表异常: " + e.getMessage());
        }
    }

    /**
     * 获取字典数据列表（分页）
     */
    @ApiOperation(value = "获取字典数据列表（分页）")
    @GetMapping
    public Result<IPage<DictDataVO>> getDictDataList(
            @ApiParam(value = "页码", defaultValue = "1") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam(value = "每页大小", defaultValue = "10") @RequestParam(defaultValue = "10") Integer size,
            @ApiParam(value = "字典标签（模糊查询）") @RequestParam(required = false) String dictLabel,
            @ApiParam(value = "字典值（模糊查询）") @RequestParam(required = false) String dictValue,
            @ApiParam(value = "字典类型编码") @RequestParam(required = false) String dictType,
            @ApiParam(value = "状态（0-禁用，1-启用）") @RequestParam(required = false) Integer status,
            @ApiParam(value = "排序字段") @RequestParam(required = false) String orderBy) {
        log.info("获取字典数据列表请求: page={}, size={}, dictLabel={}, dictValue={}, dictType={}, status={}",
                page, size, dictLabel, dictValue, dictType, status);

        try {
            IPage<DictData> dictDataPage = dictDataService.getDictDataList(
                    page, size, dictLabel, dictValue, dictType, status, orderBy);
            IPage<DictDataVO> dictDataVOPage = dictDataPage.convert(this::convertToVO);
            return Result.success(dictDataVOPage);
        } catch (Exception e) {
            log.error("获取字典数据列表异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "获取字典数据列表异常: " + e.getMessage());
        }
    }

    /**
     * 更新字典数据状态
     */
    @ApiOperation(value = "更新字典数据状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateDictDataStatus(
            @ApiParam(value = "字典数据ID", required = true) @PathVariable Long id,
            @ApiParam(value = "状态（0-禁用，1-启用）", required = true) @RequestParam Integer status) {
        log.info("更新字典数据状态请求: id={}, status={}", id, status);

        try {
            boolean success = dictDataService.updateDictDataStatus(id, status);
            if (!success) {
                return Result.error(ResultCode.SYSTEM_ERROR, "更新字典数据状态失败");
            }
            return Result.success("更新字典数据状态成功");
        } catch (IllegalArgumentException e) {
            log.error("更新字典数据状态参数错误: {}", e.getMessage(), e);
            return Result.error(ResultCode.PARAM_ERROR, e.getMessage());
        } catch (Exception e) {
            log.error("更新字典数据状态异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "更新字典数据状态异常: " + e.getMessage());
        }
    }

    /**
     * 批量更新字典数据状态
     */
    @ApiOperation(value = "批量更新字典数据状态")
    @PutMapping("/batch/status")
    public Result<Void> batchUpdateDictDataStatus(
            @ApiParam(value = "字典数据ID列表", required = true) @RequestBody List<Long> ids,
            @ApiParam(value = "状态（0-禁用，1-启用）", required = true) @RequestParam Integer status) {
        log.info("批量更新字典数据状态请求: ids={}, status={}", ids, status);

        try {
            boolean success = dictDataService.batchUpdateDictDataStatus(ids, status);
            if (!success) {
                return Result.error(ResultCode.SYSTEM_ERROR, "批量更新字典数据状态失败");
            }
            return Result.success("批量更新字典数据状态成功");
        } catch (Exception e) {
            log.error("批量更新字典数据状态异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "批量更新字典数据状态异常: " + e.getMessage());
        }
    }

    /**
     * 设置字典数据为默认
     */
    @ApiOperation(value = "设置字典数据为默认")
    @PutMapping("/{id}/default")
    public Result<Void> setDictDataAsDefault(@ApiParam(value = "字典数据ID", required = true) @PathVariable Long id) {
        log.info("设置字典数据为默认请求: id={}", id);

        try {
            boolean success = dictDataService.setDictDataAsDefault(id);
            if (!success) {
                return Result.error(ResultCode.SYSTEM_ERROR, "设置字典数据为默认失败");
            }
            return Result.success("设置字典数据为默认成功");
        } catch (IllegalArgumentException e) {
            log.error("设置字典数据为默认参数错误: {}", e.getMessage(), e);
            return Result.error(ResultCode.PARAM_ERROR, e.getMessage());
        } catch (Exception e) {
            log.error("设置字典数据为默认异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "设置字典数据为默认异常: " + e.getMessage());
        }
    }

    /**
     * 根据字典类型编码和字典值获取字典数据
     */
    @ApiOperation(value = "根据字典类型编码和字典值获取字典数据")
    @GetMapping("/type-value")
    public Result<DictDataVO> getDictDataByTypeAndValue(
            @ApiParam(value = "字典类型编码", required = true) @RequestParam String dictType,
            @ApiParam(value = "字典值", required = true) @RequestParam String dictValue) {
        log.info("根据字典类型编码和字典值获取字典数据请求: dictType={}, dictValue={}", dictType, dictValue);

        try {
            DictData dictData = dictDataService.getDictDataByTypeAndValue(dictType, dictValue);
            if (dictData == null) {
                return Result.error(ResultCode.PARAM_ERROR, "字典数据不存在");
            }

            DictDataVO dictDataVO = convertToVO(dictData);
            return Result.success(dictDataVO);
        } catch (Exception e) {
            log.error("根据字典类型编码和字典值获取字典数据异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "根据字典类型编码和字典值获取字典数据异常: " + e.getMessage());
        }
    }

    /**
     * 根据字典类型编码和字典标签获取字典数据
     */
    @ApiOperation(value = "根据字典类型编码和字典标签获取字典数据")
    @GetMapping("/type-label")
    public Result<DictDataVO> getDictDataByTypeAndLabel(
            @ApiParam(value = "字典类型编码", required = true) @RequestParam String dictType,
            @ApiParam(value = "字典标签", required = true) @RequestParam String dictLabel) {
        log.info("根据字典类型编码和字典标签获取字典数据请求: dictType={}, dictLabel={}", dictType, dictLabel);

        try {
            DictData dictData = dictDataService.getDictDataByTypeAndLabel(dictType, dictLabel);
            if (dictData == null) {
                return Result.error(ResultCode.PARAM_ERROR, "字典数据不存在");
            }

            DictDataVO dictDataVO = convertToVO(dictData);
            return Result.success(dictDataVO);
        } catch (Exception e) {
            log.error("根据字典类型编码和字典标签获取字典数据异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "根据字典类型编码和字典标签获取字典数据异常: " + e.getMessage());
        }
    }

    /**
     * 检查字典标签和值在同一个字典类型下是否已存在
     */
    @ApiOperation(value = "检查字典标签和值是否已存在")
    @GetMapping("/check-exists")
    public Result<Boolean> checkDictLabelExists(
            @ApiParam(value = "字典标签", required = true) @RequestParam String dictLabel,
            @ApiParam(value = "字典值", required = true) @RequestParam String dictValue,
            @ApiParam(value = "字典类型编码", required = true) @RequestParam String dictType) {
        log.info("检查字典标签和值是否已存在请求: dictLabel={}, dictValue={}, dictType={}",
                dictLabel, dictValue, dictType);

        try {
            boolean exists = dictDataService.isDictLabelExists(dictLabel, dictValue, dictType);
            return Result.success(exists);
        } catch (Exception e) {
            log.error("检查字典标签和值是否已存在异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "检查字典标签和值是否已存在异常: " + e.getMessage());
        }
    }

    /**
     * 将DictData实体转换为DictDataVO
     */
    private DictDataVO convertToVO(DictData dictData) {
        if (dictData == null) {
            return null;
        }
        DictDataVO dictDataVO = new DictDataVO();
        BeanUtils.copyProperties(dictData, dictDataVO);
        return dictDataVO;
    }
}
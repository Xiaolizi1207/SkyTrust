package com.skytrust.controller;

import com.skytrust.common.Result;
import com.skytrust.common.ResultCode;
import com.skytrust.entity.DictData;
import com.skytrust.entity.DictType;
import com.skytrust.service.DictDataService;
import com.skytrust.service.DictTypeService;
import com.skytrust.vo.DictDataVO;
import com.skytrust.vo.DictTypeVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 字典综合控制器
 * 提供字典相关的综合接口，如获取所有字典数据、前端缓存等
 *
 * @author SkyTrust Team
 */
@Slf4j
@Api(tags = "字典综合管理", description = "字典数据的综合查询接口")
@RestController
@RequestMapping("/api/dict")
@RequiredArgsConstructor
public class DictController {

    private final DictTypeService dictTypeService;
    private final DictDataService dictDataService;

    /**
     * 获取所有启用的字典类型及其字典数据（用于前端缓存）
     */
    @ApiOperation(value = "获取所有启用的字典类型及其字典数据")
    @GetMapping("/all-enabled")
    public Result<Map<String, List<DictDataVO>>> getAllEnabledDicts() {
        log.info("获取所有启用的字典类型及其字典数据请求");

        try {
            // 获取所有启用的字典类型
            List<DictType> dictTypes = dictTypeService.getAllEnabledDictTypes();
            Map<String, List<DictDataVO>> result = new HashMap<>();

            // 遍历每个字典类型，获取其启用的字典数据
            for (DictType dictType : dictTypes) {
                List<DictData> dictDatas = dictDataService.getEnabledDictDataByType(dictType.getTypeCode());
                List<DictDataVO> dictDataVOs = dictDatas.stream()
                        .map(this::convertToVO)
                        .collect(Collectors.toList());
                result.put(dictType.getTypeCode(), dictDataVOs);
            }

            return Result.success(result);
        } catch (Exception e) {
            log.error("获取所有启用的字典类型及其字典数据异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "获取字典数据异常: " + e.getMessage());
        }
    }

    /**
     * 根据字典类型编码获取启用的字典数据（前端通用接口）
     */
    @ApiOperation(value = "根据字典类型编码获取启用的字典数据")
    @GetMapping("/type/{dictType}")
    public Result<List<DictDataVO>> getEnabledDictDataByType(
            @ApiParam(value = "字典类型编码", required = true) @PathVariable String dictType) {
        log.info("根据字典类型编码获取启用的字典数据请求: dictType={}", dictType);

        try {
            List<DictData> dictDatas = dictDataService.getEnabledDictDataByType(dictType);
            List<DictDataVO> dictDataVOs = dictDatas.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            return Result.success(dictDataVOs);
        } catch (Exception e) {
            log.error("根据字典类型编码获取启用的字典数据异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "获取字典数据异常: " + e.getMessage());
        }
    }

    /**
     * 根据字典类型编码和字典值获取字典标签
     */
    @ApiOperation(value = "根据字典类型编码和字典值获取字典标签")
    @GetMapping("/label")
    public Result<String> getDictLabelByTypeAndValue(
            @ApiParam(value = "字典类型编码", required = true) @RequestParam String dictType,
            @ApiParam(value = "字典值", required = true) @RequestParam String dictValue) {
        log.info("根据字典类型编码和字典值获取字典标签请求: dictType={}, dictValue={}", dictType, dictValue);

        try {
            DictData dictData = dictDataService.getDictDataByTypeAndValue(dictType, dictValue);
            if (dictData == null) {
                return Result.error(ResultCode.PARAM_ERROR, "字典数据不存在");
            }
            return Result.success(dictData.getDictLabel());
        } catch (Exception e) {
            log.error("根据字典类型编码和字典值获取字典标签异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "获取字典标签异常: " + e.getMessage());
        }
    }

    /**
     * 根据字典类型编码和字典标签获取字典值
     */
    @ApiOperation(value = "根据字典类型编码和字典标签获取字典值")
    @GetMapping("/value")
    public Result<String> getDictValueByTypeAndLabel(
            @ApiParam(value = "字典类型编码", required = true) @RequestParam String dictType,
            @ApiParam(value = "字典标签", required = true) @RequestParam String dictLabel) {
        log.info("根据字典类型编码和字典标签获取字典值请求: dictType={}, dictLabel={}", dictType, dictLabel);

        try {
            DictData dictData = dictDataService.getDictDataByTypeAndLabel(dictType, dictLabel);
            if (dictData == null) {
                return Result.error(ResultCode.PARAM_ERROR, "字典数据不存在");
            }
            return Result.success(dictData.getDictValue());
        } catch (Exception e) {
            log.error("根据字典类型编码和字典标签获取字典值异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "获取字典值异常: " + e.getMessage());
        }
    }

    /**
     * 批量根据字典类型编码和字典值获取字典标签
     */
    @ApiOperation(value = "批量根据字典类型编码和字典值获取字典标签")
    @PostMapping("/batch-labels")
    public Result<Map<String, String>> getBatchDictLabels(@RequestBody Map<String, List<String>> request) {
        log.info("批量根据字典类型编码和字典值获取字典标签请求");

        try {
            Map<String, String> result = new HashMap<>();
            for (Map.Entry<String, List<String>> entry : request.entrySet()) {
                String dictType = entry.getKey();
                List<String> dictValues = entry.getValue();

                for (String dictValue : dictValues) {
                    DictData dictData = dictDataService.getDictDataByTypeAndValue(dictType, dictValue);
                    if (dictData != null) {
                        String key = dictType + ":" + dictValue;
                        result.put(key, dictData.getDictLabel());
                    }
                }
            }
            return Result.success(result);
        } catch (Exception e) {
            log.error("批量根据字典类型编码和字典值获取字典标签异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "批量获取字典标签异常: " + e.getMessage());
        }
    }

    /**
     * 刷新字典缓存（用于管理后台）
     */
    @ApiOperation(value = "刷新字典缓存")
    @PostMapping("/refresh-cache")
    public Result<Void> refreshDictCache() {
        log.info("刷新字典缓存请求");

        try {
            // 这里可以添加缓存刷新逻辑，如清除Redis缓存等
            // 目前只是一个示例，实际项目中需要根据缓存实现来编写
            return Result.success("字典缓存刷新成功");
        } catch (Exception e) {
            log.error("刷新字典缓存异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "刷新字典缓存异常: " + e.getMessage());
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
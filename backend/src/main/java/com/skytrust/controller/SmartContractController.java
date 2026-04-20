package com.skytrust.controller;

import com.skytrust.common.Result;
import com.skytrust.common.ResultCode;
import com.skytrust.dto.SmartContractDTO;
import com.skytrust.entity.SmartContract;
import com.skytrust.service.SmartContractService;
import com.skytrust.vo.SmartContractVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 智能合约控制器
 *
 * @author SkyTrust Team
 */
@Api(tags = "智能合约管理", description = "智能合约管理接口")
@Validated
@RestController
@RequestMapping("/api/smart-contracts")
public class SmartContractController {

    private final SmartContractService smartContractService;

    public SmartContractController(SmartContractService smartContractService) {
        this.smartContractService = smartContractService;
    }

    /**
     * 创建智能合约记录
     */
    @ApiOperation(value = "创建智能合约记录")
    @PostMapping
    public Result<SmartContractVO> createSmartContract(@Valid @RequestBody SmartContractDTO contractDTO) {
        // 转换DTO为实体
        SmartContract contract = new SmartContract();
        BeanUtils.copyProperties(contractDTO, contract);

        // 保存智能合约记录
        boolean saved = smartContractService.save(contract);
        if (!saved) {
            return Result.error("智能合约记录创建失败");
        }

        SmartContractVO contractVO = convertToVO(contract);
        return Result.success(contractVO, "智能合约记录创建成功");
    }

    /**
     * 更新智能合约记录信息
     */
    @ApiOperation(value = "更新智能合约记录信息")
    @PutMapping("/{id}")
    public Result<SmartContractVO> updateSmartContract(
            @ApiParam(value = "智能合约ID", required = true) @PathVariable Long id,
            @Valid @RequestBody SmartContractDTO contractDTO) {
        SmartContract contract = smartContractService.getById(id);
        if (contract == null) {
            return Result.error(ResultCode.DATA_NOT_EXIST.getCode(), "智能合约记录不存在");
        }

        // 更新字段（排除不能修改的字段）
        BeanUtils.copyProperties(contractDTO, contract, "id", "contractAddress", "createTime", "updateTime");

        boolean updated = smartContractService.updateById(contract);
        if (!updated) {
            return Result.error("智能合约记录更新失败");
        }

        SmartContractVO contractVO = convertToVO(contract);
        return Result.success(contractVO, "智能合约记录更新成功");
    }

    /**
     * 获取智能合约记录详情
     */
    @ApiOperation(value = "获取智能合约记录详情")
    @GetMapping("/{id}")
    public Result<SmartContractVO> getSmartContractById(@ApiParam(value = "智能合约ID", required = true) @PathVariable Long id) {
        SmartContract contract = smartContractService.getById(id);
        if (contract == null) {
            return Result.error(ResultCode.DATA_NOT_EXIST.getCode(), "智能合约记录不存在");
        }
        return Result.success(convertToVO(contract));
    }

    /**
     * 删除智能合约记录（逻辑删除）
     */
    @ApiOperation(value = "删除智能合约记录")
    @DeleteMapping("/{id}")
    public Result<Void> deleteSmartContract(@ApiParam(value = "智能合约ID", required = true) @PathVariable Long id) {
        boolean deleted = smartContractService.logicRemoveById(id);
        if (!deleted) {
            return Result.error("智能合约记录删除失败");
        }
        return Result.success("智能合约记录删除成功");
    }

    /**
     * 分页查询智能合约记录列表
     */
    @ApiOperation(value = "分页查询智能合约记录列表")
    @GetMapping
    public Result<List<SmartContractVO>> getSmartContractList(
            @ApiParam(value = "页码") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam(value = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @ApiParam(value = "合约地址") @RequestParam(required = false) String contractAddress,
            @ApiParam(value = "合约类型") @RequestParam(required = false) String contractType,
            @ApiParam(value = "合约状态") @RequestParam(required = false) Integer contractStatus,
            @ApiParam(value = "合约名称") @RequestParam(required = false) String contractName) {

        // 简化处理：使用Service的list方法
        List<SmartContract> contracts = smartContractService.list();

        // 应用过滤条件
        List<SmartContract> filteredContracts = contracts.stream()
                .filter(contract -> contractAddress == null || contract.getContractAddress().contains(contractAddress))
                .filter(contract -> contractType == null || contract.getContractType().toString().equals(contractType))
                .filter(contract -> contractStatus == null || contract.getContractStatus().equals(contractStatus))
                .filter(contract -> contractName == null || contract.getContractName().contains(contractName))
                .skip((page - 1) * (long) size)
                .limit(size)
                .collect(Collectors.toList());

        List<SmartContractVO> contractVOs = filteredContracts.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return Result.success(contractVOs);
    }

    /**
     * 根据合约地址查询合约
     */
    @ApiOperation(value = "根据合约地址查询合约")
    @GetMapping("/address/{contractAddress}")
    public Result<SmartContractVO> getSmartContractByAddress(@ApiParam(value = "合约地址", required = true) @PathVariable String contractAddress) {
        SmartContract contract = smartContractService.getByContractAddress(contractAddress);
        if (contract == null) {
            return Result.error(ResultCode.DATA_NOT_EXIST.getCode(), "智能合约记录不存在");
        }
        return Result.success(convertToVO(contract));
    }

    /**
     * 根据类型查询合约
     */
    @ApiOperation(value = "根据类型查询合约")
    @GetMapping("/type/{contractType}")
    public Result<List<SmartContractVO>> getSmartContractsByType(@ApiParam(value = "合约类型", required = true) @PathVariable String contractType) {
        List<SmartContract> contracts = smartContractService.getByContractType(contractType);
        List<SmartContractVO> contractVOs = contracts.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        return Result.success(contractVOs);
    }

    /**
     * 获取最新部署的合约
     */
    @ApiOperation(value = "获取最新部署的合约")
    @GetMapping("/latest")
    public Result<SmartContractVO> getLatestContract() {
        SmartContract contract = smartContractService.getLatestContract();
        if (contract == null) {
            return Result.error(ResultCode.DATA_NOT_EXIST.getCode(), "智能合约记录不存在");
        }
        return Result.success(convertToVO(contract));
    }

    /**
     * 将SmartContract实体转换为SmartContractVO
     */
    private SmartContractVO convertToVO(SmartContract contract) {
        if (contract == null) {
            return null;
        }
        SmartContractVO contractVO = new SmartContractVO();
        BeanUtils.copyProperties(contract, contractVO);
        return contractVO;
    }
}
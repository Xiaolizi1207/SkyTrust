package com.skytrust.controller;

import com.skytrust.common.Result;
import com.skytrust.common.ResultCode;
import com.skytrust.exception.BusinessException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 统一响应结果使用示例
 */
@Api(tags = "示例接口", description = "演示统一响应结果的使用")
@Validated
@RestController
@RequestMapping("/api/example")
public class ExampleController {

    /**
     * 示例1：成功响应（无数据）
     */
    @ApiOperation(value = "成功响应（无数据）")
    @GetMapping("/success")
    public Result<Void> successWithoutData() {
        return Result.success();
    }

    /**
     * 示例2：成功响应（带消息）
     */
    @ApiOperation(value = "成功响应（带消息）")
    @GetMapping("/success-with-message")
    public Result<Void> successWithMessage() {
        return Result.success("操作成功完成");
    }

    /**
     * 示例3：成功响应（带数据）
     */
    @ApiOperation(value = "成功响应（带数据）")
    @GetMapping("/success-with-data")
    public Result<Map<String, Object>> successWithData() {
        Map<String, Object> data = new HashMap<>();
        data.put("id", 1L);
        data.put("name", "测试数据");
        data.put("createTime", LocalDateTime.now());
        return Result.success(data);
    }

    /**
     * 示例4：成功响应（带数据和消息）
     */
    @ApiOperation(value = "成功响应（带数据和消息）")
    @GetMapping("/success-with-data-and-message")
    public Result<Map<String, Object>> successWithDataAndMessage() {
        Map<String, Object> data = new HashMap<>();
        data.put("id", 2L);
        data.put("name", "测试数据2");
        return Result.success(data, "查询成功");
    }

    /**
     * 示例5：失败响应（默认错误码）
     */
    @ApiOperation(value = "失败响应（默认错误码）")
    @GetMapping("/error-default")
    public Result<Void> errorDefault() {
        return Result.error();
    }

    /**
     * 示例6：失败响应（带自定义消息）
     */
    @ApiOperation(value = "失败响应（带自定义消息）")
    @GetMapping("/error-with-message")
    public Result<Void> errorWithMessage() {
        return Result.error("自定义错误消息");
    }

    /**
     * 示例7：失败响应（带状态码和消息）
     */
    @ApiOperation(value = "失败响应（带状态码和消息）")
    @GetMapping("/error-with-code")
    public Result<Void> errorWithCode() {
        return Result.error(1001, "业务异常错误");
    }

    /**
     * 示例8：失败响应（使用ResultCode枚举）
     */
    @ApiOperation(value = "失败响应（使用ResultCode枚举）")
    @GetMapping("/error-with-resultcode")
    public Result<Void> errorWithResultCode() {
        return Result.error(ResultCode.DATA_NOT_EXIST);
    }

    /**
     * 示例9：参数校验示例（@Valid）
     */
    @Data
    public static class CreateRequest {
        @NotBlank(message = "用户名不能为空")
        private String username;

        @NotNull(message = "年龄不能为空")
        private Integer age;
    }

    @ApiOperation(value = "参数校验示例（@Valid）")
    @PostMapping("/create")
    public Result<CreateRequest> create(@Valid @RequestBody CreateRequest request) {
        // 参数校验通过后执行业务逻辑
        return Result.success(request, "创建成功");
    }

    /**
     * 示例10：参数校验示例（@Validated on method parameters）
     */
    @ApiOperation(value = "参数校验示例（@Validated on method parameters）")
    @GetMapping("/query")
    public Result<String> query(@NotBlank(message = "查询条件不能为空") String keyword) {
        return Result.success("查询结果: " + keyword);
    }

    /**
     * 示例11：业务异常示例
     */
    @ApiOperation(value = "业务异常示例")
    @GetMapping("/business-exception")
    public Result<Void> businessException() {
        throw new BusinessException("这是一个业务异常示例");
    }

    /**
     * 示例12：业务异常示例（使用ResultCode）
     */
    @ApiOperation(value = "业务异常示例（使用ResultCode）")
    @GetMapping("/business-exception-with-resultcode")
    public Result<Void> businessExceptionWithResultCode() {
        throw new BusinessException(ResultCode.DATA_DUPLICATE);
    }

    /**
     * 示例13：返回原始数据（不使用Result包装）
     * 注意：如果启用了GlobalResponseAdvice，此方法返回的数据仍会被包装
     * 如果需要返回原始数据，可以将此方法的返回类型添加到EXCLUDE_RETURN_TYPES中
     */
    @ApiOperation(value = "返回原始数据")
    @GetMapping("/raw-data")
    public Map<String, Object> rawData() {
        Map<String, Object> data = new HashMap<>();
        data.put("raw", true);
        data.put("message", "这是原始数据，不会被Result包装");
        return data;
    }

    /**
     * 示例14：返回字符串（特殊处理）
     */
    @ApiOperation(value = "返回字符串")
    @GetMapping("/string")
    public String stringResult() {
        return "这是一个字符串响应";
    }
}
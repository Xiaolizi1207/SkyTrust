package com.skytrust.common;

import lombok.Getter;

/**
 * 响应状态码枚举
 */
@Getter
public enum ResultCode {

    /**
     * 成功
     */
    SUCCESS(200, "操作成功"),

    /**
     * 失败
     */
    ERROR(500, "操作失败"),

    /**
     * 参数校验失败
     */
    VALIDATE_FAILED(400, "参数校验失败"),

    /**
     * 未授权
     */
    UNAUTHORIZED(401, "暂未登录或token已经过期"),

    /**
     * 权限不足
     */
    FORBIDDEN(403, "权限不足"),

    /**
     * 资源未找到
     */
    NOT_FOUND(404, "资源未找到"),

    /**
     * 请求方法不支持
     */
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),

    /**
     * 请求过于频繁
     */
    TOO_MANY_REQUESTS(429, "请求过于频繁"),

    /**
     * 系统内部错误
     */
    INTERNAL_SERVER_ERROR(500, "系统内部错误"),

    /**
     * 服务不可用
     */
    SERVICE_UNAVAILABLE(503, "服务不可用"),

    /**
     * 业务异常
     */
    BUSINESS_ERROR(1000, "业务异常"),

    /**
     * 数据已存在
     */
    DATA_EXIST(1001, "数据已存在"),

    /**
     * 数据不存在
     */
    DATA_NOT_EXIST(1002, "数据不存在"),

    /**
     * 数据重复
     */
    DATA_DUPLICATE(1003, "数据重复"),

    /**
     * 数据状态异常
     */
    DATA_STATUS_ERROR(1004, "数据状态异常"),

    /**
     * 用户未登录
     */
    USER_NOT_LOGIN(4011, "用户未登录"),

    /**
     * 令牌无效
     */
    TOKEN_INVALID(4012, "令牌无效"),

    /**
     * 用户不存在
     */
    USER_NOT_EXIST(1005, "用户不存在"),

    /**
     * 用户已被禁用
     */
    USER_DISABLED(1006, "用户已被禁用"),

    /**
     * 密码错误
     */
    PASSWORD_ERROR(1007, "密码错误"),

    /**
     * 设备不存在
     */
    DEVICE_NOT_EXIST(1008, "设备不存在"),

    /**
     * 支付记录不存在
     */
    PAYMENT_NOT_EXIST(1009, "支付记录不存在"),

    /**
     * 订单不存在
     */
    ORDER_NOT_EXIST(1010, "订单不存在"),

    /**
     * 订单无法取消
     */
    ORDER_CANNOT_CANCEL(1011, "订单无法取消"),

    /**
     * 订单无法完成
     */
    ORDER_CANNOT_COMPLETE(1012, "订单无法完成"),

    /**
     * 文件上传失败
     */
    FILE_UPLOAD_ERROR(2001, "文件上传失败"),

    /**
     * 文件类型不支持
     */
    FILE_TYPE_NOT_SUPPORTED(2002, "文件类型不支持"),

    /**
     * 文件大小超出限制
     */
    FILE_SIZE_EXCEEDED(2003, "文件大小超出限制"),

    /**
     * 区块链操作失败
     */
    BLOCKCHAIN_ERROR(3001, "区块链操作失败"),

    /**
     * 智能合约执行失败
     */
    CONTRACT_EXECUTION_ERROR(3002, "智能合约执行失败"),

    /**
     * 物联网设备连接失败
     */
    IOT_CONNECTION_ERROR(4001, "物联网设备连接失败"),

    /**
     * 设备状态异常
     */
    DEVICE_STATUS_ERROR(4002, "设备状态异常"),

    /**
     * 系统错误
     */
    SYSTEM_ERROR(5000, "系统错误"),

    /**
     * 参数错误
     */
    PARAM_ERROR(5001, "参数错误");

    /**
     * 状态码
     */
    private final Integer code;

    /**
     * 状态消息
     */
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
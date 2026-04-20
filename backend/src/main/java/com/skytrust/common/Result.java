package com.skytrust.common;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 统一API响应结果
 *
 * @param <T> 数据泛型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 时间戳
     */
    private LocalDateTime timestamp;

    /**
     * 成功响应（无数据）
     *
     * @return 成功响应结果
     */
    public static <T> Result<T> success() {
        return success(null, ResultCode.SUCCESS.getMessage());
    }

    /**
     * 成功响应（带消息）
     *
     * @param message 成功消息
     * @return 成功响应结果
     */
    public static <T> Result<T> success(String message) {
        return success(null, message);
    }

    /**
     * 成功响应（带数据）
     *
     * @param data 响应数据
     * @return 成功响应结果
     */
    public static <T> Result<T> success(T data) {
        return success(data, ResultCode.SUCCESS.getMessage());
    }

    /**
     * 成功响应（带数据和消息）
     *
     * @param data    响应数据
     * @param message 成功消息
     * @return 成功响应结果
     */
    public static <T> Result<T> success(T data, String message) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage(message);
        result.setData(data);
        result.setTimestamp(LocalDateTime.now());
        return result;
    }

    /**
     * 失败响应（默认错误码）
     *
     * @return 失败响应结果
     */
    public static <T> Result<T> error() {
        return error(ResultCode.ERROR.getCode(), ResultCode.ERROR.getMessage());
    }

    /**
     * 失败响应（带消息）
     *
     * @param message 错误消息
     * @return 失败响应结果
     */
    public static <T> Result<T> error(String message) {
        return error(ResultCode.ERROR.getCode(), message);
    }

    /**
     * 失败响应（带状态码和消息）
     *
     * @param code    错误码
     * @param message 错误消息
     * @return 失败响应结果
     */
    public static <T> Result<T> error(Integer code, String message) {
        return error(code, message, null);
    }

    /**
     * 失败响应（带状态码、消息和数据）
     *
     * @param code    错误码
     * @param message 错误消息
     * @param data    错误数据（如验证错误详情）
     * @return 失败响应结果
     */
    public static <T> Result<T> error(Integer code, String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setData(data);
        result.setTimestamp(LocalDateTime.now());
        return result;
    }

    /**
     * 失败响应（使用ResultCode枚举）
     *
     * @param resultCode 结果码枚举
     * @return 失败响应结果
     */
    public static <T> Result<T> error(ResultCode resultCode) {
        return error(resultCode.getCode(), resultCode.getMessage());
    }

    /**
     * 失败响应（使用ResultCode枚举和自定义消息）
     *
     * @param resultCode 结果码枚举
     * @param message    自定义消息
     * @return 失败响应结果
     */
    public static <T> Result<T> error(ResultCode resultCode, String message) {
        return error(resultCode.getCode(), message);
    }

    /**
     * 是否成功
     *
     * @return true-成功，false-失败
     */
    public boolean isSuccess() {
        return ResultCode.SUCCESS.getCode().equals(this.code);
    }

    // Getter and Setter methods (added because Lombok may not work)
    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
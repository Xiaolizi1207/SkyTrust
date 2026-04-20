package com.skytrust.handler;

import com.skytrust.common.Result;
import com.skytrust.common.ResultCode;
import com.skytrust.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.http.converter.HttpMessageConversionException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理参数校验异常（@Validated）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        log.warn("参数校验失败: {}", errors);
        return Result.error(ResultCode.VALIDATE_FAILED.getCode(),
                ResultCode.VALIDATE_FAILED.getMessage(), errors);
    }

    /**
     * 处理参数绑定异常（@Valid）
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Map<String, String>> handleBindException(BindException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        log.warn("参数绑定失败: {}", errors);
        return Result.error(ResultCode.VALIDATE_FAILED.getCode(),
                ResultCode.VALIDATE_FAILED.getMessage(), errors);
    }

    /**
     * 处理约束违反异常（@Validated on method parameters）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<List<String>> handleConstraintViolationException(ConstraintViolationException ex) {
        List<String> errors = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.toList());
        log.warn("约束违反: {}", errors);
        return Result.error(ResultCode.VALIDATE_FAILED.getCode(),
                ResultCode.VALIDATE_FAILED.getMessage(), errors);
    }

    /**
     * 处理资源未找到异常
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<String> handleNoHandlerFoundException(NoHandlerFoundException ex) {
        log.warn("资源未找到: {} {}", ex.getHttpMethod(), ex.getRequestURL());
        return Result.error(ResultCode.NOT_FOUND);
    }

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<String> handleBusinessException(BusinessException ex) {
        log.warn("业务异常: {}", ex.getMessage());
        return Result.error(ex.getCode(), ex.getMessage());
    }

    /**
     * 处理权限不足异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<String> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("权限不足: {}", ex.getMessage());
        return Result.error(ResultCode.FORBIDDEN);
    }

    /**
     * 处理认证失败异常
     */
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<String> handleAuthenticationException(AuthenticationException ex) {
        log.warn("认证失败: {}", ex.getMessage());
        return Result.error(ResultCode.UNAUTHORIZED);
    }

    /**
     * 处理数据库访问异常
     */
    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<String> handleDataAccessException(DataAccessException ex) {
        log.error("数据库访问异常: ", ex);
        return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
    }

    /**
     * 处理唯一约束冲突异常
     */
    @ExceptionHandler(DuplicateKeyException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<String> handleDuplicateKeyException(DuplicateKeyException ex) {
        log.warn("唯一约束冲突: {}", ex.getMessage());
        return Result.error(ResultCode.DATA_DUPLICATE);
    }

    /**
     * 处理请求方法不支持异常
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Result<String> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        log.warn("请求方法不支持: {} {}", ex.getMethod(), ex.getSupportedHttpMethods());
        return Result.error(ResultCode.METHOD_NOT_ALLOWED);
    }

    /**
     * 处理媒体类型不支持异常
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public Result<String> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException ex) {
        log.warn("媒体类型不支持: {}", ex.getContentType());
        return Result.error(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(), "不支持的媒体类型");
    }

    /**
     * 处理缺少请求参数异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<String> handleMissingServletRequestParameterException(MissingServletRequestParameterException ex) {
        log.warn("缺少请求参数: {} {}", ex.getParameterName(), ex.getParameterType());
        return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "缺少必要参数: " + ex.getParameterName());
    }

    /**
     * 处理文件上传异常
     */
    @ExceptionHandler(MultipartException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<String> handleMultipartException(MultipartException ex) {
        log.warn("文件上传异常: {}", ex.getMessage());
        return Result.error(ResultCode.FILE_UPLOAD_ERROR);
    }

    /**
     * 处理文件大小超出限制异常
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<String> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        log.warn("文件大小超出限制: {}", ex.getMessage());
        return Result.error(ResultCode.FILE_SIZE_EXCEEDED);
    }

    /**
     * 处理缺少请求部分异常（文件上传）
     */
    @ExceptionHandler(MissingServletRequestPartException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<String> handleMissingServletRequestPartException(MissingServletRequestPartException ex) {
        log.warn("缺少请求部分: {}", ex.getRequestPartName());
        return Result.error(ResultCode.FILE_UPLOAD_ERROR.getCode(), "缺少文件上传部分: " + ex.getRequestPartName());
    }

    /**
     * 处理HTTP消息转换异常（JSON解析失败等）
     */
    @ExceptionHandler(HttpMessageConversionException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<String> handleHttpMessageConversionException(HttpMessageConversionException ex) {
        log.warn("HTTP消息转换异常: {}", ex.getMessage());
        return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "请求参数格式错误");
    }

    /**
     * 处理所有未捕获的异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<String> handleAllExceptions(Exception ex) {
        log.error("系统异常: ", ex);
        return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
    }
}
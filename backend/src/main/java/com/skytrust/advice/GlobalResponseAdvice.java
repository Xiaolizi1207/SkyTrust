package com.skytrust.advice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skytrust.common.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.Set;

/**
 * 全局响应处理器（自动包装Controller返回值）
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalResponseAdvice implements ResponseBodyAdvice<Object> {

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 注入 context-path，用于路径匹配前去除前缀
     */
    @Value("${server.servlet.context-path:}")
    private String contextPath;

    /**
     * 需要排除的路径（不进行包装）
     */
    private static final Set<String> EXCLUDE_PATHS = new HashSet<>();

    static {
        // SpringDoc OpenAPI 3 / Knife4j 路径
        EXCLUDE_PATHS.add("/v3/api-docs");
        EXCLUDE_PATHS.add("/swagger-ui");
        EXCLUDE_PATHS.add("/doc.html");
        EXCLUDE_PATHS.add("/favicon.ico");
    }

    /**
     * 需要排除的返回类型（不进行包装）
     */
    private static final Set<Class<?>> EXCLUDE_RETURN_TYPES = new HashSet<>();

    static {
        EXCLUDE_RETURN_TYPES.add(Result.class);
        EXCLUDE_RETURN_TYPES.add(byte[].class);
        EXCLUDE_RETURN_TYPES.add(Void.class);
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return !EXCLUDE_RETURN_TYPES.contains(returnType.getParameterType());
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {

        String path = request.getURI().getPath();
        // 去除 context-path 前缀后再匹配（例如 /api/v3/api-docs → /v3/api-docs）
        if (StringUtils.hasText(contextPath) && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }
        for (String excludePath : EXCLUDE_PATHS) {
            if (path.startsWith(excludePath)) {
                return body;
            }
        }

        if (body instanceof Result) {
            return body;
        }

        if (body instanceof String) {
            try {
                Result<?> result = Result.success(body);
                return objectMapper.writeValueAsString(result);
            } catch (Exception e) {
                log.error("响应结果序列化失败", e);
                return Result.error("响应结果序列化失败");
            }
        }

        return Result.success(body);
    }
}
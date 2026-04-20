package com.skytrust.advice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skytrust.common.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
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
     * 需要排除的路径（不进行包装）
     */
    private static final Set<String> EXCLUDE_PATHS = new HashSet<>();

    static {
        // Swagger2 路径（Spring Boot 2.7 专用）
        EXCLUDE_PATHS.add("/v2/api-docs");
        EXCLUDE_PATHS.add("/swagger-ui");
        EXCLUDE_PATHS.add("/doc.html");
        EXCLUDE_PATHS.add("/webjars");
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
package com.skytrust.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * JSON工具类（基于Jackson）
 *
 * @author SkyTrust Team
 */
public class JsonUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        // 注册JavaTime模块，支持LocalDateTime等Java 8时间类
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(
                DateTimeFormatter.ofPattern(DateUtil.DATETIME_FORMAT)));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(
                DateTimeFormatter.ofPattern(DateUtil.DATETIME_FORMAT)));

        objectMapper.registerModule(javaTimeModule);

        // 配置
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // 日期序列化为字符串而非时间戳
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES); // 忽略未知属性
        objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT); // 空字符串转为null
    }

    private JsonUtil() {
        // 工具类，防止实例化
    }

    /**
     * 获取ObjectMapper实例
     *
     * @return ObjectMapper
     */
    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    /**
     * 对象转JSON字符串
     *
     * @param object 对象
     * @return JSON字符串
     */
    public static String toJson(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("对象转JSON失败", e);
        }
    }

    /**
     * 对象转JSON字符串（美化输出）
     *
     * @param object 对象
     * @return 美化后的JSON字符串
     */
    public static String toPrettyJson(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("对象转JSON（美化）失败", e);
        }
    }

    /**
     * JSON字符串转对象
     *
     * @param json  JSON字符串
     * @param clazz 目标类
     * @param <T>   泛型类型
     * @return 对象
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (StringUtil.isEmpty(json)) {
            return null;
        }
        try {
            return objectMapper.readValue(json, clazz);
        } catch (IOException e) {
            throw new RuntimeException("JSON转对象失败: " + json, e);
        }
    }

    /**
     * JSON字符串转对象（支持复杂类型）
     *
     * @param json          JSON字符串
     * @param typeReference 类型引用
     * @param <T>           泛型类型
     * @return 对象
     */
    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        if (StringUtil.isEmpty(json)) {
            return null;
        }
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (IOException e) {
            throw new RuntimeException("JSON转对象失败: " + json, e);
        }
    }

    /**
     * JSON字符串转对象（支持Type）
     *
     * @param json JSON字符串
     * @param type 类型
     * @param <T>  泛型类型
     * @return 对象
     */
    public static <T> T fromJson(String json, Type type) {
        if (StringUtil.isEmpty(json)) {
            return null;
        }
        try {
            return objectMapper.readValue(json, objectMapper.getTypeFactory().constructType(type));
        } catch (IOException e) {
            throw new RuntimeException("JSON转对象失败: " + json, e);
        }
    }

    /**
     * JSON字符串转List
     *
     * @param json  JSON字符串
     * @param clazz List元素类型
     * @param <T>   泛型类型
     * @return List
     */
    public static <T> List<T> fromJsonToList(String json, Class<T> clazz) {
        if (StringUtil.isEmpty(json)) {
            return null;
        }
        try {
            JavaType javaType = objectMapper.getTypeFactory().constructCollectionType(List.class, clazz);
            return objectMapper.readValue(json, javaType);
        } catch (IOException e) {
            throw new RuntimeException("JSON转List失败: " + json, e);
        }
    }

    /**
     * JSON字符串转Map
     *
     * @param json JSON字符串
     * @return Map
     */
    public static Map<String, Object> fromJsonToMap(String json) {
        if (StringUtil.isEmpty(json)) {
            return null;
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (IOException e) {
            throw new RuntimeException("JSON转Map失败: " + json, e);
        }
    }

    /**
     * JSON字符串转Map（指定value类型）
     *
     * @param json        JSON字符串
     * @param valueClazz  Map value类型
     * @param <V>         泛型类型
     * @return Map
     */
    public static <V> Map<String, V> fromJsonToMap(String json, Class<V> valueClazz) {
        if (StringUtil.isEmpty(json)) {
            return null;
        }
        try {
            JavaType javaType = objectMapper.getTypeFactory().constructMapType(Map.class, String.class, valueClazz);
            return objectMapper.readValue(json, javaType);
        } catch (IOException e) {
            throw new RuntimeException("JSON转Map失败: " + json, e);
        }
    }

    /**
     * 对象转Map
     *
     * @param object 对象
     * @return Map
     */
    public static Map<String, Object> objectToMap(Object object) {
        if (object == null) {
            return null;
        }
        return objectMapper.convertValue(object, new TypeReference<Map<String, Object>>() {});
    }

    /**
     * Map转对象
     *
     * @param map   Map
     * @param clazz 目标类
     * @param <T>   泛型类型
     * @return 对象
     */
    public static <T> T mapToObject(Map<String, Object> map, Class<T> clazz) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        return objectMapper.convertValue(map, clazz);
    }

    /**
     * 深度拷贝对象（通过JSON序列化/反序列化）
     *
     * @param object 原对象
     * @param clazz  目标类
     * @param <T>    泛型类型
     * @return 拷贝后的对象
     */
    public static <T> T deepCopy(Object object, Class<T> clazz) {
        if (object == null) {
            return null;
        }
        String json = toJson(object);
        return fromJson(json, clazz);
    }

    /**
     * 判断字符串是否为合法JSON
     *
     * @param json 字符串
     * @return 是否为合法JSON
     */
    public static boolean isValidJson(String json) {
        if (StringUtil.isEmpty(json)) {
            return false;
        }
        try {
            objectMapper.readTree(json);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 判断字符串是否为合法JSON对象
     *
     * @param json 字符串
     * @return 是否为合法JSON对象
     */
    public static boolean isValidJsonObject(String json) {
        if (StringUtil.isEmpty(json)) {
            return false;
        }
        try {
            return objectMapper.readTree(json).isObject();
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 判断字符串是否为合法JSON数组
     *
     * @param json 字符串
     * @return 是否为合法JSON数组
     */
    public static boolean isValidJsonArray(String json) {
        if (StringUtil.isEmpty(json)) {
            return false;
        }
        try {
            return objectMapper.readTree(json).isArray();
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 安全地从JSON字符串获取值（不抛出异常）
     *
     * @param json  JSON字符串
     * @param clazz 目标类
     * @param <T>   泛型类型
     * @return 对象，如果解析失败则返回null
     */
    public static <T> T safeFromJson(String json, Class<T> clazz) {
        if (StringUtil.isEmpty(json)) {
            return null;
        }
        try {
            return objectMapper.readValue(json, clazz);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 安全地将对象转换为JSON字符串（不抛出异常）
     *
     * @param object 对象
     * @return JSON字符串，如果转换失败则返回null
     */
    public static String safeToJson(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    /**
     * 格式化JSON字符串（美化输出）
     *
     * @param json 原始JSON字符串
     * @return 美化后的JSON字符串
     */
    public static String formatJson(String json) {
        if (StringUtil.isEmpty(json)) {
            return json;
        }
        try {
            Object jsonNode = objectMapper.readValue(json, Object.class);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
        } catch (IOException e) {
            return json;
        }
    }
}
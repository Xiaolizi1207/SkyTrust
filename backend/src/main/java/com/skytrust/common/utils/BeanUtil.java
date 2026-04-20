package com.skytrust.common.utils;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Bean操作工具类
 *
 * @author SkyTrust Team
 */
public class BeanUtil {

    private BeanUtil() {
        // 工具类，防止实例化
    }

    // ==================== Bean属性拷贝 ====================

    /**
     * 拷贝源对象的属性到目标对象（忽略null值）
     *
     * @param source 源对象
     * @param target 目标对象
     */
    public static void copyProperties(Object source, Object target) {
        if (source == null || target == null) {
            return;
        }
        BeanUtils.copyProperties(source, target);
    }

    /**
     * 拷贝源对象的属性到目标对象（忽略指定属性）
     *
     * @param source           源对象
     * @param target           目标对象
     * @param ignoreProperties 忽略的属性名
     */
    public static void copyProperties(Object source, Object target, String... ignoreProperties) {
        if (source == null || target == null) {
            return;
        }
        BeanUtils.copyProperties(source, target, ignoreProperties);
    }

    /**
     * 拷贝源对象的属性到目标对象（忽略null值）
     *
     * @param source 源对象
     * @param target 目标对象
     */
    public static void copyPropertiesIgnoreNull(Object source, Object target) {
        if (source == null || target == null) {
            return;
        }
        String[] nullPropertyNames = getNullPropertyNames(source);
        BeanUtils.copyProperties(source, target, nullPropertyNames);
    }

    /**
     * 获取对象中值为null的属性名数组
     *
     * @param source 源对象
     * @return 值为null的属性名数组
     */
    public static String[] getNullPropertyNames(Object source) {
        if (source == null) {
            return new String[0];
        }
        final BeanWrapper src = new BeanWrapperImpl(source);
        PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<>();
        for (PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) {
                emptyNames.add(pd.getName());
            }
        }
        return emptyNames.toArray(new String[0]);
    }

    /**
     * 拷贝源对象的属性到目标对象（忽略空值：null和空字符串）
     *
     * @param source 源对象
     * @param target 目标对象
     */
    public static void copyPropertiesIgnoreEmpty(Object source, Object target) {
        if (source == null || target == null) {
            return;
        }
        String[] emptyPropertyNames = getEmptyPropertyNames(source);
        BeanUtils.copyProperties(source, target, emptyPropertyNames);
    }

    /**
     * 获取对象中值为null或空字符串的属性名数组
     *
     * @param source 源对象
     * @return 值为null或空字符串的属性名数组
     */
    public static String[] getEmptyPropertyNames(Object source) {
        if (source == null) {
            return new String[0];
        }
        final BeanWrapper src = new BeanWrapperImpl(source);
        PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<>();
        for (PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) {
                emptyNames.add(pd.getName());
            } else if (srcValue instanceof CharSequence && StringUtil.isEmpty((CharSequence) srcValue)) {
                emptyNames.add(pd.getName());
            } else if (srcValue instanceof Collection && ((Collection<?>) srcValue).isEmpty()) {
                emptyNames.add(pd.getName());
            } else if (srcValue instanceof Map && ((Map<?, ?>) srcValue).isEmpty()) {
                emptyNames.add(pd.getName());
            }
        }
        return emptyNames.toArray(new String[0]);
    }

    // ==================== Bean转换 ====================

    /**
     * 转换对象类型（浅拷贝）
     *
     * @param source    源对象
     * @param targetClass 目标类
     * @param <T>       目标类型
     * @return 转换后的对象
     */
    public static <T> T convert(Object source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }
        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            copyProperties(source, target);
            return target;
        } catch (Exception e) {
            throw new RuntimeException("对象转换失败: " + source.getClass() + " -> " + targetClass, e);
        }
    }

    /**
     * 转换对象类型（忽略null值）
     *
     * @param source      源对象
     * @param targetClass 目标类
     * @param <T>         目标类型
     * @return 转换后的对象
     */
    public static <T> T convertIgnoreNull(Object source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }
        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            copyPropertiesIgnoreNull(source, target);
            return target;
        } catch (Exception e) {
            throw new RuntimeException("对象转换失败: " + source.getClass() + " -> " + targetClass, e);
        }
    }

    /**
     * 转换对象类型（忽略空值）
     *
     * @param source      源对象
     * @param targetClass 目标类
     * @param <T>         目标类型
     * @return 转换后的对象
     */
    public static <T> T convertIgnoreEmpty(Object source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }
        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            copyPropertiesIgnoreEmpty(source, target);
            return target;
        } catch (Exception e) {
            throw new RuntimeException("对象转换失败: " + source.getClass() + " -> " + targetClass, e);
        }
    }

    /**
     * 转换对象类型（自定义转换器）
     *
     * @param source    源对象
     * @param converter 转换器函数
     * @param <S>       源类型
     * @param <T>       目标类型
     * @return 转换后的对象
     */
    public static <S, T> T convert(S source, Function<S, T> converter) {
        if (source == null) {
            return null;
        }
        return converter.apply(source);
    }

    /**
     * 转换对象列表
     *
     * @param sourceList  源对象列表
     * @param targetClass 目标类
     * @param <S>         源类型
     * @param <T>         目标类型
     * @return 转换后的对象列表
     */
    public static <S, T> List<T> convertList(List<S> sourceList, Class<T> targetClass) {
        if (StringUtil.isEmpty(sourceList)) {
            return new ArrayList<>();
        }
        return sourceList.stream()
                .map(source -> convert(source, targetClass))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 转换对象列表（忽略null值）
     *
     * @param sourceList  源对象列表
     * @param targetClass 目标类
     * @param <S>         源类型
     * @param <T>         目标类型
     * @return 转换后的对象列表
     */
    public static <S, T> List<T> convertListIgnoreNull(List<S> sourceList, Class<T> targetClass) {
        if (StringUtil.isEmpty(sourceList)) {
            return new ArrayList<>();
        }
        return sourceList.stream()
                .map(source -> convertIgnoreNull(source, targetClass))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 转换对象列表（自定义转换器）
     *
     * @param sourceList 源对象列表
     * @param converter  转换器函数
     * @param <S>        源类型
     * @param <T>        目标类型
     * @return 转换后的对象列表
     */
    public static <S, T> List<T> convertList(List<S> sourceList, Function<S, T> converter) {
        if (StringUtil.isEmpty(sourceList)) {
            return new ArrayList<>();
        }
        return sourceList.stream()
                .map(converter)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // ==================== Bean属性操作 ====================

    /**
     * 获取对象的所有属性名
     *
     * @param object 对象
     * @return 属性名列表
     */
    public static List<String> getPropertyNames(Object object) {
        if (object == null) {
            return new ArrayList<>();
        }
        BeanWrapper beanWrapper = new BeanWrapperImpl(object);
        PropertyDescriptor[] pds = beanWrapper.getPropertyDescriptors();
        return Arrays.stream(pds)
                .map(PropertyDescriptor::getName)
                .filter(name -> !"class".equals(name)) // 排除class属性
                .collect(Collectors.toList());
    }

    /**
     * 获取对象的属性值
     *
     * @param object     对象
     * @param propertyName 属性名
     * @return 属性值
     */
    public static Object getPropertyValue(Object object, String propertyName) {
        if (object == null || StringUtil.isEmpty(propertyName)) {
            return null;
        }
        BeanWrapper beanWrapper = new BeanWrapperImpl(object);
        return beanWrapper.getPropertyValue(propertyName);
    }

    /**
     * 设置对象的属性值
     *
     * @param object       对象
     * @param propertyName   属性名
     * @param propertyValue  属性值
     */
    public static void setPropertyValue(Object object, String propertyName, Object propertyValue) {
        if (object == null || StringUtil.isEmpty(propertyName)) {
            return;
        }
        BeanWrapper beanWrapper = new BeanWrapperImpl(object);
        beanWrapper.setPropertyValue(propertyName, propertyValue);
    }

    /**
     * 获取对象的属性值Map
     *
     * @param object 对象
     * @return 属性名-属性值Map
     */
    public static Map<String, Object> getPropertyMap(Object object) {
        if (object == null) {
            return new HashMap<>();
        }
        BeanWrapper beanWrapper = new BeanWrapperImpl(object);
        PropertyDescriptor[] pds = beanWrapper.getPropertyDescriptors();
        Map<String, Object> map = new HashMap<>();
        for (PropertyDescriptor pd : pds) {
            String name = pd.getName();
            if (!"class".equals(name)) { // 排除class属性
                Object value = beanWrapper.getPropertyValue(name);
                map.put(name, value);
            }
        }
        return map;
    }

    /**
     * 将Map转换为对象
     *
     * @param map       Map
     * @param targetClass 目标类
     * @param <T>        目标类型
     * @return 对象
     */
    public static <T> T mapToObject(Map<String, Object> map, Class<T> targetClass) {
        if (StringUtil.isEmpty(map)) {
            try {
                return targetClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException("创建对象失败: " + targetClass, e);
            }
        }
        try {
            T object = targetClass.getDeclaredConstructor().newInstance();
            BeanWrapper beanWrapper = new BeanWrapperImpl(object);
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String propertyName = entry.getKey();
                Object value = entry.getValue();
                if (beanWrapper.isWritableProperty(propertyName)) {
                    beanWrapper.setPropertyValue(propertyName, value);
                }
            }
            return object;
        } catch (Exception e) {
            throw new RuntimeException("Map转对象失败: " + targetClass, e);
        }
    }

    // ==================== Bean验证 ====================

    /**
     * 检查对象的所有属性是否都为空
     *
     * @param object 对象
     * @return 是否所有属性都为空
     */
    public static boolean isAllPropertiesEmpty(Object object) {
        if (object == null) {
            return true;
        }
        Map<String, Object> propertyMap = getPropertyMap(object);
        for (Object value : propertyMap.values()) {
            if (value != null) {
                if (value instanceof CharSequence && StringUtil.isNotEmpty((CharSequence) value)) {
                    return false;
                } else if (value instanceof Collection && !((Collection<?>) value).isEmpty()) {
                    return false;
                } else if (value instanceof Map && !((Map<?, ?>) value).isEmpty()) {
                    return false;
                } else if (!(value instanceof CharSequence) && !(value instanceof Collection) && !(value instanceof Map)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 检查对象是否有任何属性不为空
     *
     * @param object 对象
     * @return 是否有任何属性不为空
     */
    public static boolean hasAnyPropertyNotEmpty(Object object) {
        return !isAllPropertiesEmpty(object);
    }

    // ==================== 反射相关方法 ====================

    /**
     * 获取对象的所有字段（包括父类）
     *
     * @param clazz 类
     * @return 字段列表
     */
    public static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> currentClass = clazz;
        while (currentClass != null && currentClass != Object.class) {
            fields.addAll(Arrays.asList(currentClass.getDeclaredFields()));
            currentClass = currentClass.getSuperclass();
        }
        return fields;
    }

    /**
     * 获取对象的字段值（通过反射）
     *
     * @param object    对象
     * @param fieldName 字段名
     * @return 字段值
     */
    public static Object getFieldValue(Object object, String fieldName) {
        if (object == null || StringUtil.isEmpty(fieldName)) {
            return null;
        }
        try {
            List<Field> fields = getAllFields(object.getClass());
            for (Field field : fields) {
                if (field.getName().equals(fieldName)) {
                    field.setAccessible(true);
                    return field.get(object);
                }
            }
            return null;
        } catch (IllegalAccessException e) {
            throw new RuntimeException("获取字段值失败: " + fieldName, e);
        }
    }

    /**
     * 设置对象的字段值（通过反射）
     *
     * @param object    对象
     * @param fieldName 字段名
     * @param value     字段值
     */
    public static void setFieldValue(Object object, String fieldName, Object value) {
        if (object == null || StringUtil.isEmpty(fieldName)) {
            return;
        }
        try {
            List<Field> fields = getAllFields(object.getClass());
            for (Field field : fields) {
                if (field.getName().equals(fieldName)) {
                    field.setAccessible(true);
                    field.set(object, value);
                    return;
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("设置字段值失败: " + fieldName, e);
        }
    }

    /**
     * 比较两个对象的属性值差异
     *
     * @param oldObject 旧对象
     * @param newObject 新对象
     * @return 差异Map（key: 属性名, value: [旧值, 新值]）
     */
    public static Map<String, Object[]> compareProperties(Object oldObject, Object newObject) {
        Map<String, Object[]> differences = new HashMap<>();
        if (oldObject == null || newObject == null) {
            return differences;
        }
        if (!oldObject.getClass().equals(newObject.getClass())) {
            throw new IllegalArgumentException("两个对象的类型必须相同");
        }

        Map<String, Object> oldProperties = getPropertyMap(oldObject);
        Map<String, Object> newProperties = getPropertyMap(newObject);

        for (String propertyName : oldProperties.keySet()) {
            Object oldValue = oldProperties.get(propertyName);
            Object newValue = newProperties.get(propertyName);
            if (!Objects.equals(oldValue, newValue)) {
                differences.put(propertyName, new Object[]{oldValue, newValue});
            }
        }

        return differences;
    }
}
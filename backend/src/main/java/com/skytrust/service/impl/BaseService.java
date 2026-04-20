package com.skytrust.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.skytrust.common.utils.StringUtil;
import com.skytrust.service.IService;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

/**
 * 基础Service实现类
 *
 * @param <M> Mapper类型
 * @param <T> 实体类型
 * @author SkyTrust Team
 */
public abstract class BaseService<M extends com.baomidou.mybatisplus.core.mapper.BaseMapper<T>, T>
        extends ServiceImpl<M, T> implements IService<T> {

    /**
     * 获取查询条件包装器
     *
     * @param entity 查询条件实体
     * @return QueryWrapper
     */
    protected QueryWrapper<T> getLambdaQueryWrapper(T entity) {
        return new QueryWrapper<>(entity);
    }

    /**
     * 获取分页对象
     *
     * @param page 当前页
     * @param size 每页大小
     * @return Page对象
     */
    protected Page<T> getPage(Integer page, Integer size) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1) {
            size = 10;
        }
        if (size > 100) {
            size = 100; // 限制最大分页大小
        }
        return new Page<>(page, size);
    }

    /**
     * 应用排序条件
     *
     * @param wrapper 查询包装器
     * @param orderBy 排序字段
     */
    protected void applyOrderBy(Wrapper<T> wrapper, String orderBy) {
        if (StringUtil.isNotEmpty(orderBy)) {
            String[] orderParts = orderBy.trim().split("\\s+");
            if (orderParts.length == 2) {
                String field = orderParts[0];
                String direction = orderParts[1].toLowerCase();

                // 转换为QueryWrapper以使用字符串排序方法
                // LambdaQueryWrapper也是QueryWrapper的子类，所以可以直接转换
                if (wrapper instanceof QueryWrapper) {
                    QueryWrapper<T> queryWrapper = (QueryWrapper<T>) wrapper;
                    if ("asc".equals(direction)) {
                        queryWrapper.orderByAsc(getColumn(field));
                    } else if ("desc".equals(direction)) {
                        queryWrapper.orderByDesc(getColumn(field));
                    }
                }
            }
        }
    }

    /**
     * 获取字段对应的列名（简单实现）
     *
     * @param field 字段名
     * @return 列名（这里简单返回字段名，实际需要转换驼峰为下划线）
     */
    protected String getColumn(String field) {
        // 简单驼峰转下划线（不完全准确，实际项目中应使用MyBatis-Plus的注解）
        return field.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    @Override
    public T getById(Serializable id) {
        return super.getById(id);
    }

    @Override
    public List<T> listByIds(Collection<? extends Serializable> idList) {
        return super.listByIds(idList);
    }

    @Override
    public List<T> listAll() {
        return super.list();
    }

    @Override
    public IPage<T> page(Integer page, Integer size, T entity, String orderBy) {
        Page<T> pageObj = getPage(page, size);
        QueryWrapper<T> wrapper = getLambdaQueryWrapper(entity);
        applyOrderBy(wrapper, orderBy);
        return super.page(pageObj, wrapper);
    }

    @Override
    public boolean save(T entity) {
        // 验证实体
        String validateResult = validate(entity);
        if (validateResult != null) {
            throw new IllegalArgumentException(validateResult);
        }
        return super.save(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveBatch(Collection<T> entityList) {
        for (T entity : entityList) {
            String validateResult = validate(entity);
            if (validateResult != null) {
                throw new IllegalArgumentException(validateResult);
            }
        }
        return super.saveBatch(entityList);
    }

    @Override
    public boolean updateById(T entity) {
        String validateResult = validate(entity);
        if (validateResult != null) {
            throw new IllegalArgumentException(validateResult);
        }
        return super.updateById(entity);
    }

    @Override
    public boolean removeById(Serializable id) {
        return super.removeById(id);
    }

    @Override
    public boolean removeByIds(Collection<?> idList) {
        return super.removeByIds(idList);
    }

    @Override
    public boolean logicRemoveById(Serializable id) {
        // 逻辑删除，需要实体有deleted字段
        try {
            T entity = getById(id);
            if (entity == null) {
                return false;
            }

            // 尝试设置deleted字段
            Class<?> clazz = entity.getClass();
            try {
                Field deletedField = clazz.getDeclaredField("deleted");
                deletedField.setAccessible(true);
                deletedField.set(entity, 1);
                return updateById(entity);
            } catch (NoSuchFieldException e) {
                // 如果没有deleted字段，执行物理删除
                return removeById(id);
            }
        } catch (Exception e) {
            throw new RuntimeException("逻辑删除失败", e);
        }
    }

    @Override
    public boolean logicRemoveByIds(Collection<? extends Serializable> idList) {
        boolean success = true;
        for (Serializable id : idList) {
            if (!logicRemoveById(id)) {
                success = false;
            }
        }
        return success;
    }

    @Override
    public boolean exists(T entity) {
        return super.count(getLambdaQueryWrapper(entity)) > 0;
    }

    @Override
    public long count(T entity) {
        return super.count(getLambdaQueryWrapper(entity));
    }

    @Override
    public String validate(T entity) {
        // 基础验证，子类可以覆盖此方法添加具体的验证逻辑
        if (entity == null) {
            return "实体不能为空";
        }
        return null;
    }
}
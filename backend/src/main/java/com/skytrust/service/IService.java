package com.skytrust.service;

import com.baomidou.mybatisplus.core.metadata.IPage;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * 基础Service接口
 * 扩展MyBatis-Plus的IService，添加业务常用方法
 *
 * @param <T> 实体类型
 * @author SkyTrust Team
 */
public interface IService<T> extends com.baomidou.mybatisplus.extension.service.IService<T> {

    /**
     * 查询所有
     *
     * @return 所有实体列表
     */
    List<T> listAll();

    /**
     * 分页查询
     *
     * @param page     当前页
     * @param size     每页大小
     * @param entity   查询条件实体
     * @param orderBy  排序字段（格式：字段名 asc/desc）
     * @return 分页结果
     */
    IPage<T> page(Integer page, Integer size, T entity, String orderBy);

    /**
     * 逻辑删除（软删除）
     *
     * @param id ID
     * @return 是否成功
     */
    boolean logicRemoveById(Serializable id);

    /**
     * 逻辑批量删除
     *
     * @param idList ID列表
     * @return 是否成功
     */
    boolean logicRemoveByIds(Collection<? extends Serializable> idList);

    /**
     * 检查实体是否存在
     *
     * @param entity 实体条件
     * @return 是否存在
     */
    boolean exists(T entity);

    /**
     * 根据条件统计数量
     *
     * @param entity 查询条件
     * @return 数量
     */
    long count(T entity);

    /**
     * 验证实体数据
     *
     * @param entity 实体
     * @return 验证结果，如果通过则返回null，否则返回错误信息
     */
    String validate(T entity);
}
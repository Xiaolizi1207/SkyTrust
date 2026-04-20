package com.skytrust.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.skytrust.entity.Role;

import java.util.List;

/**
 * 角色服务接口
 *
 * @author SkyTrust Team
 */
public interface RoleService extends com.baomidou.mybatisplus.extension.service.IService<Role> {

    /**
     * 创建角色
     *
     * @param role 角色实体
     * @return 是否成功
     */
    boolean createRole(Role role);

    /**
     * 更新角色
     *
     * @param role 角色实体
     * @return 是否成功
     */
    boolean updateRole(Role role);

    /**
     * 删除角色（逻辑删除）
     *
     * @param id 角色ID
     * @return 是否成功
     */
    boolean deleteRole(Long id);

    /**
     * 批量删除角色
     *
     * @param ids 角色ID列表
     * @return 是否成功
     */
    boolean batchDeleteRoles(List<Long> ids);

    /**
     * 根据ID获取角色
     *
     * @param id 角色ID
     * @return 角色实体
     */
    Role getRoleById(Long id);

    /**
     * 根据角色代码获取角色
     *
     * @param roleCode 角色代码
     * @return 角色实体
     */
    Role getRoleByCode(String roleCode);

    /**
     * 获取角色列表（分页）
     *
     * @param page       页码
     * @param size       每页大小
     * @param roleCode   角色代码（模糊查询）
     * @param roleName   角色名称（模糊查询）
     * @param status     状态
     * @param orderBy    排序字段
     * @return 分页结果
     */
    IPage<Role> getRoleList(Integer page, Integer size,
                            String roleCode, String roleName,
                            Integer status, String orderBy);

    /**
     * 获取所有启用的角色列表
     *
     * @return 角色列表
     */
    List<Role> getAllEnabledRoles();

    /**
     * 检查角色代码是否已存在
     *
     * @param roleCode 角色代码
     * @return 是否存在
     */
    boolean isRoleCodeExists(String roleCode);

    /**
     * 更新角色状态
     *
     * @param id     角色ID
     * @param status 状态（0-禁用，1-启用）
     * @return 是否成功
     */
    boolean updateRoleStatus(Long id, Integer status);

    /**
     * 批量更新角色状态
     *
     * @param ids    角色ID列表
     * @param status 状态（0-禁用，1-启用）
     * @return 是否成功
     */
    boolean batchUpdateRoleStatus(List<Long> ids, Integer status);
}
package com.skytrust.service;

import com.skytrust.entity.UserRole;

import java.util.List;

/**
 * 用户角色关联服务接口
 *
 * @author SkyTrust Team
 */
public interface UserRoleService extends com.baomidou.mybatisplus.extension.service.IService<UserRole> {

    /**
     * 分配角色给用户
     *
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 是否成功
     */
    boolean assignRoleToUser(Long userId, Long roleId);

    /**
     * 批量分配角色给用户
     *
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     * @return 是否成功
     */
    boolean batchAssignRolesToUser(Long userId, List<Long> roleIds);

    /**
     * 移除用户的角色
     *
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 是否成功
     */
    boolean removeRoleFromUser(Long userId, Long roleId);

    /**
     * 移除用户的所有角色
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean removeAllRolesFromUser(Long userId);

    /**
     * 获取用户的角色ID列表
     *
     * @param userId 用户ID
     * @return 角色ID列表
     */
    List<Long> getRoleIdsByUserId(Long userId);

    /**
     * 获取用户的角色代码列表
     *
     * @param userId 用户ID
     * @return 角色代码列表
     */
    List<String> getRoleCodesByUserId(Long userId);

    /**
     * 检查用户是否拥有指定角色
     *
     * @param userId 用户ID
     * @param roleCode 角色代码
     * @return 是否拥有
     */
    boolean hasRole(Long userId, String roleCode);

    /**
     * 检查用户是否拥有任意指定角色
     *
     * @param userId 用户ID
     * @param roleCodes 角色代码列表
     * @return 是否拥有任意角色
     */
    boolean hasAnyRole(Long userId, List<String> roleCodes);

    /**
     * 根据角色ID获取用户ID列表
     *
     * @param roleId 角色ID
     * @return 用户ID列表
     */
    List<Long> getUserIdsByRoleId(Long roleId);
}
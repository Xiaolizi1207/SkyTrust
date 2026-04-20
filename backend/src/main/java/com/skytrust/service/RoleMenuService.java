package com.skytrust.service;

import com.skytrust.entity.RoleMenu;

import java.util.List;

/**
 * 角色菜单关联服务接口
 *
 * @author SkyTrust Team
 */
public interface RoleMenuService extends com.baomidou.mybatisplus.extension.service.IService<RoleMenu> {

    /**
     * 分配菜单给角色
     *
     * @param roleId 角色ID
     * @param menuId 菜单ID
     * @return 是否成功
     */
    boolean assignMenuToRole(Long roleId, Long menuId);

    /**
     * 批量分配菜单给角色
     *
     * @param roleId 角色ID
     * @param menuIds 菜单ID列表
     * @return 是否成功
     */
    boolean batchAssignMenusToRole(Long roleId, List<Long> menuIds);

    /**
     * 移除角色的菜单
     *
     * @param roleId 角色ID
     * @param menuId 菜单ID
     * @return 是否成功
     */
    boolean removeMenuFromRole(Long roleId, Long menuId);

    /**
     * 移除角色的所有菜单
     *
     * @param roleId 角色ID
     * @return 是否成功
     */
    boolean removeAllMenusFromRole(Long roleId);

    /**
     * 获取角色的菜单ID列表
     *
     * @param roleId 角色ID
     * @return 菜单ID列表
     */
    List<Long> getMenuIdsByRoleId(Long roleId);

    /**
     * 获取角色的权限标识列表
     *
     * @param roleId 角色ID
     * @return 权限标识列表
     */
    List<String> getPermsByRoleId(Long roleId);

    /**
     * 获取用户有权限的菜单ID列表（通过用户角色）
     *
     * @param userId 用户ID
     * @return 菜单ID列表
     */
    List<Long> getMenuIdsByUserId(Long userId);

    /**
     * 获取用户有权限的权限标识列表（通过用户角色）
     *
     * @param userId 用户ID
     * @return 权限标识列表
     */
    List<String> getPermsByUserId(Long userId);

    /**
     * 检查角色是否拥有指定菜单权限
     *
     * @param roleId 角色ID
     * @param perms 权限标识
     * @return 是否拥有
     */
    boolean hasPermissionByRoleId(Long roleId, String perms);

    /**
     * 检查用户是否拥有指定权限标识
     *
     * @param userId 用户ID
     * @param perms 权限标识
     * @return 是否拥有
     */
    boolean hasPermissionByUserId(Long userId, String perms);
}
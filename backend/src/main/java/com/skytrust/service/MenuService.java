package com.skytrust.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.skytrust.entity.Menu;

import java.util.List;

/**
 * 菜单服务接口
 *
 * @author SkyTrust Team
 */
public interface MenuService extends com.baomidou.mybatisplus.extension.service.IService<Menu> {

    /**
     * 创建菜单
     *
     * @param menu 菜单实体
     * @return 是否成功
     */
    boolean createMenu(Menu menu);

    /**
     * 更新菜单
     *
     * @param menu 菜单实体
     * @return 是否成功
     */
    boolean updateMenu(Menu menu);

    /**
     * 删除菜单（逻辑删除）
     *
     * @param id 菜单ID
     * @return 是否成功
     */
    boolean deleteMenu(Long id);

    /**
     * 批量删除菜单
     *
     * @param ids 菜单ID列表
     * @return 是否成功
     */
    boolean batchDeleteMenus(List<Long> ids);

    /**
     * 根据ID获取菜单
     *
     * @param id 菜单ID
     * @return 菜单实体
     */
    Menu getMenuById(Long id);

    /**
     * 根据菜单代码获取菜单
     *
     * @param menuCode 菜单代码
     * @return 菜单实体
     */
    Menu getMenuByCode(String menuCode);

    /**
     * 获取菜单列表（分页）
     *
     * @param page       页码
     * @param size       每页大小
     * @param parentId   父菜单ID
     * @param menuName   菜单名称（模糊查询）
     * @param menuCode   菜单代码（模糊查询）
     * @param menuType   菜单类型
     * @param status     状态
     * @param orderBy    排序字段
     * @return 分页结果
     */
    IPage<Menu> getMenuList(Integer page, Integer size,
                            Long parentId, String menuName, String menuCode,
                            Integer menuType, Integer status, String orderBy);

    /**
     * 获取所有启用的菜单列表
     *
     * @return 菜单列表
     */
    List<Menu> getAllEnabledMenus();

    /**
     * 获取菜单树形列表（根据父ID）
     *
     * @param parentId 父菜单ID（0表示根菜单）
     * @return 菜单树形列表
     */
    List<Menu> getMenuTreeByParentId(Long parentId);

    /**
     * 获取用户有权限的菜单树形列表
     *
     * @param userId 用户ID
     * @return 菜单树形列表
     */
    List<Menu> getMenuTreeByUserId(Long userId);

    /**
     * 检查菜单代码是否已存在
     *
     * @param menuCode 菜单代码
     * @return 是否存在
     */
    boolean isMenuCodeExists(String menuCode);

    /**
     * 检查权限标识是否已存在
     *
     * @param perms 权限标识
     * @return 是否存在
     */
    boolean isPermsExists(String perms);

    /**
     * 更新菜单状态
     *
     * @param id     菜单ID
     * @param status 状态（0-禁用，1-启用）
     * @return 是否成功
     */
    boolean updateMenuStatus(Long id, Integer status);

    /**
     * 批量更新菜单状态
     *
     * @param ids    菜单ID列表
     * @param status 状态（0-禁用，1-启用）
     * @return 是否成功
     */
    boolean batchUpdateMenuStatus(List<Long> ids, Integer status);

    /**
     * 更新菜单排序号
     *
     * @param menuId  菜单ID
     * @param newOrder 新的排序号
     * @return 是否成功
     */
    boolean updateMenuOrder(Long menuId, Integer newOrder);

    /**
     * 交换两个菜单的排序号
     *
     * @param menuId1 第一个菜单ID
     * @param menuId2 第二个菜单ID
     * @return 是否成功
     */
    boolean swapMenuOrder(Long menuId1, Long menuId2);

    /**
     * 批量导入菜单
     *
     * @param menus 菜单列表
     * @return 是否成功
     */
    boolean batchImportMenus(List<Menu> menus);

    /**
     * 导出菜单
     *
     * @param menuIds 菜单ID列表
     * @return 菜单列表
     */
    List<Menu> exportMenus(List<Long> menuIds);

    /**
     * 生成通用前端路由配置
     *
     * @param parentId 父菜单ID（0表示根菜单）
     * @return 路由配置JSON字符串
     */
    String generateFrontendRoutes(Long parentId);

    /**
     * 生成Vue.js路由配置
     *
     * @param parentId 父菜单ID（0表示根菜单）
     * @return Vue路由配置JSON字符串
     */
    String generateVueRoutes(Long parentId);

    /**
     * 生成React路由配置
     *
     * @param parentId 父菜单ID（0表示根菜单）
     * @return React路由配置JSON字符串
     */
    String generateReactRoutes(Long parentId);
}
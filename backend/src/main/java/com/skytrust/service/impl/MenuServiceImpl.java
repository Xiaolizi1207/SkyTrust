package com.skytrust.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.skytrust.common.utils.StringUtil;
import com.skytrust.entity.Menu;
import com.skytrust.enums.MenuTypeEnum;
import com.skytrust.enums.MenuStatusEnum;
import com.skytrust.mapper.MenuMapper;
import com.skytrust.service.MenuService;
import com.skytrust.service.RoleMenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.annotation.Lazy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 菜单服务实现类
 *
 * @author SkyTrust Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MenuServiceImpl extends BaseService<MenuMapper, Menu> implements MenuService {
    private final MenuMapper menuMapper;
    private final RoleMenuService roleMenuService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"menuCache", "menuTreeCache", "userMenuCache"}, allEntries = true)
    public boolean createMenu(Menu menu) {
        log.info("创建菜单: menuCode={}, menuName={}", menu.getMenuCode(), menu.getMenuName());

        // 检查菜单代码是否已存在
        if (isMenuCodeExists(menu.getMenuCode())) {
            throw new IllegalArgumentException("菜单代码已存在: " + menu.getMenuCode());
        }

        // 如果权限标识不为空，检查是否已存在
        if (StringUtil.isNotEmpty(menu.getPerms()) && isPermsExists(menu.getPerms())) {
            throw new IllegalArgumentException("权限标识已存在: " + menu.getPerms());
        }

        // 设置创建时间和更新时间
        menu.setCreateTime(LocalDateTime.now());
        menu.setUpdateTime(LocalDateTime.now());
        menu.setDeleted(false);

        return menuMapper.insert(menu) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"menuCache", "menuTreeCache", "userMenuCache"}, allEntries = true)
    public boolean updateMenu(Menu menu) {
        log.info("更新菜单: id={}, menuCode={}", menu.getId(), menu.getMenuCode());

        Menu existingMenu = menuMapper.selectById(menu.getId());
        if (existingMenu == null) {
            throw new IllegalArgumentException("菜单不存在: id=" + menu.getId());
        }

        // 如果菜单代码有变化，检查新代码是否已存在
        if (!existingMenu.getMenuCode().equals(menu.getMenuCode())) {
            if (isMenuCodeExists(menu.getMenuCode())) {
                throw new IllegalArgumentException("菜单代码已存在: " + menu.getMenuCode());
            }
        }

        // 如果权限标识有变化，检查新权限标识是否已存在
        if (StringUtil.isNotEmpty(menu.getPerms()) &&
            !menu.getPerms().equals(existingMenu.getPerms())) {
            if (isPermsExists(menu.getPerms())) {
                throw new IllegalArgumentException("权限标识已存在: " + menu.getPerms());
            }
        }

        menu.setUpdateTime(LocalDateTime.now());

        return menuMapper.updateById(menu) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"menuCache", "menuTreeCache", "userMenuCache"}, allEntries = true)
    public boolean deleteMenu(Long id) {
        log.info("删除菜单: id={}", id);

        Menu menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new IllegalArgumentException("菜单不存在: id=" + id);
        }

        // 检查是否有子菜单，如果有则不允许删除
        LambdaQueryWrapper<Menu> childrenWrapper = new LambdaQueryWrapper<>();
        childrenWrapper.eq(Menu::getParentId, id);
        childrenWrapper.eq(Menu::getDeleted, false);
        long childrenCount = menuMapper.selectCount(childrenWrapper);
        if (childrenCount > 0) {
            throw new IllegalArgumentException("存在子菜单，无法删除");
        }

        // 检查是否有角色关联该菜单，如果有则不允许删除
        LambdaQueryWrapper<com.skytrust.entity.RoleMenu> roleMenuWrapper = new LambdaQueryWrapper<>();
        roleMenuWrapper.eq(com.skytrust.entity.RoleMenu::getMenuId, id);
        roleMenuWrapper.eq(com.skytrust.entity.RoleMenu::getDeleted, false);
        long roleMenuCount = roleMenuService.count(roleMenuWrapper);
        if (roleMenuCount > 0) {
            throw new IllegalArgumentException("菜单已关联角色，无法删除");
        }

        // 逻辑删除
        menu.setDeleted(true);
        menu.setUpdateTime(LocalDateTime.now());

        return menuMapper.updateById(menu) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDeleteMenus(List<Long> ids) {
        log.info("批量删除菜单: ids={}", ids);

        boolean success = true;
        for (Long id : ids) {
            try {
                if (!deleteMenu(id)) {
                    success = false;
                }
            } catch (Exception e) {
                log.error("删除菜单失败: id={}", id, e);
                success = false;
            }
        }
        return success;
    }

    @Override
    public Menu getMenuById(Long id) {
        log.debug("根据ID获取菜单: id={}", id);
        return menuMapper.selectById(id);
    }

    @Override
    public Menu getMenuByCode(String menuCode) {
        log.debug("根据菜单代码获取菜单: menuCode={}", menuCode);

        LambdaQueryWrapper<Menu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Menu::getMenuCode, menuCode);
        wrapper.eq(Menu::getDeleted, false);
        return menuMapper.selectOne(wrapper);
    }

    @Override
    public IPage<Menu> getMenuList(Integer page, Integer size,
                                  Long parentId, String menuName, String menuCode,
                                  Integer menuType, Integer status, String orderBy) {
        log.debug("获取菜单列表: page={}, size={}, parentId={}, menuName={}, menuCode={}, menuType={}, status={}",
                page, size, parentId, menuName, menuCode, menuType, status);

        Page<Menu> pageObj = getPage(page, size);
        LambdaQueryWrapper<Menu> wrapper = new LambdaQueryWrapper<>();

        // 构建查询条件
        if (parentId != null) {
            wrapper.eq(Menu::getParentId, parentId);
        }
        if (StringUtil.isNotEmpty(menuName)) {
            wrapper.like(Menu::getMenuName, menuName);
        }
        if (StringUtil.isNotEmpty(menuCode)) {
            wrapper.like(Menu::getMenuCode, menuCode);
        }
        if (menuType != null) {
            wrapper.eq(Menu::getMenuType, menuType);
        }
        if (status != null) {
            wrapper.eq(Menu::getStatus, status);
        }
        wrapper.eq(Menu::getDeleted, false);

        // 应用排序
        applyOrderBy(wrapper, orderBy);

        return menuMapper.selectPage(pageObj, wrapper);
    }

    @Override
    @Cacheable(value = "menuCache")
    public List<Menu> getAllEnabledMenus() {
        log.debug("获取所有启用的菜单列表");

        LambdaQueryWrapper<Menu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Menu::getStatus, MenuStatusEnum.ENABLED.getCode());
        wrapper.eq(Menu::getDeleted, false);
        wrapper.orderByAsc(Menu::getSortOrder);

        return menuMapper.selectList(wrapper);
    }

    @Override
    @Cacheable(value = "menuTreeCache", key = "#parentId")
    public List<Menu> getMenuTreeByParentId(Long parentId) {
        log.debug("获取菜单树形列表: parentId={}", parentId);

        // 获取所有启用的菜单
        List<Menu> allMenus = getAllEnabledMenus();

        // 构建树形结构
        return buildMenuTree(allMenus, parentId);
    }

    @Override
    @Cacheable(value = "userMenuCache", key = "#userId")
    public List<Menu> getMenuTreeByUserId(Long userId) {
        log.debug("获取用户有权限的菜单树形列表: userId={}", userId);

        // 获取用户有权限的菜单ID列表
        List<Long> menuIds = roleMenuService.getMenuIdsByUserId(userId);
        if (menuIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 根据菜单ID列表查询启用的菜单
        LambdaQueryWrapper<Menu> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Menu::getId, menuIds);
        wrapper.eq(Menu::getStatus, MenuStatusEnum.ENABLED.getCode());
        wrapper.eq(Menu::getDeleted, false);
        wrapper.orderByAsc(Menu::getSortOrder);

        List<Menu> userMenus = menuMapper.selectList(wrapper);

        // 构建树形结构（从根开始）
        return buildMenuTree(userMenus, 0L);
    }

    @Override
    public boolean isMenuCodeExists(String menuCode) {
        if (StringUtil.isEmpty(menuCode)) {
            return false;
        }

        LambdaQueryWrapper<Menu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Menu::getMenuCode, menuCode);
        wrapper.eq(Menu::getDeleted, false);
        return menuMapper.selectCount(wrapper) > 0;
    }

    @Override
    public boolean isPermsExists(String perms) {
        if (StringUtil.isEmpty(perms)) {
            return false;
        }

        LambdaQueryWrapper<Menu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Menu::getPerms, perms);
        wrapper.eq(Menu::getDeleted, false);
        return menuMapper.selectCount(wrapper) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"menuCache", "menuTreeCache", "userMenuCache"}, allEntries = true)
    public boolean updateMenuStatus(Long id, Integer status) {
        log.info("更新菜单状态: id={}, status={}", id, status);

        Menu menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new IllegalArgumentException("菜单不存在: id=" + id);
        }

        menu.setStatus(status);
        menu.setUpdateTime(LocalDateTime.now());

        return menuMapper.updateById(menu) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"menuCache", "menuTreeCache", "userMenuCache"}, allEntries = true)
    public boolean batchUpdateMenuStatus(List<Long> ids, Integer status) {
        log.info("批量更新菜单状态: ids={}, status={}", ids, status);

        boolean success = true;
        for (Long id : ids) {
            try {
                if (!updateMenuStatus(id, status)) {
                    success = false;
                }
            } catch (Exception e) {
                log.error("更新菜单状态失败: id={}", id, e);
                success = false;
            }
        }
        return success;
    }

    @Override
    public String validate(Menu menu) {
        // 调用父类的基础验证
        String baseValidation = super.validate(menu);
        if (baseValidation != null) {
            return baseValidation;
        }

        // 验证菜单名称
        if (StringUtil.isEmpty(menu.getMenuName())) {
            return "菜单名称不能为空";
        }

        // 验证菜单代码
        if (StringUtil.isEmpty(menu.getMenuCode())) {
            return "菜单代码不能为空";
        }

        // 验证菜单类型
        if (menu.getMenuType() == null || !MenuTypeEnum.isValid(menu.getMenuType())) {
            return "菜单类型无效，必须是1（目录）、2（菜单）或3（按钮）";
        }

        // 验证状态
        if (menu.getStatus() != null && !MenuStatusEnum.isValid(menu.getStatus())) {
            return "状态值无效，必须是0（禁用）或1（启用）";
        }

        return null;
    }

    /**
     * 构建菜单树形结构
     *
     * @param menus    所有菜单列表
     * @param parentId 父菜单ID
     * @return 树形菜单列表
     */
    private List<Menu> buildMenuTree(List<Menu> menus, Long parentId) {
        // 按父ID分组（只计算一次）
        Map<Long, List<Menu>> menuMap = menus.stream()
                .collect(Collectors.groupingBy(Menu::getParentId));

        // 调用递归方法构建树
        return buildMenuTree(menuMap, parentId);
    }

    /**
     * 构建菜单树形结构（递归实现）
     *
     * @param menuMap  按父ID分组的菜单映射
     * @param parentId 父菜单ID
     * @return 树形菜单列表
     */
    private List<Menu> buildMenuTree(Map<Long, List<Menu>> menuMap, Long parentId) {
        List<Menu> tree = new ArrayList<>();

        // 递归构建树
        if (menuMap.containsKey(parentId)) {
            for (Menu menu : menuMap.get(parentId)) {
                // 设置子菜单
                menu.setChildren(buildMenuTree(menuMap, menu.getId()));
                tree.add(menu);
            }
        }

        return tree;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"menuCache", "menuTreeCache", "userMenuCache"}, allEntries = true)
    public boolean updateMenuOrder(Long menuId, Integer newOrder) {
        log.info("更新菜单排序号: menuId={}, newOrder={}", menuId, newOrder);

        Menu menu = menuMapper.selectById(menuId);
        if (menu == null) {
            throw new IllegalArgumentException("菜单不存在: id=" + menuId);
        }

        menu.setSortOrder(newOrder);
        menu.setUpdateTime(LocalDateTime.now());

        return menuMapper.updateById(menu) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"menuCache", "menuTreeCache", "userMenuCache"}, allEntries = true)
    public boolean swapMenuOrder(Long menuId1, Long menuId2) {
        log.info("交换菜单排序号: menuId1={}, menuId2={}", menuId1, menuId2);

        Menu menu1 = menuMapper.selectById(menuId1);
        Menu menu2 = menuMapper.selectById(menuId2);

        if (menu1 == null) {
            throw new IllegalArgumentException("菜单不存在: id=" + menuId1);
        }
        if (menu2 == null) {
            throw new IllegalArgumentException("菜单不存在: id=" + menuId2);
        }

        Integer tempOrder = menu1.getSortOrder();
        menu1.setSortOrder(menu2.getSortOrder());
        menu2.setSortOrder(tempOrder);

        menu1.setUpdateTime(LocalDateTime.now());
        menu2.setUpdateTime(LocalDateTime.now());

        boolean success1 = menuMapper.updateById(menu1) > 0;
        boolean success2 = menuMapper.updateById(menu2) > 0;

        return success1 && success2;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"menuCache", "menuTreeCache", "userMenuCache"}, allEntries = true)
    public boolean batchImportMenus(List<Menu> menus) {
        log.info("批量导入菜单: 数量={}", menus.size());

        if (menus == null || menus.isEmpty()) {
            throw new IllegalArgumentException("菜单列表不能为空");
        }

        boolean success = true;
        for (Menu menu : menus) {
            try {
                // 验证菜单数据
                String validationError = validate(menu);
                if (validationError != null) {
                    log.error("菜单验证失败: {}", validationError);
                    success = false;
                    continue;
                }

                // 检查菜单代码是否已存在
                if (isMenuCodeExists(menu.getMenuCode())) {
                    log.error("菜单代码已存在: {}", menu.getMenuCode());
                    success = false;
                    continue;
                }

                // 设置创建时间和更新时间
                menu.setCreateTime(LocalDateTime.now());
                menu.setUpdateTime(LocalDateTime.now());
                menu.setDeleted(false);

                if (menuMapper.insert(menu) <= 0) {
                    log.error("插入菜单失败: menuCode={}", menu.getMenuCode());
                    success = false;
                }
            } catch (Exception e) {
                log.error("导入菜单异常: menuCode={}", menu.getMenuCode(), e);
                success = false;
            }
        }

        return success;
    }

    @Override
    public List<Menu> exportMenus(List<Long> menuIds) {
        log.info("导出菜单: menuIds={}", menuIds);

        if (menuIds == null || menuIds.isEmpty()) {
            throw new IllegalArgumentException("菜单ID列表不能为空");
        }

        LambdaQueryWrapper<Menu> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Menu::getId, menuIds);
        wrapper.eq(Menu::getDeleted, false);
        wrapper.orderByAsc(Menu::getSortOrder);

        return menuMapper.selectList(wrapper);
    }

    @Override
    public String generateFrontendRoutes(Long parentId) {
        log.info("生成通用前端路由配置: parentId={}", parentId);

        List<Menu> menuTree = getMenuTreeByParentId(parentId);
        return generateRouteJson(menuTree, "frontend");
    }

    @Override
    public String generateVueRoutes(Long parentId) {
        log.info("生成Vue.js路由配置: parentId={}", parentId);

        List<Menu> menuTree = getMenuTreeByParentId(parentId);
        return generateRouteJson(menuTree, "vue");
    }

    @Override
    public String generateReactRoutes(Long parentId) {
        log.info("生成React路由配置: parentId={}", parentId);

        List<Menu> menuTree = getMenuTreeByParentId(parentId);
        return generateRouteJson(menuTree, "react");
    }

    /**
     * 生成路由配置JSON字符串
     *
     * @param menuTree 菜单树形结构
     * @param type     路由类型（frontend、vue、react）
     * @return JSON字符串
     */
    private String generateRouteJson(List<Menu> menuTree, String type) {
        if (menuTree == null || menuTree.isEmpty()) {
            return "[]";
        }

        List<Object> routes = new ArrayList<>();
        for (Menu menu : menuTree) {
            routes.add(convertMenuToRoute(menu, type));
        }

        try {
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(routes);
        } catch (Exception e) {
            log.error("生成路由配置JSON失败", e);
            return "[]";
        }
    }

    /**
     * 将菜单转换为路由对象
     *
     * @param menu 菜单对象
     * @param type 路由类型
     * @return 路由对象
     */
    private Object convertMenuToRoute(Menu menu, String type) {
        java.util.Map<String, Object> route = new java.util.HashMap<>();

        // 通用路由字段
        if (menu.getMenuPath() != null) {
            route.put("path", menu.getMenuPath());
        }
        if (menu.getComponent() != null) {
            route.put("component", menu.getComponent());
        }
        if (menu.getMenuName() != null) {
            route.put("name", menu.getMenuCode());
            route.put("meta", new java.util.HashMap<String, Object>() {{
                put("title", menu.getMenuName());
                if (menu.getIcon() != null) {
                    put("icon", menu.getIcon());
                }
                if (menu.getPerms() != null) {
                    put("permissions", new String[]{menu.getPerms()});
                }
                put("hidden", menu.getIsVisible() != null && menu.getIsVisible() == 0);
                put("cache", menu.getIsCache() != null && menu.getIsCache() == 1);
            }});
        }

        // 类型特定字段
        if ("vue".equals(type)) {
            route.put("redirect", "noRedirect");
            route.put("alwaysShow", menu.getMenuType() != null && menu.getMenuType() == MenuTypeEnum.DIRECTORY.getCode());
        } else if ("react".equals(type)) {
            route.put("exact", true);
        }

        // 递归处理子菜单
        if (menu.getChildren() != null && !menu.getChildren().isEmpty()) {
            List<Object> children = new ArrayList<>();
            for (Menu child : menu.getChildren()) {
                children.add(convertMenuToRoute(child, type));
            }
            route.put("children", children);
        }

        return route;
    }
}
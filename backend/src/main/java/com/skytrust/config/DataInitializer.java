package com.skytrust.config;

import com.skytrust.entity.Menu;
import com.skytrust.entity.Role;
import com.skytrust.entity.User;
import com.skytrust.enums.UserRoleEnum;
import com.skytrust.enums.MenuTypeEnum;
import com.skytrust.enums.MenuStatusEnum;
import com.skytrust.service.MenuService;
import com.skytrust.service.RoleService;
import com.skytrust.service.UserRoleService;
import com.skytrust.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据初始化器
 * 系统启动时初始化RBAC相关数据
 *
 * @author SkyTrust Team
 */
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleService roleService;
    private final MenuService menuService;
    private final UserService userService;
    private final UserRoleService userRoleService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void run(String... args) throws Exception {
        log.info("开始初始化RBAC系统数据...");

        // 1. 初始化角色数据
        initRoles();

        // 2. 初始化菜单数据
        initMenus();

        // 3. 迁移现有用户角色数据
        migrateUserRoles();

        log.info("RBAC系统数据初始化完成");
    }

    /**
     * 初始化角色数据
     */
    private void initRoles() {
        log.info("初始化角色数据...");

        for (UserRoleEnum roleEnum : UserRoleEnum.values()) {
            String roleCode = roleEnum.getCode();
            String roleName = roleEnum.getDescription();

            // 检查角色是否已存在
            if (roleService.isRoleCodeExists(roleCode)) {
                log.info("角色已存在: roleCode={}", roleCode);
                continue;
            }

            // 创建角色
            Role role = new Role();
            role.setRoleCode(roleCode);
            role.setRoleName(roleName);
            role.setDescription(roleName + "角色");
            role.setStatus(1);
            role.setSortOrder(0);

            boolean success = roleService.createRole(role);
            if (success) {
                log.info("创建角色成功: roleCode={}, roleName={}", roleCode, roleName);
            } else {
                log.error("创建角色失败: roleCode={}, roleName={}", roleCode, roleName);
            }
        }
    }

    /**
     * 初始化菜单数据
     */
    private void initMenus() {
        log.info("初始化菜单数据...");

        // 检查是否已存在菜单
        long menuCount = menuService.count();
        if (menuCount > 0) {
            log.info("菜单数据已存在，跳过初始化");
            return;
        }

        // 菜单定义列表：每个元素是一个Object数组，包含以下字段：
        // 0: 父菜单代码（如果为根菜单则为null）
        // 1: 菜单名称
        // 2: 菜单代码
        // 3: 菜单路径
        // 4: 组件路径
        // 5: 图标
        // 6: 菜单类型（1-目录，2-菜单，3-按钮）
        // 7: 状态（0-禁用，1-启用）
        // 8: 排序号
        // 9: 权限标识
        // 10: 是否外链（0-否，1-是）
        // 11: 外链地址
        // 12: 是否缓存（0-否，1-是）
        // 13: 是否可见（0-隐藏，1-显示）
        // 14: 备注
        // 菜单类型和状态枚举常量
        int DIRECTORY = MenuTypeEnum.DIRECTORY.getCode();
        int MENU = MenuTypeEnum.MENU.getCode();
        int BUTTON = MenuTypeEnum.BUTTON.getCode();
        int ENABLED = MenuStatusEnum.ENABLED.getCode();
        int DISABLED = MenuStatusEnum.DISABLED.getCode();

        List<Object[]> menuDefinitions = new ArrayList<>();

        // 根目录菜单（父菜单代码为null）
        menuDefinitions.add(new Object[]{null, "系统管理", "system", "/system", "Layout", "system", DIRECTORY, ENABLED, 0, "system", 0, "", 0, 1, ""});
        menuDefinitions.add(new Object[]{null, "设备管理", "device", "/device", "Layout", "device", DIRECTORY, ENABLED, 5, "device", 0, "", 0, 1, ""});
        menuDefinitions.add(new Object[]{null, "租赁管理", "rental", "/rental", "Layout", "order", DIRECTORY, ENABLED, 6, "rental", 0, "", 0, 1, ""});
        menuDefinitions.add(new Object[]{null, "个人中心", "profile", "/profile", "Layout", "user", DIRECTORY, ENABLED, 7, "profile", 0, "", 0, 1, ""});

        // 子菜单（父菜单代码为对应根目录的菜单代码）
        menuDefinitions.add(new Object[]{"system", "用户管理", "user", "/system/user", "/system/user/index", "user", MENU, ENABLED, 1, "system:user:list", 0, "", 0, 1, ""});
        menuDefinitions.add(new Object[]{"system", "角色管理", "role", "/system/role", "/system/role/index", "role", MENU, ENABLED, 2, "system:role:list", 0, "", 0, 1, ""});
        menuDefinitions.add(new Object[]{"system", "菜单管理", "menu", "/system/menu", "/system/menu/index", "menu", MENU, ENABLED, 3, "system:menu:list", 0, "", 0, 1, ""});
        menuDefinitions.add(new Object[]{"system", "字典管理", "dict", "/system/dict", "/system/dict/index", "dict", MENU, ENABLED, 4, "system:dict:list", 0, "", 0, 1, ""});

        // 第一轮：创建所有菜单，parentId临时为0，并记录菜单代码到ID的映射
        java.util.Map<String, Long> menuCodeToIdMap = new java.util.HashMap<>();
        List<Menu> createdMenus = new ArrayList<>();

        for (Object[] def : menuDefinitions) {
            String parentMenuCode = (String) def[0];
            String menuName = (String) def[1];
            String menuCode = (String) def[2];
            String menuPath = (String) def[3];
            String component = (String) def[4];
            String icon = (String) def[5];
            Integer menuType = (Integer) def[6];
            Integer status = (Integer) def[7];
            Integer sortOrder = (Integer) def[8];
            String perms = (String) def[9];
            Integer isExternal = (Integer) def[10];
            String externalUrl = (String) def[11];
            Integer isCache = (Integer) def[12];
            Integer isVisible = (Integer) def[13];
            String remark = (String) def[14];

            Menu menu = createMenu(0L, menuName, menuCode, menuPath, component, icon,
                    menuType, status, sortOrder, perms, isExternal, externalUrl,
                    isCache, isVisible, remark);

            boolean success = menuService.createMenu(menu);
            if (success) {
                // 重新查询菜单以获取生成的ID
                Menu createdMenu = menuService.getMenuByCode(menuCode);
                if (createdMenu != null) {
                    menuCodeToIdMap.put(menuCode, createdMenu.getId());
                    createdMenus.add(createdMenu);
                    log.info("创建菜单成功: menuCode={}, menuName={}, id={}", menuCode, menuName, createdMenu.getId());
                } else {
                    log.error("创建菜单后查询失败: menuCode={}", menuCode);
                }
            } else {
                log.error("创建菜单失败: menuCode={}, menuName={}", menuCode, menuName);
            }
        }

        // 第二轮：更新菜单的parentId关系
        int updatedCount = 0;
        for (Object[] def : menuDefinitions) {
            String parentMenuCode = (String) def[0];
            String menuCode = (String) def[2];

            if (parentMenuCode == null) {
                continue; // 根菜单，parentId已经是0
            }

            Long parentId = menuCodeToIdMap.get(parentMenuCode);
            Long menuId = menuCodeToIdMap.get(menuCode);

            if (parentId == null) {
                log.error("父菜单不存在: parentMenuCode={}", parentMenuCode);
                continue;
            }
            if (menuId == null) {
                log.error("菜单不存在: menuCode={}", menuCode);
                continue;
            }

            // 获取已创建的菜单对象，避免updateMenu中的唯一性检查异常
            Menu existingMenu = menuService.getMenuById(menuId);
            if (existingMenu == null) {
                log.error("菜单不存在: menuId={}", menuId);
                continue;
            }

            // 只更新parentId
            existingMenu.setParentId(parentId);
            existingMenu.setUpdateTime(java.time.LocalDateTime.now());

            boolean success = menuService.updateMenu(existingMenu);
            if (success) {
                updatedCount++;
                log.debug("更新菜单parentId成功: menuCode={}, parentId={}", menuCode, parentId);
            } else {
                log.error("更新菜单parentId失败: menuCode={}, parentId={}", menuCode, parentId);
            }
        }

        log.info("菜单数据初始化完成，共创建{}个菜单，更新{}个菜单的父子关系", createdMenus.size(), updatedCount);
    }

    /**
     * 迁移现有用户角色数据
     */
    private void migrateUserRoles() {
        log.info("迁移现有用户角色数据...");

        // 获取所有用户
        List<User> users = userService.list();
        if (users.isEmpty()) {
            log.info("没有用户数据需要迁移");
            return;
        }

        int migratedCount = 0;
        for (User user : users) {
            String roleCode = user.getRole();
            if (roleCode == null || roleCode.trim().isEmpty()) {
                log.warn("用户角色为空: userId={}, username={}", user.getId(), user.getUsername());
                continue;
            }

            // 根据角色代码查找角色
            Role role = roleService.getRoleByCode(roleCode);
            if (role == null) {
                log.error("用户角色不存在: userId={}, roleCode={}", user.getId(), roleCode);
                continue;
            }

            // 检查是否已分配
            List<Long> existingRoleIds = userRoleService.getRoleIdsByUserId(user.getId());
            if (existingRoleIds.contains(role.getId())) {
                log.debug("用户角色已存在: userId={}, roleId={}", user.getId(), role.getId());
                continue;
            }

            // 分配角色给用户
            try {
                boolean success = userRoleService.assignRoleToUser(user.getId(), role.getId());
                if (success) {
                    migratedCount++;
                    log.debug("用户角色迁移成功: userId={}, roleId={}", user.getId(), role.getId());
                } else {
                    log.error("用户角色迁移失败: userId={}, roleId={}", user.getId(), role.getId());
                }
            } catch (Exception e) {
                log.error("用户角色迁移异常: userId={}, roleId={}", user.getId(), role.getId(), e);
            }
        }

        log.info("用户角色数据迁移完成，共迁移{}个用户", migratedCount);
    }

    /**
     * 创建菜单对象（辅助方法）
     */
    private Menu createMenu(Long parentId, String menuName, String menuCode, String menuPath,
                           String component, String icon, Integer menuType, Integer status,
                           Integer sortOrder, String perms, Integer isExternal, String externalUrl,
                           Integer isCache, Integer isVisible, String remark) {
        Menu menu = new Menu();
        menu.setParentId(parentId);
        menu.setMenuName(menuName);
        menu.setMenuCode(menuCode);
        menu.setMenuPath(menuPath);
        menu.setComponent(component);
        menu.setIcon(icon);
        menu.setMenuType(menuType);
        menu.setStatus(status);
        menu.setSortOrder(sortOrder);
        menu.setPerms(perms);
        menu.setIsExternal(isExternal);
        menu.setExternalUrl(externalUrl);
        menu.setIsCache(isCache);
        menu.setIsVisible(isVisible);
        menu.setRemark(remark);
        menu.setCreateTime(LocalDateTime.now());
        menu.setUpdateTime(LocalDateTime.now());
        return menu;
    }
}
package com.skytrust.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.skytrust.entity.Menu;
import com.skytrust.entity.Role;
import com.skytrust.entity.RoleMenu;
import com.skytrust.mapper.RoleMenuMapper;
import com.skytrust.service.MenuService;
import com.skytrust.service.RoleMenuService;
import com.skytrust.service.RoleService;
import com.skytrust.service.UserRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Lazy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 角色菜单关联服务实现类
 *
 * @author SkyTrust Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleMenuServiceImpl extends BaseService<RoleMenuMapper, RoleMenu> implements RoleMenuService {


    private final RoleMenuMapper roleMenuMapper;
    private final RoleService roleService;
    @Autowired
    @Lazy
    private MenuService menuService;
    private final UserRoleService userRoleService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignMenuToRole(Long roleId, Long menuId) {
        log.info("分配菜单给角色: roleId={}, menuId={}", roleId, menuId);

        // 检查角色是否存在
        Role role = roleService.getById(roleId);
        if (role == null) {
            throw new IllegalArgumentException("角色不存在: id=" + roleId);
        }

        // 检查菜单是否存在
        Menu menu = menuService.getById(menuId);
        if (menu == null) {
            throw new IllegalArgumentException("菜单不存在: id=" + menuId);
        }

        // 检查是否已分配
        if (isMenuAssignedToRole(roleId, menuId)) {
            log.warn("菜单已分配给角色: roleId={}, menuId={}", roleId, menuId);
            return true;
        }

        // 创建角色菜单关联
        RoleMenu roleMenu = new RoleMenu();
        roleMenu.setRoleId(roleId);
        roleMenu.setMenuId(menuId);
        roleMenu.setCreateTime(LocalDateTime.now());
        roleMenu.setUpdateTime(LocalDateTime.now());
        roleMenu.setDeleted(false);

        return roleMenuMapper.insert(roleMenu) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchAssignMenusToRole(Long roleId, List<Long> menuIds) {
        log.info("批量分配菜单给角色: roleId={}, menuIds={}", roleId, menuIds);

        if (menuIds == null || menuIds.isEmpty()) {
            return true;
        }

        boolean success = true;
        for (Long menuId : menuIds) {
            try {
                if (!assignMenuToRole(roleId, menuId)) {
                    success = false;
                }
            } catch (Exception e) {
                log.error("分配菜单给角色失败: roleId={}, menuId={}", roleId, menuId, e);
                success = false;
            }
        }
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeMenuFromRole(Long roleId, Long menuId) {
        log.info("移除角色的菜单: roleId={}, menuId={}", roleId, menuId);

        // 查找角色菜单关联
        LambdaQueryWrapper<RoleMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoleMenu::getRoleId, roleId);
        wrapper.eq(RoleMenu::getMenuId, menuId);
        wrapper.eq(RoleMenu::getDeleted, false);

        RoleMenu roleMenu = roleMenuMapper.selectOne(wrapper);
        if (roleMenu == null) {
            log.warn("角色未分配该菜单: roleId={}, menuId={}", roleId, menuId);
            return true;
        }

        // 逻辑删除
        roleMenu.setDeleted(true);
        roleMenu.setUpdateTime(LocalDateTime.now());

        return roleMenuMapper.updateById(roleMenu) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeAllMenusFromRole(Long roleId) {
        log.info("移除角色的所有菜单: roleId={}", roleId);

        // 查找角色的所有菜单关联
        LambdaQueryWrapper<RoleMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoleMenu::getRoleId, roleId);
        wrapper.eq(RoleMenu::getDeleted, false);

        List<RoleMenu> roleMenus = roleMenuMapper.selectList(wrapper);
        if (roleMenus.isEmpty()) {
            return true;
        }

        // 批量逻辑删除
        boolean success = true;
        for (RoleMenu roleMenu : roleMenus) {
            roleMenu.setDeleted(true);
            roleMenu.setUpdateTime(LocalDateTime.now());
            if (roleMenuMapper.updateById(roleMenu) <= 0) {
                success = false;
            }
        }

        return success;
    }

    @Override
    public List<Long> getMenuIdsByRoleId(Long roleId) {
        log.debug("获取角色的菜单ID列表: roleId={}", roleId);

        LambdaQueryWrapper<RoleMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoleMenu::getRoleId, roleId);
        wrapper.eq(RoleMenu::getDeleted, false);
        wrapper.select(RoleMenu::getMenuId);

        List<RoleMenu> roleMenus = roleMenuMapper.selectList(wrapper);
        return roleMenus.stream()
                .map(RoleMenu::getMenuId)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getPermsByRoleId(Long roleId) {
        log.debug("获取角色的权限标识列表: roleId={}", roleId);

        List<Long> menuIds = getMenuIdsByRoleId(roleId);
        if (menuIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 查询菜单的权限标识（非空）
        LambdaQueryWrapper<Menu> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Menu::getId, menuIds);
        wrapper.eq(Menu::getStatus, 1);
        wrapper.eq(Menu::getDeleted, false);
        wrapper.isNotNull(Menu::getPerms);
        wrapper.ne(Menu::getPerms, "");
        wrapper.select(Menu::getPerms);

        List<Menu> menus = menuService.list(wrapper);
        return menus.stream()
                .map(Menu::getPerms)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> getMenuIdsByUserId(Long userId) {
        log.debug("获取用户有权限的菜单ID列表: userId={}", userId);

        // 获取用户的角色ID列表
        List<Long> roleIds = userRoleService.getRoleIdsByUserId(userId);
        if (roleIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取所有角色的菜单ID（去重）
        Set<Long> menuIdSet = new HashSet<>();
        for (Long roleId : roleIds) {
            menuIdSet.addAll(getMenuIdsByRoleId(roleId));
        }

        return new ArrayList<>(menuIdSet);
    }

    @Override
    public List<String> getPermsByUserId(Long userId) {
        log.debug("获取用户有权限的权限标识列表: userId={}", userId);

        // 获取用户的角色ID列表
        List<Long> roleIds = userRoleService.getRoleIdsByUserId(userId);
        if (roleIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取所有角色的权限标识（去重）
        Set<String> permsSet = new HashSet<>();
        for (Long roleId : roleIds) {
            permsSet.addAll(getPermsByRoleId(roleId));
        }

        return new ArrayList<>(permsSet);
    }

    @Override
    public boolean hasPermissionByRoleId(Long roleId, String perms) {
        if (roleId == null || perms == null) {
            return false;
        }

        List<String> rolePerms = getPermsByRoleId(roleId);
        return rolePerms.contains(perms);
    }

    @Override
    public boolean hasPermissionByUserId(Long userId, String perms) {
        if (userId == null || perms == null) {
            return false;
        }

        List<String> userPerms = getPermsByUserId(userId);
        return userPerms.contains(perms);
    }

    @Override
    public String validate(RoleMenu roleMenu) {
        // 调用父类的基础验证
        String baseValidation = super.validate(roleMenu);
        if (baseValidation != null) {
            return baseValidation;
        }

        // 验证角色ID
        if (roleMenu.getRoleId() == null) {
            return "角色ID不能为空";
        }

        // 验证菜单ID
        if (roleMenu.getMenuId() == null) {
            return "菜单ID不能为空";
        }

        return null;
    }

    /**
     * 检查菜单是否已分配给角色
     *
     * @param roleId 角色ID
     * @param menuId 菜单ID
     * @return 是否已分配
     */
    private boolean isMenuAssignedToRole(Long roleId, Long menuId) {
        LambdaQueryWrapper<RoleMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoleMenu::getRoleId, roleId);
        wrapper.eq(RoleMenu::getMenuId, menuId);
        wrapper.eq(RoleMenu::getDeleted, false);
        return roleMenuMapper.selectCount(wrapper) > 0;
    }
}
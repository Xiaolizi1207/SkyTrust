package com.skytrust.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.skytrust.entity.Role;
import com.skytrust.entity.User;
import com.skytrust.entity.UserRole;
import com.skytrust.mapper.UserRoleMapper;
import com.skytrust.service.RoleService;
import com.skytrust.service.UserRoleService;
import com.skytrust.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户角色关联服务实现类
 *
 * @author SkyTrust Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserRoleServiceImpl extends BaseService<UserRoleMapper, UserRole> implements UserRoleService {

    private final UserRoleMapper userRoleMapper;
    private final UserService userService;
    private final RoleService roleService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignRoleToUser(Long userId, Long roleId) {
        log.info("分配角色给用户: userId={}, roleId={}", userId, roleId);

        // 检查用户是否存在
        User user = userService.getById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在: id=" + userId);
        }

        // 检查角色是否存在
        Role role = roleService.getById(roleId);
        if (role == null) {
            throw new IllegalArgumentException("角色不存在: id=" + roleId);
        }

        // 检查是否已分配
        if (isRoleAssignedToUser(userId, roleId)) {
            log.warn("角色已分配给用户: userId={}, roleId={}", userId, roleId);
            return true;
        }

        // 创建用户角色关联
        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);
        userRole.setCreateTime(LocalDateTime.now());
        userRole.setUpdateTime(LocalDateTime.now());
        userRole.setDeleted(false);

        return userRoleMapper.insert(userRole) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchAssignRolesToUser(Long userId, List<Long> roleIds) {
        log.info("批量分配角色给用户: userId={}, roleIds={}", userId, roleIds);

        if (roleIds == null || roleIds.isEmpty()) {
            return true;
        }

        boolean success = true;
        for (Long roleId : roleIds) {
            try {
                if (!assignRoleToUser(userId, roleId)) {
                    success = false;
                }
            } catch (Exception e) {
                log.error("分配角色给用户失败: userId={}, roleId={}", userId, roleId, e);
                success = false;
            }
        }
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeRoleFromUser(Long userId, Long roleId) {
        log.info("移除用户的角色: userId={}, roleId={}", userId, roleId);

        // 查找用户角色关联
        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getUserId, userId);
        wrapper.eq(UserRole::getRoleId, roleId);
        wrapper.eq(UserRole::getDeleted, false);

        UserRole userRole = userRoleMapper.selectOne(wrapper);
        if (userRole == null) {
            log.warn("用户未分配该角色: userId={}, roleId={}", userId, roleId);
            return true;
        }

        // 逻辑删除
        userRole.setDeleted(true);
        userRole.setUpdateTime(LocalDateTime.now());

        return userRoleMapper.updateById(userRole) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeAllRolesFromUser(Long userId) {
        log.info("移除用户的所有角色: userId={}", userId);

        // 查找用户的所有角色关联
        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getUserId, userId);
        wrapper.eq(UserRole::getDeleted, false);

        List<UserRole> userRoles = userRoleMapper.selectList(wrapper);
        if (userRoles.isEmpty()) {
            return true;
        }

        // 批量逻辑删除
        boolean success = true;
        for (UserRole userRole : userRoles) {
            userRole.setDeleted(true);
            userRole.setUpdateTime(LocalDateTime.now());
            if (userRoleMapper.updateById(userRole) <= 0) {
                success = false;
            }
        }

        return success;
    }

    @Override
    public List<Long> getRoleIdsByUserId(Long userId) {
        log.debug("获取用户的角色ID列表: userId={}", userId);

        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getUserId, userId);
        wrapper.eq(UserRole::getDeleted, false);
        wrapper.select(UserRole::getRoleId);

        List<UserRole> userRoles = userRoleMapper.selectList(wrapper);
        return userRoles.stream()
                .map(UserRole::getRoleId)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getRoleCodesByUserId(Long userId) {
        log.debug("获取用户的角色代码列表: userId={}", userId);

        List<Long> roleIds = getRoleIdsByUserId(userId);
        if (roleIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 批量查询角色信息
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Role::getId, roleIds);
        wrapper.eq(Role::getDeleted, false);
        wrapper.select(Role::getRoleCode);

        List<Role> roles = roleService.list(wrapper);
        return roles.stream()
                .map(Role::getRoleCode)
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasRole(Long userId, String roleCode) {
        if (userId == null || roleCode == null) {
            return false;
        }

        List<String> roleCodes = getRoleCodesByUserId(userId);
        return roleCodes.contains(roleCode);
    }

    @Override
    public boolean hasAnyRole(Long userId, List<String> roleCodes) {
        if (userId == null || roleCodes == null || roleCodes.isEmpty()) {
            return false;
        }

        List<String> userRoleCodes = getRoleCodesByUserId(userId);
        for (String roleCode : roleCodes) {
            if (userRoleCodes.contains(roleCode)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<Long> getUserIdsByRoleId(Long roleId) {
        log.debug("根据角色ID获取用户ID列表: roleId={}", roleId);

        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getRoleId, roleId);
        wrapper.eq(UserRole::getDeleted, false);
        wrapper.select(UserRole::getUserId);

        List<UserRole> userRoles = userRoleMapper.selectList(wrapper);
        return userRoles.stream()
                .map(UserRole::getUserId)
                .collect(Collectors.toList());
    }

    @Override
    public String validate(UserRole userRole) {
        // 调用父类的基础验证
        String baseValidation = super.validate(userRole);
        if (baseValidation != null) {
            return baseValidation;
        }

        // 验证用户ID
        if (userRole.getUserId() == null) {
            return "用户ID不能为空";
        }

        // 验证角色ID
        if (userRole.getRoleId() == null) {
            return "角色ID不能为空";
        }

        return null;
    }

    /**
     * 检查角色是否已分配给用户
     *
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 是否已分配
     */
    private boolean isRoleAssignedToUser(Long userId, Long roleId) {
        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getUserId, userId);
        wrapper.eq(UserRole::getRoleId, roleId);
        wrapper.eq(UserRole::getDeleted, false);
        return userRoleMapper.selectCount(wrapper) > 0;
    }
}
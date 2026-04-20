package com.skytrust.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.skytrust.common.utils.StringUtil;
import com.skytrust.entity.Role;
import com.skytrust.mapper.RoleMapper;
import com.skytrust.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色服务实现类
 *
 * @author SkyTrust Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends BaseService<RoleMapper, Role> implements RoleService {

    private final RoleMapper roleMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createRole(Role role) {
        log.info("创建角色: roleCode={}, roleName={}", role.getRoleCode(), role.getRoleName());

        // 检查角色代码是否已存在
        if (isRoleCodeExists(role.getRoleCode())) {
            throw new IllegalArgumentException("角色代码已存在: " + role.getRoleCode());
        }

        // 设置创建时间和更新时间
        role.setCreateTime(LocalDateTime.now());
        role.setUpdateTime(LocalDateTime.now());
        role.setDeleted(false);

        return roleMapper.insert(role) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRole(Role role) {
        log.info("更新角色: id={}, roleCode={}", role.getId(), role.getRoleCode());

        Role existingRole = roleMapper.selectById(role.getId());
        if (existingRole == null) {
            throw new IllegalArgumentException("角色不存在: id=" + role.getId());
        }

        // 如果角色代码有变化，检查新代码是否已存在
        if (!existingRole.getRoleCode().equals(role.getRoleCode())) {
            if (isRoleCodeExists(role.getRoleCode())) {
                throw new IllegalArgumentException("角色代码已存在: " + role.getRoleCode());
            }
        }

        role.setUpdateTime(LocalDateTime.now());

        return roleMapper.updateById(role) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRole(Long id) {
        log.info("删除角色: id={}", id);

        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new IllegalArgumentException("角色不存在: id=" + id);
        }

        // TODO: 检查是否有用户关联该角色，如果有则不允许删除

        // 逻辑删除
        role.setDeleted(true);
        role.setUpdateTime(LocalDateTime.now());

        return roleMapper.updateById(role) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDeleteRoles(List<Long> ids) {
        log.info("批量删除角色: ids={}", ids);

        boolean success = true;
        for (Long id : ids) {
            try {
                if (!deleteRole(id)) {
                    success = false;
                }
            } catch (Exception e) {
                log.error("删除角色失败: id={}", id, e);
                success = false;
            }
        }
        return success;
    }

    @Override
    public Role getRoleById(Long id) {
        log.debug("根据ID获取角色: id={}", id);
        return roleMapper.selectById(id);
    }

    @Override
    public Role getRoleByCode(String roleCode) {
        log.debug("根据角色代码获取角色: roleCode={}", roleCode);

        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Role::getRoleCode, roleCode);
        wrapper.eq(Role::getDeleted, false);
        return roleMapper.selectOne(wrapper);
    }

    @Override
    public IPage<Role> getRoleList(Integer page, Integer size,
                                   String roleCode, String roleName,
                                   Integer status, String orderBy) {
        log.debug("获取角色列表: page={}, size={}, roleCode={}, roleName={}, status={}",
                page, size, roleCode, roleName, status);

        Page<Role> pageObj = getPage(page, size);
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();

        // 构建查询条件
        if (StringUtil.isNotEmpty(roleCode)) {
            wrapper.like(Role::getRoleCode, roleCode);
        }
        if (StringUtil.isNotEmpty(roleName)) {
            wrapper.like(Role::getRoleName, roleName);
        }
        if (status != null) {
            wrapper.eq(Role::getStatus, status);
        }
        wrapper.eq(Role::getDeleted, false);

        // 应用排序
        applyOrderBy(wrapper, orderBy);

        return roleMapper.selectPage(pageObj, wrapper);
    }

    @Override
    public List<Role> getAllEnabledRoles() {
        log.debug("获取所有启用的角色列表");

        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Role::getStatus, 1);
        wrapper.eq(Role::getDeleted, false);
        wrapper.orderByAsc(Role::getSortOrder);

        return roleMapper.selectList(wrapper);
    }

    @Override
    public boolean isRoleCodeExists(String roleCode) {
        if (StringUtil.isEmpty(roleCode)) {
            return false;
        }

        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Role::getRoleCode, roleCode);
        wrapper.eq(Role::getDeleted, false);
        return roleMapper.selectCount(wrapper) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRoleStatus(Long id, Integer status) {
        log.info("更新角色状态: id={}, status={}", id, status);

        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new IllegalArgumentException("角色不存在: id=" + id);
        }

        role.setStatus(status);
        role.setUpdateTime(LocalDateTime.now());

        return roleMapper.updateById(role) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchUpdateRoleStatus(List<Long> ids, Integer status) {
        log.info("批量更新角色状态: ids={}, status={}", ids, status);

        boolean success = true;
        for (Long id : ids) {
            try {
                if (!updateRoleStatus(id, status)) {
                    success = false;
                }
            } catch (Exception e) {
                log.error("更新角色状态失败: id={}", id, e);
                success = false;
            }
        }
        return success;
    }

    @Override
    public String validate(Role role) {
        // 调用父类的基础验证
        String baseValidation = super.validate(role);
        if (baseValidation != null) {
            return baseValidation;
        }

        // 验证角色代码
        if (StringUtil.isEmpty(role.getRoleCode())) {
            return "角色代码不能为空";
        }

        // 验证角色名称
        if (StringUtil.isEmpty(role.getRoleName())) {
            return "角色名称不能为空";
        }

        // 验证状态
        if (role.getStatus() != null && role.getStatus() != 0 && role.getStatus() != 1) {
            return "状态值必须是0或1";
        }

        return null;
    }
}
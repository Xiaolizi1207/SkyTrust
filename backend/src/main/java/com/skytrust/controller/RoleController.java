package com.skytrust.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.skytrust.common.Result;
import com.skytrust.common.ResultCode;
import com.skytrust.dto.RoleDTO;
import com.skytrust.entity.Role;
import com.skytrust.service.RoleService;
import com.skytrust.service.RoleMenuService;
import com.skytrust.vo.RoleVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色控制器
 *
 * @author SkyTrust Team
 */
@Slf4j
@Tag(name = "角色管理", description = "角色的增删改查、状态管理等接口")
@Validated
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;
    private final RoleMenuService roleMenuService;

    /**
     * 创建角色
     */
    @Operation(summary = "创建角色")
    @PostMapping
    public Result<RoleVO> createRole(@Valid @RequestBody RoleDTO roleDTO) {
        log.info("创建角色请求: roleCode={}, roleName={}", roleDTO.getRoleCode(), roleDTO.getRoleName());

        try {
            // DTO转实体
            Role role = new Role();
            BeanUtils.copyProperties(roleDTO, role);

            // 调用服务创建
            boolean success = roleService.createRole(role);
            if (!success) {
                return Result.error(ResultCode.SYSTEM_ERROR, "创建角色失败");
            }

            // 实体转VO
            RoleVO roleVO = convertToVO(role);
            return Result.success(roleVO, "创建角色成功");
        } catch (IllegalArgumentException e) {
            log.error("创建角色参数错误: {}", e.getMessage(), e);
            return Result.error(ResultCode.PARAM_ERROR, e.getMessage());
        } catch (Exception e) {
            log.error("创建角色异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "创建角色异常: " + e.getMessage());
        }
    }

    /**
     * 更新角色
     */
    @Operation(summary = "更新角色")
    @PutMapping("/{id}")
    public Result<RoleVO> updateRole(
            @Parameter(description = "角色ID", required = true) @PathVariable Long id,
            @Valid @RequestBody RoleDTO roleDTO) {
        log.info("更新角色请求: id={}, roleCode={}", id, roleDTO.getRoleCode());

        try {
            // 先查询现有角色
            Role role = roleService.getRoleById(id);
            if (role == null) {
                return Result.error(ResultCode.PARAM_ERROR, "角色不存在");
            }

            // 更新字段
            BeanUtils.copyProperties(roleDTO, role);
            role.setId(id);

            // 调用服务更新
            boolean success = roleService.updateRole(role);
            if (!success) {
                return Result.error(ResultCode.SYSTEM_ERROR, "更新角色失败");
            }

            // 重新查询获取最新数据
            Role updatedRole = roleService.getRoleById(id);
            RoleVO roleVO = convertToVO(updatedRole);
            return Result.success(roleVO, "更新角色成功");
        } catch (IllegalArgumentException e) {
            log.error("更新角色参数错误: {}", e.getMessage(), e);
            return Result.error(ResultCode.PARAM_ERROR, e.getMessage());
        } catch (Exception e) {
            log.error("更新角色异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "更新角色异常: " + e.getMessage());
        }
    }

    /**
     * 删除角色（逻辑删除）
     */
    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    public Result<Void> deleteRole(@Parameter(description = "角色ID", required = true) @PathVariable Long id) {
        log.info("删除角色请求: id={}", id);

        try {
            boolean success = roleService.deleteRole(id);
            if (!success) {
                return Result.error(ResultCode.SYSTEM_ERROR, "删除角色失败");
            }
            return Result.success("删除角色成功");
        } catch (IllegalArgumentException e) {
            log.error("删除角色参数错误: {}", e.getMessage(), e);
            return Result.error(ResultCode.PARAM_ERROR, e.getMessage());
        } catch (Exception e) {
            log.error("删除角色异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "删除角色异常: " + e.getMessage());
        }
    }

    /**
     * 批量删除角色
     */
    @Operation(summary = "批量删除角色")
    @DeleteMapping("/batch")
    public Result<Void> batchDeleteRoles(@Parameter(description = "角色ID列表", required = true) @RequestBody List<Long> ids) {
        log.info("批量删除角色请求: ids={}", ids);

        try {
            boolean success = roleService.batchDeleteRoles(ids);
            if (!success) {
                return Result.error(ResultCode.SYSTEM_ERROR, "批量删除角色失败");
            }
            return Result.success("批量删除角色成功");
        } catch (Exception e) {
            log.error("批量删除角色异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "批量删除角色异常: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取角色详情
     */
    @Operation(summary = "获取角色详情")
    @GetMapping("/{id}")
    public Result<RoleVO> getRoleById(@Parameter(description = "角色ID", required = true) @PathVariable Long id) {
        log.info("获取角色详情请求: id={}", id);

        try {
            Role role = roleService.getRoleById(id);
            if (role == null) {
                return Result.error(ResultCode.PARAM_ERROR, "角色不存在");
            }

            RoleVO roleVO = convertToVO(role);
            return Result.success(roleVO);
        } catch (Exception e) {
            log.error("获取角色详情异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "获取角色详情异常: " + e.getMessage());
        }
    }

    /**
     * 根据角色代码获取角色详情
     */
    @Operation(summary = "根据角色代码获取角色详情")
    @GetMapping("/code/{roleCode}")
    public Result<RoleVO> getRoleByCode(@Parameter(description = "角色代码", required = true) @PathVariable String roleCode) {
        log.info("根据角色代码获取角色详情请求: roleCode={}", roleCode);

        try {
            Role role = roleService.getRoleByCode(roleCode);
            if (role == null) {
                return Result.error(ResultCode.PARAM_ERROR, "角色不存在");
            }

            RoleVO roleVO = convertToVO(role);
            return Result.success(roleVO);
        } catch (Exception e) {
            log.error("根据角色代码获取角色详情异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "根据角色代码获取角色详情异常: " + e.getMessage());
        }
    }

    /**
     * 获取角色列表（分页）
     */
    @Operation(summary = "获取角色列表（分页）")
    @GetMapping
    public Result<IPage<RoleVO>> getRoleList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "角色代码（模糊查询）") @RequestParam(required = false) String roleCode,
            @Parameter(description = "角色名称（模糊查询）") @RequestParam(required = false) String roleName,
            @Parameter(description = "状态（0-禁用，1-启用）") @RequestParam(required = false) Integer status,
            @Parameter(description = "排序字段") @RequestParam(required = false) String orderBy) {
        log.info("获取角色列表请求: page={}, size={}, roleCode={}, roleName={}, status={}",
                page, size, roleCode, roleName, status);

        try {
            IPage<Role> rolePage = roleService.getRoleList(page, size, roleCode, roleName, status, orderBy);
            IPage<RoleVO> roleVOPage = rolePage.convert(this::convertToVO);
            return Result.success(roleVOPage);
        } catch (Exception e) {
            log.error("获取角色列表异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "获取角色列表异常: " + e.getMessage());
        }
    }

    /**
     * 获取所有启用的角色列表
     */
    @Operation(summary = "获取所有启用的角色列表")
    @GetMapping("/enabled")
    public Result<List<RoleVO>> getAllEnabledRoles() {
        log.info("获取所有启用的角色列表请求");

        try {
            List<Role> roles = roleService.getAllEnabledRoles();
            List<RoleVO> roleVOs = roles.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            return Result.success(roleVOs);
        } catch (Exception e) {
            log.error("获取所有启用的角色列表异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "获取所有启用的角色列表异常: " + e.getMessage());
        }
    }

    /**
     * 更新角色状态
     */
    @Operation(summary = "更新角色状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateRoleStatus(
            @Parameter(description = "角色ID", required = true) @PathVariable Long id,
            @Parameter(description = "状态（0-禁用，1-启用）", required = true) @RequestParam Integer status) {
        log.info("更新角色状态请求: id={}, status={}", id, status);

        try {
            boolean success = roleService.updateRoleStatus(id, status);
            if (!success) {
                return Result.error(ResultCode.SYSTEM_ERROR, "更新角色状态失败");
            }
            return Result.success("更新角色状态成功");
        } catch (IllegalArgumentException e) {
            log.error("更新角色状态参数错误: {}", e.getMessage(), e);
            return Result.error(ResultCode.PARAM_ERROR, e.getMessage());
        } catch (Exception e) {
            log.error("更新角色状态异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "更新角色状态异常: " + e.getMessage());
        }
    }

    /**
     * 批量更新角色状态
     */
    @Operation(summary = "批量更新角色状态")
    @PutMapping("/batch/status")
    public Result<Void> batchUpdateRoleStatus(
            @Parameter(description = "角色ID列表", required = true) @RequestBody List<Long> ids,
            @Parameter(description = "状态（0-禁用，1-启用）", required = true) @RequestParam Integer status) {
        log.info("批量更新角色状态请求: ids={}, status={}", ids, status);

        try {
            boolean success = roleService.batchUpdateRoleStatus(ids, status);
            if (!success) {
                return Result.error(ResultCode.SYSTEM_ERROR, "批量更新角色状态失败");
            }
            return Result.success("批量更新角色状态成功");
        } catch (Exception e) {
            log.error("批量更新角色状态异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "批量更新角色状态异常: " + e.getMessage());
        }
    }

    /**
     * 检查角色代码是否已存在
     */
    @Operation(summary = "检查角色代码是否已存在")
    @GetMapping("/check-role-code")
    public Result<Boolean> checkRoleCodeExists(
            @Parameter(description = "角色代码", required = true) @RequestParam String roleCode) {
        log.info("检查角色代码是否已存在请求: roleCode={}", roleCode);

        try {
            boolean exists = roleService.isRoleCodeExists(roleCode);
            return Result.success(exists);
        } catch (Exception e) {
            log.error("检查角色代码是否已存在异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "检查角色代码是否已存在异常: " + e.getMessage());
        }
    }

    /**
     * 将Role实体转换为RoleVO
     */
    private RoleVO convertToVO(Role role) {
        if (role == null) {
            return null;
        }
        RoleVO roleVO = new RoleVO();
        BeanUtils.copyProperties(role, roleVO);
        return roleVO;
    }

    /**
     * 获取角色关联的菜单ID列表
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取角色关联的菜单ID列表")
    @GetMapping("/{roleId}/menus")
    public Result<List<Long>> getMenuIdsByRoleId(@Parameter(description = "角色ID", required = true) @PathVariable Long roleId) {
        log.info("获取角色关联的菜单ID列表请求: roleId={}", roleId);

        try {
            List<Long> menuIds = roleMenuService.getMenuIdsByRoleId(roleId);
            return Result.success(menuIds);
        } catch (Exception e) {
            log.error("获取角色关联的菜单ID列表异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "获取角色关联的菜单ID列表异常: " + e.getMessage());
        }
    }

    /**
     * 批量分配菜单给角色
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "批量分配菜单给角色")
    @PostMapping("/{roleId}/menus")
    public Result<Void> batchAssignMenusToRole(
            @Parameter(description = "角色ID", required = true) @PathVariable Long roleId,
            @Parameter(description = "菜单ID列表", required = true) @RequestBody List<Long> menuIds) {
        log.info("批量分配菜单给角色请求: roleId={}, menuIds={}", roleId, menuIds);

        try {
            boolean success = roleMenuService.batchAssignMenusToRole(roleId, menuIds);
            if (!success) {
                return Result.error(ResultCode.SYSTEM_ERROR, "批量分配菜单给角色失败");
            }
            return Result.success("批量分配菜单给角色成功");
        } catch (IllegalArgumentException e) {
            log.error("批量分配菜单给角色参数错误: {}", e.getMessage(), e);
            return Result.error(ResultCode.PARAM_ERROR, e.getMessage());
        } catch (Exception e) {
            log.error("批量分配菜单给角色异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "批量分配菜单给角色异常: " + e.getMessage());
        }
    }

    /**
     * 移除角色的菜单
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "移除角色的菜单")
    @DeleteMapping("/{roleId}/menus/{menuId}")
    public Result<Void> removeMenuFromRole(
            @Parameter(description = "角色ID", required = true) @PathVariable Long roleId,
            @Parameter(description = "菜单ID", required = true) @PathVariable Long menuId) {
        log.info("移除角色的菜单请求: roleId={}, menuId={}", roleId, menuId);

        try {
            boolean success = roleMenuService.removeMenuFromRole(roleId, menuId);
            if (!success) {
                return Result.error(ResultCode.SYSTEM_ERROR, "移除角色的菜单失败");
            }
            return Result.success("移除角色的菜单成功");
        } catch (Exception e) {
            log.error("移除角色的菜单异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "移除角色的菜单异常: " + e.getMessage());
        }
    }

    /**
     * 移除角色的所有菜单
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "移除角色的所有菜单")
    @DeleteMapping("/{roleId}/menus")
    public Result<Void> removeAllMenusFromRole(@Parameter(description = "角色ID", required = true) @PathVariable Long roleId) {
        log.info("移除角色的所有菜单请求: roleId={}", roleId);

        try {
            boolean success = roleMenuService.removeAllMenusFromRole(roleId);
            if (!success) {
                return Result.error(ResultCode.SYSTEM_ERROR, "移除角色的所有菜单失败");
            }
            return Result.success("移除角色的所有菜单成功");
        } catch (Exception e) {
            log.error("移除角色的所有菜单异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "移除角色的所有菜单异常: " + e.getMessage());
        }
    }

    /**
     * 获取角色的权限标识列表
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取角色的权限标识列表")
    @GetMapping("/{roleId}/permissions")
    public Result<List<String>> getPermsByRoleId(@Parameter(description = "角色ID", required = true) @PathVariable Long roleId) {
        log.info("获取角色的权限标识列表请求: roleId={}", roleId);

        try {
            List<String> perms = roleMenuService.getPermsByRoleId(roleId);
            return Result.success(perms);
        } catch (Exception e) {
            log.error("获取角色的权限标识列表异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "获取角色的权限标识列表异常: " + e.getMessage());
        }
    }

    /**
     * 分配菜单给角色
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "分配菜单给角色")
    @PostMapping("/{roleId}/menu/{menuId}")
    public Result<Void> assignMenuToRole(
            @Parameter(description = "角色ID", required = true) @PathVariable Long roleId,
            @Parameter(description = "菜单ID", required = true) @PathVariable Long menuId) {
        log.info("分配菜单给角色请求: roleId={}, menuId={}", roleId, menuId);

        try {
            boolean success = roleMenuService.assignMenuToRole(roleId, menuId);
            if (!success) {
                return Result.error(ResultCode.SYSTEM_ERROR, "分配菜单给角色失败");
            }
            return Result.success("分配菜单给角色成功");
        } catch (IllegalArgumentException e) {
            log.error("分配菜单给角色参数错误: {}", e.getMessage(), e);
            return Result.error(ResultCode.PARAM_ERROR, e.getMessage());
        } catch (Exception e) {
            log.error("分配菜单给角色异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "分配菜单给角色异常: " + e.getMessage());
        }
    }

    /**
     * 检查角色是否拥有指定权限
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "检查角色是否拥有指定权限")
    @GetMapping("/{roleId}/check-permission")
    public Result<Boolean> checkRolePermission(
            @Parameter(description = "角色ID", required = true) @PathVariable Long roleId,
            @Parameter(description = "权限标识", required = true) @RequestParam String perms) {
        log.info("检查角色是否拥有指定权限请求: roleId={}, perms={}", roleId, perms);

        try {
            boolean hasPermission = roleMenuService.hasPermissionByRoleId(roleId, perms);
            return Result.success(hasPermission);
        } catch (Exception e) {
            log.error("检查角色是否拥有指定权限异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "检查角色是否拥有指定权限异常: " + e.getMessage());
        }
    }
}
package com.skytrust.controller;

import com.skytrust.common.Result;
import com.skytrust.common.ResultCode;
import com.skytrust.dto.RoleMenuDTO;
import com.skytrust.service.MenuService;
import com.skytrust.service.RoleMenuService;
import com.skytrust.service.RoleService;
import com.skytrust.vo.MenuVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色菜单关联控制器
 *
 * @author SkyTrust Team
 */
@Slf4j
@Tag(name = "角色菜单管理", description = "角色与菜单关联关系的增删改查、权限管理等接口")
@Validated
@RestController
@RequestMapping("/api/role-menus")
@RequiredArgsConstructor
public class RoleMenuController {

    private final RoleMenuService roleMenuService;
    private final RoleService roleService;
    private final MenuService menuService;

    /**
     * 分配菜单给角色
     */
    @Operation(summary = "分配菜单给角色")
    @PostMapping
    public Result<Void> assignMenuToRole(@Valid @RequestBody RoleMenuDTO roleMenuDTO) {
        log.info("分配菜单给角色请求: roleId={}, menuId={}", roleMenuDTO.getRoleId(), roleMenuDTO.getMenuId());

        try {
            boolean success = roleMenuService.assignMenuToRole(roleMenuDTO.getRoleId(), roleMenuDTO.getMenuId());
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
     * 批量分配菜单给角色
     */
    @Operation(summary = "批量分配菜单给角色")
    @PostMapping("/batch")
    public Result<Void> batchAssignMenusToRole(
            @Parameter(description = "角色ID", required = true) @RequestParam Long roleId,
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
    @Operation(summary = "移除角色的菜单")
    @DeleteMapping
    public Result<Void> removeMenuFromRole(@Valid @RequestBody RoleMenuDTO roleMenuDTO) {
        log.info("移除角色的菜单请求: roleId={}, menuId={}", roleMenuDTO.getRoleId(), roleMenuDTO.getMenuId());

        try {
            boolean success = roleMenuService.removeMenuFromRole(roleMenuDTO.getRoleId(), roleMenuDTO.getMenuId());
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
    @Operation(summary = "移除角色的所有菜单")
    @DeleteMapping("/role/{roleId}")
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
     * 获取角色的菜单ID列表
     */
    @Operation(summary = "获取角色的菜单ID列表")
    @GetMapping("/role/{roleId}/menu-ids")
    public Result<List<Long>> getMenuIdsByRoleId(@Parameter(description = "角色ID", required = true) @PathVariable Long roleId) {
        log.info("获取角色的菜单ID列表请求: roleId={}", roleId);

        try {
            List<Long> menuIds = roleMenuService.getMenuIdsByRoleId(roleId);
            return Result.success(menuIds);
        } catch (Exception e) {
            log.error("获取角色的菜单ID列表异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "获取角色的菜单ID列表异常: " + e.getMessage());
        }
    }

    /**
     * 获取角色的菜单详情列表
     */
    @Operation(summary = "获取角色的菜单详情列表")
    @GetMapping("/role/{roleId}/menus")
    public Result<List<MenuVO>> getMenusByRoleId(@Parameter(description = "角色ID", required = true) @PathVariable Long roleId) {
        log.info("获取角色的菜单详情列表请求: roleId={}", roleId);

        try {
            List<Long> menuIds = roleMenuService.getMenuIdsByRoleId(roleId);
            if (menuIds.isEmpty()) {
                return Result.success(List.of());
            }

            List<MenuVO> menuVOs = menuIds.stream()
                    .map(menuId -> {
                        var menu = menuService.getById(menuId);
                        if (menu != null) {
                            MenuVO menuVO = new MenuVO();
                            BeanUtils.copyProperties(menu, menuVO);
                            return menuVO;
                        }
                        return null;
                    })
                    .filter(menuVO -> menuVO != null)
                    .collect(Collectors.toList());

            return Result.success(menuVOs);
        } catch (Exception e) {
            log.error("获取角色的菜单详情列表异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "获取角色的菜单详情列表异常: " + e.getMessage());
        }
    }

    /**
     * 获取角色的权限标识列表
     */
    @Operation(summary = "获取角色的权限标识列表")
    @GetMapping("/role/{roleId}/perms")
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
     * 获取用户有权限的菜单ID列表
     */
    @Operation(summary = "获取用户有权限的菜单ID列表")
    @GetMapping("/user/{userId}/menu-ids")
    public Result<List<Long>> getMenuIdsByUserId(@Parameter(description = "用户ID", required = true) @PathVariable Long userId) {
        log.info("获取用户有权限的菜单ID列表请求: userId={}", userId);

        try {
            List<Long> menuIds = roleMenuService.getMenuIdsByUserId(userId);
            return Result.success(menuIds);
        } catch (Exception e) {
            log.error("获取用户有权限的菜单ID列表异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "获取用户有权限的菜单ID列表异常: " + e.getMessage());
        }
    }

    /**
     * 获取用户有权限的权限标识列表
     */
    @Operation(summary = "获取用户有权限的权限标识列表")
    @GetMapping("/user/{userId}/perms")
    public Result<List<String>> getPermsByUserId(@Parameter(description = "用户ID", required = true) @PathVariable Long userId) {
        log.info("获取用户有权限的权限标识列表请求: userId={}", userId);

        try {
            List<String> perms = roleMenuService.getPermsByUserId(userId);
            return Result.success(perms);
        } catch (Exception e) {
            log.error("获取用户有权限的权限标识列表异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "获取用户有权限的权限标识列表异常: " + e.getMessage());
        }
    }

    /**
     * 检查角色是否拥有指定权限
     */
    @Operation(summary = "检查角色是否拥有指定权限")
    @GetMapping("/check-role-permission")
    public Result<Boolean> hasPermissionByRole(
            @Parameter(description = "角色ID", required = true) @RequestParam Long roleId,
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

    /**
     * 检查用户是否拥有指定权限
     */
    @Operation(summary = "检查用户是否拥有指定权限")
    @GetMapping("/check-user-permission")
    public Result<Boolean> hasPermissionByUser(
            @Parameter(description = "用户ID", required = true) @RequestParam Long userId,
            @Parameter(description = "权限标识", required = true) @RequestParam String perms) {
        log.info("检查用户是否拥有指定权限请求: userId={}, perms={}", userId, perms);

        try {
            boolean hasPermission = roleMenuService.hasPermissionByUserId(userId, perms);
            return Result.success(hasPermission);
        } catch (Exception e) {
            log.error("检查用户是否拥有指定权限异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "检查用户是否拥有指定权限异常: " + e.getMessage());
        }
    }
}
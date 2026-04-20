package com.skytrust.controller;

import com.skytrust.common.Result;
import com.skytrust.common.ResultCode;
import com.skytrust.dto.UserRoleDTO;
import com.skytrust.service.RoleService;
import com.skytrust.service.UserRoleService;
import com.skytrust.service.UserService;
import com.skytrust.vo.RoleVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户角色关联控制器
 *
 * @author SkyTrust Team
 */
@Api(tags = "用户角色管理", description = "用户与角色关联关系的增删改查等接口")
@Validated
@RestController
@RequestMapping("/api/user-roles")
@RequiredArgsConstructor
public class UserRoleController {

    private static final Logger log = LoggerFactory.getLogger(UserRoleController.class);

    private final UserRoleService userRoleService;
    private final UserService userService;
    private final RoleService roleService;

    /**
     * 分配角色给用户
     */
    @ApiOperation(value = "分配角色给用户")
    @PostMapping
    public Result<Void> assignRoleToUser(@Valid @RequestBody UserRoleDTO userRoleDTO) {
        log.info("分配角色给用户请求: userId={}, roleId={}", userRoleDTO.getUserId(), userRoleDTO.getRoleId());

        try {
            boolean success = userRoleService.assignRoleToUser(userRoleDTO.getUserId(), userRoleDTO.getRoleId());
            if (!success) {
                return Result.error(ResultCode.SYSTEM_ERROR, "分配角色给用户失败");
            }
            return Result.success("分配角色给用户成功");
        } catch (IllegalArgumentException e) {
            log.error("分配角色给用户参数错误: {}", e.getMessage(), e);
            return Result.error(ResultCode.PARAM_ERROR, e.getMessage());
        } catch (Exception e) {
            log.error("分配角色给用户异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "分配角色给用户异常: " + e.getMessage());
        }
    }

    /**
     * 批量分配角色给用户
     */
    @ApiOperation(value = "批量分配角色给用户")
    @PostMapping("/batch")
    public Result<Void> batchAssignRolesToUser(
            @ApiParam(value = "用户ID", required = true) @RequestParam Long userId,
            @ApiParam(value = "角色ID列表", required = true) @RequestBody List<Long> roleIds) {
        log.info("批量分配角色给用户请求: userId={}, roleIds={}", userId, roleIds);

        try {
            boolean success = userRoleService.batchAssignRolesToUser(userId, roleIds);
            if (!success) {
                return Result.error(ResultCode.SYSTEM_ERROR, "批量分配角色给用户失败");
            }
            return Result.success("批量分配角色给用户成功");
        } catch (IllegalArgumentException e) {
            log.error("批量分配角色给用户参数错误: {}", e.getMessage(), e);
            return Result.error(ResultCode.PARAM_ERROR, e.getMessage());
        } catch (Exception e) {
            log.error("批量分配角色给用户异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "批量分配角色给用户异常: " + e.getMessage());
        }
    }

    /**
     * 移除用户的角色
     */
    @ApiOperation(value = "移除用户的角色")
    @DeleteMapping
    public Result<Void> removeRoleFromUser(@Valid @RequestBody UserRoleDTO userRoleDTO) {
        log.info("移除用户的角色请求: userId={}, roleId={}", userRoleDTO.getUserId(), userRoleDTO.getRoleId());

        try {
            boolean success = userRoleService.removeRoleFromUser(userRoleDTO.getUserId(), userRoleDTO.getRoleId());
            if (!success) {
                return Result.error(ResultCode.SYSTEM_ERROR, "移除用户的角色失败");
            }
            return Result.success("移除用户的角色成功");
        } catch (Exception e) {
            log.error("移除用户的角色异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "移除用户的角色异常: " + e.getMessage());
        }
    }

    /**
     * 移除用户的所有角色
     */
    @ApiOperation(value = "移除用户的所有角色")
    @DeleteMapping("/user/{userId}")
    public Result<Void> removeAllRolesFromUser(@ApiParam(value = "用户ID", required = true) @PathVariable Long userId) {
        log.info("移除用户的所有角色请求: userId={}", userId);

        try {
            boolean success = userRoleService.removeAllRolesFromUser(userId);
            if (!success) {
                return Result.error(ResultCode.SYSTEM_ERROR, "移除用户的所有角色失败");
            }
            return Result.success("移除用户的所有角色成功");
        } catch (Exception e) {
            log.error("移除用户的所有角色异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "移除用户的所有角色异常: " + e.getMessage());
        }
    }

    /**
     * 获取用户的角色ID列表
     */
    @ApiOperation(value = "获取用户的角色ID列表")
    @GetMapping("/user/{userId}/role-ids")
    public Result<List<Long>> getRoleIdsByUserId(@ApiParam(value = "用户ID", required = true) @PathVariable Long userId) {
        log.info("获取用户的角色ID列表请求: userId={}", userId);

        try {
            List<Long> roleIds = userRoleService.getRoleIdsByUserId(userId);
            return Result.success(roleIds);
        } catch (Exception e) {
            log.error("获取用户的角色ID列表异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "获取用户的角色ID列表异常: " + e.getMessage());
        }
    }

    /**
     * 获取用户的角色代码列表
     */
    @ApiOperation(value = "获取用户的角色代码列表")
    @GetMapping("/user/{userId}/role-codes")
    public Result<List<String>> getRoleCodesByUserId(@ApiParam(value = "用户ID", required = true) @PathVariable Long userId) {
        log.info("获取用户的角色代码列表请求: userId={}", userId);

        try {
            List<String> roleCodes = userRoleService.getRoleCodesByUserId(userId);
            return Result.success(roleCodes);
        } catch (Exception e) {
            log.error("获取用户的角色代码列表异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "获取用户的角色代码列表异常: " + e.getMessage());
        }
    }

    /**
     * 获取用户的角色详情列表
     */
    @ApiOperation(value = "获取用户的角色详情列表")
    @GetMapping("/user/{userId}/roles")
    public Result<List<RoleVO>> getRolesByUserId(@ApiParam(value = "用户ID", required = true) @PathVariable Long userId) {
        log.info("获取用户的角色详情列表请求: userId={}", userId);

        try {
            List<Long> roleIds = userRoleService.getRoleIdsByUserId(userId);
            if (roleIds.isEmpty()) {
                return Result.success(List.of());
            }

            List<RoleVO> roleVOs = roleIds.stream()
                    .map(roleId -> {
                        var role = roleService.getById(roleId);
                        if (role != null) {
                            RoleVO roleVO = new RoleVO();
                            BeanUtils.copyProperties(role, roleVO);
                            return roleVO;
                        }
                        return null;
                    })
                    .filter(roleVO -> roleVO != null)
                    .collect(Collectors.toList());

            return Result.success(roleVOs);
        } catch (Exception e) {
            log.error("获取用户的角色详情列表异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "获取用户的角色详情列表异常: " + e.getMessage());
        }
    }

    /**
     * 检查用户是否拥有指定角色
     */
    @ApiOperation(value = "检查用户是否拥有指定角色")
    @GetMapping("/check-role")
    public Result<Boolean> hasRole(
            @ApiParam(value = "用户ID", required = true) @RequestParam Long userId,
            @ApiParam(value = "角色代码", required = true) @RequestParam String roleCode) {
        log.info("检查用户是否拥有指定角色请求: userId={}, roleCode={}", userId, roleCode);

        try {
            boolean hasRole = userRoleService.hasRole(userId, roleCode);
            return Result.success(hasRole);
        } catch (Exception e) {
            log.error("检查用户是否拥有指定角色异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "检查用户是否拥有指定角色异常: " + e.getMessage());
        }
    }

    /**
     * 检查用户是否拥有任意指定角色
     */
    @ApiOperation(value = "检查用户是否拥有任意指定角色")
    @PostMapping("/check-any-role")
    public Result<Boolean> hasAnyRole(
            @ApiParam(value = "用户ID", required = true) @RequestParam Long userId,
            @ApiParam(value = "角色代码列表", required = true) @RequestBody List<String> roleCodes) {
        log.info("检查用户是否拥有任意指定角色请求: userId={}, roleCodes={}", userId, roleCodes);

        try {
            boolean hasAnyRole = userRoleService.hasAnyRole(userId, roleCodes);
            return Result.success(hasAnyRole);
        } catch (Exception e) {
            log.error("检查用户是否拥有任意指定角色异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "检查用户是否拥有任意指定角色异常: " + e.getMessage());
        }
    }

    /**
     * 根据角色ID获取用户ID列表
     */
    @ApiOperation(value = "根据角色ID获取用户ID列表")
    @GetMapping("/role/{roleId}/user-ids")
    public Result<List<Long>> getUserIdsByRoleId(@ApiParam(value = "角色ID", required = true) @PathVariable Long roleId) {
        log.info("根据角色ID获取用户ID列表请求: roleId={}", roleId);

        try {
            List<Long> userIds = userRoleService.getUserIdsByRoleId(roleId);
            return Result.success(userIds);
        } catch (Exception e) {
            log.error("根据角色ID获取用户ID列表异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "根据角色ID获取用户ID列表异常: " + e.getMessage());
        }
    }
}
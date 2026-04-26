package com.skytrust.controller;

import com.skytrust.common.Result;
import com.skytrust.common.ResultCode;
import com.skytrust.dto.LoginDTO;
import com.skytrust.dto.RegisterDTO;
import com.skytrust.dto.UserDTO;
import com.skytrust.dto.PasswordUpdateDTO;
import com.skytrust.entity.User;
import com.skytrust.enums.UserRoleEnum;
import com.skytrust.enums.UserStatusEnum;
import com.skytrust.service.UserService;
import com.skytrust.vo.LoginVO;
import com.skytrust.vo.UserVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import com.skytrust.common.utils.SecurityUtil;
import com.skytrust.service.TokenBlacklistService;

/**
 * 用户控制器
 *
 * @author SkyTrust Team
 */
@Tag(name = "用户管理", description = "用户注册、登录、信息管理等接口")
@Validated
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final TokenBlacklistService tokenBlacklistService;

    public UserController(UserService userService, TokenBlacklistService tokenBlacklistService) {
        this.userService = userService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    /**
     * 获取当前用户信息
     */
    @Operation(summary = "获取当前用户信息")
    @GetMapping("/me")
    public Result<UserVO> getCurrentUser() {
        // 从Security上下文获取当前用户名
        String username = SecurityUtil.getCurrentUsername();
        if (username == null) {
            return Result.error(ResultCode.USER_NOT_LOGIN);
        }

        // 根据用户名查询用户
        User user = userService.getUserByUsername(username);
        if (user == null) {
            return Result.error(ResultCode.USER_NOT_EXIST);
        }

        return Result.success(convertToVO(user));
    }

    /**
     * 更新用户信息
     */
    @Operation(summary = "更新用户信息")
    @PutMapping("/{id}")
    public Result<UserVO> updateUser(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id,
            @Valid @RequestBody UserDTO userDTO) {
        // 权限检查：用户只能更新自己的信息，管理员可以更新任何用户
        String currentUsername = SecurityUtil.getCurrentUsername();
        User currentUser = userService.getUserByUsername(currentUsername);
        if (currentUser == null) {
            return Result.error(ResultCode.USER_NOT_EXIST);
        }
        if (!SecurityUtil.isAdmin() && !currentUser.getId().equals(id)) {
            return Result.error(ResultCode.FORBIDDEN, "只能更新自己的信息");
        }

        User user = userService.getById(id);
        if (user == null) {
            return Result.error(ResultCode.USER_NOT_EXIST);
        }

        // 更新字段（排除不能修改的字段）
        BeanUtils.copyProperties(userDTO, user, "id", "username", "password", "createTime", "updateTime");

        // 如果提供了新密码，需要加密
        if (userDTO.getPassword() != null && !userDTO.getPassword().trim().isEmpty()) {
            user.setPassword(SecurityUtil.encryptPassword(userDTO.getPassword()));
        }

        boolean updated = userService.updateById(user);
        if (!updated) {
            return Result.error("用户信息更新失败");
        }

        return Result.success(convertToVO(user), "用户信息更新成功");
    }

    /**
     * 获取用户列表（分页）
     */
    @Operation(summary = "获取用户列表（分页）")
    @GetMapping
    public Result<IPage<UserVO>> getUserList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "用户名（模糊查询）") @RequestParam(required = false) String username,
            @Parameter(description = "手机号（模糊查询）") @RequestParam(required = false) String phone,
            @Parameter(description = "真实姓名（模糊查询）") @RequestParam(required = false) String realName,
            @Parameter(description = "角色") @RequestParam(required = false) String role,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "排序字段") @RequestParam(required = false) String orderBy) {
        // 权限检查：只允许管理员访问用户列表
        if (!SecurityUtil.isAdmin()) {
            return Result.error(ResultCode.FORBIDDEN, "权限不足");
        }

        // 使用Service的分页查询
        IPage<User> userPage = userService.pageUsers(page, size, username, phone, realName, role, status, orderBy);

        // 转换为UserVO
        IPage<UserVO> voPage = userPage.convert(this::convertToVO);

        return Result.success(voPage);
    }

    /**
     * 根据ID获取用户信息
     */
    @Operation(summary = "根据ID获取用户信息")
    @GetMapping("/{id}")
    public Result<UserVO> getUserById(@Parameter(description = "用户ID", required = true) @PathVariable Long id) {
        // 权限检查：用户只能查看自己的信息，管理员可以查看任何用户
        String currentUsername = SecurityUtil.getCurrentUsername();
        User currentUser = userService.getUserByUsername(currentUsername);
        if (currentUser == null) {
            return Result.error(ResultCode.USER_NOT_EXIST);
        }
        if (!SecurityUtil.isAdmin() && !currentUser.getId().equals(id)) {
            return Result.error(ResultCode.FORBIDDEN, "只能查看自己的信息");
        }

        User user = userService.getById(id);
        if (user == null) {
            return Result.error(ResultCode.USER_NOT_EXIST);
        }
        return Result.success(convertToVO(user));
    }

    /**
     * 删除用户（逻辑删除）
     */
    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@Parameter(description = "用户ID", required = true) @PathVariable Long id) {
        // 权限检查：只允许管理员删除用户
        if (!SecurityUtil.isAdmin()) {
            return Result.error(ResultCode.FORBIDDEN, "权限不足");
        }

        boolean deleted = userService.logicRemoveById(id);
        if (!deleted) {
            return Result.error("用户删除失败");
        }
        return Result.success("用户删除成功");
    }

    /**
     * 修改密码（用户自己）
     */
    @Operation(summary = "修改密码")
    @PutMapping("/{id}/password")
    public Result<Void> updatePassword(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id,
            @Valid @RequestBody PasswordUpdateDTO passwordUpdateDTO) {
        // 权限检查：用户只能修改自己的密码，管理员可以修改任何用户的密码
        String currentUsername = SecurityUtil.getCurrentUsername();
        User currentUser = userService.getUserByUsername(currentUsername);
        if (currentUser == null) {
            return Result.error(ResultCode.USER_NOT_EXIST);
        }

        if (!SecurityUtil.isAdmin() && !currentUser.getId().equals(id)) {
            return Result.error(ResultCode.FORBIDDEN, "只能修改自己的密码");
        }

        boolean updated = userService.updatePassword(id, passwordUpdateDTO.getOldPassword(), passwordUpdateDTO.getNewPassword());
        if (!updated) {
            return Result.error("密码修改失败");
        }
        return Result.success("密码修改成功");
    }

    /**
     * 重置密码（管理员操作）
     */
    @Operation(summary = "重置密码（管理员操作）")
    @PutMapping("/{id}/password/reset")
    public Result<Void> resetPassword(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id,
            @Parameter(description = "新密码", required = true) @RequestParam String newPassword) {
        // 权限检查：只允许管理员操作
        if (!SecurityUtil.isAdmin()) {
            return Result.error(ResultCode.FORBIDDEN, "权限不足");
        }

        boolean updated = userService.resetPassword(id, newPassword);
        if (!updated) {
            return Result.error("密码重置失败");
        }
        return Result.success("密码重置成功");
    }

    /**
     * 更新用户状态（管理员操作）
     */
    @Operation(summary = "更新用户状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id,
            @Parameter(description = "状态（0-禁用，1-启用）", required = true) @RequestParam Integer status) {
        // 权限检查：只允许管理员操作
        if (!SecurityUtil.isAdmin()) {
            return Result.error(ResultCode.FORBIDDEN, "权限不足");
        }

        boolean updated = userService.updateStatus(id, status);
        if (!updated) {
            return Result.error("状态更新失败");
        }
        return Result.success("状态更新成功");
    }

    /**
     * 更新用户角色（管理员操作）
     */
    @Operation(summary = "更新用户角色")
    @PutMapping("/{id}/role")
    public Result<Void> updateRole(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id,
            @Parameter(description = "角色（admin-管理员，user-普通用户，pilot-飞行员）", required = true) @RequestParam String role) {
        // 权限检查：只允许管理员操作
        if (!SecurityUtil.isAdmin()) {
            return Result.error(ResultCode.FORBIDDEN, "权限不足");
        }

        boolean updated = userService.updateRole(id, role);
        if (!updated) {
            return Result.error("角色更新失败");
        }
        return Result.success("角色更新成功");
    }

    /**
     * 更新用户信用评分（管理员操作）
     */
    @Operation(summary = "更新用户信用评分")
    @PutMapping("/{id}/credit-score")
    public Result<Void> updateCreditScore(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id,
            @Parameter(description = "信用评分（0-100）", required = true) @RequestParam Integer creditScore) {
        // 权限检查：只允许管理员操作
        if (!SecurityUtil.isAdmin()) {
            return Result.error(ResultCode.FORBIDDEN, "权限不足");
        }

        boolean updated = userService.updateCreditScore(id, creditScore);
        if (!updated) {
            return Result.error("信用评分更新失败");
        }
        return Result.success("信用评分更新成功");
    }

    /**
     * 强制下线用户（管理员操作）
     */
    @Operation(summary = "强制下线用户")
    @PostMapping("/{id}/force-logout")
    public Result<Void> forceLogout(@Parameter(description = "用户ID", required = true) @PathVariable Long id) {
        // 权限检查：只允许管理员操作
        if (!SecurityUtil.isAdmin()) {
            return Result.error(ResultCode.FORBIDDEN, "权限不足");
        }

        User user = userService.getById(id);
        if (user == null) {
            return Result.error(ResultCode.USER_NOT_EXIST);
        }

        tokenBlacklistService.removeAllByUsername(user.getUsername());
        return Result.success("用户已强制下线");
    }

    /**
     * 批量更新用户状态（管理员操作）
     */
    @Operation(summary = "批量更新用户状态")
    @PutMapping("/batch/status")
    public Result<Void> batchUpdateStatus(
            @Parameter(description = "用户ID列表", required = true) @RequestBody List<Long> userIds,
            @Parameter(description = "状态（0-禁用，1-启用）", required = true) @RequestParam Integer status) {
        // 权限检查：只允许管理员操作
        if (!SecurityUtil.isAdmin()) {
            return Result.error(ResultCode.FORBIDDEN, "权限不足");
        }

        boolean updated = userService.batchUpdateStatus(userIds, status);
        if (!updated) {
            return Result.error("批量状态更新失败");
        }
        return Result.success("批量状态更新成功");
    }

    /**
     * 批量更新用户角色（管理员操作）
     */
    @Operation(summary = "批量更新用户角色")
    @PutMapping("/batch/role")
    public Result<Void> batchUpdateRole(
            @Parameter(description = "用户ID列表", required = true) @RequestBody List<Long> userIds,
            @Parameter(description = "角色（admin-管理员，user-普通用户，pilot-飞行员）", required = true) @RequestParam String role) {
        // 权限检查：只允许管理员操作
        if (!SecurityUtil.isAdmin()) {
            return Result.error(ResultCode.FORBIDDEN, "权限不足");
        }

        boolean updated = userService.batchUpdateRole(userIds, role);
        if (!updated) {
            return Result.error("批量角色更新失败");
        }
        return Result.success("批量角色更新成功");
    }

    /**
     * 将User实体转换为UserVO
     */
    private UserVO convertToVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

}
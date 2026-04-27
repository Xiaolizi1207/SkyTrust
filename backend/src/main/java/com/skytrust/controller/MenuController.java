package com.skytrust.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.skytrust.common.Result;
import com.skytrust.common.ResultCode;
import com.skytrust.dto.MenuDTO;
import com.skytrust.entity.Menu;
import com.skytrust.service.MenuService;
import com.skytrust.service.RoleMenuService;
import com.skytrust.vo.MenuVO;
import com.skytrust.common.utils.SecurityUtil;
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
 * 菜单控制器
 *
 * @author SkyTrust Team
 */
@Slf4j
@Tag(name = "菜单管理", description = "菜单的增删改查、树形结构、权限管理等接口")
@Validated
@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;
    private final RoleMenuService roleMenuService;

    /**
     * 创建菜单
     */
    @Operation(summary = "创建菜单")
    @PostMapping
    public Result<MenuVO> createMenu(@Valid @RequestBody MenuDTO menuDTO) {
        log.info("创建菜单请求: menuCode={}, menuName={}", menuDTO.getMenuCode(), menuDTO.getMenuName());

        try {
            // DTO转实体
            Menu menu = new Menu();
            BeanUtils.copyProperties(menuDTO, menu);

            // 调用服务创建
            boolean success = menuService.createMenu(menu);
            if (!success) {
                return Result.error(ResultCode.SYSTEM_ERROR, "创建菜单失败");
            }

            // 实体转VO
            MenuVO menuVO = convertToVO(menu);
            return Result.success(menuVO, "创建菜单成功");
        } catch (IllegalArgumentException e) {
            log.error("创建菜单参数错误: {}", e.getMessage(), e);
            return Result.error(ResultCode.PARAM_ERROR, e.getMessage());
        } catch (Exception e) {
            log.error("创建菜单异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "创建菜单异常: " + e.getMessage());
        }
    }

    /**
     * 更新菜单
     */
    @Operation(summary = "更新菜单")
    @PutMapping("/{id}")
    public Result<MenuVO> updateMenu(
            @Parameter(description = "菜单ID", required = true) @PathVariable Long id,
            @Valid @RequestBody MenuDTO menuDTO) {
        log.info("更新菜单请求: id={}, menuCode={}", id, menuDTO.getMenuCode());

        try {
            // 先查询现有菜单
            Menu menu = menuService.getMenuById(id);
            if (menu == null) {
                return Result.error(ResultCode.PARAM_ERROR, "菜单不存在");
            }

            // 更新字段
            BeanUtils.copyProperties(menuDTO, menu);
            menu.setId(id);

            // 调用服务更新
            boolean success = menuService.updateMenu(menu);
            if (!success) {
                return Result.error(ResultCode.SYSTEM_ERROR, "更新菜单失败");
            }

            // 重新查询获取最新数据
            Menu updatedMenu = menuService.getMenuById(id);
            MenuVO menuVO = convertToVO(updatedMenu);
            return Result.success(menuVO, "更新菜单成功");
        } catch (IllegalArgumentException e) {
            log.error("更新菜单参数错误: {}", e.getMessage(), e);
            return Result.error(ResultCode.PARAM_ERROR, e.getMessage());
        } catch (Exception e) {
            log.error("更新菜单异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "更新菜单异常: " + e.getMessage());
        }
    }

    /**
     * 删除菜单（逻辑删除）
     */
    @Operation(summary = "删除菜单")
    @DeleteMapping("/{id}")
    public Result<Void> deleteMenu(@Parameter(description = "菜单ID", required = true) @PathVariable Long id) {
        log.info("删除菜单请求: id={}", id);

        try {
            boolean success = menuService.deleteMenu(id);
            if (!success) {
                return Result.error(ResultCode.SYSTEM_ERROR, "删除菜单失败");
            }
            return Result.success("删除菜单成功");
        } catch (IllegalArgumentException e) {
            log.error("删除菜单参数错误: {}", e.getMessage(), e);
            return Result.error(ResultCode.PARAM_ERROR, e.getMessage());
        } catch (Exception e) {
            log.error("删除菜单异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "删除菜单异常: " + e.getMessage());
        }
    }

    /**
     * 批量删除菜单
     */
    @Operation(summary = "批量删除菜单")
    @DeleteMapping("/batch")
    public Result<Void> batchDeleteMenus(@Parameter(description = "菜单ID列表", required = true) @RequestBody List<Long> ids) {
        log.info("批量删除菜单请求: ids={}", ids);

        try {
            boolean success = menuService.batchDeleteMenus(ids);
            if (!success) {
                return Result.error(ResultCode.SYSTEM_ERROR, "批量删除菜单失败");
            }
            return Result.success("批量删除菜单成功");
        } catch (Exception e) {
            log.error("批量删除菜单异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "批量删除菜单异常: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取菜单详情
     */
    @Operation(summary = "获取菜单详情")
    @GetMapping("/{id}")
    public Result<MenuVO> getMenuById(@Parameter(description = "菜单ID", required = true) @PathVariable Long id) {
        log.info("获取菜单详情请求: id={}", id);

        try {
            Menu menu = menuService.getMenuById(id);
            if (menu == null) {
                return Result.error(ResultCode.PARAM_ERROR, "菜单不存在");
            }

            MenuVO menuVO = convertToVO(menu);
            return Result.success(menuVO);
        } catch (Exception e) {
            log.error("获取菜单详情异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "获取菜单详情异常: " + e.getMessage());
        }
    }

    /**
     * 根据菜单代码获取菜单详情
     */
    @Operation(summary = "根据菜单代码获取菜单详情")
    @GetMapping("/code/{menuCode}")
    public Result<MenuVO> getMenuByCode(@Parameter(description = "菜单代码", required = true) @PathVariable String menuCode) {
        log.info("根据菜单代码获取菜单详情请求: menuCode={}", menuCode);

        try {
            Menu menu = menuService.getMenuByCode(menuCode);
            if (menu == null) {
                return Result.error(ResultCode.PARAM_ERROR, "菜单不存在");
            }

            MenuVO menuVO = convertToVO(menu);
            return Result.success(menuVO);
        } catch (Exception e) {
            log.error("根据菜单代码获取菜单详情异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "根据菜单代码获取菜单详情异常: " + e.getMessage());
        }
    }

    /**
     * 获取菜单列表（分页）
     */
    @Operation(summary = "获取菜单列表（分页）")
    @GetMapping
    public Result<IPage<MenuVO>> getMenuList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "父菜单ID") @RequestParam(required = false) Long parentId,
            @Parameter(description = "菜单名称（模糊查询）") @RequestParam(required = false) String menuName,
            @Parameter(description = "菜单代码（模糊查询）") @RequestParam(required = false) String menuCode,
            @Parameter(description = "菜单类型（1-目录，2-菜单，3-按钮）") @RequestParam(required = false) Integer menuType,
            @Parameter(description = "状态（0-禁用，1-启用）") @RequestParam(required = false) Integer status,
            @Parameter(description = "排序字段") @RequestParam(required = false) String orderBy) {
        log.info("获取菜单列表请求: page={}, size={}, parentId={}, menuName={}, menuCode={}, menuType={}, status={}",
                page, size, parentId, menuName, menuCode, menuType, status);

        try {
            IPage<Menu> menuPage = menuService.getMenuList(page, size, parentId, menuName, menuCode, menuType, status, orderBy);
            IPage<MenuVO> menuVOPage = menuPage.convert(this::convertToVO);
            return Result.success(menuVOPage);
        } catch (Exception e) {
            log.error("获取菜单列表异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "获取菜单列表异常: " + e.getMessage());
        }
    }

    /**
     * 获取所有启用的菜单列表
     */
    @Operation(summary = "获取所有启用的菜单列表")
    @GetMapping("/enabled")
    public Result<List<MenuVO>> getAllEnabledMenus() {
        log.info("获取所有启用的菜单列表请求");

        try {
            List<Menu> menus = menuService.getAllEnabledMenus();
            List<MenuVO> menuVOs = menus.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            return Result.success(menuVOs);
        } catch (Exception e) {
            log.error("获取所有启用的菜单列表异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "获取所有启用的菜单列表异常: " + e.getMessage());
        }
    }

    /**
     * 获取菜单树形列表（根据父ID）
     */
    @Operation(summary = "获取菜单树形列表")
    @GetMapping("/tree")
    public Result<List<MenuVO>> getMenuTreeByParentId(
            @Parameter(description = "父菜单ID（0表示根菜单）") @RequestParam(defaultValue = "0") Long parentId) {
        log.info("获取菜单树形列表请求: parentId={}", parentId);

        try {
            List<Menu> menus = menuService.getMenuTreeByParentId(parentId);
            List<MenuVO> menuVOs = menus.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            return Result.success(menuVOs);
        } catch (Exception e) {
            log.error("获取菜单树形列表异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "获取菜单树形列表异常: " + e.getMessage());
        }
    }

    /**
     * 获取用户有权限的菜单树形列表
     */
    @Operation(summary = "获取用户有权限的菜单树形列表")
    @GetMapping("/user/{userId}/tree")
    public Result<List<MenuVO>> getMenuTreeByUserId(@Parameter(description = "用户ID", required = true) @PathVariable Long userId) {
        log.info("获取用户有权限的菜单树形列表请求: userId={}", userId);

        try {
            List<Menu> menus = menuService.getMenuTreeByUserId(userId);
            List<MenuVO> menuVOs = menus.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            return Result.success(menuVOs);
        } catch (Exception e) {
            log.error("获取用户有权限的菜单树形列表异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "获取用户有权限的菜单树形列表异常: " + e.getMessage());
        }
    }

    /**
     * 更新菜单状态
     */
    @Operation(summary = "更新菜单状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateMenuStatus(
            @Parameter(description = "菜单ID", required = true) @PathVariable Long id,
            @Parameter(description = "状态（0-禁用，1-启用）", required = true) @RequestParam Integer status) {
        log.info("更新菜单状态请求: id={}, status={}", id, status);

        try {
            boolean success = menuService.updateMenuStatus(id, status);
            if (!success) {
                return Result.error(ResultCode.SYSTEM_ERROR, "更新菜单状态失败");
            }
            return Result.success("更新菜单状态成功");
        } catch (IllegalArgumentException e) {
            log.error("更新菜单状态参数错误: {}", e.getMessage(), e);
            return Result.error(ResultCode.PARAM_ERROR, e.getMessage());
        } catch (Exception e) {
            log.error("更新菜单状态异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "更新菜单状态异常: " + e.getMessage());
        }
    }

    /**
     * 批量更新菜单状态
     */
    @Operation(summary = "批量更新菜单状态")
    @PutMapping("/batch/status")
    public Result<Void> batchUpdateMenuStatus(
            @Parameter(description = "菜单ID列表", required = true) @RequestBody List<Long> ids,
            @Parameter(description = "状态（0-禁用，1-启用）", required = true) @RequestParam Integer status) {
        log.info("批量更新菜单状态请求: ids={}, status={}", ids, status);

        try {
            boolean success = menuService.batchUpdateMenuStatus(ids, status);
            if (!success) {
                return Result.error(ResultCode.SYSTEM_ERROR, "批量更新菜单状态失败");
            }
            return Result.success("批量更新菜单状态成功");
        } catch (Exception e) {
            log.error("批量更新菜单状态异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "批量更新菜单状态异常: " + e.getMessage());
        }
    }

    /**
     * 检查菜单代码是否已存在
     */
    @Operation(summary = "检查菜单代码是否已存在")
    @GetMapping("/check-menu-code")
    public Result<Boolean> checkMenuCodeExists(
            @Parameter(description = "菜单代码", required = true) @RequestParam String menuCode) {
        log.info("检查菜单代码是否已存在请求: menuCode={}", menuCode);

        try {
            boolean exists = menuService.isMenuCodeExists(menuCode);
            return Result.success(exists);
        } catch (Exception e) {
            log.error("检查菜单代码是否已存在异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "检查菜单代码是否已存在异常: " + e.getMessage());
        }
    }

    /**
     * 检查权限标识是否已存在
     */
    @Operation(summary = "检查权限标识是否已存在")
    @GetMapping("/check-perms")
    public Result<Boolean> checkPermsExists(
            @Parameter(description = "权限标识", required = true) @RequestParam String perms) {
        log.info("检查权限标识是否已存在请求: perms={}", perms);

        try {
            boolean exists = menuService.isPermsExists(perms);
            return Result.success(exists);
        } catch (Exception e) {
            log.error("检查权限标识是否已存在异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "检查权限标识是否已存在异常: " + e.getMessage());
        }
    }

    /**
     * 获取当前登录用户有权限的菜单树形列表
     */
    @Operation(summary = "获取当前用户有权限的菜单树形列表")
    @GetMapping("/current/tree")
    public Result<List<MenuVO>> getCurrentUserMenuTree() {
        log.info("获取当前用户有权限的菜单树形列表请求");

        try {
            Long userId = SecurityUtil.getCurrentUserId();
            if (userId == null) {
                return Result.error(ResultCode.UNAUTHORIZED, "用户未登录或登录已过期");
            }

            List<Menu> menus = menuService.getMenuTreeByUserId(userId);
            List<MenuVO> menuVOs = menus.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            return Result.success(menuVOs);
        } catch (Exception e) {
            log.error("获取当前用户有权限的菜单树形列表异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "获取当前用户有权限的菜单树形列表异常: " + e.getMessage());
        }
    }

    /**
     * 获取当前登录用户的权限标识列表
     */
    @Operation(summary = "获取当前用户的权限标识列表")
    @GetMapping("/current/permissions")
    public Result<List<String>> getCurrentUserPermissions() {
        log.info("获取当前用户的权限标识列表请求");

        try {
            Long userId = SecurityUtil.getCurrentUserId();
            if (userId == null) {
                return Result.error(ResultCode.UNAUTHORIZED, "用户未登录或登录已过期");
            }

            List<String> perms = roleMenuService.getPermsByUserId(userId);
            return Result.success(perms);
        } catch (Exception e) {
            log.error("获取当前用户的权限标识列表异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "获取当前用户的权限标识列表异常: " + e.getMessage());
        }
    }

    /**
     * 将Menu实体转换为MenuVO（递归处理子菜单）
     */
    private MenuVO convertToVO(Menu menu) {
        if (menu == null) {
            return null;
        }
        MenuVO menuVO = new MenuVO();
        BeanUtils.copyProperties(menu, menuVO);
        // 递归转换子菜单
        if (menu.getChildren() != null && !menu.getChildren().isEmpty()) {
            List<MenuVO> childrenVO = menu.getChildren().stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            menuVO.setChildren(childrenVO);
        }
        return menuVO;
    }

    /**
     * 检查当前用户是否拥有指定权限
     */
    @Operation(summary = "检查当前用户是否拥有指定权限")
    @GetMapping("/current/check-permission")
    public Result<Boolean> checkCurrentUserPermission(
            @Parameter(description = "权限标识", required = true) @RequestParam String permission) {
        log.info("检查当前用户是否拥有指定权限请求: permission={}", permission);

        try {
            boolean hasPermission = SecurityUtil.hasPermission(permission);
            return Result.success(hasPermission);
        } catch (Exception e) {
            log.error("检查当前用户是否拥有指定权限异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "检查当前用户是否拥有指定权限异常: " + e.getMessage());
        }
    }

    /**
     * 检查当前用户是否拥有任意指定权限
     */
    @Operation(summary = "检查当前用户是否拥有任意指定权限")
    @PostMapping("/current/check-any-permission")
    public Result<Boolean> checkCurrentUserAnyPermission(
            @Parameter(description = "权限标识列表", required = true) @RequestBody List<String> permissions) {
        log.info("检查当前用户是否拥有任意指定权限请求: permissions={}", permissions);

        try {
            boolean hasAnyPermission = SecurityUtil.hasAnyPermission(permissions.toArray(new String[0]));
            return Result.success(hasAnyPermission);
        } catch (Exception e) {
            log.error("检查当前用户是否拥有任意指定权限异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR, "检查当前用户是否拥有任意指定权限异常: " + e.getMessage());
        }
    }
}
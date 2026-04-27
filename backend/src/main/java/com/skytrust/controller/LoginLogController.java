package com.skytrust.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.skytrust.common.Result;
import com.skytrust.common.utils.SecurityUtil;
import com.skytrust.entity.LoginLog;
import com.skytrust.enums.LoginLogTypeEnum;
import com.skytrust.service.LoginLogService;
import com.skytrust.vo.LoginLogVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 登录日志控制器
 *
 * @author SkyTrust Team
 */
@Slf4j
@Tag(name = "登录日志管理", description = "登录日志查询、清理等接口")
@RestController
@RequestMapping("/api/login-logs")
@RequiredArgsConstructor
public class LoginLogController {

    private final LoginLogService loginLogService;

    /**
     * 分页查询登录日志（管理员）
     */
    @Operation(summary = "分页查询登录日志")
    @GetMapping
    public Result<IPage<LoginLogVO>> pageLoginLogs(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "用户名（模糊查询）") @RequestParam(required = false) String username,
            @Parameter(description = "日志类型") @RequestParam(required = false) Integer logType,
            @Parameter(description = "开始时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        // 权限检查：仅管理员可查看所有日志
        if (!SecurityUtil.isAdmin()) {
            return Result.error(com.skytrust.common.ResultCode.FORBIDDEN, "权限不足");
        }

        IPage<LoginLog> logPage = loginLogService.pageLoginLogs(page, size, username, logType, startTime, endTime);
        IPage<LoginLogVO> voPage = logPage.convert(this::convertToVO);
        return Result.success(voPage);
    }

    /**
     * 获取当前用户的登录日志
     */
    @Operation(summary = "获取当前用户的登录日志")
    @GetMapping("/my")
    public Result<List<LoginLogVO>> getMyLoginLogs(
            @Parameter(description = "条数限制") @RequestParam(defaultValue = "10") int limit) {
        String currentUsername = SecurityUtil.getCurrentUsername();
        if (currentUsername == null) {
            return Result.error(com.skytrust.common.ResultCode.USER_NOT_LOGIN);
        }

        List<LoginLog> logs = loginLogService.getRecentByUserId(
                SecurityUtil.getCurrentUserId(), Math.min(limit, 50));
        List<LoginLogVO> voList = logs.stream().map(this::convertToVO).collect(Collectors.toList());
        return Result.success(voList);
    }

    /**
     * 清理指定天数前的日志（管理员）
     */
    @Operation(summary = "清理登录日志")
    @DeleteMapping("/clean")
    public Result<Integer> cleanLogs(
            @Parameter(description = "保留天数", required = true) @RequestParam(defaultValue = "90") int days) {
        if (!SecurityUtil.isAdmin()) {
            return Result.error(com.skytrust.common.ResultCode.FORBIDDEN, "权限不足");
        }
        if (days < 7) {
            return Result.error("最少保留7天的日志");
        }

        LocalDateTime beforeTime = LocalDateTime.now().minusDays(days);
        int count = loginLogService.cleanLogsBefore(beforeTime);
        return Result.success(count, "已清理" + count + "条日志");
    }

    /**
     * 转换为VO
     */
    private LoginLogVO convertToVO(LoginLog loginLog) {
        if (loginLog == null) return null;
        LoginLogVO vo = new LoginLogVO();
        BeanUtils.copyProperties(loginLog, vo);
        vo.setLogTypeDesc(LoginLogTypeEnum.getDescriptionByCode(loginLog.getLogType()));
        return vo;
    }
}

package com.skytrust.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.skytrust.common.utils.StringUtil;
import com.skytrust.entity.LoginLog;
import com.skytrust.enums.LoginLogTypeEnum;
import com.skytrust.mapper.LoginLogMapper;
import com.skytrust.service.LoginLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 登录日志服务实现类
 *
 * @author SkyTrust Team
 */
@Slf4j
@Service
public class LoginLogServiceImpl extends BaseService<LoginLogMapper, LoginLog> implements LoginLogService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordLogin(Long userId, String username, Integer logType,
                            String ipAddress, String userAgent, String failReason) {
        try {
            LoginLog loginLog = new LoginLog();
            loginLog.setUserId(userId);
            loginLog.setUsername(username);
            loginLog.setLogType(logType);
            loginLog.setIpAddress(ipAddress);
            loginLog.setUserAgent(userAgent);
            loginLog.setLoginTime(LocalDateTime.now());
            loginLog.setFailReason(failReason);

            // 解析设备信息（简单地从UserAgent截取）
            if (userAgent != null && !userAgent.isEmpty()) {
                loginLog.setDeviceInfo(parseDeviceInfo(userAgent));
            }

            save(loginLog);
            log.debug("登录日志已记录: username={}, type={}, ip={}",
                    username, LoginLogTypeEnum.getDescriptionByCode(logType), ipAddress);
        } catch (Exception e) {
            log.error("记录登录日志失败: {}", e.getMessage());
            // 日志记录失败不应影响主流程
        }
    }

    @Override
    public IPage<LoginLog> pageLoginLogs(Integer page, Integer size,
                                         String username, Integer logType,
                                         LocalDateTime startTime, LocalDateTime endTime) {
        Page<LoginLog> pageObj = getPage(page, size);
        LambdaQueryWrapper<LoginLog> wrapper = new LambdaQueryWrapper<>();

        if (StringUtil.isNotEmpty(username)) {
            wrapper.like(LoginLog::getUsername, username);
        }
        if (logType != null) {
            wrapper.eq(LoginLog::getLogType, logType);
        }
        if (startTime != null) {
            wrapper.ge(LoginLog::getLoginTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(LoginLog::getLoginTime, endTime);
        }

        // 按登录时间降序排列
        wrapper.orderByDesc(LoginLog::getLoginTime);

        return super.page(pageObj, wrapper);
    }

    @Override
    public List<LoginLog> getRecentByUserId(Long userId, int limit) {
        LambdaQueryWrapper<LoginLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LoginLog::getUserId, userId);
        wrapper.orderByDesc(LoginLog::getLoginTime);
        wrapper.last("LIMIT " + limit);
        return super.list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cleanLogsBefore(LocalDateTime beforeTime) {
        LambdaQueryWrapper<LoginLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(LoginLog::getLoginTime, beforeTime);
        int count = (int) super.count(wrapper);
        if (count > 0) {
            super.remove(wrapper);
            log.info("已清理 {} 条 {} 之前的登录日志", count, beforeTime);
        }
        return count;
    }

    /**
     * 从UserAgent解析简单设备信息
     */
    private String parseDeviceInfo(String userAgent) {
        String ua = userAgent.toLowerCase();
        if (ua.contains("mobile") || ua.contains("android") || ua.contains("iphone")) {
            if (ua.contains("android")) return "Android";
            if (ua.contains("iphone")) return "iPhone";
            if (ua.contains("ipad")) return "iPad";
            return "移动端";
        }
        if (ua.contains("windows")) return "Windows";
        if (ua.contains("macintosh") || ua.contains("mac os")) return "macOS";
        if (ua.contains("linux")) return "Linux";
        return "未知";
    }
}

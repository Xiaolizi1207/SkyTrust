package com.skytrust.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.skytrust.entity.LoginLog;

import java.time.LocalDateTime;

/**
 * 登录日志服务接口
 *
 * @author SkyTrust Team
 */
public interface LoginLogService extends IService<LoginLog> {

    /**
     * 记录登录日志
     *
     * @param userId     用户ID
     * @param username   用户名
     * @param logType    日志类型（0-登录成功，1-登录失败，2-登录锁定，3-退出登录）
     * @param ipAddress  IP地址
     * @param userAgent  用户代理
     * @param failReason 失败原因（成功时为null）
     */
    void recordLogin(Long userId, String username, Integer logType,
                     String ipAddress, String userAgent, String failReason);

    /**
     * 分页查询登录日志
     *
     * @param page     页码
     * @param size     每页大小
     * @param username 用户名（模糊查询）
     * @param logType  日志类型
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 分页结果
     */
    IPage<LoginLog> pageLoginLogs(Integer page, Integer size,
                                  String username, Integer logType,
                                  LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查询用户最近登录日志
     *
     * @param userId 用户ID
     * @param limit  限制条数
     * @return 登录日志列表
     */
    java.util.List<LoginLog> getRecentByUserId(Long userId, int limit);

    /**
     * 清理指定时间之前的日志
     *
     * @param beforeTime 时间点
     * @return 删除条数
     */
    int cleanLogsBefore(LocalDateTime beforeTime);
}

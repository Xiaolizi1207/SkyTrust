package com.skytrust.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.skytrust.entity.User;
import com.skytrust.common.Result;

import java.util.List;

/**
 * 用户服务接口
 *
 * @author SkyTrust Team
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param user 用户信息
     * @return 注册结果
     */
    Result<User> register(User user);

    /**
     * 用户登录
     *
     * @param username 用户名
     * @param password 密码
     * @return 登录结果（包含用户信息和token）
     */
    Result<User> login(String username, String password);

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    User getUserByUsername(String username);

    /**
     * 根据手机号查询用户
     *
     * @param phone 手机号
     * @return 用户信息
     */
    User getUserByPhone(String phone);

    /**
     * 根据邮箱查询用户
     *
     * @param email 邮箱
     * @return 用户信息
     */
    User getUserByEmail(String email);

    /**
     * 更新用户密码
     *
     * @param userId      用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否成功
     */
    boolean updatePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 重置用户密码（管理员操作）
     *
     * @param userId      用户ID
     * @param newPassword 新密码
     * @return 是否成功
     */
    boolean resetPassword(Long userId, String newPassword);

    /**
     * 更新用户状态
     *
     * @param userId 用户ID
     * @param status 状态（0-禁用，1-启用）
     * @return 是否成功
     */
    boolean updateStatus(Long userId, Integer status);

    /**
     * 更新用户角色
     *
     * @param userId 用户ID
     * @param role   角色（admin-管理员，user-普通用户，pilot-飞行员）
     * @return 是否成功
     */
    boolean updateRole(Long userId, String role);

    /**
     * 更新信用评分
     *
     * @param userId      用户ID
     * @param creditScore 信用评分（0-100）
     * @return 是否成功
     */
    boolean updateCreditScore(Long userId, Integer creditScore);

    /**
     * 更新最后登录时间
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean updateLastLoginTime(Long userId);

    /**
     * 根据角色查询用户
     *
     * @param role 角色
     * @return 用户列表
     */
    List<User> getUsersByRole(String role);

    /**
     * 根据状态查询用户
     *
     * @param status 状态（0-禁用，1-启用）
     * @return 用户列表
     */
    List<User> getUsersByStatus(Integer status);

    /**
     * 查询用户列表（带分页和条件）
     *
     * @param page     当前页
     * @param size     每页大小
     * @param username 用户名（模糊查询）
     * @param phone    手机号（模糊查询）
     * @param realName 真实姓名（模糊查询）
     * @param role     角色
     * @param status   状态
     * @param orderBy  排序字段
     * @return 分页结果
     */
    IPage<User> pageUsers(Integer page, Integer size,
                         String username, String phone, String realName,
                         String role, Integer status, String orderBy);

    /**
     * 检查用户名是否已存在
     *
     * @param username 用户名
     * @return 是否存在
     */
    boolean isUsernameExists(String username);

    /**
     * 检查手机号是否已存在
     *
     * @param phone 手机号
     * @return 是否存在
     */
    boolean isPhoneExists(String phone);

    /**
     * 检查邮箱是否已存在
     *
     * @param email 邮箱
     * @return 是否存在
     */
    boolean isEmailExists(String email);

    /**
     * 批量更新用户状态
     *
     * @param userIds 用户ID列表
     * @param status  状态
     * @return 是否成功
     */
    boolean batchUpdateStatus(List<Long> userIds, Integer status);

    /**
     * 批量更新用户角色
     *
     * @param userIds 用户ID列表
     * @param role    角色
     * @return 是否成功
     */
    boolean batchUpdateRole(List<Long> userIds, String role);

    /**
     * 导出用户数据
     *
     * @param users 用户列表
     * @return 导出文件的字节数组
     */
    byte[] exportUsers(List<User> users);

    /**
     * 导入用户数据
     *
     * @param fileBytes 文件字节数组
     * @return 导入结果
     */
    Result<String> importUsers(byte[] fileBytes);
}
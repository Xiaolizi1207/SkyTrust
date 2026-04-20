package com.skytrust.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.skytrust.common.Result;
import com.skytrust.common.ResultCode;
import com.skytrust.common.utils.DateUtil;
import com.skytrust.common.utils.SecurityUtil;
import com.skytrust.common.utils.StringUtil;
import com.skytrust.entity.User;
import com.skytrust.exception.BusinessException;
import com.skytrust.mapper.UserMapper;
import com.skytrust.service.UserService;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户服务实现类
 *
 * @author SkyTrust Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends BaseService<UserMapper, User> implements UserService {

    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<User> register(User user) {
        log.info("用户注册: {}", user.getUsername());

        // 1. 验证必填字段
        if (StringUtil.isEmpty(user.getUsername())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "用户名不能为空");
        }
        if (StringUtil.isEmpty(user.getPassword())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "密码不能为空");
        }
        if (StringUtil.isEmpty(user.getPhone())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "手机号不能为空");
        }

        // 2. 检查用户名、手机号、邮箱是否已存在
        if (isUsernameExists(user.getUsername())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "用户名已存在");
        }
        if (isPhoneExists(user.getPhone())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "手机号已存在");
        }
        if (StringUtil.isNotEmpty(user.getEmail()) && isEmailExists(user.getEmail())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "邮箱已存在");
        }

        // 3. 设置默认值
        user.setId(null); // 确保ID为null，由数据库自增
        user.setPassword(passwordEncoder.encode(user.getPassword())); // 密码加密
        if (user.getStatus() == null) {
            user.setStatus(1); // 默认启用
        }
        if (StringUtil.isEmpty(user.getRole())) {
            user.setRole("user"); // 默认普通用户
        }
        if (user.getCreditScore() == null) {
            user.setCreditScore(80); // 默认信用评分
        }
        user.setLastLoginTime(null); // 注册时没有最后登录时间

        // 4. 保存用户
        boolean success = super.save(user);
        if (!success) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "用户注册失败");
        }

        log.info("用户注册成功: {}, ID: {}", user.getUsername(), user.getId());
        return Result.success(user, "注册成功");
    }

    @Override
    public Result<User> login(String username, String password) {
        log.info("用户登录: {}", username);

        // 1. 参数验证
        if (StringUtil.isEmpty(username) || StringUtil.isEmpty(password)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "用户名或密码不能为空");
        }

        // 2. 查询用户
        User user = getUserByUsername(username);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST, "用户不存在");
        }

        // 3. 检查用户状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ResultCode.USER_DISABLED, "用户已被禁用");
        }

        // 4. 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR, "密码错误");
        }

        // 5. 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        boolean updated = updateById(user);
        if (!updated) {
            log.warn("更新用户最后登录时间失败: {}", user.getId());
        }

        log.info("用户登录成功: {}", username);
        return Result.success(user, "登录成功");
    }

    @Override
    public User getUserByUsername(String username) {
        if (StringUtil.isEmpty(username)) {
            return null;
        }
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return super.getOne(wrapper);
    }

    @Override
    public User getUserByPhone(String phone) {
        if (StringUtil.isEmpty(phone)) {
            return null;
        }
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone);
        return super.getOne(wrapper);
    }

    @Override
    public User getUserByEmail(String email) {
        if (StringUtil.isEmpty(email)) {
            return null;
        }
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, email);
        return super.getOne(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePassword(Long userId, String oldPassword, String newPassword) {
        log.info("更新用户密码: {}", userId);

        // 1. 参数验证
        if (userId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "用户ID不能为空");
        }
        if (StringUtil.isEmpty(oldPassword) || StringUtil.isEmpty(newPassword)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "旧密码和新密码不能为空");
        }

        // 2. 查询用户
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST, "用户不存在");
        }

        // 3. 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR, "旧密码错误");
        }

        // 4. 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        return updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean resetPassword(Long userId, String newPassword) {
        log.info("重置用户密码: {}", userId);

        if (userId == null || StringUtil.isEmpty(newPassword)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "参数错误");
        }

        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST, "用户不存在");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        return updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Long userId, Integer status) {
        log.info("更新用户状态: {}, status: {}", userId, status);

        if (userId == null || status == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "参数错误");
        }
        if (status != 0 && status != 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "状态值无效 (0-禁用, 1-启用)");
        }

        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST, "用户不存在");
        }

        user.setStatus(status);
        return updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRole(Long userId, String role) {
        log.info("更新用户角色: {}, role: {}", userId, role);

        if (userId == null || StringUtil.isEmpty(role)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "参数错误");
        }

        // 验证角色是否有效
        if (!"admin".equals(role) && !"user".equals(role) && !"pilot".equals(role)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "角色无效 (admin/user/pilot)");
        }

        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST, "用户不存在");
        }

        user.setRole(role);
        return updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateCreditScore(Long userId, Integer creditScore) {
        log.info("更新用户信用评分: {}, score: {}", userId, creditScore);

        if (userId == null || creditScore == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "参数错误");
        }
        if (creditScore < 0 || creditScore > 100) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "信用评分范围 0-100");
        }

        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST, "用户不存在");
        }

        user.setCreditScore(creditScore);
        return updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateLastLoginTime(Long userId) {
        if (userId == null) {
            return false;
        }

        User user = getById(userId);
        if (user == null) {
            return false;
        }

        user.setLastLoginTime(LocalDateTime.now());
        return updateById(user);
    }

    @Override
    public List<User> getUsersByRole(String role) {
        if (StringUtil.isEmpty(role)) {
            return listAll();
        }

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getRole, role);
        return super.list(wrapper);
    }

    @Override
    public List<User> getUsersByStatus(Integer status) {
        if (status == null) {
            return listAll();
        }

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getStatus, status);
        return super.list(wrapper);
    }

    @Override
    public IPage<User> pageUsers(Integer page, Integer size,
                                String username, String phone, String realName,
                                String role, Integer status, String orderBy) {
        Page<User> pageObj = getPage(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        if (StringUtil.isNotEmpty(username)) {
            wrapper.like(User::getUsername, username);
        }
        if (StringUtil.isNotEmpty(phone)) {
            wrapper.like(User::getPhone, phone);
        }
        if (StringUtil.isNotEmpty(realName)) {
            wrapper.like(User::getRealName, realName);
        }
        if (StringUtil.isNotEmpty(role)) {
            wrapper.eq(User::getRole, role);
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }

        applyOrderBy(wrapper, orderBy);
        return super.page(pageObj, wrapper);
    }

    @Override
    public boolean isUsernameExists(String username) {
        if (StringUtil.isEmpty(username)) {
            return false;
        }
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return super.count(wrapper) > 0;
    }

    @Override
    public boolean isPhoneExists(String phone) {
        if (StringUtil.isEmpty(phone)) {
            return false;
        }
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone);
        return super.count(wrapper) > 0;
    }

    @Override
    public boolean isEmailExists(String email) {
        if (StringUtil.isEmpty(email)) {
            return false;
        }
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, email);
        return super.count(wrapper) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchUpdateStatus(List<Long> userIds, Integer status) {
        log.info("批量更新用户状态: {}, status: {}", userIds, status);

        if (userIds == null || userIds.isEmpty() || status == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "参数错误");
        }
        if (status != 0 && status != 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "状态值无效 (0-禁用, 1-启用)");
        }

        boolean success = true;
        for (Long userId : userIds) {
            if (!updateStatus(userId, status)) {
                success = false;
                log.error("更新用户状态失败: {}", userId);
            }
        }
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchUpdateRole(List<Long> userIds, String role) {
        log.info("批量更新用户角色: {}, role: {}", userIds, role);

        if (userIds == null || userIds.isEmpty() || StringUtil.isEmpty(role)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "参数错误");
        }

        // 验证角色是否有效
        if (!"admin".equals(role) && !"user".equals(role) && !"pilot".equals(role)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "角色无效 (admin/user/pilot)");
        }

        boolean success = true;
        for (Long userId : userIds) {
            if (!updateRole(userId, role)) {
                success = false;
                log.error("更新用户角色失败: {}", userId);
            }
        }
        return success;
    }

    @Override
    public byte[] exportUsers(List<User> users) {
        // TODO: 实现用户数据导出功能（如导出为Excel、CSV等）
        log.warn("用户导出功能未实现");
        return new byte[0];
    }

    @Override
    public Result<String> importUsers(byte[] fileBytes) {
        // TODO: 实现用户数据导入功能（如从Excel、CSV导入）
        log.warn("用户导入功能未实现");
        return Result.error(ResultCode.SYSTEM_ERROR, "导入功能未实现");
    }

    @Override
    public String validate(User entity) {
        // 调用父类验证
        String parentValidation = super.validate(entity);
        if (parentValidation != null) {
            return parentValidation;
        }

        // 自定义用户验证逻辑
        if (StringUtil.isNotEmpty(entity.getUsername())) {
            if (entity.getUsername().length() < 3 || entity.getUsername().length() > 50) {
                return "用户名长度需在3-50个字符之间";
            }
            if (!entity.getUsername().matches("^[a-zA-Z0-9_]{3,50}$")) {
                return "用户名只能包含字母、数字和下划线";
            }
        }

        if (StringUtil.isNotEmpty(entity.getPhone())) {
            if (!entity.getPhone().matches("^1[3-9]\\d{9}$")) {
                return "手机号格式不正确";
            }
        }

        if (StringUtil.isNotEmpty(entity.getEmail())) {
            if (!entity.getEmail().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                return "邮箱格式不正确";
            }
        }

        if (StringUtil.isNotEmpty(entity.getRole())) {
            if (!"admin".equals(entity.getRole()) && !"user".equals(entity.getRole()) && !"pilot".equals(entity.getRole())) {
                return "角色无效 (admin/user/pilot)";
            }
        }

        if (entity.getStatus() != null) {
            if (entity.getStatus() != 0 && entity.getStatus() != 1) {
                return "状态值无效 (0-禁用, 1-启用)";
            }
        }

        if (entity.getCreditScore() != null) {
            if (entity.getCreditScore() < 0 || entity.getCreditScore() > 100) {
                return "信用评分范围 0-100";
            }
        }

        return null; // 验证通过
    }
}
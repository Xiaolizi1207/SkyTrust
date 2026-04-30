package com.skytrust.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.skytrust.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper接口
 *
 * @author SkyTrust Team
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 通过微信 openid 查找用户（openid 存储在 remark 字段中）
     * 临时方案：使用 LIKE 查询 remark 字段；建议后续在 User 表增加 wechat_openid 字段
     *
     * @param openid 微信 openid
     * @return 用户信息，未找到返回 null
     */
    default User selectByOpenid(String openid) {
        if (openid == null || openid.trim().isEmpty()) {
            return null;
        }
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(User::getRemark, "openid:" + openid);
        return selectOne(wrapper);
    }
}
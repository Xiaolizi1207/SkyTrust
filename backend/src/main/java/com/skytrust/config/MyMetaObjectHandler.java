package com.skytrust.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus字段自动填充处理器
 * 用于自动填充创建时间、更新时间等公共字段
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    /**
     * 创建时间字段名
     */
    private static final String CREATE_TIME_FIELD = "createTime";

    /**
     * 更新时间字段名
     */
    private static final String UPDATE_TIME_FIELD = "updateTime";

    /**
     * 创建人字段名（如果有）
     */
    private static final String CREATE_BY_FIELD = "createBy";

    /**
     * 更新人字段名（如果有）
     */
    private static final String UPDATE_BY_FIELD = "updateBy";

    /**
     * 插入时自动填充字段
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        // 自动填充创建时间
        if (metaObject.hasSetter(CREATE_TIME_FIELD)) {
            this.strictInsertFill(metaObject, CREATE_TIME_FIELD, LocalDateTime.class, LocalDateTime.now());
        }

        // 自动填充更新时间（插入时通常与创建时间相同）
        if (metaObject.hasSetter(UPDATE_TIME_FIELD)) {
            this.strictInsertFill(metaObject, UPDATE_TIME_FIELD, LocalDateTime.class, LocalDateTime.now());
        }

        // 自动填充创建人（如果有用户上下文，可以从SecurityContext中获取）
        // 这里暂时不填充，实际项目中可以获取当前登录用户ID
        // if (metaObject.hasSetter(CREATE_BY_FIELD)) {
        //     Long currentUserId = getCurrentUserId();
        //     this.strictInsertFill(metaObject, CREATE_BY_FIELD, Long.class, currentUserId);
        // }
    }

    /**
     * 更新时自动填充字段
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        // 自动填充更新时间
        if (metaObject.hasSetter(UPDATE_TIME_FIELD)) {
            this.strictUpdateFill(metaObject, UPDATE_TIME_FIELD, LocalDateTime.class, LocalDateTime.now());
        }

        // 自动填充更新人（如果有用户上下文，可以从SecurityContext中获取）
        // if (metaObject.hasSetter(UPDATE_BY_FIELD)) {
        //     Long currentUserId = getCurrentUserId();
        //     this.strictUpdateFill(metaObject, UPDATE_BY_FIELD, Long.class, currentUserId);
        // }
    }

    /**
     * 获取当前登录用户ID（示例方法，实际项目中需要实现）
     */
    private Long getCurrentUserId() {
        // TODO: 从SecurityContext中获取当前登录用户ID
        // Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // if (authentication != null && authentication.isAuthenticated()) {
        //     UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        //     return Long.valueOf(userDetails.getUsername()); // 假设用户ID是username
        // }
        return null;
    }
}
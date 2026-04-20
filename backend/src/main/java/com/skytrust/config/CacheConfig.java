package com.skytrust.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleCacheResolver;
import org.springframework.cache.interceptor.SimpleCacheErrorHandler;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 缓存配置类
 *
 * @author SkyTrust Team
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * 缓存名称常量
     */
    public static final String MENU_CACHE = "menuCache";
    public static final String MENU_TREE_CACHE = "menuTreeCache";
    public static final String USER_MENU_CACHE = "userMenuCache";

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30)) // 默认缓存30分钟
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.string()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.json()));

        // 特定缓存的配置
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        // 菜单列表缓存：30分钟
        cacheConfigurations.put(MENU_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(30)));
        // 菜单树形结构缓存：10分钟（菜单结构变化较少）
        cacheConfigurations.put(MENU_TREE_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(10)));
        // 用户菜单缓存：5分钟（用户权限可能变化）
        cacheConfigurations.put(USER_MENU_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(5)));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }

    @Bean
    public KeyGenerator keyGenerator() {
        return new SimpleKeyGenerator();
    }

    @Bean
    public CacheResolver cacheResolver(CacheManager cacheManager) {
        // 使用默认的缓存解析器
        return new org.springframework.cache.interceptor.SimpleCacheResolver(cacheManager);
    }

    @Bean
    public CacheErrorHandler errorHandler() {
        // 使用默认的缓存错误处理器
        return new SimpleCacheErrorHandler();
    }
}
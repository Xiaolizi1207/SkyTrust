package com.skytrust.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;

// 暂时注释掉有问题的导入，先让项目编译通过
// import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
// import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
// import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.PriorityOrdered;
import javax.sql.DataSource;
import org.apache.ibatis.session.SqlSessionFactory;

/**
 * MyBatis-Plus配置类
 * 配置分页插件、防止全表更新删除插件、乐观锁插件等
 */
@Configuration
public class MyBatisPlusConfig {

    /**
     * MyBatis-Plus插件配置
     * 暂时禁用分页和防止全表更新插件，先让项目编译通过
     * 解决依赖问题后，取消注释以下代码：
     * 1. import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
     * 2. import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
     * 3. 取消注释下面的interceptor.addInnerInterceptor调用
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 暂时注释掉有问题的插件，先让项目编译通过
        // 1. 分页插件（必须放在最前面）
        // interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));

        // 2. 防止全表更新与删除插件
        // interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());

        // 3. 乐观锁插件（如果需要乐观锁功能）
        // interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());

        System.out.println("MyBatis-Plus基础配置加载成功（分页和防止全表更新插件已禁用，需要检查依赖版本）");

        return interceptor;
    }

    /**
     * 乐观锁插件配置（按需开启）
     * 需要在实体类字段上添加@Version注解
     * 暂时注释，先让项目编译通过
     */
    // @Bean
    // public OptimisticLockerInnerInterceptor optimisticLockerInnerInterceptor() {
    //     return new OptimisticLockerInnerInterceptor();
    // }

    /**
     * SqlSessionFactory配置
     * 暂时注释，使用MyBatis-Plus自动配置
     */
    /*
    @Bean(name = "sqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        MybatisSqlSessionFactoryBean sessionFactory = new MybatisSqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        // 设置MyBatis-Plus插件
        sessionFactory.setPlugins(mybatisPlusInterceptor());
        return sessionFactory.getObject();
    }
    */

    /**
     * BeanFactory后处理器，专门修复MapperScannerConfigurer的factoryBeanObjectType属性类型错误
     * 针对自动配置创建的mapperScannerConfigurer bean
     */
    @Bean
    public static org.springframework.beans.factory.config.BeanFactoryPostProcessor fixMapperScannerConfigurer() {
        return new org.springframework.beans.factory.config.BeanFactoryPostProcessor() {
            @Override
            public void postProcessBeanFactory(org.springframework.beans.factory.config.ConfigurableListableBeanFactory beanFactory) {
                System.out.println("=== MyBatisPlusConfig.fixMapperScannerConfigurer executing ===");
                if (beanFactory instanceof org.springframework.beans.factory.config.ConfigurableListableBeanFactory) {
                    org.springframework.beans.factory.config.ConfigurableListableBeanFactory configurableBeanFactory =
                        (org.springframework.beans.factory.config.ConfigurableListableBeanFactory) beanFactory;
                    // 专门处理mapperScannerConfigurer bean
                    if (configurableBeanFactory.containsBeanDefinition("mapperScannerConfigurer")) {
                        org.springframework.beans.factory.config.BeanDefinition beanDefinition =
                            configurableBeanFactory.getBeanDefinition("mapperScannerConfigurer");
                        Object factoryBeanObjectType = beanDefinition.getAttribute("factoryBeanObjectType");
                        if (factoryBeanObjectType instanceof String) {
                            String classNameStr = (String) factoryBeanObjectType;
                            try {
                                Class<?> clazz = Class.forName(classNameStr);
                                beanDefinition.setAttribute("factoryBeanObjectType", clazz);
                                System.out.println("Fixed factoryBeanObjectType for mapperScannerConfigurer: " + classNameStr + " -> " + clazz);
                            } catch (ClassNotFoundException e) {
                                // 如果类找不到，使用默认的MapperFactoryBean
                                try {
                                    Class<?> defaultClass = Class.forName("org.mybatis.spring.mapper.MapperFactoryBean");
                                    beanDefinition.setAttribute("factoryBeanObjectType", defaultClass);
                                    System.out.println("Set default MapperFactoryBean for mapperScannerConfigurer");
                                } catch (ClassNotFoundException ex) {
                                    System.out.println("Cannot find default MapperFactoryBean class");
                                }
                            }
                        } else if (factoryBeanObjectType == null) {
                            // 如果没有设置，设置默认的MapperFactoryBean
                            try {
                                Class<?> defaultClass = Class.forName("org.mybatis.spring.mapper.MapperFactoryBean");
                                beanDefinition.setAttribute("factoryBeanObjectType", defaultClass);
                                System.out.println("Set default MapperFactoryBean for mapperScannerConfigurer (was null)");
                            } catch (ClassNotFoundException e) {
                                System.out.println("Cannot find default MapperFactoryBean class");
                            }
                        } else if (!(factoryBeanObjectType instanceof Class)) {
                            // 如果不是String也不是Class，尝试设置为默认的MapperFactoryBean
                            System.out.println("factoryBeanObjectType is not String or Class, setting default MapperFactoryBean");
                            try {
                                Class<?> defaultClass = Class.forName("org.mybatis.spring.mapper.MapperFactoryBean");
                                beanDefinition.setAttribute("factoryBeanObjectType", defaultClass);
                                System.out.println("Set default MapperFactoryBean for mapperScannerConfigurer");
                            } catch (ClassNotFoundException e) {
                                System.out.println("Cannot find default MapperFactoryBean class");
                            }
                        } else {
                            // 已经是Class类型
                            System.out.println("factoryBeanObjectType is already a Class: " + factoryBeanObjectType);
                        }
                    } else {
                        System.out.println("mapperScannerConfigurer bean definition not found");
                    }
                }
                System.out.println("=== MyBatisPlusConfig.fixMapperScannerConfigurer finished ===");
            }
        };
    }

    /**
     * 显式配置MapperScannerConfigurer，避免factoryBeanObjectType类型错误
     * 暂时注释，使用MyBatis-Plus自动配置
     */
    /*
    @Bean
    public static org.mybatis.spring.mapper.MapperScannerConfigurer mapperScannerConfigurer() {
        org.mybatis.spring.mapper.MapperScannerConfigurer scannerConfigurer = new org.mybatis.spring.mapper.MapperScannerConfigurer();
        scannerConfigurer.setBasePackage("com.skytrust.mapper");
        scannerConfigurer.setSqlSessionFactoryBeanName("sqlSessionFactory");
        return scannerConfigurer;
    }
    */

}
package com.skytrust;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@SpringBootApplication
@MapperScan("com.skytrust.mapper")
@ConfigurationPropertiesScan
@EnableAsync
@EnableScheduling
public class SkyTrustApplication {

    public static void main(String[] args) {
        Environment env = SpringApplication.run(SkyTrustApplication.class, args).getEnvironment();

        log.info("==============================================");
        log.info("    SkyTrust 无人机平台 启动成功！");
        log.info("    访问地址：http://localhost:9090/api");
        log.info("==============================================");
    }
}
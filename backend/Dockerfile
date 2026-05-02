# SkyTrust无人机共享租赁区块链平台 - Docker镜像构建文件

# 第一阶段：构建
FROM maven:3.8.7-eclipse-temurin-17 AS builder

# 设置工作目录
WORKDIR /app

# 复制pom.xml和源代码
COPY pom.xml .
COPY src ./src

# 下载依赖并构建

RUN mvn clean package -DskipTests -Pprod

# 第二阶段：运行
FROM eclipse-temurin:17-jre-alpine

# 维护者信息
LABEL maintainer="SkyTrust Team <dev@skytrust.com>"
LABEL version="1.0.0"
LABEL description="SkyTrust无人机共享租赁区块链平台"

# 设置时区
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone && \
    apk del tzdata

# 创建应用用户和目录
RUN addgroup -S skytrust && adduser -S skytrust -G skytrust && \
    mkdir -p /app /logs /uploads && \
    chown -R skytrust:skytrust /app /logs /uploads

# 切换用户
USER skytrust

# 设置工作目录
WORKDIR /app

# 从构建阶段复制jar文件
COPY --from=builder /app/target/*.jar app.jar

# 创建必要的目录
RUN mkdir -p config

# 暴露端口
EXPOSE 9090

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:9090/api/actuator/health || exit 1

# 设置JVM参数
ENV JAVA_OPTS="-Xms512m -Xmx2048m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:G1HeapRegionSize=8m -XX:+ParallelRefProcEnabled -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/logs/heapdump.hprof -XX:+ExitOnOutOfMemoryError"

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar --spring.profiles.active=prod"]
# SkyTrust无人机共享租赁区块链平台

基于Spring Boot的单体应用，集成区块链、AI服务和物联网能力，为无人机共享租赁提供全栈解决方案。

## 项目概述

SkyTrust是一个创新的无人机共享租赁平台，通过区块链技术确保交易透明可信，AI算法优化调度和风险评估，物联网技术实现设备实时监控。

### 核心特性

- **多端接入**: 支持微信小程序、商家后台、运维后台、监管平台
- **区块链存证**: 所有租赁交易上链，确保数据不可篡改
- **智能调度**: AI算法优化无人机调度，提升运营效率
- **实时监控**: 物联网技术实现设备状态实时监控
- **风险评估**: 基于大数据的用户信用评估和风险控制
- **动态定价**: 根据供需关系和设备状态动态调整租赁价格

## 技术架构

### 后端技术栈

- **核心框架**: Spring Boot 3.2.0
- **安全框架**: Spring Security + JWT
- **数据持久层**: Spring Data JPA + MyBatis Plus
- **缓存**: Redis + Redisson
- **消息队列**: RabbitMQ
- **文件存储**: MinIO
- **区块链**: Web3j (Ethereum适配层)
- **物联网**: MQTT + WebSocket
- **API文档**: Knife4j (Swagger3)
- **容器化**: Docker + Docker Compose

### 数据库

- **主数据库**: MySQL 8.0
- **缓存数据库**: Redis 7.0
- **对象存储**: MinIO

### 前端技术栈（独立项目）

- **微信小程序**: UniApp
- **商家后台**: Vue 3 + Element Plus
- **运维后台**: Vue 3 + Ant Design Vue
- **监管平台**: Vue 3 + ECharts

## 项目结构

```
skytrust-platform/
├── src/main/java/com/skytrust/
│   ├── controller/          # Web层 - RESTful API
│   ├── service/             # 业务服务层
│   ├── core/                # 核心能力中心
│   │   ├── blockchain/      # 区块链适配层
│   │   ├── ai/              # AI服务层
│   │   └── iot/             # 物联网接入层
│   ├── repository/          # 数据访问层
│   ├── entity/              # JPA实体类
│   ├── dto/                 # 数据传输对象
│   ├── config/              # 配置类
│   └── common/              # 公共模块
├── src/main/resources/
│   ├── config/              # 配置文件
│   ├── static/              # 静态资源
│   └── i18n/                # 国际化文件
└── src/test/                # 测试代码
```

详细结构请参考 [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md)

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- Docker 20.10+ (推荐)
- MySQL 8.0+
- Redis 7.0+

### 本地开发

1. **克隆项目**
   ```bash
   git clone <repository-url>
   cd skytrust-platform
   ```

2. **启动依赖服务**
   ```bash
   # 使用Docker Compose启动所有依赖服务
   docker-compose up -d mysql redis rabbitmq minio mqtt ganache ai-service
   ```

3. **配置应用**
   ```bash
   # 复制开发环境配置文件
   cp src/main/resources/application-dev.yml src/main/resources/application.yml

   # 修改数据库连接信息（如果需要）
   ```

4. **构建并运行**
   ```bash
   # 使用Maven构建
   mvn clean package -DskipTests

   # 运行应用
   java -jar target/skytrust-platform-1.0.0-SNAPSHOT.jar
   ```

5. **访问应用**
   - 应用地址: http://localhost:8080
   - API文档: http://localhost:8080/api/doc.html
   - 健康检查: http://localhost:8080/api/actuator/health

### Docker部署

1. **构建镜像**
   ```bash
   docker build -t skytrust-platform:1.0.0 .
   ```

2. **启动完整服务栈**
   ```bash
   docker-compose up -d
   ```

3. **查看服务状态**
   ```bash
   docker-compose ps
   ```

## 模块说明

### 1. 用户模块
- 用户注册、登录、认证
- 个人信息管理
- 实名认证
- 信用评分

### 2. 设备模块
- 无人机设备管理
- 设备状态监控
- 设备维护记录
- 设备位置追踪

### 3. 租赁模块
- 租赁订单管理
- 租金计算
- 押金管理
- 违约处理

### 4. 支付模块
- 在线支付
- 押金冻结/解冻
- 账单管理
- 退款处理

### 5. 区块链模块
- 智能合约调用
- 交易存证
- 数据上链
- 事件监听

### 6. AI服务模块
- 调度算法
- 视觉检测
- 风险评估
- 动态定价

### 7. 物联网模块
- 设备连接管理
- 实时消息推送
- 远程控制
- 状态同步

## 配置文件说明

### 多环境配置
- `application.yml` - 主配置文件
- `application-dev.yml` - 开发环境
- `application-test.yml` - 测试环境
- `application-prod.yml` - 生产环境

### 关键配置项

```yaml
skytrust:
  # JWT配置
  jwt:
    secret: your-jwt-secret
    expiration: 7200

  # 区块链配置
  blockchain:
    enabled: true
    type: ethereum
    node-url: http://localhost:8545

  # AI服务配置
  ai:
    service-url: http://localhost:5000

  # 物联网配置
  iot:
    mqtt:
      broker-url: tcp://localhost:1883
```

## API文档

启动应用后，访问以下地址查看API文档：

- Swagger UI: http://localhost:8080/api/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api/v3/api-docs

## 测试

### 单元测试
```bash
mvn test
```

### 集成测试
```bash
mvn verify -Pintegration-test
```

### 代码质量检查
```bash
mvn checkstyle:check
mvn pmd:check
mvn spotbugs:check
```

## 部署指南

### 生产环境部署

1. **环境准备**
   ```bash
   # 创建数据目录
   mkdir -p /data/skytrust/{logs,uploads,mysql,redis}

   # 设置权限
   chmod 755 /data/skytrust
   ```

2. **数据库初始化**
   ```sql
   -- 创建数据库和用户
   CREATE DATABASE skytrust_prod CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   CREATE USER 'skytrust'@'%' IDENTIFIED BY 'strong_password';
   GRANT ALL PRIVILEGES ON skytrust_prod.* TO 'skytrust'@'%';
   FLUSH PRIVILEGES;
   ```

3. **使用Docker Compose部署**
   ```bash
   # 修改生产环境配置
   vim docker-compose.prod.yml

   # 启动服务
   docker-compose -f docker-compose.prod.yml up -d
   ```

### 监控与运维

- **应用监控**: Spring Boot Actuator + Prometheus + Grafana
- **日志收集**: ELK Stack (Elasticsearch, Logstash, Kibana)
- **链路追踪**: SkyWalking
- **告警系统**: AlertManager

## 开发规范

### 代码规范
- 遵循阿里巴巴Java开发手册
- 使用Checkstyle进行代码规范检查
- 提交前必须通过所有测试

### Git工作流
- 使用Git Flow分支模型
- 提交信息遵循Conventional Commits规范
- 代码审查必须通过才能合并

### 安全规范
- 所有API接口必须进行权限验证
- 敏感数据必须加密存储
- 定期进行安全漏洞扫描

## 贡献指南

1. Fork本仓库
2. 创建功能分支 (`git checkout -b feature/amazing-feature`)
3. 提交更改 (`git commit -m 'Add some amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 开启Pull Request

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 联系方式

- 项目负责人: SkyTrust Team
- 邮箱: dev@skytrust.com
- 问题反馈: [GitHub Issues](https://github.com/skytrust/platform/issues)

## 版本历史

### v1.0.0 (2026-03-31)
- 初始版本发布
- 基础用户、设备、租赁功能
- 区块链存证集成
- AI服务基础对接
- 物联网设备连接
# SkyTrust无人机共享租赁区块链平台 - 项目架构目录结构

## 项目概述
Spring Boot单体应用，支持微信小程序、商家后台、运维后台、监管平台等多端访问，集成区块链、AI服务、物联网等核心能力。

## 目录结构

```
SkyTrust2026.3.31/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── skytrust/
│   │   │           ├── controller/                    # Web层(Controller)
│   │   │           │   ├── user/                      # 用户API
│   │   │           │   ├── device/                    # 设备API
│   │   │           │   ├── rental/                    # 租赁API
│   │   │           │   ├── payment/                   # 支付API
│   │   │           │   └── message/                   # 消息API
│   │   │           ├── service/                       # 业务服务层(Service)
│   │   │           │   ├── user/                      # 用户服务
│   │   │           │   ├── device/                    # 设备服务
│   │   │           │   ├── rental/                    # 租赁服务
│   │   │           │   ├── payment/                   # 支付服务
│   │   │           │   └── risk/                      # 风控服务
│   │   │           ├── core/                          # 核心能力中心
│   │   │           │   ├── blockchain/                # 区块链适配层
│   │   │           │   │   ├── contract/              # 智能合约调用
│   │   │           │   │   ├── transaction/           # 交易构造
│   │   │           │   │   ├── event/                 # 事件监听
│   │   │           │   │   ├── storage/               # 数据存证
│   │   │           │   │   ├── model/                 # 区块链数据模型
│   │   │           │   │   └── config/                # 区块链配置
│   │   │           │   ├── ai/                        # AI服务层
│   │   │           │   │   ├── scheduling/            # 调度算法
│   │   │           │   │   ├── vision/                # 视觉检测
│   │   │           │   │   ├── risk/                  # 风险评估
│   │   │           │   │   ├── pricing/               # 动态定价
│   │   │           │   │   └── model/                 # AI模型
│   │   │           │   └── iot/                       # 物联网接入层
│   │   │           │       ├── connection/            # 设备连接
│   │   │           │       ├── message/               # 消息处理
│   │   │           │       ├── command/               # 指令下发
│   │   │           │       ├── state/                 # 状态同步
│   │   │           │       └── model/                 # 设备模型
│   │   │           ├── repository/                    # 数据访问层(Repository)
│   │   │           │   ├── user/                      # 用户DAO
│   │   │           │   ├── device/                    # 设备DAO
│   │   │           │   ├── order/                     # 订单DAO
│   │   │           │   ├── payment/                   # 支付DAO
│   │   │           │   └── log/                       # 日志DAO
│   │   │           ├── entity/                        # JPA实体类
│   │   │           ├── dto/                           # 数据传输对象
│   │   │           │   ├── request/                   # 请求DTO
│   │   │           │   └── response/                  # 响应DTO
│   │   │           ├── config/                        # 配置类
│   │   │           ├── common/                        # 公共模块
│   │   │           │   ├── constant/                  # 常量定义
│   │   │           │   ├── exception/                 # 异常定义
│   │   │           │   └── utils/                     # 工具类
│   │   │           ├── scheduler/                     # 定时任务
│   │   │           ├── listener/                      # 事件监听器
│   │   │           ├── interceptor/                   # 拦截器
│   │   │           └── aspect/                        # AOP切面
│   │   └── resources/
│   │       ├── config/                                # 配置文件
│   │       │   ├── dev/                               # 开发环境
│   │       │   ├── test/                              # 测试环境
│   │       │   └── prod/                              # 生产环境
│   │       ├── static/                                # 静态资源
│   │       ├── templates/                             # 模板文件
│   │       ├── public/                                # 公共资源
│   │       └── i18n/                                  # 国际化文件
│   └── test/                                          # 测试代码
│       └── java/
│           └── com/
│               └── skytrust/
│                   ├── controller/                    # 控制器测试
│                   ├── service/                       # 服务层测试
│                   ├── core/                          # 核心模块测试
│                   └── repository/                    # 数据层测试
├── docs/                                              # 项目文档
├── scripts/                                           # 脚本文件
├── .gitignore                                         # Git忽略配置
├── pom.xml                                            # Maven配置
├── Dockerfile                                         # Docker镜像配置
├── docker-compose.yml                                 # Docker编排配置
├── README.md                                          # 项目说明
└── PROJECT_STRUCTURE.md                               # 本项目结构说明
```

## 技术栈

### 后端
- **框架**: Spring Boot 3.x
- **数据库**: MySQL 8.0 + Redis 7.0
- **ORM**: Spring Data JPA + MyBatis Plus
- **消息队列**: RabbitMQ/Kafka
- **区块链**: Hyperledger Fabric/Ethereum适配层
- **AI服务**: Python微服务 + HTTP调用
- **物联网**: MQTT + WebSocket
- **文件存储**: MinIO/阿里云OSS
- **部署**: Docker + Kubernetes

### 前端（独立项目）
- **微信小程序**: UniApp
- **商家后台**: Vue 3 + Element Plus
- **运维后台**: Vue 3 + Ant Design Vue
- **监管平台**: Vue 3 + ECharts

## 模块说明

### 1. Web层 (Controller)
- 提供RESTful API接口
- 参数校验、权限验证
- API版本管理
- 统一响应格式

### 2. 业务服务层 (Service)
- 核心业务逻辑实现
- 事务管理
- 服务间调用
- 业务规则校验

### 3. 核心能力中心 (Core Modules)
#### 区块链适配层
- 智能合约调用封装
- 区块链交易构造与发送
- 区块链事件监听
- 数据存证服务

#### AI服务层
- 无人机调度算法
- 视觉检测服务调用
- 租赁风险评估
- 动态定价模型

#### 物联网接入层
- 设备连接管理
- MQTT消息处理
- 设备指令下发
- 设备状态同步

### 4. 数据访问层 (Repository)
- 数据库操作封装
- 缓存策略实现
- 数据持久化
- 复杂查询优化

## 环境配置

### 开发环境
- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 7.0+

### 测试环境
- 集成测试环境
- 性能测试环境
- 安全测试环境

### 生产环境
- 高可用部署
- 监控告警
- 日志收集
- 自动扩缩容

## 部署架构

```
用户访问层 → Nginx负载均衡 → Spring Boot应用集群
                              ↓
                      数据存储与外部服务
                      ├── MySQL主从集群
                      ├── Redis哨兵集群
                      ├── MinIO对象存储
                      ├── MQTT Broker集群
                      ├── AI服务集群
                      └── 区块链节点网络
```

## 下一步工作

1. 初始化Spring Boot项目框架
2. 配置数据库连接和JPA
3. 实现基础用户模块
4. 集成区块链适配层
5. 开发物联网接入模块
6. 对接AI服务接口
7. 编写单元测试和集成测试
8. 部署和运维配置
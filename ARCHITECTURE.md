# SkyTrust 系统架构

## 概述

SkyTrust 是一个无人机共享租赁区块链平台，采用 **monorepo + 微服务化部署** 架构。

```
用户端                    管理端                    监管端
 ┌──────┐              ┌──────────┐              ┌──────────┐
 │ 小程序 │              │ Vue 3 后台 │              │ ECharts   │
 │uni-app│              │Element Plus│              │ 大屏     │
 └──┬───┘              └─────┬─────┘              └────┬─────┘
    │                        │                         │
    └────────────┬───────────┘─────────────────────────┘
                 │  HTTPS / WSS
         ┌───────┴───────┐
         │   Nginx 反向代理 │
         └───────┬───────┘
                 │
    ┌────────────┼────────────┐
    │            │            │
    ▼            ▼            ▼
┌────────┐ ┌──────────┐ ┌──────────────────────┐
│ 静态资源 │ │ REST API │ │  WebSocket / MQTT    │
│ (前端)  │ │ (Spring) │ │  (设备实时通信)        │
└────────┘ └─────┬────┘ └──────────────────────┘
                 │
    ┌────────────┼────────────────────┐
    │            │                    │
    ▼            ▼                    ▼
┌────────┐ ┌──────────┐ ┌──────────────────────┐
│ MySQL  │ │  Redis   │ │  RabbitMQ (消息队列)   │
│ (主库)  │ │ (缓存)    │ │                      │
└────────┘ └──────────┘ └──────────────────────┘
    │            │                    │
    ▼            ▼                    ▼
┌────────┐ ┌──────────┐ ┌──────────────────────┐
│ MinIO  │ │  Ganache  │ │  AI Service (Flask)  │
│ (文件)  │ │ (ETH节点) │ │  (Python)            │
└────────┘ └──────────┘ └──────────────────────┘
```

## 模块说明

### 1. 后端 (`backend/`)
- **语言/框架**: Java 17, Spring Boot 2.7.15
- **架构**: 单体 Spring Boot，分层清晰
- **认证**: JWT (access + refresh token) + BCrypt 密码加密
- **数据层**: MyBatis-Plus + Spring Data JPA, MySQL 8.0, Redis 7
- **消息**: RabbitMQ
- **API 文档**: Knife4j/OpenAPI 3

| 层 | 说明 |
|---|------|
| `controller/` | 25 个 REST 控制器 |
| `service/` | 22 个接口 + 22 个实现 |
| `entity/` | 20 个 JPA 实体 |
| `config/` | SecurityConfig, JwtConfig, MQTT, WebSocket 等 |
| `core/` | blockchain, ai, iot 核心能力 |

### 2. 前端 (`frontend/`)
- **语言/框架**: Vue 3 + TypeScript + Vite
- **状态管理**: Pinia
- **路由**: Vue Router 4 (22 条路由)
- **API**: Axios 封装 (自动 token 刷新)

### 3. 智能合约 (`contracts/`)
- **语言**: Solidity 0.8.24
- **框架**: Hardhat 2.22 + OpenZeppelin 5.1
- **合约**: DronePassportNFT (ERC-721), LicenseVerification, FlightLogHash
- **测试**: 37 条 (mint/多签/签名/防重放/权限)

| 合约 | 功能 |
|------|------|
| `DronePassportNFT` | 无人机护照 NFT，维修多签，机载签名电池更新 |
| `LicenseVerification` | 许可证哈希存证，租赁订单绑定 |
| `FlightLogHash` | 飞行日志哈希批量上链，防重放 |

### 4. 微信小程序 (`miniprogram/`)
- **框架**: uni-app (Vue 3 + TypeScript)
- **页面**: 14 个（设备、订单、钱包、区块链等）

### 5. Edge 层 (`edge/`)
- 地理围栏预测器 (Python)
- 避障规格文档

### 6. CI/CD (`.github/workflows/ci.yml`)
- 后端: Maven 编译 + 测试
- 前端: npm build
- 合约: Hardhat 编译 + 测试
- 安全: Gitleaks 密钥扫描

## 数据模型

核心实体: `User`, `Device`, `RentalOrder`, `Payment`, `WalletTransaction`, `BlockchainTransaction`, `FlightRecord`, `Insurance`, `SmartContract`

## 部署

```bash
cp .env.example .env
vim .env  # 填入密钥
docker-compose up -d
```

详见 [DEPLOYMENT.md](DEPLOYMENT.md)

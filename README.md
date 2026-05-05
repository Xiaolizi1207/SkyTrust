# SkyTrust —— 无人机共享租赁区块链平台

<p align="center">
  <strong>Blockchain × AI × IoT 三位一体 · 让每一架无人机都可信、可溯、可智能调度</strong>
</p>

---

## 目录

- [项目简介](#项目简介)
- [技术栈](#技术栈)
- [产品亮点](#产品亮点)
- [产品新颖点](#产品新颖点)
- [系统架构](#系统架构)
- [模块说明](#模块说明)
- [快速开始](#快速开始)
- [项目结构](#项目结构)
- [部署指南](#部署指南)
- [开发规范](#开发规范)
- [许可证](#许可证)

---

## 项目简介

SkyTrust 是一个面向无人机共享租赁场景的全栈区块链平台。平台通过 **区块链存证** 确保交易透明不可篡改，通过 **AI 算法** 优化调度、动态定价与风险评估，通过 **物联网（IoT）** 实现设备实时监控与远程控制，为运营商、用户和监管机构提供一站式解决方案。

**多端覆盖**：微信小程序（消费者） + Vue 3 管理后台（运营商） + ECharts 可视化大屏（监管）。

---

## 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| **后端框架** | Java + Spring Boot | 17 / 2.7.15 |
| **ORM** | MyBatis-Plus + Spring Data JPA | 3.5.3.1 |
| **数据库** | MySQL + Redis | 8.0 / 7.0 |
| **消息队列** | RabbitMQ | 3.x |
| **对象存储** | MinIO | 8.5.9 |
| **认证** | JWT (access + refresh token) + BCrypt | jjwt 0.12.5 |
| **API 文档** | Knife4j / OpenAPI 3 | 4.5.0 |
| **区块链** | Solidity + Hardhat + Web3j + OpenZeppelin | 0.8.24 / 2.22 / 4.10.2 / 5.1 |
| **Ethereum 节点** | Ganache (开发) / Sepolia (测试) | — |
| **物联网** | MQTT + WebSocket | Paho 1.2.5 |
| **AI 服务** | Python Flask + TensorFlow Lite | 3.x |
| **边缘计算** | Python (原型) + C++ (嵌入式端口规范，ARM Cortex-A) | — |
| **前端** | Vue 3 + TypeScript + Vite | 3.5 / 5.5 / 5.4 |
| **状态管理** | Pinia | 2.2 |
| **路由** | Vue Router 4 | 4.4 |
| **HTTP 客户端** | Axios (自动 token 刷新) | 1.7 |
| **小程序** | uni-app (Vue 3 + TypeScript) | 3.0 alpha |
| **CI/CD** | GitHub Actions | — |
| **容器化** | Docker + Docker Compose | 20.10+ / 2.0+ |

---

## 产品亮点

### 1. 区块链 + AI + IoT 三位一体融合

不同于市面上单一的无人机管理平台，SkyTrust 将 **区块链（信任锚点）、AI（智能大脑）、IoT（感知触角）** 深度融合：

- **区块链**：每笔租赁交易、每次维修记录、每条飞行日志均上链存证，不可篡改
- **AI**：动态定价、需求预测、风险评估、视觉检测、调度优化、AI 对话助手
- **IoT**：MQTT 实时设备通信、WebSocket 双向推送、设备状态实时监控、远程指令下发

### 2. 全链路可审计

从用户身份认证、设备注册、租赁下单、飞行执行到支付结算，全链路数据上链。AI 决策日志采用结构化格式，支持事故审计回溯。每一笔关键操作都有可追踪的 `correlation_id` 和时间戳。

### 3. 边缘-云端协同的地理围栏系统

- **边缘端**（ARM Cortex-A 嵌入式）：30 秒轨迹预测 + 点面判定，单次检查 < 5ms，内存 < 500KB，支持断网离线运行
- **云端**：全局 NFZ 数据管理、增量同步（GeoJSON diff）、策略下发、日志聚合
- **双端协作**：设备端执行核心决策，云端提供数据与策略管理

### 4. 动态定价引擎

基于 15+ 特征（需求预测、天气窗口、历史订单、设备可用性、电池健康度、NFZ 密度、市场竞争强度、季节性指数等）的多因子定价模型，支持 LSTM / XGBoost / LightGBM 算法切换，价格在 [成本×120%, 基准价×300%] 区间内波动，兼顾收益与公平。

### 5. 多端全覆盖

| 端 | 技术 | 用户 |
|----|------|------|
| 微信小程序 | uni-app (Vue 3) | 普通用户（租赁、支付、查看订单） |
| Web 管理后台 | Vue 3 + Element Plus | 运营商（设备管理、订单管理、用户管理） |
| 可视化大屏 | Vue 3 + ECharts | 监管机构（实时数据监控） |

---

## 产品新颖点

### 🔷 无人机数字护照 NFT（DronePassportNFT）

每架无人机在区块链上拥有唯一的 ERC-721 数字护照，记录制造商、序列号、固件版本、电池循环次数、维修次数等全生命周期数据。NFT 元数据完全链上编码（Base64 JSON），无需依赖外部存储。

**维修多签机制**：维修记录需要 2-of-N 个授权维修商的签名才能上链，防止单方篡改维修历史。

**机载签名电池更新**：无人机机载计算机通过以太坊签名消息自动上报电池循环次数，使用 nonce 防重放，实现无信任的自动化遥测日志。

### 🔷 地理围栏边缘预测器（Edge Geofence Predictor）

- 30 秒轨迹预测：基于当前位置与速度向量，预测未来 30 秒是否进入禁飞区
- 三层状态判定：SAFE → VIOLATION_IMMINENT（200 米预警） → VIOLATED
- Hover 为最高优先级指令，覆盖遥控输入，直至风险解除
- 提供 Python 原型 + 完整 C++ 嵌入式端口规范（ARM Cortex-A72/A53，FreeRTOS/Linux）
- 双向集成接口：DJI MSDK v5 FlightController 和 PX4 MAVLink
- 禁飞区数据支持 GeoJSON 增量同步（版本化 diff），TLS + AES-256 加密

### 🔷 AI 对话助手 + 危险指令二次确认流

自然语言指令 → AI 推理链 → 安全检查清单 → 参数 Payload → 执行。当检测到 **超视距（BVLOS）、高风速、低电量** 等风险时，触发二次确认流程：

1. 系统生成风险分数与建议方案
2. 向操作者展示风险摘要（风险分数、BVLOS 半径、应急降落点、电量余量）
3. 操作者确认后进入增强安全检查（备用航线、额外电量预留）
4. 操作者取消则回退到安全状态（Hover / Return-to-Home）

### 🔷 飞行日志哈希批量上链 + 防重放

飞行日志通过 (orderId, logHash, timestamp) 三元组哈希批量提交上链，内置防重放映射表。日志存储不可变且可验证，适合监管审计，又避免全量数据上链造成的链上膨胀。

### 🔷 许可证哈希存证 + 隐私保护

用户飞行许可证仅将哈希值上链，明文许可证号永不上链。每次租赁订单绑定许可证哈希 + 用户签名，实现隐私保护下的合规校验。

### 🔷 DJI MSDK v5 深度对接规范

完整的飞行参数格式定义（FlightPlan + SafetyParams），与 DJI MSDK v5 FlightController 指令集对齐：
- 航线点（waypoints）、最大高度、速度、朝向
- 地理锁定（geo_lock）、悬停超时、失控返航策略
- 参数派发支持 REST（`POST /api/drone/params`）和 MQTT（`drone/{id}/flight_plan`）双通道

### 🔷 TensorFlow Lite 避障模型规格

- 输入：图像张量 `[H, W, 3]` uint8 或 float32
- 输出：深度图 `[1, H, W]` float32 或避障向量 `[1, 3]` float32
- 支持 160×120 / 320×180 / 640×360 多分辨率
- 推荐 int8 量化版本用于边缘部署

### 🔷 智能调度 + 动态定价闭环

AI 需求预测 → 动态定价 → 派单生成 → 设备调度 → 执行反馈，形成完整的智能运营闭环。调度算法支持最近邻匹配，定价模型综合 15+ 特征，派单生成器根据需求预测自动决定配送/侦察任务类型。

---

## 系统架构

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

---

## 模块说明

### 后端 (`backend/`) —— 25 个控制器 + 22 个服务

| 模块 | 控制器 | 说明 |
|------|--------|------|
| 用户认证 | `AuthController`, `UserController` | JWT 双 token、注册、登录、角色权限 |
| 设备管理 | `DeviceController`, `DeviceTrackController` | 设备 CRUD、状态追踪、位置追踪 |
| 租赁订单 | `RentalOrderController` | 下单、接单、完成、违约处理 |
| 支付 | `PaymentController` | 支付、押金冻结/解冻、退款 |
| 钱包 | `WalletController` | 余额、交易记录 |
| 区块链 | `BlockchainTransactionController`, `SmartContractController` | 交易上链、合约调用、事件监听 |
| 飞行记录 | `FlightRecordController` | 飞行日志存储与查询 |
| 保险 | `InsuranceController` | 保单管理 |
| IoT | `IotController` | 设备连接、MQTT 消息、远程指令 |
| 文件 | `FileUploadController` | 图片/文件上传到 MinIO |
| 资质 | `QualificationController` | 飞行许可证管理 |
| 系统管理 | `MenuController`, `RoleController`, `DictController`, `SystemConfigController` | RBAC、数据字典、系统配置 |
| AI 服务核心 | `core/ai/`, `core/blockchain/`, `core/iot/` | AI 调用适配、区块链交互、IoT 接入 |

### 智能合约 (`contracts/`) —— 3 个 Solidity 合约 + 37 条测试

| 合约 | 功能 | 新颖点 |
|------|------|--------|
| `DronePassportNFT` | ERC-721 无人机数字护照 | 维修多签、机载签名电池更新、全链上元数据 |
| `LicenseVerification` | 许可证哈希存证 | 哈希上链保护隐私、订单绑定 + 用户签名 |
| `FlightLogHash` | 飞行日志哈希上链 | 批量提交、防重放三元组 |

### 前端 (`frontend/`) —— 20 个视图 + 22 条路由

| 模块 | 页面 | 说明 |
|------|------|------|
| 认证 | login, register, forgot-password | 登录、注册、忘记密码 |
| 仪表盘 | dashboard | 运营数据总览 |
| 设备管理 | device | 设备列表、详情、状态 |
| 订单 | order | 租赁订单管理 |
| 支付 | payment | 支付流水 |
| 钱包 | wallet | 余额、交易记录 |
| 用户/角色/菜单 | user, role, menu | RBAC 权限管理 |
| 区块链 | blockchain/passport, licensing, flight-log | NFT 护照、执照验证、飞行日志 |
| AI | geofence, pricing, ai/chatbot | 地理围栏、动态定价、AI 对话助手 |
| IoT | iot/monitor, iot/control | 设备实时监控、远程控制 |
| 资质 | qualification | 飞行许可证 |
| 个人中心 | profile | 个人信息 |

### 微信小程序 (`miniprogram/`) —— 14 个页面

覆盖设备浏览、租赁下单、订单管理、钱包、区块链查询等核心用户场景。

### AI 服务 (`backend/ai-service/`) —— 4+ API

| 端点 | 功能 |
|------|------|
| `POST /api/v1/scheduling/optimize` | 无人机调度优化（最近邻算法） |
| `POST /api/v1/vision/detect` | 视觉检测（目标识别 + 风险评估） |
| `POST /api/v1/risk/assess` | 综合风险评估（信用、设备、天气、时间） |
| `POST /api/v1/pricing/dynamic` | 动态定价（多因子模型） |
| `POST /api/v1/ai/batch` | AI 任务批量处理 |

### Edge 层 (`edge/`)

| 文件 | 说明 |
|------|------|
| `geofence_predictor.py` | 地理围栏预测器（Python 原型 + C++ 嵌入式端口规范） |
| `obstacle_avoidance_spec.txt` | 避障规格说明 |

---

## 快速开始

### 环境要求

| 组件 | 版本 |
|------|------|
| Docker | 20.10+ |
| Docker Compose | 2.0+ |
| JDK | 17+ |
| Node.js | 18+ |
| Maven | 3.8+ |

### 1. 克隆项目

```bash
git clone <repository-url>
cd SkyTrust2026.4.11
```

### 2. 配置密钥

```bash
cp .env.example .env
# 编辑 .env 填入所有密码（JWT_SECRET、数据库密码等）
```

### 3. 一键启动

```bash
cd backend
docker-compose up -d
```

启动后服务端口：

| 端口 | 服务 |
|------|------|
| 9090 | Spring Boot 后端 |
| 3000 | Vue 3 前端 |
| 3307 | MySQL |
| 6380 | Redis |
| 5672 | RabbitMQ |
| 9000 | MinIO |
| 8545 | Ganache (Ethereum) |
| 5000 | AI Service (Flask) |
| 1883 | MQTT |

### 4. 验证

```bash
# 健康检查
curl http://localhost:9090/api/actuator/health

# API 文档
open http://localhost:9090/doc.html

# 前端
open http://localhost:3000
```

### 本地开发

```bash
# 后端
cd backend && mvn spring-boot:run

# 前端
cd frontend && npm install && npm run dev

# 合约
cd contracts && npm install && npx hardhat compile && npx hardhat test

# 小程序
cd miniprogram && npm install && npm run dev:mp-weixin
```

---

## 项目结构

```
SkyTrust/
├── backend/                    # Spring Boot 后端
│   ├── src/main/java/com/skytrust/
│   │   ├── controller/         # 25 个 REST 控制器
│   │   ├── service/            # 22 个服务接口 + 实现
│   │   ├── entity/             # 20 个 JPA 实体
│   │   ├── core/               # blockchain / ai / iot 核心能力
│   │   ├── config/             # Security、JWT、MQTT、WebSocket 配置
│   │   ├── dto/                # 数据传输对象
│   │   ├── security/           # JWT 过滤器、认证提供者
│   │   ├── aspect/             # AOP 切面（日志、权限）
│   │   └── ...
│   ├── ai-service/             # Python Flask AI 服务
│   ├── docker-compose.yml      # 容器编排
│   └── pom.xml
├── frontend/                   # Vue 3 管理后台
│   ├── src/views/              # 20 个页面
│   ├── src/router/             # 路由配置（22 条）
│   ├── src/store/              # Pinia 状态
│   ├── src/api/                # Axios 封装
│   └── package.json
├── contracts/                  # Solidity 智能合约
│   ├── src/                    # 3 个合约
│   ├── tests/                  # 37 条测试
│   └── hardhat.config.js
├── miniprogram/                # uni-app 微信小程序
│   ├── pages/                  # 14 个页面
│   └── package.json
├── edge/                       # 边缘计算
│   ├── geofence_predictor.py   # 地理围栏预测器
│   └── obstacle_avoidance_spec.txt
├── docs/                       # 规格文档
│   ├── ai_chatbot_spec.md      # AI 对话助手规格
│   ├── ai_geofence_spec.md     # AI 地理围栏规格
│   ├── ai_pricing_spec.md      # AI 定价与派单规格
│   └── API_REFERENCE.md        # API 参考
├── tests/                      # 跨模块测试
├── tools/                      # 辅助工具
├── ARCHITECTURE.md             # 架构文档
├── DEPLOYMENT.md               # 部署指南
└── .github/workflows/ci.yml    # CI/CD 流水线
```

---

## 部署指南

详见 [DEPLOYMENT.md](DEPLOYMENT.md)。核心要点：

- **密钥管理**：生产环境使用环境变量或 Vault/AWS Secrets Manager 注入，`.env` 仅用于本地开发
- **TLS/SSL**：Nginx 配置支持 SSL，证书路径 `backend/config/nginx/ssl/`
- **合约部署**：配置 `SEPOLIA_URL` 和 `SEPOLIA_PRIVATE_KEY` 后部署到测试网
- **健康检查**：每个服务提供独立的健康检查端点
- **推荐监控**：Prometheus + Grafana（应用）/ ELK（日志）/ SkyWalking（链路）/ AlertManager（告警）

---

## 开发规范

- **代码规范**：阿里巴巴 Java 开发手册 + ESLint（前端）
- **Git 工作流**：Git Flow 分支模型 + Conventional Commits
- **安全规范**：所有 API 需权限验证，敏感数据加密存储，定期安全扫描
- **CI/CD**：每次 PR 触发 Maven 编译 + 测试、前端 build、合约编译 + 测试、Gitleaks 密钥扫描

---

## 许可证

本项目采用 [MIT](LICENSE) 许可证。

---

<p align="center">
  <sub>Built with ❤️ by SkyTrust Team · 2026</sub>
</p>

# SkyTrust 部署指南

## 环境要求

| 组件 | 版本 |
|------|------|
| Docker | 20.10+ |
| Docker Compose | 2.0+ |
| JDK | 17+ (本地开发) |
| Node.js | 18+ (前端/合约) |
| Maven | 3.8+ (后端) |

## 快速启动（开发环境）

### 1. 配置密钥

```bash
cp .env.example .env
# 编辑 .env 填入所有密码
vim .env
```

**最小必填项**:
```ini
MYSQL_ROOT_PASSWORD=your_mysql_root_pw
MYSQL_PASSWORD=your_mysql_pw
RABBITMQ_DEFAULT_PASS=your_rabbitmq_pw
MINIO_ROOT_PASSWORD=your_minio_pw
MQTT_PASSWORD=your_mqtt_pw
DB_PASSWORD=your_mysql_pw
JWT_SECRET=生成一个256位随机字符串
```

### 2. 启动全部服务

```bash
cd backend
docker-compose up -d
```

服务列表:
| 端口 | 服务 | 说明 |
|------|------|------|
| 9090 | skytrust-app | Spring Boot 后端 |
| 3000 | skytrust-frontend | Vue 3 前端 |
| 3307 | MySQL | 数据库 |
| 6380 | Redis | 缓存 |
| 5672 | RabbitMQ | 消息队列 |
| 9000 | MinIO | 对象存储 |
| 8545 | Ganache | Ethereum 开发节点 |
| 5000 | AI Service | Python Flask AI |
| 1883 | MQTT | 设备通信 |

### 3. 验证

```bash
# 健康检查
curl http://localhost:9090/api/actuator/health

# API 文档
open http://localhost:9090/doc.html

# 前端
open http://localhost:3000
```

## 本地开发

### 后端

```bash
cd backend
# 确保已设置环境变量 JWT_SECRET
export JWT_SECRET=dev-secret-do-not-use-in-prod
mvn spring-boot:run
```

### 前端

```bash
cd frontend
npm install
npm run dev
```

### 合约

```bash
cd contracts
npm install
npx hardhat compile
npx hardhat test

# 启动本地链
npx hardhat node

# 部署合约
npx hardhat run scripts/deploy.js --network localhost
```

## 生产部署

### 密钥管理

生产环境必须通过环境变量或密钥管理服务（Vault/AWS Secrets Manager）注入所有密钥。`.env` 文件仅用于本地开发，**绝不能**提交到代码仓库或用于生产环境。

以下密钥必须配置:
```
JWT_SECRET          — 至少 256 位随机字符串
DB_PASSWORD         — MySQL 密码
MYSQL_ROOT_PASSWORD — MySQL root 密码
RABBITMQ_DEFAULT_PASS
MINIO_ROOT_PASSWORD
MINIO_SECRET_KEY
MQTT_PASSWORD
```

### TLS/SSL

Nginx 配置中包含 SSL 支持，证书路径:
```
backend/config/nginx/ssl/
```

### 数据库初始化

```sql
CREATE DATABASE skytrust_prod CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'skytrust'@'%' IDENTIFIED BY '<strong_password>';
GRANT ALL PRIVILEGES ON skytrust_prod.* TO 'skytrust'@'%';
FLUSH PRIVILEGES;
```

然后运行 `backend/scripts/db/init.sql` 初始化种子数据。

### 健康检查

| 服务 | 端点 |
|------|------|
| Spring Boot | `GET /api/actuator/health` |
| Redis | `redis-cli ping` |
| RabbitMQ | `rabbitmq-diagnostics ping` |
| MinIO | `GET /minio/health/live` |
| Ganache | `POST /` (JSON-RPC) |
| AI Service | `GET /health` |

## 合约部署

部署到测试网/主网前:
1. 配置 `contracts/hardhat.config.js` 中对应网络
2. 设置 `SEPOLIA_URL` 和 `SEPOLIA_PRIVATE_KEY` 环境变量
3. 运行 `npx hardhat run scripts/deploy.js --network sepolia`
4. 将部署地址更新到 `application.yml` 的 `skytrust.blockchain.contract-address`

## 监控

推荐组合:
- **应用监控**: Spring Boot Actuator + Prometheus + Grafana
- **日志**: ELK Stack
- **链路追踪**: SkyWalking
- **告警**: AlertManager

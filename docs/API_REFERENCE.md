# SkyTrust API 参考手册

基于 Knife4j/OpenAPI 3 自生文档。所有路径以 `/api` 为前缀。

## 认证 `/api/auth`

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| POST | `/api/auth/login` | 公开 | 密码登录 |
| POST | `/api/auth/register` | 公开 | 用户注册 |
| POST | `/api/auth/refresh` | 公开 | 刷新 Token |
| POST | `/api/auth/captcha` | 公开 | 获取图形验证码 |
| POST | `/api/auth/send-code` | 公开 | 发送短信/邮箱验证码 |
| POST | `/api/auth/code-login` | 公开 | 验证码登录 |
| POST | `/api/auth/forgot-password` | 公开 | 忘记密码 |
| POST | `/api/auth/reset-password` | 公开 | 重置密码 |
| POST | `/api/auth/wechat-login` | 公开 | 微信小程序登录 |
| POST | `/api/auth/decrypt-phone` | 公开 | 解密微信手机号 |

## 用户 `/api/users`

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| GET | `/api/users/me` | JWT | 当前用户信息 |
| PUT | `/api/users/me` | JWT | 更新个人信息 |
| GET | `/api/users/{id}` | JWT | 用户详情 |
| GET | `/api/users/list` | JWT | 用户列表（分页） |
| PUT | `/api/users/{id}/password` | JWT | 修改密码 |
| PUT | `/api/users/{id}/status` | ADMIN | 启用/禁用用户 |
| PUT | `/api/users/{id}/role` | ADMIN | 分配角色 |

## 设备 `/api/devices`

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| GET | `/api/devices` | 公开 | 设备列表 |
| GET | `/api/devices/{id}` | 公开 | 设备详情 |
| POST | `/api/devices` | ADMIN | 注册设备 |
| PUT | `/api/devices/{id}` | ADMIN | 更新设备 |

## 租赁订单 `/api/orders`

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| POST | `/api/orders` | JWT | 创建租赁订单 |
| GET | `/api/orders/{id}` | JWT | 订单详情 |
| GET | `/api/orders/my` | JWT | 我的订单列表 |
| PUT | `/api/orders/{id}/cancel` | JWT | 取消订单 |

## 支付 `/api/payments`

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| POST | `/api/payments` | JWT | 创建支付 |
| GET | `/api/payments/order/{orderId}` | JWT | 按订单查支付 |

## 钱包 `/api/wallet`

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| GET | `/api/wallet/balance` | JWT | 查询余额 |
| POST | `/api/wallet/recharge` | JWT | 充值 |
| GET | `/api/wallet/transactions` | JWT | 交易记录 |

## 区块链 `/api/blockchain`

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| GET | `/api/blockchain/passport/{id}` | JWT | 查询无人机护照 NFT |
| POST | `/api/blockchain/license` | ADMIN | 许可证哈希存证 |
| POST | `/api/blockchain/flight-log` | JWT | 飞行日志哈希上链 |

## IoT `/api/iot`

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| POST | `/api/iot/devices/{id}/command` | JWT | 发送设备指令 |
| GET | `/api/iot/devices/{id}/telemetry` | JWT | 设备遥测数据 |

## 角色与权限 `/api/roles` `/api/menus`

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| GET | `/api/roles` | ADMIN | 角色列表 |
| POST | `/api/roles` | ADMIN | 创建角色 |
| GET | `/api/menus` | ADMIN | 菜单树 |
| POST | `/api/menus` | ADMIN | 创建菜单 |

## Token 传递

所有需要认证的请求在 Header 中携带：
```
Authorization: Bearer <access_token>
```

Access Token 有效期 2 小时（可配置），Refresh Token 有效期 7 天。

## 错误码

| 码 | 含义 |
|----|------|
| 200 | 成功 |
| 500 | 服务器内部错误 |
| 1001 | Token 无效 |
| 1002 | Token 过期 |
| 1005 | 用户不存在 |
| 1006 | 用户已被禁用 |
| 1007 | 用户已锁定 |
| 3001 | 密码错误 |
| 5001 | 参数错误 |

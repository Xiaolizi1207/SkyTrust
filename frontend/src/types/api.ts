/** 统一后端响应格式 */
export interface ApiResult<T = any> {
  code: number
  message: string
  data: T
  timestamp: string
}

/** 登录请求参数 */
export interface LoginParams {
  username: string
  password: string
  captchaKey?: string
  captchaCode?: string
}

/** 登录响应数据 */
export interface LoginResult {
  accessToken: string
  tokenType: string
  expiresIn: number
  refreshToken: string
  user: UserInfo
}

/** 用户信息 */
export interface UserInfo {
  id: number
  username: string
  phone?: string
  email?: string
  realName?: string
  status: number
  role: string
  creditScore?: number
  lastLoginTime?: string
  createTime?: string
  updateTime?: string
}

/** 注册请求参数 */
export interface RegisterParams {
  username: string
  password: string
  confirmPassword: string
  phone: string
  email?: string
}

/** 验证码响应 */
export interface CaptchaResult {
  captchaKey: string
  captchaImage: string
}

/** 发送验证码请求参数 */
export interface SendCodeParams {
  phone?: string
  email?: string
}

/** 验证码登录请求参数 */
export interface CodeLoginParams {
  phone?: string
  email?: string
  code: string
}

/** 设备视图对象 */
export interface DeviceVO {
  id: number
  deviceName: string
  model: string
  serialNumber: string
  status: number
  latitude?: number
  longitude?: number
  altitude?: number
  batteryLevel?: number
  speed?: number
  totalFlightHours?: number
  ownerId: number
  rentalPrice: number
  insuranceFee: number
  description?: string
  images?: string
  specifications?: string
  lastOnlineTime?: string
  lastMaintenanceTime?: string
  remark?: string
  createTime?: string
  updateTime?: string
}

/** 设备创建/更新请求 */
export interface DeviceDTO {
  deviceName: string
  model: string
  serialNumber: string
  status: number
  latitude?: number
  longitude?: number
  altitude?: number
  batteryLevel?: number
  speed?: number
  totalFlightHours?: number
  ownerId: number
  rentalPrice: number
  insuranceFee: number
  description?: string
  images?: string
  specifications?: string
  remark?: string
}

/** 设备分页查询参数 */
export interface DeviceQueryParams {
  page?: number
  size?: number
  deviceName?: string
  model?: string
  serialNumber?: string
  status?: number
  minPrice?: number
  maxPrice?: number
  minBattery?: number
  orderBy?: string
}

/** 设备统计信息（对应后端 DeviceService.DeviceStatistics） */
export interface DeviceStatistics {
  totalDevices: number
  onlineDevices: number
  flyingDevices: number
  maintenanceDevices: number
  scrappedDevices: number
}

// ============================================================
// 租赁订单 (Rental Order)
// ============================================================

/** 租赁订单视图对象 (后端 VO 映射) */
export interface RentalOrderVO {
  id: number
  orderNo: string
  userId: number
  deviceId: number
  deviceName?: string
  deviceModel?: string
  deviceImage?: string
  rentalPrice?: number
  startTime?: string
  endTime?: string
  totalDays?: number
  totalAmount?: number
  amount?: number        // 别名兼容
  insuranceFee?: number
  dailyInsuranceFee?: number
  status: number        // 前端: 0=待支付 1=进行中 2=已完成 3=已取消
  payStatus?: number    // 0=未支付 1=已支付 2=退款中 3=已退款
  cancelReason?: string
  remark?: string
  createTime?: string
  updateTime?: string
}

/** 租赁订单创建/更新请求 */
export interface RentalOrderDTO {
  userId: number
  deviceId: number
  startTime: string
  endTime: string
  totalAmount?: number
  insuranceFee?: number
  remark?: string
}

/** 租赁订单查询参数 */
export interface RentalOrderQueryParams {
  page?: number
  size?: number
  userId?: number
  deviceId?: number
  status?: number
  orderNo?: string
}

// ============================================================
// 支付 (Payment)
// ============================================================

/** 支付记录视图对象 */
export interface PaymentVO {
  id: number
  transactionNo: string
  userId: number
  orderId?: number
  amount: number
  payType?: number       // 0=微信 1=支付宝 2=银行卡 3=钱包余额
  status?: number         // 0=待支付 1=支付成功 2=支付失败 3=已退款
  description?: string
  createTime?: string
  updateTime?: string
}

/** 支付记录创建/更新请求 */
export interface PaymentDTO {
  userId: number
  orderId?: number
  amount: number
  payType?: number
  description?: string
}

/** 支付记录查询参数 */
export interface PaymentQueryParams {
  page?: number
  size?: number
  userId?: number
  orderId?: number
  status?: number
  transactionNo?: string
}

// ============================================================
// 钱包 (Wallet)
// ============================================================

/** 钱包余额视图 */
export interface WalletVO {
  userId: number
  balance: number
}

/** 钱包交易记录 */
export interface WalletTransactionVO {
  id: number
  userId: number
  type?: number          // 0=充值 1=消费 2=退款 3=提现
  typeText?: string
  amount: number
  balanceBefore?: number
  balanceAfter?: number
  description?: string
  orderId?: number
  status?: number
  createTime?: string
}

/** 充值请求 */
export interface RechargeDTO {
  amount: number
  description?: string
}

// ============================================================
// 用户管理后台 (User Admin)
// ============================================================

/** 后台用户管理视图 (比 UserInfo 更详细) */
export interface UserAdminVO {
  id: number
  username: string
  phone?: string
  email?: string
  realName?: string
  idCard?: string
  status: number         // 0=禁用 1=正常 2=未激活
  role?: string
  creditScore?: number
  balance?: number
  lastLoginTime?: string
  createTime?: string
  updateTime?: string
}

/** 用户创建/更新请求（后台） */
export interface UserAdminDTO {
  username: string
  password?: string
  phone?: string
  email?: string
  realName?: string
  idCard?: string
  status: number
  role?: string
}

/** 用户查询参数 */
export interface UserQueryParams {
  page?: number
  size?: number
  username?: string
  phone?: string
  email?: string
  status?: number
}

// ============================================================
// 角色 (Role)
// ============================================================

/** 角色视图对象 */
export interface RoleVO {
  id: number
  roleName: string
  roleCode: string
  description?: string
  status?: number
  createTime?: string
  updateTime?: string
  menuIds?: number[]
}

/** 角色创建/更新请求 */
export interface RoleDTO {
  roleName: string
  roleCode: string
  description?: string
  status?: number
  menuIds?: number[]
}

/** 角色查询参数 */
export interface RoleQueryParams {
  page?: number
  size?: number
  roleName?: string
  roleCode?: string
}

// ============================================================
// 菜单 (Menu) — 完整类型定义，覆盖 api/menu.ts 的 MenuNode
// ============================================================

/** 菜单节点 (后端 MenuVO 映射，含权限字) */
export { type MenuNode } from '@/api/menu'

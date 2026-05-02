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

// ===================== 微信小程序特有类型 =====================

/** 微信登录请求 */
export interface WechatLoginParams {
  code: string
  encryptedData?: string
  iv?: string
}

/** 微信手机号解密请求 */
export interface DecryptPhoneParams {
  encryptedData: string
  iv: string
  sessionKey?: string
}

/** 租赁订单视图 */
export interface RentalOrderVO {
  id: number
  orderNo: string
  userId: number
  deviceId: number
  deviceName: string
  deviceModel: string
  deviceImage?: string
  startTime: string
  endTime: string
  totalDays: number
  rentalPrice: number
  insuranceFee: number
  totalAmount: number
  status: number // 0-待支付 1-进行中 2-已完成 3-已取消
  payStatus: number // 0-未支付 1-已支付
  cancelReason?: string
  remark?: string
  createTime: string
  updateTime?: string
}

/** 创建租赁订单请求 */
export interface CreateOrderParams {
  deviceId: number
  startTime: string
  endTime: string
  remark?: string
}

/** 支付请求 */
export interface PaymentParams {
  orderId: number
  paymentMethod: string // 'wechat'
}

/** 支付结果 */
export interface PaymentResult {
  paySign: string
  nonceStr: string
  package: string
  signType: string
  timeStamp: string
}

/** 订单查询参数 */
export interface OrderQueryParams {
  page?: number
  size?: number
  status?: number
}

/** 分页结果 */
export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

/** 钱包余额 */
export interface WalletVO {
  userId: number
  balance: number
}

/** 钱包交易记录 */
export interface WalletTransactionVO {
  id: number
  userId: number
  type: number
  typeText: string
  amount: number
  balanceBefore: number
  balanceAfter: number
  description?: string
  orderId?: number
  status: number
  createTime: string
}

/** 充值请求 */
export interface RechargeParams {
  amount: number
  description?: string
}

/** 修改密码 */
export interface PasswordUpdateParams {
  oldPassword: string
  newPassword: string
}

// ===================== 区块链 =====================

/** 无人机数字护照 */
export interface DronePassport {
  tokenId: number
  manufacturer: string
  manufactureDate: string
  serialNumber: string
  firmwareVersion: string
  batteryCycles: number
  repairCount: number
  repairHistory: RepairRecord[]
}

/** 维修记录 */
export interface RepairRecord {
  repairDataHash: string
  maintenanceProviderDID: string
  timestamp: number
}

/** 飞行日志条目 */
export interface FlightLogEntry {
  logHash: string
  timestamp: number
  verified: boolean
}

/** 执照记录 */
export interface LicenseRecord {
  userDID: string
  licenseHash: string
  orderId: string
  verified: boolean
}

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

/** 验证码响应 */
export interface CaptchaResult {
  captchaKey: string
  captchaImage: string
}

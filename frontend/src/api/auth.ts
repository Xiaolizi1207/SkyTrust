import request from '@/utils/request'
import type { ApiResult, LoginParams, LoginResult, CaptchaResult, RegisterParams, SendCodeParams, CodeLoginParams } from '@/types/api'

/** 登录 */
export function loginApi(data: LoginParams) {
  return request.post<ApiResult<LoginResult>>('/auth/login', data)
}

/** 获取图形验证码 */
export function getCaptchaApi() {
  return request.get<ApiResult<CaptchaResult>>('/auth/captcha')
}

/** 刷新令牌 */
export function refreshTokenApi(refreshToken: string) {
  return request.post<ApiResult<LoginResult>>('/auth/refresh', { refreshToken })
}

/** 注销 */
export function logoutApi() {
  return request.post<ApiResult<null>>('/auth/logout')
}

/** 获取当前用户信息 */
export function getUserInfoApi() {
  return request.get<ApiResult<any>>('/users/me')
}

/** 注册 */
export function registerApi(data: RegisterParams) {
  return request.post<ApiResult<LoginResult>>('/auth/register', data)
}

/** 发送验证码 */
export function sendCodeApi(data: SendCodeParams) {
  return request.post<ApiResult<null>>('/auth/send-code', data)
}

/** 验证码登录 */
export function codeLoginApi(data: CodeLoginParams) {
  return request.post<ApiResult<LoginResult>>('/auth/code-login', data)
}

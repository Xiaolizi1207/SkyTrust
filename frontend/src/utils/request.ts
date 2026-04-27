import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' },
})

/** 存储待刷新请求的队列 */
let isRefreshing = false
let pendingRequests: Array<{
  config: InternalAxiosRequestConfig
  resolve: (token: string) => void
  reject: (err: any) => void
}> = []

/** 不需要携带 Token 的公开路径 */
const PUBLIC_PATHS = [
  '/auth/login',
  '/auth/register',
  '/auth/refresh',
  '/auth/captcha',
  '/auth/send-code',
  '/auth/code-login',
  '/auth/forgot-password',
  '/auth/reset-password',
  '/users/register',
  '/users/login',
]

/** 判断是否为公开路径 */
function isPublicPath(url: string = ''): boolean {
  return PUBLIC_PATHS.some((path) => url.startsWith(path))
}

/** 从 localStorage 读取 accessToken */
function getAccessToken(): string | null {
  return localStorage.getItem('accessToken')
}

function getRefreshToken(): string | null {
  return localStorage.getItem('refreshToken')
}

function setTokens(access: string, refresh: string) {
  localStorage.setItem('accessToken', access)
  localStorage.setItem('refreshToken', refresh)
}

function clearTokens() {
  localStorage.removeItem('accessToken')
  localStorage.removeItem('refreshToken')
  localStorage.removeItem('userInfo')
}

// ===================== 请求拦截器 =====================
request.interceptors.request.use(
  (config) => {
    // 公开接口不携带 Token
    if (!isPublicPath(config.url)) {
      const token = getAccessToken()
      if (token && config.headers) {
        config.headers.Authorization = `Bearer ${token}`
      }
    }
    return config
  },
  (error) => Promise.reject(error),
)

// ===================== 响应拦截器 =====================
request.interceptors.response.use(
  (response) => {
    const res = response.data
    // 后端统一包了一层 ApiResult，code !== 200 视为业务错误
    if (res.code !== undefined && res.code !== 200) {
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return response
  },
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & {
      _retry?: boolean
    }

    // 401 且尚未重试过 → 尝试刷新令牌
    if (error.response?.status === 401 && !originalRequest._retry) {
      // 如果已经在刷新，排队等待
      if (isRefreshing) {
        return new Promise<string>((resolve, reject) => {
          pendingRequests.push({ config: originalRequest, resolve, reject })
        }).then((newToken) => {
          originalRequest.headers.Authorization = `Bearer ${newToken}`
          return request(originalRequest)
        })
      }

      originalRequest._retry = true
      isRefreshing = true

      try {
        const refreshToken = getRefreshToken()
        if (!refreshToken) throw new Error('无刷新令牌')

        const res = await axios.post('/api/auth/refresh', {
          refreshToken,
        })
        const { accessToken: newAccess, refreshToken: newRefresh } = res.data.data
        setTokens(newAccess, newRefresh)

        // 重放排队的请求
        pendingRequests.forEach((p) => p.resolve(newAccess))
        pendingRequests = []

        originalRequest.headers.Authorization = `Bearer ${newAccess}`
        return request(originalRequest)
      } catch {
        clearTokens()
        pendingRequests.forEach((p) => p.reject(error))
        pendingRequests = []
        window.location.href = '/login'
        return Promise.reject(error)
      } finally {
        isRefreshing = false
      }
    }

    return Promise.reject(error)
  },
)

export default request

/**
 * uni.request 封装 —— 替代 Axios
 * 提供请求/响应拦截器、Token 自动刷新、错误统一处理
 */

const API_BASE_URL = 'http://localhost:9090/api'

/** 存储待刷新请求的队列 */
let isRefreshing = false
let pendingRequests: Array<{
  config: UniRequestConfig
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
  '/auth/wechat-login',
  '/auth/decrypt-phone',
  '/users/register',
  '/users/login',
]

function isPublicPath(url: string = ''): boolean {
  return PUBLIC_PATHS.some((path) => url.startsWith(path))
}

function getAccessToken(): string {
  return uni.getStorageSync('accessToken') || ''
}

function getRefreshToken(): string {
  return uni.getStorageSync('refreshToken') || ''
}

function setTokens(access: string, refresh: string) {
  uni.setStorageSync('accessToken', access)
  uni.setStorageSync('refreshToken', refresh)
}

function clearTokens() {
  uni.removeStorageSync('accessToken')
  uni.removeStorageSync('refreshToken')
  uni.removeStorageSync('userInfo')
}

export interface UniRequestConfig {
  url: string
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
  data?: any
  header?: Record<string, string>
  /** 是否跳过 Token 刷新重试（内部使用） */
  _retry?: boolean
}

export interface UniResponse<T = any> {
  statusCode: number
  data: {
    code: number
    message: string
    data: T
    timestamp: string
  }
}

/**
 * 核心请求函数
 */
function request<T = any>(config: UniRequestConfig): Promise<UniResponse<T>> {
  return new Promise((resolve, reject) => {
    const method = config.method || 'GET'
    const url = config.url.startsWith('http') ? config.url : `${API_BASE_URL}${config.url}`

    // 构建请求头
    const header: Record<string, string> = {
      'Content-Type': 'application/json',
      ...config.header,
    }

    // 非公开路径自动带 Token
    if (!isPublicPath(config.url)) {
      const token = getAccessToken()
      if (token) {
        header['Authorization'] = `Bearer ${token}`
      }
    }

    uni.request({
      url,
      method,
      data: config.data,
      header,
      success: (res) => {
        // HTTP 状态码错误（如 403、404、500 等）
        if (res.statusCode !== 200) {
          const body = res.data as any
          reject(new Error(body?.message || body?.error || `请求失败 (${res.statusCode})`))
          return
        }

        const responseData = res.data as { code: number; message: string; data: T; timestamp: string }

        // 业务错误
        if (responseData.code !== undefined && responseData.code !== 200) {
          reject(new Error(responseData.message || '请求失败'))
          return
        }

        resolve({
          statusCode: res.statusCode,
          data: responseData,
        })
      },
      fail: async (err) => {
        // 401 未授权 → 尝试刷新 Token
        if (err.errMsg?.includes('401') && !config._retry) {
          if (isRefreshing) {
            // 排队等待刷新
            try {
              const newToken = await new Promise<string>((resolveToken, rejectToken) => {
                pendingRequests.push({ config, resolve: resolveToken, reject: rejectToken })
              })
              header['Authorization'] = `Bearer ${newToken}`
              // 重试原请求
              try {
                const retryRes = await request<T>({ ...config, _retry: true })
                resolve(retryRes)
              } catch (retryErr) {
                reject(retryErr)
              }
            } catch (queueErr) {
              reject(queueErr)
            }
            return
          }

          isRefreshing = true
          try {
            const refreshTokenStr = getRefreshToken()
            if (!refreshTokenStr) throw new Error('无刷新令牌')

            const refreshRes = await request<{ accessToken: string; refreshToken: string }>({
              url: '/auth/refresh',
              method: 'POST',
              data: { refreshToken: refreshTokenStr },
            })

            const { accessToken: newAccess, refreshToken: newRefresh } = refreshRes.data.data
            setTokens(newAccess, newRefresh)

            // 重放排队请求
            pendingRequests.forEach((p) => p.resolve(newAccess))
            pendingRequests = []

            // 重试原请求
            header['Authorization'] = `Bearer ${newAccess}`
            const retryRes = await request<T>({ ...config, _retry: true })
            resolve(retryRes)
          } catch (refreshErr) {
            clearTokens()
            pendingRequests.forEach((p) => p.reject(refreshErr))
            pendingRequests = []
            // 跳转登录页
            uni.reLaunch({ url: '/pages/login/index' })
            reject(refreshErr)
          } finally {
            isRefreshing = false
          }
          return
        }

        // 网络错误
        let errorMsg = '网络请求失败'
        if (err.errMsg) {
          if (err.errMsg.includes('timeout')) errorMsg = '请求超时，请检查网络'
          else if (err.errMsg.includes('fail')) errorMsg = '网络连接失败，请检查网络'
        }

        uni.showToast({ title: errorMsg, icon: 'none', duration: 2000 })
        reject(new Error(errorMsg))
      },
    })
  })
}

/** GET 请求 */
request.get = <T = any>(url: string, params?: Record<string, any>): Promise<UniResponse<T>> => {
  let queryString = ''
  if (params) {
    const entries = Object.entries(params).filter(([, v]) => v !== undefined && v !== null && v !== '')
    if (entries.length > 0) {
      queryString = '?' + entries.map(([k, v]) => `${k}=${encodeURIComponent(v)}`).join('&')
    }
  }
  return request<T>({ url: `${url}${queryString}`, method: 'GET' })
}

/** POST 请求 */
request.post = <T = any>(url: string, data?: any): Promise<UniResponse<T>> => {
  return request<T>({ url, method: 'POST', data })
}

/** PUT 请求 */
request.put = <T = any>(url: string, data?: any): Promise<UniResponse<T>> => {
  return request<T>({ url, method: 'PUT', data })
}

/** DELETE 请求 */
request.delete = <T = any>(url: string): Promise<UniResponse<T>> => {
  return request<T>({ url, method: 'DELETE' })
}

export default request

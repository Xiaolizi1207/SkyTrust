import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { LoginParams, UserInfo, WechatLoginParams } from '@/types/api'
import {
  loginApi,
  wechatLoginApi,
  logoutApi,
  getUserInfoApi,
} from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  // ========== 状态（uni.storage 替代 localStorage） ==========
  const accessToken = ref(uni.getStorageSync('accessToken') || '')
  const refreshToken = ref(uni.getStorageSync('refreshToken') || '')
  const user = ref<UserInfo | null>(
    uni.getStorageSync('userInfo')
      ? JSON.parse(uni.getStorageSync('userInfo'))
      : null,
  )

  // ========== 计算属性 ==========
  const isAuthenticated = computed(() => !!accessToken.value)

  // ========== 方法 ==========

  /** 保存令牌 */
  function saveTokens(access: string, refresh: string) {
    accessToken.value = access
    refreshToken.value = refresh
    uni.setStorageSync('accessToken', access)
    uni.setStorageSync('refreshToken', refresh)
  }

  /** 保存用户信息 */
  function saveUser(userInfo: UserInfo) {
    user.value = userInfo
    uni.setStorageSync('userInfo', JSON.stringify(userInfo))
  }

  /** 密码登录 */
  async function login(params: LoginParams) {
    const res = await loginApi(params)
    const data = res.data.data as any
    const { accessToken: access, refreshToken: refresh, user: userInfo } = data
    saveTokens(access, refresh)
    saveUser(userInfo)
    return data
  }

  /** 微信一键登录 */
  async function wechatLogin(params: WechatLoginParams) {
    const res = await wechatLoginApi(params)
    const data = res.data.data as any
    const { accessToken: access, refreshToken: refresh, user: userInfo } = data
    saveTokens(access, refresh)
    saveUser(userInfo)
    return data
  }

  /** 刷新令牌回调 */
  function setRefreshedTokens(access: string, refresh: string) {
    saveTokens(access, refresh)
  }

  /** 获取用户信息 */
  async function fetchUserInfo() {
    try {
      const res = await getUserInfoApi()
      saveUser(res.data.data as any)
    } catch {
      // token 失效由请求拦截器处理
    }
  }

  /** 注销 */
  async function logout() {
    try {
      await logoutApi()
    } catch {
      // 即使接口失败也清除本地状态
    } finally {
      accessToken.value = ''
      refreshToken.value = ''
      user.value = null
      uni.removeStorageSync('accessToken')
      uni.removeStorageSync('refreshToken')
      uni.removeStorageSync('userInfo')
    }
  }

  return {
    accessToken,
    refreshToken,
    user,
    isAuthenticated,
    login,
    wechatLogin,
    saveTokens,
    saveUser,
    logout,
    fetchUserInfo,
    setRefreshedTokens,
  }
})

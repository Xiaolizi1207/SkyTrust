import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { LoginParams, UserInfo } from '@/types/api'
import type { MenuNode } from '@/api/menu'
import { loginApi, logoutApi, getUserInfoApi } from '@/api/auth'
import { getUserMenusApi } from '@/api/menu'

export const useAuthStore = defineStore('auth', () => {
  // ========== 状态 ==========
  const accessToken = ref(localStorage.getItem('accessToken') || '')
  const refreshToken = ref(localStorage.getItem('refreshToken') || '')
  const user = ref<UserInfo | null>(JSON.parse(localStorage.getItem('userInfo') || 'null'))
  const menus = ref<MenuNode[]>([])

  // ========== 计算属性 ==========
  const isAuthenticated = computed(() => !!accessToken.value)

  // ========== 方法 ==========

  /** 保存令牌到 state 和 localStorage */
  function saveTokens(access: string, refresh: string) {
    accessToken.value = access
    refreshToken.value = refresh
    localStorage.setItem('accessToken', access)
    localStorage.setItem('refreshToken', refresh)
  }

  /** 保存用户信息 */
  function saveUser(userInfo: UserInfo) {
    user.value = userInfo
    localStorage.setItem('userInfo', JSON.stringify(userInfo))
  }

  /** 登录 */
  async function login(params: LoginParams) {
    const res = await loginApi(params)
    const { accessToken: access, refreshToken: refresh, user: userInfo } = res.data.data
    saveTokens(access, refresh)
    saveUser(userInfo)
    // 登录成功后获取菜单
    await fetchMenus()
    return res.data.data
  }

  /** 刷新令牌 */
  function setRefreshedTokens(access: string, refresh: string) {
    saveTokens(access, refresh)
  }

  /** 获取用户信息 */
  async function fetchUserInfo() {
    try {
      const res = await getUserInfoApi()
      saveUser(res.data.data)
    } catch {
      // token 失效由拦截器处理
    }
  }

  /** 获取当前用户菜单树 */
  async function fetchMenus() {
    try {
      const res = await getUserMenusApi()
      menus.value = res.data.data || []
    } catch {
      menus.value = []
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
      menus.value = []
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('userInfo')
    }
  }

  return {
    accessToken,
    refreshToken,
    user,
    menus,
    isAuthenticated,
    login,
    saveTokens,
    saveUser,
    logout,
    fetchUserInfo,
    fetchMenus,
    setRefreshedTokens,
  }
})

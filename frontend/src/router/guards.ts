import type { Router } from 'vue-router'
import { useAuthStore } from '@/store/auth'

export function setupGuards(router: Router) {
  router.beforeEach((to, _from, next) => {
    const authStore = useAuthStore()

    // 页面设置了 requiresAuth: true 但未登录 → 跳登录页
    if (to.meta.requiresAuth !== false && !authStore.isAuthenticated) {
      return next({ name: 'Login', query: { redirect: to.fullPath } })
    }

    // 已登录用户访问登录页或介绍首页 → 跳仪表盘（登录后的真正首页）
    if ((to.name === 'Login' || to.name === 'Home') && authStore.isAuthenticated) {
      return next({ name: 'Dashboard' })
    }

    next()
  })
}

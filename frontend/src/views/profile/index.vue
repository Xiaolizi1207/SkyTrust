<template>
  <div class="profile-page">
    <div class="profile-header">
      <div class="avatar-box">{{ initials }}</div>
      <div class="header-info">
        <h2>{{ userDisplayName }}</h2>
        <p class="user-role">{{ roleText }}</p>
      </div>
    </div>

    <div class="section">
      <h3 class="section-title">基本信息</h3>
      <div class="info-grid">
        <div class="info-item">
          <span class="label">用户名</span>
          <span class="value">{{ authStore.user?.username || '-' }}</span>
        </div>
        <div class="info-item">
          <span class="label">真实姓名</span>
          <span class="value">{{ authStore.user?.realName || '-' }}</span>
        </div>
        <div class="info-item">
          <span class="label">手机号</span>
          <span class="value">{{ authStore.user?.phone || '-' }}</span>
        </div>
        <div class="info-item">
          <span class="label">邮箱</span>
          <span class="value">{{ authStore.user?.email || '-' }}</span>
        </div>
        <div class="info-item">
          <span class="label">角色</span>
          <span class="value">{{ roleText }}</span>
        </div>
        <div class="info-item">
          <span class="label">注册时间</span>
          <span class="value">{{ formatDate(authStore.user?.createTime) }}</span>
        </div>
        <div class="info-item">
          <span class="label">最后登录</span>
          <span class="value">{{ formatDate(authStore.user?.lastLoginTime) }}</span>
        </div>
      </div>
    </div>

    <div class="section">
      <h3 class="section-title">账号安全</h3>
      <div class="security-list">
        <div class="security-item">
          <span class="s-label">密码</span>
          <span class="s-status">已设置</span>
          <button class="btn btn-sm btn-secondary">修改密码</button>
        </div>
        <div class="security-item">
          <span class="s-label">手机号</span>
          <span class="s-status">{{ authStore.user?.phone ? '已绑定' : '未绑定' }}</span>
          <button class="btn btn-sm btn-secondary">{{ authStore.user?.phone ? '更换绑定' : '绑定手机' }}</button>
        </div>
      </div>
    </div>

    <div class="section">
      <h3 class="section-title">统计信息</h3>
      <div class="stats-row">
        <div class="stat-box">
          <span class="stat-num">0</span>
          <span class="stat-desc">租赁订单</span>
        </div>
        <div class="stat-box">
          <span class="stat-num">0</span>
          <span class="stat-desc">注册无人机</span>
        </div>
        <div class="stat-box">
          <span class="stat-num">0</span>
          <span class="stat-desc">飞行小时</span>
        </div>
      </div>
    </div>

    <div class="actions">
      <router-link to="/admin/dashboard" class="btn btn-primary">返回首页</router-link>
      <button class="btn btn-secondary" @click="handleLogout">退出登录</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'

const router = useRouter()
const authStore = useAuthStore()

const userDisplayName = computed(() => authStore.user?.realName || authStore.user?.username || '用户')
const roleText = computed(() => {
  const r = authStore.user?.role
  if (r === 'ADMIN') return '管理员'
  if (r === 'PILOT') return '飞行员'
  return r || '普通用户'
})
const initials = computed(() => {
  const name = userDisplayName.value
  return name ? name.charAt(0).toUpperCase() : 'U'
})

function formatDate(ts?: string | number | null) {
  if (!ts) return '-'
  try { return new Date(ts).toLocaleString('zh-CN') } catch { return String(ts) }
}

async function handleLogout() {
  await authStore.logout()
  router.replace('/login')
}
</script>

<style scoped>
.profile-page { max-width: 800px; margin: 0 auto; }

.profile-header { display: flex; align-items: center; gap: 24px; margin-bottom: 40px; padding: 24px 0; border-bottom: 2px solid #000; }
.avatar-box { width: 64px; height: 64px; border: 2px solid #000; display: flex; align-items: center; justify-content: center; font-size: 28px; font-weight: 700; flex-shrink: 0; }
.header-info h2 { margin: 0 0 4px 0; font-size: 22px; font-weight: 700; }
.user-role { color: #666; font-size: 14px; margin: 0; }

.section { margin-bottom: 36px; }
.section-title { font-size: 16px; font-weight: 700; margin: 0 0 16px 0; padding-bottom: 10px; border-bottom: 1px solid #e0e0e0; }

.info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.info-item { display: flex; flex-direction: column; gap: 4px; padding: 12px; background: #fff; border: 1px solid #e0e0e0; }
.label { font-size: 12px; color: #999; }
.value { font-size: 15px; font-weight: 600; color: #000; }

.security-list { display: flex; flex-direction: column; gap: 0; }
.security-item { display: flex; align-items: center; padding: 14px 0; border-bottom: 1px solid #e0e0e0; gap: 24px; }
.security-item:last-child { border-bottom: none; }
.s-label { font-weight: 600; width: 80px; flex-shrink: 0; }
.s-status { flex: 1; color: #666; font-size: 14px; }

.stats-row { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; }
.stat-box { padding: 20px; background: #fff; border: 1px solid #e0e0e0; text-align: center; }
.stat-num { display: block; font-size: 28px; font-weight: 700; color: #000; }
.stat-desc { font-size: 13px; color: #999; margin-top: 4px; }

.actions { display: flex; gap: 12px; padding-top: 24px; border-top: 1px solid #e0e0e0; }

@media (max-width: 768px) {
  .info-grid, .stats-row { grid-template-columns: 1fr; }
}
</style>

<template>
  <div class="dashboard">
    <header class="header">
      <h1>SkyTrust 仪表盘</h1>
      <div class="user-info">
        <span>{{ authStore.user?.username }}</span>
        <button class="btn-logout" @click="handleLogout">退出登录</button>
      </div>
    </header>
    <main class="main">
      <p>欢迎回来，{{ authStore.user?.realName || authStore.user?.username }}</p>
    </main>
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from '@/store/auth'
import { useRouter } from 'vue-router'

const authStore = useAuthStore()
const router = useRouter()

async function handleLogout() {
  await authStore.logout()
  router.replace('/login')
}
</script>

<style scoped>
.dashboard {
  min-height: 100vh;
  background: #f5f7fa;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 32px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}
.header h1 {
  font-size: 18px;
  color: #1a73e8;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 14px;
  color: #333;
}
.btn-logout {
  padding: 6px 16px;
  background: none;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}
.btn-logout:hover {
  border-color: #dc2626;
  color: #dc2626;
}
.main {
  padding: 32px;
  font-size: 16px;
  color: #555;
}
</style>

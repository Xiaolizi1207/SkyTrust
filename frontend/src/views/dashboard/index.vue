<template>
  <div class="dashboard">
    <!-- ========== 统计卡片 ========== -->
    <div class="stat-row">
      <div class="stat-card">
        <div class="stat-icon device-icon">🛸</div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.deviceTotal }}</span>
          <span class="stat-label">设备总数</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon online-icon">🟢</div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.deviceOnline }}</span>
          <span class="stat-label">在线设备</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon order-icon">📋</div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.todayOrders }}</span>
          <span class="stat-label">今日订单</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon user-icon">👤</div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.activeUsers }}</span>
          <span class="stat-label">活跃用户</span>
        </div>
      </div>
    </div>

    <div class="content-row">
      <!-- ========== 最近活动 ========== -->
      <div class="panel">
        <h3 class="panel-title">最近活动</h3>
        <div class="activity-list">
          <div v-if="activities.length === 0" class="empty-tip">暂无最近活动</div>
          <div v-for="(act, idx) in activities" :key="idx" class="activity-item">
            <span class="activity-dot"></span>
            <span class="activity-text">{{ act }}</span>
          </div>
        </div>
      </div>

      <!-- ========== 快速入口 ========== -->
      <div class="panel">
        <h3 class="panel-title">快速入口</h3>
        <div class="quick-links">
          <div class="quick-item" @click="goTo('/admin/dashboard')">
            <span class="quick-icon">📊</span>
            <span>仪表盘</span>
          </div>
          <div class="quick-item" @click="goTo('/admin/device')">
            <span class="quick-icon">🛸</span>
            <span>设备管理</span>
          </div>
          <div class="quick-item" @click="goTo('/admin/orders')">
            <span class="quick-icon">📋</span>
            <span>我的订单</span>
          </div>
          <div class="quick-item" @click="goTo('/admin/profile')">
            <span class="quick-icon">👤</span>
            <span>个人中心</span>
          </div>
        </div>
      </div>
    </div>

    <!-- ========== 欢迎信息 ========== -->
    <div class="welcome-bar">
      <p>欢迎回来，{{ authStore.user?.realName || authStore.user?.username }}！今天是 {{ currentDate }}。</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'

const router = useRouter()
const authStore = useAuthStore()

const currentDate = new Date().toLocaleDateString('zh-CN', {
  year: 'numeric',
  month: 'long',
  day: 'numeric',
  weekday: 'long',
})

const stats = reactive({
  deviceTotal: 0,
  deviceOnline: 0,
  todayOrders: 0,
  activeUsers: 0,
})

const activities = ref<string[]>([
  '登录成功 - ' + (authStore.user?.lastLoginTime || '刚刚'),
  '欢迎使用 SkyTrust 无人机共享租赁平台',
])

function goTo(path: string) {
  router.push(path)
}
</script>

<style scoped>
.dashboard {
  max-width: 1200px;
}

/* ========== 统计卡片 ========== */
.stat-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}
.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
}
.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  flex-shrink: 0;
}
.device-icon { background: #e8f4fd; }
.online-icon { background: #e8faf0; }
.order-icon { background: #fef3e2; }
.user-icon { background: #f0e8fd; }
.stat-info {
  display: flex;
  flex-direction: column;
}
.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a2e;
  line-height: 1.2;
}
.stat-label {
  font-size: 13px;
  color: #888;
  margin-top: 2px;
}

/* ========== 内容行 ========== */
.content-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 24px;
}
@media (max-width: 768px) {
  .content-row {
    grid-template-columns: 1fr;
  }
}
.panel {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
}
.panel-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

/* ========== 最近活动 ========== */
.activity-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.activity-item {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  color: #555;
}
.activity-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #1a73e8;
  flex-shrink: 0;
}
.activity-text {
  line-height: 1.5;
}
.empty-tip {
  color: #999;
  font-size: 14px;
  text-align: center;
  padding: 20px;
}

/* ========== 快速入口 ========== */
.quick-links {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}
.quick-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 20px 12px;
  background: #f8f9fb;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s, transform 0.15s;
  font-size: 14px;
  color: #555;
}
.quick-item:hover {
  background: #e8f0fe;
  transform: translateY(-2px);
}
.quick-icon {
  font-size: 28px;
}

/* ========== 欢迎信息 ========== */
.welcome-bar {
  background: linear-gradient(135deg, #1a73e8, #0d47a1);
  color: #fff;
  padding: 16px 20px;
  border-radius: 8px;
  font-size: 14px;
  box-shadow: 0 2px 8px rgba(26, 115, 232, 0.3);
}
</style>

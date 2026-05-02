<template>
  <div class="dashboard">
    <!-- ========== 物联网模块 ========== -->
    <h3 class="section-title">物联网模块</h3>
    <div class="stat-row">
      <div class="stat-card">
        <div class="stat-icon">D</div>
        <div class="stat-info"><span class="stat-value">{{ stats.deviceTotal }}</span><span class="stat-label">设备总数</span></div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">■</div>
        <div class="stat-info"><span class="stat-value">{{ stats.deviceOnline }}</span><span class="stat-label">在线设备</span></div>
      </div>
    </div>

    <!-- ========== 实时更新 ========== -->
    <h3 class="section-title">实时更新</h3>
    <div class="stat-row">
      <div class="stat-card">
        <div class="stat-icon">O</div>
        <div class="stat-info"><span class="stat-value">{{ stats.todayOrders }}</span><span class="stat-label">今日订单</span></div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">U</div>
        <div class="stat-info"><span class="stat-value">{{ stats.activeUsers }}</span><span class="stat-label">活跃用户</span></div>
      </div>
    </div>

    <!-- ========== 快速入口 ========== -->
    <div class="module-grid quick-links">
      <div class="module-card" @click="goTo('/admin/device')">
        <div class="module-icon">D</div>
        <div class="module-info"><span class="module-name">设备管理</span><span class="module-desc">管理所有注册无人机</span></div>
      </div>
      <div class="module-card" @click="goTo('/admin/orders')">
        <div class="module-icon">O</div>
        <div class="module-info"><span class="module-name">订单管理</span><span class="module-desc">查看与处理租赁订单</span></div>
      </div>
      <div class="module-card" @click="goTo('/admin/profile')">
        <div class="module-icon">U</div>
        <div class="module-info"><span class="module-name">个人中心</span><span class="module-desc">账号信息与安全设置</span></div>
      </div>
    </div>

    <!-- ========== 区块链模块入口 ========== -->
    <h3 class="section-title">区块链模块</h3>
    <div class="module-grid">
      <div class="module-card" @click="goTo('/admin/blockchain/passport')">
        <div class="module-icon">P</div>
        <div class="module-info">
          <span class="module-name">数字护照</span>
          <span class="module-desc">查看无人机 NFT 护照、维修记录</span>
        </div>
      </div>
      <div class="module-card" @click="goTo('/admin/blockchain/licensing')">
        <div class="module-icon">L</div>
        <div class="module-info">
          <span class="module-name">执照验证</span>
          <span class="module-desc">飞手执照上链、租用订单绑定</span>
        </div>
      </div>
      <div class="module-card" @click="goTo('/admin/blockchain/flight-log')">
        <div class="module-icon">F</div>
        <div class="module-info">
          <span class="module-name">飞行日志</span>
          <span class="module-desc">日志哈希上链、完整性校验</span>
        </div>
      </div>
    </div>

    <!-- ========== AI 智能模块入口 ========== -->
    <h3 class="section-title">AI 智能模块</h3>
    <div class="module-grid">
      <div class="module-card" @click="goTo('/admin/geofence')">
        <div class="module-icon">G</div>
        <div class="module-info">
          <span class="module-name">地理围栏</span>
          <span class="module-desc">禁飞区配置、位置实时检测</span>
        </div>
      </div>
      <div class="module-card" @click="goTo('/admin/pricing')">
        <div class="module-icon">¥</div>
        <div class="module-info">
          <span class="module-name">动态定价</span>
          <span class="module-desc">需求预测、调度任务管理</span>
        </div>
      </div>
      <div class="module-card" @click="goTo('/admin/ai/chatbot')">
        <div class="module-icon">AI</div>
        <div class="module-info">
          <span class="module-name">AI 助手</span>
          <span class="module-desc">飞行检查清单、参数一键下发</span>
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
.dashboard { max-width: 1200px; }

/* ========== 统计卡片 ========== */
.stat-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 28px;
}
.stat-card {
  background: #fff;
  border: 2px solid #000;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  transition: background 0.1s;
}
.stat-card:hover { background: #f5f5f5; }
.stat-icon {
  width: 44px;
  height: 44px;
  border: 2px solid #000;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 18px;
  flex-shrink: 0;
  background: #fff;
}
.stat-value { font-size: 28px; font-weight: 700; color: #000; line-height: 1.2; }
.stat-label { font-size: 13px; color: #666; margin-top: 2px; }

/* ========== 快速入口 ========== */
.quick-links { margin-bottom: 8px; }

@media (max-width: 768px) {
  .stat-row { grid-template-columns: repeat(2, 1fr); }
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
  border: 1px solid #e0e0e0;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
}
.stat-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  flex-shrink: 0;
  border: 1px solid #000;
  background: #fff;
}
.stat-info {
  display: flex;
  flex-direction: column;
}
.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #000;
  line-height: 1.2;
}
.stat-label {
  font-size: 13px;
  color: #666;
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
  border: 1px solid #e0e0e0;
  padding: 20px;
}
.panel-title {
  font-size: 15px;
  font-weight: 600;
  color: #000;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 2px solid #000;
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
  background: #000;
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
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}
.quick-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 20px 12px;
  background: #fff;
  border: 1px solid #ccc;
  cursor: pointer;
  transition: border-color 0.2s, background 0.2s;
  font-size: 14px;
  color: #333;
}
.quick-item:hover {
  border-color: #000;
  background: #fafafa;
}
.quick-icon {
  font-size: 28px;
}

/* ========== 欢迎信息 ========== */
.welcome-bar {
  background: #000;
  color: #fff;
  padding: 16px 20px;
  font-size: 14px;
  font-weight: 600;
  margin-top: 20px;
}

/* ========== 模块入口 (统一样式) ========== */
.section-title {
  font-size: 16px;
  font-weight: 700;
  color: #000;
  margin: 28px 0 14px 0;
}
.module-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-bottom: 8px;
}
@media (max-width: 768px) {
  .module-grid { grid-template-columns: 1fr; }
}
.module-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 16px;
  background: #fff;
  border: 2px solid #000;
  cursor: pointer;
  transition: background 0.1s;
}
.module-card:hover {
  background: #f5f5f5;
}
.module-icon {
  width: 44px;
  height: 44px;
  border: 2px solid #000;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 18px;
  flex-shrink: 0;
  background: #fff;
}
.module-info { display: flex; flex-direction: column; gap: 2px; }
.module-name { font-size: 14px; font-weight: 600; color: #000; }
.module-desc { font-size: 12px; color: #666; }

@media (max-width: 768px) { .module-grid { grid-template-columns: 1fr; } }
</style>

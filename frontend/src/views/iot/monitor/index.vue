<template>
  <div class="iot-page">
    <div class="page-header">
      <h2>物联网设备监控</h2>
      <span class="subtitle">实时监控所有在线无人机状态与遥测数据</span>
      <span class="connection-badge" :class="wsConnected ? 'online' : 'offline'">
        {{ wsConnected ? '实时连接中' : '未连接' }}
      </span>
    </div>

    <div class="stat-row">
      <div class="stat-card">
        <div class="stat-icon">D</div>
        <div class="stat-info">
          <span class="stat-value">{{ registeredCount }}</span>
          <span class="stat-label">注册设备</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">●</div>
        <div class="stat-info">
          <span class="stat-value">{{ onlineCount }}</span>
          <span class="stat-label">在线设备</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">▲</div>
        <div class="stat-info">
          <span class="stat-value">{{ flyingCount }}</span>
          <span class="stat-label">飞行中</span>
        </div>
      </div>
    </div>

    <div v-if="!wsConnected && !loading" class="warning-banner">
      无法连接到实时服务器 - 显示缓存数据
    </div>

    <div class="card">
      <h3 class="section-title">设备列表</h3>
      <table>
        <thead>
          <tr>
            <th>设备名称</th>
            <th>设备ID</th>
            <th>状态</th>
            <th>GPS 坐标</th>
            <th>电池</th>
            <th>最后心跳</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="6" class="empty-state">加载中...</td>
          </tr>
          <tr v-else-if="deviceList.length === 0">
            <td colspan="6" class="empty-state">暂无设备连接，请等待设备上线或检查 MQTT Broker 配置</td>
          </tr>
          <tr v-for="dev in deviceList" :key="dev.id">
            <td>{{ dev.deviceName }}</td>
            <td>{{ dev.id }}</td>
            <td><span class="tag" :class="statusClass(dev.status)">{{ statusText(dev.status) }}</span></td>
            <td>
              <span v-if="dev.latitude != null">{{ dev.latitude?.toFixed(4) }}, {{ dev.longitude?.toFixed(4) }}</span>
              <span v-else class="text-muted">--</span>
            </td>
            <td>
              <div class="battery-bar">
                <div class="battery-fill" :class="batteryClass(dev.batteryLevel)" :style="{ width: (dev.batteryLevel ?? 0) + '%' }"></div>
                <span class="battery-text">{{ dev.batteryLevel ?? '--' }}%</span>
              </div>
            </td>
            <td>{{ dev.lastOnlineTime ? formatRelative(dev.lastOnlineTime) : '--' }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getDeviceListApi } from '@/api/device'
import { useWebSocket } from '@/composables/useWebSocket'
import type { DeviceVO, DeviceTelemetry } from '@/types/api'

const { connected: wsConnected, telemetry } = useWebSocket()

const loading = ref(true)
const allDevices = ref<DeviceVO[]>([])

const deviceList = computed(() => {
  return allDevices.value.map((dev) => {
    const realtime: DeviceTelemetry | undefined = telemetry.value.get(dev.id)
    if (realtime) {
      return {
        ...dev,
        status: realtime.status ?? dev.status,
        latitude: realtime.latitude ?? dev.latitude,
        longitude: realtime.longitude ?? dev.longitude,
        batteryLevel: realtime.batteryLevel ?? dev.batteryLevel,
        speed: realtime.speed ?? dev.speed,
      }
    }
    return dev
  })
})

const onlineCount = computed(() => deviceList.value.filter((d) => d.status === 1 || d.status === 2).length)
const flyingCount = computed(() => deviceList.value.filter((d) => d.status === 2).length)
const registeredCount = computed(() => allDevices.value.length)

function statusClass(status: number) {
  const map: Record<number, string> = { 0: 'offline', 1: 'online', 2: 'flying', 3: 'maintenance', 4: 'scrapped' }
  return map[status] || ''
}
function statusText(status: number) {
  const map: Record<number, string> = { 0: '离线', 1: '在线', 2: '飞行中', 3: '维护中', 4: '已报废' }
  return map[status] || '未知'
}
function batteryClass(level?: number) {
  if (level == null) return ''
  if (level < 20) return 'low'
  if (level < 50) return 'mid'
  return 'high'
}
function formatRelative(iso?: string) {
  if (!iso) return '--'
  const diff = Date.now() - new Date(iso).getTime()
  const sec = Math.floor(diff / 1000)
  if (sec < 60) return sec + '秒前'
  if (sec < 3600) return Math.floor(sec / 60) + '分钟前'
  return Math.floor(sec / 3600) + '小时前'
}

onMounted(async () => {
  try {
    const res = await getDeviceListApi({ page: 1, size: 100 })
    allDevices.value = res.data.data || []
  } catch {
    // keep empty list
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.iot-page { max-width: 1100px; }
.page-header { margin-bottom: 24px; display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.page-header h2 { margin: 0; font-size: 20px; font-weight: 700; }
.subtitle { color: #666; font-size: 13px; }
.connection-badge { font-size: 12px; padding: 2px 10px; font-weight: 600; }
.connection-badge.online { background: #e8f5e9; color: #2e7d32; }
.connection-badge.offline { background: #fff3e0; color: #e65100; }
.warning-banner { background: #fff3e0; border: 1px solid #e65100; padding: 10px 16px; margin-bottom: 16px; font-size: 13px; color: #e65100; }
.stat-row { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; margin-bottom: 24px; }
.stat-card { background: #fff; border: 2px solid #000; padding: 20px; display: flex; align-items: center; gap: 14px; }
.stat-icon { width: 44px; height: 44px; border: 2px solid #000; display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 18px; flex-shrink: 0; }
.stat-info { display: flex; flex-direction: column; }
.stat-value { font-size: 28px; font-weight: 700; color: #000; }
.stat-label { font-size: 13px; color: #666; }
.card { background: #fff; border: 1px solid #e0e0e0; padding: 20px; margin-bottom: 20px; }
.section-title { font-size: 16px; font-weight: 700; margin: 0 0 16px; padding-bottom: 10px; border-bottom: 2px solid #000; }
table { width: 100%; border-collapse: collapse; font-size: 14px; }
th, td { padding: 10px 12px; text-align: left; border-bottom: 1px solid #e0e0e0; white-space: nowrap; }
th { background: #f5f5f5; font-weight: 600; }
.empty-state { text-align: center; padding: 24px; color: #999; }
.text-muted { color: #999; }
.tag { display: inline-block; padding: 2px 8px; font-size: 12px; font-weight: 600; border: 1px solid #000; }
.tag.online { background: #e8f5e9; color: #2e7d32; }
.tag.offline { background: #f5f5f5; color: #999; }
.tag.flying { background: #e3f2fd; color: #1565c0; }
.tag.maintenance { background: #fff3e0; color: #e65100; }
.tag.scrapped { background: #fbe9e7; color: #b71c1c; }
.battery-bar { display: flex; align-items: center; gap: 8px; min-width: 100px; }
.battery-fill { height: 14px; min-width: 4px; background: #4caf50; transition: width 0.3s; }
.battery-fill.low { background: #e53935; }
.battery-fill.mid { background: #ff9800; }
.battery-text { font-size: 12px; font-weight: 600; }
@media (max-width: 768px) { .stat-row { grid-template-columns: 1fr; } }
</style>

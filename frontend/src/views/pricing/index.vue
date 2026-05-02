<template>
  <section class="pricing-page page-container">
    <!-- Header -->
    <div class="card page-header">
      <div>
        <h2>动态定价仪表盘</h2>
        <span class="subtitle">供需预测 · 价格调控 · 运维调度</span>
      </div>
      <div class="header-controls">
        <label>机库选择</label>
        <select v-model="selectedBase" class="input" @change="onBaseChange">
          <option value="">全部机库</option>
          <option v-for="b in baseList" :key="b" :value="b">{{ b }}</option>
        </select>
      </div>
    </div>

    <!-- Loading / Error -->
    <div v-if="loading" class="state">正在加载预测数据…</div>
    <div v-if="errorMsg" class="state error">{{ errorMsg }}</div>

    <!-- Price Bounds Card -->
    <div class="card bounds-card">
      <div class="bound-item">
        <span class="bound-label">最低价格 (成本 × 1.2)</span>
        <span class="bound-value min-price">¥{{ forecastData?.minPrice?.toFixed(2) ?? '--' }}</span>
      </div>
      <div class="bound-divider"></div>
      <div class="bound-item">
        <span class="bound-label">最高价格 (基础 × 3.0)</span>
        <span class="bound-value max-price">¥{{ forecastData?.maxPrice?.toFixed(2) ?? '--' }}</span>
      </div>
    </div>

    <!-- Forecast Chart -->
    <div class="card forecast-section">
      <h3 class="panel-title">48小时价格预测</h3>
      <div v-if="forecastData?.forecast?.length" class="forecast-table-wrap">
        <div class="forecast-header">
          <span>时间</span>
          <span>价格 (¥)</span>
          <span>需求指数</span>
        </div>
        <div v-for="(item, idx) in forecastData.forecast" :key="idx" class="forecast-row">
          <span class="time-col">{{ formatTime(item.timestamp) }}</span>
          <span class="price-col">¥{{ item.price?.toFixed(2) }}</span>
          <div class="demand-col">
            <div class="demand-bar" :style="{ width: Math.min(100, (item.demandLevel ?? 0) * 100) + '%', background: demandColor(item.demandLevel) }"></div>
            <span>{{ ((item.demandLevel ?? 0) * 100).toFixed(0) }}%</span>
          </div>
        </div>
      </div>
      <div v-else class="empty-state">暂无预测数据，请选择机库后刷新。</div>
    </div>

    <!-- Dispatch Tasks -->
    <div class="card task-section">
      <div class="task-header">
        <h3 class="panel-title">运维调度任务</h3>
        <div class="task-actions">
          <button class="btn secondary" @click="showTaskForm = !showTaskForm">
            {{ showTaskForm ? '收起' : '+ 新增任务' }}
          </button>
          <button class="btn secondary" @click="fetchTasks">刷新</button>
        </div>
      </div>
      <!-- Create Task Form -->
      <div v-if="showTaskForm" class="task-form">
        <div class="form-row">
          <div class="form-group">
          <label>设备 ID</label><input v-model="newTask.deviceId" class="input" placeholder="base-a-dev01" />
          </div>
          <div class="form-group">
            <label>任务类型</label>
            <select v-model="newTask.taskType" class="input">
              <option value="BATTERY_REPLACE">更换电池</option>
              <option value="REDEPLOY">重新调配</option>
            </select>
          </div>
          <div class="form-group">
            <label>优先级</label>
            <select v-model="newTask.priority" class="input">
              <option value="HIGH">[高]</option>
              <option value="MEDIUM">[中]</option>
              <option value="LOW">[低]</option>
            </select>
          </div>
          <div class="form-group">
            <label>原因</label><input v-model="newTask.reason" class="input" placeholder="例如：电池低于 20%" />
          </div>
          <button class="btn primary" @click="submitTask" :disabled="taskSubmitting">
            {{ taskSubmitting ? '提交中…' : '提交' }}
          </button>
        </div>
      </div>
      <!-- Task Table -->
      <div v-if="tasks.length > 0" class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>机库</th><th>设备 ID</th><th>任务类型</th><th>时间窗口</th><th>优先级</th><th>原因</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="t in tasks" :key="t.id">
              <td>{{ t.baseId }}</td>
              <td>{{ t.deviceId }}</td>
              <td><span class="badge" :class="t.taskType">{{ t.taskType === 'BATTERY_REPLACE' ? '换电' : '调配' }}</span></td>
              <td class="time-window">{{ t.suggestedTimeWindow?.start?.slice(11,16) ?? '--' }} - {{ t.suggestedTimeWindow?.end?.slice(11,16) ?? '--' }}</td>
              <td><span class="badge priority" :class="t.priority">{{ {HIGH:'高',MEDIUM:'中',LOW:'低'}[t.priority] }}</span></td>
              <td class="reason-col">{{ t.reason }}</td>
            </tr>
          </tbody>
        </table>
      </div>
      <div v-else class="empty-state">暂无调度任务。</div>
    </div>

    <!-- Demand Heatmap -->
    <div class="card heatmap-section">
      <h3 class="panel-title">需求热力图</h3>
      <div v-if="heatmap.length > 0" class="heatmap-grid">
        <div v-for="h in heatmap" :key="h.baseId" class="heatmap-card" :style="{ background: heatColor(h.demand) }">
          <span class="heatmap-base">{{ h.baseId }}</span>
          <span class="heatmap-value">{{ (h.demand * 100).toFixed(0) }}%</span>
        </div>
      </div>
      <div v-else class="empty-state">暂无需求热力数据。</div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import type { PriceForecast, DispatchTask } from '@/types/api'
import { getPriceForecast, getDispatchTasks, createDispatchTask, getDemandHeatmap } from '@/api/pricing'

const selectedBase = ref('')
const baseList = ref<string[]>([])
const loading = ref(false)
const errorMsg = ref('')
const forecastData = ref<PriceForecast | null>(null)
const tasks = ref<DispatchTask[]>([])
const heatmap = ref<{baseId: string; demand: number; lat: number; lon: number}[]>([])
const showTaskForm = ref(false)
const taskSubmitting = ref(false)

const newTask = reactive({
  deviceId: '',
  taskType: 'BATTERY_REPLACE' as 'BATTERY_REPLACE' | 'REDEPLOY',
  priority: 'MEDIUM' as 'HIGH' | 'MEDIUM' | 'LOW',
  reason: '',
  baseId: '',
})

async function fetchAll() {
  loading.value = true
  errorMsg.value = ''
  try {
    const [fc, tk, hm] = await Promise.all([
      selectedBase.value ? getPriceForecast(selectedBase.value) : Promise.resolve({ data: null }),
      getDispatchTasks(selectedBase.value || undefined),
      getDemandHeatmap(),
    ])
    forecastData.value = fc?.data ?? null
    tasks.value = tk?.data ?? []
    heatmap.value = hm?.data ?? []
    if (hm?.data) {
      const bases = new Set<string>()
      hm.data.forEach((h: any) => bases.add(h.baseId))
      baseList.value = Array.from(bases)
      if (!selectedBase.value && baseList.value.length > 0) {
        selectedBase.value = baseList.value[0]
        const fc2 = await getPriceForecast(selectedBase.value)
        forecastData.value = fc2?.data ?? null
      }
    }
  } catch (e: any) {
    errorMsg.value = e?.message ?? '加载数据失败'
  } finally {
    loading.value = false
  }
}

function onBaseChange() { fetchAll() }
async function fetchTasks() {
  try {
    const tk = await getDispatchTasks(selectedBase.value || undefined)
    tasks.value = tk?.data ?? []
  } catch (e: any) { errorMsg.value = e?.message ?? '刷新任务失败' }
}

async function submitTask() {
  if (!newTask.deviceId.trim()) return
  taskSubmitting.value = true
  try {
    await createDispatchTask({
      baseId: selectedBase.value || 'default',
      deviceId: newTask.deviceId,
      taskType: newTask.taskType,
      priority: newTask.priority,
      reason: newTask.reason,
      suggestedTimeWindow: { start: new Date().toISOString(), end: new Date(Date.now() + 3600000).toISOString() },
    })
    showTaskForm.value = false
    newTask.deviceId = ''; newTask.reason = ''
    await fetchTasks()
  } catch (e: any) {
    errorMsg.value = e?.message ?? '提交任务失败'
  } finally {
    taskSubmitting.value = false
  }
}

function formatTime(ts: string) {
  if (!ts) return '--'
  try { return new Date(ts).toLocaleString('zh-CN', { month:'numeric', day:'numeric', hour:'2-digit', minute:'2-digit' }) }
  catch { return ts }
}

function demandColor(level: number | undefined) {
  const v = (level ?? 0) * 100
  if (v > 70) return '#000'
  if (v > 40) return '#666'
  return '#ccc'
}

function heatColor(demand: number) {
  const v = demand * 100
  if (v > 70) return '#e0e0e0'
  if (v > 40) return '#f0f0f0'
  return '#fafafa'
}

onMounted(fetchAll)
</script>

<style scoped>
.pricing-page { padding: 16px 0; }
.page-header { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 20px; }
.page-header h2 { margin: 0 0 4px 0; font-size: 20px; }
.subtitle { color: var(--text-secondary, #999); font-size: 13px; }
.header-controls { display: flex; gap: 10px; align-items: center; }
.header-controls label { font-size: 13px; }

.bounds-card { display: flex; align-items: center; padding: 16px 24px; margin-bottom: 20px; }
.bound-item { flex: 1; display: flex; justify-content: space-between; align-items: center; }
.bound-label { font-size: 14px; color: var(--text-secondary, #999); }
.bound-value { font-size: 24px; font-weight: 700; }
.min-price { color: #000; }
.max-price { color: #666; }
.bound-divider { width: 1px; height: 40px; background: var(--border-color, #e0e0e0); margin: 0 24px; }

.forecast-section { margin-bottom: 20px; padding: 20px; }
.forecast-table-wrap { margin-top: 12px; }
.forecast-header, .forecast-row { display: grid; grid-template-columns: 160px 1fr 1fr; gap: 12px; padding: 8px 0; font-size: 13px; align-items: center; }
.forecast-header { font-weight: 600; border-bottom: 2px solid var(--border-color, #e0e0e0); color: var(--text-secondary, #999); }
.forecast-row { border-bottom: 1px solid var(--border-color, #e0e0e0); }
.demand-col { display: flex; align-items: center; gap: 8px; }
.demand-bar { height: 12px; border-radius: 0; min-width: 4px; transition: width .3s; }

.task-section { margin-bottom: 20px; padding: 20px; }
.task-header { display: flex; justify-content: space-between; align-items: center; }
.task-actions { display: flex; gap: 8px; }
.task-form { margin: 16px 0; padding: 16px; background: var(--card-bg, #f5f5f5); border-radius: 0; }
.task-form .form-row { display: flex; flex-wrap: wrap; gap: 12px; align-items: flex-end; }
.form-group { display: flex; flex-direction: column; gap: 4px; }
.form-group label { font-size: 12px; color: var(--text-secondary, #999); }
.form-group .input { width: 140px; }

.table-wrap { overflow-x: auto; margin-top: 12px; }
table { width: 100%; border-collapse: collapse; font-size: 13px; }
th, td { padding: 10px 12px; text-align: left; border-bottom: 1px solid var(--border-color, #e0e0e0); }
th { background: var(--card-bg, #f5f5f5); font-weight: 600; position: sticky; top: 0; }
.time-window { font-family: monospace; font-size: 12px; white-space: nowrap; }
.reason-col { max-width: 200px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

.badge { display: inline-block; padding: 2px 10px; border-radius: 0; font-size: 11px; font-weight: 500; border: none; color: #000; }
.badge.BATTERY_REPLACE { background: #f0f0f0; color: #000; border: 2px solid #000; }
.badge.REDEPLOY { background: #f0f0f0; color: #000; border: 2px solid #000; }
.badge.priority.HIGH { background: #f0f0f0; color: #000; border: 2px solid #000; }
.badge.priority.MEDIUM { background: #f0f0f0; color: #000; border: 2px dashed #000; }
.badge.priority.LOW { background: #f0f0f0; color: #000; border: 2px dotted #000; }

.heatmap-section { padding: 20px; }
.heatmap-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(150px, 1fr)); gap: 12px; margin-top: 12px; }
.heatmap-card { padding: 16px; border-radius: 0; text-align: center; display: flex; flex-direction: column; gap: 6px; }
.heatmap-base { font-weight: 600; font-size: 14px; }
.heatmap-value { font-size: 20px; font-weight: 700; }
</style>

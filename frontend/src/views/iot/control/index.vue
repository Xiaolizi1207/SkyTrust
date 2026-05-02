<template>
  <div class="iot-page">
    <div class="page-header">
      <h2>远程控制</h2>
      <span class="subtitle">通过 MQTT 下发控制指令到指定无人机设备</span>
    </div>

    <div class="card">
      <h3 class="section-title">指令下发</h3>
      <div class="form-grid">
        <div class="form-group">
          <label>设备 ID</label>
          <input v-model="deviceId" class="input" type="number" placeholder="输入设备ID" />
        </div>
        <div class="form-group">
          <label>指令类型</label>
          <select v-model="cmdType" class="input">
            <option value="hover">悬停 Hover</option>
            <option value="rtl">返航 RTL</option>
            <option value="land">降落 Land</option>
            <option value="takeoff">起飞 Takeoff</option>
          </select>
        </div>
        <button class="btn btn-primary" @click="sendCmd" :disabled="sending">
          {{ sending ? '发送中…' : '下发指令' }}
        </button>
      </div>
      <div v-if="cmdResult" class="result" :class="cmdResult.success ? 'ok' : 'err'">
        {{ cmdResult.message }}
      </div>
    </div>

    <div class="card">
      <h3 class="section-title">指令历史</h3>
      <table>
        <thead>
          <tr><th>时间</th><th>设备 ID</th><th>指令</th><th>状态</th></tr>
        </thead>
        <tbody>
          <tr v-if="history.length === 0">
            <td colspan="4" class="empty-state">暂无指令历史</td>
          </tr>
          <tr v-for="(item, i) in history" :key="i">
            <td>{{ item.time }}</td>
            <td>{{ item.deviceId }}</td>
            <td>{{ item.command }}</td>
            <td><span class="tag" :class="item.success ? 'ok' : 'err'">{{ item.success ? '成功' : '失败' }}</span></td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { sendCommandApi } from '@/api/iot'

const deviceId = ref('')
const cmdType = ref('hover')
const sending = ref(false)
const cmdResult = ref<{ success: boolean; message: string } | null>(null)

interface HistoryItem {
  time: string
  deviceId: string
  command: string
  success: boolean
}
const history = ref<HistoryItem[]>([])

async function sendCmd() {
  const id = parseInt(deviceId.value)
  if (!id) {
    cmdResult.value = { success: false, message: '请输入有效的设备 ID' }
    return
  }
  sending.value = true
  try {
    const res = await sendCommandApi(id, cmdType.value)
    const success = res.data.code === 200
    cmdResult.value = { success, message: success ? `指令 [${cmdType.value}] 已成功发送至设备 ${id}` : res.data.message }
    history.value.unshift({
      time: new Date().toLocaleString(),
      deviceId: String(id),
      command: cmdType.value,
      success,
    })
    if (history.value.length > 50) history.value.pop()
  } catch {
    cmdResult.value = { success: false, message: '指令发送失败，请检查设备连接状态' }
    history.value.unshift({
      time: new Date().toLocaleString(),
      deviceId: String(id),
      command: cmdType.value,
      success: false,
    })
  } finally {
    sending.value = false
  }
}
</script>

<style scoped>
.iot-page { max-width: 1100px; }
.page-header { margin-bottom: 24px; }
.page-header h2 { margin: 0 0 4px; font-size: 20px; font-weight: 700; }
.subtitle { color: #666; font-size: 13px; }
.card { background: #fff; border: 1px solid #e0e0e0; padding: 20px; margin-bottom: 20px; }
.section-title { font-size: 16px; font-weight: 700; margin: 0 0 16px; padding-bottom: 10px; border-bottom: 2px solid #000; }
.form-grid { display: flex; gap: 16px; align-items: flex-end; flex-wrap: wrap; }
.form-group { display: flex; flex-direction: column; gap: 4px; }
.form-group label { font-size: 12px; color: #666; }
.input { padding: 10px 12px; border: 1px solid #ccc; font-size: 14px; min-width: 160px; }
.btn-primary { padding: 10px 24px; background: #000; color: #fff; border: none; cursor: pointer; font-size: 14px; font-weight: 600; }
.btn-primary:disabled { opacity: 0.5; cursor: not-allowed; }
.result { margin-top: 12px; padding: 10px; font-size: 13px; font-weight: 600; }
.result.ok { background: #e8f5e9; color: #2e7d32; }
.result.err { background: #fbe9e7; color: #b71c1c; }
table { width: 100%; border-collapse: collapse; font-size: 14px; }
th, td { padding: 10px 12px; text-align: left; border-bottom: 1px solid #e0e0e0; }
th { background: #f5f5f5; font-weight: 600; }
.empty-state { text-align: center; padding: 24px; color: #999; }
.tag { display: inline-block; padding: 2px 8px; font-size: 12px; font-weight: 600; }
.tag.ok { background: #e8f5e9; color: #2e7d32; }
.tag.err { background: #fbe9e7; color: #b71c1c; }
</style>

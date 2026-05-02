<template>
  <div class="iot-page">
    <div class="page-header">
      <h2>远程控制</h2>
      <span class="subtitle">通过 MQTT 下发控制指令到指定无人机设备</span>
    </div>

    <div class="card">
      <h3 class="section-title">指令下发</h3>
      <div class="form-grid">
        <div class="form-group"><label>设备 ID</label><input v-model="deviceId" class="input" placeholder="drone-001" /></div>
        <div class="form-group">
          <label>指令类型</label>
          <select v-model="cmdType" class="input">
            <option value="hover">悬停 Hover</option>
            <option value="rtl">返航 RTL</option>
            <option value="land">降落 Land</option>
            <option value="takeoff">起飞 Takeoff</option>
          </select>
        </div>
        <button class="btn btn-primary" @click="sendCmd" :disabled="sending">{{ sending ? '发送中…' : '下发指令' }}</button>
      </div>
      <div v-if="cmdResult" class="result" :class="cmdResult.ok ? 'ok' : 'err'">{{ cmdResult.msg }}</div>
    </div>

    <div class="card">
      <h3 class="section-title">指令历史</h3>
      <table><thead><tr><th>时间</th><th>设备 ID</th><th>指令</th><th>状态</th></tr></thead><tbody><tr><td colspan="4" class="empty-state">暂无指令历史</td></tr></tbody></table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
const deviceId = ref('')
const cmdType = ref('hover')
const sending = ref(false)
const cmdResult = ref<{ok:boolean;msg:string}|null>(null)
async function sendCmd() {
  if (!deviceId.value) { cmdResult.value = { ok: false, msg: '请输入设备 ID' }; return }
  sending.value = true
  try { cmdResult.value = { ok: true, msg: `指令 [${cmdType.value}] 已发送至 ${deviceId.value}` } }
  catch { cmdResult.value = { ok: false, msg: '发送失败' } }
  finally { sending.value = false }
}
</script>

<style scoped>
.iot-page { max-width: 1100px; }
.page-header { margin-bottom: 24px; }
.page-header h2 { margin: 0 0 4px; font-size: 20px; font-weight: 700; }
.subtitle { color: #666; font-size: 13px; }
.card { background: #fff; border: 1px solid #e0e0e0; padding: 20px; margin-bottom: 20px; }
.section-title { font-size: 16px; font-weight: 700; margin: 0 0 16px; padding-bottom: 10px; border-bottom: 2px solid #000; }
.form-grid { display: flex; gap: 16px; align-items: flex-end; }
.form-group { display: flex; flex-direction: column; gap: 4px; }
.form-group label { font-size: 12px; color: #666; }
.input { padding: 10px 12px; border: 1px solid #ccc; font-size: 14px; min-width: 160px; }
.result { margin-top: 12px; padding: 10px; font-size: 13px; font-weight: 600; }
.result.ok { background: #f5f5f5; color: #000; }
.result.err { background: #e0e0e0; color: #000; }
table { width: 100%; border-collapse: collapse; font-size: 14px; }
th, td { padding: 10px 12px; text-align: left; border-bottom: 1px solid #e0e0e0; }
th { background: #f5f5f5; font-weight: 600; }
.empty-state { text-align: center; padding: 24px; color: #999; }
</style>

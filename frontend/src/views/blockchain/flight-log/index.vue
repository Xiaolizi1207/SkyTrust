<template>
  <section class="blockchain-page flight-log-page page-container">
    <div class="card header" aria-label="Flight log header">
      <div class="row" style="display:flex; gap:12px; align-items:center;">
        <input v-model="orderId" placeholder="请输入订单 ID (orderId)" class="input" type="text" />
        <button class="btn primary" @click="searchFlightLogs" :disabled="loadingSearch">{{ loadingSearch ? '搜索中…' : '搜索订单日志' }}</button>
      </div>
    </div>

    <!-- Batch Upload Section -->
    <div class="card upload-section" aria-label="Batch upload">
      <div style="display:flex; gap:12px; align-items:center;">
        <input ref="fileInput" type="file" accept=".json" @change="handleFileChange" style="display:none" />
        <button class="btn" @click="triggerFileSelect">上传 JSON 日志</button>
        <span class="hint" style="color:#666; font-size:12px;">支持 JSON 数组，字段应包含 logHash、timestamp</span>
      </div>
      <div class="preview" v-if="previewEntries.length" style="margin-top:12px; max-height:320px; overflow:auto; border-top:1px solid var(--border-color); padding-top:8px;">
        <table class="table" aria-label="Flight log preview">
          <thead>
            <tr><th>Index</th><th>LogHash</th><th>Timestamp</th></tr>
          </thead>
          <tbody>
            <tr v-for="(e, idx) in previewEntries" :key="idx">
              <td>{{ idx + 1 }}</td>
              <td class="hash" @click="copyHash(e.logHash)" style="cursor: pointer; color:#1d4ed8; user-select: none;">
                {{ shortHash(e.logHash) }}
              </td>
              <td>{{ formatDate(e.timestamp) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Verification Section -->
    <div class="card verification-section" aria-label="Flight log verification" style="margin-top:12px;">
      <div style="display:flex; align-items:center; gap:12px;">
        <input v-model="externalHash" placeholder="请输入外部哈希 externalHash" class="input" type="text" />
        <button class="btn primary" @click="verifyFlightLog" :disabled="verifyingHash">
          {{ verifyingHash ? '校验中…' : '校验哈希' }}
        </button>
      </div>
      <div class="result" v-if="verificationResult" style="margin-top:10px;">
        <span v-if="verificationResult === 'VALID'" class="badge green">VALID</span>
        <span v-else class="badge red">TAMPERED</span>
      </div>
    </div>

    <!-- Log Entries Table -->
    <div v-if="logs.length" class="card logs-table" aria-label="Flight logs table" style="margin-top:12px;">
      <h3 style="margin:0 0 8px 0;">日志条目</h3>
      <table class="table" aria-label="Flight logs">
        <thead>
          <tr><th>LogHash</th><th>Timestamp</th><th>Verified</th></tr>
        </thead>
        <tbody>
          <tr v-for="(row, idx) in logs" :key="idx">
            <td class="hash" @click="copyHash(row.logHash)" style="cursor: pointer; color:#1d4ed8;">{{ shortHash(row.logHash) }}</td>
            <td>{{ formatDate(row.timestamp) }}</td>
            <td>{{ row.verified ? 'Yes' : 'No' }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
  </template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import type { FlightLogEntry } from '@/types/api'
import { submitFlightLogs, verifyFlightLogIntegrity, getFlightLogs } from '@/api/blockchain'

// Order id for flight logs context
const orderId = ref<string>('')
const loadingSearch = ref(false)
const logs = ref<FlightLogEntry[]>([])
const previewEntries = ref<FlightLogEntry[]>([])
const fileInput = ref<HTMLInputElement | null>(null)
const externalHash = ref<string>('')
const verificationResult = ref<string | null>(null)
const verifyingHash = ref(false)

// File handling
async function triggerFileSelect() {
  fileInput.value?.click()
}

async function handleFileChange(e: Event) {
  const target = e.target as HTMLInputElement
  const file = target?.files?.[0]
  if (!file) return
  const reader = new FileReader()
  reader.onload = (ev) => {
    try {
      const text = ev.target?.result as string
      const json = JSON.parse(text) as any[]
      // Expect [{ logHash, timestamp }, ...]
      previewEntries.value = json.map((it) => ({ logHash: it.logHash ?? it.hash, timestamp: it.timestamp ?? it.time }))
    } catch {
      // invalid json
      previewEntries.value = []
    }
  }
  reader.readAsText(file)
  // reset input
  target.value = ''
}

function formatDate(ts: string | number | undefined) {
  if (!ts) return ''
  const d = new Date(ts as any)
  return d.toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}
function shortHash(h: string) {
  if (!h) return ''
  if (h.length <= 20) return h
  return h.slice(0, 10) + '...' + h.slice(-10)
}
function copyHash(hash: string) {
  if (!hash) return
  navigator.clipboard?.writeText(hash)
  // optional user feedback could be added
}

async function searchFlightLogs() {
  if (!orderId.value) return
  loadingSearch.value = true
  try {
    const res = await getFlightLogs(orderId.value)
    logs.value = (res?.data ?? res) as FlightLogEntry[]
  } catch {
    logs.value = []
  } finally {
    loadingSearch.value = false
  }
}

async function verifyFlightLog() {
  if (!externalHash.value) return
  verifyingHash.value = true
  try {
    const res = await verifyFlightLogIntegrity(externalHash.value)
    verificationResult.value = (res?.data?.status ?? res) as string
  } catch {
    verificationResult.value = 'TAMPERED'
  } finally {
    verifyingHash.value = false
  }
}

async function submitFlightLogs() {
  // Submit provided previewEntries to chain for this order
  if (!orderId.value || !previewEntries.value.length) return
  try {
    const payload = { orderId: orderId.value, logs: previewEntries.value }
    const res = await submitFlightLogs(payload)
    // refresh logs after submission
    await searchFlightLogs()
    return res?.data ?? res
  } catch {
    // ignore
  }
}
</script>

<style scoped>
.page-container { padding: 20px; display: flex; flex-direction: column; gap: 20px; }
.input { padding: 10px 12px; border: 1px solid var(--border-color); border-radius: 8px; min-width: 240px; }
.card { background: var(--card-bg); border: 1px solid var(--border-color); border-radius: 12px; padding: 16px; }
.header { display:flex; align-items: center; justify-content: space-between; }
.row { display:flex; align-items:center; gap:12px; }
.hash { max-width: 320px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.table { width: 100%; border-collapse: collapse; }
.table th, .table td { border-bottom: 1px solid var(--border-color); padding: 8px; font-size: 13px; text-align:left; }
.logs-table { margin-top: 8px; }
.hint { font-size: 12px; color: #666; }
.green { color: #16a34a; font-weight:700; }
.red { color: #dc2626; font-weight:700; }
</style>

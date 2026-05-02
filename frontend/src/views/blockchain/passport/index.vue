<template>
  <section class="blockchain-page page-container">
    <!-- Search Bar -->
    <div class="card search-bar" aria-label="Passport search">
      <input
        v-model="tokenId"
        placeholder="请输入 Token ID"
        class="input"
        type="text"
      />
      <button class="btn primary" @click="fetchPassport" :disabled="loadingPassport">
        {{ loadingPassport ? '加载中...' : '搜索 Passport' }}
      </button>
    </div>

    <!-- Loading / Error / Empty States -->
    <div v-if="loadingPassport" class="state">正在加载，请稍候…</div>
    <div v-if="errorPassport" class="state error">错误: {{ errorPassport }}</div>
    <div v-if="notFound && !loadingPassport" class="empty-state">未找到对应的 Drone Passport。请确认 Token ID 是否正确。</div>

    <!-- Passport Card -->
    <div v-if="passport" class="card passport-card" aria-label="Passport details">
      <div class="passport-header">
        <div class="left">
          <span class="badge manufacturer">制造商: {{ passport?.manufacturer ?? '' }}</span>
          <span class="title">Drone Passport</span>
        </div>
        <div class="right">
          <span class="token-id-tag">Token ID: {{ passport?.tokenId ?? tokenId }}</span>
        </div>
      </div>
      <div class="passport-body">
        <div class="grid two-col">
          <div class="grid-item">
            <div class="label">制造商</div>
            <div class="value">{{ passport?.manufacturer ?? '-' }}</div>
          </div>
          <div class="grid-item">
            <div class="label">生产日期</div>
            <div class="value">{{ formatDate(passport?.productionDate) }}</div>
          </div>
          <div class="grid-item">
            <div class="label">序列号</div>
            <div class="value">{{ passport?.serialNumber ?? '-' }}</div>
          </div>
          <div class="grid-item">
            <div class="label">固件版本</div>
            <div class="value">{{ passport?.firmwareVersion ?? '-' }}</div>
          </div>
          <div class="grid-item">
            <div class="label">电池循环</div>
            <div class="value">{{ passport?.batteryCycles ?? '-' }}</div>
          </div>
          <div class="grid-item">
            <div class="label">维修次数</div>
            <div class="value">{{ passport?.repairs?.length ?? 0 }}</div>
          </div>
        </div>

        <div class="history" v-if="passport?.repairs?.length">
          <h3>维修历史</h3>
          <table class="table" aria-label="Repair history">
            <thead>
              <tr>
                <th>时间</th>
                <th>维修数据哈希</th>
                <th>维修人员DID</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(r, idx) in passport.repairs" :key="idx">
                 <td>{{ formatDate(r.time) }}</td>
                 <td>{{ r.dataHash }}</td>
                 <td>{{ r.did }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </section>
  </template>

<script setup lang="ts">
import { ref } from 'vue'
import type { DronePassport } from '@/types/api'
import { getDronePassport } from '@/api/blockchain'

// token search state
const tokenId = ref<string>('')
const passport = ref<DronePassport | null>(null)
const loadingPassport = ref(false)
const errorPassport = ref<string | null>(null)
const notFound = ref(false)

// fetch passport by tokenId
async function fetchPassport() {
  errorPassport.value = null
  notFound.value = false
  if (!tokenId.value?.trim()) {
    errorPassport.value = '请输入 Token ID'
    return
  }
  loadingPassport.value = true
  try {
    const res = await getDronePassport(tokenId.value.trim())
    passport.value = (res?.data ?? res) as DronePassport
    if (!passport.value) {
      notFound.value = true
    }
  } catch (e: any) {
    errorPassport.value = e?.message ?? '加载 Passport 失败'
  } finally {
    loadingPassport.value = false
  }
}

function formatDate(ts: string | undefined | null): string {
  if (!ts) return ''
  const d = new Date(ts)
  if (Number.isNaN(d.getTime())) return String(ts)
  return d.toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

</script>

<style scoped>
.page-container { display: flex; flex-direction: column; gap: 20px; padding: 20px; }
.input { padding: 10px 12px; border: 1px solid #ccc; min-width: 280px; background: #fff; }
.card { background: #fff; border: 1px solid #e0e0e0; padding: 16px; }
.search-bar { display: flex; align-items: center; gap: 12px; }
.btn { padding: 10px 14px; border: 2px solid #000; cursor: pointer; font-weight: 600; font-size: 14px; }
.btn.primary { background: #000; color: #fff; }
.btn.primary:hover { background: #fff; color: #000; }
.state { text-align: center; color: #666; padding: 12px; }
.state.error { color: #000; font-weight: 600; border: 1px solid #000; padding: 12px; }
.empty-state { text-align: center; padding: 16px; color: #666; }
.passport-card { overflow: hidden; }
.passport-header { background: #000; color: #fff; padding: 14px 16px; display: flex; justify-content: space-between; align-items: center; }
.passport-header .badge { background: #333; padding: 6px 12px; font-weight: 600; border: 1px solid #fff; }
.passport-header .title { font-size: 1.1rem; font-weight: 700; }
.passport-body { padding: 16px; }
.grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; }
.grid-item .label { font-size: 12px; color: #666; }
.grid-item .value { font-weight: 600; margin-top: 4px; font-size: 14px; color: #000; }
.table { width: 100%; border-collapse: collapse; }
.table th, .table td { border-bottom: 1px solid #e0e0e0; padding: 8px; text-align: left; font-size: 13px; }
.history { margin-top: 14px; }
@media (max-width: 900px) { .grid { grid-template-columns: 1fr; } }
</style>

<template>
  <view class="page">
    <view class="search-section">
      <text class="section-title">飞行日志</text>
      <text class="section-desc">查询订单的区块链飞行日志记录</text>
      <input v-model="orderId" class="search-input" type="text" placeholder="请输入订单 ID" />
      <button class="search-btn" @tap="search" :disabled="loading">
        {{ loading ? '查询中...' : '查询' }}
      </button>
    </view>

    <view v-if="error" class="error-msg">{{ error }}</view>

    <view v-if="logs.length > 0" class="log-section">
      <view class="log-header">
        <text class="log-title">订单 {{ orderId }} - {{ logs.length }} 条记录</text>
      </view>
      <view v-for="(entry, i) in logs" :key="i" class="log-item">
        <view class="log-row">
          <text class="log-index">#{{ i + 1 }}</text>
          <text class="log-hash">{{ entry.logHash.substring(0, 20) }}...</text>
        </view>
        <view class="log-row">
          <text class="log-time">{{ formatDateTime(entry.timestamp) }}</text>
          <text class="log-status" :class="entry.verified ? 'verified' : 'unverified'">
            {{ entry.verified ? '已验证' : '未验证' }}
          </text>
        </view>
      </view>

      <view class="verify-section">
        <input v-model="verifyHash" class="search-input small" type="text" placeholder="输入待验证哈希" />
        <button class="search-btn small" @tap="verify" :disabled="verifying">
          {{ verifying ? '验证中...' : '验证完整性' }}
        </button>
        <view v-if="verifyResult !== null" class="verify-result" :class="verifyResult ? 'ok' : 'err'">
          {{ verifyResult ? '哈希验证通过 - 日志完整' : '哈希不匹配 - 日志可能被篡改' }}
        </view>
      </view>
    </view>

    <view v-if="!logs.length && !error && !loading" class="empty-msg">
      输入订单 ID 以加载飞行日志
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { getFlightLogsApi, verifyFlightLogApi } from '@/api/blockchain'
import type { FlightLogEntry } from '@/types/api'

const orderId = ref('')
const logs = ref<FlightLogEntry[]>([])
const loading = ref(false)
const error = ref('')

const verifyHash = ref('')
const verifying = ref(false)
const verifyResult = ref<boolean | null>(null)

function formatDateTime(ts: number) {
  return new Date(ts * 1000).toLocaleString()
}

async function search() {
  if (!orderId.value) { error.value = '请输入订单 ID'; return }
  loading.value = true
  error.value = ''
  logs.value = []
  verifyResult.value = null
  try {
    const res = await getFlightLogsApi(orderId.value)
    logs.value = res.data.data || []
  } catch (e: any) {
    error.value = e.message || '查询失败'
  } finally {
    loading.value = false
  }
}

async function verify() {
  if (!verifyHash.value) return
  verifying.value = true
  try {
    const res = await verifyFlightLogApi(orderId.value, verifyHash.value)
    verifyResult.value = res.data.data?.verified ?? false
  } catch {
    verifyResult.value = false
  } finally {
    verifying.value = false
  }
}
</script>

<style scoped>
.page { padding: 24rpx; }
.search-section { background: #fff; border: 2rpx solid #000; padding: 32rpx; margin-bottom: 24rpx; }
.section-title { font-size: 32rpx; font-weight: 700; display: block; margin-bottom: 8rpx; }
.section-desc { font-size: 24rpx; color: #999; display: block; margin-bottom: 24rpx; }
.search-input { border: 2rpx solid #000; padding: 20rpx 24rpx; font-size: 28rpx; margin-bottom: 16rpx; width: 100%; box-sizing: border-box; }
.search-input.small { padding: 16rpx 20rpx; font-size: 26rpx; margin-bottom: 0; flex: 1; }
.search-btn { background: #000; color: #fff; font-size: 28rpx; font-weight: 600; padding: 20rpx; border-radius: 0; border: none; }
.search-btn[disabled] { opacity: 0.5; }
.search-btn.small { font-size: 24rpx; padding: 16rpx 24rpx; flex-shrink: 0; }
.error-msg { background: #fbe9e7; color: #b71c1c; padding: 20rpx; font-size: 26rpx; margin-bottom: 24rpx; }
.log-section { background: #fff; border: 2rpx solid #000; padding: 24rpx; }
.log-header { margin-bottom: 16rpx; padding-bottom: 16rpx; border-bottom: 2rpx solid #000; }
.log-title { font-size: 28rpx; font-weight: 700; }
.log-item { padding: 16rpx 0; border-bottom: 1rpx solid #f0f0f0; }
.log-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 4rpx; }
.log-index { font-size: 24rpx; color: #999; font-weight: 600; }
.log-hash { font-size: 24rpx; font-family: monospace; color: #333; }
.log-time { font-size: 22rpx; color: #999; }
.log-status { font-size: 22rpx; font-weight: 600; padding: 2rpx 12rpx; }
.log-status.verified { background: #e8f5e9; color: #2e7d32; }
.log-status.unverified { background: #f5f5f5; color: #999; }
.verify-section { margin-top: 24rpx; padding-top: 24rpx; border-top: 1rpx solid #e0e0e0; display: flex; gap: 12rpx; align-items: center; flex-wrap: wrap; }
.verify-result { width: 100%; padding: 16rpx; font-size: 26rpx; font-weight: 600; text-align: center; }
.verify-result.ok { background: #e8f5e9; color: #2e7d32; }
.verify-result.err { background: #fbe9e7; color: #b71c1c; }
.empty-msg { text-align: center; padding: 80rpx 0; color: #999; font-size: 28rpx; }
</style>

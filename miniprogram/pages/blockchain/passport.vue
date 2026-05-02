<template>
  <view class="page">
    <view class="search-section">
      <text class="section-title">数字护照查询</text>
      <text class="section-desc">输入 Token ID 查询无人机区块链护照信息</text>
      <input v-model="tokenId" class="search-input" type="number" placeholder="请输入 Token ID" />
      <button class="search-btn" @tap="search" :disabled="loading">
        {{ loading ? '查询中...' : '查询' }}
      </button>
    </view>

    <view v-if="error" class="error-msg">{{ error }}</view>

    <view v-if="passport" class="passport-card">
      <view class="passport-header">
        <text class="passport-title">数字护照 #{{ passport.tokenId }}</text>
        <text class="passport-badge">已验证</text>
      </view>
      <view class="info-grid">
        <view class="info-item">
          <text class="info-label">制造商</text>
          <text class="info-value">{{ passport.manufacturer }}</text>
        </view>
        <view class="info-item">
          <text class="info-label">生产日期</text>
          <text class="info-value">{{ passport.manufactureDate }}</text>
        </view>
        <view class="info-item">
          <text class="info-label">序列号</text>
          <text class="info-value">{{ passport.serialNumber }}</text>
        </view>
        <view class="info-item">
          <text class="info-label">固件版本</text>
          <text class="info-value">{{ passport.firmwareVersion }}</text>
        </view>
        <view class="info-item">
          <text class="info-label">电池循环</text>
          <text class="info-value">{{ passport.batteryCycles }} 次</text>
        </view>
        <view class="info-item">
          <text class="info-label">维修次数</text>
          <text class="info-value">{{ passport.repairCount }} 次</text>
        </view>
      </view>

      <view v-if="passport.repairHistory && passport.repairHistory.length > 0" class="repair-section">
        <text class="info-label">维修历史</text>
        <view class="repair-list">
          <view v-for="(record, i) in passport.repairHistory" :key="i" class="repair-item">
            <text class="repair-hash">{{ record.repairDataHash.substring(0, 16) }}...</text>
            <text class="repair-did">{{ record.maintenanceProviderDID }}</text>
            <text class="repair-time">{{ formatDate(record.timestamp) }}</text>
          </view>
        </view>
      </view>
    </view>

    <view v-if="!passport && !error && !loading" class="empty-msg">
      输入 Token ID 以查看数字护照
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { getDronePassportApi } from '@/api/blockchain'
import type { DronePassport } from '@/types/api'

const tokenId = ref('')
const passport = ref<DronePassport | null>(null)
const loading = ref(false)
const error = ref('')

function formatDate(ts: number) {
  return new Date(ts * 1000).toLocaleDateString()
}

async function search() {
  const id = parseInt(tokenId.value)
  if (!id) { error.value = '请输入有效的 Token ID'; return }
  loading.value = true
  error.value = ''
  passport.value = null
  try {
    const res = await getDronePassportApi(id)
    passport.value = res.data.data
  } catch (e: any) {
    error.value = e.message || '查询失败'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.page { padding: 24rpx; }
.search-section { background: #fff; border: 2rpx solid #000; padding: 32rpx; margin-bottom: 24rpx; }
.section-title { font-size: 32rpx; font-weight: 700; display: block; margin-bottom: 8rpx; }
.section-desc { font-size: 24rpx; color: #999; display: block; margin-bottom: 24rpx; }
.search-input { border: 2rpx solid #000; padding: 20rpx 24rpx; font-size: 28rpx; margin-bottom: 16rpx; width: 100%; box-sizing: border-box; }
.search-btn { background: #000; color: #fff; font-size: 28rpx; font-weight: 600; padding: 20rpx; border-radius: 0; border: none; }
.search-btn[disabled] { opacity: 0.5; }
.error-msg { background: #fbe9e7; color: #b71c1c; padding: 20rpx; font-size: 26rpx; margin-bottom: 24rpx; }
.passport-card { background: #fff; border: 2rpx solid #000; padding: 32rpx; }
.passport-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24rpx; padding-bottom: 16rpx; border-bottom: 2rpx solid #000; }
.passport-title { font-size: 30rpx; font-weight: 700; }
.passport-badge { font-size: 22rpx; background: #e8f5e9; color: #2e7d32; padding: 4rpx 16rpx; font-weight: 600; }
.info-grid { display: flex; flex-wrap: wrap; gap: 24rpx; }
.info-item { width: 45%; }
.info-label { font-size: 24rpx; color: #999; display: block; margin-bottom: 4rpx; }
.info-value { font-size: 28rpx; font-weight: 600; }
.repair-section { margin-top: 24rpx; padding-top: 24rpx; border-top: 1rpx solid #e0e0e0; }
.repair-list { margin-top: 16rpx; }
.repair-item { display: flex; justify-content: space-between; padding: 12rpx 0; border-bottom: 1rpx solid #f0f0f0; font-size: 24rpx; }
.repair-hash { font-family: monospace; color: #333; }
.repair-did { color: #999; }
.repair-time { color: #666; }
.empty-msg { text-align: center; padding: 80rpx 0; color: #999; font-size: 28rpx; }
</style>

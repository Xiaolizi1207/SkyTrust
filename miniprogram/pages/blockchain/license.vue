<template>
  <view class="page">
    <view class="search-section">
      <text class="section-title">执照验证</text>
      <text class="section-desc">验证无人机飞行执照区块链存证</text>
      <input v-model="orderId" class="search-input" type="text" placeholder="请输入订单 ID" />
      <button class="search-btn" @tap="search" :disabled="loading">
        {{ loading ? '查询中...' : '验证' }}
      </button>
    </view>

    <view v-if="error" class="error-msg">{{ error }}</view>

    <view v-if="license" class="license-card">
      <view class="license-header">
        <text class="license-title">执照信息</text>
        <text class="license-status" :class="license.verified ? 'ok' : 'err'">
          {{ license.verified ? '验证通过' : '未验证' }}
        </text>
      </view>
      <view class="info-grid">
        <view class="info-item full">
          <text class="info-label">用户 DID</text>
          <text class="info-value did">{{ license.userDID }}</text>
        </view>
        <view class="info-item full">
          <text class="info-label">执照哈希</text>
          <text class="info-value hash">{{ license.licenseHash.substring(0, 32) }}...</text>
        </view>
        <view class="info-item">
          <text class="info-label">绑定订单</text>
          <text class="info-value">{{ license.orderId }}</text>
        </view>
        <view class="info-item">
          <text class="info-label">验证状态</text>
          <text class="info-value">{{ license.verified ? '执照有效' : '执照无效' }}</text>
        </view>
      </view>
    </view>

    <view v-if="!license && !error && !loading" class="empty-msg">
      输入订单 ID 以验证执照信息
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { verifyLicenseApi } from '@/api/blockchain'
import type { LicenseRecord } from '@/types/api'

const orderId = ref('')
const license = ref<LicenseRecord | null>(null)
const loading = ref(false)
const error = ref('')

async function search() {
  if (!orderId.value) { error.value = '请输入订单 ID'; return }
  loading.value = true
  error.value = ''
  license.value = null
  try {
    const res = await verifyLicenseApi(orderId.value)
    license.value = res.data.data
  } catch (e: any) {
    error.value = e.message || '验证失败'
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
.license-card { background: #fff; border: 2rpx solid #000; padding: 32rpx; }
.license-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24rpx; padding-bottom: 16rpx; border-bottom: 2rpx solid #000; }
.license-title { font-size: 30rpx; font-weight: 700; }
.license-status { font-size: 22rpx; padding: 4rpx 16rpx; font-weight: 600; }
.license-status.ok { background: #e8f5e9; color: #2e7d32; }
.license-status.err { background: #fbe9e7; color: #b71c1c; }
.info-grid { display: flex; flex-wrap: wrap; gap: 24rpx; }
.info-item { width: 45%; }
.info-item.full { width: 100%; }
.info-label { font-size: 24rpx; color: #999; display: block; margin-bottom: 4rpx; }
.info-value { font-size: 28rpx; font-weight: 600; word-break: break-all; }
.info-value.did, .info-value.hash { font-family: monospace; font-size: 24rpx; }
.empty-msg { text-align: center; padding: 80rpx 0; color: #999; font-size: 28rpx; }
</style>

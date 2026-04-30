<template>
  <view class="wallet-page">
    <!-- 余额卡片 -->
    <view class="balance-card">
      <text class="balance-label">钱包余额（元）</text>
      <text class="balance-amount">{{ balance.toFixed(2) }}</text>
    </view>

    <!-- 操作按钮 -->
    <view class="action-row">
      <button class="recharge-btn" :loading="recharging" @click="openRecharge">
        充值
      </button>
    </view>

    <!-- 充值弹窗 -->
    <view v-if="showRecharge" class="modal-mask" @click="showRecharge = false">
      <view class="modal-content" @click.stop>
        <text class="modal-title">钱包充值</text>
        <view class="modal-item">
          <text class="modal-label">充值金额（元）</text>
          <input
            v-model="rechargeAmount"
            class="modal-input"
            type="digit"
            placeholder="请输入充值金额"
            placeholder-style="color: #999"
          />
        </view>
        <text v-if="rechargeError" class="error-text">{{ rechargeError }}</text>
        <view class="modal-actions">
          <button class="modal-cancel" @click="showRecharge = false">取消</button>
          <button class="modal-confirm" :loading="recharging" @click="handleRecharge">
            确认充值
          </button>
        </view>
      </view>
    </view>

    <!-- 交易记录 -->
    <view class="tx-section">
      <text class="section-title">交易记录</text>

      <view v-if="loading" class="loading-hint">加载中...</view>
      <view v-else-if="transactions.length === 0" class="empty-hint">
        暂无交易记录
      </view>

      <view v-for="tx in transactions" :key="tx.id" class="tx-item">
        <view class="tx-left">
          <text class="tx-type">{{ tx.typeText }}</text>
          <text class="tx-desc">{{ tx.description || '' }}</text>
          <text class="tx-time">{{ formatDateTime(tx.createTime) }}</text>
        </view>
        <view class="tx-right">
          <text
            class="tx-amount"
            :class="tx.type === 0 ? 'amount-positive' : 'amount-negative'"
          >
            {{ tx.type === 0 ? '+' : '-' }}{{ tx.amount.toFixed(2) }}
          </text>
          <text class="tx-balance">余额 {{ tx.balanceAfter.toFixed(2) }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getWalletBalanceApi, getWalletTransactionsApi, rechargeApi } from '@/api/wallet'

const balance = ref(0)
const transactions = ref<any[]>([])
const loading = ref(false)
const showRecharge = ref(false)
const rechargeAmount = ref('')
const rechargeError = ref('')
const recharging = ref(false)

// ========== 数据加载 ==========
async function loadBalance() {
  try {
    const res = await getWalletBalanceApi()
    const data = (res.data as any).data || res.data
    balance.value = data.balance || 0
  } catch (err: any) {
    // ignore silently
  }
}

async function loadTransactions() {
  loading.value = true
  try {
    const res = await getWalletTransactionsApi()
    const data = (res.data as any).data || res.data
    transactions.value = data || []
  } catch (err: any) {
    // ignore silently
  } finally {
    loading.value = false
  }
}

// ========== 充值 ==========
function openRecharge() {
  rechargeAmount.value = ''
  rechargeError.value = ''
  showRecharge.value = true
}

async function handleRecharge() {
  const amount = parseFloat(rechargeAmount.value)
  if (!amount || amount <= 0) {
    rechargeError.value = '请输入有效的充值金额'
    return
  }

  recharging.value = true
  rechargeError.value = ''

  try {
    await rechargeApi({ amount })
    uni.showToast({ title: '充值成功', icon: 'success' })
    showRecharge.value = false
    await loadBalance()
    await loadTransactions()
  } catch (err: any) {
    rechargeError.value = err.message || '充值失败'
  } finally {
    recharging.value = false
  }
}

// ========== 工具 ==========
function formatDateTime(dateStr: string): string {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

onLoad(() => {
  loadBalance()
  loadTransactions()
})
</script>

<style lang="scss" scoped>
.wallet-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 48rpx;
}

/* 余额卡片 */
.balance-card {
  background: #000;
  padding: 56rpx 48rpx 48rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.balance-label {
  font-size: 24rpx;
  color: #999;
  margin-bottom: 12rpx;
}

.balance-amount {
  font-size: 64rpx;
  font-weight: 800;
  color: #fff;
}

/* 操作按钮 */
.action-row {
  padding: 24rpx 32rpx;
}

.recharge-btn {
  width: 100%;
  height: 88rpx;
  background: #000;
  color: #fff;
  font-size: 30rpx;
  font-weight: 600;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 交易记录 */
.tx-section {
  padding: 0 32rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: 700;
  color: #000;
  margin-bottom: 24rpx;
  display: block;
}

.loading-hint,
.empty-hint {
  text-align: center;
  font-size: 26rpx;
  color: #999;
  padding: 48rpx 0;
}

.tx-item {
  background: #fff;
  padding: 24rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16rpx;
}

.tx-left {
  display: flex;
  flex-direction: column;
}

.tx-type {
  font-size: 28rpx;
  font-weight: 600;
  color: #000;
  margin-bottom: 4rpx;
}

.tx-desc {
  font-size: 24rpx;
  color: #999;
  margin-bottom: 4rpx;
}

.tx-time {
  font-size: 22rpx;
  color: #ccc;
}

.tx-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.tx-amount {
  font-size: 30rpx;
  font-weight: 700;
  margin-bottom: 4rpx;
}

.amount-positive {
  color: #07c160;
}

.amount-negative {
  color: #e53e3e;
}

.tx-balance {
  font-size: 22rpx;
  color: #999;
}

/* 弹窗 */
.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  width: 600rpx;
  background: #fff;
  padding: 48rpx 40rpx;
}

.modal-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #000;
  display: block;
  text-align: center;
  margin-bottom: 32rpx;
}

.modal-item {
  margin-bottom: 24rpx;
}

.modal-label {
  font-size: 26rpx;
  color: #000;
  font-weight: 600;
  display: block;
  margin-bottom: 12rpx;
}

.modal-input {
  width: 100%;
  height: 80rpx;
  border: 2rpx solid #ccc;
  padding: 0 20rpx;
  font-size: 28rpx;
  color: #333;
  box-sizing: border-box;
}

.error-text {
  font-size: 24rpx;
  color: #e53e3e;
  display: block;
  margin-bottom: 16rpx;
}

.modal-actions {
  display: flex;
  gap: 16rpx;
  margin-top: 32rpx;
}

.modal-cancel {
  flex: 1;
  height: 80rpx;
  background: #f5f5f5;
  color: #333;
  font-size: 28rpx;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-confirm {
  flex: 2;
  height: 80rpx;
  background: #000;
  color: #fff;
  font-size: 28rpx;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>

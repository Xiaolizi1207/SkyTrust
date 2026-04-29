<template>
  <view class="page-container">
    <!-- 订单状态头 -->
    <view class="status-header">
      <text class="status-text">{{ getOrderStatusText(order.status) }}</text>
      <text v-if="order.payStatus === 0 && order.status === 0" class="status-hint">
        请尽快完成支付
      </text>
    </view>

    <!-- 设备信息卡片 -->
    <view class="info-card">
      <view class="card-title">设备信息</view>
      <view class="device-row">
        <image
          v-if="order.deviceImage"
          class="device-thumb"
          :src="order.deviceImage"
          mode="aspectFill"
        />
        <view v-else class="device-thumb-placeholder">
          <text>ST</text>
        </view>
        <view class="device-info">
          <text class="device-name">{{ order.deviceName }}</text>
          <text class="device-model">{{ order.deviceModel }}</text>
        </view>
      </view>
    </view>

    <!-- 订单信息卡片 -->
    <view class="info-card">
      <view class="card-title">订单信息</view>
      <view class="info-row">
        <text class="info-label">订单编号</text>
        <text class="info-value">{{ order.orderNo }}</text>
      </view>
      <view class="info-row">
        <text class="info-label">租赁天数</text>
        <text class="info-value">{{ order.totalDays }}天</text>
      </view>
      <view class="info-row">
        <text class="info-label">开始时间</text>
        <text class="info-value">{{ formatDateTime(order.startTime) }}</text>
      </view>
      <view class="info-row">
        <text class="info-label">结束时间</text>
        <text class="info-value">{{ formatDateTime(order.endTime) }}</text>
      </view>
      <view v-if="order.createTime" class="info-row">
        <text class="info-label">创建时间</text>
        <text class="info-value">{{ formatDateTime(order.createTime) }}</text>
      </view>
    </view>

    <!-- 费用卡片 -->
    <view class="info-card">
      <view class="card-title">费用明细</view>
      <view class="info-row">
        <text class="info-label">租赁费</text>
        <text class="info-value">¥{{ order.rentalPrice }} × {{ order.totalDays }}天</text>
      </view>
      <view class="info-row">
        <text class="info-label">保险费</text>
        <text class="info-value">¥{{ order.insuranceFee }} × {{ order.totalDays }}天</text>
      </view>
      <view class="info-row total-row">
        <text class="info-label">合计</text>
        <text class="total-price">¥{{ order.totalAmount }}</text>
      </view>
    </view>

    <!-- 取消原因 -->
    <view v-if="order.cancelReason" class="info-card">
      <view class="card-title">取消原因</view>
      <text class="cancel-text">{{ order.cancelReason }}</text>
    </view>

    <!-- 底部操作栏 -->
    <view v-if="hasActions" class="bottom-actions">
      <!-- 待支付：支付 + 取消 -->
      <template v-if="order.status === 0 && order.payStatus === 0">
        <button class="action-cancel" @click="handleCancel">取消订单</button>
        <button class="action-primary" :loading="paying" @click="handlePay">
          立即支付 ¥{{ order.totalAmount }}
        </button>
      </template>

      <!-- 进行中：续租 -->
      <template v-if="order.status === 1">
        <button class="action-primary" @click="handleRenew">续租</button>
      </template>

      <!-- 已完成：删除 -->
      <template v-if="order.status === 2">
        <button class="action-cancel" @click="handleDelete">删除订单</button>
      </template>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getOrderDetailApi, cancelOrderApi } from '@/api/order'
import type { RentalOrderVO } from '@/types/api'

const orderId = ref(0)
const order = ref<RentalOrderVO>({
  id: 0,
  orderNo: '',
  userId: 0,
  deviceId: 0,
  deviceName: '',
  deviceModel: '',
  startTime: '',
  endTime: '',
  totalDays: 0,
  rentalPrice: 0,
  insuranceFee: 0,
  totalAmount: 0,
  status: 0,
  payStatus: 0,
  createTime: '',
})
const paying = ref(false)

const hasActions = computed(() => {
  if (order.value.status === 0 && order.value.payStatus === 0) return true
  if (order.value.status === 1) return true
  if (order.value.status === 2) return true
  return false
})

onLoad((options) => {
  if (options?.id) {
    orderId.value = Number(options.id)
    loadOrder()
  }
})

async function loadOrder() {
  try {
    const res = await getOrderDetailApi(orderId.value)
    const data = (res.data as any).data || res.data
    order.value = data
  } catch {
    uni.showToast({ title: '加载失败', icon: 'none' })
  }
}

// ========== 支付 ==========
async function handlePay() {
  paying.value = true
  try {
    // 调用后端统一下单，获取微信支付参数
    const { createPaymentApi } = await import('@/api/payment')
    const res = await createPaymentApi({ orderId: orderId.value, paymentMethod: 'wechat' })

    const payData = (res.data as any).data || res.data

    // 调起微信支付
    uni.requestPayment({
      provider: 'wxpay',
      timeStamp: payData.timeStamp,
      nonceStr: payData.nonceStr,
      package: payData.package,
      signType: payData.signType || 'MD5',
      paySign: payData.paySign,
      success: () => {
        uni.showToast({ title: '支付成功', icon: 'success' })
        setTimeout(() => loadOrder(), 1500)
      },
      fail: (err: any) => {
        if (err.errMsg?.includes('cancel')) {
          uni.showToast({ title: '支付已取消', icon: 'none' })
        } else {
          uni.showToast({ title: '支付失败', icon: 'none' })
        }
      },
    })
  } catch (err: any) {
    uni.showToast({ title: err.message || '支付失败', icon: 'none' })
  } finally {
    paying.value = false
  }
}

// ========== 取消订单 ==========
async function handleCancel() {
  const res = await uni.showModal({
    title: '取消订单',
    content: '确定要取消此订单吗？',
  })
  if (!res.confirm) return

  try {
    await cancelOrderApi(orderId.value)
    uni.showToast({ title: '已取消', icon: 'success' })
    setTimeout(() => loadOrder(), 500)
  } catch (err: any) {
    uni.showToast({ title: err.message || '取消失败', icon: 'none' })
  }
}

// ========== 续租 ==========
function handleRenew() {
  uni.showModal({
    title: '续租',
    content: '续租功能开发中，请联系客服',
    showCancel: false,
  })
}

// ========== 删除 ==========
function handleDelete() {
  uni.showToast({ title: '已删除', icon: 'none' })
  setTimeout(() => uni.navigateBack(), 500)
}

// ========== 工具函数 ==========
function formatDateTime(dateStr: string): string {
  if (!dateStr) return ''
  return dateStr.slice(0, 16).replace('T', ' ')
}

function getOrderStatusText(status: number) {
  const map: Record<number, string> = {
    0: '待支付',
    1: '进行中',
    2: '已完成',
    3: '已取消',
  }
  return map[status] || '未知'
}
</script>

<style lang="scss" scoped>
/* 状态头 */
.status-header {
  background: #000;
  padding: 40rpx 32rpx;
  display: flex;
  flex-direction: column;
}

.status-text {
  font-size: 40rpx;
  font-weight: 800;
  color: #fff;
}

.status-hint {
  font-size: 26rpx;
  color: #999;
  margin-top: 8rpx;
}

/* 信息卡片 */
.info-card {
  background: #fff;
  margin: 16rpx 24rpx;
  padding: 24rpx;
  border: 2rpx solid #e0e0e0;
}

.card-title {
  font-size: 26rpx;
  font-weight: 700;
  color: #000;
  margin-bottom: 16rpx;
  padding-bottom: 12rpx;
  border-bottom: 2rpx solid #f0f0f0;
}

/* 设备行 */
.device-row {
  display: flex;
  gap: 16rpx;
}

.device-thumb {
  width: 120rpx;
  height: 100rpx;
  background: #f5f5f5;
}

.device-thumb-placeholder {
  width: 120rpx;
  height: 100rpx;
  background: #000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.device-thumb-placeholder text {
  color: #fff;
  font-size: 32rpx;
  font-weight: 800;
}

.device-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.device-name {
  font-size: 30rpx;
  color: #000;
  font-weight: 600;
}

.device-model {
  font-size: 24rpx;
  color: #999;
  margin-top: 4rpx;
}

/* 信息行 */
.info-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12rpx;
}

.info-label {
  font-size: 26rpx;
  color: #999;
}

.info-value {
  font-size: 26rpx;
  color: #333;
}

.total-row {
  padding-top: 12rpx;
  border-top: 2rpx solid #000;
  margin-top: 8rpx;
}

.total-price {
  font-size: 36rpx;
  font-weight: 800;
  color: #000;
}

.cancel-text {
  font-size: 26rpx;
  color: #666;
}

/* 底部操作 */
.bottom-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  border-top: 2rpx solid #000;
  display: flex;
  padding: 16rpx 24rpx;
  padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
  gap: 16rpx;
}

.action-primary {
  flex: 2;
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

.action-cancel {
  flex: 1;
  height: 88rpx;
  background: #fff;
  color: #999;
  border: 2rpx solid #ccc;
  font-size: 30rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>

<template>
  <view class="page-container">
    <!-- Tab 切换 -->
    <view class="tab-bar">
      <view
        v-for="tab in tabs"
        :key="tab.value"
        class="tab-item"
        :class="{ 'tab-active': activeTab === tab.value }"
        @click="switchTab(tab.value)"
      >
        <text>{{ tab.label }}</text>
      </view>
    </view>

    <!-- 订单列表 -->
    <scroll-view
      class="order-scroll"
      scroll-y
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
      @scrolltolower="onLoadMore"
    >
      <view
        v-for="order in orderList"
        :key="order.id"
        class="order-card"
        @click="goDetail(order.id)"
      >
        <!-- 订单头部 -->
        <view class="order-header">
          <text class="order-no">{{ order.orderNo }}</text>
          <view class="order-status" :class="getOrderStatusClass(order.status)">
            <text>{{ getOrderStatusText(order.status) }}</text>
          </view>
        </view>

        <!-- 设备信息 -->
        <view class="order-device">
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

        <!-- 租赁时间 -->
        <view class="order-period">
          <text class="period-text">
            {{ formatDate(order.startTime) }} ~ {{ formatDate(order.endTime) }}
          </text>
          <text class="period-days">{{ order.totalDays }}天</text>
        </view>

        <!-- 金额 + 操作 -->
        <view class="order-footer">
          <text class="order-amount">¥{{ order.totalAmount }}</text>
          <view class="order-actions">
            <button
              v-if="order.status === 0 && order.payStatus === 0"
              class="action-btn action-primary"
              @click.stop="goPay(order.id)"
            >
              去支付
            </button>
            <button
              v-if="order.status === 0"
              class="action-btn action-cancel"
              @click.stop="handleCancel(order.id)"
            >
              取消
            </button>
            <button
              v-if="order.status === 1"
              class="action-btn action-renew"
              @click.stop="handleRenew(order.id)"
            >
              续租
            </button>
          </view>
        </view>
      </view>

      <!-- 加载更多 -->
      <view v-if="loading && !refreshing" class="loading-more">
        <text>加载中...</text>
      </view>

      <!-- 空状态 -->
      <view v-if="!loading && orderList.length === 0" class="empty-state">
        <text>暂无订单</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getOrderListApi, cancelOrderApi } from '@/api/order'
import type { RentalOrderVO } from '@/types/api'

// ========== Tab 状态 ==========
const tabs = [
  { label: '全部', value: -1 },
  { label: '待支付', value: 0 },
  { label: '进行中', value: 1 },
  { label: '已完成', value: 2 },
  { label: '已取消', value: 3 },
]
const activeTab = ref(-1)

// ========== 列表状态 ==========
const orderList = ref<RentalOrderVO[]>([])
const page = ref(1)
const size = 10
const total = ref(0)
const loading = ref(false)
const refreshing = ref(false)

onMounted(() => {
  loadOrders(true)
})

// ========== Tab 切换 ==========
function switchTab(value: number) {
  if (activeTab.value === value) return
  activeTab.value = value
  loadOrders(true)
}

// ========== 数据加载 ==========
async function loadOrders(reset = false) {
  if (loading.value) return
  loading.value = true

  if (reset) {
    page.value = 1
    orderList.value = []
  }

  try {
    const res = await getOrderListApi({
      page: page.value,
      size,
      status: activeTab.value >= 0 ? activeTab.value : undefined,
    })

    const data = res.data
    const list = Array.isArray(data.data) ? data.data : (data.data as any)?.records || []
    const t = (data as any).total || list.length

    if (reset) {
      orderList.value = list
    } else {
      orderList.value = [...orderList.value, ...list]
    }
    total.value = t
  } catch {
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

// ========== 下拉刷新 ==========
async function onRefresh() {
  refreshing.value = true
  await loadOrders(true)
  refreshing.value = false
}

// ========== 上拉加载 ==========
function onLoadMore() {
  if (loading.value || orderList.value.length >= total.value) return
  page.value++
  loadOrders()
}

// ========== 操作 ==========
function goDetail(id: number) {
  uni.navigateTo({ url: `/pages/order-detail/index?id=${id}` })
}

function goPay(id: number) {
  uni.navigateTo({ url: `/pages/order-detail/index?id=${id}&action=pay` })
}

async function handleCancel(id: number) {
  const res = await uni.showModal({
    title: '取消订单',
    content: '确定要取消此订单吗？',
  })
  if (!res.confirm) return

  try {
    await cancelOrderApi(id)
    uni.showToast({ title: '已取消', icon: 'success' })
    loadOrders(true)
  } catch (err: any) {
    uni.showToast({ title: err.message || '取消失败', icon: 'none' })
  }
}

function handleRenew(id: number) {
  uni.navigateTo({ url: `/pages/order-detail/index?id=${id}&action=renew` })
}

// ========== 工具函数 ==========
function formatDate(dateStr: string): string {
  if (!dateStr) return ''
  return dateStr.slice(0, 10)
}

function getOrderStatusClass(status: number) {
  const map: Record<number, string> = {
    0: 'status-pending',
    1: 'status-active',
    2: 'status-done',
    3: 'status-cancelled',
  }
  return map[status] || ''
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
/* Tab 栏 */
.tab-bar {
  display: flex;
  background: #fff;
  border-bottom: 2rpx solid #e0e0e0;
  position: sticky;
  top: 0;
  z-index: 10;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 24rpx 0;
  font-size: 26rpx;
  color: #999;
  border-bottom: 4rpx solid transparent;
}

.tab-active {
  color: #000;
  font-weight: 700;
  border-bottom-color: #000;
}

/* 滚动容器 */
.order-scroll {
  height: calc(100vh - 88rpx);
}

/* 订单卡片 */
.order-card {
  background: #fff;
  border: 2rpx solid #e0e0e0;
  margin: 16rpx 24rpx;
  padding: 24rpx;
}

.order-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16rpx;
}

.order-no {
  font-size: 24rpx;
  color: #999;
}

.order-status {
  font-size: 22rpx;
  padding: 4rpx 12rpx;
  border: 2rpx solid #000;
  font-weight: 600;
}

.status-pending {
  border-color: #000;
  background: #000;
  color: #fff;
}

.status-active {
  border-color: #000;
  color: #000;
}

.status-done {
  border-color: #ccc;
  color: #999;
  font-weight: 400;
}

.status-cancelled {
  border-color: #e0e0e0;
  color: #ccc;
  font-weight: 400;
}

/* 设备信息 */
.order-device {
  display: flex;
  gap: 16rpx;
  margin-bottom: 16rpx;
}

.device-thumb {
  width: 100rpx;
  height: 80rpx;
  background: #f5f5f5;
}

.device-thumb-placeholder {
  width: 100rpx;
  height: 80rpx;
  background: #000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.device-thumb-placeholder text {
  color: #fff;
  font-size: 28rpx;
  font-weight: 800;
}

.device-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.device-name {
  font-size: 28rpx;
  color: #000;
  font-weight: 600;
}

.device-model {
  font-size: 24rpx;
  color: #999;
  margin-top: 4rpx;
}

/* 租赁时间 */
.order-period {
  margin-bottom: 16rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.period-text {
  font-size: 24rpx;
  color: #666;
}

.period-days {
  font-size: 24rpx;
  color: #999;
}

/* 底部 */
.order-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 16rpx;
  border-top: 1rpx solid #f0f0f0;
}

.order-amount {
  font-size: 32rpx;
  font-weight: 700;
  color: #000;
}

.order-actions {
  display: flex;
  gap: 12rpx;
}

.action-btn {
  font-size: 24rpx;
  padding: 8rpx 20rpx;
  border: 2rpx solid #000;
  background: #fff;
  color: #000;
}

.action-primary {
  background: #000;
  color: #fff;
  font-weight: 600;
}

.action-cancel {
  border-color: #ccc;
  color: #999;
}

.action-renew {
  border-color: #000;
  color: #000;
}

/* 加载中 */
.loading-more {
  padding: 32rpx;
  text-align: center;
  font-size: 24rpx;
  color: #999;
}

/* 空状态 */
.empty-state {
  padding: 120rpx 0;
  text-align: center;
  font-size: 28rpx;
  color: #999;
}
</style>

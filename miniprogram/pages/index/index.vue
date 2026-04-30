<template>
  <view class="page-container">
    <!-- 搜索栏 -->
    <view class="search-bar">
      <view class="search-box">
        <text class="search-icon">&#xe601;</text>
        <input
          v-model="keyword"
          class="search-input"
          placeholder="搜索设备名称/型号"
          placeholder-style="color: #999"
          confirm-type="search"
          @confirm="onSearch"
        />
        <text
          v-if="keyword"
          class="search-clear"
          @click="keyword = ''; onSearch()"
        >&times;</text>
      </view>
    </view>

    <!-- 设备列表 -->
    <scroll-view
      class="device-scroll"
      scroll-y
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
      @scrolltolower="onLoadMore"
    >
      <!-- 设备卡片 -->
      <view
        v-for="device in deviceList"
        :key="device.id"
        class="device-card"
        @click="goDeviceDetail(device.id)"
      >
        <!-- 设备图片 -->
        <view class="card-image-wrap">
          <image
            v-if="device.images"
            class="card-image"
            :src="getFirstImage(device.images)"
            mode="aspectFill"
          />
          <view v-else class="card-image-placeholder">
            <text class="placeholder-text">SkyTrust</text>
          </view>
        </view>

        <!-- 设备信息 -->
        <view class="card-body">
          <view class="card-header">
            <text class="device-name">{{ device.deviceName }}</text>
            <view class="status-tag" :class="{'status-online': device.status === 1, 'status-flying': device.status === 2, 'status-maintenance': device.status === 3, 'status-scrapped': device.status === 4, 'status-offline': device.status !== 1 && device.status !== 2 && device.status !== 3 && device.status !== 4}">
              <text>{{ device.status === 0 ? '离线' : device.status === 1 ? '在线' : device.status === 2 ? '飞行中' : device.status === 3 ? '维护中' : device.status === 4 ? '已报废' : '未知' }}</text>
            </view>
          </view>

          <text class="device-model">{{ device.model }}</text>

          <view class="card-meta">
            <view v-if="device.batteryLevel != null" class="battery-row">
              <view class="battery-bar">
                <view class="battery-fill" :style="{ width: device.batteryLevel + '%' }" />
              </view>
              <text class="battery-text">{{ device.batteryLevel }}%</text>
            </view>
          </view>

          <view class="card-footer">
            <view class="price-row">
              <text class="price-symbol">¥</text>
              <text class="price-value">{{ device.rentalPrice }}</text>
              <text class="price-unit">/天</text>
            </view>
            <view class="insurance-row">
              <text class="insurance-text">保险 ¥{{ device.insuranceFee }}/天</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 加载更多 -->
      <view v-if="loading && !refreshing" class="loading-more">
        <text class="loading-text">加载中...</text>
      </view>

      <!-- 没有更多 -->
      <view v-if="!loading && deviceList.length > 0 && noMore" class="no-more">
        <text class="no-more-text">— 没有更多了 —</text>
      </view>

      <!-- 空状态 -->
      <view v-if="!loading && deviceList.length === 0" class="empty-state">
        <text class="empty-text">暂无可用设备</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getDeviceListApi } from '@/api/device'
import type { DeviceVO } from '@/types/api'

// ========== 状态 ==========
const keyword = ref('')
const deviceList = ref<DeviceVO[]>([])
const page = ref(1)
const size = 10
const total = ref(0)
const loading = ref(false)
const refreshing = ref(false)

const noMore = computed(() => deviceList.value.length >= total.value)

// ========== 生命周期 ==========
onMounted(() => {
  loadDevices()
})

// ========== 数据加载 ==========
async function loadDevices(reset = false) {
  if (loading.value) return
  loading.value = true

  if (reset) {
    page.value = 1
    deviceList.value = []
  }

  try {
    const res = await getDeviceListApi({
      page: page.value,
      size,
      deviceName: keyword.value || undefined,
      model: keyword.value || undefined,
    })

    const data = res.data
    // 根据实际后端返回结构调整
    const list = Array.isArray(data.data) ? data.data : (data.data as any)?.records || []
    const t = (data as any).total || list.length

    if (reset) {
      deviceList.value = list
    } else {
      deviceList.value = [...deviceList.value, ...list]
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
  await loadDevices(true)
  refreshing.value = false
}

// ========== 上拉加载 ==========
function onLoadMore() {
  if (loading.value || noMore.value) return
  page.value++
  loadDevices()
}

// ========== 搜索 ==========
function onSearch() {
  loadDevices(true)
}

// ========== 工具函数 ==========
function getFirstImage(images: string): string {
  try {
    const arr = JSON.parse(images)
    return Array.isArray(arr) && arr.length > 0 ? arr[0] : ''
  } catch {
    return images.split(',')[0]?.trim() || ''
  }
}

function getStatusClass(status: number) {
  const map: Record<number, string> = {
    0: 'status-offline',
    1: 'status-online',
    2: 'status-flying',
    3: 'status-maintenance',
    4: 'status-scrapped',
  }
  return map[status] || 'status-offline'
}

function getStatusText(status: number) {
  const map: Record<number, string> = {
    0: '离线',
    1: '在线',
    2: '飞行中',
    3: '维护中',
    4: '已报废',
  }
  return map[status] || '未知'
}

// ========== 导航 ==========
function goDeviceDetail(id: number) {
  uni.navigateTo({ url: `/pages/device/index?id=${id}` })
}
</script>

<style lang="scss" scoped>
/* 搜索栏 */
.search-bar {
  padding: 16rpx 24rpx;
  background: #fff;
  border-bottom: 2rpx solid #e0e0e0;
  position: sticky;
  top: 0;
  z-index: 10;
}

.search-box {
  display: flex;
  align-items: center;
  height: 68rpx;
  background: #f5f5f5;
  border: 1rpx solid #e0e0e0;
  padding: 0 16rpx;
}

.search-icon {
  font-size: 28rpx;
  color: #999;
  margin-right: 8rpx;
}

.search-input {
  flex: 1;
  font-size: 28rpx;
  color: #333;
  background: transparent;
}

.search-clear {
  font-size: 36rpx;
  color: #999;
  padding: 8rpx;
}

/* 滚动容器 */
.device-scroll {
  height: calc(100vh - 100rpx);
}

/* 设备卡片 */
.device-card {
  background: #fff;
  border: 2rpx solid #e0e0e0;
  margin: 16rpx 24rpx;
  display: flex;
  flex-direction: column;
}

.card-image-wrap {
  width: 100%;
  height: 360rpx;
  background: #f5f5f5;
  overflow: hidden;
}

.card-image {
  width: 100%;
  height: 100%;
}

.card-image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #000;
}

.placeholder-text {
  color: #fff;
  font-size: 40rpx;
  font-weight: 800;
  letter-spacing: 4rpx;
}

.card-body {
  padding: 24rpx;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8rpx;
}

.device-name {
  font-size: 32rpx;
  font-weight: 700;
  color: #000;
}

/* 状态标签 */
.status-tag {
  font-size: 20rpx;
  padding: 4rpx 12rpx;
  border: 2rpx solid #000;
  font-weight: 600;
  color: #000;
}

.status-offline {
  border-color: #ccc;
  color: #999;
  font-weight: 400;
}

.status-flying {
  border-color: #000;
  background: #000;
  color: #fff;
}

.status-maintenance {
  border-color: #ccc;
  color: #666;
  background: #f5f5f5;
}

.status-scrapped {
  border-color: #e0e0e0;
  color: #ccc;
  font-weight: 400;
}

.device-model {
  font-size: 24rpx;
  color: #999;
  display: block;
  margin-bottom: 12rpx;
}

/* 电量条 */
.card-meta {
  margin-bottom: 16rpx;
}

.battery-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.battery-bar {
  flex: 1;
  height: 8rpx;
  background: #f0f0f0;
}

.battery-fill {
  height: 100%;
  background: #000;
}

.battery-text {
  font-size: 22rpx;
  color: #666;
  width: 60rpx;
  text-align: right;
}

/* 价格区 */
.card-footer {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  border-top: 1rpx solid #f0f0f0;
  padding-top: 16rpx;
}

.price-row {
  display: flex;
  align-items: baseline;
}

.price-symbol {
  font-size: 24rpx;
  font-weight: 700;
  color: #000;
}

.price-value {
  font-size: 40rpx;
  font-weight: 800;
  color: #000;
  line-height: 1;
}

.price-unit {
  font-size: 22rpx;
  color: #999;
  margin-left: 4rpx;
}

.insurance-row {
  font-size: 22rpx;
  color: #999;
}

/* 加载中 */
.loading-more {
  padding: 32rpx;
  text-align: center;
}

.loading-text {
  font-size: 24rpx;
  color: #999;
}

/* 没有更多 */
.no-more {
  padding: 32rpx;
  text-align: center;
}

.no-more-text {
  font-size: 24rpx;
  color: #ccc;
}

/* 空状态 */
.empty-state {
  padding: 120rpx 0;
  text-align: center;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
}
</style>

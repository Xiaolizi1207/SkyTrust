<template>
  <view class="page-container">
    <!-- 图片轮播 -->
    <swiper
      v-if="images.length > 0"
      class="image-swiper"
      indicator-dots
      indicator-color="#ccc"
      indicator-active-color="#000"
      autoplay
      circular
    >
      <swiper-item v-for="(img, idx) in images" :key="idx">
        <image class="swiper-image" :src="img" mode="aspectFill" />
      </swiper-item>
    </swiper>
    <view v-else class="no-image">
      <text class="no-image-text">SkyTrust</text>
    </view>

    <!-- 基本信息 -->
    <view class="info-section">
      <view class="info-header">
        <text class="device-name">{{ device.deviceName }}</text>
        <view class="status-tag" :class="getStatusClass(device.status)">
          <text>{{ getStatusText(device.status) }}</text>
        </view>
      </view>

      <view class="info-grid">
        <view class="info-item">
          <text class="info-label">型号</text>
          <text class="info-value">{{ device.model }}</text>
        </view>
        <view class="info-item">
          <text class="info-label">序列号</text>
          <text class="info-value">{{ device.serialNumber }}</text>
        </view>
      </view>
    </view>

    <!-- 电量 -->
    <view v-if="device.batteryLevel != null" class="info-section">
      <view class="section-label">电量</view>
      <view class="battery-display">
        <view class="battery-bar-lg">
          <view class="battery-fill-lg" :style="{ width: device.batteryLevel + '%' }" />
        </view>
        <text class="battery-pct">{{ device.batteryLevel }}%</text>
      </view>
    </view>

    <!-- 飞行数据 -->
    <view v-if="device.speed != null || device.totalFlightHours != null" class="info-section">
      <view class="section-label">飞行数据</view>
      <view class="info-grid">
        <view v-if="device.speed != null" class="info-item">
          <text class="info-label">速度</text>
          <text class="info-value">{{ device.speed }} m/s</text>
        </view>
        <view v-if="device.totalFlightHours != null" class="info-item">
          <text class="info-label">总飞行时长</text>
          <text class="info-value">{{ device.totalFlightHours }} h</text>
        </view>
      </view>
    </view>

    <!-- 位置信息 -->
    <view
      v-if="device.latitude && device.longitude"
      class="info-section map-section"
      @click="openLocation"
    >
      <view class="section-label">设备位置</view>
      <view class="location-info">
        <text class="location-text">
          {{ device.latitude.toFixed(6) }}, {{ device.longitude.toFixed(6) }}
        </text>
        <text class="location-link">查看地图 &gt;</text>
      </view>
    </view>

    <!-- 设备描述 -->
    <view v-if="device.description" class="info-section">
      <view class="section-label">设备描述</view>
      <text class="desc-text">{{ device.description }}</text>
    </view>

    <!-- 规格参数 -->
    <view v-if="device.specifications" class="info-section">
      <view class="section-label">规格参数</view>
      <text class="desc-text">{{ device.specifications }}</text>
    </view>

    <!-- 底部下单栏 -->
    <view class="bottom-bar">
      <view class="price-section">
        <view class="price-main">
          <text class="price-symbol">¥</text>
          <text class="price-value">{{ device.rentalPrice }}</text>
          <text class="price-unit">/天</text>
        </view>
        <text class="insurance-info">含保险 ¥{{ device.insuranceFee }}/天</text>
      </view>
      <button
        class="rent-btn"
        :class="{ 'rent-btn--disabled': !canRent }"
        :disabled="!canRent"
        @click="showOrderModal = true"
      >
        立即租赁
      </button>
    </view>

    <!-- 下单确认弹窗 -->
    <view v-if="showOrderModal" class="modal-mask" @click="showOrderModal = false">
      <view class="modal-content" @click.stop>
        <text class="modal-title">确认租赁</text>

        <view class="modal-item">
          <text class="modal-label">设备</text>
          <text class="modal-value">{{ device.deviceName }}</text>
        </view>

        <view class="modal-item">
          <text class="modal-label">租金</text>
          <text class="modal-value">¥{{ device.rentalPrice }}/天</text>
        </view>

        <view class="modal-item">
          <text class="modal-label">保险</text>
          <text class="modal-value">¥{{ device.insuranceFee }}/天</text>
        </view>

        <view class="modal-item">
          <text class="modal-label">租赁天数</text>
          <view class="days-selector">
            <button class="day-btn" @click="days = Math.max(1, days - 1)">-</button>
            <text class="day-num">{{ days }}</text>
            <button class="day-btn" @click="days = Math.min(30, days + 1)">+</button>
          </view>
        </view>

        <view class="modal-divider" />

        <view class="modal-total">
          <text class="total-label">合计</text>
          <text class="total-price">
            ¥{{ (device.rentalPrice + device.insuranceFee) * days }}
          </text>
        </view>

        <view class="modal-actions">
          <button class="modal-cancel" @click="showOrderModal = false">取消</button>
          <button class="modal-confirm" :loading="submitting" @click="submitOrder">
            确认下单
          </button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getDeviceApi } from '@/api/device'
import { createOrderApi } from '@/api/order'
import type { DeviceVO } from '@/types/api'

// ========== 路由参数 ==========
const deviceId = ref(0)

onLoad((options) => {
  if (options?.id) {
    deviceId.value = Number(options.id)
    loadDevice()
  }
})

// ========== 设备信息 ==========
const device = ref<DeviceVO>({
  id: 0,
  deviceName: '',
  model: '',
  serialNumber: '',
  status: 0,
  ownerId: 0,
  rentalPrice: 0,
  insuranceFee: 0,
})
const images = ref<string[]>([])
const loading = ref(false)

// ========== 下单相关 ==========
const showOrderModal = ref(false)
const days = ref(1)
const submitting = ref(false)

const canRent = computed(() => device.value.status === 1) // 仅在线设备可租

// ========== 加载设备详情 ==========
async function loadDevice() {
  loading.value = true
  try {
    const res = await getDeviceApi(deviceId.value)
    const d = (res.data as any).data || res.data
    device.value = d

    // 解析图片
    if (d.images) {
      try {
        images.value = JSON.parse(d.images)
      } catch {
        images.value = d.images.split(',').map((s: string) => s.trim()).filter(Boolean)
      }
    }
  } catch {
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

// ========== 提交订单 ==========
async function submitOrder() {
  submitting.value = true
  try {
    const startTime = new Date()
    const endTime = new Date()
    endTime.setDate(endTime.getDate() + days.value)

    await createOrderApi({
      deviceId: deviceId.value,
      startTime: startTime.toISOString().slice(0, 19),
      endTime: endTime.toISOString().slice(0, 19),
    })

    showOrderModal.value = false
    uni.showToast({ title: '下单成功', icon: 'success' })
    // 跳转到订单列表
    setTimeout(() => {
      uni.switchTab({ url: '/pages/orders/index' })
    }, 1000)
  } catch (err: any) {
    uni.showToast({ title: err.message || '下单失败', icon: 'none' })
  } finally {
    submitting.value = false
  }
}

// ========== 打开地图 ==========
function openLocation() {
  if (!device.value.latitude || !device.value.longitude) return
  uni.openLocation({
    latitude: device.value.latitude,
    longitude: device.value.longitude,
    name: device.value.deviceName,
    scale: 16,
  })
}

// ========== 工具函数 ==========
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
</script>

<style lang="scss" scoped>
/* 图片轮播 */
.image-swiper {
  width: 100%;
  height: 500rpx;
  background: #f5f5f5;
}

.swiper-image {
  width: 100%;
  height: 100%;
}

.no-image {
  width: 100%;
  height: 360rpx;
  background: #000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.no-image-text {
  color: #fff;
  font-size: 48rpx;
  font-weight: 800;
}

/* 信息区块 */
.info-section {
  background: #fff;
  padding: 24rpx 32rpx;
  border-bottom: 2rpx solid #f0f0f0;
}

.info-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16rpx;
}

.device-name {
  font-size: 36rpx;
  font-weight: 800;
  color: #000;
}

/* 状态标签 */
.status-tag {
  font-size: 22rpx;
  padding: 6rpx 16rpx;
  border: 2rpx solid #000;
  font-weight: 600;
}

.status-offline { border-color: #ccc; color: #999; font-weight: 400; }
.status-flying { background: #000; color: #fff; }
.status-maintenance { background: #f0f0f0; border-color: #ccc; color: #666; }
.status-scrapped { border-color: #e0e0e0; color: #ccc; font-weight: 400; }

/* 信息网格 */
.info-grid {
  display: flex;
  flex-wrap: wrap;
}

.info-item {
  width: 50%;
  margin-bottom: 16rpx;
}

.info-label {
  font-size: 24rpx;
  color: #999;
  display: block;
  margin-bottom: 4rpx;
}

.info-value {
  font-size: 28rpx;
  color: #333;
}

/* 区块标签 */
.section-label {
  font-size: 26rpx;
  color: #999;
  margin-bottom: 16rpx;
}

/* 电量 */
.battery-display {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.battery-bar-lg {
  flex: 1;
  height: 12rpx;
  background: #f0f0f0;
}

.battery-fill-lg {
  height: 100%;
  background: #000;
}

.battery-pct {
  font-size: 28rpx;
  font-weight: 600;
  color: #000;
  width: 70rpx;
}

/* 位置 */
.map-section {
  cursor: pointer;
}

.location-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.location-text {
  font-size: 28rpx;
  color: #333;
}

.location-link {
  font-size: 26rpx;
  color: #000;
  font-weight: 600;
}

/* 描述 */
.desc-text {
  font-size: 28rpx;
  color: #666;
  line-height: 1.6;
}

/* 底部栏 */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  border-top: 2rpx solid #000;
  display: flex;
  align-items: center;
  padding: 16rpx 24rpx;
  padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
}

.price-section {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.price-main {
  display: flex;
  align-items: baseline;
}

.price-symbol {
  font-size: 24rpx;
  font-weight: 700;
  color: #000;
}

.price-value {
  font-size: 44rpx;
  font-weight: 800;
  color: #000;
  line-height: 1;
}

.price-unit {
  font-size: 22rpx;
  color: #999;
}

.insurance-info {
  font-size: 22rpx;
  color: #999;
}

.rent-btn {
  padding: 16rpx 48rpx;
  background: #000;
  color: #fff;
  font-size: 30rpx;
  font-weight: 600;
  border: none;
  white-space: nowrap;
}

.rent-btn--disabled {
  background: #e0e0e0;
  color: #999;
}

/* 弹窗遮罩 */
.modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  z-index: 100;
}

.modal-content {
  width: 100%;
  background: #fff;
  border-top: 2rpx solid #000;
  padding: 32rpx;
  padding-bottom: calc(32rpx + env(safe-area-inset-bottom));
}

.modal-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #000;
  display: block;
  margin-bottom: 24rpx;
}

.modal-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16rpx;
}

.modal-label {
  font-size: 28rpx;
  color: #666;
}

.modal-value {
  font-size: 28rpx;
  color: #000;
  font-weight: 600;
}

/* 天数选择 */
.days-selector {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.day-btn {
  width: 48rpx;
  height: 48rpx;
  border: 2rpx solid #000;
  background: #fff;
  font-size: 32rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
  padding: 0;
}

.day-num {
  font-size: 32rpx;
  font-weight: 700;
  min-width: 40rpx;
  text-align: center;
}

.modal-divider {
  height: 2rpx;
  background: #e0e0e0;
  margin: 16rpx 0;
}

.modal-total {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24rpx;
}

.total-label {
  font-size: 28rpx;
  color: #000;
  font-weight: 600;
}

.total-price {
  font-size: 40rpx;
  font-weight: 800;
  color: #000;
}

.modal-actions {
  display: flex;
  gap: 16rpx;
}

.modal-cancel {
  flex: 1;
  height: 88rpx;
  border: 2rpx solid #000;
  background: #fff;
  color: #000;
  font-size: 30rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-confirm {
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
</style>

<template>
  <view class="scan-page">
    <view class="header">
      <text class="title">扫码租机</text>
      <text class="subtitle">扫描无人机机身二维码开始租赁</text>
    </view>

    <view class="scan-area" @click="handleScan">
      <view class="scan-frame">
        <view class="corner tl"></view>
        <view class="corner tr"></view>
        <view class="corner bl"></view>
        <view class="corner br"></view>
        <text class="scan-hint">点击扫码</text>
      </view>
    </view>

    <view v-if="deviceInfo" class="device-card">
      <view class="device-name">{{ deviceInfo.deviceName }}</view>
      <view class="device-model">{{ deviceInfo.model }}</view>
      <view class="device-price">¥{{ deviceInfo.rentalPrice }}/小时</view>
      <view class="device-status" :class="deviceInfo.status === 1 ? 'online' : 'offline'">
        {{ deviceInfo.status === 1 ? '可租' : '不可租' }}
      </view>
    </view>

    <view v-if="deviceInfo && deviceInfo.status === 1" class="order-form">
      <view class="form-title">创建租赁订单</view>
      <view class="form-item">
        <text class="label">设备</text>
        <text class="value">{{ deviceInfo.deviceName }} ({{ deviceInfo.model }})</text>
      </view>
      <view class="form-row">
        <view class="form-item half">
          <text class="label">开始时间</text>
          <picker mode="date" :value="startDate" @change="onStartDateChange">
            <text>{{ startDate }}</text>
          </picker>
        </view>
        <view class="form-item half">
          <text class="label">结束时间</text>
          <picker mode="date" :value="endDate" @change="onEndDateChange">
            <text>{{ endDate }}</text>
          </picker>
        </view>
      </view>
      <view class="form-item">
        <text class="label">预估金额</text>
        <text class="price">¥{{ estimatedAmount }}</text>
      </view>
      <button class="submit-btn" :disabled="submitting" @click="handleSubmit">
        {{ submitting ? '提交中...' : '确认下单' }}
      </button>
    </view>

    <view v-if="errorMsg" class="error-tip">{{ errorMsg }}</view>
    <view v-if="successMsg" class="success-tip">{{ successMsg }}</view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { createOrderApi } from '@/api/order'

const deviceInfo = ref<any>(null)
const startDate = ref(getToday())
const endDate = ref(getTomorrow())
const submitting = ref(false)
const errorMsg = ref('')
const successMsg = ref('')

function getToday() { const d = new Date(); return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}` }
function getTomorrow() { const d = new Date(); d.setDate(d.getDate()+1); return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}` }

const estimatedAmount = computed(() => {
  if (!deviceInfo.value?.rentalPrice) return 0
  const start = new Date(startDate.value)
  const end = new Date(endDate.value)
  const hours = Math.max(1, Math.ceil((end.getTime() - start.getTime()) / 3600000))
  return (hours * parseFloat(deviceInfo.value.rentalPrice)).toFixed(2)
})

function onStartDateChange(e: any) { startDate.value = e.detail.value }
function onEndDateChange(e: any) { endDate.value = e.detail.value }

async function handleScan() {
  errorMsg.value = ''
  try {
    // @ts-ignore
    const res = await uni.scanCode({ onlyFromCamera: true })
    const scanned = res.result
    // 扫码结果期望格式: deviceId=123
    const match = scanned.match(/deviceId[=:](\d+)/)
    if (!match) {
      errorMsg.value = '无效二维码，请扫描 SkyTrust 无人机机身二维码'
      return
    }
    const deviceId = parseInt(match[1])
    await loadDevice(deviceId)
  } catch (e: any) {
    if (e.errMsg !== 'scanCode:fail cancel') {
      errorMsg.value = '扫码失败，请重试'
    }
  }
}

async function loadDevice(deviceId: number) {
  try {
    const res = await uni.request({ url: `/api/devices/${deviceId}`, method: 'GET' })
    deviceInfo.value = (res.data as any)?.data
  } catch {
    errorMsg.value = '获取设备信息失败'
  }
}

async function handleSubmit() {
  if (!deviceInfo.value) return
  submitting.value = true; errorMsg.value = ''; successMsg.value = ''
  try {
    const payload = {
      deviceId: deviceInfo.value.id,
      startTime: startDate.value + 'T00:00:00',
      endTime: endDate.value + 'T00:00:00',
      totalAmount: parseFloat(estimatedAmount.value),
      insuranceFee: 0,
      paymentMethod: 'wechat',
    }
    const res = await createOrderApi(payload as any)
    if ((res as any)?.code === 0 || (res as any)?.success) {
      successMsg.value = '下单成功！查看我的订单'
      setTimeout(() => uni.switchTab({ url: '/pages/orders/index' }), 1500)
    } else {
      errorMsg.value = (res as any)?.message || '下单失败'
    }
  } catch (e: any) {
    errorMsg.value = e?.message || '网络错误，下单失败'
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.scan-page { min-height: 100vh; background: #f5f5f5; padding: 32px 24px; display: flex; flex-direction: column; align-items: center; }
.header { text-align: center; margin-bottom: 40px; }
.title { font-size: 22px; font-weight: 700; color: #000; display: block; }
.subtitle { font-size: 13px; color: #999; margin-top: 8px; display: block; }
.scan-area { margin-bottom: 32px; }
.scan-frame { width: 240px; height: 240px; border: 2px solid #000; position: relative; display: flex; align-items: center; justify-content: center; }
.corner { position: absolute; width: 20px; height: 20px; border-color: #000; border-style: solid; }
.corner.tl { top: -2px; left: -2px; border-width: 4px 0 0 4px; }
.corner.tr { top: -2px; right: -2px; border-width: 4px 4px 0 0; }
.corner.bl { bottom: -2px; left: -2px; border-width: 0 0 4px 4px; }
.corner.br { bottom: -2px; right: -2px; border-width: 0 4px 4px 0; }
.scan-hint { font-size: 14px; color: #666; }
.device-card { width: 100%; max-width: 400px; background: #fff; border: 2px solid #000; padding: 20px; margin-bottom: 24px; }
.device-name { font-size: 18px; font-weight: 700; }
.device-model { font-size: 13px; color: #999; margin-top: 4px; }
.device-price { font-size: 20px; font-weight: 700; margin-top: 8px; }
.device-status { display: inline-block; padding: 2px 10px; font-size: 12px; font-weight: 600; margin-top: 8px; }
.device-status.online { background: #000; color: #fff; }
.device-status.offline { background: #e0e0e0; color: #000; }
.order-form { width: 100%; max-width: 400px; background: #fff; border: 2px solid #000; padding: 20px; }
.form-title { font-size: 16px; font-weight: 700; margin-bottom: 16px; padding-bottom: 10px; border-bottom: 2px solid #000; }
.form-item { margin-bottom: 14px; }
.form-item .label { font-size: 12px; color: #999; display: block; margin-bottom: 4px; }
.form-item .value { font-size: 15px; font-weight: 500; }
.form-item .price { font-size: 20px; font-weight: 700; }
.form-row { display: flex; gap: 12px; }
.form-item.half { flex: 1; }
.submit-btn { width: 100%; padding: 14px; background: #000; color: #fff; font-size: 16px; font-weight: 600; border: none; margin-top: 12px; }
.submit-btn[disabled] { background: #ccc; color: #999; }
.error-tip { margin-top: 16px; padding: 12px; background: #e0e0e0; color: #000; font-size: 14px; width: 100%; max-width: 400px; text-align: center; }
.success-tip { margin-top: 16px; padding: 12px; background: #000; color: #fff; font-size: 14px; width: 100%; max-width: 400px; text-align: center; }
</style>

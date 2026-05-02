<template>
  <section class="blockchain-page page-container licensing-page">
    <!-- Verification Panel -->
    <div class="card verification-panel" aria-label="License verification">
      <div class="row" style="display:flex; gap:12px; align-items:center;">
        <input v-model="orderId" placeholder="请输入订单 ID (orderId)" class="input" type="text" />
        <button class="btn primary" @click="verify" :disabled="loadingVerify">{{ loadingVerify ? '验证中…' : '验证许可证' }}</button>
      </div>
      <div class="hint" style="margin-top:8px; color:#666; font-size:12px;">注: 使用订单 ID 验证许可证并查看绑定信息。</div>
    </div>

    <!-- Result Card (collapsible) -->
    <div v-if="licenseRecord" class="card result-card" aria-label="License result">
      <div class="row" style="display:flex; justify-content: space-between; align-items:center;">
        <div class="section-title" style="font-weight:700;">许可证信息</div>
        <span class="badge" :class="{'green': licenseRecord?.status === 'VERIFIED', 'red': licenseRecord?.status !== 'VERIFIED'}">
          {{ licenseRecord?.status ?? '' }}
        </span>
      </div>
      <div class="details" style="margin-top:12px; display:grid; grid-template-columns: repeat(2, 1fr); gap:12px;">
        <div><strong>UserDID:</strong> {{ licenseRecord?.userDID ?? '-' }}</div>
        <div><strong>LicenseHash:</strong> {{ licenseRecord?.licenseHash ?? '-' }}</div>
        <div v-if="licenseRecord?.orderId"><strong>Order ID:</strong> {{ licenseRecord?.orderId }}</div>
        <div v-if="licenseRecord?.orderBinding"><strong>Order Binding:</strong> {{ licenseRecord?.orderBinding }}</div>
      </div>
    </div>

    <!-- Rental Order Form (Admin) -->
    <div class="card rental-form" aria-label="Rental order form" v-if="isAdmin">
      <div style="display:flex; justify-content: space-between; align-items: center; margin-bottom:8px;">
        <strong>创建租赁订单 (Admin)</strong>
        <button class="btn toggle" @click="toggleCreateMode">切换模式: {{ createMode ? '创建中' : '验证中' }}</button>
      </div>
      <div v-if="createMode" class="form" style="display:grid; grid-template-columns: 1fr 1fr; gap:12px; align-items:end;">
        <div>
          <label>用户 DID</label>
          <input v-model="form.userDID" class="input" type="text" />
        </div>
        <div>
          <label>LicenseHash</label>
          <input v-model="form.licenseHashInput" class="input" type="text" />
        </div>
        <div style="grid-column:span 2; display:flex; gap:12px; align-items:center;">
          <label>签名</label>
          <input v-model="form.signature" class="input" type="text" placeholder="签名签字 (signature)" />
          <button class="btn primary" @click="createRental" :disabled="creating">{{ creating ? '创建中…' : '创建租赁订单' }}</button>
        </div>
      </div>
      <div class="created-info" v-if="createdOrder" style="margin-top:12px; padding:12px; background: rgba(0,0,0,.03); border-radius:8px;">
        <div><strong>租赁订单创建成功</strong> - 订单ID: {{ createdOrder?.orderId }}</div>
        <div>状态: {{ createdOrder?.status }} </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import type { LicenseRecord } from '@/types/api'
import { verifyLicense, getLicenseHashByOrder, createRentalOrder } from '@/api/blockchain'

// Admin flag (in real app, fetch current user role)
const isAdmin = true

// Verification state
const orderId = ref<string>('')
const licenseRecord = ref<LicenseRecord | null>(null)
const loadingVerify = ref(false)
const errorVerify = ref<string | null>(null)

// Create mode flag
const createMode = ref(true)
function toggleCreateMode() { createMode.value = !createMode.value }
const form = reactive({ userDID: '', licenseHashInput: '', signature: '' })
const creating = ref(false)
const createdOrder = ref<{ orderId: string; status: string } | null>(null)

// Verify license by orderId
async function verify() {
  errorVerify.value = null
  if (!orderId.value) { errorVerify.value = '请输入订单 ID'; return }
  loadingVerify.value = true
  try {
    const res = await verifyLicense(orderId.value)
    licenseRecord.value = (res?.data ?? res) as LicenseRecord
  } catch (e: any) {
    errorVerify.value = e?.message ?? '无法验证许可证'
  } finally {
    loadingVerify.value = false
  }
}

// Fetch license hash by order (optional helper on page load)
async function fetchHash() {
  try {
    const res = await getLicenseHashByOrder(orderId.value)
    // update licenseRecord with hash if available
    if (res?.data?.licenseHash) {
      licenseRecord.value = { ...(licenseRecord.value ?? {} as LicenseRecord), licenseHash: res.data.licenseHash } as LicenseRecord
    }
  } catch {
    // ignore
  }
}

// Create rental order (admin)
async function createRental() {
  if (!form.userDID || !form.licenseHashInput || !form.signature) return
  creating.value = true
  try {
    const payload = { userDID: form.userDID, licenseHash: form.licenseHashInput, signature: form.signature }
    const res = await createRentalOrder(payload)
    createdOrder.value = res?.data ?? res
  } catch (e: any) {
    // show error in console. For simplicity, push to createdOrder with error-like shape
    createdOrder.value = null
  } finally {
    creating.value = false
  }
}

</script>

<style scoped>
.page-container { padding: 20px; display: flex; flex-direction: column; gap: 20px; }
.input { padding: 10px 12px; border: 1px solid #ccc; min-width: 260px; }
.card { background: #fff; border: 1px solid #e0e0e0; padding: 16px; }
.verification-panel { display: flex; flex-direction: column; gap: 8px; }
.row { display:flex; gap:12px; align-items:center; }
.badge { display:inline-block; padding:6px 12px; font-weight:700; border: 2px solid #000; }
.green { background: #000; color: #fff; }
.red { background: #e0e0e0; color: #000; border-style: dashed; }
.title { font-size: 1.15rem; font-weight: 700; color: #000; }
.rental-form { margin-top: 8px; }
.form { display:grid; gap:12px; margin-top:8px; }
.created-info { border-left: 3px solid #000; padding-left: 12px; }
.table { width: 100%; border-collapse: collapse; }
.table th, .table td { border-bottom: 1px solid #e0e0e0; padding: 8px; font-size: 13px; text-align:left; }
.empty-state { text-align:center; padding:16px; color:#666; }
.btn { padding: 10px 14px; border: 2px solid #000; cursor: pointer; font-weight: 600; font-size: 14px; }
.btn.primary { background: #000; color: #fff; }
.btn.primary:hover { background: #fff; color: #000; }
.btn.secondary { background: #fff; color: #000; }
.btn.secondary:hover { background: #000; color: #fff; }
</style>

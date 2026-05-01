<template>
  <div class="wallet-page">
    <!-- Wallet Balance Card -->
    <div class="stat-row">
      <div class="stat-card" style="border-top-color: #000;">
        <div class="stat-value">¥{{ balanceDisplay }}</div>
        <div class="stat-label">钱包余额</div>
      </div>
    </div>

    <!-- Recharge Button -->
    <div class="action-bar" style="justify-content:flex-start; margin-bottom:12px;">
      <button class="btn btn-primary" @click="openRechargeDialog">充值</button>
    </div>

    <!-- Transaction History -->
    <div class="table-wrap">
      <table class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Type</th>
            <th>Amount</th>
            <th>Balance Before</th>
            <th>Balance After</th>
            <th>Description</th>
            <th>Order ID</th>
            <th>Create Time</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loadingTx">
            <td colspan="8" class="td-center">加载中...</td>
          </tr>
          <tr v-else-if="transactions.length === 0">
            <td colspan="8" class="td-center">暂无交易记录</td>
          </tr>
          <tr v-for="tx in transactions" :key="tx.id">
            <td>{{ tx.id }}</td>
            <td>
              <span class="tag" :class="tagClass(tx.type)">{{ typeLabel(tx.type) }}</span>
            </td>
            <td>
              <span :style="amountStyle(tx.amount)">¥{{ tx.amount >= 0 ? '+' : '-' }}{{ Math.abs(tx.amount).toFixed(2) }}</span>
            </td>
            <td>¥{{ (tx.balanceBefore ?? 0).toFixed(2) }}</td>
            <td>¥{{ (tx.balanceAfter ?? 0).toFixed(2) }}</td>
            <td>{{ tx.description ?? '-' }}</td>
            <td>{{ tx.orderId ?? '-' }}</td>
            <td>{{ formatTime(tx.createTime) }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Pagination -->
    <div class="pagination" v-if="!loadingTx">
      <button class="btn btn-outline" :disabled="txPage <= 1" @click="prevPage">上一页</button>
      <span class="page-info">第 {{ txPage }} 页</span>
      <button class="btn btn-outline" :disabled="!hasMoreTx" @click="nextPage">下一页</button>
    </div>

    <!-- Recharge Dialog (Teleported) -->
    <teleport to="body">
      <div v-if="rechargeVisible" class="dialog-overlay" @click.self="rechargeVisible = false">
        <div class="dialog" style="max-width:480px;">
          <div class="dialog-header">
            <h3>充值</h3>
            <button class="dialog-close" @click="rechargeVisible = false">✕</button>
          </div>
          <div class="dialog-body">
            <div class="form-grid" style="grid-template-columns: 1fr; gap: 12px;">
              <div class="form-group">
                <label>金额 (¥) <span class="required">*</span></label>
                <input class="input" v-model.number="rechargeForm.amount" type="number" min="0" step="0.01" placeholder="输入充值金额" />
              </div>
              <div class="form-group">
                <label>描述</label>
                <textarea class="textarea" v-model="rechargeForm.description" rows="3" placeholder="充值描述（可选）"></textarea>
              </div>
            </div>
          </div>
          <div class="dialog-footer">
            <button class="btn btn-outline" @click="rechargeVisible = false">取消</button>
            <button class="btn btn-primary" @click="handleRecharge" :disabled="!rechargeForm.amount || submittingRecharge">
              {{ submittingRecharge ? '充值中...' : '确认充值' }}
            </button>
          </div>
        </div>
      </div>
    </teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import type { WalletVO, WalletTransactionVO, RechargeDTO } from '@/types/api'
import { getBalanceApi, getTransactionsApi, rechargeApi } from '@/api/wallet'

// Balance
const balance = ref<WalletVO | null>(null)
const loading = ref(false)
const balanceDisplay = computed(() => {
  const amt = (balance.value?.balance ?? 0) as number
  return amt?.toFixed(2) ?? '0.00'
})

// Transactions
const transactions = ref<WalletTransactionVO[]>([])
const loadingTx = ref(false)
const txPage = ref(1)
const txPageSize = 10
const txTotal = ref(0)

const txHasMore = computed(() => {
  return txPage.value * txPageSize < txTotal.value
})
const hasMoreTx = computed(() => txHasMore.value)

// Recharge dialog
const rechargeVisible = ref(false)
type RechargeFormType = { amount: number | null; description: string; [key: string]: any }
const rechargeForm = reactive<RechargeFormType>({ amount: null, description: '' })
const submittingRecharge = ref(false)

// UI helpers
function typeLabel(t: number | undefined): string {
  const map: Record<number, string> = { 0: '充值', 1: '消费', 2: '退款', 3: '提现' }
  const key = t ?? 0
  return map[key] ?? '未知'
}
function tagClass(t: number | undefined): string {
  // CSS classes to color-coded tags
  const map: Record<number, string> = { 0: 'tag', 1: 'tag', 2: 'tag', 3: 'tag' }
  const key = t ?? 0
  return map[key] ?? 'tag'
}
function amountStyle(amount: number) {
  // Positive green, negative red, zero black
  const color = amount > 0 ? '#4caf50' : amount < 0 ? '#f44336' : '#000'
  const sign = amount >= 0 ? '+' : ''
  return { color, fontWeight: 600 }
}
function formatTime(t?: string): string {
  if (!t) return '-'
  // Simple formatting: replace T with space and trim seconds if ISO-like
  const s = t.toString()
  return s.includes('T') ? s.replace('T', ' ').slice(0, 19) : s
}
function formatMoney(n?: number) {
  if (n == null) return '0.00'
  return Number(n).toFixed(2)
}

async function loadBalance() {
  loading.value = true
  try {
    const res = await getBalanceApi()
    // Normalize to WalletVO shape
    balance.value = (res?.data?.data ?? res?.data ?? null) as WalletVO | null
  } catch {
    balance.value = null
  } finally {
    loading.value = false
  }
}

async function loadTransactions() {
  loadingTx.value = true
  try {
    const res = await getTransactionsApi(txPage.value, txPageSize)
    const data = (res?.data?.data ?? res?.data ?? {}) as any
    // Support common shapes
    const list: WalletTransactionVO[] = data.list ?? data.items ?? []
    const total: number = data.total ?? data.totalItems ?? list.length
    transactions.value = list
    txTotal.value = total
  } catch {
    transactions.value = []
  } finally {
    loadingTx.value = false
  }
}

async function refreshAll() {
  await loadBalance()
  await loadTransactions()
}

// Actions
function openRechargeDialog() {
  rechargeForm.amount = null
  rechargeForm.description = ''
  rechargeVisible.value = true
}

async function handleRecharge() {
  if (rechargeForm.amount == null || rechargeForm.amount <= 0) {
    alert('请填写有效的充值金额')
    return
  }
  submittingRecharge.value = true
  try {
    const dto: RechargeDTO = {
      amount: rechargeForm.amount,
      description: rechargeForm.description || '',
    }
    await rechargeApi(dto)
    rechargeVisible.value = false
    await refreshAll()
  } catch (e: any) {
    alert(e?.message ?? '充值失败')
  } finally {
    submittingRecharge.value = false
  }
}

function prevPage() { if (txPage.value > 1) { txPage.value -= 1; loadTransactions() } }
function nextPage() { if (hasMoreTx.value) { txPage.value += 1; loadTransactions() } }

onMounted(() => {
  // Initial load
  refreshAll()
})

// Derived display values (no duplication of balanceDisplay)
</script>

<style scoped>
/* Reuse the same CSS classes as device page to maintain exact look */
.wallet-page { padding-bottom: 40px; }
.stat-row { display: grid; grid-template-columns: repeat(auto-fit, minmax(140px, 1fr)); gap: 12px; margin-bottom: 12px; }
.stat-card { background: #fff; border: 1px solid #e0e0e0; padding: 16px 20px; text-align: center; border-top: 3px solid #000; }
.stat-card .stat-value { font-size: 28px; font-weight: 700; }
.stat-card .stat-label { font-size: 12px; color: #666; margin-top: 6px; }
.action-bar { display: flex; gap: 12px; margin-bottom: 12px; }
.table-wrap { background: #fff; border: 1px solid #e0e0e0; overflow-x: auto; }
.data-table { width: 100%; border-collapse: collapse; font-size: 14px; min-width: 800px; }
.data-table thead { background: #fafafa; }
.data-table th, .data-table td { padding: 12px 14px; border-bottom: 1px solid #eee; text-align: left; }
.data-table th { border-bottom: 2px solid #000; font-weight: 600; }
.td-center { text-align: center; color: #999; padding: 20px 0; }
.tag { display: inline-block; padding: 2px 10px; border: 1px solid #000; font-size: 12px; }
.tag-online { }
.tag { font-weight: 500; }
.td-name { cursor: pointer; font-weight: 600; }
.td-name:hover { text-decoration: underline; }
.td-mono { font-family: Menlo, Consolas, monospace; font-size: 12px; }
.td-time { color: #666; font-size: 12px; }
.td-actions { display: flex; gap: 4px; }
.btn { padding: 6px 12px; border: none; background: #fff; border-radius: 2px; cursor: pointer; }
.btn-primary { background: #000; color: #fff; }
.btn-outline { background: #fff; border: 1px solid #ccc; color: #333; }
.btn-danger { background: #c62828; color: #fff; }
.input { padding: 6px 10px; border: 1px solid #ccc; font-size: 14px; }
.textarea { padding: 6px 10px; border: 1px solid #ccc; font-size: 14px; width: 100%; resize: vertical; }
.pagination { display: flex; align-items: center; justify-content: center; gap: 12px; margin: 14px 0; }
.dialog-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; padding: 20px; z-index: 1000; }
.dialog { background: #fff; border: 2px solid #000; padding: 0; width: 100%; max-width: 560px; display: flex; flex-direction: column; }
.dialog-header { display: flex; justify-content: space-between; align-items: center; padding: 12px 16px; border-bottom: 1px solid #e0e0e0; }
.dialog-header h3 { margin: 0; font-size: 14px; font-weight: 600; }
.dialog-body { padding: 16px; overflow: auto; }
.dialog-footer { display: flex; justify-content: flex-end; padding: 12px 16px; border-top: 1px solid #e0e0e0; }
.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.form-group { display: flex; flex-direction: column; gap: 6px; }
.form-group label { font-size: 12px; color: #555; }
.required { color: #d00; font-weight: 700; }
.confirm-msg { font-size: 13px; color: #333; margin-bottom: 8px; }
</style>

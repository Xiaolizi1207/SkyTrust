<template>
  <div class="payment-page">
    <!-- ========= 搜索过滤 ========= -->
    <div class="search-bar">
      <div class="search-row">
        <input v-model="query.transactionNo" placeholder="交易单号" class="input" @keyup.enter="handleSearch" />
        <input v-model="query.userId" placeholder="用户ID" type="number" class="input" @keyup.enter="handleSearch" />
        <input v-model="query.orderId" placeholder="订单ID" class="input" @keyup.enter="handleSearch" />
        <select v-model.number="query.status" class="select" @change="handleSearch">
          <option :value="undefined">全部状态</option>
          <option :value="0">待支付</option>
          <option :value="1">支付成功</option>
          <option :value="2">支付失败</option>
          <option :value="3">已退款</option>
        </select>
      </div>
      <div class="search-row">
        <button class="btn btn-primary" @click="handleSearch">搜索</button>
        <button class="btn btn-outline" @click="handleReset">重置</button>
      </div>
    </div>

    <!-- ========== 操作栏 ========== -->
    <div class="action-bar">
      <button class="btn btn-primary" @click="openCreateDialog">+ 新增支付记录</button>
    </div>

    <!-- ========== 表格 ========== -->
    <div class="table-wrap">
      <table class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>交易单号</th>
            <th>用户ID</th>
            <th>订单ID</th>
            <th>金额 (¥)</th>
            <th>支付方式</th>
            <th>状态</th>
            <th>描述</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="10" class="td-center">加载中...</td>
          </tr>
          <tr v-else-if="payments.length === 0">
            <td colspan="10" class="td-center">暂无支付记录</td>
          </tr>
          <tr v-for="p in payments" :key="p.id">
            <td>{{ p.id }}</td>
            <td class="td-mono">{{ p.transactionNo }}</td>
            <td>{{ p.userId }}</td>
            <td class="td-mono">{{ p.orderId ?? '-' }}</td>
            <td>¥{{ formatPrice(p.amount) }}</td>
            <td>{{ payTypeText(p.payType ?? 0) }}</td>
            <td>
              <span class="tag" :class="statusClass(p.status ?? 0)">{{ statusText(p.status ?? 0) }}</span>
            </td>
            <td class="td-long">{{ p.description ?? '-' }}</td>
            <td class="td-time">{{ formatTime(p.createTime) }}</td>
            <td class="td-actions">
              <button class="act-btn" title="查看" @click="openDetailDialog(p)">👁</button>
              <button class="act-btn" title="编辑" @click="openEditDialog(p)">✏</button>
              <button class="act-btn danger" title="删除" @click="handleDelete(p)">🗑</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- ========== 分页 ========== -->
    <div class="pagination" v-if="!loading">
      <button class="btn btn-outline" :disabled="(query.page ?? 1) === 1" @click="goPage((query.page ?? 1) - 1)">上一页</button>
      <span class="page-info">第 {{ query.page ?? 1 }} 页</span>
      <button class="btn btn-outline" :disabled="isLastPage" @click="goPage((query.page ?? 1) + 1)">下一页</button>
    </div>

    <!-- ========== Create/Edit/Detail Teleport ========== -->
    <teleport to="body">
      <div v-if="dialogVisible" class="dialog-overlay" @click.self="dialogVisible = false">
        <div class="dialog" :class="{ wide: dialogMode !== 'detail' }">
          <div class="dialog-header">
            <h3>{{ dialogTitle }}</h3>
            <button class="dialog-close" @click="dialogVisible = false">✕</button>
          </div>
          <div class="dialog-body">
            <!-- 详情 -->
            <template v-if="dialogMode === 'detail'">
              <div class="detail-grid">
                <div class="detail-item"><label>ID</label><span>{{ form.id }}</span></div>
                <div class="detail-item"><label>交易单号</label><span>{{ form.transactionNo }}</span></div>
                <div class="detail-item"><label>用户ID</label><span>{{ form.userId }}</span></div>
                <div class="detail-item"><label>订单ID</label><span>{{ form.orderId ?? '-' }}</span></div>
                <div class="detail-item"><label>金额</label><span>¥{{ formatPrice(form.amount) }}</span></div>
                <div class="detail-item"><label>支付方式</label><span>{{ payTypeText(form.payType ?? 0) }}</span></div>
                <div class="detail-item"><label>状态</label><span class="tag" :class="statusClass(form.status ?? 0)">{{ statusText(form.status ?? 0) }}</span></div>
                <div class="detail-item wide"><label>描述</label><span>{{ form.description ?? '-' }}</span></div>
                <div class="detail-item"><label>创建时间</label><span>{{ formatTime(form.createTime) }}</span></div>
              </div>
            </template>

            <!-- 编辑/创建 -->
            <template v-else>
              <div class="form-grid">
                <div class="form-group">
                  <label>用户ID <span class="required">*</span></label>
                  <input v-model.number="form.userId" type="number" class="input" placeholder="请输入用户ID" />
                </div>
                <div class="form-group">
                  <label>订单ID</label>
                  <input v-model="form.orderId" class="input" placeholder="请输入订单ID" />
                </div>
                <div class="form-group">
                  <label>金额 (元) <span class="required">*</span></label>
                  <input v-model.number="form.amount" type="number" class="input" step="0.01" />
                </div>
                <div class="form-group">
                  <label>支付方式</label>
                  <select v-model.number="form.payType" class="select">
                    <option :value="0">微信</option>
                    <option :value="1">支付宝</option>
                    <option :value="2">银行卡</option>
                    <option :value="3">钱包余额</option>
                  </select>
                </div>
                <div class="form-group wide">
                  <label>描述</label>
                  <textarea v-model="form.description" class="textarea" placeholder="支付描述" maxlength="1000" rows="2"></textarea>
                </div>
              </div>
            </template>
          </div>
          <div class="dialog-footer" v-if="dialogMode !== 'detail'">
            <button class="btn btn-outline" @click="dialogVisible = false">取消</button>
            <button class="btn btn-primary" @click="handleSubmit" :disabled="submitting">
              {{ submitting ? '提交中...' : (dialogMode === 'create' ? '创建' : '保存') }}
            </button>
          </div>
          <div class="dialog-footer" v-else>
            <button class="btn btn-outline" @click="dialogVisible = false">关闭</button>
          </div>
        </div>
      </div>
    </teleport>
  </div>
  </template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import type { PaymentVO, PaymentDTO, PaymentQueryParams } from '@/types/api'
import {
  getPaymentListApi,
  createPaymentApi,
  updatePaymentApi,
  deletePaymentApi,
} from '@/api/payment'

// ========== 数据 ========== 
const payments = ref<PaymentVO[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogMode = ref<'create' | 'edit' | 'detail'>('create')
const submitting = ref(false)
const form = reactive<Partial<PaymentVO> & PaymentDTO>({ id: 0, userId: 0, amount: 0, payType: 0, description: '' })
const query = reactive<PaymentQueryParams>({ page: 1, size: 10, transactionNo: undefined, userId: undefined, orderId: undefined, status: undefined })

const confirmVisible = ref(false) // reserved for future confirmation if needed

// title helper for dialog
const dialogTitle = computed(() => {
  if (dialogMode.value === 'create') return '创建支付记录'
  if (dialogMode.value === 'edit') return '编辑支付记录'
  return '支付记录详情'
})

const formDefault = (): PaymentDTO & Partial<PaymentVO> => ({ userId: 0, orderId: 0, amount: 0, payType: 0, description: '' })

// 将数据转成前端需要的展示格式
function formatTime(t?: string): string {
  if (!t) return '-'
  return t.replace('T', ' ').substring(0, 19)
}
function formatPrice(v?: number): string {
  if (v == null) return '0.00'
  return v.toFixed(2)
}

function payTypeText(t?: number): string {
  const map: Record<number, string> = { 0: '微信', 1: '支付宝', 2: '银行卡', 3: '钱包余额' }
  return map[t ?? 0] ?? '未知'
}
function statusText(s?: number): string {
  const map: Record<number, string> = { 0: '待支付', 1: '支付成功', 2: '支付失败', 3: '已退款' }
  return map[s ?? 0] ?? '未知'
}
function statusClass(s?: number): string {
  const map: Record<number, string> = { 0: 'tag-offline', 1: 'tag-online', 2: 'tag-maintain', 3: 'tag-scrapped' }
  return map[s ?? 0] ?? ''
}

/** 获取支付列表 */
async function fetchPayments() {
  loading.value = true
  try {
    const params = {
      page: query.page ?? 1,
      size: query.size ?? 10,
      transactionNo: query.transactionNo,
      userId: query.userId,
      orderId: query.orderId,
      status: (query.status ?? 0),
    } as any
    const res = await getPaymentListApi(params)
    payments.value = res.data.data || []
  } catch (e) {
    console.error('获取支付记录失败', e)
    payments.value = []
  } finally {
    loading.value = false
  }
}

/** 搜索/重置/分页 */
function handleSearch() {
  query.page = 1
  fetchPayments()
}
function handleReset() {
  query.page = 1
  query.transactionNo = undefined
  query.userId = undefined
  query.orderId = undefined
  query.status = undefined
  fetchPayments()
}
function goPage(p: number) {
  query.page = p
  fetchPayments()
}
const isLastPage = computed(() => payments.value.length < (query.size ?? 10))

/** 创建/编辑弹窗 */
function openCreateDialog() {
  Object.assign(form, formDefault())
  dialogMode.value = 'create'
  dialogVisible.value = true
}
function openEditDialog(p: PaymentVO) {
  Object.assign(form, p)
  dialogMode.value = 'edit'
  dialogVisible.value = true
}
function openDetailDialog(p: PaymentVO) {
  Object.assign(form, p)
  dialogMode.value = 'detail'
  dialogVisible.value = true
}

async function handleSubmit() {
  if (form.userId == null || form.amount == null) {
    alert('请填写必填字段')
    return
  }
  submitting.value = true
  const dto: PaymentDTO = {
    userId: form.userId as number,
    orderId: form.orderId,
    amount: form.amount as number,
    payType: form.payType ?? 0,
    description: form.description ?? '',
  }
  try {
    if (dialogMode.value === 'create') {
      await createPaymentApi(dto)
    } else {
      await updatePaymentApi(form.id as number, dto)
    }
    dialogVisible.value = false
    fetchPayments()
  } catch (e: any) {
    alert(e?.message ?? '操作失败')
  } finally {
    submitting.value = false
  }
}

/** 删除 */
async function handleDelete(p: PaymentVO) {
  if (!confirm(`确认删除支付记录 ${p.id} 吗？`)) return
  try {
    await deletePaymentApi(p.id as number)
    fetchPayments()
  } catch (e: any) {
    alert(e?.message ?? '删除失败')
  }
}

onMounted(() => {
  fetchPayments()
})
</script>

<style scoped>
.payment-page {
  max-width: 1320px;
}

/* 搜索条样式沿用 device 页面的 pattern */
.search-bar {
  background: #fff;
  border: 1px solid #e0e0e0;
  padding: 16px 20px;
  margin-bottom: 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.search-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.sep { color: #999; flex-shrink: 0; }

/* 表单控件 */
.input { padding: 8px 12px; border: 1px solid #ccc; font-size: 14px; color: #333; background: #fff; outline: none; transition: border-color 0.2s; min-width: 120px; }
.input:focus { border-color: #000; }
.select { padding: 8px 12px; border: 1px solid #ccc; font-size: 14px; color: #333; background: #fff; outline: none; cursor: pointer; min-width: 120px; }
.textarea { padding: 8px 12px; border: 1px solid #ccc; font-size: 14px; color: #333; background: #fff; outline: none; resize: vertical; width: 100%; box-sizing: border-box; font-family: inherit; }
.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.form-group { display: flex; flex-direction: column; gap: 6px; }
.form-group.wide { grid-column: 1 / -1; }
.form-group label { font-size: 13px; font-weight: 500; color: #333; }
.required { color: #000; font-weight: 700; }

/* 按钮 */
.btn { padding: 8px 16px; font-size: 14px; cursor: pointer; border: none; transition: background 0.2s, color 0.2s, border-color 0.2s; white-space: nowrap; }
.btn:disabled { opacity: 0.5; cursor: not-allowed; }
.btn-primary { background: #000; color: #fff; }
.btn-primary:hover:not(:disabled) { background: #333; }
.btn-outline { background: #fff; color: #333; border: 1px solid #ccc; }
.btn-outline:hover:not(:disabled) { border-color: #000; color: #000; }
.btn-danger { background: #000; color: #fff; }
.btn-danger:hover:not(:disabled) { background: #333; }
.action-bar { display: flex; align-items: center; gap: 10px; margin-bottom: 16px; }

/* 表格 */
.table-wrap { background: #fff; border: 1px solid #e0e0e0; overflow-x: auto; }
.data-table { width: 100%; border-collapse: collapse; font-size: 14px; min-width: 1100px; }
.data-table thead { background: #fafafa; }
.data-table th { padding: 12px 14px; text-align: left; font-weight: 600; color: #333; border-bottom: 2px solid #000; white-space: nowrap; }
.data-table td { padding: 12px 14px; border-bottom: 1px solid #e0e0e0; color: #333; white-space: nowrap; }
.data-table tbody tr:hover { background: #fafafa; }
.td-center { text-align: center; color: #999; padding: 40px 14px; }
.td-name { color: #000; cursor: pointer; font-weight: 600; }
.td-name:hover { text-decoration: underline; }
.td-mono { font-family: monospace; font-size: 13px; }
.td-time { font-size: 13px; color: #666; }
.td-actions { display: flex; gap: 4px; }
.act-btn { background: none; border: 1px solid #ccc; padding: 4px 8px; cursor: pointer; font-size: 14px; transition: background 0.2s, border-color 0.2s; }
.act-btn:hover { background: #f0f0f0; border-color: #000; }
.tag { display: inline-block; padding: 2px 10px; font-size: 12px; font-weight: 500; background: #fff; color: #000; border: 1px solid #000; }
.tag-online { border-width: 2px; font-weight: 700; }
.tag-offline { border-color: #ccc; color: #999; font-weight: 400; }
.tag-maintain { background: #f0f0f0; border-color: #ccc; font-weight: 400; }
.tag-scrapped { background: #f0f0f0; border-color: #ccc; color: #999; text-decoration: line-through; }
.td-long { max-width: 300px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.td-loc { font-family: monospace; font-size: 13px; }
.td-time { color: #666; }

/* 分页 */
.pagination { display: flex; align-items: center; gap: 12px; margin-top: 16px; justify-content: center; }
.page-info { font-size: 14px; color: #666; }

/* 弹窗 */
.dialog-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.5); z-index: 1000; display: flex; align-items: center; justify-content: center; padding: 20px; }
.dialog { background: #fff; border: 2px solid #000; width: 100%; max-width: 560px; max-height: 90vh; display: flex; flex-direction: column; }
.dialog.wide { max-width: 720px; }
.dialog-sm { max-width: 420px; }
.dialog-header { display: flex; align-items: center; justify-content: space-between; padding: 16px 24px; border-bottom: 1px solid #e0e0e0; flex-shrink: 0; }
.dialog-header h3 { margin: 0; font-size: 16px; font-weight: 600; color: #000; }
.dialog-close { background: none; border: none; font-size: 20px; cursor: pointer; color: #999; padding: 0; line-height: 1; }
.dialog-body { padding: 24px; overflow-y: auto; flex: 1; }
.dialog-footer { display: flex; justify-content: flex-end; gap: 10px; padding: 16px 24px; border-top: 1px solid #e0e0e0; flex-shrink: 0; }
.detail-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.detail-item { display: flex; flex-direction: column; gap: 4px; }
.detail-item label { font-size: 12px; font-weight: 500; color: #666; text-transform: uppercase; }
.detail-item span { font-size: 14px; color: #333; }
.confirm-msg { font-size: 14px; color: #333; margin: 0 0 16px; }
</style>

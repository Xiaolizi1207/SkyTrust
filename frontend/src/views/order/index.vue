<template>
  <div class="device-page">
    <!-- ========== 统计卡片 ========== -->
    <div class="stat-row">
      <div class="stat-card">
        <div class="stat-value">{{ stats.totalOrders }}</div>
        <div class="stat-label">订单总数</div>
      </div>
      <div class="stat-card online">
        <div class="stat-value">{{ stats.pendingPayment }}</div>
        <div class="stat-label">待支付</div>
      </div>
      <div class="stat-card flying">
        <div class="stat-value">{{ stats.inProgress }}</div>
        <div class="stat-label">进行中</div>
      </div>
      <div class="stat-card offline">
        <div class="stat-value">{{ stats.completed }}</div>
        <div class="stat-label">已完成</div>
      </div>
      <div class="stat-card maintenance">
        <div class="stat-value">{{ stats.cancelled }}</div>
        <div class="stat-label">已取消</div>
      </div>
    </div>

    <!-- ========== 搜索过滤 ========== -->
    <div class="search-bar">
      <div class="search-row">
        <input v-model="query.orderNo" placeholder="订单号" class="input" @keyup.enter="fetchOrders" />
        <input v-model.number="query.userId" placeholder="用户ID" class="input" @keyup.enter="fetchOrders" />
        <input v-model.number="query.deviceId" placeholder="设备ID" class="input" @keyup.enter="fetchOrders" />
        <select v-model="query.status" class="select">
          <option :value="undefined">全部状态</option>
          <option :value="0">待支付</option>
          <option :value="1">进行中</option>
          <option :value="2">已完成</option>
          <option :value="3">已取消</option>
        </select>
      </div>
      <div class="search-row">
        <button class="btn btn-primary" @click="fetchOrders">搜索</button>
        <button class="btn btn-outline" @click="handleReset">重置</button>
      </div>
    </div>

    <!-- ========== 操作栏 ========== -->
    <div class="action-bar">
      <button class="btn btn-primary" @click="openCreateDialog">+ 创建订单</button>
    </div>

    <!-- ========== 订单表格 ========== -->
    <div class="table-wrap">
      <table class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>订单号</th>
            <th>设备名称</th>
            <th>设备型号</th>
            <th>用户ID</th>
            <th>状态</th>
            <th>金额</th>
            <th>支付状态</th>
            <th>开始时间</th>
            <th>结束时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="11" class="td-center">加载中...</td>
          </tr>
          <tr v-else-if="orders.length === 0">
            <td colspan="11" class="td-center">暂无订单数据</td>
          </tr>
          <tr v-for="order in orders" :key="order.id">
            <td>{{ order.id }}</td>
            <td class="td-mono">{{ order.orderNo }}</td>
            <td class="td-name" @click="openDetailDialog(order)">{{ order.deviceName }}</td>
            <td>{{ order.deviceModel }}</td>
            <td>{{ order.userId }}</td>
            <td>
              <span class="tag" :class="statusClass(order.status)">{{ statusText(order.status) }}</span>
            </td>
            <td>¥{{ formatPrice(order.totalAmount) }}</td>
            <td>{{ payStatusText(order.payStatus) }}</td>
            <td class="td-time">{{ formatTime(order.startTime) }}</td>
            <td class="td-time">{{ formatTime(order.endTime) }}</td>
            <td class="td-actions">
              <button class="act-btn" title="查看" @click="openDetailDialog(order)">👁</button>
              <button class="act-btn" title="编辑" @click="openEditDialog(order)">✏</button>
              <button
                class="act-btn warn"
                title="取消"
                @click="openCancelDialog(order)"
              >✖</button>
              <button class="act-btn" title="完成" @click="openCompleteDialog(order)">✅</button>
              <button class="act-btn danger" title="删除" @click="handleDelete(order)">✕</button>
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

    <!-- ========== 创建/编辑/详情弹窗 ========== -->
    <teleport to="body">
      <div v-if="dialogVisible" class="dialog-overlay" @click.self="dialogVisible = false">
        <div class="dialog" :class="{ wide: dialogMode === 'detail' }">
          <div class="dialog-header">
            <h3>{{ dialogTitle }}</h3>
            <button class="dialog-close" @click="dialogVisible = false">✕</button>
          </div>
          <div class="dialog-body">
            <!-- 详情模式 -->
            <template v-if="dialogMode === 'detail'">
              <div class="detail-grid">
                <div class="detail-item"><label>ID</label><span>{{ form.id }}</span></div>
                <div class="detail-item"><label>订单号</label><span>{{ form.orderNo }}</span></div>
                <div class="detail-item"><label>设备名称</label><span>{{ form.deviceName }}</span></div>
                <div class="detail-item"><label>设备型号</label><span>{{ form.deviceModel }}</span></div>
                <div class="detail-item"><label>用户ID</label><span>{{ form.userId }}</span></div>
                <div class="detail-item"><label>状态</label><span class="tag" :class="statusClass(form.status ?? 0)">{{ statusText(form.status ?? 0) }}</span></div>
            <div class="detail-item"><label>金额</label><span>¥{{ formatPrice((form.totalAmount ?? 0) as number) }}</span></div>
                <div class="detail-item"><label>支付状态</label><span>{{ payStatusText(form.payStatus) }}</span></div>
                <div class="detail-item"><label>开始时间</label><span>{{ formatTime(form.startTime) }}</span></div>
                <div class="detail-item"><label>结束时间</label><span>{{ formatTime(form.endTime) }}</span></div>
                <div class="detail-item wide"><label>备注</label><span>{{ form.remark || '-' }}</span></div>
              </div>
            </template>
            <!-- 编辑/创建模式 -->
            <template v-else>
              <div class="form-grid">
                <div class="form-group">
                  <label>用户ID <span class="required">*</span></label>
                  <input v-model.number="form.userId" class="input" placeholder="输入用户ID" />
                </div>
                <div class="form-group">
                  <label>设备ID <span class="required">*</span></label>
                  <input v-model.number="form.deviceId" class="input" placeholder="输入设备ID" />
                </div>
                <div class="form-group">
                  <label>开始时间 <span class="required">*</span></label>
                  <input v-model="form.startTime" type="datetime-local" class="input" />
                </div>
                <div class="form-group">
                  <label>结束时间 <span class="required">*</span></label>
                  <input v-model="form.endTime" type="datetime-local" class="input" />
                </div>
                <div class="form-group wide">
                  <label>备注</label>
                  <textarea v-model="form.remark" class="textarea" placeholder="备注信息" maxlength="500" rows="2"></textarea>
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
            <button class="btn btn-primary" @click="openEditDialog(form)">编辑</button>
          </div>
        </div>
      </div>
    </teleport>

    <!-- ========== 取消/完成确认弹窗 ========== -->
    <teleport to="body">
      <div v-if="confirmVisible" class="dialog-overlay" @click.self="confirmVisible = false">
        <div class="dialog dialog-sm">
          <div class="dialog-header">
            <h3>{{ confirmTitle }}</h3>
            <button class="dialog-close" @click="confirmVisible = false">✕</button>
          </div>
          <div class="dialog-body">
            <p class="confirm-msg">{{ confirmMsg }}</p>
            <div class="form-group">
              <label>备注 <span class="required">*</span></label>
              <textarea v-model="confirmRemark" class="textarea" :placeholder="confirmPlaceholder" maxlength="500" rows="3"></textarea>
            </div>
          </div>
          <div class="dialog-footer">
            <button class="btn btn-outline" @click="confirmVisible = false">取消</button>
            <button class="btn" :class="confirmDanger ? 'btn-danger' : 'btn-primary'" @click="handleConfirm" :disabled="submitting">
              {{ submitting ? '处理中...' : '确认' }}
            </button>
          </div>
        </div>
      </div>
    </teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import type { RentalOrderVO, RentalOrderDTO, RentalOrderQueryParams } from '@/types/api'
import {
  getOrderListApi,
  createOrderApi,
  updateOrderApi,
  deleteOrderApi,
  cancelOrderApi,
  completeOrderApi,
} from '@/api/rental'

// ========== 数据 ========== 
const orders = ref<RentalOrderVO[]>([])
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const dialogMode = ref<'create'|'edit'|'detail'>('create')
const confirmVisible = ref(false)
const confirmAction = ref<'cancel'|'complete'>('cancel')
const confirmTarget = ref<RentalOrderVO | null>(null)
const confirmRemark = ref('')

const query = reactive({
  page: 1,
  size: 10,
  orderNo: undefined as string | undefined,
  userId: undefined as number | undefined,
  deviceId: undefined as number | undefined,
  status: undefined as number | undefined,
})

const stats = reactive({
  totalOrders: 0,
  pendingPayment: 0,
  inProgress: 0,
  completed: 0,
  cancelled: 0,
})

const form = reactive<RentalOrderDTO & Partial<RentalOrderVO>>({
  id: 0,
  orderNo: '',
  deviceId: 0,
  deviceName: '',
  deviceModel: '',
  userId: 0,
  status: 0,
  amount: 0,
  payStatus: 0,
  startTime: '',
  endTime: '',
  remark: '',
})

const defaultForm = (): RentalOrderDTO & Partial<RentalOrderVO> => ({
  id: 0,
  orderNo: '',
  deviceId: 0,
  deviceName: '',
  deviceModel: '',
  userId: 0,
  status: 0,
  amount: 0,
  payStatus: 0,
  startTime: '',
  endTime: '',
  remark: '',
})

const formReset = () => {
  Object.assign(form, defaultForm())
}

// ========== 计算属性 ========== 
const dialogTitle = computed(() => {
  if (dialogMode.value === 'create') return '创建订单'
  if (dialogMode.value === 'edit') return '编辑订单'
  return '订单详情'
})

const confirmTitle = computed(() => confirmAction.value === 'cancel' ? '取消订单' : '完成订单')
const confirmMsg = computed(() => {
  if (!confirmTarget.value) return ''
  const orderNo = confirmTarget.value.orderNo
  return confirmAction.value === 'cancel'
    ? `确认取消订单「${orderNo}」？`
    : `确认将订单「${orderNo}」标记为已完成？`
})
const confirmPlaceholder = computed(() => confirmAction.value === 'cancel' ? '取消原因' : '')
const confirmDanger = computed(() => confirmAction.value === 'cancel')
const isLastPage = computed(() => orders.value.length < query.size)

// ========== 辅助函数 ========== 
function statusText(status: number): string {
  const map: Record<number,string> = {0: '待支付', 1: '进行中', 2: '已完成', 3: '已取消'}
  return map[status] ?? '未知'
}
function payStatusText(pay?: number): string {
  const map: Record<number,string> = {0: '未支付', 1: '已支付', 2: '退款中', 3: '已退款'}
  const key = pay ?? -1
  return map[key] ?? '未知'
}
function statusClass(status?: number): string {
  const map: Record<number,string> = {0: 'tag-offline', 1: 'tag-online', 2: 'tag-maintain', 3: 'tag-scrapped'}
  const key = status ?? -1
  return map[key] ?? ''
}
function formatTime(t?: string): string {
  if (!t) return '-'
  return t.replace('T', ' ').substring(0, 19)
}
function formatPrice(v?: number): string {
  if (v == null) return '0.00'
  return Number(v).toFixed(2)
}

// ========== 请求/数据获取 ========== 
async function fetchOrders() {
  loading.value = true
  try {
    const params: any = { page: query.page ?? 1, size: query.size ?? 10 }
    if (query.orderNo) params.orderNo = query.orderNo
    if (query.userId != null) params.userId = query.userId
    if (query.deviceId != null) params.deviceId = query.deviceId
    if (query.status != null) params.status = (query.status ?? 0) as number
    const res = await getOrderListApi(params)
    orders.value = res.data.data || []
    computeStats()
  } catch (e: any) {
    alert(e?.message || '获取订单失败')
    orders.value = []
  } finally {
    loading.value = false
  }
}
function computeStats() {
  const arr = orders.value || []
  stats.totalOrders = arr.length
  stats.pendingPayment = arr.filter(o => o.status === 0).length
  stats.inProgress = arr.filter(o => o.status === 1).length
  stats.completed = arr.filter(o => o.status === 2).length
  stats.cancelled = arr.filter(o => o.status === 3).length
}

/** 搜索/重置/分页 */
function handleReset() {
  query.page = 1
  query.orderNo = undefined
  query.userId = undefined
  query.deviceId = undefined
  query.status = undefined
  fetchOrders()
}
function goPage(page: number | undefined) {
  query.page = page ?? 1
  fetchOrders()
}

/** 创建/编辑弹窗 */
function openCreateDialog() {
  Object.assign(form, defaultForm())
  dialogMode.value = 'create'
  dialogVisible.value = true
}
function openEditDialog(order: any) {
  Object.assign(form, { ...order })
  dialogMode.value = 'edit'
  dialogVisible.value = true
}
function openDetailDialog(order: RentalOrderVO) {
  Object.assign(form, { ...order, id: (order.id ?? 0) })
  dialogMode.value = 'detail'
  dialogVisible.value = true
}
function openCancelDialog(order: RentalOrderVO) {
  confirmAction.value = 'cancel'
  confirmTarget.value = order
  confirmRemark.value = ''
  confirmVisible.value = true
}
function openCompleteDialog(order: RentalOrderVO) {
  confirmAction.value = 'complete'
  confirmTarget.value = order
  confirmRemark.value = ''
  confirmVisible.value = true
}

/** 提交创建/编辑 */
async function handleSubmit() {
  if (!form.userId || !form.deviceId || !form.startTime || !form.endTime) {
    alert('请填写必填字段')
    return
  }
  submitting.value = true
  try {
    const dto: RentalOrderDTO = {
      userId: form.userId!,
      deviceId: form.deviceId!,
      startTime: form.startTime,
      endTime: form.endTime,
      remark: form.remark ?? '',
    }
    if (dialogMode.value === 'create') {
      await createOrderApi(dto)
    } else {
      await updateOrderApi(form.id as number, dto)
    }
    dialogVisible.value = false
    await fetchOrders()
  } catch (e: any) {
    alert(e?.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

/** 删除 */
async function handleDelete(order: RentalOrderVO) {
  if (!confirm(`确认删除订单「${order.orderNo}」？`)) return
  try {
    await deleteOrderApi(order.id!)
    await fetchOrders()
  } catch (e: any) {
    alert(e?.message || '删除失败')
  }
}

/** 执行确认操作 */
async function handleConfirm() {
  if (!confirmTarget.value) return
  if (confirmAction.value === 'cancel' && !confirmRemark.value.trim()) {
    alert('请填写取消原因')
    return
  }
  submitting.value = true
  try {
    const id = confirmTarget.value.id
    if (confirmAction.value === 'cancel') {
      await cancelOrderApi(id, confirmRemark.value)
    } else {
      await completeOrderApi(id)
    }
    confirmVisible.value = false
    await fetchOrders()
  } catch (e: any) {
    alert(e?.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.device-page {
  max-width: 1320px;
}

/* ========== 统计卡片 ========== */
.stat-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 12px;
  margin-bottom: 20px;
}
.stat-card {
  background: #fff;
  border: 1px solid #e0e0e0;
  padding: 16px 20px;
  text-align: center;
  border-top: 3px solid #000;
}
.stat-card.online { background: #fafafa; }
.stat-card.flying { background: #f5f5f5; }
.stat-card.offline { background: #fff; border-top-color: #ccc; }
.stat-card.maintenance { background: #f5f5f5; border-top-color: #999; }
.stat-card.scrapped { background: #f0f0f0; border-top-color: #ccc; }
.stat-value { font-size: 28px; font-weight: 700; color: #000; line-height: 1.2; }
.stat-label { font-size: 13px; color: #666; margin-top: 4px; }

/* ========== 搜索 ========== */
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

/* ========== 表单控件 ========== */
.input {
  padding: 8px 12px;
  border: 1px solid #ccc;
  font-size: 14px;
  color: #333;
  background: #fff;
  outline: none;
  transition: border-color 0.2s;
  flex: 1;
  min-width: 120px;
}
.input:focus { border-color: #000; }
.input.short { flex: none; width: 130px; }
.select { padding: 8px 12px; border: 1px solid #ccc; font-size: 14px; color: #333; background: #fff; outline: none; cursor: pointer; flex: 1; min-width: 120px; }
.select:focus { border-color: #000; }
.textarea { padding: 8px 12px; border: 1px solid #ccc; font-size: 14px; color: #333; background: #fff; outline: none; resize: vertical; width: 100%; box-sizing: border-box; font-family: inherit; }
.textarea:focus { border-color: #000; }

/* ========== 按钮 ========== */
.btn { padding: 8px 16px; font-size: 14px; cursor: pointer; border: none; transition: background 0.2s, color 0.2s, border-color 0.2s; white-space: nowrap; }
.btn:disabled { opacity: 0.5; cursor: not-allowed; }
.btn-primary { background: #000; color: #fff; }
.btn-primary:hover:not(:disabled) { background: #333; }
.btn-outline { background: #fff; color: #333; border: 1px solid #ccc; }
.btn-outline:hover:not(:disabled) { border-color: #000; color: #000; }
.btn-danger { background: #000; color: #fff; }
.btn-danger:hover:not(:disabled) { background: #333; }

/* ========== 操作栏 ========== */
.action-bar { display: flex; align-items: center; gap: 10px; margin-bottom: 16px; }

/* ========== 表格 ========== */
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

/* 标签 */
.tag { display: inline-block; padding: 2px 10px; font-size: 12px; font-weight: 500; background: #fff; color: #000; border: 1px solid #000; }
.tag-online { border-width: 2px; font-weight: 700; }
.tag-flying { border-width: 1px; font-weight: 500; }
.tag-offline { border-color: #ccc; color: #999; font-weight: 400; }
.tag-maintain { background: #f0f0f0; border-color: #ccc; font-weight: 400; }
.tag-scrapped { background: #f0f0f0; border-color: #ccc; color: #999; text-decoration: line-through; }

/* 电量条 (保留兼容结构，若设备页面有此元素，这里不使用) */
.battery-bar { display: inline-block; width: 60px; height: 8px; background: #e0e0e0; overflow: hidden; vertical-align: middle; margin-right: 6px; }
.battery-fill { height: 100%; transition: width 0.3s; }
.battery-high { background: #000; }
.battery-mid { background: #666; }
.battery-low { background: #ccc; }
.battery-text { font-size: 13px; color: #666; vertical-align: middle; }

/* ========== 分页 ========== */
.pagination { display: flex; align-items: center; gap: 12px; margin-top: 16px; justify-content: center; }
.page-info { font-size: 14px; color: #666; }

/* ========== 弹窗 ========== */
.dialog-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.5); z-index: 1000; display: flex; align-items: center; justify-content: center; padding: 20px; }
.dialog { background: #fff; border: 2px solid #000; width: 100%; max-width: 560px; max-height: 90vh; display: flex; flex-direction: column; }
.dialog.wide { max-width: 720px; }
.dialog-sm { max-width: 420px; }
.dialog-header { display: flex; align-items: center; justify-content: space-between; padding: 16px 24px; border-bottom: 1px solid #e0e0e0; flex-shrink: 0; }
.dialog-header h3 { margin: 0; font-size: 16px; font-weight: 600; color: #000; }
.dialog-close { background: none; border: none; font-size: 20px; cursor: pointer; color: #999; padding: 0; line-height: 1; transition: color 0.2s; }
.dialog-close:hover { color: #000; }
.dialog-body { padding: 24px; overflow-y: auto; flex: 1; }
.dialog-footer { display: flex; justify-content: flex-end; gap: 10px; padding: 16px 24px; border-top: 1px solid #e0e0e0; flex-shrink: 0; }
.confirm-msg { font-size: 14px; color: #333; margin: 0 0 16px; line-height: 1.6; }

/* 表单网格 */
.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.form-group { display: flex; flex-direction: column; gap: 6px; }
.form-group.wide { grid-column: 1 / -1; }
.form-group label { font-size: 13px; font-weight: 500; color: #333; }
.required { color: #000; font-weight: 700; }

/* 详情网格 */
.detail-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.detail-item { display: flex; flex-direction: column; gap: 4px; }
.detail-item.wide { grid-column: 1 / -1; }
.detail-item label { font-size: 12px; font-weight: 500; color: #666; text-transform: uppercase; }
.detail-item span { font-size: 14px; color: #333; }
</style>

<template>
  <div class="device-page">
    <!-- ========== 统计卡片 ========== -->
    <div class="stat-row">
      <div class="stat-card">
        <div class="stat-value">{{ stats.totalDevices }}</div>
        <div class="stat-label">设备总数</div>
      </div>
      <div class="stat-card online">
        <div class="stat-value">{{ stats.onlineDevices }}</div>
        <div class="stat-label">在线</div>
      </div>
      <div class="stat-card flying">
        <div class="stat-value">{{ stats.flyingDevices }}</div>
        <div class="stat-label">飞行中</div>
      </div>
      <div class="stat-card offline">
        <div class="stat-value">{{ offlineCount }}</div>
        <div class="stat-label">离线</div>
      </div>
      <div class="stat-card maintenance">
        <div class="stat-value">{{ stats.maintenanceDevices }}</div>
        <div class="stat-label">维修中</div>
      </div>
      <div class="stat-card scrapped">
        <div class="stat-value">{{ stats.scrappedDevices }}</div>
        <div class="stat-label">已报废</div>
      </div>
    </div>

    <!-- ========== 搜索过滤 ========== -->
    <div class="search-bar">
      <div class="search-row">
        <input v-model="query.deviceName" placeholder="设备名称" class="input" @keyup.enter="handleSearch" />
        <input v-model="query.model" placeholder="设备型号" class="input" @keyup.enter="handleSearch" />
        <input v-model="query.serialNumber" placeholder="序列号" class="input" @keyup.enter="handleSearch" />
        <select v-model="query.status" class="select">
          <option :value="undefined">全部状态</option>
          <option :value="0">离线</option>
          <option :value="1">在线</option>
          <option :value="2">飞行中</option>
          <option :value="3">维修中</option>
          <option :value="4">已报废</option>
        </select>
      </div>
      <div class="search-row">
        <input v-model.number="query.minPrice" type="number" placeholder="最低价格" class="input short" @keyup.enter="handleSearch" />
        <span class="sep">-</span>
        <input v-model.number="query.maxPrice" type="number" placeholder="最高价格" class="input short" @keyup.enter="handleSearch" />
        <input v-model.number="query.minBattery" type="number" placeholder="最低电量" class="input short" @keyup.enter="handleSearch" />
        <button class="btn btn-primary" @click="handleSearch">搜索</button>
        <button class="btn btn-outline" @click="handleReset">重置</button>
      </div>
    </div>

    <!-- ========== 操作栏 ========== -->
    <div class="action-bar">
      <button class="btn btn-primary" @click="openCreateDialog">+ 添加设备</button>
    </div>

    <!-- ========== 设备表格 ========== -->
    <div class="table-wrap">
      <table class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>设备名称</th>
            <th>型号</th>
            <th>序列号</th>
            <th>状态</th>
            <th>电量</th>
            <th>租赁价格</th>
            <th>位置</th>
            <th>所有者ID</th>
            <th>更新时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="11" class="td-center">加载中...</td>
          </tr>
          <tr v-else-if="devices.length === 0">
            <td colspan="11" class="td-center">暂无设备数据</td>
          </tr>
          <tr v-for="device in devices" :key="device.id">
            <td>{{ device.id }}</td>
            <td class="td-name" @click="openDetailDialog(device)">{{ device.deviceName }}</td>
            <td>{{ device.model }}</td>
            <td class="td-mono">{{ device.serialNumber }}</td>
            <td>
              <span class="tag" :class="statusClass(device.status)">{{ statusText(device.status) }}</span>
            </td>
            <td>
              <div class="battery-bar">
                <div class="battery-fill" :class="batteryClass(device.batteryLevel)" :style="{ width: (device.batteryLevel || 0) + '%' }"></div>
              </div>
              <span class="battery-text">{{ device.batteryLevel ?? '-' }}%</span>
            </td>
            <td>¥{{ formatPrice(device.rentalPrice) }}/h</td>
            <td class="td-loc">{{ formatCoord(device.latitude, device.longitude) }}</td>
            <td>{{ device.ownerId }}</td>
            <td class="td-time">{{ formatTime(device.updateTime) }}</td>
            <td class="td-actions">
              <button class="act-btn" title="查看" @click="openDetailDialog(device)">👁</button>
              <button class="act-btn" title="编辑" @click="openEditDialog(device)">✏</button>
              <button
                v-if="device.status !== 3"
                class="act-btn warn"
                title="维护"
                @click="openMaintainDialog(device)"
              >🔧</button>
              <button
                v-if="device.status !== 4"
                class="act-btn danger"
                title="报废"
                @click="openScrapDialog(device)"
              >🗑</button>
              <button class="act-btn danger" title="删除" @click="handleDelete(device)">✕</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- ========== 分页 ========== -->
    <div class="pagination" v-if="!loading">
      <button class="btn btn-outline" :disabled="query.page === 1" @click="goPage(query.page! - 1)">上一页</button>
      <span class="page-info">第 {{ query.page }} 页</span>
      <button class="btn btn-outline" :disabled="isLastPage" @click="goPage(query.page! + 1)">下一页</button>
    </div>

    <!-- ========== 创建/编辑弹窗 ========== -->
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
                <div class="detail-item"><label>设备名称</label><span>{{ form.deviceName }}</span></div>
                <div class="detail-item"><label>型号</label><span>{{ form.model }}</span></div>
                <div class="detail-item"><label>序列号</label><span>{{ form.serialNumber }}</span></div>
                <div class="detail-item"><label>状态</label><span class="tag" :class="statusClass(form.status)">{{ statusText(form.status) }}</span></div>
                <div class="detail-item"><label>电量</label><span>{{ form.batteryLevel ?? '-' }}%</span></div>
                <div class="detail-item"><label>租赁价格</label><span>¥{{ formatPrice(form.rentalPrice) }}/h</span></div>
                <div class="detail-item"><label>保险费用</label><span>¥{{ formatPrice(form.insuranceFee) }}/次</span></div>
                <div class="detail-item"><label>纬度</label><span>{{ form.latitude ?? '-' }}</span></div>
                <div class="detail-item"><label>经度</label><span>{{ form.longitude ?? '-' }}</span></div>
                <div class="detail-item"><label>高度</label><span>{{ form.altitude != null ? form.altitude + 'm' : '-' }}</span></div>
                <div class="detail-item"><label>飞行速度</label><span>{{ form.speed != null ? form.speed + 'm/s' : '-' }}</span></div>
                <div class="detail-item"><label>飞行总时长</label><span>{{ form.totalFlightHours != null ? form.totalFlightHours + 'h' : '-' }}</span></div>
                <div class="detail-item"><label>所有者ID</label><span>{{ form.ownerId }}</span></div>
                <div class="detail-item"><label>最后上线</label><span>{{ formatTime(form.lastOnlineTime) }}</span></div>
                <div class="detail-item"><label>最后维护</label><span>{{ formatTime(form.lastMaintenanceTime) }}</span></div>
                <div class="detail-item"><label>创建时间</label><span>{{ formatTime(form.createTime) }}</span></div>
                <div class="detail-item"><label>更新时间</label><span>{{ formatTime(form.updateTime) }}</span></div>
                <div class="detail-item wide"><label>描述</label><span>{{ form.description || '-' }}</span></div>
                <div class="detail-item wide"><label>规格</label><span>{{ form.specifications || '-' }}</span></div>
                <div class="detail-item wide"><label>备注</label><span>{{ form.remark || '-' }}</span></div>
              </div>
            </template>

            <!-- 编辑/创建模式 -->
            <template v-else>
              <div class="form-grid">
                <div class="form-group">
                  <label>设备名称 <span class="required">*</span></label>
                  <input v-model="form.deviceName" class="input" placeholder="请输入设备名称" maxlength="100" />
                </div>
                <div class="form-group">
                  <label>设备型号 <span class="required">*</span></label>
                  <input v-model="form.model" class="input" placeholder="请输入设备型号" maxlength="50" />
                </div>
                <div class="form-group">
                  <label>序列号 <span class="required">*</span></label>
                  <input v-model="form.serialNumber" class="input" placeholder="请输入序列号" maxlength="100" />
                </div>
                <div class="form-group">
                  <label>状态 <span class="required">*</span></label>
                  <select v-model="form.status" class="select">
                    <option :value="0">离线</option>
                    <option :value="1">在线</option>
                    <option :value="2">飞行中</option>
                    <option :value="3">维修中</option>
                    <option :value="4">已报废</option>
                  </select>
                </div>
                <div class="form-group">
                  <label>租赁价格 (元/小时) <span class="required">*</span></label>
                  <input v-model.number="form.rentalPrice" type="number" class="input" step="0.01" min="0" />
                </div>
                <div class="form-group">
                  <label>保险费用 (元/次) <span class="required">*</span></label>
                  <input v-model.number="form.insuranceFee" type="number" class="input" step="0.01" min="0" />
                </div>
                <div class="form-group">
                  <label>所有者ID <span class="required">*</span></label>
                  <input v-model.number="form.ownerId" type="number" class="input" min="1" />
                </div>
                <div class="form-group">
                  <label>电量 (%)</label>
                  <input v-model.number="form.batteryLevel" type="number" class="input" min="0" max="100" />
                </div>
                <div class="form-group">
                  <label>纬度</label>
                  <input v-model.number="form.latitude" type="number" class="input" step="any" min="-90" max="90" />
                </div>
                <div class="form-group">
                  <label>经度</label>
                  <input v-model.number="form.longitude" type="number" class="input" step="any" min="-180" max="180" />
                </div>
                <div class="form-group">
                  <label>高度 (m)</label>
                  <input v-model.number="form.altitude" type="number" class="input" step="any" min="0" />
                </div>
                <div class="form-group">
                  <label>飞行速度 (m/s)</label>
                  <input v-model.number="form.speed" type="number" class="input" step="0.01" min="0" />
                </div>
                <div class="form-group">
                  <label>飞行总时长 (h)</label>
                  <input v-model.number="form.totalFlightHours" type="number" class="input" step="0.01" min="0" />
                </div>
                <div class="form-group wide">
                  <label>描述</label>
                  <textarea v-model="form.description" class="textarea" placeholder="设备描述" maxlength="1000" rows="2"></textarea>
                </div>
                <div class="form-group wide">
                  <label>规格 (JSON)</label>
                  <textarea v-model="form.specifications" class="textarea" placeholder='{"camera":"4K","range":"10km"}' maxlength="2000" rows="2"></textarea>
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

    <!-- ========== 维护/报废确认弹窗 ========== -->
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
import type { DeviceVO, DeviceDTO } from '@/types/api'
import {
  getDeviceListApi,
  createDeviceApi,
  updateDeviceApi,
  deleteDeviceApi,
  maintainDeviceApi,
  scrapDeviceApi,
  getDeviceStatisticsApi,
} from '@/api/device'

// ========== 状态 ==========
const devices = ref<DeviceVO[]>([])
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const dialogMode = ref<'create' | 'edit' | 'detail'>('create')
const confirmVisible = ref(false)
const confirmAction = ref<'maintain' | 'scrap'>('maintain')
const confirmTarget = ref<DeviceVO | null>(null)
const confirmRemark = ref('')

const query = reactive({
  page: 1,
  size: 10,
  deviceName: undefined as string | undefined,
  model: undefined as string | undefined,
  serialNumber: undefined as string | undefined,
  status: undefined as number | undefined,
  minPrice: undefined as number | undefined,
  maxPrice: undefined as number | undefined,
  minBattery: undefined as number | undefined,
})

const stats = reactive({
  totalDevices: 0,
  onlineDevices: 0,
  flyingDevices: 0,
  maintenanceDevices: 0,
  scrappedDevices: 0,
})

/** 离线数 = 总数 - 其他状态 */
const offlineCount = computed(() => {
  const rest = stats.totalDevices - stats.onlineDevices - stats.flyingDevices - stats.maintenanceDevices - stats.scrappedDevices
  return Math.max(0, rest)
})

const defaultForm = (): DeviceDTO & Partial<DeviceVO> => ({
  id: undefined,
  deviceName: '',
  model: '',
  serialNumber: '',
  status: 0,
  latitude: undefined,
  longitude: undefined,
  altitude: undefined,
  batteryLevel: undefined,
  speed: undefined,
  totalFlightHours: undefined,
  ownerId: 1,
  rentalPrice: 0,
  insuranceFee: 0,
  description: '',
  specifications: '',
  images: '',
  remark: '',
})

const form = reactive<DeviceDTO & Partial<DeviceVO>>(defaultForm())

// ========== 计算属性 ==========
const dialogTitle = computed(() => {
  if (dialogMode.value === 'create') return '添加设备'
  if (dialogMode.value === 'edit') return '编辑设备'
  return '设备详情'
})

const confirmTitle = computed(() => confirmAction.value === 'maintain' ? '设备维护' : '设备报废')
const confirmMsg = computed(() => {
  if (!confirmTarget.value) return ''
  const name = confirmTarget.value.deviceName
  return confirmAction.value === 'maintain'
    ? `确认将设备「${name}」标记为维修状态？`
    : `确认将设备「${name}」报废？此操作不可撤消。`
})
const confirmPlaceholder = computed(() => confirmAction.value === 'maintain' ? '维护原因' : '报废原因')
const confirmDanger = computed(() => confirmAction.value === 'scrap')
const isLastPage = computed(() => devices.value.length < query.size)

// ========== 方法 ==========

/** 状态文本 */
function statusText(status: number): string {
  const map: Record<number, string> = { 0: '离线', 1: '在线', 2: '飞行中', 3: '维修中', 4: '已报废' }
  return map[status] ?? '未知'
}

/** 状态样式类 */
function statusClass(status: number): string {
  const map: Record<number, string> = { 0: 'tag-offline', 1: 'tag-online', 2: 'tag-flying', 3: 'tag-maintain', 4: 'tag-scrapped' }
  return map[status] ?? ''
}

/** 电量颜色类 */
function batteryClass(level?: number): string {
  if (level == null) return 'battery-low'
  if (level >= 60) return 'battery-high'
  if (level >= 30) return 'battery-mid'
  return 'battery-low'
}

/** 格式化价格 */
function formatPrice(v?: number): string {
  if (v == null) return '-'
  return v.toFixed(2)
}

/** 格式化坐标 */
function formatCoord(lat?: number, lng?: number): string {
  if (lat == null || lng == null) return '-'
  return `${lat.toFixed(4)}, ${lng.toFixed(4)}`
}

/** 格式化时间 */
function formatTime(t?: string): string {
  if (!t) return '-'
  return t.replace('T', ' ').substring(0, 19)
}

/** 查询设备列表 */
async function fetchDevices() {
  loading.value = true
  try {
    const params: any = { page: query.page, size: query.size }
    if (query.deviceName) params.deviceName = query.deviceName
    if (query.model) params.model = query.model
    if (query.serialNumber) params.serialNumber = query.serialNumber
    if (query.status !== undefined && query.status !== null) params.status = query.status
    if (query.minPrice != null) params.minPrice = query.minPrice
    if (query.maxPrice != null) params.maxPrice = query.maxPrice
    if (query.minBattery != null) params.minBattery = query.minBattery
    const res = await getDeviceListApi(params)
    devices.value = res.data.data || []
  } catch (e) {
    console.error('获取设备列表失败', e)
    devices.value = []
  } finally {
    loading.value = false
  }
}

/** 查询统计 */
async function fetchStats() {
  try {
    const res = await getDeviceStatisticsApi()
    const data = res.data.data
    if (data) {
      stats.totalDevices = data.totalDevices
      stats.onlineDevices = data.onlineDevices
      stats.flyingDevices = data.flyingDevices
      stats.maintenanceDevices = data.maintenanceDevices
      stats.scrappedDevices = data.scrappedDevices
    }
  } catch {
    // 统计加载失败不阻塞列表
  }
}

/** 搜索 */
function handleSearch() {
  query.page = 1
  fetchDevices()
}

/** 重置 */
function handleReset() {
  query.page = 1
  query.deviceName = undefined
  query.model = undefined
  query.serialNumber = undefined
  query.status = undefined
  query.minPrice = undefined
  query.maxPrice = undefined
  query.minBattery = undefined
  fetchDevices()
}

/** 翻页 */
function goPage(page: number) {
  query.page = page
  fetchDevices()
}

/** 创建 */
function openCreateDialog() {
  Object.assign(form, defaultForm())
  dialogMode.value = 'create'
  dialogVisible.value = true
}

/** 编辑 */
function openEditDialog(device: DeviceVO) {
  Object.assign(form, { ...device })
  dialogMode.value = 'edit'
  dialogVisible.value = true
}

/** 详情 */
function openDetailDialog(device: DeviceVO) {
  Object.assign(form, { ...device })
  dialogMode.value = 'detail'
  dialogVisible.value = true
}

/** 提交创建/编辑 */
async function handleSubmit() {
  if (!form.deviceName || !form.model || !form.serialNumber || form.ownerId == null) {
    alert('请填写必填字段')
    return
  }
  submitting.value = true
  try {
    const dto: DeviceDTO = {
      deviceName: form.deviceName,
      model: form.model,
      serialNumber: form.serialNumber,
      status: form.status ?? 0,
      latitude: form.latitude,
      longitude: form.longitude,
      altitude: form.altitude,
      batteryLevel: form.batteryLevel,
      speed: form.speed,
      totalFlightHours: form.totalFlightHours,
      ownerId: form.ownerId!,
      rentalPrice: form.rentalPrice ?? 0,
      insuranceFee: form.insuranceFee ?? 0,
      description: form.description,
      images: form.images,
      specifications: form.specifications,
      remark: form.remark,
    }
    if (dialogMode.value === 'create') {
      await createDeviceApi(dto)
    } else {
      await updateDeviceApi(form.id!, dto)
    }
    dialogVisible.value = false
    fetchDevices()
    fetchStats()
  } catch (e: any) {
    alert(e.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

/** 删除 */
async function handleDelete(device: DeviceVO) {
  if (!confirm(`确认删除设备「${device.deviceName}」？`)) return
  try {
    await deleteDeviceApi(device.id)
    fetchDevices()
    fetchStats()
  } catch (e: any) {
    alert(e.message || '删除失败')
  }
}

/** 维护弹窗 */
function openMaintainDialog(device: DeviceVO) {
  confirmAction.value = 'maintain'
  confirmTarget.value = device
  confirmRemark.value = ''
  confirmVisible.value = true
}

/** 报废弹窗 */
function openScrapDialog(device: DeviceVO) {
  confirmAction.value = 'scrap'
  confirmTarget.value = device
  confirmRemark.value = ''
  confirmVisible.value = true
}

/** 执行维护/报废 */
async function handleConfirm() {
  if (!confirmRemark.value.trim()) {
    alert('请填写备注')
    return
  }
  if (!confirmTarget.value) return
  submitting.value = true
  try {
    const id = confirmTarget.value.id
    if (confirmAction.value === 'maintain') {
      await maintainDeviceApi(id, confirmRemark.value)
    } else {
      await scrapDeviceApi(id, confirmRemark.value)
    }
    confirmVisible.value = false
    fetchDevices()
    fetchStats()
  } catch (e: any) {
    alert(e.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchDevices()
  fetchStats()
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
.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #000;
  line-height: 1.2;
}
.stat-label {
  font-size: 13px;
  color: #666;
  margin-top: 4px;
}

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
.sep {
  color: #999;
  flex-shrink: 0;
}

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
.input:focus {
  border-color: #000;
}
.input.short {
  flex: none;
  width: 130px;
}
.select {
  padding: 8px 12px;
  border: 1px solid #ccc;
  font-size: 14px;
  color: #333;
  background: #fff;
  outline: none;
  cursor: pointer;
  flex: 1;
  min-width: 120px;
}
.select:focus {
  border-color: #000;
}
.textarea {
  padding: 8px 12px;
  border: 1px solid #ccc;
  font-size: 14px;
  color: #333;
  background: #fff;
  outline: none;
  resize: vertical;
  width: 100%;
  box-sizing: border-box;
  font-family: inherit;
}
.textarea:focus {
  border-color: #000;
}

/* ========== 按钮 ========== */
.btn {
  padding: 8px 16px;
  font-size: 14px;
  cursor: pointer;
  border: none;
  transition: background 0.2s, color 0.2s, border-color 0.2s;
  white-space: nowrap;
}
.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.btn-primary {
  background: #000;
  color: #fff;
}
.btn-primary:hover:not(:disabled) {
  background: #333;
}
.btn-outline {
  background: #fff;
  color: #333;
  border: 1px solid #ccc;
}
.btn-outline:hover:not(:disabled) {
  border-color: #000;
  color: #000;
}
.btn-danger {
  background: #000;
  color: #fff;
}
.btn-danger:hover:not(:disabled) {
  background: #333;
}

/* ========== 操作栏 ========== */
.action-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

/* ========== 表格 ========== */
.table-wrap {
  background: #fff;
  border: 1px solid #e0e0e0;
  overflow-x: auto;
}
.data-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
  min-width: 1100px;
}
.data-table thead {
  background: #fafafa;
}
.data-table th {
  padding: 12px 14px;
  text-align: left;
  font-weight: 600;
  color: #333;
  border-bottom: 2px solid #000;
  white-space: nowrap;
}
.data-table td {
  padding: 12px 14px;
  border-bottom: 1px solid #e0e0e0;
  color: #333;
  white-space: nowrap;
}
.data-table tbody tr:hover {
  background: #fafafa;
}
.td-center {
  text-align: center;
  color: #999;
  padding: 40px 14px;
}
.td-name {
  color: #000;
  cursor: pointer;
  font-weight: 600;
}
.td-name:hover {
  text-decoration: underline;
}
.td-mono {
  font-family: monospace;
  font-size: 13px;
}
.td-time {
  font-size: 13px;
  color: #666;
}
.td-loc {
  font-family: monospace;
  font-size: 13px;
}
.td-actions {
  display: flex;
  gap: 4px;
}

.act-btn {
  background: none;
  border: 1px solid #ccc;
  padding: 4px 8px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.2s, border-color 0.2s;
}
.act-btn:hover {
  background: #f0f0f0;
  border-color: #000;
}

/* 标签 */
.tag {
  display: inline-block;
  padding: 2px 10px;
  font-size: 12px;
  font-weight: 500;
  background: #fff;
  color: #000;
  border: 1px solid #000;
}
.tag-online {
  border-width: 2px;
  font-weight: 700;
}
.tag-flying {
  border-width: 1px;
  font-weight: 500;
}
.tag-offline {
  border-color: #ccc;
  color: #999;
  font-weight: 400;
}
.tag-maintain {
  background: #f0f0f0;
  border-color: #ccc;
  font-weight: 400;
}
.tag-scrapped {
  background: #f0f0f0;
  border-color: #ccc;
  color: #999;
  text-decoration: line-through;
}

/* 电量条 */
.battery-bar {
  display: inline-block;
  width: 60px;
  height: 8px;
  background: #e0e0e0;
  overflow: hidden;
  vertical-align: middle;
  margin-right: 6px;
}
.battery-fill {
  height: 100%;
  transition: width 0.3s;
}
.battery-high { background: #000; }
.battery-mid { background: #666; }
.battery-low { background: #ccc; }
.battery-text {
  font-size: 13px;
  color: #666;
  vertical-align: middle;
}

/* ========== 分页 ========== */
.pagination {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 16px;
  justify-content: center;
}
.page-info {
  font-size: 14px;
  color: #666;
}

/* ========== 弹窗 ========== */
.dialog-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}
.dialog {
  background: #fff;
  border: 2px solid #000;
  width: 100%;
  max-width: 560px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
}
.dialog.wide {
  max-width: 720px;
}
.dialog-sm {
  max-width: 420px;
}
.dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  border-bottom: 1px solid #e0e0e0;
  flex-shrink: 0;
}
.dialog-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #000;
}
.dialog-close {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #999;
  padding: 0;
  line-height: 1;
  transition: color 0.2s;
}
.dialog-close:hover {
  color: #000;
}
.dialog-body {
  padding: 24px;
  overflow-y: auto;
  flex: 1;
}
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 16px 24px;
  border-top: 1px solid #e0e0e0;
  flex-shrink: 0;
}
.confirm-msg {
  font-size: 14px;
  color: #333;
  margin: 0 0 16px;
  line-height: 1.6;
}

/* 表单网格 */
.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.form-group.wide {
  grid-column: 1 / -1;
}
.form-group label {
  font-size: 13px;
  font-weight: 500;
  color: #333;
}
.required {
  color: #000;
  font-weight: 700;
}

/* 详情网格 */
.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
.detail-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.detail-item.wide {
  grid-column: 1 / -1;
}
.detail-item label {
  font-size: 12px;
  font-weight: 500;
  color: #666;
  text-transform: uppercase;
}
.detail-item span {
  font-size: 14px;
  color: #333;
}
</style>

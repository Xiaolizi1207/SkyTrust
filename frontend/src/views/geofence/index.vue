<template>
  <section class="geofence-page page-container">
    <!-- Header -->
    <div class="card page-header">
      <div class="header-left">
        <h2>地理围栏配置</h2>
        <span class="subtitle">管理禁飞区与实时位置检测</span>
      </div>
      <div class="header-right">
        <button class="btn primary" @click="openAddModal">+ 添加围栏</button>
        <button class="btn secondary" @click="uploadGeoJSON">批量导入 GeoJSON</button>
      </div>
    </div>

    <!-- Loading / Error / Empty States -->
    <div v-if="loading" class="state">正在加载围栏数据…</div>
    <div v-if="errorMsg" class="state error">{{ errorMsg }}</div>

    <!-- Zone List -->
    <div v-if="zones.length > 0" class="zone-grid">
      <div v-for="zone in zones" :key="zone.id" class="card zone-card" :class="{ inactive: !zone.active }">
        <div class="zone-card-header">
          <span class="zone-name">{{ zone.name }}</span>
          <div class="zone-actions">
            <label class="toggle-switch">
              <input type="checkbox" :checked="zone.active" @change="toggleZone(zone)" />
              <span class="toggle-track"></span>
            </label>
            <button class="btn-icon" @click="openEditModal(zone)" title="编辑">[ 编辑 ]</button>
            <button class="btn-icon" @click="confirmDelete(zone)" title="删除">[ 删除 ]</button>
          </div>
        </div>
        <div class="zone-body">
          <span class="tag" :class="zone.type === 'Polygon' ? 'tag-poly' : 'tag-circle'">{{ zone.type === 'Polygon' ? '多边形' : '圆形' }}</span>
          <div class="coord-preview">
            <span v-if="zone.type === 'Circle'">
              中心: ({{ zone.coordinates?.[0]?.[0]?.toFixed(4) }}, {{ zone.coordinates?.[0]?.[1]?.toFixed(4) }})
              半径: {{ zone.radius }}m
            </span>
            <span v-else>
              顶点: {{ Array.isArray(zone.coordinates?.[0]) ? zone.coordinates[0].length : 0 }} 个
            </span>
          </div>
        </div>
      </div>
    </div>
    <div v-else-if="!loading" class="empty-state">暂无围栏数据，点击「添加围栏」创建第一个。</div>

    <!-- Position Check Panel -->
    <div class="card position-check">
      <h3 class="panel-title">位置实时检测</h3>
      <div class="check-form">
        <div class="form-row">
          <div class="form-group">
            <label>纬度 (Lat)</label>
            <input v-model.number="checkLat" type="number" step="0.000001" class="input" placeholder="31.2304" />
          </div>
          <div class="form-group">
            <label>经度 (Lon)</label>
            <input v-model.number="checkLon" type="number" step="0.000001" class="input" placeholder="121.4737" />
          </div>
          <div class="form-group">
            <label>速度 (m/s)</label>
            <input v-model.number="checkSpeed" type="number" step="0.1" class="input" placeholder="5.0" />
          </div>
          <div class="form-group">
            <label>航向 (°)</label>
            <input v-model.number="checkHeading" type="number" step="0.1" class="input" placeholder="90" />
          </div>
          <button class="btn primary" @click="doCheckPosition" :disabled="checking" style="align-self: flex-end;">
            {{ checking ? '检测中…' : '检测位置' }}
          </button>
        </div>
      </div>
      <!-- Check Result -->
      <div v-if="checkResult" class="check-result">
        <div class="result-row">
          <span class="result-label">当前状态</span>
          <span class="status-tag" :class="'status-' + checkResult.status">{{ statusLabel(checkResult.status) }}</span>
        </div>
        <div class="result-row">
          <span class="result-label">当前位置</span>
          <span>({{ checkResult.currentPos.lat.toFixed(6) }}, {{ checkResult.currentPos.lon.toFixed(6) }})</span>
        </div>
        <div class="result-row">
          <span class="result-label">预测位置 (30s)</span>
          <span>({{ checkResult.predictedPos.lat.toFixed(6) }}, {{ checkResult.predictedPos.lon.toFixed(6) }})</span>
        </div>
        <div class="result-row">
          <span class="result-label">最近围栏距离</span>
          <span>{{ checkResult.distanceToZone.toFixed(1) }} m</span>
        </div>
      </div>
    </div>

    <!-- Add / Edit Modal -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal">
        <h3>{{ editingZone ? '编辑围栏' : '添加围栏' }}</h3>
        <div class="form-group">
          <label>围栏名称</label>
          <input v-model="formData.name" class="input" placeholder="例如：机场禁飞区" />
        </div>
        <div class="form-group">
          <label>类型</label>
          <select v-model="formData.type" class="input">
            <option value="Polygon">多边形 Polygon</option>
            <option value="Circle">圆形 Circle</option>
          </select>
        </div>
        <div class="form-group" v-if="formData.type === 'Polygon'">
          <label>坐标点 (每行一对 lat,lon)</label>
          <textarea v-model="coordText" class="input" rows="5" placeholder="31.23,121.47&##10;31.24,121.48&##10;31.25,121.47"></textarea>
        </div>
        <div class="form-group" v-if="formData.type === 'Circle'">
          <label>中心纬度</label>
          <input v-model.number="circleCenterLat" type="number" step="0.000001" class="input" />
          <label>中心经度</label>
          <input v-model.number="circleCenterLon" type="number" step="0.000001" class="input" />
          <label>半径 (米)</label>
          <input v-model.number="formData.radius" type="number" step="1" class="input" placeholder="500" />
        </div>
        <div class="form-group">
          <label class="toggle-label">
            <input type="checkbox" v-model="formData.active" /> 启用
          </label>
        </div>
        <div class="modal-actions">
          <button class="btn secondary" @click="closeModal">取消</button>
          <button class="btn primary" @click="saveZone" :disabled="saving">{{ saving ? '保存中…' : '保存' }}</button>
        </div>
      </div>
    </div>

    <!-- Delete Confirmation Modal -->
    <div v-if="showDeleteModal" class="modal-overlay" @click.self="showDeleteModal = false">
      <div class="modal">
        <h3>确认删除</h3>
        <p>确定要删除围栏「{{ deleteTarget?.name }}」吗？此操作不可撤销。</p>
        <div class="modal-actions">
          <button class="btn secondary" @click="showDeleteModal = false">取消</button>
          <button class="btn danger" @click="doDelete" :disabled="deleting">{{ deleting ? '删除中…' : '确认删除' }}</button>
        </div>
      </div>
    </div>

    <!-- Hidden file input for GeoJSON upload -->
    <input ref="geoJSONInput" type="file" accept=".json,.geojson" style="display:none" @change="onGeoJSONSelected" />
  </section>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import type { NoFlyZone, GeofenceCheckResult } from '@/types/api'
import { getNoFlyZones, updateNoFlyZone, checkPosition, uploadZoneGeoJSON } from '@/api/geofence'

const zones = ref<NoFlyZone[]>([])
const loading = ref(false)
const errorMsg = ref('')
const showModal = ref(false)
const showDeleteModal = ref(false)
const saving = ref(false)
const deleting = ref(false)
const editingZone = ref<NoFlyZone | null>(null)
const deleteTarget = ref<NoFlyZone | null>(null)
const coordText = ref('')
const circleCenterLat = ref(0)
const circleCenterLon = ref(0)
const geoJSONInput = ref<HTMLInputElement>()

const formData = reactive<Partial<NoFlyZone>>({ name: '', type: 'Polygon', coordinates: [], radius: undefined, active: true })

const checkLat = ref(31.23)
const checkLon = ref(121.47)
const checkSpeed = ref(5.0)
const checkHeading = ref(90)
const checking = ref(false)
const checkResult = ref<GeofenceCheckResult | null>(null)

async function fetchZones() {
  loading.value = true; errorMsg.value = ''
  try { const res = await getNoFlyZones(); zones.value = res.data ?? [] }
  catch (e: any) { errorMsg.value = e?.message ?? '加载围栏数据失败' }
  finally { loading.value = false }
}

function openAddModal() {
  editingZone.value = null; formData.name = ''; formData.type = 'Polygon'; formData.coordinates = []; formData.radius = undefined; formData.active = true
  coordText.value = ''; circleCenterLat.value = 0; circleCenterLon.value = 0; showModal.value = true
}

function openEditModal(zone: NoFlyZone) {
  editingZone.value = zone; formData.name = zone.name; formData.type = zone.type; formData.active = zone.active; formData.radius = zone.radius
  if (zone.type === 'Polygon' && Array.isArray(zone.coordinates?.[0])) coordText.value = zone.coordinates[0].map((c: number[]) => `${c[0]},${c[1]}`).join('\n')
  if (zone.type === 'Circle') { circleCenterLat.value = zone.coordinates?.[0]?.[0]?.[0] ?? 0; circleCenterLon.value = zone.coordinates?.[0]?.[0]?.[1] ?? 0 }
  showModal.value = true
}

function closeModal() { showModal.value = false; editingZone.value = null }

async function saveZone() {
  if (!formData.name?.trim()) return; saving.value = true
  try {
    let coords: number[][]
    if (formData.type === 'Polygon') coords = coordText.value.split('\n').filter(l => l.trim()).map(line => { const [lat, lon] = line.split(',').map(Number); return [lat, lon] })
    else coords = [[circleCenterLat.value, circleCenterLon.value]]
    const payload: Partial<NoFlyZone> = { name: formData.name, type: formData.type, coordinates: [coords], radius: formData.type === 'Circle' ? (formData.radius ?? 500) : undefined, active: formData.active }
    if (editingZone.value) await updateNoFlyZone(editingZone.value.id!, payload)
    else await updateNoFlyZone(`zone-${Date.now()}`, payload)
    closeModal(); await fetchZones()
  } catch (e: any) { errorMsg.value = e?.message ?? '保存失败' }
  finally { saving.value = false }
}

function confirmDelete(zone: NoFlyZone) { deleteTarget.value = zone; showDeleteModal.value = true }

async function doDelete() {
  if (!deleteTarget.value) return; deleting.value = true
  try { await updateNoFlyZone(deleteTarget.value.id!, { active: false } as any); showDeleteModal.value = false; await fetchZones() }
  catch (e: any) { errorMsg.value = e?.message ?? '删除失败' }
  finally { deleting.value = false }
}

async function toggleZone(zone: NoFlyZone) {
  try { await updateNoFlyZone(zone.id!, { active: !zone.active } as any); await fetchZones() }
  catch (e: any) { errorMsg.value = e?.message ?? '切换状态失败' }
}

async function doCheckPosition() {
  checking.value = true; checkResult.value = null
  try { const res = await checkPosition(checkLat.value, checkLon.value, checkSpeed.value, checkHeading.value); checkResult.value = res.data ?? null }
  catch (e: any) { errorMsg.value = e?.message ?? '位置检测失败' }
  finally { checking.value = false }
}

function statusLabel(status: string) { const m: Record<string, string> = { SAFE: '安全', WARNING: '警告', VIOLATION_IMMINENT: '即将闯入', VIOLATED: '已闯入' }; return m[status] ?? status }

function uploadGeoJSON() { geoJSONInput.value?.click() }

async function onGeoJSONSelected(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]; if (!file) return
  try { const text = await file.text(); const json = JSON.parse(text); await uploadZoneGeoJSON(json); await fetchZones() }
  catch (err: any) { errorMsg.value = 'GeoJSON 上传失败: ' + (err?.message ?? '') }
}

onMounted(fetchZones)
</script>

<style scoped>
.geofence-page { padding: 16px 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.header-left h2 { margin: 0 0 4px 0; font-size: 20px; font-weight:700; color:#000; }
.subtitle { color: #666; font-size: 13px; }
.header-right { display: flex; gap: 10px; }

.zone-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 16px; margin-bottom: 24px; }
.zone-card { padding: 16px; }
.zone-card.inactive { opacity: 0.4; }
.zone-card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.zone-name { font-weight: 600; font-size: 16px; color:#000; }
.zone-actions { display: flex; gap: 8px; align-items: center; }
.zone-body { display: flex; gap: 8px; align-items: center; font-size: 13px; color: #666; }
.coord-preview { font-family: monospace; font-size: 12px; }

/* Toggle switch — rectangular */
.toggle-switch { position: relative; display: inline-block; width: 36px; height: 20px; }
.toggle-switch input { opacity: 0; width: 0; height: 0; }
.toggle-track { position: absolute; cursor: pointer; inset: 0; background: #ccc; border: 1px solid #999; transition: 0.1s; }
.toggle-track::before { content: ''; position: absolute; height: 14px; width: 14px; left: 2px; top: 2px; background: #fff; border: 1px solid #666; transition: 0.1s; }
.toggle-switch input:checked + .toggle-track { background: #000; border-color: #000; }
.toggle-switch input:checked + .toggle-track::before { transform: translateX(16px); border-color: #fff; }

.position-check { padding: 20px; margin-bottom: 24px; }
.check-form .form-row { display: flex; flex-wrap: wrap; gap: 12px; align-items: flex-end; }
.form-group { display: flex; flex-direction: column; gap: 4px; }
.form-group label { font-size: 12px; color: #666; }
.form-group .input { width: 120px; }
.check-result { margin-top: 16px; padding: 16px; background: #f5f5f5; }
.result-row { display: flex; justify-content: space-between; padding: 6px 0; font-size: 14px; border-bottom: 1px solid #e0e0e0; }
.result-row:last-child { border-bottom: none; }
.result-label { font-weight: 500; color: #666; }

/* Monochrome status tags — border + bg shade only */
.tag { display: inline-block; padding: 2px 10px; font-size: 12px; font-weight: 600; border: 1px solid #000; background: #fff; color: #000; }
.tag-poly { border-width: 2px; border-style: solid; }
.tag-circle { border-width: 2px; border-style: dashed; }

.status-tag { display: inline-block; padding: 2px 12px; font-size: 12px; font-weight: 600; border: 2px solid #000; }
.status-SAFE { background: #f5f5f5; color: #000; }
.status-WARNING { background: #fff; color: #000; border-style: dashed; }
.status-VIOLATION_IMMINENT { background: #e0e0e0; color: #000; }
.status-VIOLATED { background: #000; color: #fff; }

.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; z-index: 100; }
.modal { background: #fff; border: 1px solid #000; padding: 24px; width: 480px; max-width: 90vw; max-height: 80vh; overflow-y: auto; }
.modal h3 { margin: 0 0 16px 0; font-size: 16px; font-weight: 700; color: #000; }
.modal .form-group { margin-bottom: 12px; width: 100%; }
.modal .form-group .input { width: 100%; }
.modal-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 16px; }

.toggle-label { display: flex; align-items: center; gap: 8px; cursor: pointer; font-size: 14px; }

.btn-icon { background: none; border: 1px solid #ccc; padding: 2px 8px; font-size: 12px; cursor: pointer; color: #666; font-weight: 600; }
.btn-icon:hover { border-color: #000; color: #000; }

.btn.danger { background: #000; color: #fff; border: 2px solid #000; }
.btn.danger:hover { background: #fff; color: #000; }
</style>

<template>
  <div class="device-page">
    <!-- ========== 统计卡片 (保持与 device 页同样的结构与样式) ========== -->
    <div class="stat-row">
      <div class="stat-card">
        <div class="stat-value">{{ stats.totalRoles }}</div>
        <div class="stat-label">角色总数</div>
      </div>
      <div class="stat-card online">
        <div class="stat-value">{{ stats.enabledRoles }}</div>
        <div class="stat-label">启用</div>
      </div>
      <div class="stat-card offline">
        <div class="stat-value">{{ stats.disabledRoles }}</div>
        <div class="stat-label">禁用</div>
      </div>
    </div>

    <!-- ========== 搜索过滤 ========== -->
    <div class="search-bar">
      <div class="search-row">
        <input v-model="query.roleName" placeholder="角色名称" class="input" @keyup.enter="handleSearch" />
        <input v-model="query.roleCode" placeholder="角色代码" class="input" @keyup.enter="handleSearch" />
        <button class="btn btn-primary" @click="handleSearch">搜索</button>
        <button class="btn btn-outline" @click="handleReset">重置</button>
      </div>
    </div>

    <!-- ========== 操作栏 ========== -->
    <div class="action-bar">
      <button class="btn btn-primary" @click="openCreateDialog">+ 添加角色</button>
    </div>

    <!-- ========== 角色表格 ========== -->
    <div class="table-wrap">
      <table class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>角色名称</th>
            <th>角色代码</th>
            <th>描述</th>
            <th>状态</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="7" class="td-center">加载中...</td>
          </tr>
          <tr v-else-if="roles.length === 0">
            <td colspan="7" class="td-center">暂无角色数据</td>
          </tr>
          <tr v-for="role in roles" :key="role.id">
            <td>{{ role.id }}</td>
            <td class="td-name" @click="openDetailDialog(role)">{{ role.roleName }}</td>
            <td class="td-mono">{{ role.roleCode }}</td>
            <td>{{ role.description }}</td>
            <td>
              <span class="tag" :class="statusClass(role.status ?? 1)">{{ statusText(role.status ?? 1) }}</span>
            </td>
            <td class="td-time">{{ formatTime(role.createTime) }}</td>
            <td class="td-actions">
              <button class="act-btn" title="查看" @click="openDetailDialog(role)">👁</button>
              <button class="act-btn" title="编辑" @click="openEditDialog(role)">✏</button>
              <button class="act-btn" title="分配菜单" @click="openAssignDialog(role)">🗂</button>
              <button class="act-btn danger" title="删除" @click="handleDelete(role)">✕</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- ========== 分页 ========== -->
    <div class="pagination" v-if="!loading">
      <button class="btn btn-outline" :disabled="(query.page ?? 1) === 1" @click="goPage((query.page ?? 1) - 1)">上一页</button>
      <span class="page-info">第 {{ query.page }} 页</span>
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
                <div class="detail-item"><label>ID</label><span>{{ form.id ?? '' }}</span></div>
                <div class="detail-item"><label>角色名称</label><span>{{ form.roleName }}</span></div>
                <div class="detail-item"><label>角色代码</label><span>{{ form.roleCode }}</span></div>
                <div class="detail-item"><label>描述</label><span>{{ form.description || '-' }}</span></div>
                <div class="detail-item"><label>状态</label><span class="tag" :class="statusClass(form.status ?? 1)">{{ statusText(form.status ?? 1) }}</span></div>
              </div>
            </template>
            <!-- 编辑/创建模式 -->
            <template v-else>
              <div class="form-grid">
                <div class="form-group">
                  <label>角色名称 <span class="required">*</span></label>
                  <input v-model="form.roleName" class="input" placeholder="请输入角色名称" maxlength="50" />
                </div>
                <div class="form-group">
                  <label>角色代码 <span class="required">*</span></label>
                  <input v-model="form.roleCode" class="input" placeholder="请输入角色代码" maxlength="50" />
                </div>
                <div class="form-group wide">
                  <label>描述</label>
                  <textarea v-model="form.description" class="textarea" placeholder="描述角色用途" maxlength="500" rows="2"></textarea>
                </div>
                <div class="form-group">
                  <label>状态 <span class="required">*</span></label>
                  <select v-model="form.status" class="select">
                    <option :value="0">禁用</option>
                    <option :value="1">启用</option>
                  </select>
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
            <button class="btn btn-primary" @click="openEditDialog(form as RoleVO)">编辑</button>
          </div>
        </div>
      </div>
    </teleport>

    <!-- ========== 菜单分配弹窗 ========== -->
    <teleport to="body">
      <div v-if="assignVisible" class="dialog-overlay" @click.self="assignVisible = false">
        <div class="dialog wide" style="max-width: 860px;">
          <div class="dialog-header">
            <h3>分配菜单</h3>
            <button class="dialog-close" @click="assignVisible = false">✕</button>
          </div>
          <div class="dialog-body" style="display: flex; gap: 20px;">
            <div style="flex: 1; min-width: 320px; border: 1px solid #eee; padding: 12px; border-radius: 6px;">
              <menu-tree :nodes="menuTree" :checked="checkedMenuIds" @change="updateChecked"></menu-tree>
            </div>
          </div>
          <div class="dialog-footer">
            <button class="btn btn-outline" @click="assignVisible = false">取消</button>
            <button class="btn btn-primary" @click="handleAssign" :disabled="submitting">
              {{ submitting ? '提交中...' : '保存分配' }}
            </button>
          </div>
        </div>
      </div>
    </teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, defineProps, defineEmits, defineComponent, h } from 'vue'
import type { MenuNode } from '@/api/menu'
import type { RoleVO, RoleDTO, RoleQueryParams } from '@/types/api'
import {
  getRoleListApi,
  createRoleApi,
  updateRoleApi,
  deleteRoleApi,
  getRoleApi,
  assignRoleMenusApi,
  getRoleMenuIdsApi,
} from '@/api/role'
import { getMenuTreeApi } from '@/api/menu'

// ========== 结构与状态 ===========
const roles = ref<RoleVO[]>([])
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const dialogMode = ref<'create'|'edit'|'detail'>('create')
const assignVisible = ref(false)

// 详情/创建表单
const defaultForm = (): RoleDTO & Partial<RoleVO> => ({
  id: undefined,
  roleName: '',
  roleCode: '',
  description: '',
  status: 1,
})
const form = reactive<RoleDTO & Partial<RoleVO>>(defaultForm())

// 详情视图需要的本地缓存
const dialogTitle = computed(() => {
  if (dialogMode.value === 'create') return '添加角色'
  if (dialogMode.value === 'edit') return '编辑角色'
  return '角色详情'
})

// 分页/筛选
const query = reactive<any>({
  page: 1,
  size: 10,
  roleName: undefined,
  roleCode: undefined,
})

// 统计占位数据（与设备页风格保持相同）
const stats = reactive({ totalRoles: 0, enabledRoles: 0, disabledRoles: 0 })

const isLastPage = computed(() => roles.value.length < query.size)

  // 菜单分配相关
 const menuTree = ref<MenuNode[]>([])
const checkedMenuIds = ref<number[]>([])
const menuTreeLoaded = ref(false)
const assignTargetRoleId = ref<number | null>(null)

// 递归菜单树组件（局部实现）
  const MenuTree = defineComponent({
  name: 'MenuTree',
  props: {
    nodes: { type: Array as () => MenuNode[], required: true },
    checked: { type: Array as () => number[], required: true },
  },
  emits: ['change'],
  setup(props, { emit }) {
    function handleToggle(node: MenuNode) {
      const ids: number[] = []
      const walk = (n: MenuNode) => { ids.push(n.id); if (n.children) n.children.forEach(walk) }
      walk(node)
      const exists = props.checked.includes(node.id)
      const next = new Set(props.checked)
      if (exists) {
        ids.forEach(id => next.delete(id))
      } else {
        ids.forEach(id => next.add(id))
      }
      emit('change', Array.from(next))
    }
    const renderNodes = (nodes: MenuNode[]): any[] => {
      return nodes.map((n) => {
        const isChecked = props.checked.includes(n.id)
        return h('div', { key: n.id, style: { marginLeft: '20px' } }, [
          h('label', { style: 'cursor: pointer; display: inline-flex; align-items: center; gap: 6px;' }, [
            h('input', { type: 'checkbox', checked: isChecked, onChange: () => handleToggle(n) }),
            h('span', null, n.menuName ?? '')
          ]),
          n.children && n.children.length ? h('div', null, renderNodes(n.children)) : null
        ])
      })
    }
    return () => h('div', null, renderNodes(props.nodes))
  }
})

// 角色列表获取
async function fetchRoles() {
  loading.value = true
  try {
    const params: any = { page: query.page, size: query.size }
    if (query.roleName) params.roleName = query.roleName
    if (query.roleCode) params.roleCode = query.roleCode
    const res = await getRoleListApi(params)
    roles.value = (res?.data?.data) ?? []
  } catch (e) {
    console.error('获取角色列表失败', e)
    roles.value = []
  } finally {
    loading.value = false
  }
}

async function fetchStats() {
  // 这里简单占位，避免阻塞列表渲染；在真实实现中，应调用后端统计接口
  try {
    // 可能有一个后端统计接口，若无，保持默认值
  } catch {
    // ignore
  }
}

/** 搜索与翻页 */
function handleSearch() {
  query.page = 1
  fetchRoles()
}
function handleReset() {
  query.page = 1
  query.roleName = undefined
  query.roleCode = undefined
  fetchRoles()
}
function goPage(page?: number) {
  query.page = page
  fetchRoles()
}

/** 创建/编辑/查看详情 */
function openCreateDialog() {
  Object.assign(form, defaultForm())
  dialogMode.value = 'create'
  dialogVisible.value = true
}
async function openEditDialog(role: RoleVO) {
  // 拉取最新数据以编辑
  try {
    const res = await getRoleApi(role.id)
    Object.assign(form, res?.data?.data ?? role)
  } catch {
    Object.assign(form, role)
  }
  dialogMode.value = 'edit'
  dialogVisible.value = true
}
async function openDetailDialog(role: RoleVO) {
  try {
    const res = await getRoleApi(role.id)
    Object.assign(form, res?.data?.data ?? role)
  } catch {
    Object.assign(form, role)
  }
  dialogMode.value = 'detail'
  dialogVisible.value = true
}

/** 提交创建/编辑 */
async function handleSubmit() {
  if (!form.roleName || !form.roleCode) {
    alert('请填写必填字段')
    return
  }
  submitting.value = true
  try {
    const dto: RoleDTO = {
      roleName: form.roleName!,
      roleCode: form.roleCode!,
      description: form.description ?? '',
      status: form.status ?? 0,
    }
    if (dialogMode.value === 'create') {
      await createRoleApi(dto)
    } else {
      await updateRoleApi(form.id!, dto)
    }
    dialogVisible.value = false
    fetchRoles()
  } catch (e: any) {
    alert(e?.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

/** 删除 */
async function handleDelete(role: RoleVO) {
  if (!confirm(`确认删除角色「${role.roleName}」？`)) return
  try {
    await deleteRoleApi(role.id)
    fetchRoles()
  } catch (e: any) {
    alert(e?.message || '删除失败')
  }
}

/** 状态文本/样式 */
function statusText(status: number): string {
  const map: Record<number, string> = { 0: '禁用', 1: '启用' }
  return map[status] ?? '未知'
}
function statusClass(status: number): string {
  const map: Record<number, string> = { 0: 'tag-offline', 1: 'tag-online' }
  return map[status] ?? ''
}

/** 格式化时间 */
function formatTime(t?: string): string {
  if (!t) return '-'
  return t.toString().replace('T', ' ').substring(0, 19)
}

/** 菜单分配相关：打开分配对话框 */
async function openAssignDialog(role: RoleVO) {
  assignTargetRoleId.value = role.id ?? null
  checkedMenuIds.value = []
  // 加载菜单树与当前角色菜单
    try {
    const [treeRes, idsRes] = await Promise.all([
      getMenuTreeApi(),
      getRoleMenuIdsApi(role.id),
    ])
    menuTree.value = (treeRes?.data?.data) ?? []
    checkedMenuIds.value = (idsRes?.data?.data ?? []) as number[]
    assignVisible.value = true
  } catch (e) {
    alert('加载菜单树失败')
  }
}

function updateChecked(newChecked: number[]) {
  checkedMenuIds.value = newChecked
}

async function handleAssign() {
  if (assignTargetRoleId.value == null) return
  submitting.value = true
  try {
    await assignRoleMenusApi(assignTargetRoleId.value as number, checkedMenuIds.value)
    assignVisible.value = false
  } catch (e: any) {
    alert(e?.message || '分配失败')
  } finally {
    submitting.value = false
  }
}

// 初次加载
onMounted(() => {
  fetchRoles()
  fetchStats()
})

// 子模板中的递归树：在同文件内实现局部组件
const MenuTreeComponent = {
  props: { nodes: Array as () => MenuNode[], checked: Array as () => number[] },
  emits: ['change'],
  setup(props: { nodes: MenuNode[]; checked: number[] }, { emit }: any) {
    function toggleAll(node: MenuNode) {
      // 这里简单实现：勾选/反勾选当前节点及其所有子节点
      const ids: number[] = []
      function collect(n: MenuNode) {
        ids.push(n.id)
        if (n.children) n.children.forEach(collect)
      }
      collect(node)
      const exist = props.checked.includes(node.id)
      const next = new Set(props.checked)
      if (exist) {
        ids.forEach((i) => next.delete(i))
      } else {
        ids.forEach((i) => next.add(i))
      }
      emit('change', Array.from(next))
    }
    return { toggleAll }
  },
  template: `
    <div>
      <div v-for="node in nodes" :key="node.id" style="margin-left: 0; padding: 4px 0;">
        <label style="cursor: pointer; display: inline-flex; align-items: center; gap: 6px;">
          <input type="checkbox" :checked="checked.includes(node.id)" @change="$emit('change', Array.from(new Set([node.id].concat(checked))))" />
          <span>{{ node.label }}</span>
        </label>
        <div v-if="node.children && node.children.length" style="margin-left: 16px;">
          <MenuTreeNode v-for="child in node.children" :key="child.id" :node="child" :checked="checked" @change="$emit('change', $event)" />
        </div>
      </div>
    </div>
  `,
  components: {
    MenuTreeNode: null as any,
  },
}

</script>

<style scoped>
/* 使用与设备页相同的样式，保持一致的 UI 风格 */
.device-page {
  max-width: 1320px;
}

/* 统计卡片样式复用
   其他样式同 device/index.vue 以确保视觉一致 */
.stat-row { display: grid; grid-template-columns: repeat(auto-fit, minmax(140px, 1fr)); gap: 12px; margin-bottom: 20px; }
.stat-card { background: #fff; border: 1px solid #e0e0e0; padding: 16px 20px; text-align: center; border-top: 3px solid #000; }
.stat-card.online { background: #fafafa; }
.stat-card.flying { background: #f5f5f5; }
.stat-card.offline { background: #fff; border-top-color: #ccc; }
.stat-card.maintenance { background: #f5f5f5; border-top-color: #999; }
.stat-card.scrapped { background: #f0f0f0; border-top-color: #ccc; }
.stat-value { font-size: 28px; font-weight: 700; color: #000; line-height: 1.2; }
.stat-label { font-size: 13px; color: #666; margin-top: 4px; }

/* 搜索 */
.search-bar { background: #fff; border: 1px solid #e0e0e0; padding: 16px 20px; margin-bottom: 16px; display: flex; flex-direction: column; gap: 10px; }
.search-row { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.sep { color: #999; flex-shrink: 0; }

/* 表单控件 */
.input { padding: 8px 12px; border: 1px solid #ccc; font-size: 14px; color: #333; background: #fff; outline: none; transition: border-color 0.2s; flex: 1; min-width: 120px; }
.input:focus { border-color: #000; }
.input.short { flex: none; width: 130px; }
.select { padding: 8px 12px; border: 1px solid #ccc; font-size: 14px; color: #333; background: #fff; outline: none; cursor: pointer; flex: 1; min-width: 120px; }
.select:focus { border-color: #000; }
.textarea { padding: 8px 12px; border: 1px solid #ccc; font-size: 14px; color: #333; background: #fff; outline: none; resize: vertical; width: 100%; box-sizing: border-box; font-family: inherit; }
.textarea:focus { border-color: #000; }

/* 按钮 */
.btn { padding: 8px 16px; font-size: 14px; cursor: pointer; border: none; transition: background 0.2s, color 0.2s, border-color 0.2s; white-space: nowrap; }
.btn:disabled { opacity: 0.5; cursor: not-allowed; }
.btn-primary { background: #000; color: #fff; }
.btn-primary:hover:not(:disabled) { background: #333; }
.btn-outline { background: #fff; color: #333; border: 1px solid #ccc; }
.btn-outline:hover:not(:disabled) { border-color: #000; color: #000; }
.btn-danger { background: #000; color: #fff; }
.btn-danger:hover:not(:disabled) { background: #333; }

/* 操作栏 */
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
.td-loc { font-family: monospace; font-size: 13px; }
.td-actions { display: flex; gap: 4px; }
.act-btn { background: none; border: 1px solid #ccc; padding: 4px 8px; cursor: pointer; font-size: 14px; transition: background 0.2s, border-color 0.2s; }
.act-btn:hover { background: #f0f0f0; border-color: #000; }
.tag { display: inline-block; padding: 2px 10px; font-size: 12px; font-weight: 500; background: #fff; color: #000; border: 1px solid #000; }
.tag-online { border-width: 2px; font-weight: 700; }
.tag-offline { border-color: #ccc; color: #999; font-weight: 400; }
.tag-maintain { background: #f0f0f0; border-color: #ccc; font-weight: 400; }
.tag-scrapped { background: #f0f0f0; border-color: #ccc; color: #999; text-decoration: line-through; }

/* 详情/弹窗 */
.dialog-overlay { position: fixed; inset: 0; background: rgba(0, 0, 0, 0.5); z-index: 1000; display: flex; align-items: center; justify-content: center; padding: 20px; }
.dialog { background: #fff; border: 2px solid #000; width: 100%; max-width: 560px; max-height: 90vh; display: flex; flex-direction: column; }
.dialog.wide { max-width: 720px; }
.dialog-sm { max-width: 420px; }
.dialog-header { display: flex; align-items: center; justify-content: space-between; padding: 16px 24px; border-bottom: 1px solid #e0e0e0; flex-shrink: 0; }
.dialog-header h3 { margin: 0; font-size: 16px; font-weight: 600; color: #000; }
.dialog-close { background: none; border: none; font-size: 20px; cursor: pointer; color: #999; padding: 0; line-height: 1; transition: color 0.2s; }
.dialog-close:hover { color: #000; }
.dialog-body { padding: 24px; overflow-y: auto; flex: 1; }
.dialog-footer { display: flex; justify-content: flex-end; gap: 10px; padding: 16px 24px; border-top: 1px solid #e0e0e0; flex-shrink: 0; }
.detail-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.detail-item { display: flex; flex-direction: column; gap: 4px; }
.detail-item.wide { grid-column: 1 / -1; }
.detail-item label { font-size: 12px; font-weight: 500; color: #666; text-transform: uppercase; }
.detail-item span { font-size: 14px; color: #333; }
.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.form-group { display: flex; flex-direction: column; gap: 6px; }
.form-group.wide { grid-column: 1 / -1; }
.form-group label { font-size: 13px; font-weight: 500; color: #333; }
.required { color: #000; font-weight: 700; }
</style>

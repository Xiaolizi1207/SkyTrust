<template>
  <div class="device-page">
    <!-- ========== 搜索过滤 ========== -->
    <div class="search-bar">
      <div class="search-row">
        <input v-model="query.username" placeholder="用户名" class="input" @keyup.enter="handleSearch" />
        <input v-model="query.phone" placeholder="手机号" class="input" @keyup.enter="handleSearch" />
        <input v-model="query.email" placeholder="邮箱" class="input" @keyup.enter="handleSearch" />
        <select v-model="query.status" class="select">
          <option :value="undefined">全部</option>
          <option :value="1">正常</option>
          <option :value="0">禁用</option>
          <option :value="2">未激活</option>
        </select>
      </div>
      <div class="search-row">
        <button class="btn btn-primary" @click="handleSearch">搜索</button>
        <button class="btn btn-outline" @click="handleReset">重置</button>
      </div>
    </div>

    <!-- ========== 操作栏 ========== -->
    <div class="action-bar">
      <button class="btn btn-primary" @click="openCreateDialog">+ 新增用户</button>
    </div>

    <!-- ========== 数据表 ========== -->
    <div class="table-wrap">
      <table class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>用户名</th>
            <th>真实姓名</th>
            <th>电话</th>
            <th>邮箱</th>
            <th>状态</th>
            <th>角色</th>
            <th>信用分</th>
            <th>余额 (¥)</th>
            <th>最近登录</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="12" class="td-center">加载中...</td>
          </tr>
          <tr v-else-if="users.length === 0">
            <td colspan="12" class="td-center">暂无用户数据</td>
          </tr>
          <tr v-for="u in users" :key="u.id">
            <td>{{ u.id }}</td>
            <td class="td-name" @click="openDetailDialog(u)">{{ u.username }}</td>
            <td>{{ u.realName }}</td>
            <td>{{ u.phone }}</td>
            <td>{{ u.email }}</td>
            <td>
              <span class="tag" :class="statusClass(u.status)">{{ statusText(u.status) }}</span>
            </td>
            <td>{{ u.role }}</td>
            <td>{{ u.creditScore ?? '-' }}</td>
            <td>¥{{ formatPrice(u.balance) }}</td>
            <td>{{ formatTime(u.lastLoginTime) }}</td>
            <td>{{ formatTime(u.createTime) }}</td>
            <td class="td-actions">
              <button class="act-btn" title="查看" @click="openDetailDialog(u)">👁</button>
              <button class="act-btn" title="编辑" @click="openEditDialog(u)">✏</button>
              <button class="act-btn danger" title="删除" @click="handleDelete(u)">✕</button>
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

    <!-- ========== 创建/编辑弹窗 ========== -->
    <teleport to="body">
      <div v-if="dialogVisible" class="dialog-overlay" @click.self="dialogVisible = false">
        <div class="dialog" :class="{ wide: dialogMode !== 'detail' }">
          <div class="dialog-header">
            <h3>{{ dialogTitle }}</h3>
            <button class="dialog-close" @click="dialogVisible = false">✕</button>
          </div>
          <div class="dialog-body">
            <!-- 详情模式 -->
            <template v-if="dialogMode === 'detail'">
              <div class="detail-grid">
                <div class="detail-item"><label>ID</label><span>{{ form.id }}</span></div>
                <div class="detail-item"><label>用户名</label><span>{{ form.username }}</span></div>
                <div class="detail-item"><label>真实姓名</label><span>{{ form.realName }}</span></div>
                <div class="detail-item"><label>电话</label><span>{{ form.phone }}</span></div>
                <div class="detail-item"><label>邮箱</label><span>{{ form.email }}</span></div>
                <div class="detail-item"><label>身份证</label><span>{{ form.idCard }}</span></div>
                <div class="detail-item"><label>状态</label><span class="tag" :class="statusClass(form.status)">{{ statusText(form.status) }}</span></div>
                <div class="detail-item"><label>角色</label><span>{{ form.role }}</span></div>
                <div class="detail-item"><label>信用分</label><span>{{ form.creditScore ?? '-' }}</span></div>
                <div class="detail-item"><label>余额</label><span>¥{{ formatPrice(form.balance) }}</span></div>
                <div class="detail-item"><label>最近登录</label><span>{{ formatTime(form.lastLoginTime) }}</span></div>
                <div class="detail-item"><label>创建时间</label><span>{{ formatTime(form.createTime) }}</span></div>
                <div class="detail-item"><label>更新时间</label><span>{{ formatTime(form.updateTime) }}</span></div>
              </div>
            </template>

            <!-- 编辑/创建模式 -->
            <template v-else>
              <div class="form-grid">
                <div class="form-group">
                  <label>用户名 <span class="required">*</span></label>
                  <input v-model="form.username" class="input" placeholder="请输入用户名" />
                </div>
                <div class="form-group" v-if="dialogMode === 'create'">
                  <label>密码 <span class="required">*</span></label>
                  <input v-model="form.password" type="password" class="input" placeholder="请输入密码" />
                </div>
                <div class="form-group">
                  <label>电话</label>
                  <input v-model="form.phone" class="input" />
                </div>
                <div class="form-group">
                  <label>邮箱</label>
                  <input v-model="form.email" class="input" />
                </div>
                <div class="form-group">
                  <label>真实姓名</label>
                  <input v-model="form.realName" class="input" />
                </div>
                <div class="form-group">
                  <label>身份证</label>
                  <input v-model="form.idCard" class="input" />
                </div>
                <div class="form-group">
                  <label>状态</label>
                  <select v-model="form.status" class="select">
                    <option :value="0">禁用</option>
                    <option :value="1">正常</option>
                    <option :value="2">未激活</option>
                  </select>
                </div>
                <div class="form-group">
                  <label>角色</label>
                  <select v-model="form.role" class="select">
                    <option value="ADMIN">ADMIN</option>
                    <option value="USER">USER</option>
                    <option value="PILOT">PILOT</option>
                  </select>
                </div>
                <div class="form-group">
                  <label>信用分</label>
                  <input v-model.number="form.creditScore" class="input" type="number" />
                </div>
                <div class="form-group">
                  <label>余额</label>
                  <input v-model.number="form.balance" class="input" type="number" step="0.01" />
                </div>
              </div>
            </template>
          </div>
          <div class="dialog-footer" v-if="dialogMode !== 'detail'">
            <button class="btn btn-outline" @click="dialogVisible = false">取消</button>
            <button class="btn btn-primary" @click="handleSubmit" :disabled="submitting">{{ submitting ? '提交中...' : (dialogMode === 'create' ? '创建' : '保存') }}</button>
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
import type { UserAdminVO, UserAdminDTO, UserQueryParams } from '@/types/api'
import { getUserListApi, createUserApi, updateUserApi, deleteUserApi, getUserApi } from '@/api/user'

// ========== 数据 ========== 
const users = ref<UserAdminVO[]>([])
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const dialogMode = ref<'create'|'edit'|'detail'>('create')
const form = reactive<UserAdminDTO & Partial<UserAdminVO>>({
  id: undefined,
  username: '',
  password: '',
  phone: '',
  email: '',
  realName: '',
  idCard: '',
  status: 1,
  role: 'USER',
  creditScore: 0,
  balance: 0,
  lastLoginTime: undefined,
  createTime: undefined,
  updateTime: undefined,
})

const query = reactive<UserQueryParams>({
  page: 1,
  size: 10,
  username: undefined,
  phone: undefined,
  email: undefined,
  status: undefined,
})

const isLastPage = computed(() => users.value.length < (query.size as number))
const dialogTitle = computed(() => (dialogMode.value === 'create' ? '创建用户' : dialogMode.value === 'edit' ? '编辑用户' : '用户详情'))

// ========== 辅助方法 ==========
function statusText(status: number | undefined): string {
  if (status == null) return '未知'
  const map: Record<number, string> = { 0: '禁用', 1: '正常', 2: '未激活' }
  return map[status] ?? '未知'
}
function statusClass(status: number | undefined): string {
  if (status == null) return ''
  const map: Record<number, string> = { 0: 'tag-offline', 1: 'tag-online', 2: 'tag-maintain' }
  return map[status] ?? ''
}
function formatPrice(v?: number): string {
  if (v == null) return '0'
  return Number(v).toFixed(2)
}
function formatTime(t?: string): string {
  if (!t) return '-'
  return t.replace('T', ' ').substring(0, 19)
}

// 供模板使用的时间格式化函数，同步当前对象格式
const lastLoginDisplay = computed(() => formatTime((form as any).lastLoginTime))
const createTimeDisplay = computed(() => formatTime(form.createTime))

// ========== 数据获取/操作 ========== 
async function fetchUsers() {
  loading.value = true
  try {
    const params = {
      page: query.page,
      size: query.size,
      username: query.username,
      phone: query.phone,
      email: query.email,
      status: query.status,
    } as any
    const res = await getUserListApi(params)
    users.value = res.data?.data ?? []
  } catch (e) {
    console.error('获取用户列表失败', e)
    users.value = []
  } finally {
    loading.value = false
  }
}

async function handleSearch() {
  query.page = 1
  fetchUsers()
}
function handleReset() {
  query.page = 1
  query.username = undefined
  query.phone = undefined
  query.email = undefined
  query.status = undefined
  fetchUsers()
}
async function goPage(p: number) {
  query.page = p
  fetchUsers()
}

function openCreateDialog() {
  Object.assign(form, {
    id: undefined,
    username: '',
    password: '',
    phone: '',
    email: '',
    realName: '',
    idCard: '',
    status: 1,
    role: 'USER',
    creditScore: 0,
    balance: 0,
  } as UserAdminDTO & Partial<UserAdminVO>)
  dialogMode.value = 'create'
  dialogVisible.value = true
}
function openEditDialog(u: UserAdminVO) {
  Object.assign(form, u as UserAdminDTO)
  dialogMode.value = 'edit'
  dialogVisible.value = true
}
function openDetailDialog(u: UserAdminVO) {
  Object.assign(form, u as UserAdminDTO)
  dialogMode.value = 'detail'
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!form.username || (dialogMode.value === 'create' && !form.password)) {
    alert('请填写必填字段')
    return
  }
  submitting.value = true
  try {
    const dto: UserAdminDTO = {
      username: form.username,
      password: form.password,
      phone: form.phone,
      email: form.email,
      realName: form.realName,
      idCard: form.idCard,
      status: form.status ?? 1,
      role: form.role ?? 'USER'
    } as any
    if (dialogMode.value === 'create') {
      await createUserApi(dto)
    } else {
      await updateUserApi(form.id!, dto)
    }
    dialogVisible.value = false
    fetchUsers()
  } catch (e: any) {
    alert(e?.message ?? '操作失败')
  } finally {
    submitting.value = false
  }
}

async function handleDelete(u: UserAdminVO) {
  if (!confirm(`确认删除用户「${u.username}」？`)) return
  try {
    await deleteUserApi(u.id)
    fetchUsers()
  } catch (e: any) {
    alert(e?.message ?? '删除失败')
  }
}

onMounted(() => {
  fetchUsers()
})
</script>

<style scoped>
.device-page {
  max-width: 1320px;
}

/* 复用 device 页面样式，确保 CSS 类名与 pattern 匹配 */
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
.sep { color: #999; }
.input { padding: 8px 12px; border: 1px solid #ccc; font-size: 14px; color: #333; background: #fff; outline: none; transition: border-color 0.2s; }
.select { padding: 8px 12px; border: 1px solid #ccc; font-size: 14px; color: #333; background: #fff; outline: none; }
.btn { padding: 8px 16px; font-size: 14px; cursor: pointer; border: none; }
.btn-primary { background: #000; color: #fff; }
.btn-outline { background: #fff; color: #333; border: 1px solid #ccc; }
.td-center { text-align: center; color: #999; padding: 40px 0; }
.td-name { font-weight: 600; color: #000; cursor: pointer; }
.td-name:hover { text-decoration: underline; }
.tag { display: inline-block; padding: 2px 10px; border: 1px solid #000; font-size: 12px; font-weight: 500; }
.tag-online { font-weight: 700; border-width: 2px; }
.tag-offline { color: #666; border-color: #bbb; }
.tag-maintain { background: #f0f0f0; border-color: #ccc; }
.data-table { width: 100%; border-collapse: collapse; }
.data-table th, .data-table td { padding: 12px 14px; border-bottom: 1px solid #e0e0e0; text-align: left; }
.table-wrap { background: #fff; border: 1px solid #e0e0e0; overflow: auto; }
.act-btn { border: 1px solid #ccc; padding: 4px 8px; background: #fff; cursor: pointer; }
.pagination { display: flex; justify-content: center; align-items: center; gap: 12px; padding: 12px 0; }
.dialog-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 1000; padding: 20px; }
.dialog { background: #fff; border: 2px solid #000; width: 100%; max-width: 720px; display: flex; flex-direction: column; }
.dialog-header { display: flex; justify-content: space-between; align-items: center; padding: 16px 24px; border-bottom: 1px solid #ddd; }
.dialog-body { padding: 16px 24px; }
.dialog-footer { display: flex; justify-content: flex-end; padding: 16px 24px; border-top: 1px solid #ddd; }
.detail-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.detail-item { display: flex; flex-direction: column; gap: 6px; }
.detail-item label { font-size: 12px; font-weight: 500; color: #666; text-transform: uppercase; }
.detail-item span { font-size: 14px; color: #333; }
.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.form-group { display: flex; flex-direction: column; gap: 6px; }
.form-group.wide { grid-column: 1 / -1; }
.required { color: #000; font-weight: 700; }
</style>

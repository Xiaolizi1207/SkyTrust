<script setup lang="ts">
// GOAL: Create a Vue 3 page with a TREE VIEW for menu management.
// This file follows the project patterns from the device/index.vue for dialogs, inputs, buttons, and tags.
// It uses a local recursive MenuTreeItem component defined inline.
// API imports follow the pattern: import { getMenuTreeApi, createMenuApi, updateMenuApi, deleteMenuApi, getMenuApi } from '@/api/menu'

import { defineComponent, h, onMounted, ref, reactive, type PropType } from 'vue'
import type { MenuNode, MenuDTO } from '@/api/menu'
import {
  getMenuTreeApi,
  createMenuApi,
  updateMenuApi,
  deleteMenuApi
} from '@/api/menu'

// Local recursive tree item component
const MenuTreeItem: any = defineComponent({
  name: 'MenuTreeItem',
  props: {
    node: { type: Object as PropType<MenuNode>, required: true },
    depth: { type: Number, default: 0 },
    expandedSet: { type: Object as PropType<Set<number>>, required: true }
  },
  emits: ['add-child','edit','delete','toggle'],
  setup(props: any, { emit }: any){
    const onToggle = () => emit('toggle', props.node.id)
    const onAddChild = () => emit('add-child', props.node)
    const onEdit = () => emit('edit', props.node)
    const onDelete = () => emit('delete', props.node)

    return () => {
      const pad = props.depth * 20
      const hasChildren = props.node.children && props.node.children.length > 0
      const isExpanded = props.expandedSet.has(props.node.id)
      // Safely collect children, filter out any undefined/null entries
      const safeChildren = hasChildren && isExpanded ? (props.node.children ?? []).filter((c: any) => !!c) : []
      const childNodesVNodes = safeChildren.map((child: MenuNode) => {
        return h(MenuTreeItem, {
          key: child.id,
          node: child,
          depth: props.depth + 1,
          expandedSet: props.expandedSet,
          onAddChild: (n: any) => emit('add-child', n),
          onEdit: (n: any) => emit('edit', n),
          onDelete: (n: any) => emit('delete', n),
          onToggle: (id: number) => emit('toggle', id)
        })
      })

      return h('li', { class: 'tree-node', style: { paddingLeft: pad + 'px' } }, [
        h('span', { class: 'expand', onClick: onToggle }, hasChildren ? (isExpanded ? '▾' : '▸') : '' ),
        h('span', { class: 'icon' }, props.node.icon ? h('i', { class: props.node.icon }) : ''),
        h('span', { class: 'name' }, props.node.menuName ?? props.node.name ?? 'Unnamed'),
        props.node.menuPath ? h('span', { class: 'path' }, `(${props.node.menuPath})`) : null,
        h('span', { class: 'tag' },
          props.node.menuType === 0
            ? h('span', { class: 'tag directory' }, '目录')
            : props.node.menuType === 1
              ? h('span', { class: 'tag menu' }, '菜单')
              : h('span', { class: 'tag button' }, '按钮')
        ),
        h('button', { class: 'btn small', onClick: onAddChild }, '添加子菜单'),
        h('button', { class: 'btn small', onClick: onEdit }, '编辑'),
        h('button', { class: 'btn small', onClick: onDelete }, '删除'),
        ...childNodesVNodes
      ])
    }
  }
})

// Main component state
const menuTree = ref<MenuNode[]>([])
const expanded = ref<Set<number>>(new Set())

const showDialog = ref(false)
const dialogMode = ref<'create'|'edit'>('create')
// dialogForm allows arbitrary MenuDTO fields accessed via dialogForm[fieldKey]
type DialogFormType = Partial<MenuDTO> & { id?: number; [key: string]: any }
const dialogForm = reactive<DialogFormType>({ id: undefined } as any)

async function fetchTree(){
  try {
    const res = await getMenuTreeApi()
    // API returns { data: MenuNode[] } inside ApiResult
    menuTree.value = res?.data?.data ?? []
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  fetchTree()
})

function toggleExpand(id: number){ expanded.value.has(id) ? expanded.value.delete(id) : expanded.value.add(id) }

async function openCreate(parent: MenuNode | null){
  dialogMode.value = 'create'
  showDialog.value = true
  Object.assign(dialogForm, {
    id: undefined,
    menuName: '',
    menuCode: '',
    parentId: parent?.id ?? 0,
    menuPath: '',
    component: '',
    icon: '',
    menuType: 0,
    sortOrder: 0,
    perms: '',
    isExternal: 0,
    isCache: 0,
    isVisible: 1
  } as Partial<MenuDTO>)
  buildParentOptions()
}

async function openEdit(node: MenuNode){
  dialogMode.value = 'edit'
  showDialog.value = true
  Object.assign(dialogForm, {
    id: node.id,
    menuName: node.menuName,
    menuCode: node.menuCode,
    parentId: node.parentId ?? 0,
    menuPath: node.menuPath,
    component: node.component,
    icon: node.icon,
    menuType: node.menuType,
    sortOrder: node.sortOrder,
    perms: node.perms,
    isExternal: node.isExternal,
    isCache: node.isCache,
    isVisible: node.isVisible
  } as Partial<MenuDTO>)
  buildParentOptions()
}

const dialogFields = [
  { key: 'menuName', label: '菜单名称', type: 'text' },
  { key: 'menuCode', label: '菜单编码', type: 'text' },
  { key: 'parentId', label: '父菜单', type: 'select', options: [] as { value: number; label: string }[] },
  { key: 'menuPath', label: '菜单路径', type: 'text' },
  { key: 'component', label: '组件', type: 'text' },
  { key: 'icon', label: '图标', type: 'text' },
  { key: 'menuType', label: '菜单类型', type: 'select', options: [
    { value: 0, label: '目录' },
    { value: 1, label: '菜单' },
    { value: 2, label: '按钮' }
  ] },
  { key: 'sortOrder', label: '排序', type: 'number' },
  { key: 'perms', label: '权限', type: 'text' },
  { key: 'isExternal', label: '外部链接', type: 'checkbox' },
  { key: 'isCache', label: '缓存', type: 'checkbox' },
  { key: 'isVisible', label: '可见', type: 'checkbox' },
] as Array<{ key: string; label: string; type: string; options?: any[] }>

const dialogParentOptions = ref<Array<{ value: number; label: string }>>([{ value: 0, label: 'Root' }])

function buildParentOptions(){
  const options = [{ value: 0, label: 'Root' }]
  const walk = (nodes: MenuNode[], prefix = '') => {
    nodes.forEach((n) => {
      options.push({ value: n.id, label: `${prefix}${n.menuName ?? '节点'}` })
      if ((n as any).children?.length) walk((n as any).children, prefix + '  ')
    })
  }
  walk(menuTree.value, '')
  // assign to dialog field options
  const f = dialogFields.find(f => f.key === 'parentId')
  if (f) (f as any).options = options
  dialogParentOptions.value = options
}

async function submitDialog(){
  if (dialogMode.value === 'create') {
    await createMenuApi(dialogForm as MenuDTO)
  } else {
    await updateMenuApi((dialogForm as any).id as number, dialogForm as MenuDTO)
  }
  showDialog.value = false
  await fetchTree()
}

function closeDialog(){ showDialog.value = false }

async function handleDelete(node: MenuNode){
  if (!node?.id) return
  if (!confirm(`确定删除菜单 ${node.menuName}?`)) return
  await deleteMenuApi(node.id)
  await fetchTree()
}

function selectNode(node: MenuNode){
  // Optional: enable right-side detail when a node is selected (not strictly required for this task)
}

</script>

<template>
  <div class="page-menu">
    <!-- Action bar -->
    <div class="toolbar">
      <button class="btn primary" @click="openCreate(null)">+ 添加菜单</button>
    </div>

    <div class="layout">
      <!-- LEFT: Tree view -->
      <aside class="tree-col">
        <ul class="menu-tree">
          <MenuTreeItem
            v-for="node in menuTree"
            :key="node.id"
            :node="node"
            :depth="0"
            :expandedSet="expanded"
            @add-child="openCreate"
            @edit="openEdit"
            @delete="handleDelete"
            @toggle="toggleExpand"
          />
        </ul>
      </aside>

      <!-- RIGHT? Detail/Editor panel (dialog used for create/edit) -->
      <section class="detail-col" aria-label="menu-detail" />
    </div>

    <!-- Create / Edit Dialog -->
    <div class="dialog-overlay" v-if="showDialog">
      <div class="dialog">
        <div class="dialog-header">{{ dialogMode === 'create' ? '+ 添加菜单' : '编辑菜单' }}</div>
        <div class="dialog-content">
          <div class="form-grid">
            <div v-for="field in dialogFields" :key="field.key" class="form-row">
              <label>{{ field.label }}</label>
              <input v-if="field.type === 'text' || field.type === 'number'" :type="field.type" v-model="dialogForm[field.key]" class="input" />
              <input v-else-if="field.type === 'checkbox'" type="checkbox" v-model="dialogForm[field.key]" />
              <select v-else-if="field.type === 'select'" v-model="dialogForm[field.key]" class="input">
                <option v-for="opt in field.options" :value="opt.value" :key="opt.value">{{ opt.label }}</option>
              </select>
            </div>
          </div>
        </div>
        <div class="dialog-actions">
          <button class="btn" @click="closeDialog">取消</button>
          <button class="btn primary" @click="submitDialog">保存</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Basic visual styles inspired by device/index.vue patterns */
.page-menu { padding: 16px; font-family: Inter, ui-sans-serif, system-ui; }
.toolbar { display:flex; justify-content: flex-end; margin-bottom: 8px; }
.btn { border: 1px solid #d9d9d9; background: #fff; padding: 6px 12px; border-radius: 6px; cursor: pointer; }
.btn.primary { background: #409eff; color: #fff; border-color: #409eff; }
.layout { display: grid; grid-template-columns: 420px 1fr; gap: 16px; }
.tree-col { border: 1px solid #e5e5e5; border-radius: 6px; padding: 8px; background: #fff; min-height: 420px; }
.menu-tree { list-style: none; margin: 0; padding: 0; }
.tree-node { display: flex; align-items: center; gap: 6px; padding: 6px 4px; border-radius: 4px; }
.tree-node:hover { background: #f5f7fa; }
.tree-node .expand { width: 20px; cursor: pointer; color: #888; }
.tree-node .icon { width: 18px; height: 18px; display: inline-block; }
.tree-node .name { font-weight: 500; }
.tree-node .path { color: #666; margin-left: 6px; }
.tree-node .tag { padding: 2px 6px; border-radius: 6px; font-size: 12px; margin-left: 6px; }
.tag.directory { background: #1f2d3d; color: #fff; font-weight: 700; }
.tag.menu { background: #e6f7ff; color: #0050b3; }
.tag.button { background: #f2f2f2; color: #555; }
.dialog-overlay { position: fixed; left:0; top:0; right:0; bottom:0; background: rgba(0,0,0,.25); display:flex; align-items: center; justify-content: center; }
.dialog { width: 560px; background: #fff; border-radius: 8px; padding: 12px; box-shadow: 0 6px 30px rgba(0,0,0,.25); }
.dialog-header { font-weight: 700; margin-bottom: 6px; }
.dialog-content { padding: 6px 0; }
.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; padding: 6px 0; }
.form-row { display: contents; }
.form-row > label { grid-column: 1 / 2; align-self: center; padding-right: 8px; font-size: 13px; color: #555; }
.form-row .input { width: 100%; }
.dialog-actions { display: flex; justify-content: flex-end; gap: 8px; padding-top: 8px; border-top: 1px solid #eee; }
</style>

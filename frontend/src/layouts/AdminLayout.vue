<template>
  <div class="admin-layout">
    <!-- 侧边栏遮罩（移动端） -->
    <div v-if="sidebarVisible && isMobile" class="sidebar-overlay" @click="toggleSidebar"></div>

    <!-- ========== 侧边栏 ========== -->
    <aside class="sidebar" :class="{ collapsed: sidebarCollapsed, visible: sidebarVisible }">
      <div class="sidebar-header">
        <div class="logo">
          <span class="logo-icon">S</span>
          <span v-show="!sidebarCollapsed" class="logo-text">SkyTrust</span>
        </div>
      </div>

      <nav class="sidebar-menu">
        <template v-for="item in authStore.menus" :key="item.id">
          <!-- 目录类型（有子菜单） -->
          <div v-if="item.children && item.children.length > 0" class="menu-group">
            <div class="menu-item" :class="{ active: isMenuActive(item) }" @click="toggleSubMenu(item.id)">
              <span class="menu-icon">{{ getIcon(item.icon) }}</span>
              <span v-show="!sidebarCollapsed" class="menu-label">{{ item.menuName }}</span>
              <span v-show="!sidebarCollapsed" class="menu-arrow" :class="{ open: openSubMenus.has(item.id) }">▸</span>
            </div>
            <div v-show="!sidebarCollapsed && openSubMenus.has(item.id)" class="sub-menu">
              <div
                v-for="child in item.children"
                :key="child.id"
                class="menu-item sub-item"
                :class="{ active: isMenuActive(child) }"
                @click="navigateTo(child)"
              >
                <span class="menu-icon small">{{ getIcon(child.icon) }}</span>
                <span class="menu-label">{{ child.menuName }}</span>
              </div>
            </div>
          </div>
          <!-- 菜单/按钮类型 -->
          <div v-else class="menu-item" :class="{ active: isMenuActive(item) }" @click="navigateTo(item)">
            <span class="menu-icon">{{ getIcon(item.icon) }}</span>
            <span v-show="!sidebarCollapsed" class="menu-label">{{ item.menuName }}</span>
          </div>
        </template>
      </nav>

      <!-- 折叠按钮 -->
      <div class="sidebar-footer" @click="toggleCollapse">
        <span class="collapse-icon">{{ sidebarCollapsed ? '▸' : '◂' }}</span>
        <span v-show="!sidebarCollapsed" class="collapse-label">收起侧边栏</span>
      </div>
    </aside>

    <!-- ========== 右侧区域 ========== -->
    <div class="main-area" :class="{ collapsed: sidebarCollapsed }">
      <!-- 顶部导航 -->
      <header class="top-header">
        <button class="btn-toggle" @click="toggleSidebar">
          ☰
        </button>
        <div class="header-title">{{ currentTitle }}</div>
        <div class="header-right">
          <span class="user-name">{{ authStore.user?.realName || authStore.user?.username }}</span>
          <span class="user-role">{{ roleText }}</span>
          <button class="btn-logout" @click="handleLogout">退出登录</button>
        </div>
      </header>

      <!-- 内容区域 -->
      <main class="content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import type { MenuNode } from '@/api/menu'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const sidebarCollapsed = ref(false)
const sidebarVisible = ref(false)
const isMobile = ref(false)
const openSubMenus = ref(new Set<number>())

// ========== 计算属性 ==========
const currentTitle = computed(() => {
  return (route.meta?.title as string) || 'SkyTrust 管理后台'
})

const roleText = computed(() => {
  const role = authStore.user?.role
  if (role === 'ADMIN') return '管理员'
  if (role === 'PILOT') return '飞行员'
  if (role === 'USER') return '普通用户'
  return role || ''
})

// ========== 方法 ==========

/** 判断菜单是否激活 */
function isMenuActive(item: MenuNode): boolean {
  if (item.menuPath && route.path.startsWith(item.menuPath)) {
    return true
  }
  return false
}

/** 导航到菜单 */
function navigateTo(item: MenuNode) {
  if (item.menuPath) {
    router.push(item.menuPath)
  }
  if (isMobile.value) {
    sidebarVisible.value = false
  }
}

/** 展开/收起子菜单 */
function toggleSubMenu(id: number) {
  if (openSubMenus.value.has(id)) {
    openSubMenus.value.delete(id)
  } else {
    openSubMenus.value.add(id)
  }
}

/** 切换侧边栏折叠 */
function toggleCollapse() {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

/** 切换侧边栏可见（移动端） */
function toggleSidebar() {
  sidebarVisible.value = !sidebarVisible.value
}

/** 检测屏幕宽度 */
function checkScreen() {
  isMobile.value = window.innerWidth < 768
  if (isMobile.value) {
    sidebarVisible.value = false
  } else {
    sidebarVisible.value = true
  }
}

/** 退出登录 */
async function handleLogout() {
  await authStore.logout()
  router.replace('/login')
}

/** 获取图标（简单映射） */
function getIcon(icon?: string): string {
  if (!icon) return '○'
  const iconMap: Record<string, string> = {
    user: '👤',
    system: '⚙',
    device: '🛸',
    order: '📋',
    payment: '💰',
    message: '✉',
    dashboard: '📊',
    setting: '🔧',
    menu: '📑',
    role: '🔐',
    log: '📝',
  }
  return iconMap[icon] || '○'
}

// 路由变化时自动展开当前菜单的父级
watch(() => route.path, () => {
  if (authStore.menus.length > 0) {
    expandActiveParent()
  }
})

function expandActiveParent() {
  for (const item of authStore.menus) {
    if (item.children && item.children.length > 0) {
      for (const child of item.children) {
        if (child.menuPath && route.path.startsWith(child.menuPath)) {
          openSubMenus.value.add(item.id)
          return
        }
      }
    }
  }
}

onMounted(() => {
  checkScreen()
  window.addEventListener('resize', checkScreen)
  if (authStore.menus.length === 0) {
    authStore.fetchMenus()
  } else {
    expandActiveParent()
  }
})

onUnmounted(() => {
  window.removeEventListener('resize', checkScreen)
})
</script>

<style scoped>
.admin-layout {
  display: flex;
  height: 100vh;
  background: var(--bg-page, #f5f5f5);
}

/* ========== 侧边栏遮罩 ========== */
.sidebar-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 99;
}

/* ========== 侧边栏 ========== */
.sidebar {
  width: 240px;
  background: #000;
  color: #fff;
  display: flex;
  flex-direction: column;
  transition: width 0.3s, transform 0.3s;
  overflow: hidden;
  flex-shrink: 0;
  z-index: 100;
}
.sidebar.collapsed {
  width: 64px;
}
.sidebar-header {
  padding: 16px;
  border-bottom: 1px solid #333;
  flex-shrink: 0;
}
.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  white-space: nowrap;
}
.logo-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  background: #fff;
  color: #000;
  font-weight: 700;
  font-size: 18px;
  flex-shrink: 0;
}
.logo-text {
  font-size: 18px;
  font-weight: 600;
  white-space: nowrap;
}

/* ========== 菜单 ========== */
.sidebar-menu {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
}
.sidebar-menu::-webkit-scrollbar {
  width: 4px;
}
.sidebar-menu::-webkit-scrollbar-thumb {
  background: #555;
}
.menu-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
  white-space: nowrap;
  gap: 10px;
  color: #999;
  font-size: 14px;
  user-select: none;
  border-left: 3px solid transparent;
}
.menu-item:hover {
  background: #1a1a1a;
  color: #fff;
}
.menu-item.active {
  background: #1a1a1a;
  color: #fff;
  border-left-color: #fff;
}
.menu-icon {
  font-size: 16px;
  width: 20px;
  text-align: center;
  flex-shrink: 0;
}
.menu-icon.small {
  font-size: 14px;
}
.menu-label {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
}
.menu-arrow {
  transition: transform 0.2s;
  font-size: 12px;
}
.menu-arrow.open {
  transform: rotate(90deg);
}
.sub-menu {
  background: #0d0d0d;
}
.sub-item {
  padding-left: 46px;
  font-size: 13px;
}

/* ========== 侧边栏底部 ========== */
.sidebar-footer {
  padding: 12px 16px;
  border-top: 1px solid #333;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 10px;
  color: #999;
  font-size: 13px;
  transition: color 0.2s;
  flex-shrink: 0;
}
.sidebar-footer:hover {
  color: #fff;
}
.collapse-icon {
  font-size: 14px;
  width: 20px;
  text-align: center;
}

/* ========== 主区域 ========== */
.main-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

/* ========== 顶部导航 ========== */
.top-header {
  display: flex;
  align-items: center;
  padding: 0 24px;
  height: 56px;
  background: #fff;
  border-bottom: 1px solid #e0e0e0;
  gap: 16px;
  flex-shrink: 0;
}
.btn-toggle {
  display: none;
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #333;
  padding: 4px;
}
.header-title {
  flex: 1;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
}
.user-name {
  color: #333;
  font-weight: 500;
}
.user-role {
  color: #999;
  font-size: 12px;
  background: #f0f0f0;
  padding: 2px 8px;
}
.btn-logout {
  padding: 6px 16px;
  background: none;
  border: 1px solid #ccc;
  cursor: pointer;
  font-size: 14px;
  color: #666;
  transition: border-color 0.2s, color 0.2s;
}
.btn-logout:hover {
  border-color: #000;
  color: #000;
}

/* ========== 内容区 ========== */
.content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}

/* ========== 响应式 ========== */
@media (max-width: 767px) {
  .sidebar {
    position: fixed;
    left: 0;
    top: 0;
    bottom: 0;
    transform: translateX(-100%);
  }
  .sidebar.visible {
    transform: translateX(0);
  }
  .sidebar.collapsed {
    width: 240px;
  }
  .btn-toggle {
    display: block;
  }
}
</style>

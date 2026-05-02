import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { requiresAuth: false },
  },
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/home/index.vue'),
    meta: { requiresAuth: false },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/register/index.vue'),
    meta: { requiresAuth: false },
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: () => import('@/views/forgot-password/index.vue'),
    meta: { requiresAuth: false },
  },
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { requiresAuth: true, title: '仪表盘' },
      },
      {
        path: 'device',
        name: 'Device',
        component: () => import('@/views/device/index.vue'),
        meta: { requiresAuth: true, title: '设备管理' },
      },
      {
        path: 'orders',
        name: 'Orders',
        component: () => import('@/views/order/index.vue'),
        meta: { requiresAuth: true, title: '租赁订单' },
      },
      {
        path: 'payments',
        name: 'Payments',
        component: () => import('@/views/payment/index.vue'),
        meta: { requiresAuth: true, title: '支付管理' },
      },
      {
        path: 'wallet',
        name: 'Wallet',
        component: () => import('@/views/wallet/index.vue'),
        meta: { requiresAuth: true, title: '钱包' },
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('@/views/user/index.vue'),
        meta: { requiresAuth: true, title: '用户管理' },
      },
      {
        path: 'roles',
        name: 'Roles',
        component: () => import('@/views/role/index.vue'),
        meta: { requiresAuth: true, title: '角色管理' },
      },
      {
        path: 'menus',
        name: 'Menus',
        component: () => import('@/views/menu/index.vue'),
        meta: { requiresAuth: true, title: '菜单管理' },
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/index.vue'),
        meta: { requiresAuth: true, title: '个人中心' },
      },
      // ===== 区块链模块 =====
      {
        path: 'blockchain/passport',
        name: 'BlockchainPassport',
        component: () => import('@/views/blockchain/passport/index.vue'),
        meta: { requiresAuth: true, title: '数字护照' },
      },
      {
        path: 'blockchain/licensing',
        name: 'Licensing',
        component: () => import('@/views/blockchain/licensing/index.vue'),
        meta: { requiresAuth: true, title: '执照验证' },
      },
      {
        path: 'blockchain/flight-log',
        name: 'FlightLog',
        component: () => import('@/views/blockchain/flight-log/index.vue'),
        meta: { requiresAuth: true, title: '飞行日志' },
      },
      // ===== AI 与智能模块 =====
      {
        path: 'geofence',
        name: 'Geofence',
        component: () => import('@/views/geofence/index.vue'),
        meta: { requiresAuth: true, title: '地理围栏' },
      },
      {
        path: 'pricing',
        name: 'Pricing',
        component: () => import('@/views/pricing/index.vue'),
        meta: { requiresAuth: true, title: '动态定价' },
      },
      {
        path: 'ai/chatbot',
        name: 'AIBot',
        component: () => import('@/views/ai/chatbot/index.vue'),
        meta: { requiresAuth: true, title: 'AI 助手' },
      },
      // ===== 物联网模块 =====
      {
        path: 'iot/monitor',
        name: 'IoTMonitor',
        component: () => import('@/views/iot/monitor/index.vue'),
        meta: { requiresAuth: true, title: '设备监控' },
      },
      {
        path: 'iot/control',
        name: 'IoTControl',
        component: () => import('@/views/iot/control/index.vue'),
        meta: { requiresAuth: true, title: '远程控制' },
      },
    ],
  },
  {
    path: '/qualification',
    name: 'Qualification',
    component: () => import('@/views/qualification/index.vue'),
    meta: { requiresAuth: false },
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/NotFound.vue'),
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router

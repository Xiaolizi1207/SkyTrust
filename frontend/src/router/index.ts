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
    redirect: '/admin/dashboard',
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
    ],
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

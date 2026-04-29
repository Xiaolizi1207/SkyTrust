<template>
  <view class="page-container">
    <!-- 个人头部 -->
    <view class="profile-header">
      <view class="avatar">
        <text class="avatar-text">{{ initial }}</text>
      </view>
      <view class="user-info">
        <text class="user-name">{{ displayName }}</text>
        <view class="role-tag">
          <text>{{ roleText }}</text>
        </view>
      </view>
    </view>

    <!-- 功能菜单 -->
    <view class="menu-group">
      <view class="menu-item" @click="goOrders">
        <text class="menu-text">我的订单</text>
        <text class="menu-arrow">&gt;</text>
      </view>
      <view class="menu-item" @click="goWallet">
        <text class="menu-text">我的钱包</text>
        <text class="menu-arrow">&gt;</text>
      </view>
    </view>

    <view class="menu-group">
      <view class="menu-item" @click="goSettings">
        <text class="menu-text">设置</text>
        <text class="menu-arrow">&gt;</text>
      </view>
      <view class="menu-item" @click="goAbout">
        <text class="menu-text">关于 SkyTrust</text>
        <text class="menu-arrow">&gt;</text>
      </view>
    </view>

    <!-- 退出登录 -->
    <view class="logout-section">
      <button class="logout-btn" @click="handleLogout">退出登录</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useAuthStore } from '@/store/auth'

const authStore = useAuthStore()

const initial = computed(() => {
  const name = authStore.user?.realName || authStore.user?.username || 'U'
  return name.charAt(0).toUpperCase()
})

const displayName = computed(() => {
  return authStore.user?.realName || authStore.user?.username || '未登录'
})

const roleText = computed(() => {
  const role = authStore.user?.role
  const map: Record<string, string> = {
    admin: '管理员',
    user: '普通用户',
    renter: '租赁用户',
  }
  return map[role || ''] || '用户'
})

// ========== 导航 ==========
function goOrders() {
  uni.switchTab({ url: '/pages/orders/index' })
}

function goWallet() {
  uni.showToast({ title: '钱包功能开发中', icon: 'none' })
}

function goSettings() {
  uni.showToast({ title: '设置功能开发中', icon: 'none' })
}

function goAbout() {
  uni.showModal({
    title: 'SkyTrust',
    content: '无人机租赁平台 v1.0.0\n安全·信任·高效',
    showCancel: false,
  })
}

// ========== 退出登录 ==========
async function handleLogout() {
  const res = await uni.showModal({
    title: '退出登录',
    content: '确定要退出登录吗？',
  })
  if (!res.confirm) return

  await authStore.logout()
  uni.reLaunch({ url: '/pages/login/index' })
}
</script>

<style lang="scss" scoped>
/* 头部 */
.profile-header {
  background: #000;
  padding: 60rpx 32rpx 40rpx;
  display: flex;
  align-items: center;
  gap: 24rpx;
}

.avatar {
  width: 100rpx;
  height: 100rpx;
  border: 3rpx solid #fff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-text {
  font-size: 44rpx;
  font-weight: 800;
  color: #fff;
}

.user-info {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.user-name {
  font-size: 34rpx;
  font-weight: 700;
  color: #fff;
}

.role-tag {
  align-self: flex-start;
  font-size: 20rpx;
  color: #999;
  border: 1rpx solid #666;
  padding: 2rpx 12rpx;
}

/* 菜单组 */
.menu-group {
  background: #fff;
  margin: 16rpx 24rpx;
  border: 2rpx solid #e0e0e0;
}

.menu-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28rpx 24rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-text {
  font-size: 28rpx;
  color: #000;
}

.menu-arrow {
  font-size: 28rpx;
  color: #ccc;
}

/* 退出 */
.logout-section {
  padding: 48rpx 24rpx;
}

.logout-btn {
  width: 100%;
  height: 88rpx;
  border: 2rpx solid #ccc;
  background: #fff;
  color: #999;
  font-size: 28rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>

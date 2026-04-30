<template>
  <view class="settings-page">
    <!-- 资料编辑 -->
    <view class="section">
      <text class="section-title">个人资料</text>
      <view class="form-card">
        <view class="form-item">
          <text class="form-label">用户名</text>
          <text class="form-value">{{ userInfo.username }}</text>
        </view>
        <view class="form-item">
          <text class="form-label">真实姓名</text>
          <input
            v-model="form.realName"
            class="form-input"
            placeholder="请输入真实姓名"
            placeholder-style="color: #ccc"
          />
        </view>
        <view class="form-item">
          <text class="form-label">手机号</text>
          <text class="form-value">{{ userInfo.phone || '未绑定' }}</text>
        </view>
        <view class="form-item">
          <text class="form-label">邮箱</text>
          <input
            v-model="form.email"
            class="form-input"
            placeholder="请输入邮箱"
            placeholder-style="color: #ccc"
          />
        </view>
        <view class="form-item">
          <text class="form-label">信用分</text>
          <text class="form-value">{{ userInfo.creditScore || 0 }}</text>
        </view>
      </view>
      <button class="save-btn" :loading="saving" @click="handleSave">保存修改</button>
    </view>

    <!-- 安全 -->
    <view class="section">
      <text class="section-title">安全</text>
      <view class="menu-card">
        <view class="menu-item" @click="openPasswordModal">
          <text class="menu-text">修改密码</text>
          <text class="menu-arrow">></text>
        </view>
      </view>
    </view>

    <!-- 修改密码弹窗 -->
    <view v-if="showPwdModal" class="modal-mask" @click="showPwdModal = false">
      <view class="modal-content" @click.stop>
        <text class="modal-title">修改密码</text>
        <view class="modal-item">
          <text class="modal-label">旧密码</text>
          <input
            v-model="pwdForm.oldPassword"
            class="modal-input"
            :password="true"
            placeholder="请输入旧密码"
            placeholder-style="color: #999"
          />
        </view>
        <view class="modal-item">
          <text class="modal-label">新密码</text>
          <input
            v-model="pwdForm.newPassword"
            class="modal-input"
            :password="true"
            placeholder="8-20位，含大小写字母和数字"
            placeholder-style="color: #999"
          />
        </view>
        <view class="modal-item">
          <text class="modal-label">确认新密码</text>
          <input
            v-model="pwdForm.confirmPassword"
            class="modal-input"
            :password="true"
            placeholder="再次输入新密码"
            placeholder-style="color: #999"
          />
        </view>
        <text v-if="pwdError" class="error-text">{{ pwdError }}</text>
        <view class="modal-actions">
          <button class="modal-cancel" @click="showPwdModal = false">取消</button>
          <button class="modal-confirm" :loading="changingPwd" @click="handleChangePassword">
            确认修改
          </button>
        </view>
      </view>
    </view>

    <!-- 关于 -->
    <view class="section">
      <text class="section-title">关于</text>
      <view class="about-card">
        <view class="about-row">
          <text class="about-label">应用名称</text>
          <text class="about-value">SkyTrust</text>
        </view>
        <view class="about-row">
          <text class="about-label">版本号</text>
          <text class="about-value">1.0.0</text>
        </view>
        <view class="about-row">
          <text class="about-label">技术栈</text>
          <text class="about-value">uni-app + Vue 3 + Pinia</text>
        </view>
        <view class="about-copyright">
          Copyright © 2026 SkyTrust Team
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useAuthStore } from '@/store/auth'
import { updateUserApi, updatePasswordApi } from '@/api/user'

const authStore = useAuthStore()
const userInfo = ref<any>(authStore.user || {})
const saving = ref(false)

const form = reactive({
  realName: userInfo.value.realName || '',
  email: userInfo.value.email || '',
})

// ========== 保存资料 ==========
async function handleSave() {
  saving.value = true
  try {
    await updateUserApi(userInfo.value.id, {
      realName: form.realName,
      email: form.email,
    })
    uni.showToast({ title: '保存成功', icon: 'success' })
    // 刷新本地用户信息
    authStore.fetchUserInfo()
  } catch (err: any) {
    uni.showToast({ title: err.message || '保存失败', icon: 'none' })
  } finally {
    saving.value = false
  }
}

// ========== 修改密码 ==========
const showPwdModal = ref(false)
const changingPwd = ref(false)
const pwdError = ref('')
const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

function openPasswordModal() {
  pwdForm.oldPassword = ''
  pwdForm.newPassword = ''
  pwdForm.confirmPassword = ''
  pwdError.value = ''
  showPwdModal.value = true
}

async function handleChangePassword() {
  pwdError.value = ''

  if (!pwdForm.oldPassword) {
    pwdError.value = '请输入旧密码'
    return
  }
  if (!/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,20}$/.test(pwdForm.newPassword)) {
    pwdError.value = '新密码需8-20位，含大小写字母和数字'
    return
  }
  if (pwdForm.newPassword !== pwdForm.confirmPassword) {
    pwdError.value = '两次输入的密码不一致'
    return
  }

  changingPwd.value = true
  try {
    await updatePasswordApi(userInfo.value.id, pwdForm.oldPassword, pwdForm.newPassword)
    uni.showToast({ title: '密码修改成功', icon: 'success' })
    showPwdModal.value = false
  } catch (err: any) {
    pwdError.value = err.message || '密码修改失败'
  } finally {
    changingPwd.value = false
  }
}

onLoad(() => {
  // 如果 store 中没有用户信息，尝试从 API 获取
  if (!userInfo.value.id) {
    authStore.fetchUserInfo().then(() => {
      userInfo.value = authStore.user || {}
      form.realName = userInfo.value.realName || ''
      form.email = userInfo.value.email || ''
    })
  }
})
</script>

<style lang="scss" scoped>
.settings-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 48rpx;
}

.section {
  margin-bottom: 24rpx;
}

.section-title {
  font-size: 24rpx;
  color: #999;
  padding: 24rpx 32rpx 12rpx;
  display: block;
}

.form-card,
.about-card {
  background: #fff;
  margin: 0 24rpx;
}

.menu-card {
  background: #fff;
  margin: 0 24rpx;
}

.form-item {
  display: flex;
  align-items: center;
  padding: 24rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.form-label {
  width: 140rpx;
  font-size: 26rpx;
  color: #999;
  flex-shrink: 0;
}

.form-value {
  font-size: 26rpx;
  color: #333;
}

.form-input {
  flex: 1;
  font-size: 26rpx;
  color: #333;
  text-align: right;
}

.save-btn {
  width: calc(100% - 48rpx);
  height: 88rpx;
  background: #000;
  color: #fff;
  font-size: 30rpx;
  font-weight: 600;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 24rpx 24rpx 0;
}

/* 菜单 */
.menu-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28rpx 24rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.menu-text {
  font-size: 28rpx;
  color: #333;
}

.menu-arrow {
  font-size: 24rpx;
  color: #ccc;
}

/* 关于 */
.about-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.about-label {
  font-size: 26rpx;
  color: #999;
}

.about-value {
  font-size: 26rpx;
  color: #333;
}

.about-copyright {
  text-align: center;
  font-size: 22rpx;
  color: #ccc;
  padding: 32rpx 24rpx;
}

/* 弹窗 */
.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  width: 600rpx;
  background: #fff;
  padding: 48rpx 40rpx;
}

.modal-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #000;
  display: block;
  text-align: center;
  margin-bottom: 32rpx;
}

.modal-item {
  margin-bottom: 24rpx;
}

.modal-label {
  font-size: 26rpx;
  color: #000;
  font-weight: 600;
  display: block;
  margin-bottom: 12rpx;
}

.modal-input {
  width: 100%;
  height: 80rpx;
  border: 2rpx solid #ccc;
  padding: 0 20rpx;
  font-size: 28rpx;
  color: #333;
  box-sizing: border-box;
}

.error-text {
  font-size: 24rpx;
  color: #e53e3e;
  display: block;
  margin-bottom: 16rpx;
}

.modal-actions {
  display: flex;
  gap: 16rpx;
  margin-top: 32rpx;
}

.modal-cancel {
  flex: 1;
  height: 80rpx;
  background: #f5f5f5;
  color: #333;
  font-size: 28rpx;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-confirm {
  flex: 2;
  height: 80rpx;
  background: #000;
  color: #fff;
  font-size: 28rpx;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>

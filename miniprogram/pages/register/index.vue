<template>
  <view class="register-page">
    <!-- Logo -->
    <view class="logo-section">
      <text class="logo-text">SkyTrust</text>
      <text class="logo-sub">创建账号</text>
    </view>

    <!-- 注册表单 -->
    <view class="form-section">
      <!-- 用户名 -->
      <view class="input-group">
        <input
          v-model="username"
          class="input-field"
          placeholder="用户名（字母数字，4-20位）"
          placeholder-style="color: #999"
          maxlength="20"
        />
      </view>

      <!-- 手机号 -->
      <view class="input-group">
        <input
          v-model="phone"
          class="input-field"
          type="number"
          maxlength="11"
          placeholder="手机号"
          placeholder-style="color: #999"
        />
      </view>

      <!-- 邮箱（选填） -->
      <view class="input-group">
        <input
          v-model="email"
          class="input-field"
          placeholder="邮箱（选填）"
          placeholder-style="color: #999"
        />
      </view>

      <!-- 密码 -->
      <view class="input-group">
        <input
          v-model="password"
          class="input-field"
          :password="!showPwd"
          placeholder="密码（8-20位，含大小写字母和数字）"
          placeholder-style="color: #999"
          maxlength="20"
        />
        <text class="input-suffix" @click="showPwd = !showPwd">
          {{ showPwd ? '隐藏' : '显示' }}
        </text>
      </view>

      <!-- 确认密码 -->
      <view class="input-group">
        <input
          v-model="confirmPassword"
          class="input-field"
          :password="!showConfirmPwd"
          placeholder="确认密码"
          placeholder-style="color: #999"
          maxlength="20"
        />
        <text class="input-suffix" @click="showConfirmPwd = !showConfirmPwd">
          {{ showConfirmPwd ? '隐藏' : '显示' }}
        </text>
      </view>

      <!-- 错误提示 -->
      <text v-if="errorMsg" class="error-text">{{ errorMsg }}</text>

      <!-- 注册按钮 -->
      <button
        class="register-btn"
        :class="{ 'register-btn--disabled': !canRegister }"
        :disabled="!canRegister || loading"
        :loading="loading"
        @click="handleRegister"
      >
        注册
      </button>
    </view>

    <!-- 跳转登录 -->
    <view class="footer-link">
      <text class="link-text">已有账号？</text>
      <text class="link-action" @click="goLogin">立即登录</text>
    </view>
  </view>
</template>

<script lang="ts">
import { defineComponent, ref, computed } from 'vue'
import { registerApi } from '@/api/auth'
import { useAuthStore } from '@/store/auth'

export default defineComponent({
  setup() {
    const authStore = useAuthStore()

    const username = ref('')
    const phone = ref('')
    const email = ref('')
    const password = ref('')
    const confirmPassword = ref('')
    const showPwd = ref(false)
    const showConfirmPwd = ref(false)
    const loading = ref(false)
    const errorMsg = ref('')

    const validUsername = computed(() => /^[a-zA-Z0-9]{4,20}$/.test(username.value))
    const validPhone = computed(() => /^1[3-9]\d{9}$/.test(phone.value))
    const validPassword = computed(() => /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,20}$/.test(password.value))
    const pwdMatch = computed(() => password.value === confirmPassword.value)

    const canRegister = computed(() =>
      validUsername.value && validPhone.value && validPassword.value && pwdMatch.value,
    )

    async function handleRegister() {
      if (!canRegister.value) return
      loading.value = true
      errorMsg.value = ''
      try {
        const res = await registerApi({
          username: username.value.trim(),
          password: password.value,
          confirmPassword: confirmPassword.value,
          phone: phone.value.trim(),
          email: email.value.trim() || undefined,
        })
        const data = res.data.data as any
        const { accessToken, refreshToken, user: userInfo } = data
        authStore.saveTokens(accessToken, refreshToken)
        authStore.saveUser(userInfo)
        uni.showToast({ title: '注册成功', icon: 'success' })
        setTimeout(() => {
          uni.switchTab({ url: '/pages/index/index' })
        }, 800)
      } catch (err: any) {
        errorMsg.value = err.message || '注册失败，请稍后重试'
      } finally {
        loading.value = false
      }
    }

    function goLogin() {
      uni.navigateBack()
    }

    return {
      username,
      phone,
      email,
      password,
      confirmPassword,
      showPwd,
      showConfirmPwd,
      loading,
      errorMsg,
      validUsername,
      validPhone,
      validPassword,
      pwdMatch,
      canRegister,
      handleRegister,
      goLogin,
    }
  },
})
</script>

<style lang="scss" scoped>
.register-page {
  min-height: 100vh;
  background: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80rpx 48rpx 48rpx;
  box-sizing: border-box;
}

/* Logo */
.logo-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 64rpx;
}

.logo-text {
  font-size: 56rpx;
  font-weight: 900;
  color: #000;
  letter-spacing: 8rpx;
}

.logo-sub {
  font-size: 26rpx;
  color: #999;
  margin-top: 8rpx;
}

/* 表单 */
.form-section {
  width: 100%;
}

.input-group {
  width: 100%;
  position: relative;
  margin-bottom: 24rpx;
}

.input-field {
  width: 100%;
  height: 88rpx;
  border: 2rpx solid #ccc;
  padding: 0 24rpx;
  font-size: 28rpx;
  color: #333;
  box-sizing: border-box;
  background: #fff;
}

.input-field:focus {
  border-color: #000;
}

.input-suffix {
  position: absolute;
  right: 24rpx;
  top: 50%;
  transform: translateY(-50%);
  font-size: 24rpx;
  color: #999;
}

/* 错误提示 */
.error-text {
  font-size: 24rpx;
  color: #e53e3e;
  display: block;
  margin-bottom: 16rpx;
}

/* 注册按钮 */
.register-btn {
  width: 100%;
  height: 88rpx;
  background: #000;
  color: #fff;
  font-size: 30rpx;
  font-weight: 600;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 16rpx;
}

.register-btn--disabled {
  background: #e0e0e0;
  color: #999;
}

.register-btn::after {
  border: none;
}

/* 底部链接 */
.footer-link {
  margin-top: 40rpx;
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.link-text {
  font-size: 26rpx;
  color: #999;
}

.link-action {
  font-size: 26rpx;
  color: #000;
  font-weight: 600;
}
</style>

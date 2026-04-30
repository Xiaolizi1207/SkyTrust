<template>
  <view class="login-page">
    <!-- Logo 区域 -->
    <view class="logo-section">
      <text class="logo-text">SkyTrust</text>
      <text class="logo-sub">无人机租赁平台</text>
    </view>

    <!-- 微信一键登录（主） -->
    <view class="login-section">
      <view class="section-title">
        <view class="title-line" />
        <text class="title-text">微信授权登录</text>
        <view class="title-line" />
      </view>

      <button
        class="wechat-login-btn"
        :loading="wechatLoading"
        :disabled="wechatLoading"
        @click="handleWechatLogin"
      >
        <text class="wechat-icon">&#xe600;</text>
        <text>微信一键登录</text>
      </button>

      <text v-if="wechatError" class="error-text">{{ wechatError }}</text>
    </view>

    <!-- 微信手机号授权（单独按钮） -->
    <view class="login-section">
      <button
        class="phone-auth-btn"
        open-type="getPhoneNumber"
        @getphonenumber="handleGetPhoneNumber"
        :loading="phoneAuthLoading"
        :disabled="phoneAuthLoading"
      >
        <text>微信手机号快速登录</text>
      </button>
    </view>

    <!-- 分隔 -->
    <view class="divider-section">
      <view class="divider-line" />
      <text class="divider-text">其他方式登录</text>
      <view class="divider-line" />
    </view>

    <!-- 手机号验证码登录（兜底） -->
    <view class="login-section">
      <view class="input-group">
        <input
          v-model="phone"
          class="input-field"
          type="number"
          maxlength="11"
          placeholder="请输入手机号"
        />
      </view>

      <view class="input-group code-row">
        <input
          v-model="code"
          class="input-field code-input"
          type="number"
          maxlength="6"
          placeholder="验证码"
        />
        <button
          class="code-btn"
          :class="{ 'code-btn--disabled': countdown > 0 }"
          :disabled="countdown > 0 || !validPhone"
          @click="sendCode"
        >
          {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
        </button>
      </view>

      <button
        class="phone-login-btn"
        :disabled="!canLogin || codeLoading"
        :loading="codeLoading"
        @click="handleCodeLogin"
      >
        登录
      </button>
    </view>

    <!-- 协议 -->
    <view class="agreement">
      <text class="agreement-text">
        登录即表示同意《用户协议》和《隐私政策》
      </text>
    </view>

    <!-- 注册 / 忘记密码 -->
    <view class="footer-links">
      <text class="footer-link" @click="goRegister">注册账号</text>
      <text class="footer-divider">|</text>
      <text class="footer-link" @click="goForgotPassword">忘记密码</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useAuthStore } from '@/store/auth'
import { sendCodeApi, codeLoginApi } from '@/api/auth'

const authStore = useAuthStore()

// ========== 微信登录状态 ==========
const wechatLoading = ref(false)
const wechatError = ref('')

// ========== 微信手机号授权状态 ==========
const phoneAuthLoading = ref(false)

// ========== 手机号+验证码登录状态 ==========
const phone = ref('')
const code = ref('')
const codeLoading = ref(false)
const countdown = ref(0)
let timer: ReturnType<typeof setInterval> | null = null

const validPhone = computed(() => /^1[3-9]\d{9}$/.test(phone.value))
const canLogin = computed(() => validPhone.value && code.value.length >= 4)

// ========== 微信一键登录 ==========
async function handleWechatLogin() {
  wechatLoading.value = true
  wechatError.value = ''

  try {
    const loginRes = await uni.login({ provider: 'weixin' })
    if (!loginRes.code) {
      throw new Error('获取微信授权失败，请确认已配置微信 AppID')
    }
    await authStore.wechatLogin({ code: loginRes.code })
    uni.switchTab({ url: '/pages/index/index' })
  } catch (err: any) {
    const msg = err.message || ''
    if (msg.includes('login:fail') || msg.includes('not exist')) {
      wechatError.value = '微信登录需要在微信开发者工具中配置 AppID，请使用手机号验证码登录'
    } else {
      wechatError.value = msg || '微信登录失败，请使用手机号登录'
    }
  } finally {
    wechatLoading.value = false
  }
}

// ========== 手机号验证码登录 ==========
async function sendCode() {
  if (!validPhone.value || countdown.value > 0) return

  try {
    await sendCodeApi({ phone: phone.value })

    // 倒计时
    countdown.value = 60
    timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0 && timer) {
        clearInterval(timer)
        timer = null
      }
    }, 1000)

    uni.showToast({ title: '验证码已发送', icon: 'none' })
  } catch (err: any) {
    uni.showToast({ title: err.message || '发送失败', icon: 'none' })
  }
}

async function handleCodeLogin() {
  if (!canLogin.value) return

  codeLoading.value = true
  try {
    const res = await codeLoginApi({ phone: phone.value, code: code.value })
    const data = res.data.data as any
    const { accessToken, refreshToken, user: userInfo } = data
    authStore.saveTokens(accessToken, refreshToken)
    authStore.saveUser(userInfo)

    uni.switchTab({ url: '/pages/index/index' })
  } catch (err: any) {
    uni.showToast({ title: err.message || '登录失败', icon: 'none' })
  } finally {
    codeLoading.value = false
  }
}

// ========== 微信手机号授权 ==========
async function handleGetPhoneNumber(e: any) {
  if (e.detail.errMsg !== 'getPhoneNumber:ok') {
    uni.showToast({ title: '授权已取消', icon: 'none' })
    return
  }

  phoneAuthLoading.value = true
  try {
    // 1. 先获取微信登录 code
    const loginRes = await uni.login({ provider: 'weixin' })
    if (!loginRes.code) throw new Error('获取微信授权失败')

    // 2. 用 code 换取 openid/session_key，同时传入手机号加密数据
    await authStore.wechatLogin({
      code: loginRes.code,
      encryptedData: e.detail.encryptedData,
      iv: e.detail.iv,
    })

    uni.switchTab({ url: '/pages/index/index' })
  } catch (err: any) {
    uni.showToast({ title: err.message || '登录失败', icon: 'none' })
  } finally {
    phoneAuthLoading.value = false
  }
}

// ========== 导航 ==========
function goRegister() {
  uni.navigateTo({ url: '/pages/register/index' })
}

function goForgotPassword() {
  uni.navigateTo({ url: '/pages/forgot-password/index' })
}
</script>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  background: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 48rpx 48rpx;
  box-sizing: border-box;
}

/* Logo */
.logo-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 80rpx;
}

.logo-text {
  font-size: 64rpx;
  font-weight: 900;
  color: #000;
  letter-spacing: 8rpx;
}

.logo-sub {
  font-size: 26rpx;
  color: #999;
  margin-top: 12rpx;
}

/* 分区标题 */
.login-section {
  width: 100%;
  margin-bottom: 48rpx;
}

.section-title {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 40rpx;
}

.title-line {
  width: 60rpx;
  height: 2rpx;
  background: #e0e0e0;
}

.title-text {
  font-size: 26rpx;
  color: #999;
  margin: 0 20rpx;
}

/* 微信登录按钮 */
.wechat-login-btn {
  width: 100%;
  height: 96rpx;
  background: #000;
  color: #fff;
  font-size: 32rpx;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
}

.wechat-icon {
  margin-right: 12rpx;
  font-size: 36rpx;
}

/* 分隔线 */
.divider-section {
  width: 100%;
  display: flex;
  align-items: center;
  margin-bottom: 48rpx;
}

.divider-line {
  flex: 1;
  height: 2rpx;
  background: #e0e0e0;
}

.divider-text {
  font-size: 24rpx;
  color: #ccc;
  margin: 0 20rpx;
}

/* 输入框 */
.input-group {
  width: 100%;
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

/* 验证码行 */
.code-row {
  display: flex;
  gap: 16rpx;
}

.code-input {
  flex: 1;
}

.code-btn {
  width: 200rpx;
  height: 88rpx;
  font-size: 26rpx;
  background: #fff;
  color: #000;
  border: 2rpx solid #000;
  display: flex;
  align-items: center;
  justify-content: center;
  white-space: nowrap;
  padding: 0 12rpx;
}

.code-btn--disabled {
  border-color: #ccc;
  color: #999;
}

.code-btn::after {
  border: none;
}

/* 手机号登录按钮 */
.phone-login-btn {
  width: 100%;
  height: 88rpx;
  background: #fff;
  color: #000;
  border: 2rpx solid #000;
  font-size: 30rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 8rpx;
}

.phone-login-btn[disabled] {
  border-color: #ccc;
  color: #999;
}

/* 错误提示 */
.error-text {
  font-size: 24rpx;
  color: #e53e3e;
  margin-top: 16rpx;
  text-align: center;
  display: block;
}

/* 协议 */
.agreement {
  margin-top: 48rpx;
}

.agreement-text {
  font-size: 22rpx;
  color: #ccc;
}

/* 手机号授权按钮 */
.phone-auth-btn {
  width: 100%;
  height: 80rpx;
  background: #fff;
  color: #000;
  font-size: 28rpx;
  border: 2rpx solid #000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.phone-auth-btn::after {
  border: none;
}

/* 底部链接 */
.footer-links {
  margin-top: 36rpx;
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.footer-link {
  font-size: 26rpx;
  color: #000;
  font-weight: 600;
}

.footer-divider {
  font-size: 22rpx;
  color: #ccc;
}
</style>

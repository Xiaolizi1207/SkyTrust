<template>
  <div class="login-page">
    <div class="login-card">
      <h2 class="title">SkyTrust 无人机共享租赁平台</h2>

      <!-- 错误提示 -->
      <div v-if="errorMsg" class="error-msg">{{ errorMsg }}</div>

      <!-- 登录方式 Tab -->
      <div class="tab-bar">
        <span
          class="tab-item"
          :class="{ active: loginMode === 'password' }"
          @click="loginMode = 'password'"
        >密码登录</span>
        <span
          class="tab-item"
          :class="{ active: loginMode === 'code' }"
          @click="loginMode = 'code'"
        >验证码登录</span>
      </div>

      <!-- ========== 密码登录 ========== -->
      <form v-if="loginMode === 'password'" @submit.prevent="handlePasswordLogin">
        <!-- 用户名 -->
        <div class="form-group">
          <label>用户名 / 手机号 / 邮箱</label>
          <input
            v-model="passwordForm.username"
            type="text"
            placeholder="请输入用户名、手机号或邮箱"
            :disabled="loading"
            autocomplete="username"
          />
        </div>

        <!-- 密码 -->
        <div class="form-group">
          <label>密码</label>
          <input
            v-model="passwordForm.password"
            type="password"
            placeholder="请输入密码"
            :disabled="loading"
            autocomplete="current-password"
          />
        </div>

        <!-- 验证码 -->
        <div v-if="captchaData.captchaKey" class="form-group captcha-row">
          <label>验证码</label>
          <div class="captcha-input-row">
            <input
              v-model="passwordForm.captchaCode"
              type="text"
              placeholder="验证码"
              maxlength="4"
              :disabled="loading"
            />
            <img
              :src="captchaData.captchaImage"
              class="captcha-img"
              alt="验证码"
              title="点击刷新"
              @click="getCaptcha"
            />
          </div>
        </div>

        <!-- 选项 -->
        <div class="options">
          <label class="remember">
            <input v-model="rememberMe" type="checkbox" />
            记住我
          </label>
          <a href="#" class="forgot" @click.prevent="router.push('/forgot-password')">忘记密码？</a>
        </div>

        <!-- 提交按钮 -->
        <button type="submit" class="btn-login" :disabled="loading">
          {{ loading ? '登录中...' : '登 录' }}
        </button>

        <!-- 注册入口 -->
        <p class="register-link">
          还没有账号？<router-link to="/register">立即注册</router-link>
        </p>
      </form>

      <!-- ========== 验证码登录 ========== -->
      <form v-else @submit.prevent="handleCodeLogin">
        <!-- 手机号 / 邮箱 -->
        <div class="form-group">
          <label>手机号 / 邮箱</label>
          <input
            v-model="codeForm.account"
            type="text"
            placeholder="请输入手机号或邮箱"
            :disabled="loading"
            autocomplete="username"
          />
        </div>

        <!-- 验证码 -->
        <div class="form-group">
          <label>验证码</label>
          <div class="code-input-row">
            <input
              v-model="codeForm.code"
              type="text"
              placeholder="请输入验证码"
              maxlength="6"
              :disabled="loading"
            />
            <button
              type="button"
              class="btn-send-code"
              :disabled="codeSending || codeCountdown > 0 || !codeForm.account.trim()"
              @click="handleSendCode"
            >
              {{ codeCountdown > 0 ? `${codeCountdown}s` : (codeSending ? '发送中...' : '获取验证码') }}
            </button>
          </div>
        </div>

        <!-- 提交按钮 -->
        <button type="submit" class="btn-login" :disabled="loading || !codeForm.code">
          {{ loading ? '登录中...' : '登 录' }}
        </button>

        <!-- 注册入口 -->
        <p class="register-link">
          还没有账号？<router-link to="/register">立即注册</router-link>
        </p>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { getCaptchaApi, sendCodeApi, codeLoginApi } from '@/api/auth'
import type { CaptchaResult } from '@/types/api'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

// ========== 登录方式切换 ==========
const loginMode = ref<'password' | 'code'>('password')

// ========== 密码登录表单 ==========
const passwordForm = reactive({
  username: '',
  password: '',
  captchaCode: '',
})
const rememberMe = ref(localStorage.getItem('rememberMe') === 'true')

// ========== 验证码登录表单 ==========
const codeForm = reactive({
  account: '',
  code: '',
})

// ========== 公共状态 ==========
const loading = ref(false)
const errorMsg = ref('')
const codeSending = ref(false)
const codeCountdown = ref(0)
let countdownTimer: ReturnType<typeof setInterval> | null = null

const captchaData = reactive<CaptchaResult>({
  captchaKey: '',
  captchaImage: '',
})

// ========== 切换 tab 时重置 ==========
watch(loginMode, () => {
  errorMsg.value = ''
  // 切换到密码登录时，自动获取图形验证码
  if (loginMode.value === 'password' && !captchaData.captchaKey) {
    getCaptcha()
  }
})

// ========== 方法 ==========

/** 获取图形验证码 */
async function getCaptcha() {
  try {
    errorMsg.value = ''
    const res = await getCaptchaApi()
    const data = res.data.data
    captchaData.captchaKey = data.captchaKey
    captchaData.captchaImage = data.captchaImage
    passwordForm.captchaCode = ''
  } catch (err: any) {
    captchaData.captchaKey = ''
    captchaData.captchaImage = ''
    errorMsg.value = '获取验证码失败，请稍后重试'
  }
}

/** 密码登录 */
async function handlePasswordLogin() {
  errorMsg.value = ''

  // 前端校验
  if (!passwordForm.username.trim()) {
    errorMsg.value = '请输入用户名、手机号或邮箱'
    return
  }
  if (!passwordForm.password) {
    errorMsg.value = '请输入密码'
    return
  }

  loading.value = true
  try {
    const params: any = {
      username: passwordForm.username.trim(),
      password: passwordForm.password,
    }
    if (captchaData.captchaKey) {
      params.captchaKey = captchaData.captchaKey
      params.captchaCode = passwordForm.captchaCode
    }

    await authStore.login(params)

    // 记住我
    if (rememberMe.value) {
      localStorage.setItem('rememberMe', 'true')
      localStorage.setItem('savedUsername', passwordForm.username.trim())
    } else {
      localStorage.removeItem('rememberMe')
      localStorage.removeItem('savedUsername')
    }

    const redirect = (route.query.redirect as string) || '/admin/dashboard'
    router.replace(redirect)
  } catch (err: any) {
    getCaptcha()
    errorMsg.value = err?.response?.data?.message || err.message || '登录失败'
  } finally {
    loading.value = false
  }
}

/** 发送验证码 */
async function handleSendCode() {
  const account = codeForm.account.trim()
  if (!account) {
    errorMsg.value = '请输入手机号或邮箱'
    return
  }

  errorMsg.value = ''
  codeSending.value = true

  try {
    // 判断是手机号还是邮箱
    const isPhone = /^1[3-9]\d{9}$/.test(account)
    const params = isPhone ? { phone: account } : { email: account }

    await sendCodeApi(params)
    errorMsg.value = '' // 清空可能的上次错误
    startCountdown()
  } catch (err: any) {
    errorMsg.value = err?.response?.data?.message || err.message || '验证码发送失败'
  } finally {
    codeSending.value = false
  }
}

/** 验证码登录 */
async function handleCodeLogin() {
  const account = codeForm.account.trim()
  if (!account) {
    errorMsg.value = '请输入手机号或邮箱'
    return
  }
  if (!codeForm.code) {
    errorMsg.value = '请输入验证码'
    return
  }

  errorMsg.value = ''
  loading.value = true

  try {
    const isPhone = /^1[3-9]\d{9}$/.test(account)
    const params: any = { code: codeForm.code }
    if (isPhone) {
      params.phone = account
    } else {
      params.email = account
    }

    const res = await codeLoginApi(params)
    const { accessToken, refreshToken, user } = res.data.data

    // 通过 authStore 保存令牌和用户信息（同时更新state和localStorage）
    authStore.setRefreshedTokens(accessToken, refreshToken)
    authStore.saveUser(user)

    const redirect = (route.query.redirect as string) || '/admin/dashboard'
    router.replace(redirect)
  } catch (err: any) {
    errorMsg.value = err?.response?.data?.message || err.message || '登录失败'
  } finally {
    loading.value = false
  }
}

/** 验证码发送倒计时 */
function startCountdown() {
  codeCountdown.value = 60
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }
  countdownTimer = setInterval(() => {
    codeCountdown.value--
    if (codeCountdown.value <= 0) {
      codeCountdown.value = 0
      if (countdownTimer) {
        clearInterval(countdownTimer)
        countdownTimer = null
      }
    }
  }, 1000)
}

// ========== 初始化 ==========
onMounted(() => {
  // 密码登录：记住用户名
  if (rememberMe.value) {
    passwordForm.username = localStorage.getItem('savedUsername') || ''
  }

  // 获取图形验证码
  getCaptcha()
})
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1a73e8 0%, #0d47a1 100%);
}
.login-card {
  width: 400px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
}
.title {
  text-align: center;
  color: #1a73e8;
  margin-bottom: 24px;
  font-size: 20px;
}

/* Tab 栏 */
.tab-bar {
  display: flex;
  border-bottom: 2px solid #e5e7eb;
  margin-bottom: 24px;
}
.tab-item {
  flex: 1;
  text-align: center;
  padding: 10px 0;
  font-size: 15px;
  color: #666;
  cursor: pointer;
  transition: color 0.2s, border-color 0.2s;
  border-bottom: 2px solid transparent;
  margin-bottom: -2px;
  user-select: none;
}
.tab-item:hover {
  color: #1a73e8;
}
.tab-item.active {
  color: #1a73e8;
  border-bottom-color: #1a73e8;
  font-weight: 600;
}

/* 错误提示 */
.error-msg {
  background: #fef2f2;
  color: #dc2626;
  padding: 10px 14px;
  border-radius: 6px;
  margin-bottom: 16px;
  font-size: 14px;
}
.form-group {
  margin-bottom: 20px;
}
.form-group label {
  display: block;
  margin-bottom: 6px;
  font-size: 14px;
  color: #333;
  font-weight: 500;
}
.form-group input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
  outline: none;
  box-sizing: border-box;
  transition: border-color 0.2s;
}
.form-group input:focus {
  border-color: #1a73e8;
  box-shadow: 0 0 0 2px rgba(26, 115, 232, 0.15);
}

/* 图形验证码 */
.captcha-input-row {
  display: flex;
  gap: 10px;
}
.captcha-input-row input {
  flex: 1;
}
.captcha-img {
  width: 120px;
  height: 40px;
  border-radius: 6px;
  cursor: pointer;
  border: 1px solid #d1d5db;
}

/* 验证码输入行（短信验证码） */
.code-input-row {
  display: flex;
  gap: 10px;
}
.code-input-row input {
  flex: 1;
}
.btn-send-code {
  flex-shrink: 0;
  padding: 10px 14px;
  background: #1a73e8;
  color: #fff;
  border: none;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
  white-space: nowrap;
  transition: background 0.2s;
}
.btn-send-code:hover:not(:disabled) {
  background: #1557b0;
}
.btn-send-code:disabled {
  background: #93c5fd;
  cursor: not-allowed;
}

/* 选项行 */
.options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  font-size: 14px;
}
.remember {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #666;
  cursor: pointer;
}
.forgot {
  color: #1a73e8;
  text-decoration: none;
}
.forgot:hover {
  text-decoration: underline;
}

/* 登录按钮 */
.btn-login {
  width: 100%;
  padding: 12px;
  background: #1a73e8;
  color: #fff;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  cursor: pointer;
  transition: background 0.2s;
}
.btn-login:hover:not(:disabled) {
  background: #1557b0;
}
.btn-login:disabled {
  background: #93c5fd;
  cursor: not-allowed;
}

/* 注册/入口链接 */
.register-link {
  text-align: center;
  margin-top: 16px;
  font-size: 14px;
  color: #666;
}
.register-link a {
  color: #1a73e8;
  text-decoration: none;
}
.register-link a:hover {
  text-decoration: underline;
}
</style>

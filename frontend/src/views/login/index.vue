<template>
  <div class="login-page">
    <div class="login-card">
      <h2 class="title">SkyTrust 无人机共享租赁平台</h2>

      <!-- 错误提示 -->
      <div v-if="errorMsg" class="error-msg">{{ errorMsg }}</div>

      <form @submit.prevent="handleLogin">
        <!-- 用户名 -->
        <div class="form-group">
          <label>用户名 / 手机号 / 邮箱</label>
          <input
            v-model="form.username"
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
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            :disabled="loading"
            autocomplete="current-password"
          />
        </div>

        <!-- 验证码：captchaKey 存在时显示 -->
        <div v-if="captchaData.captchaKey" class="form-group captcha-row">
          <label>验证码</label>
          <div class="captcha-input-row">
            <input
              v-model="form.captchaCode"
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
          还没有账号？<a href="#" @click.prevent="router.push('/register')">立即注册</a>
        </p>

        <!-- 获取验证码入口 -->
        <p class="captcha-trigger">
          <a href="#" @click.prevent="getCaptcha">获取验证码</a>
        </p>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { getCaptchaApi } from '@/api/auth'
import type { CaptchaResult } from '@/types/api'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

// ========== 状态 ==========
const form = reactive({
  username: '',
  password: '',
  captchaCode: '',
})
const rememberMe = ref(localStorage.getItem('rememberMe') === 'true')
const loading = ref(false)
const errorMsg = ref('')
const captchaData = reactive<CaptchaResult>({
  captchaKey: '',
  captchaImage: '',
})

// ========== 方法 ==========

/** 获取验证码 */
async function getCaptcha() {
  try {
    errorMsg.value = ''
    const res = await getCaptchaApi()
    const data = res.data.data
    captchaData.captchaKey = data.captchaKey
    captchaData.captchaImage = data.captchaImage
    form.captchaCode = ''
  } catch (err: any) {
    captchaData.captchaKey = ''
    captchaData.captchaImage = ''
    errorMsg.value = '获取验证码失败，请稍后重试'
  }
}

/** 登录 */
async function handleLogin() {
  errorMsg.value = ''

  // 前端校验
  if (!form.username.trim()) {
    errorMsg.value = '请输入用户名、手机号或邮箱'
    return
  }
  if (!form.password) {
    errorMsg.value = '请输入密码'
    return
  }

  loading.value = true
  try {
    const params: any = {
      username: form.username.trim(),
      password: form.password,
    }
    // 如果已有验证码，一起提交
    if (captchaData.captchaKey) {
      params.captchaKey = captchaData.captchaKey
      params.captchaCode = form.captchaCode
    }

    await authStore.login(params)

    // 记住我：保存用户名到 localStorage
    if (rememberMe.value) {
      localStorage.setItem('rememberMe', 'true')
      localStorage.setItem('savedUsername', form.username.trim())
    } else {
      localStorage.removeItem('rememberMe')
      localStorage.removeItem('savedUsername')
    }

    // 跳转
    const redirect = (route.query.redirect as string) || '/dashboard'
    router.replace(redirect)
  } catch (err: any) {
    errorMsg.value = err?.response?.data?.message || err.message || '登录失败'

    // 登录失败后自动获取验证码
    getCaptcha()
  } finally {
    loading.value = false
  }
}

// ========== 初始化 ==========
onMounted(() => {
  // 记住用户名
  if (rememberMe.value) {
    form.username = localStorage.getItem('savedUsername') || ''
  }
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
  margin-bottom: 30px;
  font-size: 20px;
}
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
.register-link,
.captcha-trigger {
  text-align: center;
  margin-top: 16px;
  font-size: 14px;
  color: #666;
}
.register-link a,
.captcha-trigger a {
  color: #1a73e8;
  text-decoration: none;
}
.register-link a:hover,
.captcha-trigger a:hover {
  text-decoration: underline;
}
.captcha-trigger {
  margin-top: 8px;
}
</style>

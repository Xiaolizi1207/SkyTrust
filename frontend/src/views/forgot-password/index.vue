<template>
  <div class="forgot-page">
    <div class="forgot-card">
      <h2 class="title">{{ step === 1 ? '找回密码' : '重置密码' }}</h2>

      <!-- 错误提示 -->
      <div v-if="errorMsg" class="error-msg">{{ errorMsg }}</div>

      <!-- 成功提示 -->
      <div v-if="successMsg" class="success-msg">{{ successMsg }}</div>

      <!-- ========== 步骤 1：输入邮箱发送验证码 ========== -->
      <form v-if="step === 1" @submit.prevent="handleSendCode">
        <div class="form-group">
          <label>注册邮箱</label>
          <input
            v-model="email"
            type="email"
            placeholder="请输入注册时使用的邮箱"
            :disabled="sending"
            autocomplete="email"
          />
        </div>

        <button type="submit" class="btn-submit" :disabled="sending || !email.trim()">
          {{ sending ? '发送中...' : '发送重置验证码' }}
        </button>

        <p class="back-link">
          <router-link to="/login">返回登录</router-link>
        </p>
      </form>

      <!-- ========== 步骤 2：验证码 + 新密码 ========== -->
      <form v-else @submit.prevent="handleResetPassword">
        <div class="form-group">
          <label>邮箱</label>
          <input :value="email" type="email" disabled class="input-disabled" />
        </div>

        <!-- 验证码 -->
        <div class="form-group">
          <label>验证码</label>
          <div class="code-input-row">
            <input
              v-model="code"
              type="text"
              placeholder="请输入验证码"
              maxlength="6"
              :disabled="resetting"
            />
            <button
              type="button"
              class="btn-send-code"
              :disabled="codeSending || codeCountdown > 0"
              @click="handleSendCode"
            >
              {{ codeCountdown > 0 ? `${codeCountdown}s` : (codeSending ? '发送中...' : '重新发送') }}
            </button>
          </div>
        </div>

        <!-- 新密码 -->
        <div class="form-group">
          <label>新密码</label>
          <input
            v-model="newPassword"
            type="password"
            placeholder="请输入新密码"
            :disabled="resetting"
            autocomplete="new-password"
          />
        </div>

        <!-- 确认密码 -->
        <div class="form-group">
          <label>确认密码</label>
          <input
            v-model="confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            :disabled="resetting"
            autocomplete="new-password"
          />
        </div>

        <button type="submit" class="btn-submit" :disabled="resetting || !code || !newPassword || !confirmPassword">
          {{ resetting ? '重置中...' : '重置密码' }}
        </button>

        <p class="back-link">
          <router-link to="/login">返回登录</router-link>
        </p>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { forgotPasswordApi, resetPasswordApi } from '@/api/auth'

const router = useRouter()

// ========== 状态 ==========
const step = ref(1)
const email = ref('')
const code = ref('')
const newPassword = ref('')
const confirmPassword = ref('')
const errorMsg = ref('')
const successMsg = ref('')
const sending = ref(false)
const resetting = ref(false)
const codeSending = ref(false)
const codeCountdown = ref(0)
let countdownTimer: ReturnType<typeof setInterval> | null = null

// ========== 发送重置验证码 ==========
async function handleSendCode() {
  if (!email.value.trim()) {
    errorMsg.value = '请输入邮箱'
    return
  }

  errorMsg.value = ''
  successMsg.value = ''
  sending.value = true
  codeSending.value = true

  try {
    await forgotPasswordApi(email.value.trim())
    // 步骤1发送成功后，进入步骤2
    if (step.value === 1) {
      step.value = 2
    }
    errorMsg.value = ''
    startCountdown()
  } catch (err: any) {
    errorMsg.value = err?.response?.data?.message || err.message || '验证码发送失败'
  } finally {
    sending.value = false
    codeSending.value = false
  }
}

// ========== 重置密码 ==========
async function handleResetPassword() {
  errorMsg.value = ''
  successMsg.value = ''

  // 前端校验
  if (!email.value.trim()) {
    errorMsg.value = '邮箱不能为空'
    return
  }
  if (!code.value) {
    errorMsg.value = '请输入验证码'
    return
  }
  if (!newPassword.value) {
    errorMsg.value = '请输入新密码'
    return
  }
  if (newPassword.value !== confirmPassword.value) {
    errorMsg.value = '两次输入的密码不一致'
    return
  }

  resetting.value = true
  try {
    await resetPasswordApi({
      email: email.value.trim(),
      code: code.value,
      newPassword: newPassword.value,
      confirmPassword: confirmPassword.value,
    })
    successMsg.value = '密码重置成功，即将跳转到登录页...'
    setTimeout(() => {
      router.replace('/login')
    }, 2000)
  } catch (err: any) {
    errorMsg.value = err?.response?.data?.message || err.message || '密码重置失败'
  } finally {
    resetting.value = false
  }
}

// ========== 验证码倒计时 ==========
function startCountdown() {
  codeCountdown.value = 60
  if (countdownTimer) clearInterval(countdownTimer)
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
</script>

<style scoped>
.forgot-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1a73e8 0%, #0d47a1 100%);
}
.forgot-card {
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
.error-msg {
  background: #fef2f2;
  color: #dc2626;
  padding: 10px 14px;
  border-radius: 6px;
  margin-bottom: 16px;
  font-size: 14px;
}
.success-msg {
  background: #f0fdf4;
  color: #16a34a;
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
.input-disabled {
  background: #f3f4f6;
  color: #6b7280;
  cursor: not-allowed;
}
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
.btn-submit {
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
.btn-submit:hover:not(:disabled) {
  background: #1557b0;
}
.btn-submit:disabled {
  background: #93c5fd;
  cursor: not-allowed;
}
.back-link {
  text-align: center;
  margin-top: 16px;
  font-size: 14px;
  color: #666;
}
.back-link a {
  color: #1a73e8;
  text-decoration: none;
}
.back-link a:hover {
  text-decoration: underline;
}
</style>

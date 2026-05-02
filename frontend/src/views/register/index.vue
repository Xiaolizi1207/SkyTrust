<template>
  <div class="register-page">
    <div class="register-card">
      <h2 class="title">企业管理员注册</h2>

      <!-- 错误提示 -->
      <div v-if="errorMsg" class="error-msg">{{ errorMsg }}</div>

      <!-- 成功提示 -->
      <div v-if="successMsg" class="success-msg">{{ successMsg }}</div>

      <form @submit.prevent="handleRegister">
        <!-- 邀请码 -->
        <div class="form-group">
          <label>邀请码 <span class="required">*</span></label>
          <input
            v-model="form.inviteCode"
            type="text"
            placeholder="请输入企业邀请码"
            :disabled="loading"
          />
          <span class="hint">邀请码由企业上级管理者审核资质通过后发放，一个邀请码只能注册一个账号</span>
        </div>
        <!-- 用户名 -->
        <div class="form-group">
          <label>用户名</label>
          <input
            v-model="form.username"
            type="text"
            placeholder="3-50位字母、数字或下划线"
            :disabled="loading"
            autocomplete="username"
          />
        </div>

        <!-- 手机号 -->
        <div class="form-group">
          <label>手机号</label>
          <input
            v-model="form.phone"
            type="text"
            placeholder="请输入手机号"
            :disabled="loading"
            autocomplete="tel"
          />
        </div>

        <!-- 邮箱 -->
        <div class="form-group">
          <label>邮箱（选填）</label>
          <input
            v-model="form.email"
            type="email"
            placeholder="请输入邮箱"
            :disabled="loading"
            autocomplete="email"
          />
        </div>

        <!-- 密码 -->
        <div class="form-group">
          <label>密码</label>
          <input
            v-model="form.password"
            type="password"
            placeholder="至少8位，包含大写、小写、数字"
            :disabled="loading"
            autocomplete="new-password"
          />
          <div class="password-rules">
            <span :class="{ pass: pwRule.length }">✓ 至少8位字符</span>
            <span :class="{ pass: pwRule.upper }">✓ 至少1个大写字母</span>
            <span :class="{ pass: pwRule.lower }">✓ 至少1个小写字母</span>
            <span :class="{ pass: pwRule.digit }">✓ 至少1个数字</span>
          </div>
        </div>

        <!-- 确认密码 -->
        <div class="form-group">
          <label>确认密码</label>
          <input
            v-model="form.confirmPassword"
            type="password"
            placeholder="再次输入密码"
            :disabled="loading"
            autocomplete="new-password"
          />
        </div>

        <!-- 提交按钮 -->
        <button type="submit" class="btn-register" :disabled="loading">
          {{ loading ? '注册中...' : '注 册' }}
        </button>

        <!-- 返回登录 -->
        <p class="login-link">
          已有账号？<router-link to="/login">返回登录</router-link>
        </p>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { registerApi } from '@/api/auth'

const router = useRouter()

const form = reactive({
  username: '',
  phone: '',
  email: '',
  password: '',
  confirmPassword: '',
  inviteCode: '',
})
const loading = ref(false)
const errorMsg = ref('')
const successMsg = ref('')

// 密码规则实时检测
const pwRule = computed(() => ({
  length: form.password.length >= 8,
  upper: /[A-Z]/.test(form.password),
  lower: /[a-z]/.test(form.password),
  digit: /\d/.test(form.password),
}))

async function handleRegister() {
  errorMsg.value = ''
  successMsg.value = ''

  // 前端校验
  if (!form.inviteCode.trim()) {
    errorMsg.value = '请输入企业邀请码'
    return
  }
  if (!form.username.trim()) {
    errorMsg.value = '请输入用户名'
    return
  }
  if (!form.phone.trim()) {
    errorMsg.value = '请输入手机号'
    return
  }
  if (!form.password) {
    errorMsg.value = '请输入密码'
    return
  }
  if (form.password.length < 8 || !pwRule.value.upper || !pwRule.value.lower || !pwRule.value.digit) {
    errorMsg.value = '密码不满足安全要求：至少8位，需包含大写字母、小写字母和数字'
    return
  }
  if (form.password !== form.confirmPassword) {
    errorMsg.value = '两次密码输入不一致'
    return
  }

  loading.value = true
  try {
    await registerApi({
      username: form.username.trim(),
      phone: form.phone.trim(),
      email: form.email.trim() || undefined,
      password: form.password,
      confirmPassword: form.confirmPassword,
      inviteCode: form.inviteCode.trim() || undefined,
    })
    successMsg.value = '注册成功！即将跳转登录页...'
    setTimeout(() => {
      router.push('/login')
    }, 1500)
  } catch (err: any) {
    errorMsg.value = err?.response?.data?.message || err.message || '注册失败'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #000;
}
.register-card {
  width: 420px;
  padding: 40px;
  background: #fff;
  border: 2px solid #000;
}
.title {
  text-align: center;
  color: #000;
  margin-bottom: 30px;
  font-size: 20px;
}
.error-msg {
  background: #f5f5f5;
  color: #000;
  padding: 10px 14px;
  border: 1px solid #000;
  margin-bottom: 16px;
  font-size: 14px;
}
.success-msg {
  background: #f5f5f5;
  color: #000;
  padding: 10px 14px;
  border: 1px solid #000;
  margin-bottom: 16px;
  font-size: 14px;
}
.form-group {
  margin-bottom: 18px;
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
  border: 1px solid #ccc;
  font-size: 14px;
  outline: none;
  box-sizing: border-box;
  transition: border-color 0.2s;
}
.form-group input:focus {
  border-color: #000;
}
.btn-register {
  width: 100%;
  padding: 12px;
  background: #000;
  color: #fff;
  border: none;
  font-size: 16px;
  cursor: pointer;
  margin-top: 6px;
  transition: background 0.2s;
}
.btn-register:hover:not(:disabled) {
  background: #333;
}
.btn-register:disabled {
  background: #ccc;
  color: #999;
  cursor: not-allowed;
}
.login-link {
  text-align: center;
  margin-top: 16px;
  font-size: 14px;
  color: #666;
}
.login-link a {
  color: #000;
  font-weight: 600;
  text-decoration: none;
}
.login-link a:hover {
  text-decoration: underline;
}
</style>

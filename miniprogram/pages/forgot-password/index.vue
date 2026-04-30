<template>
  <view class="forgot-page">
    <!-- 标题 -->
    <view class="title-section">
      <text class="title-text">找回密码</text>
      <text class="title-hint">输入注册邮箱，获取重置验证码</text>
    </view>

    <!-- 步骤1: 输入邮箱 -->
    <view v-if="step === 1" class="step-content">
      <view class="input-group">
        <input
          v-model="email"
          class="input-field"
          placeholder="注册邮箱"
          placeholder-style="color: #999"
        />
      </view>

      <text v-if="step1Error" class="error-text">{{ step1Error }}</text>

      <button
        class="action-btn"
        :class="{ 'action-btn--disabled': !validEmail }"
        :disabled="!validEmail || sending"
        :loading="sending"
        @click="sendCode"
      >
        获取验证码
      </button>
    </view>

    <!-- 步骤2: 输入验证码和新密码 -->
    <view v-if="step === 2" class="step-content">
      <view class="input-group code-row">
        <input
          v-model="code"
          class="input-field"
          type="number"
          maxlength="6"
          placeholder="6位验证码"
          placeholder-style="color: #999"
        />
        <button
          class="resend-btn"
          :class="{ 'resend-btn--disabled': countdown > 0 }"
          :disabled="countdown > 0"
          @click="sendCode"
        >
          {{ countdown > 0 ? `${countdown}s` : '重新发送' }}
        </button>
      </view>

      <view class="input-group">
        <input
          v-model="newPassword"
          class="input-field"
          :password="!showPwd"
          placeholder="新密码（8-20位，含大小写字母和数字）"
          placeholder-style="color: #999"
          maxlength="20"
        />
        <text class="input-suffix" @click="showPwd = !showPwd">
          {{ showPwd ? '隐藏' : '显示' }}
        </text>
      </view>

      <view class="input-group">
        <input
          v-model="confirmPassword"
          class="input-field"
          :password="!showConfirmPwd"
          placeholder="确认新密码"
          placeholder-style="color: #999"
          maxlength="20"
        />
        <text class="input-suffix" @click="showConfirmPwd = !showConfirmPwd">
          {{ showConfirmPwd ? '隐藏' : '显示' }}
        </text>
      </view>

      <text v-if="step2Error" class="error-text">{{ step2Error }}</text>

      <button
        class="action-btn"
        :class="{ 'action-btn--disabled': !canReset }"
        :disabled="!canReset || resetting"
        :loading="resetting"
        @click="resetPassword"
      >
        重置密码
      </button>
    </view>

    <!-- 步骤3: 成功 -->
    <view v-if="step === 3" class="step-content">
      <view class="success-icon">&#10003;</view>
      <text class="success-text">密码重置成功</text>
      <button class="action-btn" @click="goLogin">返回登录</button>
    </view>
  </view>
</template>

<script lang="ts">
import { defineComponent, ref, computed } from 'vue'
import { forgotPasswordApi, resetPasswordApi } from '@/api/auth'

export default defineComponent({
  setup() {
    const step = ref(1)
    const email = ref('')
    const code = ref('')
    const newPassword = ref('')
    const confirmPassword = ref('')
    const showPwd = ref(false)
    const showConfirmPwd = ref(false)
    const sending = ref(false)
    const resetting = ref(false)
    const countdown = ref(0)
    const step1Error = ref('')
    const step2Error = ref('')
    let timer: ReturnType<typeof setInterval> | null = null

    const validEmail = computed(() => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.value))
    const validPassword = computed(() => /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,20}$/.test(newPassword.value))
    const pwdMatch = computed(() => newPassword.value === confirmPassword.value)
    const canReset = computed(() => code.value.length >= 4 && validPassword.value && pwdMatch.value)

    async function sendCode() {
      if (!validEmail.value) return
      sending.value = true
      step1Error.value = ''
      try {
        await forgotPasswordApi(email.value.trim())
        uni.showToast({ title: '验证码已发送到邮箱', icon: 'none' })
        step.value = 2
        countdown.value = 60
        timer = setInterval(() => {
          countdown.value--
          if (countdown.value <= 0 && timer) {
            clearInterval(timer)
            timer = null
          }
        }, 1000)
      } catch (err: any) {
        step1Error.value = err.message || '发送失败，请检查邮箱是否正确'
      } finally {
        sending.value = false
      }
    }

    async function resetPassword() {
      if (!canReset.value) return
      resetting.value = true
      step2Error.value = ''
      try {
        await resetPasswordApi({
          email: email.value.trim(),
          code: code.value,
          newPassword: newPassword.value,
          confirmPassword: confirmPassword.value,
        })
        if (timer) {
          clearInterval(timer)
          timer = null
        }
        step.value = 3
      } catch (err: any) {
        step2Error.value = err.message || '重置失败，请稍后重试'
      } finally {
        resetting.value = false
      }
    }

    function goLogin() {
      uni.navigateBack()
    }

    return {
      step,
      email,
      code,
      newPassword,
      confirmPassword,
      showPwd,
      showConfirmPwd,
      sending,
      resetting,
      countdown,
      step1Error,
      step2Error,
      validEmail,
      validPassword,
      pwdMatch,
      canReset,
      sendCode,
      resetPassword,
      goLogin,
    }
  },
})
</script>

<style lang="scss" scoped>
.forgot-page {
  min-height: 100vh;
  background: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80rpx 48rpx 48rpx;
  box-sizing: border-box;
}

/* 标题 */
.title-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 64rpx;
}

.title-text {
  font-size: 40rpx;
  font-weight: 800;
  color: #000;
}

.title-hint {
  font-size: 26rpx;
  color: #999;
  margin-top: 12rpx;
}

/* 步骤内容 */
.step-content {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
}

/* 输入框 */
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

/* 验证码行 */
.code-row {
  display: flex;
  gap: 16rpx;
}

.code-input {
  flex: 1;
}

.resend-btn {
  width: 180rpx;
  height: 88rpx;
  font-size: 26rpx;
  background: #fff;
  color: #000;
  border: 2rpx solid #000;
  display: flex;
  align-items: center;
  justify-content: center;
  white-space: nowrap;
  padding: 0 8rpx;
}

.resend-btn--disabled {
  border-color: #ccc;
  color: #999;
}

.resend-btn::after {
  border: none;
}

/* 错误 */
.error-text {
  font-size: 24rpx;
  color: #e53e3e;
  margin-bottom: 16rpx;
  text-align: center;
}

/* 操作按钮 */
.action-btn {
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

.action-btn--disabled {
  background: #e0e0e0;
  color: #999;
}

.action-btn::after {
  border: none;
}

/* 成功状态 */
.success-icon {
  width: 100rpx;
  height: 100rpx;
  border: 4rpx solid #000;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48rpx;
  color: #000;
  margin-bottom: 24rpx;
  margin-top: 48rpx;
}

.success-text {
  font-size: 32rpx;
  font-weight: 600;
  color: #000;
  margin-bottom: 48rpx;
}
</style>

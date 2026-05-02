<template>
  <div class="qual-page">
    <router-link to="/login" class="back-link">&larr; 返回登录</router-link>

    <div class="card main-card">
      <h2>企业资质申请</h2>
      <p class="intro">SkyTrust 面向企业提供无人机租赁管理服务。请填写以下信息申请企业管理员账号，审核通过后您将收到包含邀请码的邮件。</p>

      <div v-if="submitted" class="success-box">
        <strong>申请已提交</strong>
        <p>我们将在 1-3 个工作日内审核您的资质。审核通过后，邀请码将发送至您填写的邮箱，请使用邀请码完成注册。</p>
        <router-link to="/login" class="btn btn-primary">返回登录</router-link>
      </div>

      <form v-else @submit.prevent="handleSubmit">
        <div v-if="errorMsg" class="error-box">{{ errorMsg }}</div>
        <div class="form-group"><label>企业名称 <span class="req">*</span></label><input v-model="form.company" class="input" placeholder="请输入企业全称" /></div>
        <div class="form-row">
          <div class="form-group"><label>联系人 <span class="req">*</span></label><input v-model="form.contact" class="input" placeholder="姓名" /></div>
          <div class="form-group"><label>手机号 <span class="req">*</span></label><input v-model="form.phone" class="input" placeholder="11位手机号" /></div>
        </div>
        <div class="form-group"><label>企业邮箱 <span class="req">*</span></label><input v-model="form.email" class="input" type="email" placeholder="用于接收邀请码" /></div>
        <div class="form-group"><label>企业规模</label>
          <select v-model="form.scale" class="input">
            <option value="">请选择</option><option value="small">小型 (1-50人)</option><option value="medium">中型 (51-500人)</option><option value="large">大型 (500人+)</option>
          </select>
        </div>
        <div class="form-group"><label>申请用途说明</label><textarea v-model="form.reason" class="input" rows="3" placeholder="请简要说明使用 SkyTrust 平台的业务场景"></textarea></div>
        <div class="form-group"><label>营业执照或资质证明</label><input type="file" accept=".pdf,.jpg,.png" class="input-file" /></div>
        <button type="submit" class="btn btn-primary btn-lg" :disabled="submitting">{{ submitting ? '提交中…' : '提交申请' }}</button>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { submitQualificationApi } from '@/api/qualification'

const form = reactive({ company: '', contact: '', phone: '', email: '', scale: '', reason: '' })
const submitting = ref(false)
const submitted = ref(false)
const errorMsg = ref('')

async function handleSubmit() {
  errorMsg.value = ''
  if (!form.company || !form.contact || !form.phone || !form.email) {
    errorMsg.value = '请填写所有必填字段'
    return
  }
  submitting.value = true
  try {
    await submitQualificationApi({
      company: form.company,
      contact: form.contact,
      phone: form.phone,
      email: form.email,
      scale: form.scale,
      reason: form.reason,
    })
    submitted.value = true
  } catch (e: any) {
    errorMsg.value = e.message || '提交失败，请重试'
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.qual-page { min-height: 100vh; background: #f5f5f5; display: flex; flex-direction: column; align-items: center; padding: 40px 20px; }
.back-link { align-self: flex-start; max-width: 680px; width: 100%; margin: 0 auto 20px; color: #000; font-size: 14px; font-weight: 600; text-decoration: none; }
.back-link:hover { text-decoration: underline; }
.main-card { background: #fff; border: 2px solid #000; padding: 40px; max-width: 680px; width: 100%; }
.main-card h2 { margin: 0 0 8px; font-size: 22px; font-weight: 700; }
.intro { color: #666; font-size: 14px; margin: 0 0 28px; line-height: 1.6; }
.form-group { margin-bottom: 16px; }
.form-group label { display: block; margin-bottom: 4px; font-size: 14px; font-weight: 500; }
.req { color: #000; }
.input { width: 100%; padding: 10px 12px; border: 1px solid #ccc; font-size: 14px; box-sizing: border-box; }
.input:focus { border-color: #000; outline: none; }
textarea.input { resize: vertical; }
.input-file { margin-top: 4px; font-size: 14px; }
.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.btn-lg { width: 100%; padding: 14px; font-size: 16px; margin-top: 8px; }
.success-box { background: #f5f5f5; border: 2px solid #000; padding: 24px; text-align: center; }
.success-box strong { display: block; font-size: 18px; margin-bottom: 8px; }
.success-box p { color: #666; font-size: 14px; margin: 0 0 20px; line-height: 1.6; }
.error-box { background: #fbe9e7; color: #b71c1c; border: 1px solid #b71c1c; padding: 10px 16px; font-size: 13px; margin-bottom: 16px; }
@media (max-width: 768px) { .form-row { grid-template-columns: 1fr; } .main-card { padding: 24px; } }
</style>

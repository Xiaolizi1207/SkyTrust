<template>
  <div class="forgot-page">
    <div class="forgot-card">
      <h2 class="title">找回密码</h2>

      <div class="notice">
        <p>该功能正在开发中，敬请期待。</p>
        <p>如有需要，请联系管理员重置密码。</p>
      </div>

      <router-link to="/login" class="btn-back">返回登录</router-link>
    </div>
  </div>
</template>

<script setup lang="ts">
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
  text-align: center;
}
.title {
  text-align: center;
  color: #1a73e8;
  margin-bottom: 24px;
  font-size: 20px;
}
.notice {
  background: #fff7ed;
  color: #9a3412;
  padding: 16px;
  border-radius: 6px;
  margin-bottom: 24px;
  font-size: 14px;
  line-height: 1.8;
}
.btn-back {
  display: inline-block;
  padding: 10px 32px;
  background: #1a73e8;
  color: #fff;
  border-radius: 6px;
  text-decoration: none;
  font-size: 14px;
  transition: background 0.2s;
}
.btn-back:hover {
  background: #1557b0;
}
</style>

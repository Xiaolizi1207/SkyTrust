<template>
  <div class="not-found">
    <h1>404</h1>
    <p>页面未找到</p>
    <router-link to="/">返回首页</router-link>
  </div>
</template>

<style scoped>
.not-found {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  color: #666;
}
.not-found h1 {
  font-size: 72px;
  color: #000;
}
.not-found p {
  font-size: 18px;
  margin: 16px 0;
}
.not-found a {
  color: #000;
  font-weight: 600;
  text-decoration: underline;
}
</style>

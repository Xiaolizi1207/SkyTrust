import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import { setupGuards } from './router/guards'
import './styles/global.css'

const app = createApp(App)

// 状态管理
app.use(createPinia())

// 路由
app.use(router)

// 路由守卫
setupGuards(router)

app.mount('#app')

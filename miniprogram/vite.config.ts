// @ts-nocheck
import { defineConfig } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'
import path from 'path'

// 修复 uni-app alpha 版本中 global.uniPlugin 未初始化的问题
if (typeof global.uniPlugin === 'undefined') {
    global.uniPlugin = { options: {} }
}

export default defineConfig({
    plugins: [uni()],
    resolve: {
        alias: {
            '@': path.resolve(__dirname, '.'),
        },
    },
})

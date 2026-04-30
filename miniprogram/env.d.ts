/// <reference types="vite/client" />
/// <reference types="@dcloudio/types" />

declare module '*.vue' {
  import { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}

declare module '@dcloudio/vite-plugin-uni' {
  import { Plugin } from 'vite'
  const uni: () => Plugin
  export default uni
}

// ===== HBuilderX 兼容层 =====

// 1. uni-app 缺少类型声明（ts-loader 报 TS7016）
declare module '@dcloudio/uni-app' {
  type UniAppLifecycleFn = (...args: any[]) => void
  export const onLaunch: UniAppLifecycleFn
  export const onShow: UniAppLifecycleFn
  export const onHide: UniAppLifecycleFn
  export const onLoad: UniAppLifecycleFn
}

// 2. 恢复 Vue 3 真实导出（@dcloudio/types 含 Vue 2 shim 遮蔽 ref/computed）
declare module 'vue' {
  export * from '@vue/runtime-dom'
}

// 3. 全局 uniplugin 类型
declare global {
  var uni: any
  var uniPlugin: {
    options: Record<string, any>
    preprocess?: { vueContext: Set<string> }
  }
}

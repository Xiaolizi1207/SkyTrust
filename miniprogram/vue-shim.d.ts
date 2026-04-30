// 修复 HBuilderX 内置 ts-loader 中 @dcloudio/types 对 Vue 3 类型覆盖的问题
// @dcloudio/types 含 Vue 2 风格的 vue 模块声明，会遮蔽 vue@3 的真实导出 (ref,computed等)
declare module 'vue' {
  export * from '@vue/runtime-dom'
}

// HBuilderX 内置 ts-loader 无法识别 <script setup> 的默认导出，
// 需要显式声明 .vue 文件模块类型
declare module '*.vue' {
  import { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}

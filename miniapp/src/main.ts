/**
 * ============================================================================
 * main.ts — Uni-app 应用的入口文件
 * ============================================================================
 *
 * 【文件说明】
 * 这是 Uni-app 项目的入口文件，负责创建 Vue 应用实例并注册插件。
 * 与普通 Vue Web 项目不同，Uni-app 的入口文件需要导出一个 createApp 函数，
 * 这是为了兼容 SSR（服务端渲染）和多端编译的需要。
 *
 * 【与 H5 前端的差异对比】
 * - H5 (标准 Vue 3): 直接 createApp(App).mount('#app')
 * - Uni-app: 导出 createApp() 函数，由 Uni-app 框架调用。
 *   框架会根据编译目标（微信小程序/H5/App）来决定如何挂载应用。
 * - 使用 createSSRApp 而非 createApp，是为了支持 SSR 和小程序的初始化机制。
 *   SSRApp 会在客户端进行"hydration"（注水），复用服务端渲染的 HTML。
 *
 * 【关键 API 说明】
 * - createSSRApp: Vue 3 提供的 SSR 兼容的应用创建函数
 * - createPinia:  Pinia 状态管理库的创建函数
 * - app.use(pinia): 注册 Pinia 插件，使所有组件都能使用 store
 *
 * 【Uni-app 编译目标】
 * Uni-app 可以编译到多个平台：
 * - MP-WEIXIN: 微信小程序（使用 WXML/WXSS）
 * - H5: 网页应用（使用标准 HTML/CSS/JS）
 * - APP: 原生 App（iOS/Android，基于 weex/uni-app x）
 * - MP-ALIPAY: 支付宝小程序
 * - MP-BAIDU: 百度小程序
 * 等等...
 *
 * 【参考文档】
 * - Uni-app 入口文件: https://uniapp.dcloud.net.cn/collocation/main.html
 * - Pinia 官方文档: https://pinia.vuejs.org/zh/
 * ============================================================================
 */

// 导入 Vue 3 的 SSR 兼容应用创建函数
// 【注意】必须使用 createSSRApp 而不是 createApp，因为小程序的渲染层和逻辑层分离，
// 需要通过 SSR 模式来正确处理初始状态
import { createSSRApp } from 'vue'

// 导入 Pinia 状态管理库
// Pinia 是 Vue 3 官方推荐的状态管理方案，是 Vuex 的继任者
import { createPinia } from 'pinia'

// 导入根组件
import App from './App.vue'

/**
 * 创建应用实例
 *
 * 【为什么是导出函数而不是直接创建？】
 * Uni-app 框架需要在不同的运行环境中（小程序/H5/App）以不同方式初始化应用，
 * 因此要求入口文件导出一个工厂函数，由框架控制调用时机。
 *
 * @returns {Object} 包含 app 属性的对象，框架会从中获取应用实例
 */
export function createApp() {
  // 创建 SSR 兼容的 Vue 应用实例
  const app = createSSRApp(App)

  // 创建 Pinia 状态管理实例
  const pinia = createPinia()

  // 将 Pinia 注册为 Vue 插件
  // 注册后，所有组件都可以通过 useXxxStore() 来访问状态
  app.use(pinia)

  // 返回应用实例（框架会自动处理挂载）
  return { app }
}

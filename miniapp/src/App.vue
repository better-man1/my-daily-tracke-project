<!--
  ============================================================================
  App.vue — Uni-app 应用的根组件
  ============================================================================
  【文件说明】
  这是整个 Uni-app 小程序的入口组件，类似于 Vue Web 项目中的 App.vue。
  每个 Uni-app 项目只有一个 App.vue，它在所有页面之前加载。

  【与 H5 前端的差异对比】
  - H5 (浏览器端): App.vue 只是一个普通的 Vue 根组件，生命周期和标准 Vue 一致。
  - 小程序端: App.vue 中可以使用小程序特有的"应用级生命周期"钩子，
    如 onLaunch、onShow、onHide、onError 等（见下方说明）。
  - 在小程序中，App.vue 的 <style> 是全局样式，会应用到所有页面。

  【小程序应用级生命周期】
  - onLaunch: 小程序初始化时触发（全局只触发一次），类似 Vue 的 created。
    常用于：读取本地缓存、检查登录状态、获取系统信息等。
  - onShow:   小程序从后台进入前台时触发（可多次触发）。
    常用于：刷新数据、恢复页面状态。
  - onHide:   小程序从前台进入后台时触发。
    常用于：保存临时数据、暂停音频等。
  - onError:  小程序发生脚本错误或 API 调用失败时触发。
    常用于：全局错误收集、上报。

  【Uni-app 语法说明】
  - import { onLaunch, onShow } from '@dcloudio/uni-app'
    这是 Uni-app 特有的导入方式，生命周期钩子需要从 '@dcloudio/uni-app' 包导入。
  - uni.navigateTo() 是 Uni-app 的路由 API，用于跳转到非 tabBar 页面。
  - uni 对象是 Uni-app 的全局 API 对象，类似于微信原生的 wx 对象。

  【参考文档】
  - Uni-app 应用生命周期: https://uniapp.dcloud.net.cn/collocation/App.html#applifecycle
  - Uni-app 路由 API: https://uniapp.dcloud.net.cn/api/router.html
  ============================================================================
-->
<script setup lang="ts">
// 【导入说明】
// onLaunch 和 onShow 是 Uni-app 的应用级生命周期钩子，需要从 '@dcloudio/uni-app' 包导入。
// 这与标准 Vue 3 的 onMounted、onUnmounted 等组合式 API 不同。
// 注意：onLaunch 等钩子只能在 App.vue 中使用，普通页面应使用 onLoad、onShow 等页面级钩子。
import { onLaunch, onShow } from '@dcloudio/uni-app'

// 导入 Pinia 状态管理中的用户 store，用于判断登录状态
import { useUserStore } from '@/stores/user'

/**
 * onLaunch — 小程序启动时执行（全局仅执行一次）
 *
 * 执行时机：小程序冷启动（首次打开或被微信销毁后重新打开）时触发。
 * 类比 Web：类似于 main.ts 中 new Vue() 之后的初始化逻辑。
 *
 * 这里做的事：
 * 1. 检查用户是否已登录（通过 Pinia store 读取本地缓存的 token）
 * 2. 如果未登录，跳转到登录页面
 *
 * 注意：uni.navigateTo 可以打开 sub-pages 下的非 tabBar 页面。
 * 而 tabBar 页面（如首页）必须用 uni.switchTab 打开。
 */
onLaunch(() => {
  console.log('[DailyTracker] App launched')
  const userStore = useUserStore()
  // 检查登录状态 — isLoggedIn 是一个 computed 属性，检查 accessToken 是否存在
  if (!userStore.isLoggedIn) {
    // 跳转到登录页（sub-pages 是分包页面，在 pages.json 中配置的 subPackages）
    uni.navigateTo({ url: '/sub-pages/login/login' })
  }
})

/**
 * onShow — 小程序从后台切到前台时执行
 *
 * 触发场景：
 * - 小程序首次打开（在 onLaunch 之后触发）
 * - 用户从其他小程序/聊天窗口切换回来
 * - 手机解锁后恢复小程序
 *
 * 对比 H5：H5 没有"前后台切换"的概念（浏览器 tab 切换用 Page Visibility API）。
 * 这是小程序特有的运行环境决定的——小程序可以被挂起到后台。
 */
onShow(() => {
  console.log('[DailyTracker] App shown')
})
</script>

<!--
  【全局样式说明】
  App.vue 的 <style> 标签中的样式会作为全局样式应用到所有页面。
  注意：这里没有 scoped 属性，因为 App.vue 的样式需要全局生效。

  @import '@/styles/common.scss' 引入了全局公共样式文件。
  该文件包含了页面基础样式、安全区适配、通用工具类等。

  【rpx 单位说明】
  小程序使用 rpx（responsive pixel）作为响应式长度单位：
  - 750rpx = 屏幕宽度（无论什么设备）
  - iPhone 6 上: 1rpx ≈ 0.5px
  - 这是小程序特有的单位，H5 中会自动转换为 rem 或 px
-->
<style lang="scss">
@import '@/styles/common.scss';
</style>

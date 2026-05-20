<!--
================================================================================
文件：App.vue — Vue 3 根组件
================================================================================

【文件用途】
这是整个 Vue 应用的根组件，也是组件树的"最顶层"。
所有页面（如登录页、数据看板、每日计划等）都是通过 <router-view /> 渲染在这个组件内部的。

本组件的职责：
  1. 提供 Element Plus 的全局国际化配置（中文语言包）
  2. 通过 <router-view /> 渲染当前路由对应的页面组件
  3. 提供全局暗黑模式的基础样式

【在项目中的位置】
frontend/src/App.vue
在 main.ts 中通过 createApp(App) 被引入作为根组件。

【相关技术知识】
- Vue 单文件组件 (.vue)：将 <template>、<script>、<style> 写在同一个文件中，
  实现关注点分离的同时保持组件的内聚性。
- <script setup> 语法糖：Vue 3.2+ 引入的编译时优化语法，无需手动 export default，
  顶层变量自动暴露给模板使用。
================================================================================
-->

<!--
================================================================================
<template> — 模板区域（HTML 结构）
================================================================================

【解析】
Vue 单文件组件的模板部分，定义了组件渲染出的 HTML 结构。

<el-config-provider> 是 Element Plus 提供的全局配置组件，
用于统一设置所有 Element Plus 组件的默认行为（如语言、尺寸、主题等）。

【知识扩展：Element Plus 的 ConfigProvider】
ConfigProvider 使用了 Vue 3 的 provide/inject 机制：
  - 在根组件 provide 配置数据
  - 所有子组件（无论嵌套多深）都能 inject 并读取配置
这样只需在 App.vue 配置一次，整个应用的所有 Element Plus 组件都会使用中文。

<router-view /> 是 Vue Router 提供的核心组件，它是一个"占位符"，
会根据当前浏览器的 URL 自动渲染对应的路由组件。
例如：
  - 访问 /login       → 渲染 LoginView.vue
  - 访问 /dashboard   → 渲染 DashboardView.vue
  - 访问 /plan        → 渲染 PlanView.vue
-->
<template>
  <!--
    el-config-provider：Element Plus 全局配置容器
    :locale="zhCn" 将所有 Element Plus 组件（如日期选择器、分页、表格等）的语言设置为中文。
    如果不加这个配置，日期选择器会显示 "January" 而不是 "一月"，分页会显示 "Total" 而不是 "共"。
  -->
  <el-config-provider :locale="zhCn">
    <!--
      router-view：路由视图出口
      这是 Vue Router 的核心组件，它会根据当前 URL 匹配到的路由规则，
      动态渲染对应的页面组件。相当于一个"动态容器"。

      【工作原理】
      当用户访问 /dashboard 时：
      1. Vue Router 匹配到 path: '/dashboard' 的路由规则
      2. <router-view /> 被替换为 DashboardView.vue 组件的内容
      3. URL 变化时，<router-view /> 会自动切换显示的组件

      【嵌套路由中的 router-view】
      本项目中，AppLayout.vue 内部也有一个 <router-view />，
      形成了两层嵌套：
        App.vue 的 <router-view /> → AppLayout.vue
          AppLayout.vue 的 <router-view /> → DashboardView.vue 等具体页面
    -->
    <router-view />
  </el-config-provider>
</template>

<!--
================================================================================
<script setup> — 脚本区域（TypeScript + Composition API）
================================================================================

【解析】
<script setup lang="ts"> 是 Vue 3.2+ 引入的编译时语法糖：
  - "setup" 表示使用 Composition API 的 setup 语法糖
  - "lang='ts'" 表示使用 TypeScript

【知识扩展：<script setup> vs 普通 <script>】

普通写法（Options API 风格）：
  <script>
  export default {
    data() { return { count: 0 } },
    methods: { increment() { this.count++ } }
  }
  </script>

Composition API 写法（无 setup 语法糖）：
  <script>
  import { ref } from 'vue'
  export default {
    setup() {
      const count = ref(0)
      function increment() { count.value++ }
      return { count, increment }  // 必须手动 return
    }
  }
  </script>

setup 语法糖写法（推荐，本项目使用）：
  <script setup lang="ts">
  import { ref } from 'vue'
  const count = ref(0)            // 无需 export default
  function increment() { count.value++ }  // 无需 return
  </script>
  // 模板中可以直接使用 count 和 increment

优点：
  - 更少的样板代码（不需要 export default 和 return）
  - 更好的 TypeScript 类型推断
  - 更好的运行时性能（编译时优化）
-->
<script setup lang="ts">
/**
 * 导入 Element Plus 的中文语言包。
 *
 * 【路径解析】
 * 'element-plus/es/locale/lang/zh-cn' 使用了 ES Module 版本，
 * 这样 Vite 在打包时可以进行 tree-shaking（摇树优化），
 * 只打包实际使用到的代码，减小最终产物体积。
 *
 * 【为什么在 App.vue 中导入？】
 * 因为 App.vue 是根组件，所有 Element Plus 组件都是它的后代组件。
 * 在这里通过 <el-config-provider> 注入中文配置，可以确保所有组件都受到影响。
 *
 * 其他可用的语言包：
 *   import en from 'element-plus/es/locale/lang/en'     // 英文
 *   import ja from 'element-plus/es/locale/lang/ja'     // 日文
 *   import zhTw from 'element-plus/es/locale/lang/zh-tw' // 繁体中文
 */
import zhCn from 'element-plus/es/locale/lang/zh-cn'
</script>

<!--
================================================================================
<style> — 样式区域（全局暗黑模式支持）
================================================================================

【解析】
这里的 <style> 没有 scoped 属性，因此样式会全局生效。

【知识扩展：scoped vs 非 scoped】
- <style scoped>：样式只作用于当前组件，通过给元素添加 data-v-xxxxx 属性实现隔离。
- <style>（无 scoped）：样式全局生效，会影响所有组件。
- <style module>：CSS Modules 模式，通过 :class="$style.xxx" 使用。

这里选择非 scoped 是因为暗黑模式的基础设置需要全局生效。

【CSS 属性说明】
color-scheme: dark 告诉浏览器当前页面使用暗色方案，浏览器原生 UI 元素
（如滚动条、表单控件、下拉框等）会自动使用暗色风格。
这是 CSS 原生属性，不需要任何框架支持。

【暗黑模式的触发方式】
当 HTML 根元素（<html>）拥有 class="dark" 时，
Tailwind CSS / Element Plus 的暗黑模式会被激活。
通常配合 @vueuse/core 的 useDark() 来自动切换：
  import { useDark } from '@vueuse/core'
  const isDark = useDark()  // 自动读取/写入 localStorage 并切换 class
-->
<style>
html.dark {
  color-scheme: dark;
}
</style>

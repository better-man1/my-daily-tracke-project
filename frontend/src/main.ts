/**
 * ============================================================================
 * 文件：main.ts — Vue 3 应用程序的入口文件
 * ============================================================================
 *
 * 【文件用途】
 * 这是整个前端 H5 项目的"启动点"。浏览器加载页面后，首先执行的 TypeScript 文件就是它。
 * 它的核心职责是：
 *   1. 创建 Vue 应用实例
 *   2. 注册全局插件（Pinia 状态管理、Vue Router 路由等）
 *   3. 配置全局国际化/日期库
 *   4. 将应用挂载到 HTML 页面的 DOM 节点上
 *
 * 【在项目中的位置】
 * frontend/src/main.ts
 * 当运行 `npm run dev` 时，Vite 会以这个文件为入口，构建依赖图并启动开发服务器。
 *
 * 【相关技术知识】
 * - Vue 3 应用实例：通过 createApp() 创建，每个 Vue 应用都由一个根实例开始。
 * - 插件系统：通过 app.use() 安装插件，插件可以提供全局功能（如路由、状态管理）。
 * - 挂载点：最终 app.mount('#app') 会将 Vue 应用渲染到 index.html 中 id="app" 的 DOM 元素内。
 * ============================================================================
 */

// ============================================================================
// 第一步：导入核心依赖
// ============================================================================

/**
 * createApp — Vue 3 的工厂函数，用于创建一个新的 Vue 应用实例。
 *
 * 【知识扩展：Vue 2 vs Vue 3 的区别】
 * - Vue 2 使用 new Vue({...}) 创建实例，全局 API（如 Vue.use、Vue.mixin）会修改
 *   全局的 Vue 构造函数，导致多个实例之间共享状态，容易产生冲突。
 * - Vue 3 改为 createApp() 工厂模式，每次创建的都是独立的应用实例，不同实例之间
 *   的插件、指令、mixin 完全隔离，更加安全、灵活。
 *
 * 示例：
 *   const app1 = createApp(App1)  // 独立实例1
 *   const app2 = createApp(App2)  // 独立实例2，不会互相影响
 */
import { createApp } from 'vue'

/**
 * createPinia — Pinia 是 Vue 3 官方推荐的状态管理库（替代了 Vuex）。
 *
 * 【知识扩展：为什么用 Pinia 而不是 Vuex？】
 * - Pinia 的 API 更简洁，没有 mutations，直接通过 actions 修改状态。
 * - 完整的 TypeScript 支持，自动推断类型。
 * - 支持多个 Store，按需引入，代码分割更友好。
 * - 支持插件扩展（如持久化存储、开发者工具集成）。
 *
 * 【基本使用方式】
 * // 定义 Store（通常在 stores/ 目录下）
 * export const useUserStore = defineStore('user', () => {
 *   const token = ref('')
 *   function setToken(val: string) { token.value = val }
 *   return { token, setToken }
 * })
 *
 * // 在组件中使用
 * const userStore = useUserStore()
 * console.log(userStore.token)
 */
import { createPinia } from 'pinia'

/**
 * App — 根组件，即整个 Vue 应用的"最顶层组件"。
 * 所有页面和子组件都渲染在 App 组件内部。
 * 在 Vue 3 中，根组件通常使用 <router-view /> 来展示当前路由对应的页面。
 */
import App from './App.vue'

/**
 * router — Vue Router 路由实例，定义了 URL 路径与页面组件之间的映射关系。
 *
 * 【知识扩展：Vue Router 的两种模式】
 * - createWebHistory()：使用 HTML5 History API（pushState/replaceState），
 *   URL 看起来像 /dashboard，没有 # 号。需要服务器配置回退到 index.html。
 * - createWebHashHistory()：使用 URL 的 hash 部分（#），如 /#/dashboard，
 *   不需要服务器特殊配置，但 URL 不够优雅。
 *
 * 本项目使用 createWebHistory()，即 History 模式。
 */
import router from './router'

/**
 * dayjs — 一个轻量级的日期处理库（仅 2KB），API 设计与 moment.js 几乎一致。
 *
 * 【为什么选择 dayjs？】
 * - 体积小：moment.js 约 200KB+，dayjs 仅约 2KB（gzip 后）。
 * - 链式调用：dayjs('2024-01-01').add(1, 'day').format('YYYY-MM-DD')
 * - 插件机制：按需引入插件（如相对时间、季度、时区等）。
 *
 * 本项目使用 dayjs 处理日期显示、日期计算等场景。
 */
import dayjs from 'dayjs'

/**
 * 导入 dayjs 的中文语言包。
 * dayjs 默认使用英文，引入 zh-cn 语言包后可以显示中文格式，如"一月"、"周一"等。
 */
import 'dayjs/locale/zh-cn'

// ============================================================================
// 第二步：全局配置
// ============================================================================

/**
 * 设置 dayjs 的全局语言为中文。
 * 调用后，所有 dayjs 实例的格式化输出都会使用中文。
 *
 * 示例：
 *   dayjs().format('MMMM')    // 默认输出 "January"
 *   设置中文后输出 "一月"
 *   dayjs().format('dddd')    // 默认输出 "Monday"
 *   设置中文后输出 "星期一"
 */
dayjs.locale('zh-cn')

/**
 * 导入全局样式文件。
 *
 * 【重要说明】
 * 这行代码必须在 Element Plus 之前引入！原因是 CSS 的优先级由"后加载覆盖先加载"决定。
 * 如果我们的自定义样式在 Element Plus 之后加载，就能覆盖 Element Plus 的默认样式，
 * 实现自定义主题效果。
 *
 * 【知识扩展：SCSS vs CSS】
 * - SCSS (Sassy CSS) 是 CSS 的预处理器，支持变量、嵌套、混入（mixin）、函数等。
 * - 浏览器不能直接运行 SCSS，需要通过构建工具（Vite + sass 包）编译成标准 CSS。
 * - 本项目使用 variables.scss 存储全局变量（如主题颜色、间距等）。
 */
import '@/styles/global.scss'

// ============================================================================
// 第三步：创建 Vue 应用实例并注册插件
// ============================================================================

/**
 * 创建 Vue 应用实例。
 *
 * 【解析】
 * createApp(App) 接收的参数是根组件（App.vue），它定义了整个应用的模板、脚本和样式。
 * 这个 app 实例是所有全局配置的起点。
 *
 * 【类比理解】
 * 就像建房子一样：
 *   - App.vue 是地基
 *   - app.use() 安装的插件是水电管道
 *   - app.mount() 是把房子建在指定位置
 */
const app = createApp(App)

/**
 * 注册 Pinia 状态管理插件。
 *
 * 【解析】
 * app.use(createPinia()) 将 Pinia 安装到 Vue 应用中，之后在任何组件内
 * 都可以通过 useXxxStore() 来访问和修改全局状态。
 *
 * 注意：createPinia() 必须在 app.mount() 之前调用。
 *
 * 【Vue 3 插件机制】
 * 插件本质是一个带有 install(app, options) 方法的对象。
 * 当调用 app.use(plugin) 时，Vue 会执行 plugin.install(app)，
 * 插件借此向 app 注册全局组件、指令、provide/inject 等。
 */
app.use(createPinia())

/**
 * 注册 Vue Router 路由插件。
 *
 * 【解析】
 * 安装后，Vue Router 会：
 *   1. 注册全局组件：<router-view>（显示当前路由组件）和 <router-link>（导航链接）
 *   2. 注入全局属性：$router（路由器实例）和 $route（当前路由信息）
 *   3. 启用路由守卫：beforeEach、afterEach 等钩子函数
 *
 * 【在 Composition API 中的使用方式】
 * import { useRouter, useRoute } from 'vue-router'
 * const router = useRouter()  // 获取路由器实例，用于编程式导航
 * const route = useRoute()    // 获取当前路由信息，如 route.params、route.query
 * router.push('/dashboard')   // 导航到指定路径
 */
app.use(router)

// ============================================================================
// 第四步：挂载应用
// ============================================================================

/**
 * 将 Vue 应用挂载到 DOM 上。
 *
 * 【解析】
 * app.mount('#app') 会查找 index.html 中 id="app" 的元素，
 * 然后将 Vue 应用渲染到该元素内部。
 *
 * 【对应的 index.html 大致结构】
 * <!DOCTYPE html>
 * <html>
 *   <head>...</head>
 *   <body>
 *     <div id="app"></div>     <-- Vue 应用将挂载到这里
 *     <script type="module" src="/src/main.ts"></script>
 *   </body>
 * </html>
 *
 * 【知识扩展：mount 的工作原理】
 * 1. Vue 读取 App 组件的 template（或编译 render 函数）
 * 2. 将虚拟 DOM (VNode) 渲染为真实 DOM
 * 3. 替换 <div id="app"> 的内部内容
 * 4. 之后所有更新都通过虚拟 DOM diff 算法高效完成
 *
 * 【注意事项】
 * mount() 必须在所有 app.use() 之后调用，因为挂载后应用就开始运行了，
 * 之后再注册插件不会生效。
 */
app.mount('#app')

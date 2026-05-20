/**
 * ============================================================================
 * 文件：router/index.ts — Vue Router 路由配置文件
 * ============================================================================
 *
 * 【文件用途】
 * 定义整个应用的 URL 路由规则，即"哪个 URL 显示哪个页面组件"。
 * 同时包含全局路由守卫（导航守卫），用于在页面跳转前执行逻辑（如登录校验、标题更新等）。
 *
 * 【在项目中的位置】
 * frontend/src/router/index.ts
 * 在 main.ts 中通过 `import router from './router'` 引入并注册到 Vue 应用。
 *
 * 【相关技术知识 — Vue Router 核心概念】
 *
 * 1. 路由 (Route)：URL 路径与组件的映射关系。
 *    例如：path: '/dashboard' → component: DashboardView.vue
 *
 * 2. 路由器 (Router)：管理所有路由规则的对象，负责：
 *    - 监听浏览器 URL 的变化
 *    - 匹配对应的路由规则
 *    - 渲染对应的组件到 <router-view />
 *    - 提供编程式导航方法（push、replace、go 等）
 *
 * 3. 路由守卫 (Navigation Guards)：路由跳转过程中的"检查站"：
 *    - beforeEach：全局前置守卫（每次导航前执行）
 *    - afterEach：全局后置钩子（导航完成后执行）
 *    - beforeEnter：路由独享守卫（在路由配置中定义）
 *    - onBeforeRouteLeave：组件内守卫（离开当前页面时触发）
 *
 * 4. 懒加载 (Lazy Loading)：
 *    使用 () => import('...') 动态导入组件，只有在首次访问该路由时才会加载对应的 JS 文件。
 *    这样可以显著减少首页加载时间（不需要一次性加载所有页面的代码）。
 *
 * 5. 嵌套路由 (Nested Routes)：
 *    子路由渲染在父路由组件的 <router-view /> 中。
 *    本项目中，'/' 路径对应 AppLayout.vue（布局组件），
 *    其子路由（dashboard、plan 等）渲染在 AppLayout.vue 内部的 <router-view /> 中。
 *
 * 6. 路由元信息 (Route Meta)：
 *    每个路由规则可以通过 meta 字段附加自定义数据。
 *    本项目用 meta.public 标记公开页面，用 meta.title 设置页面标题。
 * ============================================================================
 */

// ============================================================================
// 导入依赖
// ============================================================================

/**
 * createRouter — 创建 Vue Router 实例的工厂函数。
 *
 * 【参数说明】
 * createRouter 接收一个配置对象，包含：
 *   - history：路由模式（History 或 Hash）
 *   - routes：路由规则数组
 *
 * 【Vue Router 4 的新特性（相比 Vue Router 3）】
 * - 全面支持 TypeScript，类型推断更完善
 * - 动态路由添加（addRoute / removeRoute）
 * - 更简洁的导航守卫 API
 */
import { createRouter, createWebHistory } from 'vue-router'

/**
 * useUserStore — 用户状态管理 Store（Pinia）。
 *
 * 【为什么在路由文件中导入 Store？】
 * 在路由守卫中需要判断用户是否已登录，以决定是否允许访问某些页面。
 * 用户登录状态（token、用户信息）存储在 Pinia 的 userStore 中。
 *
 * 【注意】
 * 这里只在路由守卫函数（beforeEach）内部调用 useUserStore()，
 * 而不是在模块顶层调用。因为在模块顶层时，Pinia 可能还没有被安装到 Vue 应用中。
 */
import { useUserStore } from '@/stores/user'

// ============================================================================
// 创建路由实例
// ============================================================================

/**
 * 创建路由器实例，传入路由配置。
 *
 * 【配置解析】
 * - history：使用 HTML5 History API，URL 没有 # 号，更加美观
 * - routes：路由规则数组，定义了所有页面的 URL 映射
 */
const router = createRouter({
  /**
   * createWebHistory() — 使用 HTML5 History 模式。
   *
   * 【两种路由模式对比】
   * ┌──────────────┬─────────────────────┬─────────────────────┐
   * │   特性       │  History 模式       │  Hash 模式          │
   * ├──────────────┼─────────────────────┼─────────────────────┤
   * │ URL 样式     │ /dashboard          │ /#/dashboard        │
   * │ 是否需要#号  │ 否                  │ 是                  │
   * │ 服务器配置   │ 需要回退到index.html│ 不需要              │
   * │ SEO 友好     │ 是                  │ 否（#后内容不发送） │
   * │ 底层API      │ pushState/replaceState│ location.hash     │
   * └──────────────┴─────────────────────┴─────────────────────┘
   *
   * 【服务器配置要求】
   * 使用 History 模式时，需要配置服务器将所有请求都回退到 index.html，
   * 否则直接访问 /dashboard 会出现 404 错误。
   * Nginx 配置示例：try_files $uri $uri/ /index.html;
   */
  history: createWebHistory(),

  /**
   * routes — 路由规则数组。
   *
   * 每条路由规则包含以下常用字段：
   *   - path：URL 路径（支持动态参数，如 /user/:id）
   *   - name：路由名称（用于编程式导航：router.push({ name: 'Dashboard' })）
   *   - component：要渲染的组件（使用懒加载语法）
   *   - meta：路由元信息（自定义数据，可在守卫中访问）
   *   - redirect：重定向到另一个路径
   *   - children：子路由数组（嵌套路由）
   */
  routes: [
    // ========================================================================
    // 公开页面（不需要登录即可访问）
    // ========================================================================

    /**
     * 登录页面路由。
     *
     * 【路由配置解析】
     * - path: '/login' → 访问 http://域名/login 时匹配此路由
     * - name: 'Login' → 可以通过 router.push({ name: 'Login' }) 导航
     * - component: () => import('...') → 懒加载，只在访问 /login 时才下载该组件的代码
     * - meta.public: true → 标记为公开页面，路由守卫中用于判断是否需要登录
     * - meta.title → 页面标题，路由守卫中会设置为 document.title
     *
     * 【知识扩展：懒加载的原理】
     * () => import('@/views/login/LoginView.vue') 使用了 ES2020 的动态导入语法。
     * Vite 会将每个懒加载的组件打包成独立的 JS 文件（chunk），
     * 只有当用户首次导航到该路由时，浏览器才会请求并加载这个文件。
     * 这就是"按需加载"，可以显著提升首屏加载速度。
     *
     * 打包后的文件大致结构：
     *   dist/assets/LoginView-abc123.js     ← 登录页
     *   dist/assets/DashboardView-def456.js ← 数据看板
     *   dist/assets/PlanView-ghi789.js      ← 每日计划
     *   ...（每个页面一个独立文件）
     */
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/login/LoginView.vue'),
      meta: { public: true, title: '登录 - DailyTracker' }
    },

    /**
     * 注册页面路由。
     * 配置与登录页面类似，meta.public: true 表示不需要登录即可访问。
     */
    {
      path: '/register',
      name: 'Register',
      component: () => import('@/views/register/RegisterView.vue'),
      meta: { public: true, title: '注册 - DailyTracker' }
    },

    // ========================================================================
    // 主应用页面（需要登录后才能访问）
    // ========================================================================

    /**
     * 主应用的布局路由。
     *
     * 【嵌套路由结构】
     * 这个路由使用了 children 字段来定义子路由，形成了嵌套结构：
     *
     * App.vue
     *   └─ <router-view />  ← 这里渲染 AppLayout.vue
     *        ├─ 顶部导航栏（AppLayout 内部）
     *        ├─ 侧边菜单（AppLayout 内部）
     *        └─ <router-view />  ← 这里渲染子路由组件（如 DashboardView）
     *
     * 【redirect 配置】
     * redirect: '/dashboard' 表示当用户访问根路径 '/' 时，
     * 自动重定向到 '/dashboard'（数据看板页面）。
     * 这样用户打开网站后直接看到首页，而不是空白页。
     *
     * 【component 懒加载】
     * AppLayout.vue 是布局组件（包含顶部导航、侧边菜单等），
     * 也使用懒加载，因为访问根路径时才会需要它。
     */
    {
      path: '/',
      component: () => import('@/components/layout/AppLayout.vue'),
      redirect: '/dashboard',  // 访问 / 自动跳转到 /dashboard
      children: [
        /**
         * 数据看板页面 — 展示统计图表、数据概览等。
         * meta.icon 用于侧边栏菜单图标的显示。
         */
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('@/views/dashboard/DashboardView.vue'),
          meta: { title: '数据看板 - DailyTracker', icon: 'DataLine' }
        },

        /**
         * 每日计划页面 — 创建和管理每日任务/计划。
         */
        {
          path: 'plan',
          name: 'Plan',
          component: () => import('@/views/plan/PlanView.vue'),
          meta: { title: '每日计划 - DailyTracker', icon: 'Calendar' }
        },

        /**
         * 每日记账页面 — 记录每日收支。
         */
        {
          path: 'accounting',
          name: 'Accounting',
          component: () => import('@/views/accounting/AccountingView.vue'),
          meta: { title: '每日记账 - DailyTracker', icon: 'CreditCard' }
        },

        /**
         * 每日摘录页面 — 收集和整理阅读摘录/笔记。
         */
        {
          path: 'excerpt',
          name: 'Excerpt',
          component: () => import('@/views/excerpt/ExcerptView.vue'),
          meta: { title: '每日摘录 - DailyTracker', icon: 'Notebook' }
        },

        /**
         * 每日总结页面 — 撰写每日总结/日记。
         */
        {
          path: 'summary',
          name: 'Summary',
          component: () => import('@/views/summary/SummaryView.vue'),
          meta: { title: '每日总结 - DailyTracker', icon: 'EditPen' }
        },

        /**
         * 目标管理页面 — 设定和追踪长期/短期目标。
         */
        {
          path: 'goal',
          name: 'Goal',
          component: () => import('@/views/goal/GoalView.vue'),
          meta: { title: '目标管理 - DailyTracker', icon: 'TrophyBase' }
        },

        /**
         * 个人中心页面 — 用户信息管理、设置等。
         */
        {
          path: 'profile',
          name: 'Profile',
          component: () => import('@/views/profile/ProfileView.vue'),
          meta: { title: '个人中心 - DailyTracker', icon: 'User' }
        }
      ]
    },

    // ========================================================================
    // 404 页面处理
    // ========================================================================

    /**
     * 通配符路由 — 捕获所有未匹配的 URL。
     *
     * 【路径语法解析】
     * ':pathMatch(.*)*' 使用了 Vue Router 4 的动态参数语法：
     *   - :pathMatch → 定义一个名为 pathMatch 的动态参数
     *   - (.*) → 正则表达式，匹配任意字符
     *   - * → 重复修饰符，表示可以重复匹配（支持多级路径）
     *
     * 【工作原理】
     * 当用户访问一个不存在的 URL（如 /abc/xyz）时，
     * 前面定义的所有路由规则都不会匹配，最终落到这里。
     * 这里将用户重定向到 /dashboard（首页），避免出现空白页。
     *
     * 【Vue Router 3 vs 4 的区别】
     * Vue Router 3 使用 path: '*' 来匹配所有路径。
     * Vue Router 4 改为使用参数语法，更加灵活和明确。
     *
     * 【其他处理方式】
     * 也可以显示一个 404 页面：
     *   { path: '/:pathMatch(.*)*', name: 'NotFound', component: NotFoundView }
     */
    {
      path: '/:pathMatch(.*)*',
      redirect: '/dashboard'
    }
  ]
})

// ============================================================================
// 全局路由守卫（Navigation Guards）
// ============================================================================

/**
 * beforeEach — 全局前置守卫。
 *
 * 【工作原理】
 * 每次路由导航（页面跳转）之前，都会执行这个回调函数。
 * 它接收三个参数：
 *   - to：即将进入的目标路由对象（RouteLocationNormalized）
 *   - from：当前正要离开的路由对象（RouteLocationNormalized）
 *   - next：控制导航行为的函数（必须调用！）
 *
 * 【next() 函数的用法】
 * - next()：放行，继续导航到 to 指定的路由
 * - next('/login')：中断当前导航，重定向到 /login
 * - next(false)：中断当前导航，停留在当前页面
 * - next(new Error('...'))：导航失败，触发 router.onError 回调
 *
 * 【知识扩展：路由守卫的执行顺序】
 * 完整的导航解析流程：
 *   1. 导航被触发
 *   2. 调用失活组件的 beforeRouteLeave（组件内守卫）
 *   3. 调用全局的 beforeEach（就是这里）
 *   4. 调用重用组件的 beforeRouteUpdate
 *   5. 调用路由配置中的 beforeEnter
 *   6. 解析异步路由组件
 *   7. 调用激活组件的 beforeRouteEnter
 *   8. 导航被确认
 *   9. 调用全局的 afterEach
 *   10. 触发 DOM 更新
 *   11. 调用 beforeRouteEnter 的 next 回调
 *
 * 【本项目守卫逻辑】
 * 本守卫实现了简单的登录认证控制：
 *   - 公开页面（meta.public: true）：
 *     - 已登录用户 → 重定向到首页（避免重复登录）
 *     - 未登录用户 → 正常访问
 *   - 非公开页面（默认需要登录）：
 *     - 已登录用户 → 正常访问
 *     - 未登录用户 → 重定向到登录页
 */
router.beforeEach((to, _from, next) => {
  /**
   * 更新页面标题。
   *
   * 根据路由元信息中的 title 字段动态设置浏览器标签页标题。
   * 如果没有配置 title，则使用默认标题 'DailyTracker'。
   *
   * 【为什么要更新标题？】
   * - 用户体验：标签页标题能让用户快速识别当前页面
   * - SEO：搜索引擎会根据 <title> 标签理解页面内容
   * - 书签：用户收藏页面时，标题会作为默认书签名称
   */
  document.title = (to.meta.title as string) || 'DailyTracker'

  /**
   * 获取用户 Store 实例。
   * 这里在守卫内部调用 useUserStore()，确保 Pinia 已经安装完毕。
   */
  const userStore = useUserStore()

  /**
   * 根据路由的 meta.public 字段判断是否为公开页面。
   */
  if (to.meta.public) {
    /**
     * 公开页面的处理逻辑。
     *
     * 如果用户已经登录，还尝试访问登录/注册页面，
     * 则自动重定向到首页，避免已登录用户看到登录页面。
     * 这是一个常见的 UX 优化。
     */
    if (userStore.isLoggedIn) {
      next('/dashboard')  // 已登录 → 跳转首页
    } else {
      next()  // 未登录 → 正常访问公开页面
    }
  } else {
    /**
     * 非公开页面的处理逻辑。
     *
     * 如果用户未登录就尝试访问需要认证的页面，
     * 则重定向到登录页，登录成功后再跳回目标页面。
     *
     * 【改进建议】
     * 可以在跳转登录页时保存目标路径，登录成功后自动跳回：
     *   next({ path: '/login', query: { redirect: to.fullPath } })
     * 然后在登录成功后：
     *   router.push(route.query.redirect || '/dashboard')
     */
    if (!userStore.isLoggedIn) {
      next('/login')  // 未登录 → 跳转登录页
    } else {
      next()  // 已登录 → 正常访问
    }
  }
})

// ============================================================================
// 导出路由实例
// ============================================================================

/**
 * 导出路由实例，供 main.ts 注册到 Vue 应用中。
 *
 * 【ES Module 导出说明】
 * export default 是 ES6 模块的默认导出语法：
 *   - 一个模块只能有一个 default export
 *   - 导入时可以任意命名：import router from './router'
 *   - 无需使用花括号：import { createRouter } from 'vue-router' ← 命名导出用花括号
 */
export default router

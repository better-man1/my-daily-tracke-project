/**
 * ============================================================================
 * 【应用全局状态管理模块（App Store）】
 * ============================================================================
 *
 * 【模块用途】
 * 使用 Pinia 管理应用级别的全局状态，包括侧边栏状态、Loading 加载状态、
 * 当前日期、请求计数器、全局错误等。与 useUserStore 不同，
 * useAppStore 管理的是 UI 状态和应用行为，而非业务数据。
 *
 * 【设计思想】
 * 1. Loading 计数器模式：
 *    使用 requestCount 计数器而非简单的 boolean 来管理 Loading 状态。
 *    当有多个 API 请求同时进行时，计数器递增；请求完成后递减。
 *    只有当计数器归零时才隐藏 Loading。这样可以避免一个请求完成就把
 *    其他还在进行的请求的 Loading 提前关闭的问题。
 *
 * 2. 关注点分离：
 *    将应用 UI 状态（useAppStore）与用户业务状态（useUserStore）分离，
 *    遵循单一职责原则，使每个 Store 的职责清晰。
 *
 * 3. Pinia Setup Store 风格：
 *    使用 defineStore + setup 函数的写法，与 Vue 3 Composition API 风格一致。
 *    - ref() => state（可变状态）
 *    - computed() => getters（只读计算属性）
 *    - function => actions（操作方法）
 *
 * 【学习要点】
 * - Pinia Setup Store 的完整结构
 * - Loading 计数器模式（多请求并发时的 Loading 管理）
 * - ref、computed 在 Store 中的应用
 * - 日期字符串的处理方式
 * - Store 方法与 Axios 拦截器的协作（request.ts 调用 incrementRequest/decrementRequest）
 *
 * ============================================================================
 */

// ---- 【导入依赖】 ----

// defineStore：Pinia 的核心函数，用于定义 Store
import { defineStore } from 'pinia'

// ref：创建响应式状态
// computed：创建计算属性（带缓存的只读派生值）
import { ref, computed } from 'vue'

// ============================================================================
// 【Store 定义】
// ============================================================================

/**
 * useAppStore — 应用全局状态管理 Hook
 *
 * 管理应用的 UI 状态和行为，如侧边栏、Loading、日期选择、错误处理等。
 * 与 request.ts 紧密协作，通过请求计数器实现全局 Loading 管理。
 *
 * 使用方式：
 * ```ts
 * import { useAppStore } from '@/stores/app'
 *
 * const appStore = useAppStore()
 *
 * // 切换侧边栏
 * appStore.toggleSidebar()
 *
 * // 手动设置 Loading
 * appStore.setLoading(true, '正在保存...')
 *
 * // 检查是否有请求在进行中
 * if (appStore.isLoading) {
 *   console.log('有请求正在进行中')
 * }
 * ```
 */
export const useAppStore = defineStore('app', () => {

  // ==========================================================================
  // 【State（状态）】— 使用 ref() 定义
  // ==========================================================================

  /**
   * sidebarCollapsed — 侧边栏是否折叠
   *
   * - false：侧边栏展开（默认）
   * - true：侧边栏折叠，只显示图标
   *
   * 使用场景：在宽屏时展开侧边栏显示完整菜单，窄屏时折叠节省空间。
   */
  const sidebarCollapsed = ref(false)

  /**
   * loading — 全局手动 Loading 状态
   *
   * 与 isLoading（基于请求计数器）不同，这是手动控制的 Loading 状态。
   * 适用于需要在非 API 请求场景下显示 Loading 的情况，
   * 如：页面初始化、复杂的本地计算等。
   *
   * - false：不显示手动 Loading（默认）
   * - true：显示手动 Loading
   */
  const loading = ref(false)

  /**
   * loadingText — Loading 提示文字
   *
   * 显示在 Loading 遮罩层上的文字，默认为"加载中..."。
   * 可以通过 setLoading 方法的第二个参数自定义文字。
   */
  const loadingText = ref('加载中...')

  /**
   * currentDate — 当前选中的日期
   *
   * 用于全局日期筛选，多个页面共享此值。
   * 默认值为今天的日期（格式：'YYYY-MM-DD'）。
   *
   * 初始化方式：
   * new Date().toISOString() 返回如 '2024-01-15T08:30:00.000Z'
   * .split('T')[0] 提取日期部分 '2024-01-15'
   *
   * 使用场景：
   * - 计划页面：显示选中日期的任务列表
   * - 记账页面：显示选中日期的收支记录
   * - 总结页面：显示选中日期的每日总结
   */
  const currentDate = ref(new Date().toISOString().split('T')[0])

  /**
   * requestCount — 正在进行中的 API 请求计数
   *
   * 这是实现 Loading 计数器模式的核心变量。
   *
   * 工作原理：
   * - 当 API 请求发出时，request.ts 的请求拦截器调用 incrementRequest()，计数器 +1
   * - 当 API 响应返回时（无论成功或失败），request.ts 的响应拦截器调用 decrementRequest()，计数器 -1
   * - isLoading 计算属性根据 requestCount > 0 判断是否显示全局 Loading
   *
   * 为什么不直接用 boolean？
   * 假设有三个请求 A、B、C 同时发出：
   * - A 请求开始：count = 1（显示 Loading）
   * - B 请求开始：count = 2（继续显示 Loading）
   * - C 请求开始：count = 3（继续显示 Loading）
   * - A 请求完成：count = 2（继续显示 Loading，因为 B、C 还在进行）
   * - B 请求完成：count = 1（继续显示 Loading，因为 C 还在进行）
   * - C 请求完成：count = 0（隐藏 Loading）
   * 如果用 boolean，A 完成时就会把 Loading 关闭，导致 B、C 的 Loading 消失。
   */
  const requestCount = ref(0)

  /**
   * globalError — 全局错误对象
   *
   * 存储最近一次的全局错误，用于全局错误展示。
   * null 表示没有错误。
   *
   * 使用场景：
   * - 捕获未处理的 Promise 异常
   * - 显示全局错误边界组件
   */
  const globalError = ref<Error | null>(null)

  // ==========================================================================
  // 【Getters（计算属性）】— 使用 computed() 定义
  // ==========================================================================

  /**
   * isLoading — 是否有请求正在进行中
   *
   * 基于 requestCount 计数器判断，只要有任何请求在进行中就返回 true。
   * 通常用于控制全局 Loading 遮罩层的显示/隐藏。
   *
   * @returns {boolean}
   *   - true：有 API 请求正在进行中，应显示 Loading
   *   - false：所有请求已完成，应隐藏 Loading
   */
  const isLoading = computed(() => requestCount.value > 0)

  // ==========================================================================
  // 【Actions（操作方法）】— 使用普通函数定义
  // ==========================================================================

  /**
   * toggleSidebar — 切换侧边栏的折叠/展开状态
   *
   * 取反 sidebarCollapsed 的值：
   * - true => false（展开）
   * - false => true（折叠）
   */
  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  /**
   * setLoading — 手动设置 Loading 状态
   *
   * @param {boolean} val  - Loading 是否显示
   * @param {string} [text] - Loading 提示文字（可选）
   *
   * 使用示例：
   * ```ts
   * appStore.setLoading(true, '正在保存数据...')  // 显示 Loading
   * // ... 执行耗时操作 ...
   * appStore.setLoading(false)                     // 隐藏 Loading
   * ```
   */
  function setLoading(val: boolean, text?: string) {
    loading.value = val
    if (text) loadingText.value = text
  }

  /**
   * setCurrentDate — 设置当前选中的日期
   *
   * @param {string} date - 日期字符串（格式：'YYYY-MM-DD'）
   *
   * 设置后，所有使用 currentDate 的组件会自动更新显示内容。
   */
  function setCurrentDate(date: string) {
    currentDate.value = date
  }

  // ---- 请求计数管理方法 ----

  /**
   * incrementRequest — 递增请求计数器
   *
   * 由 request.ts 的请求拦截器调用（每次发出 API 请求时）。
   * 计数器 +1，isLoading 变为 true（如果之前是 0）。
   */
  function incrementRequest() {
    requestCount.value++
  }

  /**
   * decrementRequest — 递减请求计数器
   *
   * 由 request.ts 的响应拦截器调用（每次 API 响应返回时）。
   * 计数器 -1（但不低于 0），当归零时 isLoading 变为 false。
   *
   * 防御性编程：if (requestCount.value > 0) 确保不会出现负数。
   * 在极端情况下（如请求拦截器被跳过），可能会出现多余的 decrement 调用。
   */
  function decrementRequest() {
    if (requestCount.value > 0) {
      requestCount.value--
    }
  }

  /**
   * resetRequestCount — 重置请求计数器为 0
   *
   * 在某些异常情况下（如路由切换时），可能需要强制重置计数器，
   * 确保 Loading 不会卡在显示状态。
   */
  function resetRequestCount() {
    requestCount.value = 0
  }

  // ---- 错误处理方法 ----

  /**
   * setError — 设置全局错误
   *
   * @param {Error | null} error - 错误对象，传 null 表示清除错误
   */
  function setError(error: Error | null) {
    globalError.value = error
  }

  /**
   * clearError — 清除全局错误
   *
   * 等价于 setError(null)，更语义化的写法。
   */
  function clearError() {
    globalError.value = null
  }

  // ==========================================================================
  // 【返回值】— 暴露给外部的状态、计算属性和方法
  // ==========================================================================
  return {
    // ---- 状态（State）----
    sidebarCollapsed,   // 侧边栏折叠状态
    loading,            // 手动 Loading 状态
    loadingText,        // Loading 提示文字
    currentDate,        // 当前选中日期
    requestCount,       // 请求计数器
    globalError,        // 全局错误对象

    // ---- 计算属性（Getters）----
    isLoading,          // 是否有请求在进行中

    // ---- 方法（Actions）----
    toggleSidebar,      // 切换侧边栏
    setLoading,         // 设置手动 Loading
    setCurrentDate,     // 设置当前日期
    incrementRequest,   // 递增请求计数
    decrementRequest,   // 递减请求计数
    resetRequestCount,  // 重置请求计数
    setError,           // 设置全局错误
    clearError          // 清除全局错误
  }
})

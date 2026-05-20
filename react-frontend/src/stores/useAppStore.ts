/**
 * ============================================================================
 * 【应用级全局状态管理模块 (src/stores/useAppStore.ts)】
 * ============================================================================
 *
 * 【知识点解析：0-1 学习 React】
 *
 * 1. 为什么需要全局状态管理？
 *    - 在 React 中，组件状态（useState）是局部的、私有的。
 *    - 如果多个没有任何父子关系的组件（比如 Axios 拦截器、根布局组件、某个深层的业务卡片）
 *      需要共享同一个状态（如：当前是否正在加载数据？），使用常规的传参极其困难。
 *    - 全局状态库允许我们在组件树外部建立一个“公共状态池”（Store），任何组件都可以直接从池中
 *      存取数据或调用方法，且同样具有 React 的响应式更新能力。
 *
 * 2. 什么是 Zustand？
 *    - Zustand 是目前 React 社区非常流行的轻量级全局状态管理库。
 *    - **对比 Redux**：极其简单，不需要写大量的 Action、Reducer、Dispatch 等样板代码，
 *      也不需要在 Root 组件外层套上 `<Provider>` 容器。
 *    - **对比 Context**：不会导致整颗组件树无脑重新渲染，支持“选择器（Selector）”机制，
 *      只有订阅的具体属性改变时，组件才会重新渲染，性能表现极其优异。
 *
 * 3. 【React 核心概念：选择器订阅模式 (Selector Subscription)】
 *    - 使用 Zustand Store 时，我们通过传入一个选择器函数来订阅状态：
 *      `const isLoading = useAppStore(state => state.isLoading())`
 *    - 这里的 `state => state.isLoading()` 就是选择器。Zustand 会监控这个表达式的返回值，
 *      只有当 `isLoading()` 的值从 `false` 变成 `true`（或反过来）时，使用该 Hook 的组件才会重新渲染。
 *      如果 Store 里的其他状态（比如未来增加的其它应用配置）变了，该组件**不会**发生无意义的重新渲染。
 *
 * 4. 【系统设计模式：请求计数器 (Request Counter) 模式】
 *    - 场景：在单页应用中，页面可能会同时并发发出 3 个接口请求。
 *    - 如果简单用布尔值 `isLoading: true/false` 控制：
 *      - 请求 A 开始：`isLoading = true`（显示 loading）
 *      - 请求 B 开始：`isLoading = true`
 *      - 请求 A 完成：`isLoading = false`（Loading 遮罩关闭！但此时请求 B 还在运行中，页面出现残缺！）
 *    - 计数器解决方案：
 *      - 维护一个数字 `requestCount`。每个请求开始时 `+1`，完成时 `-1`。
 *      - 只要 `requestCount > 0` 就显示 Loading。只有当所有并发请求都结束，计数器归零时，才关闭 Loading。
 */

import { create } from 'zustand'

/**
 * AppState — 定义状态库的 TypeScript 接口类型
 * 明确列出状态库里有哪些数据（State）和哪些改变状态的方法（Actions）
 */
interface AppState {
  // 正在进行中的 API 请求数量
  requestCount: number

  // 增加请求计数器（在 Axios 请求拦截器中调用）
  incrementRequest: () => void

  // 减少请求计数器（在 Axios 响应拦截器或异常捕获中调用）
  decrementRequest: () => void

  // 计算属性：当前是否处于加载状态
  isLoading: () => boolean
}

/**
 * useAppStore — Zustand 状态管理 Hook
 *
 * `create<AppState>((set, get) => ({ ... }))`
 * - `set`：用于修改 Store 中的状态值。在 Zustand 中，状态是只读的，必须通过 `set` 统一修改。
 * - `get`：用于在方法内部获取当前 Store 里的最新状态。
 */
export const useAppStore = create<AppState>((set, get) => ({
  // ---- 状态数据（State）----
  requestCount: 0,

  // ---- 改变状态的方法（Actions）----

  /**
   * 增加请求计数
   * 使用 set 函数，接受一个回调函数，回调参数 state 即为当前的最新状态。
   * 必须返回一个包含要修改字段的新对象（Zustand 会自动与旧状态做浅层合并）。
   */
  incrementRequest: () => set((state) => ({ requestCount: state.requestCount + 1 })),

  /**
   * 减少请求计数
   * 使用 Math.max(0, ...) 兜底，防止因异常处理不当导致计数器扣减成负数。
   */
  decrementRequest: () => set((state) => ({ requestCount: Math.max(0, state.requestCount - 1) })),

  // ---- 计算属性（Getters）----

  /**
   * 判断是否显示加载遮罩
   * get() 可以直接拿到当前整个 Store，从中提取 requestCount 进行计算
   */
  isLoading: () => get().requestCount > 0
}))
/**
 * ============================================================================
 * 【应用状态管理模块（App Store）】
 * ============================================================================
 *
 * 【模块用途】
 * 使用 Zustand 管理应用级别的全局状态，主要管理 Loading 状态。
 * 这是整个应用中最轻量级的 Store，主要用于控制全局 Loading 遮罩的显示。
 *
 * 【设计思想】
 * 1. Zustand 的简洁性：
 *    Zustand 是一个轻量级的状态管理库，相比 Redux 更加简洁。
 *    不需要创建 Provider，不需要 action/reducer，直接使用 hooks 即可。
 *
 * 2. 请求计数器模式：
 *    为了正确控制 Loading 遮罩，使用请求计数器而不是布尔值。
 *    - 每个请求发出前：incrementRequest()
 *    - 每个请求完成后：decrementRequest()
 *    - 当计数器 > 0 时显示 Loading
 *    - 这样可以处理多个并发请求的情况
 *
 * 【学习要点】
 * - Zustand 的基本用法（create、getState、setState）
 * - React 组件中使用 Zustand store
 * - 请求计数器模式的实现
 *
 * ============================================================================
 */

import { create } from 'zustand'

/**
 * AppState — 应用状态类型定义
 */
interface AppState {
  /**
   * requestCount — 当前正在进行的请求数量
   * 用于判断是否应该显示全局 Loading 遮罩
   */
  requestCount: number

  /**
   * incrementRequest — 增加请求计数器
   * 在请求发出前调用
   */
  incrementRequest: () => void

  /**
   * decrementRequest — 减少请求计数器
   * 在请求完成后调用
   */
  decrementRequest: () => void

  /**
   * isLoading — 是否正在加载
   * 计算属性，当 requestCount > 0 时返回 true
   */
  isLoading: () => boolean
}

/**
 * useAppStore — 应用状态管理 Hook
 *
 * 使用 Zustand 的 create 函数创建 store。
 * 参数是一个函数，返回 store 的状态和方法。
 *
 * 使用方式：
 * ```tsx
 * import { useAppStore } from '@/stores/useAppStore'
 *
 * function Component() {
 *   const isLoading = useAppStore(state => state.isLoading())
 *   const incrementRequest = useAppStore(state => state.incrementRequest)
 *
 *   return (
 *     <div>
 *       {isLoading && <Spin />}
 *     </div>
 *   )
 * }
 * ```
 */
export const useAppStore = create<AppState>((set, get) => ({
  // ---- 状态（State）----

  /**
   * requestCount — 请求计数器初始值为 0
   */
  requestCount: 0,

  // ---- 方法（Actions）----

  /**
   * incrementRequest — 增加请求计数器
   *
   * 使用 set() 更新状态，通过函数式更新确保正确性：
   * (state) => ({ ...state, requestCount: state.requestCount + 1 })
   */
  incrementRequest: () => set((state) => ({ ...state, requestCount: state.requestCount + 1 })),

  /**
   * decrementRequest — 减少请求计数器
   *
   * 使用函数式更新，确保不会出现负数：
   * Math.max(0, state.requestCount - 1)
   */
  decrementRequest: () => set((state) => ({ ...state, requestCount: Math.max(0, state.requestCount - 1) })),

  /**
   * isLoading — 判断是否正在加载
   *
   * 使用 get() 获取当前状态，而不是通过 set()。
   * 这是一个计算属性，每次调用时动态计算。
   */
  isLoading: () => get().requestCount > 0
}))
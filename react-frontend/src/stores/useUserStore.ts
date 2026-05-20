/**
 * ============================================================================
 * 【用户状态管理模块（User Store）】
 * ============================================================================
 *
 * 【模块用途】
 * 使用 Zustand 管理用户相关的全局状态，包括用户信息、认证 Token、
 * 登录状态判断等。这是整个应用中最核心的 Store 之一。
 *
 * 【设计思想】
 * 1. Zustand 的简洁性：
 *    Zustand 是一个轻量级的状态管理库，相比 Redux 更加简洁。
 *    不需要创建 Provider，不需要 action/reducer，直接使用 hooks 即可。
 *
 * 2. 持久化策略：
 *    用户信息和 Token 同时存储在 Zustand（内存）和 localStorage（磁盘）中。
 *    - Zustand：提供响应式数据，页面刷新后丢失
 *    - localStorage：提供持久化存储，页面刷新后恢复
 *    - 初始化时从 localStorage 读取数据到 Zustand，确保刷新后状态不丢失
 *
 * 3. Partial<T> 的使用：
 *    userInfo 使用 Partial<LoginResponse> 类型，因为初始化时可能为空对象 {}，
 *    而登录后才包含完整数据。
 *
 * 【学习要点】
 * - Zustand 的基本用法（create、getState、setState）
 * - React 组件中使用 Zustand store
 * - localStorage 与 Zustand 状态的同步策略
 * - TypeScript Partial<T> 工具类型的应用
 *
 * ============================================================================
 */

import { create } from 'zustand'
import type { LoginResponse } from '@/api/auth'

/**
 * UserState — 用户状态类型定义
 */
interface UserState {
  /**
   * userInfo — 用户信息对象
   * 存储 LoginResponse 中的用户基本信息。使用 Partial<LoginResponse> 类型
   * 是因为在未登录时这是一个空对象 {}。
   */
  userInfo: Partial<LoginResponse>

  /**
   * accessToken — 访问令牌
   * 存储当前的 JWT Access Token，用于 API 请求的身份认证。
   */
  accessToken: string

  /**
   * refreshToken — 刷新令牌
   * 存储当前的 JWT Refresh Token，用于在 Access Token 过期后获取新 Token。
   */
  refreshToken: string

  /**
   * isLoggedIn — 是否已登录
   * 计算属性，通过检查 accessToken 是否存在来判断
   */
  isLoggedIn: () => boolean

  /**
   * userId — 当前用户 ID
   * 计算属性，从 userInfo 中提取
   */
  userId: () => number | undefined

  /**
   * username — 当前用户名
   * 计算属性，从 userInfo 中提取
   */
  username: () => string | undefined

  /**
   * nickname — 当前用户昵称
   * 计算属性，优先显示 nickname，如果未设置则回退显示 username
   */
  nickname: () => string | undefined

  /**
   * avatar — 当前用户头像 URL
   * 计算属性，可能为 undefined（未设置头像）
   */
  avatar: () => string | null | undefined

  /**
   * setLoginData — 保存登录数据
   * 在用户登录成功后调用，将登录响应数据保存到 Zustand 状态和 localStorage 中。
   */
  setLoginData: (data: LoginResponse) => void

  /**
   * logout — 退出登录
   * 清除所有用户状态和持久化数据，将应用恢复到未登录状态。
   */
  logout: () => void

  /**
   * updateUserInfo — 部分更新用户信息
   * 在用户修改个人资料后调用，只更新变化的字段。
   */
  updateUserInfo: (data: Partial<LoginResponse>) => void
}

/**
 * useUserStore — 用户状态管理 Hook
 *
 * 使用 Zustand 的 create 函数创建 store。
 * 参数是一个函数，返回 store 的状态和方法。
 *
 * 使用方式：
 * ```tsx
 * import { useUserStore } from '@/stores/useUserStore'
 *
 * function Component() {
 *   const isLoggedIn = useUserStore(state => state.isLoggedIn())
 *   const nickname = useUserStore(state => state.nickname())
 *   const logout = useUserStore(state => state.logout)
 *
 *   return (
 *     <div>
 *       {isLoggedIn ? (
 *         <>
 *           <span>{nickname}</span>
 *           <button onClick={logout}>退出登录</button>
 *         </>
 *       ) : (
 *         <Link to="/login">登录</Link>
 *       )}
 *     </div>
 *   )
 * }
 * ```
 */
export const useUserStore = create<UserState>((set, get) => ({
  // ---- 状态（State）----

  /**
   * userInfo — 用户信息对象
   * 从 localStorage 初始化，确保页面刷新后状态不丢失
   */
  userInfo: JSON.parse(localStorage.getItem('user_info') || '{}'),

  /**
   * accessToken — 访问令牌
   * 从 localStorage 初始化
   */
  accessToken: localStorage.getItem('access_token') || '',

  /**
   * refreshToken — 刷新令牌
   * 从 localStorage 初始化
   */
  refreshToken: localStorage.getItem('refresh_token') || '',

  // ---- 计算属性（Getters）----

  /**
   * isLoggedIn — 是否已登录
   * 通过检查 accessToken 是否存在来判断用户是否已登录。
   * !! 双重否定将 string 转换为 boolean：
   *   - '' => false（未登录）
   *   - 'eyJhbG...' => true（已登录）
   */
  isLoggedIn: () => !!get().accessToken,

  /**
   * userId — 当前用户 ID
   * 从 userInfo 中提取 userId
   */
  userId: () => get().userInfo.userId,

  /**
   * username — 当前用户名
   * 从 userInfo 中提取 username
   */
  username: () => get().userInfo.username,

  /**
   * nickname — 当前用户昵称
   * 优先显示 nickname，如果未设置则回退显示 username
   */
  nickname: () => get().userInfo.nickname || get().userInfo.username,

  /**
   * avatar — 当前用户头像 URL
   * 可能为 undefined（未设置头像），前端需要处理空值显示默认头像
   */
  avatar: () => get().userInfo.avatar,

  // ---- 方法（Actions）----

  /**
   * setLoginData — 保存登录数据
   *
   * 在用户登录成功后调用，将登录响应数据保存到 Zustand 状态和 localStorage 中。
   *
   * 双写策略：
   * 1. 更新 Zustand 状态（set()）— 使 React 组件响应式更新
   * 2. 写入 localStorage（localStorage.setItem）— 实现持久化存储
   *
   * @param data - 登录成功后后端返回的完整数据
   *   包含：userId, username, nickname, avatar, accessToken, refreshToken, accessTokenExpireIn
   */
  setLoginData: (data: LoginResponse) => {
    // 更新 Zustand 状态
    set({
      userInfo: data,
      accessToken: data.accessToken,
      refreshToken: data.refreshToken
    })
    // 同步到 localStorage（持久化）
    localStorage.setItem('user_info', JSON.stringify(data))
    localStorage.setItem('access_token', data.accessToken)
    localStorage.setItem('refresh_token', data.refreshToken)
  },

  /**
   * logout — 退出登录
   *
   * 清除所有用户状态和持久化数据，将应用恢复到未登录状态。
   * 通常在用户点击"退出登录"按钮时调用。
   *
   * 清除的内容：
   * - Zustand 状态：userInfo、accessToken、refreshToken 重置为空
   * - localStorage：删除 user_info、access_token、refresh_token 三个键
   *
   * 注意：此方法只清除前端数据，不会通知后端使 Token 失效。
   * 如果需要更高的安全性，可以在此方法中调用后端的 logout API。
   */
  logout: () => {
    // 清除 Zustand 状态
    set({
      userInfo: {},
      accessToken: '',
      refreshToken: ''
    })
    // 清除 localStorage 持久化数据
    localStorage.removeItem('user_info')
    localStorage.removeItem('access_token')
    localStorage.removeItem('refresh_token')
  },

  /**
   * updateUserInfo — 部分更新用户信息
   *
   * 在用户修改个人资料后调用，只更新变化的字段。
   * 使用对象展开运算符合并新旧数据。
   *
   * @param data - 要更新的字段（只传需要修改的）
   */
  updateUserInfo: (data: Partial<LoginResponse>) => {
    const newUserInfo = { ...get().userInfo, ...data }
    // 更新 Zustand 状态
    set({ userInfo: newUserInfo })
    // 同步更新 localStorage
    localStorage.setItem('user_info', JSON.stringify(newUserInfo))
  }
}))
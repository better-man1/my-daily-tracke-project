/**
 * ============================================================================
 * 【用户认证与全局状态管理模块 (src/stores/useUserStore.ts)】
 * ============================================================================
 *
 * 【知识点解析：0-1 学习 React】
 *
 * 1. 为什么用户状态需要全局化和持久化？
 *    - 用户是否登录（`isLoggedIn`）决定了路由守卫是否放行、侧边栏菜单展示哪些内容、以及全局请求头是否携带 JWT 令牌。
 *    - 仅存在内存中（Zustand/useState）的状态，会在用户**手动刷新网页时瞬间丢失**。
 *    - 因此，必须采用“内存（Zustand）与物理存储（localStorage）双写同步”的持久化策略：
 *      - 初始化时：优先从 localStorage 读取数据并填充到 Zustand。
 *      - 登录成功时：同步存入 Zustand 和 localStorage。
 *      - 退出登录时：同步清空 Zustand 和 localStorage。
 *
 * 2. 【TypeScript 实战技巧：Partial<T> 工具类型】
 *    - 接口 `LoginResponse` 定义了登录成功后后端返回的完整字段（含头像、用户名等）。
 *    - 但在未登录状态下，用户对象是个空对象 `{}`，此时不满足 `LoginResponse` 的必填属性契约。
 *    - `Partial<LoginResponse>` 会将 `LoginResponse` 中的所有属性都变为“可选”（即带 `?`），
 *      这样初始化为 `{}` 时才不会触发 TS 报错。
 *
 * 3. 什么是 Zustand 中的 Getters（计算属性）？
 *    - Zustand 没有直接提供 Vuex 那样的 getters 字段，但我们可以通过定义返回值的“无参函数”来变相实现：
 *      `isLoggedIn: () => !!get().accessToken`
 *    - 外部组件使用时，只需调用 `const isLoggedIn = useUserStore(state => state.isLoggedIn())` 即可。
 *    - 其中的 `!!`（双重否定）是 JS 常用技巧：将字符串转为布尔值。`!!""` 为 `false`，`!!"token"` 为 `true`。
 */

import { create } from 'zustand'
import type { LoginResponse } from '@/api/auth'

/**
 * UserState — 用户状态管理接口定义
 */
interface UserState {
  // 用户基本信息，初始化可能为空对象，故使用 Partial
  userInfo: Partial<LoginResponse>

  // 访问令牌，用于 Axios 请求拦截器中自动附加 Authorization 头
  accessToken: string

  // 刷新令牌，用于主令牌过期后的静默刷新（无感登录）
  refreshToken: string

  // 计算属性：判断当前是否已登录
  isLoggedIn: () => boolean

  // 计算属性：安全地获取当前用户的 ID
  userId: () => number | undefined

  // 计算属性：获取当前用户名
  username: () => string | undefined

  // 计算属性：获取用户昵称（若无则降级显示用户名）
  nickname: () => string | undefined

  // 计算属性：获取头像 URL
  avatar: () => string | null | undefined

  // 动作方法：登录成功后存储整套认证数据
  setLoginData: (data: LoginResponse) => void

  // 动作方法：退出登录，清理内存和本地物理磁盘的存储
  logout: () => void

  // 动作方法：当用户在个人中心修改头像或昵称时，局部更新信息
  updateUserInfo: (data: Partial<LoginResponse>) => void
}

/**
 * useUserStore — 创建全局用户状态 Hook
 */
export const useUserStore = create<UserState>((set, get) => ({
  // ============================================================================
  // ---- 状态数据（State） ----
  // ============================================================================

  // 惰性加载：初始化时直接从磁盘读取。即使刷新页面，状态也能完好无损地复原
  userInfo: JSON.parse(localStorage.getItem('user_info') || '{}'),
  accessToken: localStorage.getItem('access_token') || '',
  refreshToken: localStorage.getItem('refresh_token') || '',

  // ============================================================================
  // ---- 计算属性（Getters） ----
  // ============================================================================

  // 基于当前 accessToken 的有无来计算登录状态
  isLoggedIn: () => !!get().accessToken,

  userId: () => get().userInfo.userId,

  username: () => get().userInfo.username,

  nickname: () => get().userInfo.nickname || get().userInfo.username,

  avatar: () => get().userInfo.avatar,

  // ============================================================================
  // ---- 动作方法（Actions） ----
  // ============================================================================

  /**
   * 保存登录数据
   * @param data 登录成功后，API 接口返回的用户令牌及基础资料
   */
  setLoginData: (data: LoginResponse) => {
    // 1. 更新内存状态，通知所有订阅了该 Store 的 React 组件进行响应式界面重绘
    set({
      userInfo: data,
      accessToken: data.accessToken,
      refreshToken: data.refreshToken
    })
    // 2. 同步写入浏览器的物理存储（localStorage），防止页面刷新丢失
    localStorage.setItem('user_info', JSON.stringify(data))
    localStorage.setItem('access_token', data.accessToken)
    localStorage.setItem('refresh_token', data.refreshToken)
  },

  /**
   * 退出登录
   * 将所有的状态数据还原，并彻底从 localStorage 中抹去
   */
  logout: () => {
    // 1. 重置 Zustand 中的全局状态为初始空值
    set({
      userInfo: {},
      accessToken: '',
      refreshToken: ''
    })
    // 2. 擦除本地物理存储，防止下一次无凭证自动登录
    localStorage.removeItem('user_info')
    localStorage.removeItem('access_token')
    localStorage.removeItem('refresh_token')
  },

  /**
   * 局部更新用户信息（例如在个人中心修改了昵称或上传了新头像）
   * @param data 仅包含需要修改的字段，例如 { nickname: '新昵称' }
   */
  updateUserInfo: (data: Partial<LoginResponse>) => {
    // 使用对象解构展开展开合并：保留旧的 userInfo 信息，用传入的 data 覆盖已修改的字段
    const newUserInfo = { ...get().userInfo, ...data }
    
    // 更新内存状态
    set({ userInfo: newUserInfo })
    // 同步更新本地存储
    localStorage.setItem('user_info', JSON.stringify(newUserInfo))
  }
}))
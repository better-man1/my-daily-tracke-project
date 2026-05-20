/**
 * ============================================================================
 * 【用户状态管理模块（User Store）】
 * ============================================================================
 *
 * 【模块用途】
 * 使用 Pinia 管理用户相关的全局状态，包括用户信息、认证 Token、
 * 登录状态判断等。这是整个应用中最核心的 Store 之一。
 *
 * 【设计思想】
 * 1. Pinia Composition API 风格（Setup Store）：
 *    使用 defineStore + 函数式写法（类似 Vue 3 的 setup()），
 *    而非 Options API 风格（state/getters/actions）。
 *    - ref() 定义的状态 === Options 风格的 state
 *    - computed() 定义的计算属性 === Options 风格的 getters
 *    - function 定义的函数 === Options 风格的 actions
 *
 * 2. 持久化策略：
 *    用户信息和 Token 同时存储在 Pinia（内存）和 localStorage（磁盘）中。
 *    - Pinia：提供响应式数据，页面刷新后丢失
 *    - localStorage：提供持久化存储，页面刷新后恢复
 *    - 初始化时从 localStorage 读取数据到 Pinia，确保刷新后状态不丢失
 *
 * 3. Partial<T> 的使用：
 *    userInfo 使用 Partial<LoginResponse> 类型，因为初始化时可能为空对象 {}，
 *    而登录后才包含完整数据。
 *
 * 【学习要点】
 * - Pinia Setup Store 的定义方式（defineStore + setup 函数）
 * - ref() 和 computed() 在 Store 中的使用
 * - localStorage 与 Pinia 状态的同步策略
 * - TypeScript Partial<T> 工具类型的应用
 * - Store 的导出和使用方式
 *
 * ============================================================================
 */

// ---- 【导入依赖】 ----

// defineStore：Pinia 的核心函数，用于定义一个 Store
// 每个 Store 需要一个唯一的 ID（这里使用 'user'）和一个 setup 函数
import { defineStore } from 'pinia'

// ref：Vue 3 的响应式引用，用于创建响应式状态
//   - 当 ref 的值发生变化时，所有使用该值的地方会自动更新
// computed：Vue 3 的计算属性，用于根据其他响应式状态派生新值
//   - 具有缓存特性，只在依赖项变化时重新计算
import { ref, computed } from 'vue'

// LoginResponse：登录响应的类型定义，从 auth API 模块导入
// import type 表示只导入类型，不会增加运行时代码体积
import type { LoginResponse } from '@/api/auth'

// ============================================================================
// 【Store 定义】
// ============================================================================

/**
 * useUserStore — 用户状态管理 Hook
 *
 * 使用 Pinia 的 Composition API 风格（Setup Store）定义。
 * 函数名约定以 use 开头，这是 Vue 3 Composition API 的命名约定。
 *
 * defineStore 的两个参数：
 * - 'user'：Store 的唯一 ID，用于 DevTools 调试和 SSR 时识别
 * - () => { ... }：Setup 函数，返回要暴露的状态、计算属性和方法
 *
 * 使用方式：
 * ```ts
 * import { useUserStore } from '@/stores/user'
 *
 * // 在组件中使用（注意：必须在 setup 中调用）
 * const userStore = useUserStore()
 *
 * // 读取状态
 * console.log(userStore.isLoggedIn)  // 是否已登录
 * console.log(userStore.nickname)    // 用户昵称
 *
 * // 调用方法
 * userStore.setLoginData(loginResponse)
 * userStore.logout()
 * ```
 */
export const useUserStore = defineStore('user', () => {

  // ==========================================================================
  // 【State（状态）】— 使用 ref() 定义
  // ==========================================================================

  /**
   * userInfo — 用户信息对象
   *
   * 存储 LoginResponse 中的用户基本信息。使用 Partial<LoginResponse> 类型
   * 是因为在未登录时这是一个空对象 {}。
   *
   * 初始化逻辑：
   * 1. 尝试从 localStorage 读取 'user_info' 键的值
   * 2. 如果存在，解析 JSON 字符串为对象
   * 3. 如果不存在，使用空对象 {} 作为默认值
   *
   * 类型：Partial<LoginResponse> 表示 LoginResponse 的所有字段都是可选的
   * 这是因为：初始状态为 {}，不包含任何字段；登录后才包含完整数据
   */
  const userInfo = ref<Partial<LoginResponse>>(
    JSON.parse(localStorage.getItem('user_info') || '{}')
  )

  /**
   * accessToken — 访问令牌
   *
   * 存储当前的 JWT Access Token，用于 API 请求的身份认证。
   * 从 localStorage 初始化，确保页面刷新后 Token 不丢失。
   */
  const accessToken = ref<string>(localStorage.getItem('access_token') || '')

  /**
   * refreshToken — 刷新令牌
   *
   * 存储当前的 JWT Refresh Token，用于在 Access Token 过期后获取新 Token。
   * 同样从 localStorage 初始化。
   */
  const refreshToken = ref<string>(localStorage.getItem('refresh_token') || '')

  // ==========================================================================
  // 【Getters（计算属性）】— 使用 computed() 定义
  // ==========================================================================

  /**
   * isLoggedIn — 是否已登录
   *
   * 通过检查 accessToken 是否存在来判断用户是否已登录。
   * !! 双重否定将 string 转换为 boolean：
   *   - '' => false（未登录）
   *   - 'eyJhbG...' => true（已登录）
   *
   * 使用场景：
   * - 路由守卫中判断是否需要跳转登录页
   * - 导航栏中显示"登录"按钮还是"用户头像"
   */
  const isLoggedIn = computed(() => !!accessToken.value)

  /**
   * userId — 当前用户 ID
   *
   * 从 userInfo 中提取 userId。
   * 类型为 number | undefined（因为 userInfo 是 Partial<LoginResponse>）
   */
  const userId = computed(() => userInfo.value.userId)

  /**
   * username — 当前用户名
   *
   * 从 userInfo 中提取 username。
   */
  const username = computed(() => userInfo.value.username)

  /**
   * nickname — 当前用户昵称
   *
   * 优先显示 nickname，如果未设置则回退显示 username。
   * 这是一个常见的 "显示名称" 策略。
   */
  const nickname = computed(() => userInfo.value.nickname || userInfo.value.username)

  /**
   * avatar — 当前用户头像 URL
   *
   * 可能为 undefined（未设置头像），前端需要处理空值显示默认头像。
   */
  const avatar = computed(() => userInfo.value.avatar)

  // ==========================================================================
  // 【Actions（操作方法）】— 使用普通函数定义
  // ==========================================================================

  /**
   * setLoginData — 保存登录数据
   *
   * 在用户登录成功后调用，将登录响应数据保存到 Pinia 状态和 localStorage 中。
   *
   * 双写策略：
   * 1. 更新 Pinia 状态（ref.value = newValue）— 使 Vue 组件响应式更新
   * 2. 写入 localStorage（localStorage.setItem）— 实现持久化存储
   *
   * @param {LoginResponse} data - 登录成功后后端返回的完整数据
   *   包含：userId, username, nickname, avatar, accessToken, refreshToken, accessTokenExpireIn
   *
   * 调用示例：
   * ```ts
   * const loginRes = await authApi.login({ username, password })
   * userStore.setLoginData(loginRes)  // 保存登录数据
   * router.push('/')                  // 跳转到首页
   * ```
   */
  function setLoginData(data: LoginResponse) {
    // 更新 Pinia 状态
    userInfo.value = data
    accessToken.value = data.accessToken
    refreshToken.value = data.refreshToken
    // 同步到 localStorage（持久化）
    localStorage.setItem('user_info', JSON.stringify(data))
    localStorage.setItem('access_token', data.accessToken)
    localStorage.setItem('refresh_token', data.refreshToken)
  }

  /**
   * logout — 退出登录
   *
   * 清除所有用户状态和持久化数据，将应用恢复到未登录状态。
   * 通常在用户点击"退出登录"按钮时调用。
   *
   * 清除的内容：
   * - Pinia 状态：userInfo、accessToken、refreshToken 重置为空
   * - localStorage：删除 user_info、access_token、refresh_token 三个键
   *
   * 注意：此方法只清除前端数据，不会通知后端使 Token 失效。
   * 如果需要更高的安全性，可以在此方法中调用后端的 logout API。
   */
  function logout() {
    // 清除 Pinia 状态
    userInfo.value = {}
    accessToken.value = ''
    refreshToken.value = ''
    // 清除 localStorage 持久化数据
    localStorage.removeItem('user_info')
    localStorage.removeItem('access_token')
    localStorage.removeItem('refresh_token')
  }

  /**
   * updateUserInfo — 部分更新用户信息
   *
   * 在用户修改个人资料后调用，只更新变化的字段。
   * 使用对象展开运算符 { ...userInfo.value, ...data } 合并新旧数据。
   *
   * @param {Partial<LoginResponse>} data - 要更新的字段（只传需要修改的）
   *
   * 调用示例：
   * ```ts
   * // 只更新昵称
   * userStore.updateUserInfo({ nickname: '新昵称' })
   *
   * // 更新头像
   * userStore.updateUserInfo({ avatar: 'https://example.com/avatar.jpg' })
   * ```
   *
   * 注意：这里将 data 类型设为 Partial<LoginResponse> 而非 Partial<UserProfile>，
   * 因为 localStorage 中存储的是 LoginResponse 结构，保持一致。
   */
  function updateUserInfo(data: Partial<LoginResponse>) {
    // 使用展开运算符合并：旧数据被新数据中同名字段覆盖
    userInfo.value = { ...userInfo.value, ...data }
    // 同步更新 localStorage
    localStorage.setItem('user_info', JSON.stringify(userInfo.value))
  }

  // ==========================================================================
  // 【返回值】— 暴露给外部的状态和方法
  // ==========================================================================
  //
  // 只有在这里 return 的状态和方法才能被外部使用。
  // 如果某些状态是内部使用的（如临时变量），不需要 return。
  //
  return {
    // ---- 状态（State）----
    userInfo,        // 用户信息对象
    accessToken,     // 访问令牌
    refreshToken,    // 刷新令牌

    // ---- 计算属性（Getters）----
    isLoggedIn,      // 是否已登录
    userId,          // 用户 ID
    username,        // 用户名
    nickname,        // 昵称
    avatar,          // 头像

    // ---- 方法（Actions）----
    setLoginData,    // 保存登录数据
    logout,          // 退出登录
    updateUserInfo   // 更新用户信息
  }
})

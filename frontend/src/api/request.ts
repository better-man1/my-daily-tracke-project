/**
 * ============================================================================
 * 【Axios HTTP 请求封装模块】
 * ============================================================================
 *
 * 【模块用途】
 * 本文件是整个前端项目的 HTTP 请求基础设施层。所有 API 模块（如 auth.ts、
 * accounting.ts 等）都通过此文件导出的 request 实例来发送 HTTP 请求。
 *
 * 【设计思想】
 * 1. 单例模式：整个应用只创建一个 Axios 实例，统一管理请求配置（baseURL、
 *    timeout、headers 等），避免在每个 API 文件中重复配置。
 * 2. 拦截器机制：利用 Axios 的请求拦截器和响应拦截器，在请求发出前自动注入
 *    Token、管理全局 Loading 状态；在响应返回后统一处理业务错误码、网络错误、
 *    Token 过期自动刷新等逻辑。
 * 3. Token 无感刷新：当 Access Token 过期时（401），自动使用 Refresh Token 去
 *    获取新的 Access Token，并将刷新期间排队等待的请求在 Token 刷新成功后
 *    自动重发，用户完全无感知。
 * 4. 集中式错误处理：将业务错误、网络错误、认证错误分别封装为独立函数，
 *    通过 Element Plus 的 ElMessage 组件统一展示错误提示。
 *
 * 【学习要点】
 * - Axios 实例的创建与配置
 * - 请求拦截器（interceptors.request）的使用
 * - 响应拦截器（interceptors.response）的使用
 * - Token 刷新的并发控制（订阅/发布模式）
 * - Promise 异步流程控制
 * - TypeScript 类型在 Axios 中的应用
 *
 * ============================================================================
 */

// ---- 【导入依赖】 ----

// axios：HTTP 客户端库，支持浏览器和 Node.js
// AxiosInstance：Axios 实例的类型定义
// AxiosRequestConfig：请求配置的类型定义
// AxiosResponse：响应对象的类型定义
// AxiosError：错误对象的类型定义
import axios, { type AxiosInstance, type AxiosRequestConfig, type AxiosResponse, type AxiosError } from 'axios'

// ElMessage：Element Plus 的消息提示组件，用于在页面顶部显示操作反馈
import { ElMessage } from 'element-plus'

// router：Vue Router 实例，用于在认证失败时跳转到登录页
import router from '@/router'

// useAppStore：Pinia 全局应用状态 Store，用于管理 Loading 状态和请求计数
import { useAppStore } from '@/stores/app'

// ---- 【创建 Axios 实例】 ----

/**
 * 创建一个自定义配置的 Axios 实例。
 *
 * 为什么不直接使用 axios.get() / axios.post()？
 * - 直接使用全局 axios 无法设置统一的 baseURL、timeout 等配置
 * - 通过 axios.create() 创建的实例可以拥有独立的配置和拦截器
 * - 不同的 API 服务可以创建不同的实例（如本项目中只用了一个实例）
 *
 * @type {AxiosInstance}
 *
 * 配置说明：
 * - baseURL: '/api/v1' — 所有请求的 URL 前缀，实际请求时会拼接为 /api/v1/xxx
 *   开发环境下通过 vite.config.ts 中的 proxy 配置代理到后端服务器
 * - timeout: 15000 — 请求超时时间 15 秒（单位：毫秒），超时后请求会被中止
 * - headers: { 'Content-Type': 'application/json' } — 默认请求头，
 *   表示请求体以 JSON 格式发送
 */
const request: AxiosInstance = axios.create({
  baseURL: '/api/v1',          // API 基础路径，对应后端的接口版本号
  timeout: 15000,               // 请求超时时间：15秒
  headers: { 'Content-Type': 'application/json' } // 默认发送 JSON 格式数据
})

// ---- 【Token 刷新状态管理】 ----

/**
 * isRefreshing — 标记当前是否正在进行 Token 刷新
 *
 * 应用场景：当多个请求同时收到 401 响应时，只需要发起一次 Token 刷新请求，
 * 其他请求应该排队等待刷新完成后自动重试，而不是各自发起刷新请求。
 * - false：当前没有在刷新 Token
 * - true：当前正在刷新 Token，新来的 401 请求需要排队等待
 */
let isRefreshing = false

/**
 * refreshSubscribers — 刷新 Token 完成后的回调队列
 *
 * 存储所有在 Token 刷新期间被挂起的请求的回调函数。
 * 每个回调函数接收新的 accessToken 作为参数，用于更新请求头后重新发送请求。
 * 当 Token 刷新成功后，会依次调用队列中的所有回调，将挂起的请求重新发送。
 *
 * @type {Array<(token: string) => void>}
 */
let refreshSubscribers: ((token: string) => void)[] = []

/**
 * subscribeTokenRefresh — 将回调加入 Token 刷新等待队列
 *
 * 当 Token 正在刷新时（isRefreshing === true），新收到 401 错误的请求
 * 会调用此函数将自己的重试逻辑注册到等待队列中。
 *
 * @param callback - Token 刷新成功后要执行的回调函数
 *                   参数 token 是刷新后获取到的新 Access Token
 *
 * 使用示例（概念性代码，实际在拦截器中调用）：
 * ```ts
 * subscribeTokenRefresh((newToken) => {
 *   // 用新 Token 更新请求头并重新发送请求
 *   config.headers['Authorization'] = `Bearer ${newToken}`
 *   return request(config)
 * })
 * ```
 */
function subscribeTokenRefresh(callback: (token: string) => void) {
  refreshSubscribers.push(callback)
}

/**
 * onTokenRefreshed — Token 刷新成功后，通知所有等待中的请求
 *
 * 遍历等待队列中的所有回调函数，依次执行它们（即重新发送之前挂起的请求），
 * 然后清空等待队列，为下一次可能的 Token 刷新做准备。
 *
 * @param token - 刷新后获取到的新 Access Token
 */
function onTokenRefreshed(token: string) {
  refreshSubscribers.forEach(callback => callback(token))
  refreshSubscribers = [] // 清空队列，防止内存泄漏
}

// ============================================================================
// 【请求拦截器】
// ============================================================================
//
// 请求拦截器会在每个请求发出之前执行。这里用它做两件事：
// 1. 自动注入 JWT Token 到请求头（如果用户已登录）
// 2. 管理 Loading 状态（请求计数器递增，用于控制全局 Loading 遮罩层的显示）
//
// request.interceptors.request.use(成功回调, 错误回调)
// - 成功回调：接收 config 对象，可以修改请求配置，必须 return config
// - 错误回调：当请求配置出现异常时执行（较少触发）
// ============================================================================
request.interceptors.request.use(
  // ---- 成功回调：请求发出前的处理 ----
  (config) => {
    // 获取全局应用状态 Store 实例
    const appStore = useAppStore()

    // ---- 自动注入 Token ----
    // 从 localStorage 中读取保存的 Access Token
    // 注意：Token 是在用户登录成功后存储到 localStorage 的（见 stores/user.ts）
    const token = localStorage.getItem('access_token')
    if (token) {
      // 设置 Authorization 请求头，格式为 "Bearer <token>"
      // 这是 JWT (JSON Web Token) 的标准认证头格式
      // 后端会通过解析这个 Token 来识别用户身份
      config.headers['Authorization'] = `Bearer ${token}`
    }

    // ---- 全局 Loading 管理 ----
    // 递增请求计数器，用于控制页面上的 Loading 遮罩
    // config.showLoading 是自定义配置项（非 Axios 内置），
    // 默认为 true，可以在某些不需要 Loading 的请求中设为 false
    // 例如：后台轮询请求、静默刷新数据的请求等
    if (config.showLoading !== false) {
      appStore.incrementRequest()
    }

    // 必须返回修改后的 config，否则请求不会发出
    return config
  },
  // ---- 错误回调：请求配置出错时的处理 ----
  (error) => {
    // 请求计数器递减，保持 Loading 状态的准确性
    const appStore = useAppStore()
    appStore.decrementRequest()
    // 将错误继续向下传递，让调用方可以捕获处理
    return Promise.reject(error)
  }
)

// ============================================================================
// 【响应拦截器】
// ============================================================================
//
// 响应拦截器会在每个请求收到响应后执行。这是整个请求封装中最复杂的部分，
// 主要负责：
// 1. 成功响应（HTTP 200）：解包业务数据，处理业务错误码
// 2. 错误响应：处理 401 Token 过期（自动刷新）、网络错误等
//
// request.interceptors.response.use(成功回调, 错误回调)
// - 成功回调：HTTP 状态码 2xx 时执行
// - 错误回调：HTTP 状态码非 2xx 时执行
// ============================================================================
request.interceptors.response.use(
  // ---- 成功回调：HTTP 状态码 2xx ----
  (response: AxiosResponse) => {
    // 响应成功，请求计数器递减
    const appStore = useAppStore()
    appStore.decrementRequest()

    // 从 Axios 响应对象中提取实际的业务数据
    // 后端统一响应格式为：{ code: 200, message: "success", data: {...} }
    // response.data 是 Axios 解析后的响应体，即 { code, message, data }
    const res = response.data

    // 判断业务状态码是否为成功（200）
    if (res.code === 200) {
      // 业务成功，直接返回 data 字段的内容
      // 这样调用方拿到的就是纯粹的业务数据，无需再解包
      // 例如：const user = await userApi.getProfile() 直接得到用户对象
      return res.data
    }

    // ---- 业务错误处理 ----
    // code 不是 200，说明业务逻辑出错（如参数校验失败、数据不存在等）
    // 提取错误消息，默认为 "操作失败"
    const errorMsg = res.message || '操作失败'
    // 调用业务错误处理函数（可能会弹出错误提示）
    handleBusinessError(errorMsg, response.config)
    // 返回一个 rejected Promise，让调用方的 catch 能够捕获到错误
    return Promise.reject(new Error(errorMsg))
  },

  // ---- 错误回调：HTTP 状态码非 2xx ----
  async (error: AxiosError) => {
    // 即使是错误响应，也要递减请求计数器
    const appStore = useAppStore()
    appStore.decrementRequest()

    // 获取原始请求的配置，并添加自定义属性 _retry 用于防止重复刷新
    // AxiosRequestConfig & { _retry?: boolean } 是 TypeScript 的交叉类型，
    // 在原有配置类型基础上扩展了 _retry 属性
    const config = error.config as AxiosRequestConfig & { _retry?: boolean }

    // ---- 401 未授权错误处理（Token 过期） ----
    // 条件：
    // 1. 响应状态码是 401（未授权）
    // 2. 该请求没有重试过（!config?._retry），防止无限循环
    if (error.response?.status === 401 && !config?._retry) {
      // ---- 场景一：当前没有在刷新 Token，由本请求发起刷新 ----
      if (!isRefreshing) {
        isRefreshing = true   // 标记正在刷新，防止其他请求也发起刷新
        config!._retry = true // 标记本请求已重试，防止刷新后再次 401 导致死循环

        // 从 localStorage 获取 Refresh Token
        const refreshToken = localStorage.getItem('refresh_token')
        if (refreshToken) {
          try {
            // 使用原始 axios（不是封装的 request 实例）发起 Token 刷新请求
            // 避免触发 request 实例的拦截器导致死循环
            const res = await axios.post('/api/v1/auth/refresh', { refreshToken })
            // 从响应中提取新的 Access Token
            const { accessToken } = res.data.data
            // 将新 Token 保存到 localStorage，后续请求会自动使用
            localStorage.setItem('access_token', accessToken)

            // 通知所有在刷新期间排队等待的请求，让它们用新 Token 重试
            onTokenRefreshed(accessToken)

            // 更新当前请求的 Authorization 头，使用新 Token
            if (config?.headers) {
              config.headers['Authorization'] = `Bearer ${accessToken}`
            }
            // 用新 Token 重新发送原始请求
            return request(config!)
          } catch {
            // Token 刷新失败（Refresh Token 也过期了），清除用户信息并跳转登录页
            handleAuthError()
          } finally {
            // 无论刷新成功还是失败，都要重置刷新状态
            isRefreshing = false
          }
        } else {
          // 没有 Refresh Token，直接跳转登录页
          handleAuthError()
        }
      } else {
        // ---- 场景二：当前已有其他请求在刷新 Token，本请求排队等待 ----
        // 返回一个新的 Promise，当 Token 刷新成功后自动 resolve（重发请求）
        return new Promise((resolve) => {
          subscribeTokenRefresh((token: string) => {
            // Token 刷新成功，更新请求头中的 Token
            if (config?.headers) {
              config.headers['Authorization'] = `Bearer ${token}`
            }
            // 使用新 Token 重新发送本请求，并将结果 resolve 给调用方
            resolve(request(config!))
          })
        })
      }
    } else {
      // ---- 非 401 错误（400、403、404、500 等） ----
      handleNetworkError(error)
    }

    // 返回 rejected Promise，让调用方可以捕获错误
    return Promise.reject(error)
  }
)

// ============================================================================
// 【错误处理辅助函数】
// ============================================================================

/**
 * handleBusinessError — 处理业务逻辑错误
 *
 * 当后端返回的 HTTP 状态码为 200，但业务状态码不是 200 时调用。
 * 例如：请求参数校验失败、数据不存在、权限不足等业务层面的错误。
 *
 * @param message - 错误提示信息，来自后端的 res.message 字段
 * @param config  - 可选，原始请求的配置对象。如果 config.showError === false，
 *                  则不弹出错误提示（适用于需要自行处理错误的场景）
 *
 * 设计思路：
 * 提供 showError 配置项，让某些特殊的 API 调用可以自行决定如何展示错误。
 * 例如：登录失败时可能需要自定义错误提示样式，而不是使用全局的 ElMessage。
 */
function handleBusinessError(message: string, config?: AxiosRequestConfig) {
  // 如果请求配置中明确指定不显示错误提示，则直接返回
  if (config?.showError === false) return

  // 使用 Element Plus 的 Message 组件弹出错误提示
  ElMessage.error(message)
}

/**
 * handleNetworkError — 处理网络层错误
 *
 * 当 HTTP 请求本身失败（状态码非 2xx）时调用。根据不同的错误类型
 * 显示不同的错误提示信息，帮助用户理解问题原因。
 *
 * @param error - Axios 错误对象，包含请求和响应的详细信息
 *
 * 错误分类：
 * 1. error.response 存在：服务器返回了错误状态码（4xx、5xx）
 * 2. error.request 存在但 error.response 不存在：请求已发出但没收到响应
 *    （如网络断开、DNS 解析失败、请求超时等）
 */
function handleNetworkError(error: AxiosError) {
  // 同样检查是否配置了不显示错误提示
  if (error.config?.showError === false) return

  // 默认错误提示
  let message = '网络错误，请稍后重试'

  if (error.response) {
    // ---- 服务器返回了错误状态码 ----
    // error.response.status 是 HTTP 状态码
    const status = error.response.status
    switch (status) {
      case 400:
        message = '请求参数错误'           // Bad Request：请求参数格式不正确
        break
      case 401:
        message = '登录已过期，请重新登录'  // Unauthorized：Token 过期或无效
        break
      case 403:
        message = '没有权限执行此操作'      // Forbidden：无权限访问该资源
        break
      case 404:
        message = '请求的资源不存在'        // Not Found：API 路径错误或资源已删除
        break
      case 500:
        message = '服务器错误，请稍后重试'  // Internal Server Error：后端代码异常
        break
      case 502:
        message = '网关错误'               // Bad Gateway：反向代理无法连接后端
        break
      case 503:
        message = '服务暂时不可用'          // Service Unavailable：服务过载或维护中
        break
      case 504:
        message = '请求超时'               // Gateway Timeout：网关等待后端响应超时
        break
      default:
        // 其他未明确处理的错误码，尝试使用后端返回的错误消息
        message = error.response.data?.message || `请求失败 (${status})`
    }
  } else if (error.request) {
    // ---- 请求已发出但未收到响应 ----
    // error.code === 'ECONNABORTED' 表示请求被中止（通常是超时）
    if (error.code === 'ECONNABORTED') {
      message = '请求超时，请检查网络连接'
    } else {
      // 其他情况（如网络断开、CORS 错误等）
      message = '网络连接失败，请检查网络'
    }
  }

  // 弹出错误提示
  ElMessage.error(message)
}

/**
 * handleAuthError — 处理认证失败错误
 *
 * 当 Token 刷新失败（Refresh Token 也过期）或没有 Refresh Token 时调用。
 * 执行以下操作：
 * 1. 清除 localStorage 中所有用户数据（Token、用户信息等）
 * 2. 弹出提示消息告知用户需要重新登录
 * 3. 使用 Vue Router 跳转到登录页面
 */
function handleAuthError() {
  // 清除 localStorage 中的所有数据
  // 这里使用 clear() 而非 removeItem() 是为了确保清除所有可能的残留数据
  localStorage.clear()
  // 弹出警告级别的提示（黄色）
  ElMessage.warning('登录已过期，请重新登录')
  // 跳转到登录页面，让用户重新认证
  router.push('/login')
}

// ---- 【模块导出】 ----

// 导出 request 实例，供其他 API 模块使用
// 其他文件通过 import request from './request' 来使用
// 然后调用 request.get()、request.post() 等方法发送请求
export default request

// 导出 AxiosRequestConfig 类型，供其他文件在需要时引用
// 例如：需要自定义请求配置（如 showError、showLoading 等）时
export type { AxiosRequestConfig }

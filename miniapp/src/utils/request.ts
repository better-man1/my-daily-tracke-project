/**
 * ============================================================================
 * request.ts — 统一网络请求封装
 * ============================================================================
 *
 * 【文件说明】
 * 基于 uni.request() 封装的 HTTP 请求工具，提供以下能力：
 * - 自动注入 Token（Bearer Authentication）
 * - 401 自动刷新 Token（支持并发请求排队等待）
 * - 统一错误提示（中文化）
 * - 支持 GET / POST / PUT / DELETE 方法
 * - 支持多端（微信小程序 / H5 / App）
 *
 * 【与 H5 前端的差异 — uni.request vs axios】
 *
 * | 特性              | uni.request              | axios                   |
 * |-------------------|--------------------------|-------------------------|
 * | 基础 API          | uni.request()            | axios()                 |
 * | 拦截器            | 无（需手动封装）          | 有（interceptors）      |
 * | 请求取消          | RequestTask.abort()      | CancelToken             |
 * | Promise 支持      | 手动封装 Promise         | 原生 Promise            |
 * | 超时控制          | timeout 参数             | timeout 参数            |
 * | 上传文件          | uni.uploadFile()         | 单独配置                |
 * | 响应类型          | 固定 JSON/text           | 支持 blob/arraybuffer   |
 * | 跨域              | 不存在（小程序无跨域）    | 需要配置 CORS/代理      |
 *
 * 【小程序没有跨域问题】
 * 小程序不运行在浏览器中，不存在浏览器的同源策略（CORS）限制。
 * 但微信对请求域名有白名单限制，需要在 mp.weixin.qq.com 后台配置。
 *
 * 【Token 刷新机制说明】
 * 当多个请求同时发出且 Token 已过期时：
 * 1. 第一个 401 响应触发 Token 刷新
 * 2. 后续的 401 响应将对应的请求放入等待队列（refreshQueue）
 * 3. Token 刷新成功后，统一通知队列中所有请求使用新 Token 重试
 * 4. 这样避免了多个并发请求同时刷新 Token 的问题
 *
 * 【参考文档】
 * - uni.request 文档: https://uniapp.dcloud.net.cn/api/request/request.html
 * - 微信小程序网络 API: https://developers.weixin.qq.com/miniprogram/dev/api/network/request/wx.request.html
 * ============================================================================
 */
import { BASE_URL, REQUEST_TIMEOUT } from './config'

/**
 * Token 刷新锁 — 防止并发请求同时触发多次 Token 刷新
 * 当 isRefreshing = true 时，新的 401 请求会进入 refreshQueue 排队等待
 */
let isRefreshing = false

/** Token 刷新等待队列 — 存储等待新 Token 的回调函数 */
let refreshQueue: Array<(token: string) => void> = []

/**
 * 请求配置接口
 * 封装了 uni.request 需要的所有参数，提供更友好的 TypeScript 类型
 */
export interface RequestOptions {
  url: string
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
  data?: any
  params?: Record<string, any>
  header?: Record<string, string>
  showLoading?: boolean
  showError?: boolean
  _retry?: boolean
}

/** 后端统一响应格式 */
interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

/**
 * 将 params 对象拼接到 URL 的查询字符串中
 * uni.request 不像 axios 那样支持 params 参数，需要手动拼接
 * @param url - 基础 URL
 * @param params - 查询参数对象
 * @returns 拼接后的完整 URL
 */
function buildUrl(url: string, params?: Record<string, any>): string {
  if (!params) return url
  const query = Object.entries(params)
    .filter(([, v]) => v !== undefined && v !== null && v !== '')
    .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(String(v))}`)
    .join('&')
  return query ? `${url}?${query}` : url
}

/**
 * 核心请求函数 — 封装 uni.request 为 Promise
 *
 * @param options - 请求配置
 * @returns Promise<T> — 解析后的业务数据（body.data）
 *
 * 【uni.request 的回调模式 vs Promise】
 * uni.request 原生使用回调函数（success/fail/complete），
 * 这里通过 new Promise 将其转换为 Promise 风格，支持 async/await。
 */
function request<T = any>(options: RequestOptions): Promise<T> {
  const {
    url,
    method = 'GET',
    data,
    params,
    header = {},
    showLoading = false,   // 默认关闭 loading，避免并发请求频繁弹出
    showError = true,
    _retry = false
  } = options

  // 【uni.showLoading】— 显示加载提示框
  // mask: true 表示显示透明蒙层，防止触摸穿透（用户不能在 loading 时点击其他区域）
  if (showLoading) {
    uni.showLoading({ title: '加载中...', mask: true })
  }

  // 【uni.getStorageSync】— 同步读取本地缓存
  // 小程序的本地存储类似于浏览器的 localStorage，但有以下区别：
  // - 存储上限：小程序 10MB（H5 的 localStorage 约 5MB）
  // - 同步方法以 Sync 结尾，也有异步版本 uni.getStorage()
  // - 小程序中推荐使用同步方法（避免回调嵌套）
  const token = uni.getStorageSync('access_token')
  if (token) {
    header['Authorization'] = `Bearer ${token}`
  }
  if (!header['Content-Type']) {
    header['Content-Type'] = 'application/json'
  }

  // 拼接完整 URL
  const fullUrl = buildUrl(`${BASE_URL}${url}`, params)

  return new Promise<T>((resolve, reject) => {
    /**
     * 【uni.request】— Uni-app 的网络请求 API
     * 这是小程序中最核心的网络请求方法，类似于浏览器的 fetch() 或 axios。
     *
     * 参数说明：
     * - url: 请求地址（必须包含完整域名，或使用相对路径在 H5 中走代理）
     * - method: HTTP 方法（GET/POST/PUT/DELETE 等）
     * - data: 请求体数据（POST/PUT 时使用）
     * - header: 请求头（用于设置 Content-Type、Authorization 等）
     * - timeout: 超时时间（毫秒）
     * - success: 成功回调（HTTP 状态码 2xx 时触发）
     * - fail: 失败回调（网络错误、超时等触发）
     */
    uni.request({
      url: fullUrl,
      method,
      data,
      header,
      timeout: REQUEST_TIMEOUT,
      success: async (res) => {
        if (showLoading) uni.hideLoading()

        const statusCode = res.statusCode
        const body = res.data as ApiResponse<T>

        // 业务成功
        // 判断业务成功：HTTP 200 + 后端业务码 200
        // 【注意】uni.request 的 success 回调在任何有 HTTP 响应时都会触发，
        // 包括 401、403、404、500 等。需要在 success 中手动判断状态码。
        // 这与 axios 不同（axios 在非 2xx 时走 catch）。
        if (statusCode === 200 && body?.code === 200) {
          resolve(body.data)
          return
        }

        // 401 未授权 — 尝试使用 refresh_token 刷新 access_token
        // _retry 标志防止刷新请求本身再次触发刷新（无限循环）
        if (statusCode === 401 && !_retry) {
          try {
            const newToken = await handleTokenRefresh()
            const retryResult = await request<T>({
              ...options,
              showLoading: false,
              _retry: true,
              header: { ...header, Authorization: `Bearer ${newToken}` }
            })
            resolve(retryResult)
          } catch {
            handleAuthError()
            reject(new Error('登录已过期'))
          }
          return
        }

        const errMsg = body?.message || getStatusMessage(statusCode)
        // 【uni.showToast】— 显示消息提示框
        // icon: 'none' 表示不显示图标，用于纯文字提示
        // duration: 提示显示时长（毫秒）
        if (showError) {
          uni.showToast({ title: errMsg, icon: 'none', duration: 2500 })
        }
        reject(new Error(errMsg))
      },
      fail: (err) => {
        if (showLoading) uni.hideLoading()

        // 提供更友好的错误提示
        const errMsg = getNetworkError(err.errMsg || '')
        if (showError) {
          uni.showToast({ title: errMsg, icon: 'none', duration: 3000 })
        }
        console.error('[request] fail:', fullUrl, err)
        reject(new Error(err.errMsg || errMsg))
      }
    })
  })
}

/**
 * Token 刷新处理
 * 当 access_token 过期时，使用 refresh_token 获取新的 access_token
 * 支持并发请求排队：第一个请求执行刷新，后续请求等待刷新完成后复用新 Token
 */
function handleTokenRefresh(): Promise<string> {
  if (isRefreshing) {
    return new Promise((resolve) => {
      refreshQueue.push(resolve)
    })
  }

  isRefreshing = true
  const refreshToken = uni.getStorageSync('refresh_token')

  if (!refreshToken) {
    isRefreshing = false
    return Promise.reject(new Error('No refresh token'))
  }

  return new Promise((resolve, reject) => {
    uni.request({
      url: `${BASE_URL}/auth/refresh`,
      method: 'POST',
      header: { 'Content-Type': 'application/json' },
      data: { refreshToken },
      timeout: REQUEST_TIMEOUT,
      success: (res) => {
        const body = res.data as ApiResponse<{ accessToken: string }>
        if (res.statusCode === 200 && body?.code === 200) {
          const newToken = body.data.accessToken
          uni.setStorageSync('access_token', newToken)
          refreshQueue.forEach((cb) => cb(newToken))
          refreshQueue = []
          resolve(newToken)
        } else {
          reject(new Error('Token 刷新失败'))
        }
      },
      fail: () => reject(new Error('Token 刷新网络错误')),
      complete: () => { isRefreshing = false }
    })
  })
}

/**
 * 认证失败处理 — 清除本地缓存并跳转到登录页
 * 使用 uni.clearStorageSync 清除所有本地存储
 * 使用 uni.reLaunch 关闭所有页面并打开登录页（因为栈中的页面可能需要登录态）
 */
function handleAuthError() {
  uni.clearStorageSync()
  uni.showToast({ title: '请重新登录', icon: 'none' })
  setTimeout(() => {
    uni.reLaunch({ url: '/sub-pages/login/login' })
  }, 1500)
}

/** HTTP 状态码映射为中文提示 */
function getStatusMessage(code: number): string {
  const map: Record<number, string> = {
    400: '请求参数错误',
    401: '登录已过期',
    403: '无权限访问',
    404: '请求的资源不存在',
    500: '服务器内部错误',
    502: '网关错误',
    503: '服务暂时不可用',
    504: '请求超时'
  }
  return map[code] || `请求失败 (${code})`
}

/**
 * 网络错误信息映射为中文提示
 * 小程序中的网络错误信息是英文的（如 'request:fail timeout'），
 * 需要转换为用户友好的中文提示
 */
function getNetworkError(errMsg: string): string {
  const msg = errMsg.toLowerCase()
  if (msg.includes('timeout') || msg.includes('timed out')) {
    return '请求超时，请检查网络'
  }
  if (msg.includes('ssl') || msg.includes('https') || msg.includes('tls')) {
    return '证书错误，请检查网络安全设置'
  }
  if (msg.includes('url not in domain') || msg.includes('not in whitelist')) {
    return '域名未授权，请在开发工具中关闭域名校验'
  }
  if (msg.includes('connect') || msg.includes('network') || msg.includes('fail')) {
    return '连接服务器失败，请确认后端已启动'
  }
  return '网络异常，请稍后重试'
}

// ── 便捷方法（模仿 axios 的用法风格）──────────────────────────────
// 提供 get/post/put/delete 四个快捷方法，简化调用
export const http = {
  get: <T = any>(url: string, options?: Partial<RequestOptions>) =>
    request<T>({ url, method: 'GET', ...options }),

  post: <T = any>(url: string, data?: any, options?: Partial<RequestOptions>) =>
    request<T>({ url, method: 'POST', data, ...options }),

  put: <T = any>(url: string, data?: any, options?: Partial<RequestOptions>) =>
    request<T>({ url, method: 'PUT', data, ...options }),

  delete: <T = any>(url: string, options?: Partial<RequestOptions>) =>
    request<T>({ url, method: 'DELETE', ...options })
}

export default http

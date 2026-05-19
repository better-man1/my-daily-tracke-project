/**
 * 统一请求封装 — 使用 uni.request
 * ✅ 自动注入 Token
 * ✅ 401 自动刷新（支持并发排队）
 * ✅ 统一错误提示
 * ✅ 支持小程序 / H5 / App 多端
 */
import { BASE_URL, REQUEST_TIMEOUT } from './config'

// Token 刷新锁
let isRefreshing = false
let refreshQueue: Array<(token: string) => void> = []

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

interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

/** 将 params 拼接到 URL querystring */
function buildUrl(url: string, params?: Record<string, any>): string {
  if (!params) return url
  const query = Object.entries(params)
    .filter(([, v]) => v !== undefined && v !== null && v !== '')
    .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(String(v))}`)
    .join('&')
  return query ? `${url}?${query}` : url
}

/** 核心请求函数 */
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

  if (showLoading) {
    uni.showLoading({ title: '加载中...', mask: true })
  }

  // 注入 Token
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
        if (statusCode === 200 && body?.code === 200) {
          resolve(body.data)
          return
        }

        // 401 — 尝试刷新 Token
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

/** Token 刷新 */
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

/** 认证失败 */
function handleAuthError() {
  uni.clearStorageSync()
  uni.showToast({ title: '请重新登录', icon: 'none' })
  setTimeout(() => {
    uni.reLaunch({ url: '/sub-pages/login/login' })
  }, 1500)
}

/** HTTP 状态码 → 中文提示 */
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

/** 网络错误 → 中文提示（避免显示英文技术错误） */
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

// ── 便捷方法 ─────────────────────────────────────────────
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

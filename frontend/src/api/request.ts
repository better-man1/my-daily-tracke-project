import axios, { type AxiosInstance, type AxiosRequestConfig, type AxiosResponse, type AxiosError } from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { useAppStore } from '@/stores/app'

// 创建 axios 实例
const request: AxiosInstance = axios.create({
  baseURL: '/api/v1',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' }
})

// Token 刷新状态
let isRefreshing = false
let refreshSubscribers: ((token: string) => void)[] = []

// 订阅 Token 刷新完成
function subscribeTokenRefresh(callback: (token: string) => void) {
  refreshSubscribers.push(callback)
}

// 通知 Token 刷新完成
function onTokenRefreshed(token: string) {
  refreshSubscribers.forEach(callback => callback(token))
  refreshSubscribers = []
}

// 请求拦截器 — 自动注入 Token 和 Loading 管理
request.interceptors.request.use(
  (config) => {
    const appStore = useAppStore()

    // 自动注入 Token
    const token = localStorage.getItem('access_token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }

    // 全局 Loading 管理（可通过配置禁用）
    if (config.showLoading !== false) {
      appStore.incrementRequest()
    }

    return config
  },
  (error) => {
    const appStore = useAppStore()
    appStore.decrementRequest()
    return Promise.reject(error)
  }
)

// 响应拦截器 — 统一处理错误
request.interceptors.response.use(
  (response: AxiosResponse) => {
    const appStore = useAppStore()
    appStore.decrementRequest()

    const res = response.data
    if (res.code === 200) {
      return res.data
    }

    // 业务错误处理
    const errorMsg = res.message || '操作失败'
    handleBusinessError(errorMsg, response.config)
    return Promise.reject(new Error(errorMsg))
  },
  async (error: AxiosError) => {
    const appStore = useAppStore()
    appStore.decrementRequest()

    const config = error.config as AxiosRequestConfig & { _retry?: boolean }

    // 401 错误处理 - Token 刷新
    if (error.response?.status === 401 && !config?._retry) {
      if (!isRefreshing) {
        isRefreshing = true
        config!._retry = true

        const refreshToken = localStorage.getItem('refresh_token')
        if (refreshToken) {
          try {
            const res = await axios.post('/api/v1/auth/refresh', { refreshToken })
            const { accessToken } = res.data.data
            localStorage.setItem('access_token', accessToken)

            // 通知所有等待的请求
            onTokenRefreshed(accessToken)

            // 重试原请求
            if (config?.headers) {
              config.headers['Authorization'] = `Bearer ${accessToken}`
            }
            return request(config!)
          } catch {
            // 刷新失败，清除用户信息并跳转登录
            handleAuthError()
          } finally {
            isRefreshing = false
          }
        } else {
          handleAuthError()
        }
      } else {
        // 正在刷新中，将请求加入队列
        return new Promise((resolve) => {
          subscribeTokenRefresh((token: string) => {
            if (config?.headers) {
              config.headers['Authorization'] = `Bearer ${token}`
            }
            resolve(request(config!))
          })
        })
      }
    } else {
      // 其他错误处理
      handleNetworkError(error)
    }

    return Promise.reject(error)
  }
)

// 处理业务错误
function handleBusinessError(message: string, config?: AxiosRequestConfig) {
  // 如果配置中指定不显示错误提示，则跳过
  if (config?.showError === false) return

  ElMessage.error(message)
}

// 处理网络错误
function handleNetworkError(error: AxiosError) {
  if (error.config?.showError === false) return

  let message = '网络错误，请稍后重试'

  if (error.response) {
    // 服务器返回了错误状态码
    const status = error.response.status
    switch (status) {
      case 400:
        message = '请求参数错误'
        break
      case 401:
        message = '登录已过期，请重新登录'
        break
      case 403:
        message = '没有权限执行此操作'
        break
      case 404:
        message = '请求的资源不存在'
        break
      case 500:
        message = '服务器错误，请稍后重试'
        break
      case 502:
        message = '网关错误'
        break
      case 503:
        message = '服务暂时不可用'
        break
      case 504:
        message = '请求超时'
        break
      default:
        message = error.response.data?.message || `请求失败 (${status})`
    }
  } else if (error.request) {
    // 请求已发出但没有收到响应
    if (error.code === 'ECONNABORTED') {
      message = '请求超时，请检查网络连接'
    } else {
      message = '网络连接失败，请检查网络'
    }
  }

  ElMessage.error(message)
}

// 处理认证错误
function handleAuthError() {
  localStorage.clear()
  ElMessage.warning('登录已过期，请重新登录')
  router.push('/login')
}

export default request
export type { AxiosRequestConfig }

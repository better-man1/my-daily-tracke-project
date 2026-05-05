// ==============================
// DailyTracker 小程序 HTTP 请求封装
// 支持 H5（开发代理）和微信小程序（局域网 IP）双模式
// ==============================

// 运行时环境判断：H5 用相对路径走 vite 代理，小程序用局域网 IP 直连
const isH5 = process.env.UNI_PLATFORM === 'h5'
const BASE_URL = isH5 ? '/api/v1' : 'http://192.168.1.155:8080/api/v1'

interface RequestOptions {
  url: string
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
  data?: Record<string, any>
  params?: Record<string, any>
}

function getToken(): string {
  return uni.getStorageSync('access_token') || ''
}

function buildUrl(url: string, params?: Record<string, any>): string {
  if (!params) return url
  const query = Object.entries(params)
    .filter(([, v]) => v !== undefined && v !== null && v !== '')
    .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(v)}`)
    .join('&')
  return query ? `${url}?${query}` : url
}

function request<T = any>(options: RequestOptions): Promise<T> {
  return new Promise((resolve, reject) => {
    const token = getToken()
    const fullUrl = BASE_URL + buildUrl(options.url, options.params)

    uni.request({
      url: fullUrl,
      method: options.method || 'GET',
      data: options.data,
      header: {
        'Content-Type': 'application/json',
        ...(token ? { 'Authorization': `Bearer ${token}` } : {})
      },
      success: (res: any) => {
        const body = res.data as any
        if (res.statusCode === 401) {
          // Token 过期，跳转登录
          uni.removeStorageSync('access_token')
          uni.removeStorageSync('user_info')
          uni.reLaunch({ url: '/pages/login/index' })
          reject(new Error('未授权，请重新登录'))
          return
        }
        if (body?.code === 200) {
          resolve(body.data as T)
        } else {
          const msg = body?.message || '请求失败'
          uni.showToast({ title: msg, icon: 'none' })
          reject(new Error(msg))
        }
      },
      fail: (err) => {
        uni.showToast({ title: '网络错误，请检查连接', icon: 'none' })
        reject(err)
      }
    })
  })
}

export const http = {
  get: <T>(url: string, params?: Record<string, any>) =>
    request<T>({ url, method: 'GET', params }),

  post: <T>(url: string, data?: Record<string, any>) =>
    request<T>({ url, method: 'POST', data }),

  put: <T>(url: string, data?: Record<string, any>, params?: Record<string, any>) =>
    request<T>({ url, method: 'PUT', data, params }),

  delete: <T>(url: string) =>
    request<T>({ url, method: 'DELETE' })
}

export default http

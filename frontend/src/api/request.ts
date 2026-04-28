import axios from 'axios'
import type { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

// 创建 axios 实例
const request: AxiosInstance = axios.create({
  baseURL: '/api/v1',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' }
})

// 请求拦截器 — 自动注入 Token
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('access_token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器 — 统一处理错误
let isRefreshing = false

request.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data
    if (res.code === 200) {
      return res.data
    }
    // 业务错误
    ElMessage.error(res.message || '操作失败')
    return Promise.reject(new Error(res.message))
  },
  async (error) => {
    if (error.response?.status === 401 && !isRefreshing) {
      isRefreshing = true
      const refreshToken = localStorage.getItem('refresh_token')
      if (refreshToken) {
        try {
          const res = await axios.post('/api/v1/auth/refresh', { refreshToken })
          const { accessToken } = res.data.data
          localStorage.setItem('access_token', accessToken)
          // 重试原请求
          error.config.headers['Authorization'] = `Bearer ${accessToken}`
          return request(error.config)
        } catch {
          // 刷新失败，跳转登录
          localStorage.clear()
          router.push('/login')
        } finally {
          isRefreshing = false
        }
      } else {
        localStorage.clear()
        router.push('/login')
        isRefreshing = false
      }
    } else {
      const msg = error.response?.data?.message || error.message || '网络错误'
      ElMessage.error(msg)
    }
    return Promise.reject(error)
  }
)

export default request

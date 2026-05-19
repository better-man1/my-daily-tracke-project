/**
 * 认证相关 API
 */
import http from '@/utils/request'

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  password: string
  nickname?: string
}

export interface LoginResponse {
  userId: number
  username: string
  nickname: string
  avatar: string | null
  accessToken: string
  refreshToken: string
  accessTokenExpireIn: number
}

export const authApi = {
  /** 用户名密码登录 */
  login: (data: LoginRequest) =>
    http.post<LoginResponse>('/auth/login', data),

  /** 用户注册 */
  register: (data: RegisterRequest) =>
    http.post<void>('/auth/register', data),

  /** 刷新 Token */
  refresh: (refreshToken: string) =>
    http.post<LoginResponse>('/auth/refresh', { refreshToken })
}

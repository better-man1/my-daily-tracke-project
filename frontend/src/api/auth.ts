import request from './request'

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
  login: (data: LoginRequest) =>
    request.post<any, LoginResponse>('/auth/login', data),

  register: (data: RegisterRequest) =>
    request.post<any, void>('/auth/register', data),

  refresh: (refreshToken: string) =>
    request.post<any, LoginResponse>('/auth/refresh', { refreshToken }),

  logout: () => {
    localStorage.removeItem('access_token')
    localStorage.removeItem('refresh_token')
    localStorage.removeItem('user_info')
  }
}

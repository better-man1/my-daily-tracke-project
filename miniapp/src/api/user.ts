/**
 * 用户相关 API
 */
import http from '@/utils/request'

export interface UserProfile {
  id: number
  username: string
  nickname: string
  avatar: string | null
  email: string | null
  phone: string | null
  signature: string | null
  status: string
  lastLoginAt: string | null
  createdAt: string
}

export const userApi = {
  /** 获取用户信息 */
  getProfile: () =>
    http.get<UserProfile>('/users/profile'),

  /** 更新用户信息 */
  updateProfile: (data: Partial<UserProfile>) =>
    http.put<void>('/users/profile', data),

  /** 修改密码 */
  changePassword: (data: { oldPassword: string; newPassword: string }) =>
    http.put<void>('/users/password', data),

  /** 获取用户设置 */
  getSettings: () =>
    http.get<any>('/users/settings'),

  /** 更新设置 */
  updateSettings: (data: any) =>
    http.put<void>('/users/settings', data)
}

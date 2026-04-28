import request from './request'

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

export interface ChangePasswordRequest {
  oldPassword: string
  newPassword: string
}

export const userApi = {
  getProfile: () => request.get<any, UserProfile>('/users/profile'),

  updateProfile: (data: Partial<UserProfile>) => request.put<any, void>('/users/profile', data),

  changePassword: (data: ChangePasswordRequest) => request.put<any, void>('/users/password', data),

  getSettings: () => request.get<any, any>('/users/settings'),

  updateSettings: (data: any) => request.put<any, void>('/users/settings', data)
}

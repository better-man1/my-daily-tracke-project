import request from './request'

// ============================================================================
// 【TypeScript 接口定义】
// ============================================================================

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

export interface UserSettings {
  theme: 'light' | 'dark'
  language: string
  notifications: {
    email: boolean
    push: boolean
    reminder: boolean
  }
  privacy: {
    showProfile: boolean
    showStatistics: boolean
  }
}

export interface ChangePasswordRequest {
  oldPassword: string
  newPassword: string
}

export const userApi = {
  // 获取个人信息
  getProfile: () =>
    request.get<any, UserProfile>('/users/profile'),

  // 更新个人信息
  updateProfile: (data: Partial<UserProfile>) =>
    request.put<any, UserProfile>('/users/profile', data),

  // 修改密码
  changePassword: (data: ChangePasswordRequest) =>
    request.post<any, void>('/users/change-password', data),

  // 上传头像
  updateAvatar: (file: File) => {
    const formData = new FormData()
    formData.append('avatar', file)
    return request.post<any, { avatarUrl: string }>('/users/avatar', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // 获取用户设置
  getSettings: () =>
    request.get<any, UserSettings>('/users/settings'),

  // 更新用户设置
  updateSettings: (data: Partial<UserSettings>) =>
    request.put<any, UserSettings>('/users/settings', data),

  // 导出数据
  exportData: () =>
    request.get<any, any>('/users/export-data')
}
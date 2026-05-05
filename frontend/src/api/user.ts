import request from './request'

export interface UserProfile {
  id: number
  username: string
  nickname: string | null
  avatar: string | null
  email: string | null
  phone: string | null
  signature: string | null
  status: number
  lastLoginAt: string | null
  createdAt: string
}

export interface UserSettings {
  id?: number
  userId?: number
  theme: string
  defaultView: string
  reminderTime: string | null
  weekStartDay: number
  language: string
}

export interface UpdateProfileRequest {
  nickname?: string
  email?: string
  phone?: string
  signature?: string
}

export interface ChangePasswordRequest {
  oldPassword: string
  newPassword: string
}

export const userApi = {
  /** 获取个人信息 */
  getProfile: () =>
    request.get<any, UserProfile>('/users/profile'),

  /** 更新个人信息 */
  updateProfile: (data: UpdateProfileRequest) =>
    request.put<any, void>('/users/profile', data),

  /** 修改密码 */
  changePassword: (data: ChangePasswordRequest) =>
    request.put<any, void>('/users/password', data),

  /** 获取偏好设置 */
  getSettings: () =>
    request.get<any, UserSettings>('/users/settings'),

  /** 更新偏好设置 */
  updateSettings: (data: Partial<UserSettings>) =>
    request.put<any, void>('/users/settings', data),

  /** 导出用户数据（返回 blob） */
  exportData: () =>
    request.post<any, Blob>('/users/export', null, { responseType: 'blob' }),
}

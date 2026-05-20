/**
 * ============================================================================
 * 【用户信息 API 模块（User API）】
 * ============================================================================
 *
 * 【模块用途】
 * 封装用户个人信息管理相关的 HTTP 请求，包括获取/更新用户资料、
 * 修改密码、获取/更新用户设置、上传头像、导出数据等功能。
 *
 * 【设计思想】
 * 1. 个人信息管理：提供完整的用户资料 CRUD 操作，包括基本信息更新和密码修改。
 * 2. 文件上传支持：updateAvatar 方法使用 FormData 上传头像图片，
 *    通过 multipart/form-data 编码传输二进制文件数据。
 * 3. Partial<T> 工具类型：updateProfile 的参数使用 Partial<UserProfile>，
 *    表示所有字段都是可选的，用户可以只更新部分信息而不需要提交完整对象。
 *
 * 【学习要点】
 * - TypeScript Partial<T> 工具类型（将所有属性变为可选）
 * - FormData 的使用（文件上传）
 * - multipart/form-data 请求头的设置
 * - PUT 请求用于资源更新
 *
 * ============================================================================
 */

// 导入封装好的 Axios 请求实例
import request from './request'

// ============================================================================
// 【TypeScript 接口定义】
// ============================================================================

/**
 * UserProfile — 用户个人资料的数据结构
 *
 * 描述用户的完整个人信息，包括基本资料和账户状态。
 * 这个接口与后端的 User 实体对应。
 *
 * @interface UserProfile
 */
export interface UserProfile {
  id: number                 // 用户唯一标识 ID
  username: string           // 用户名（登录账号，通常不可修改）
  nickname: string           // 用户昵称（显示名称，可修改）
  avatar: string | null      // 头像 URL（null 表示使用默认头像）
  email: string | null       // 邮箱地址（null 表示未绑定邮箱）
  phone: string | null       // 手机号（null 表示未绑定手机）
  signature: string | null   // 个人签名/简介
  status: string             // 账户状态（如 "ACTIVE"、"DISABLED" 等）
  lastLoginAt: string | null // 最后登录时间
  createdAt: string          // 账户创建时间
}

/**
 * ChangePasswordRequest — 修改密码的请求数据结构
 *
 * @interface ChangePasswordRequest
 * @property {string} oldPassword - 当前密码（用于验证用户身份）
 * @property {string} newPassword - 新密码
 */
export interface ChangePasswordRequest {
  oldPassword: string   // 当前密码
  newPassword: string   // 新密码
}

// ============================================================================
// 【API 方法定义】
// ============================================================================

/**
 * userApi — 用户信息相关的 API 方法集合
 *
 * 提供用户资料的查看和修改、密码修改、设置管理、头像上传、数据导出等功能。
 */
export const userApi = {
  /**
   * getProfile — 获取当前登录用户的个人资料
   *
   * @returns {Promise<UserProfile>} 用户个人资料
   *
   * 请求方式：GET /api/v1/users/profile
   * 后端通过请求头中的 Token 识别当前用户，不需要传用户 ID
   */
  getProfile: () => request.get<any, UserProfile>('/users/profile'),

  /**
   * updateProfile — 更新用户个人资料
   *
   * 使用 Partial<UserProfile> 类型，允许只更新部分字段。
   * 例如：只更新昵称 { nickname: '新昵称' }，其他字段保持不变。
   *
   * Partial<T> 是 TypeScript 内置的工具类型，它将类型 T 的所有属性变为可选。
   * 等价于：
   * ```ts
   * interface PartialUserProfile {
   *   id?: number
   *   username?: string
   *   nickname?: string
   *   // ... 所有字段都变成可选
   * }
   * ```
   *
   * @param {Partial<UserProfile>} data - 要更新的字段（只传需要修改的字段）
   * @returns {Promise<void>} 更新成功无返回数据
   *
   * 请求方式：PUT /api/v1/users/profile
   */
  updateProfile: (data: Partial<UserProfile>) => request.put<any, void>('/users/profile', data),

  /**
   * changePassword — 修改密码
   *
   * 需要提供当前密码进行身份验证，防止未授权的密码修改。
   *
   * @param {ChangePasswordRequest} data - 包含旧密码和新密码的对象
   * @returns {Promise<void>} 修改成功无返回数据
   *
   * 请求方式：PUT /api/v1/users/password
   */
  changePassword: (data: ChangePasswordRequest) => request.put<any, void>('/users/password', data),

  /**
   * getSettings — 获取用户个性化设置
   *
   * 返回用户的应用偏好设置（如主题、通知开关、默认视图等）。
   *
   * @returns {Promise<any>} 用户设置数据
   *
   * 请求方式：GET /api/v1/users/settings
   */
  getSettings: () => request.get<any, any>('/users/settings'),

  /**
   * updateSettings — 更新用户个性化设置
   *
   * @param {any} data - 设置数据（键值对形式）
   * @returns {Promise<void>} 更新成功无返回数据
   *
   * 请求方式：PUT /api/v1/users/settings
   */
  updateSettings: (data: any) => request.put<any, void>('/users/settings', data),

  /**
   * updateAvatar — 上传/更新用户头像
   *
   * 使用 FormData 对象传输文件数据，这是浏览器端上传文件的标准方式。
   * 需要设置 Content-Type 为 'multipart/form-data'，
   * 浏览器会自动添加 boundary 分隔符。
   *
   * 为什么头像上传不用 JSON 格式？
   * - JSON 不适合传输二进制数据（图片文件）
   * - multipart/form-data 是 HTTP 协议专门为文件上传设计的编码格式
   * - FormData 会自动处理文件编码，无需手动转换
   *
   * @param {FormData} formData - 包含头像文件的 FormData 对象
   *   前端通常通过 <input type="file"> 获取文件，然后构建 FormData：
   *   ```ts
   *   const formData = new FormData()
   *   formData.append('file', fileInput.files[0])
   *   await userApi.updateAvatar(formData)
   *   ```
   * @returns {Promise<string>} 新头像的 URL 地址
   *
   * 请求方式：POST /api/v1/users/avatar
   */
  updateAvatar: (formData: FormData) =>
    request.post<any, string>('/users/avatar', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    }),

  /**
   * exportData — 导出用户所有数据
   *
   * 触发后端将用户的所有数据（计划、记账、总结、摘录等）打包导出。
   *
   * @returns {Promise<any>} 导出结果（可能包含下载链接或文件数据）
   *
   * 请求方式：POST /api/v1/users/export
   */
  exportData: () => request.post<any, any>('/users/export')
}

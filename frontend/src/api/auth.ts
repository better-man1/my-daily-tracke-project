/**
 * ============================================================================
 * 【认证 API 模块（Authentication API）】
 * ============================================================================
 *
 * 【模块用途】
 * 封装用户认证相关的 HTTP 请求，包括登录、注册、Token 刷新和退出登录。
 * 认证是应用的入口功能，所有其他 API 调用都依赖于认证成功后获取的 Token。
 *
 * 【设计思想】
 * 1. 接口与实现分离：先定义 TypeScript 接口描述请求和响应的数据结构，
 *    再实现具体的 API 调用函数，这样可以在编译时发现类型错误。
 * 2. 对象式 API 组织：将所有认证相关的 API 封装在 authApi 对象中，
 *    调用时通过 authApi.login()、authApi.register() 等方式使用，
 *    语义清晰、便于管理。
 * 3. JWT 双 Token 机制：后端返回 accessToken（短期有效，如 2 小时）和
 *    refreshToken（长期有效，如 7 天），accessToken 用于日常请求认证，
 *    refreshToken 用于在 accessToken 过期后获取新的 accessToken。
 *
 * 【学习要点】
 * - TypeScript interface 的定义和使用
 * - 泛型在 Axios 请求中的应用（request.post<any, LoginResponse>）
 * - JWT 认证的基本流程
 * - localStorage 的 Token 存储管理
 *
 * ============================================================================
 */

// 导入封装好的 Axios 请求实例
// request 实例已经配置了 baseURL、拦截器、错误处理等，这里直接使用即可
import request from './request'

// ============================================================================
// 【TypeScript 接口定义】
// ============================================================================

/**
 * LoginRequest — 登录请求的数据结构
 *
 * 定义了用户登录时需要提交的字段。
 * 用于约束调用方传入的参数类型，防止传入多余或缺失的字段。
 *
 * @interface LoginRequest
 * @property {string} username - 用户名，必填
 * @property {string} password - 密码，必填
 */
export interface LoginRequest {
  username: string   // 用户名
  password: string   // 密码（明文传输，实际应由 HTTPS 加密保护）
}

/**
 * RegisterRequest — 注册请求的数据结构
 *
 * 定义了用户注册时需要提交的字段。
 * 与 LoginRequest 相比多了可选的 nickname 字段。
 *
 * @interface RegisterRequest
 * @property {string}  username - 用户名，必填
 * @property {string}  password - 密码，必填
 * @property {string}  [nickname] - 昵称，可选（? 表示可选属性）
 *                                  如果不填，后端可能会使用用户名作为默认昵称
 */
export interface RegisterRequest {
  username: string       // 用户名
  password: string       // 密码
  nickname?: string      // 昵称（可选）
}

/**
 * LoginResponse — 登录成功后的响应数据结构
 *
 * 定义了后端在登录/注册/Token刷新成功后返回的用户和认证信息。
 * 这个接口同时被登录、注册、Token 刷新三个 API 共用。
 *
 * @interface LoginResponse
 * @property {number}      userId              - 用户唯一标识 ID
 * @property {string}      username            - 用户名
 * @property {string}      nickname            - 用户昵称（显示名称）
 * @property {string|null} avatar              - 用户头像 URL，可能为 null（未设置头像）
 * @property {string}      accessToken         - 访问令牌（短期有效，如 2 小时）
 *                                                用于后续 API 请求的身份认证
 * @property {string}      refreshToken        - 刷新令牌（长期有效，如 7 天）
 *                                                用于在 accessToken 过期后获取新 Token
 * @property {number}      accessTokenExpireIn - AccessToken 的过期时间（通常为秒级时间戳）
 */
export interface LoginResponse {
  userId: number                 // 用户 ID
  username: string               // 用户名
  nickname: string               // 昵称
  avatar: string | null          // 头像 URL（null 表示未设置）
  accessToken: string            // 访问令牌
  refreshToken: string           // 刷新令牌
  accessTokenExpireIn: number    // 访问令牌过期时间
}

// ============================================================================
// 【API 方法定义】
// ============================================================================

/**
 * authApi — 认证相关的 API 方法集合
 *
 * 将所有认证相关的 HTTP 请求封装为对象方法，便于统一管理和调用。
 *
 * 使用方式：
 * ```ts
 * import { authApi } from '@/api/auth'
 *
 * // 登录
 * const loginData = await authApi.login({ username: 'admin', password: '123456' })
 *
 * // 注册
 * await authApi.register({ username: 'newuser', password: '123456', nickname: '新用户' })
 *
 * // 刷新 Token
 * const newTokenData = await authApi.refresh('old-refresh-token')
 *
 * // 退出登录
 * authApi.logout()
 * ```
 */
export const authApi = {
  /**
   * login — 用户登录
   *
   * 向后端发送用户名和密码，验证成功后返回用户信息和 Token。
   * 登录成功后，Token 会自动存储到 localStorage（由 stores/user.ts 处理）。
   *
   * @param {LoginRequest} data - 登录表单数据（用户名 + 密码）
   * @returns {Promise<LoginResponse>} 登录成功后返回的用户信息和 Token
   *
   * 请求方式：POST /api/v1/auth/login
   * 请求体：{ username: string, password: string }
   * 响应数据：LoginResponse 对象
   *
   * 注意：request.post<any, LoginResponse> 中的泛型参数含义：
   * - 第一个泛型 any：请求体的类型（这里不约束）
   * - 第二个泛型 LoginResponse：响应数据的类型（经过拦截器解包后的 data 字段类型）
   */
  login: (data: LoginRequest) => request.post<any, LoginResponse>('/auth/login', data),

  /**
   * register — 用户注册
   *
   * 向后端提交注册信息，创建新用户账户。注册成功后返回 void（无数据）。
   * 通常注册成功后需要再调用 login 进行登录。
   *
   * @param {RegisterRequest} data - 注册表单数据（用户名 + 密码 + 可选昵称）
   * @returns {Promise<void>} 注册成功无返回数据
   *
   * 请求方式：POST /api/v1/auth/register
   */
  register: (data: RegisterRequest) => request.post<any, void>('/auth/register', data),

  /**
   * refresh — 刷新 Access Token
   *
   * 当 Access Token 过期时，使用 Refresh Token 获取新的 Access Token。
   * 注意：此方法通常不需要手动调用，request.ts 的响应拦截器会在
   * 收到 401 错误时自动调用此 API 进行 Token 刷新。
   *
   * @param {string} refreshToken - 刷新令牌
   * @returns {Promise<LoginResponse>} 新的 Token 信息
   *
   * 请求方式：POST /api/v1/auth/refresh
   */
  refresh: (refreshToken: string) =>
    request.post<any, LoginResponse>('/auth/refresh', { refreshToken }),

  /**
   * logout — 退出登录
   *
   * 清除本地存储的用户信息和 Token。
   * 注意：这是一个纯前端操作，不会向后端发送请求。
   * 如果后端有 Token 黑名单机制，这里应该增加一个 API 调用来通知后端。
   *
   * 清除的 localStorage 项：
   * - access_token：访问令牌
   * - refresh_token：刷新令牌
   * - user_info：用户基本信息（JSON 字符串）
   */
  logout: () => {
    localStorage.removeItem('access_token')
    localStorage.removeItem('refresh_token')
    localStorage.removeItem('user_info')
  }
}

/**
 * 通用类型定义
 */

// 通用响应结构
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

// 分页参数
export interface PaginationParams {
  page?: number
  pageSize?: number
}

// 分页响应
export interface PaginationResult<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
}
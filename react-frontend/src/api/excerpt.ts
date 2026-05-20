import request from './request'
import type { PageResult } from './accounting'

// ============================================================================
// 【TypeScript 接口定义】
// ============================================================================

export interface ExcerptItem {
  id: number
  userId: number
  content: string
  sourceType: string
  sourceTitle: string | null
  author: string | null
  page: string | null
  thought: string | null
  isFavorite: number
  tags: Array<{ id: number; name: string; color: string }>
  createdAt: string
  updatedAt: string
}

export interface CreateExcerptRequest {
  content: string
  sourceType?: string
  sourceTitle?: string
  author?: string
  page?: string
  thought?: string
  tags?: number[]
}

export const excerptApi = {
  // 分页查询
  list: (params?: {
    page?: number
    pageSize?: number
    keyword?: string
    tagId?: number
    isFavorite?: number
    sourceType?: string
  }) => request.get<any, PageResult<ExcerptItem>>('/excerpts', { params }),

  // 创建
  create: (data: CreateExcerptRequest) =>
    request.post<any, ExcerptItem>('/excerpts', data),

  // 更新
  update: (id: number, data: Partial<CreateExcerptRequest>) =>
    request.put<any, ExcerptItem>(`/excerpts/${id}`, data),

  // 删除
  delete: (id: number) =>
    request.delete<any, void>(`/excerpts/${id}`),

  // 收藏/取消收藏
  toggleFavorite: (id: number) =>
    request.put<any, ExcerptItem>(`/excerpts/${id}/favorite`),

  // 获取随机摘录
  getRandom: () =>
    request.get<any, ExcerptItem>('/excerpts/random'),

  // 导出为 Markdown
  exportMarkdown: (params?: {
    tagId?: number
    startDate?: string
    endDate?: string
  }) => request.get<any, string>('/excerpts/export/markdown', { params })
}
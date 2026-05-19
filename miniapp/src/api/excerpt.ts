/**
 * 感悟摘录相关 API
 */
import http from '@/utils/request'

export interface ExcerptItem {
  id: number
  content: string
  sourceType: string
  sourceTitle: string | null
  sourceUrl: string | null
  thought: string | null
  images: string | null
  isFavorite: number
  excerptDate: string
  tags: Array<{ id: number; name: string; color: string }>
  createdAt: string
}

export interface CreateExcerptRequest {
  content: string
  sourceType?: string
  sourceTitle?: string
  sourceUrl?: string
  thought?: string
  excerptDate: string
  tagIds?: number[]
  isFavorite?: number
}

export interface PageResult<T> {
  records: T[]
  total: number
  pageNum: number
  pageSize: number
  pages: number
}

export const excerptApi = {
  /** 分页查询摘录 */
  page: (params: {
    pageNum?: number
    pageSize?: number
    startDate?: string
    endDate?: string
    sourceType?: string
    isFavorite?: number
  }) => http.get<PageResult<ExcerptItem>>('/excerpts', { params }),

  /** 创建摘录 */
  create: (data: CreateExcerptRequest) =>
    http.post<ExcerptItem>('/excerpts', data),

  /** 更新摘录 */
  update: (id: number, data: CreateExcerptRequest) =>
    http.put<ExcerptItem>(`/excerpts/${id}`, data),

  /** 删除摘录 */
  delete: (id: number) =>
    http.delete<void>(`/excerpts/${id}`),

  /** 切换收藏 */
  toggleFavorite: (id: number) =>
    http.put<void>(`/excerpts/${id}/favorite`),

  /** 随机获取一条 */
  getRandom: () =>
    http.get<ExcerptItem>('/excerpts/random'),

  /** 获取所有标签 */
  getAllTags: () =>
    http.get<any[]>('/excerpts/tags')
}

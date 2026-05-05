import request from './request'
import type { PageResult } from './accounting'

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
  images?: string[]
  excerptDate: string
  tagIds?: number[]
  isFavorite?: number
}

export const excerptApi = {
  page: (params: {
    pageNum?: number
    pageSize?: number
    startDate?: string
    endDate?: string
    sourceType?: string
    tagId?: number
    isFavorite?: number
  }) => request.get<any, PageResult<ExcerptItem>>('/excerpts', { params }),

  create: (data: CreateExcerptRequest) => request.post<any, ExcerptItem>('/excerpts', data),

  update: (id: number, data: CreateExcerptRequest) =>
    request.put<any, ExcerptItem>(`/excerpts/${id}`, data),

  delete: (id: number) => request.delete<any, void>(`/excerpts/${id}`),

  toggleFavorite: (id: number) => request.put<any, void>(`/excerpts/${id}/favorite`),

  getRandom: () => request.get<any, ExcerptItem>('/excerpts/random'),

  getAllTags: () => request.get<any, any[]>('/excerpts/tags'),
  
  createTag: (name: string, color?: string) => 
    request.post<any, any>('/excerpts/tags', null, { params: { name, color } }),

  search: (keyword: string, pageNum = 1, pageSize = 20) =>
    request.get<any, PageResult<ExcerptItem>>('/excerpts/search', {
      params: { keyword, pageNum, pageSize }
    }),

  exportMarkdown: () => request.get<any, string>('/excerpts/export-markdown')
}

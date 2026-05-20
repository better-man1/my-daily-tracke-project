import request from './request'

// ============================================================================
// 【TypeScript 接口定义】
// ============================================================================

export interface SummaryItem {
  id: number
  userId: number
  summaryDate: string
  mood: number
  score: number
  achievement: string | null
  improvement: string | null
  tomorrowPlan: string | null
  gratitude: string | null
  healthNote: string | null
  createdAt: string
  updatedAt: string
}

export interface CreateSummaryRequest {
  summaryDate: string
  mood: number
  score: number
  achievement?: string
  improvement?: string
  tomorrowPlan?: string
  gratitude?: string
  healthNote?: string
}

export const summaryApi = {
  // 获取列表
  list: (params?: {
    startDate?: string
    endDate?: string
  }) => request.get<any, SummaryItem[]>('/summaries', { params }),

  // 获取单条
  get: (summaryDate: string) =>
    request.get<any, SummaryItem>(`/summaries/date/${summaryDate}`),

  // 创建
  create: (data: CreateSummaryRequest) =>
    request.post<any, SummaryItem>('/summaries', data),

  // 更新
  update: (id: number, data: Partial<CreateSummaryRequest>) =>
    request.put<any, SummaryItem>(`/summaries/${id}`, data),

  // 删除
  delete: (id: number) =>
    request.delete<any, void>(`/summaries/${id}`),

  // 获取连续打卡天数
  getStreak: () =>
    request.get<any, { currentStreak: number; longestStreak: number }>('/summaries/streak'),

  // 获取心情趋势
  getMoodTrend: (days: number = 30) =>
    request.get<any, Array<{ date: string; mood: number; score: number }>>('/summaries/mood-trend', {
      params: { days }
    })
}
/**
 * 每日总结相关 API
 */
import http from '@/utils/request'

export interface SummaryItem {
  id: number
  summaryDate: string
  mood: number
  score: number
  achievement: string | null
  improvement: string | null
  tomorrowPlan: string | null
  gratitude: string | null
  healthNote: string | null
  freeWriting: string | null
  tags: string | null
  createdAt: string
}

export interface CreateSummaryRequest {
  summaryDate: string
  mood?: number
  score?: number
  achievement?: string
  improvement?: string
  tomorrowPlan?: string
  gratitude?: string[]
  healthNote?: string
  freeWriting?: string
}

export const summaryApi = {
  /** 获取总结列表 */
  list: (params: { pageNum?: number; pageSize?: number; startDate?: string; endDate?: string }) =>
    http.get<SummaryItem[]>('/summaries', { params }),

  /** 创建总结 */
  create: (data: CreateSummaryRequest) =>
    http.post<SummaryItem>('/summaries', data),

  /** 更新总结 */
  update: (id: number, data: CreateSummaryRequest) =>
    http.put<SummaryItem>(`/summaries/${id}`, data),

  /** 获取今日总结 */
  getToday: () =>
    http.get<SummaryItem | null>('/summaries/today'),

  /** 获取连续记录 */
  getStreak: () =>
    http.get<{ currentStreak: number; longestStreak: number; totalDays: number }>('/summaries/streak'),

  /** 获取心情趋势 */
  getMoodTrend: (days = 30) =>
    http.get<Array<{ date: string; mood: number; score: number }>>('/summaries/mood-trend', { params: { days } })
}

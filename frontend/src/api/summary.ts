import request from './request'

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
  tags?: string[]
}

export const summaryApi = {
  list: (params: { pageNum?: number; pageSize?: number; startDate?: string; endDate?: string }) =>
    request.get<any, SummaryItem[]>('/summaries', { params }),

  create: (data: CreateSummaryRequest) => request.post<any, SummaryItem>('/summaries', data),

  update: (id: number, data: CreateSummaryRequest) =>
    request.put<any, SummaryItem>(`/summaries/${id}`, data),

  delete: (id: number) => request.delete<any, void>(`/summaries/${id}`),

  getToday: () => request.get<any, SummaryItem | null>('/summaries/today'),

  getStreak: () =>
    request.get<any, { currentStreak: number; longestStreak: number; totalDays: number }>(
      '/summaries/streak'
    ),

  getMoodTrend: (days = 30) =>
    request.get<any, Array<{ date: string; mood: number; score: number }>>(
      '/summaries/mood-trend',
      {
        params: { days }
      }
    )
}

import http from './request'

export const authApi = {
  login: (data: { username: string; password: string }) =>
    http.post<any>('/auth/login', data),

  register: (data: { username: string; password: string; nickname?: string }) =>
    http.post<void>('/auth/register', data)
}

export const dashboardApi = {
  getToday: () => http.get<any>('/dashboard/today'),
  getWeek: () => http.get<any>('/dashboard/week'),
  getMonth: () => http.get<any>('/dashboard/month')
}

export const planApi = {
  list: (planDate?: string) =>
    http.get<any[]>('/daily-plans', planDate ? { planDate } : undefined),

  create: (data: any) => http.post<any>('/daily-plans', data),

  updateStatus: (id: number, status: string) =>
    http.put<void>(`/daily-plans/${id}/status`, undefined, { status }),

  delete: (id: number) => http.delete<void>(`/daily-plans/${id}`),

  statistics: (planDate?: string) =>
    http.get<any>('/daily-plans/statistics', planDate ? { planDate } : undefined),

  postpone: (id: number) => http.post<any>(`/daily-plans/${id}/postpone`)
}

export const accountingApi = {
  page: (params: any) => http.get<any>('/accounting', params),

  create: (data: any) => http.post<any>('/accounting', data),

  delete: (id: number) => http.delete<void>(`/accounting/${id}`),

  monthlyStats: (year: number, month: number) =>
    http.get<any>('/accounting/statistics/monthly', { year, month }),

  getCategories: (type?: string) =>
    http.get<any[]>('/accounting/categories', type ? { type } : undefined)
}

export const excerptApi = {
  page: (params: any) => http.get<any>('/excerpts', params),

  create: (data: any) => http.post<any>('/excerpts', data),

  toggleFavorite: (id: number) => http.put<void>(`/excerpts/${id}/favorite`),

  getRandom: () => http.get<any>('/excerpts/random')
}

export const summaryApi = {
  list: (params: any) => http.get<any[]>('/summaries', params),

  create: (data: any) => http.post<any>('/summaries', data),

  update: (id: number, data: any) => http.put<any>(`/summaries/${id}`, data),

  getToday: () => http.get<any>('/summaries/today'),

  getStreak: () => http.get<any>('/summaries/streak'),

  getMoodTrend: (days = 30) =>
    http.get<any[]>('/summaries/mood-trend', { days })
}

export const goalApi = {
  list: (params?: any) => http.get<any[]>('/goals', params),

  create: (data: any) => http.post<any>('/goals', data),

  updateProgress: (id: number, progress: number) =>
    http.put<void>(`/goals/${id}/progress`, undefined, { progress }),

  delete: (id: number) => http.delete<void>(`/goals/${id}`)
}

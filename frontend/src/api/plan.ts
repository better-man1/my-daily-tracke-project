import request from './request'

export interface PlanItem {
  id: number
  userId: number
  goalId: number | null
  title: string
  description: string | null
  planDate: string
  priority: 'P0' | 'P1' | 'P2' | 'P3'
  category: 'WORK' | 'STUDY' | 'LIFE' | 'HEALTH'
  estimatedMins: number | null
  actualMins: number | null
  status: 'TODO' | 'IN_PROGRESS' | 'DONE' | 'CANCELLED'
  sortOrder: number
  completedAt: string | null
  isTemplate: number
  templateName: string | null
  createdAt: string
  updatedAt: string
}

export interface CreatePlanRequest {
  title: string
  description?: string
  planDate: string
  priority?: string
  category?: string
  estimatedMins?: number
  goalId?: number
  sortOrder?: number
}

export interface PlanStatistics {
  date: string
  total: number
  done: number
  inProgress: number
  todo: number
  cancelled: number
  completionRate: number
  totalEstimatedMins: number
  totalActualMins: number
}

export const planApi = {
  list: (planDate?: string) =>
    request.get<any, PlanItem[]>('/daily-plans', { params: { planDate } }),

  create: (data: CreatePlanRequest) => request.post<any, PlanItem>('/daily-plans', data),

  update: (id: number, data: CreatePlanRequest) =>
    request.put<any, PlanItem>(`/daily-plans/${id}`, data),

  delete: (id: number) => request.delete<any, void>(`/daily-plans/${id}`),

  updateStatus: (id: number, status: string) =>
    request.put<any, void>(`/daily-plans/${id}/status`, null, { params: { status } }),

  batchSort: (sortMap: Record<number, number>) =>
    request.put<any, void>('/daily-plans/batch-sort', sortMap),

  postpone: (id: number) => request.post<any, PlanItem>(`/daily-plans/${id}/postpone`),

  statistics: (planDate?: string) =>
    request.get<any, PlanStatistics>('/daily-plans/statistics', { params: { planDate } }),

  listTemplates: () => request.get<any, PlanItem[]>('/daily-plans/templates'),

  saveAsTemplate: (id: number, templateName: string) =>
    request.post<any, void>(`/daily-plans/${id}/templates`, null, { params: { templateName } })
}

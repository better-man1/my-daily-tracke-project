/**
 * 计划管理相关 API
 */
import http from '@/utils/request'

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
  repeatType?: string
  parentId?: number | null
  subtaskCount?: number
  completedSubtaskCount?: number
  children?: PlanItem[]
  startTime?: string | null
  endTime?: string | null
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
  startTime?: string
  endTime?: string
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
  /** 获取某日计划列表 */
  list: (planDate?: string) =>
    http.get<PlanItem[]>('/daily-plans', { params: { planDate } }),

  /** 创建计划 */
  create: (data: CreatePlanRequest) =>
    http.post<PlanItem>('/daily-plans', data),

  /** 更新计划 */
  update: (id: number, data: CreatePlanRequest) =>
    http.put<PlanItem>(`/daily-plans/${id}`, data),

  /** 删除计划 */
  delete: (id: number) =>
    http.delete<void>(`/daily-plans/${id}`),

  /** 更新计划状态 */
  updateStatus: (id: number, status: string) =>
    http.put<void>(`/daily-plans/${id}/status`, null, { params: { status } }),

  /** 延期计划 */
  postpone: (id: number) =>
    http.post<PlanItem>(`/daily-plans/${id}/postpone`),

  /** 获取统计数据 */
  statistics: (planDate?: string) =>
    http.get<PlanStatistics>('/daily-plans/statistics', { params: { planDate } })
}

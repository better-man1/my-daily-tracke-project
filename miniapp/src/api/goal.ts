/**
 * 目标管理相关 API
 */
import http from '@/utils/request'

export interface GoalItem {
  id: number
  parentId: number | null
  title: string
  description: string | null
  goalType: 'FIVE_YEAR' | 'YEARLY' | 'MONTHLY' | 'WEEKLY'
  category: string
  startDate: string
  endDate: string
  progress: number
  status: 'NOT_STARTED' | 'IN_PROGRESS' | 'COMPLETED' | 'ABANDONED'
  priority: number
  sortOrder: number
  children?: GoalItem[]
  keyResults?: Array<{
    id?: number
    title: string
    targetValue: number
    currentValue: number
    unit: string
    progress?: number
  }>
  createdAt: string
}

export interface CreateGoalRequest {
  parentId?: number | null
  title: string
  description?: string
  goalType: string
  category?: string
  startDate: string
  endDate: string
  progress?: number
  status?: string
  priority?: number
}

export const goalApi = {
  /** 获取目标列表 */
  list: (params: { goalType?: string; category?: string; status?: string }) =>
    http.get<GoalItem[]>('/goals', { params }),

  /** 创建目标 */
  create: (data: CreateGoalRequest) =>
    http.post<GoalItem>('/goals', data),

  /** 更新目标 */
  update: (id: number, data: CreateGoalRequest) =>
    http.put<GoalItem>(`/goals/${id}`, data),

  /** 删除目标 */
  delete: (id: number) =>
    http.delete<void>(`/goals/${id}`),

  /** 更新进度 */
  updateProgress: (id: number, progress: number) =>
    http.put<void>(`/goals/${id}/progress`, null, { params: { progress } }),

  /** 获取目标树 */
  getTree: (goalType?: string) =>
    http.get<GoalItem[]>('/goals/tree', { params: { goalType } }),

  /** 获取统计 */
  getStatistics: () =>
    http.get<Record<string, any>>('/goals/statistics')
}

import request from './request'

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
  createdAt: string
}

export interface CreateGoalRequest {
  parentId?: number
  title: string
  description?: string
  goalType: string
  category?: string
  startDate: string
  endDate: string
  progress?: number
  status?: string
  priority?: number
  sortOrder?: number
}

export const goalApi = {
  list: (params: { goalType?: string; category?: string; status?: string }) =>
    request.get<any, GoalItem[]>('/goals', { params }),

  create: (data: CreateGoalRequest) =>
    request.post<any, GoalItem>('/goals', data),

  update: (id: number, data: CreateGoalRequest) =>
    request.put<any, GoalItem>(`/goals/${id}`, data),

  delete: (id: number) =>
    request.delete<any, void>(`/goals/${id}`),

  updateProgress: (id: number, progress: number) =>
    request.put<any, void>(`/goals/${id}/progress`, null, { params: { progress } }),

  getTree: (goalType?: string) =>
    request.get<any, GoalItem[]>('/goals/tree', { params: { goalType } }),

  getStatistics: () =>
    request.get<any, Record<string, any>>('/goals/statistics')
}

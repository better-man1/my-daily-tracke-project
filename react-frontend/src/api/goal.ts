import request from './request'

// ============================================================================
// 【TypeScript 接口定义】
// ============================================================================

export interface GoalKr {
  id?: number
  title: string
  targetValue: number
  currentValue: number
  unit: string
  completedAt: string | null
}

export interface GoalItem {
  id: number
  userId: number
  parentId: number | null
  title: string
  description: string | null
  goalType: 'FIVE_YEAR' | 'YEARLY' | 'MONTHLY' | 'WEEKLY'
  status: 'NOT_STARTED' | 'IN_PROGRESS' | 'COMPLETED' | 'ABANDONED'
  startDate: string
  targetDate: string
  actualEndDate: string | null
  progress: number
  keyResults: GoalKr[]
  children?: GoalItem[]
  completedKrCount: number
  totalKrCount: number
  createdAt: string
  updatedAt: string
}

export interface CreateGoalRequest {
  title: string
  description?: string
  goalType: 'FIVE_YEAR' | 'YEARLY' | 'MONTHLY' | 'WEEKLY'
  startDate: string
  targetDate: string
  parentId?: number
  keyResults?: GoalKr[]
}

export interface GoalStatistics {
  totalGoals: number
  completedGoals: number
  inProgressGoals: number
  notStartedGoals: number
  abandonedGoals: number
  overallProgress: number
  byType: Record<string, { total: number; completed: number }>
}

export const goalApi = {
  // 获取树形结构
  getTree: (goalType?: string) =>
    request.get<any, GoalItem[]>('/goals/tree', { params: { goalType } }),

  // 获取列表
  list: (params?: {
    goalType?: string
    status?: string
    parentId?: number
  }) => request.get<any, GoalItem[]>('/goals', { params }),

  // 创建目标
  create: (data: CreateGoalRequest) =>
    request.post<any, GoalItem>('/goals', data),

  // 更新目标
  update: (id: number, data: Partial<CreateGoalRequest>) =>
    request.put<any, GoalItem>(`/goals/${id}`, data),

  // 删除目标
  delete: (id: number) =>
    request.delete<any, void>(`/goals/${id}`),

  // 更新状态
  updateStatus: (id: number, status: string) =>
    request.patch<any, GoalItem>(`/goals/${id}/status`, { status }),

  // 更新进度
  updateProgress: (id: number, progress: number) =>
    request.patch<any, GoalItem>(`/goals/${id}/progress`, { progress }),

  // ---- 关键结果管理 ----

  createKr: (goalId: number, data: GoalKr) =>
    request.post<any, GoalKr>(`/goals/${goalId}/krs`, data),

  updateKr: (goalId: number, krId: number, data: Partial<GoalKr>) =>
    request.put<any, GoalKr>(`/goals/${goalId}/krs/${krId}`, data),

  deleteKr: (goalId: number, krId: number) =>
    request.delete<any, void>(`/goals/${goalId}/krs/${krId}`),

  updateKrProgress: (goalId: number, krId: number, currentValue: number) =>
    request.patch<any, GoalKr>(`/goals/${goalId}/krs/${krId}/progress`, { currentValue }),

  // ---- 统计数据 ----

  getStatistics: () =>
    request.get<any, GoalStatistics>('/goals/statistics')
}
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
  repeatType?: 'NONE' | 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'CUSTOM'
  repeatPattern?: string
  repeatEndDate?: string
  parentId?: number | null
  subtaskCount?: number
  completedSubtaskCount?: number
  children?: PlanItem[]
  startTime?: string | null
  endTime?: string | null
  isTimeblock?: number
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

export interface RepeatUpdateRequest {
  repeatType?: 'NONE' | 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'CUSTOM'
  repeatPattern?: string
  repeatEndDate?: string
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
    request.post<any, void>(`/daily-plans/${id}/templates`, null, { params: { templateName } }),

  // 重复任务相关
  generateRepeatInstances: (id: number, startDate: string, endDate: string) =>
    request.post<any, PlanItem[]>(`/daily-plans/${id}/repeat/instances`, null, {
      params: { startDate, endDate }
    }),

  updateRepeatRule: (id: number, data: RepeatUpdateRequest) =>
    request.put<any, PlanItem>(`/daily-plans/${id}/repeat`, data),

  stopRepeat: (id: number) => request.delete<any, void>(`/daily-plans/${id}/repeat`),

  // 子任务相关
  createSubtask: (parentId: number, data: CreatePlanRequest) =>
    request.post<any, PlanItem>(`/daily-plans/${parentId}/subtasks`, data),

  getSubtasks: (parentId: number) =>
    request.get<any, PlanItem[]>(`/daily-plans/${parentId}/subtasks`),

  updateSubtaskStatus: (id: number, status: string) =>
    request.put<any, void>(`/daily-plans/subtasks/${id}/status`, null, { params: { status } }),

  convertToMainTask: (id: number) =>
    request.post<any, void>(`/daily-plans/subtasks/${id}/convert`),

  // 数据分析相关
  getCompletionTrend: (startDate: string, endDate: string) =>
    request.get<any, any[]>('/daily-plans/analytics/trend', { params: { startDate, endDate } }),

  getCategoryDistribution: (startDate: string, endDate: string) =>
    request.get<any, any>('/daily-plans/analytics/category', { params: { startDate, endDate } }),

  getPriorityDistribution: (startDate: string, endDate: string) =>
    request.get<any, any>('/daily-plans/analytics/priority', { params: { startDate, endDate } }),

  getTimeDistribution: (startDate: string, endDate: string) =>
    request.get<any, any>('/daily-plans/analytics/time', { params: { startDate, endDate } }),

  // 时间块相关
  detectTimeConflicts: (planDate: string) =>
    request.get<any, PlanItem[]>('/daily-plans/timeblock/conflicts', { params: { planDate } }),

  getTimeBlocks: (planDate: string) =>
    request.get<any, PlanItem[]>('/daily-plans/timeblock', { params: { planDate } })
}

// 提醒相关 API
export interface ReminderSetRequest {
  reminderType: 'START' | 'DUE' | 'CUSTOM'
  reminderTime?: string
  advanceMinutes?: number
}

export interface ReminderInfo {
  id: number
  reminderType: string
  reminderTime: string
  isSent: boolean
}

export const reminderApi = {
  setReminder: (planId: number, data: ReminderSetRequest) =>
    request.post<any, void>(`/plan-reminders/${planId}`, data),

  deleteReminder: (planId: number) =>
    request.delete<any, void>(`/plan-reminders/${planId}`),

  getReminders: (planId: number) =>
    request.get<any, ReminderInfo[]>(`/plan-reminders/${planId}`)
}

// 批量操作相关
export interface BatchUpdateRequest {
  ids: number[]
  priority?: string
  category?: string
  status?: string
}

export const batchApi = {
  batchUpdate: (data: BatchUpdateRequest) =>
    request.put<any, void>('/daily-plans/batch-update', data),

  batchDelete: (ids: number[]) =>
    request.delete<any, void>('/daily-plans/batch-delete', { data: ids }),

  batchPostpone: (ids: number[]) =>
    request.put<any, void>('/daily-plans/batch-postpone', ids),

  batchComplete: (ids: number[]) =>
    request.put<any, void>('/daily-plans/batch-complete', ids)
}

// 标签相关
export interface TagItem {
  id: number
  userId: number
  name: string
  color: string
  createdAt: string
  updatedAt: string
}

export interface TagCreateRequest {
  name: string
  color?: string
}

export const tagApi = {
  list: () => request.get<any, TagItem[]>('/plan-tags'),

  create: (data: TagCreateRequest) => request.post<any, TagItem>('/plan-tags', data),

  update: (id: number, data: TagCreateRequest) =>
    request.put<any, TagItem>(`/plan-tags/${id}`, data),

  delete: (id: number) => request.delete<any, void>(`/plan-tags/${id}`),

  addTagsToPlan: (planId: number, tagIds: number[]) =>
    request.post<any, void>(`/plan-tags/plans/${planId}`, tagIds),

  removeTagsFromPlan: (planId: number, tagIds: number[]) =>
    request.delete<any, void>(`/plan-tags/plans/${planId}`, { data: tagIds }),

  getPlanTags: (planId: number) =>
    request.get<any, TagItem[]>(`/plan-tags/plans/${planId}`)
}

import request from './request'

// ============================================================================
// 【TypeScript 接口定义 — 每日计划】
// ============================================================================

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
  startTime?: string
  endTime?: string
  isTimeblock?: number
  repeatType?: string
  repeatPattern?: string
  repeatEndDate?: string
  parentId?: number
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

// ============================================================================
// 【planApi — 每日计划相关的 API 方法集合】
// ============================================================================
export const planApi = {
  // ---- 基础 CRUD 操作 ----

  list: (planDate?: string) =>
    request.get<any, PlanItem[]>('/daily-plans', { params: { planDate } }),

  create: (data: CreatePlanRequest) =>
    request.post<any, PlanItem>('/daily-plans', data),

  update: (id: number, data: Partial<CreatePlanRequest>) =>
    request.put<any, PlanItem>(`/daily-plans/${id}`, data),

  delete: (id: number) =>
    request.delete<any, void>(`/daily-plans/${id}`),

  // ---- 状态更新 ----

  updateStatus: (id: number, status: string) =>
    request.patch<any, PlanItem>(`/daily-plans/${id}/status`, { status }),

  // ---- 排序操作 ----

  reorder: (items: Array<{ id: number; sortOrder: number }>) =>
    request.post<any, void>('/daily-plans/reorder', { items }),

  // ---- 时间记录 ----

  recordActualTime: (id: number, actualMins: number) =>
    request.patch<any, PlanItem>(`/daily-plans/${id}/actual-time`, { actualMins }),

  // ---- 模板相关 ----

  getTemplates: () =>
    request.get<any, PlanItem[]>('/daily-plans/templates'),

  saveAsTemplate: (id: number, templateName: string) =>
    request.post<any, PlanItem>('/daily-plans/templates', { planId: id, templateName }),

  applyTemplate: (templateId: number, planDate: string) =>
    request.post<any, PlanItem[]>('/daily-plans/templates/apply', { templateId, planDate }),

  // ---- 重复任务 ----

  updateRepeat: (id: number, data: RepeatUpdateRequest) =>
    request.patch<any, PlanItem>(`/daily-plans/${id}/repeat`, data),

  stopRepeat: (id: number) =>
    request.post<any, PlanItem>(`/daily-plans/${id}/stop-repeat`),

  // ---- 统计数据 ----

  getStatistics: (startDate: string, endDate: string) =>
    request.get<any, PlanStatistics[]>('/daily-plans/statistics', {
      params: { startDate, endDate }
    }),

  // ---- 时间块冲突检测 ----

  checkTimeConflicts: (planDate: string, startTime: string, endTime: string, excludeId?: number) =>
    request.get<any, PlanItem[]>('/daily-plans/time-conflicts', {
      params: { planDate, startTime, endTime, excludeId }
    }),

  // ---- 时间块相关 ----

  getTimeBlocks: (planDate: string) =>
    request.get<any, PlanItem[]>('/daily-plans/time-blocks', {
      params: { planDate }
    }),

  detectTimeConflicts: (planDate: string) =>
    request.get<any, PlanItem[]>('/daily-plans/time-conflicts/detect', {
      params: { planDate }
    }),

  // ---- 数据分析相关 ----

  getCompletionTrend: (startDate: string, endDate: string) =>
    request.get<any, any[]>('/daily-plans/analytics/completion-trend', {
      params: { startDate, endDate }
    }),

  getCategoryDistribution: (startDate: string, endDate: string) =>
    request.get<any, any>('/daily-plans/analytics/category-distribution', {
      params: { startDate, endDate }
    }),

  getPriorityDistribution: (startDate: string, endDate: string) =>
    request.get<any, any>('/daily-plans/analytics/priority-distribution', {
      params: { startDate, endDate }
    }),

  getTimeDistribution: (startDate: string, endDate: string) =>
    request.get<any, any>('/daily-plans/analytics/time-distribution', {
      params: { startDate, endDate }
    })
}

// ============================================================================
// 【subtaskApi — 子任务相关的 API 方法集合】
// ============================================================================

export interface SubtaskItem {
  id: number
  userId: number
  parentId: number
  title: string
  description: string | null
  priority: 'P0' | 'P1' | 'P2' | 'P3'
  status: 'TODO' | 'IN_PROGRESS' | 'DONE' | 'CANCELLED'
  sortOrder: number
  completedAt: string | null
  createdAt: string
  updatedAt: string
}

export interface CreateSubtaskRequest {
  title: string
  description?: string
  priority?: string
  sortOrder?: number
}

export const subtaskApi = {
  list: (parentId: number) =>
    request.get<any, SubtaskItem[]>(`/daily-plans/${parentId}/subtasks`),

  create: (parentId: number, data: CreateSubtaskRequest) =>
    request.post<any, SubtaskItem>(`/daily-plans/${parentId}/subtasks`, data),

  update: (parentId: number, id: number, data: Partial<CreateSubtaskRequest>) =>
    request.put<any, SubtaskItem>(`/daily-plans/${parentId}/subtasks/${id}`, data),

  delete: (parentId: number, id: number) =>
    request.delete<any, void>(`/daily-plans/${parentId}/subtasks/${id}`),

  updateStatus: (parentId: number, id: number, status: string) =>
    request.patch<any, SubtaskItem>(`/daily-plans/${parentId}/subtasks/${id}/status`, { status }),

  reorder: (parentId: number, items: Array<{ id: number; sortOrder: number }>) =>
    request.post<any, void>(`/daily-plans/${parentId}/subtasks/reorder`, { items }),

  promoteToMainTask: (parentId: number, id: number) =>
    request.post<any, PlanItem>(`/daily-plans/${parentId}/subtasks/${id}/promote`)
}

// ============================================================================
// 【reminderApi — 提醒相关的 API 方法集合】
// ============================================================================

export interface ReminderItem {
  id: number
  userId: number
  planId: number
  reminderTime: string
  isNotified: number
  createdAt: string
}

export const reminderApi = {
  list: (planId?: number) =>
    request.get<any, ReminderItem[]>('/reminders', { params: { planId } }),

  create: (data: { planId: number; reminderTime: string }) =>
    request.post<any, ReminderItem>('/reminders', data),

  update: (id: number, data: { reminderTime?: string; isNotified?: number }) =>
    request.put<any, ReminderItem>(`/reminders/${id}`, data),

  delete: (id: number) =>
    request.delete<any, void>(`/reminders/${id}`),

  markAsNotified: (id: number) =>
    request.patch<any, ReminderItem>(`/reminders/${id}/mark-notified`)
}

// ============================================================================
// 【batchApi — 批量操作相关的 API 方法集合】
// ============================================================================

export const batchApi = {
  delete: (ids: number[]) =>
    request.delete<any, void>('/daily-plans/batch', { data: { ids } }),

  updateStatus: (ids: number[], status: string) =>
    request.patch<any, void>('/daily-plans/batch/status', { ids, status })
}

// ============================================================================
// 【tagApi — 标签管理相关的 API 方法集合】
// ============================================================================

export interface TagItem {
  id: number
  userId: number
  name: string
  color: string
  planCount: number
  createdAt: string
}

export interface CreateTagRequest {
  name: string
  color: string
}

export const tagApi = {
  list: () =>
    request.get<any, TagItem[]>('/tags'),

  create: (data: CreateTagRequest) =>
    request.post<any, TagItem>('/tags', data),

  update: (id: number, data: Partial<CreateTagRequest>) =>
    request.put<any, TagItem>(`/tags/${id}`, data),

  delete: (id: number) =>
    request.delete<any, void>(`/tags/${id}`),

  // 关联/取消关联标签和计划
  associateWithPlan: (tagId: number, planId: number) =>
    request.post<any, void>(`/tags/${tagId}/plans/${planId}`),

  dissociateFromPlan: (tagId: number, planId: number) =>
    request.delete<any, void>(`/tags/${tagId}/plans/${planId}`),

  // 获取某个计划的所有标签
  getPlansTags: (planId: number) =>
    request.get<any, TagItem[]>(`/tags/plan/${planId}`)
}
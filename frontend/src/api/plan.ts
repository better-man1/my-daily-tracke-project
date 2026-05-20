/**
 * ============================================================================
 * 【每日计划 API 模块（Plan API）】
 * ============================================================================
 *
 * 【模块用途】
 * 这是项目中最大、最复杂的 API 模块，封装了每日计划/任务管理相关的所有 HTTP 请求。
 * 包含五大功能区域：
 *   1. 基础 CRUD：计划的增删改查、状态更新、排序、推迟
 *   2. 重复任务：重复规则的设置、实例生成、停止重复
 *   3. 子任务：子任务的创建、查询、状态更新、转为主任务
 *   4. 数据分析：完成趋势、分类分布、优先级分布、时间分布
 *   5. 时间块：时间冲突检测、时间块查询
 *   6. 模板：模板列表、保存为模板
 *
 * 此外还导出了三个独立的 API 对象：
 *   - reminderApi：提醒相关接口
 *   - batchApi：批量操作接口
 *   - tagApi：标签管理接口
 *
 * 【设计思想】
 * 1. RESTful 资源嵌套：子任务通过 /daily-plans/:parentId/subtasks 的嵌套路径体现父子关系。
 * 2. 功能分组导出：将不同关注点的 API 拆分为独立对象（planApi、reminderApi、batchApi、tagApi），
 *    避免 一个对象过于庞大，同时保持各功能的内聚性。
 * 3. 丰富的字面量类型：优先级（P0-P3）、分类（工作/学习/生活/健康）、
 *    状态（待办/进行中/完成/取消）等都使用字面量联合类型严格约束。
 *
 * 【学习要点】
 * - 复杂 TypeScript 接口的组织方式
 * - RESTful API 的嵌套资源路径设计
 * - 多个 API 对象从同一模块导出的模式
 * - Axios DELETE 请求传递 body 数据的方式（batchDelete 使用 { data: ids }）
 *
 * ============================================================================
 */

// 导入封装好的 Axios 请求实例
import request from './request'

// ============================================================================
// 【TypeScript 接口定义 — 每日计划】
// ============================================================================

/**
 * PlanItem — 每日计划/任务的数据结构
 *
 * 这是整个计划模块的核心数据结构，描述一个完整的任务对象。
 * 包含任务基本信息、优先级、分类、时间估算、状态、重复规则、子任务等。
 *
 * @interface PlanItem
 */
export interface PlanItem {
  id: number                     // 任务唯一标识 ID
  userId: number                 // 所属用户 ID
  goalId: number | null          // 关联的目标 ID（null 表示未关联目标）
  title: string                  // 任务标题
  description: string | null     // 任务详细描述
  planDate: string               // 计划日期（格式：'YYYY-MM-DD'）

  // ---- 优先级 ----
  // P0：紧急且重要（最高优先级）
  // P1：重要不紧急
  // P2：紧急不重要
  // P3：不紧急不重要（最低优先级）
  priority: 'P0' | 'P1' | 'P2' | 'P3'

  // ---- 分类 ----
  // WORK：工作相关
  // STUDY：学习相关
  // LIFE：生活相关
  // HEALTH：健康相关
  category: 'WORK' | 'STUDY' | 'LIFE' | 'HEALTH'

  // ---- 时间估算 ----
  estimatedMins: number | null   // 预估耗时（分钟），null 表示未估算
  actualMins: number | null      // 实际耗时（分钟），null 表示未记录

  // ---- 状态 ----
  // TODO：待办
  // IN_PROGRESS：进行中
  // DONE：已完成
  // CANCELLED：已取消
  status: 'TODO' | 'IN_PROGRESS' | 'DONE' | 'CANCELLED'

  sortOrder: number              // 排序序号（用于拖拽排序）
  completedAt: string | null     // 完成时间（null 表示未完成）

  // ---- 模板相关 ----
  isTemplate: number             // 是否为模板（0=否，1=是）
  templateName: string | null    // 模板名称

  // ---- 重复任务相关 ----
  // NONE：不重复（一次性任务）
  // DAILY：每天重复
  // WEEKLY：每周重复
  // MONTHLY：每月重复
  // CUSTOM：自定义重复规则
  repeatType?: 'NONE' | 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'CUSTOM'
  repeatPattern?: string         // 自定义重复模式（如 cron 表达式或特定格式）
  repeatEndDate?: string         // 重复结束日期

  // ---- 子任务相关 ----
  parentId?: number | null       // 父任务 ID（null 表示顶层任务）
  subtaskCount?: number          // 子任务总数
  completedSubtaskCount?: number // 已完成的子任务数
  children?: PlanItem[]          // 子任务列表（递归结构，与 GoalItem 类似）

  // ---- 时间块相关 ----
  startTime?: string | null      // 时间块开始时间（格式：'HH:mm'）
  endTime?: string | null        // 时间块结束时间（格式：'HH:mm'）
  isTimeblock?: number           // 是否为时间块（0=否，1=是）

  // ---- 时间戳 ----
  createdAt: string              // 创建时间
  updatedAt: string              // 更新时间
}

/**
 * CreatePlanRequest — 创建/更新计划的请求数据结构
 *
 * 与 PlanItem 相比：
 * - 去掉了自动生成的字段（id、userId、createdAt 等）
 * - 大部分字段为可选，创建任务时只需提供 title 和 planDate
 *
 * @interface CreatePlanRequest
 */
export interface CreatePlanRequest {
  title: string              // 任务标题（必填）
  description?: string       // 任务描述（可选）
  planDate: string           // 计划日期（必填）
  priority?: string          // 优先级（可选，默认 P2）
  category?: string          // 分类（可选）
  estimatedMins?: number     // 预估耗时（可选）
  goalId?: number            // 关联目标 ID（可选）
  sortOrder?: number         // 排序序号（可选）
  startTime?: string         // 时间块开始时间（可选）
  endTime?: string           // 时间块结束时间（可选）
  isTimeblock?: number       // 是否为时间块（可选）
  repeatType?: string        // 重复类型（可选）
  repeatPattern?: string     // 重复模式（可选）
  repeatEndDate?: string     // 重复结束日期（可选）
}

/**
 * PlanStatistics — 计划统计数据结构
 *
 * 描述某一天的任务完成情况统计。
 *
 * @interface PlanStatistics
 */
export interface PlanStatistics {
  date: string               // 统计日期
  total: number              // 总任务数
  done: number               // 已完成数
  inProgress: number         // 进行中数
  todo: number               // 待办数
  cancelled: number          // 已取消数
  completionRate: number     // 完成率（百分比，0-100）
  totalEstimatedMins: number // 总预估时间（分钟）
  totalActualMins: number    // 总实际时间（分钟）
}

/**
 * RepeatUpdateRequest — 更新重复规则的请求数据结构
 *
 * @interface RepeatUpdateRequest
 */
export interface RepeatUpdateRequest {
  repeatType?: 'NONE' | 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'CUSTOM'  // 重复类型
  repeatPattern?: string     // 重复模式
  repeatEndDate?: string     // 重复结束日期
}

// ============================================================================
// 【planApi — 每日计划相关的 API 方法集合】
// ============================================================================
export const planApi = {
  // ---- 基础 CRUD 操作 ----

  /**
   * list — 查询指定日期的计划列表
   *
   * @param {string} [planDate] - 计划日期（格式：'YYYY-MM-DD'），不传则查全部
   * @returns {Promise<PlanItem[]>} 计划列表
   *
   * 请求方式：GET /api/v1/daily-plans?planDate=xxx
   */
  list: (planDate?: string) =>
    request.get<any, PlanItem[]>('/daily-plans', { params: { planDate } }),

  /**
   * create — 创建新的计划/任务
   *
   * @param {CreatePlanRequest} data - 计划数据
   * @returns {Promise<PlanItem>} 创建成功后返回完整的计划对象
   *
   * 请求方式：POST /api/v1/daily-plans
   */
  create: (data: CreatePlanRequest) => request.post<any, PlanItem>('/daily-plans', data),

  /**
   * update — 更新指定的计划
   *
   * @param {number} id              - 计划 ID
   * @param {CreatePlanRequest} data - 更新后的计划数据
   * @returns {Promise<PlanItem>} 更新后的完整计划对象
   *
   * 请求方式：PUT /api/v1/daily-plans/:id
   */
  update: (id: number, data: CreatePlanRequest) =>
    request.put<any, PlanItem>(`/daily-plans/${id}`, data),

  /**
   * delete — 删除指定的计划
   *
   * @param {number} id - 计划 ID
   * @returns {Promise<void>} 删除成功无返回数据
   *
   * 请求方式：DELETE /api/v1/daily-plans/:id
   */
  delete: (id: number) => request.delete<any, void>(`/daily-plans/${id}`),

  /**
   * updateStatus — 更新任务状态
   *
   * 专门用于快速切换任务状态的接口，比 update 更轻量。
   *
   * @param {number} id     - 计划 ID
   * @param {string} status - 新状态（'TODO' | 'IN_PROGRESS' | 'DONE' | 'CANCELLED'）
   * @returns {Promise<void>} 更新成功无返回数据
   *
   * 请求方式：PUT /api/v1/daily-plans/:id/status?status=xxx
   * 注意：请求体为 null，状态通过 URL 参数传递
   */
  updateStatus: (id: number, status: string) =>
    request.put<any, void>(`/daily-plans/${id}/status`, null, { params: { status } }),

  /**
   * batchSort — 批量更新排序
   *
   * 接收一个 { id: sortOrder } 的映射对象，一次性更新多个任务的排序序号。
   * 通常在用户拖拽排序后调用。
   *
   * @param {Record<number, number>} sortMap - 排序映射，key 是任务 ID，value 是新的排序序号
   *   例如：{ 1: 0, 2: 1, 3: 2 } 表示 ID 为 1 的任务排第 0 位
   * @returns {Promise<void>} 排序成功无返回数据
   *
   * 请求方式：PUT /api/v1/daily-plans/batch-sort
   */
  batchSort: (sortMap: Record<number, number>) =>
    request.put<any, void>('/daily-plans/batch-sort', sortMap),

  /**
   * postpone — 推迟任务到明天
   *
   * 将任务的计划日期改为明天，用于今天无法完成的任务。
   *
   * @param {number} id - 计划 ID
   * @returns {Promise<PlanItem>} 推迟后的计划对象（planDate 已更新为明天）
   *
   * 请求方式：POST /api/v1/daily-plans/:id/postpone
   */
  postpone: (id: number) => request.post<any, PlanItem>(`/daily-plans/${id}/postpone`),

  // ---- 统计 ----

  /**
   * statistics — 获取指定日期的统计数据
   *
   * @param {string} [planDate] - 日期，不传则默认查今天
   * @returns {Promise<PlanStatistics>} 计划统计数据
   *
   * 请求方式：GET /api/v1/daily-plans/statistics?planDate=xxx
   */
  statistics: (planDate?: string) =>
    request.get<any, PlanStatistics>('/daily-plans/statistics', { params: { planDate } }),

  // ---- 模板 ----

  /**
   * listTemplates — 获取所有任务模板
   *
   * 模板是预设的任务结构，可以快速创建相似的任务。
   *
   * @returns {Promise<PlanItem[]>} 模板列表
   *
   * 请求方式：GET /api/v1/daily-plans/templates
   */
  listTemplates: () => request.get<any, PlanItem[]>('/daily-plans/templates'),

  /**
   * saveAsTemplate — 将现有任务保存为模板
   *
   * @param {number} id            - 要保存为模板的任务 ID
   * @param {string} templateName  - 模板名称
   * @returns {Promise<void>} 保存成功无返回数据
   *
   * 请求方式：POST /api/v1/daily-plans/:id/templates?templateName=xxx
   */
  saveAsTemplate: (id: number, templateName: string) =>
    request.post<any, void>(`/daily-plans/${id}/templates`, null, { params: { templateName } }),

  // ---- 重复任务相关 ----

  /**
   * generateRepeatInstances — 生成指定日期范围内的重复任务实例
   *
   * 根据重复规则，在指定的日期范围内生成任务实例。
   *
   * @param {number} id         - 重复任务模板 ID
   * @param {string} startDate  - 起始日期
   * @param {string} endDate    - 截止日期
   * @returns {Promise<PlanItem[]>} 生成的任务实例列表
   *
   * 请求方式：POST /api/v1/daily-plans/:id/repeat/instances?startDate=xxx&endDate=xxx
   */
  generateRepeatInstances: (id: number, startDate: string, endDate: string) =>
    request.post<any, PlanItem[]>(`/daily-plans/${id}/repeat/instances`, null, {
      params: { startDate, endDate }
    }),

  /**
   * updateRepeatRule — 更新任务的重复规则
   *
   * @param {number} id                  - 任务 ID
   * @param {RepeatUpdateRequest} data   - 新的重复规则
   * @returns {Promise<PlanItem>} 更新后的任务对象
   *
   * 请求方式：PUT /api/v1/daily-plans/:id/repeat
   */
  updateRepeatRule: (id: number, data: RepeatUpdateRequest) =>
    request.put<any, PlanItem>(`/daily-plans/${id}/repeat`, data),

  /**
   * stopRepeat — 停止任务的重复规则
   *
   * 取消任务的重复设置，使其变为一次性任务。
   *
   * @param {number} id - 任务 ID
   * @returns {Promise<void>} 停止成功无返回数据
   *
   * 请求方式：DELETE /api/v1/daily-plans/:id/repeat
   */
  stopRepeat: (id: number) => request.delete<any, void>(`/daily-plans/${id}/repeat`),

  // ---- 子任务相关 ----

  /**
   * createSubtask — 在父任务下创建子任务
   *
   * 使用嵌套路径 /daily-plans/:parentId/subtasks 体现父子关系。
   *
   * @param {number} parentId           - 父任务 ID
   * @param {CreatePlanRequest} data    - 子任务数据
   * @returns {Promise<PlanItem>} 创建的子任务对象
   *
   * 请求方式：POST /api/v1/daily-plans/:parentId/subtasks
   */
  createSubtask: (parentId: number, data: CreatePlanRequest) =>
    request.post<any, PlanItem>(`/daily-plans/${parentId}/subtasks`, data),

  /**
   * getSubtasks — 获取父任务的所有子任务
   *
   * @param {number} parentId - 父任务 ID
   * @returns {Promise<PlanItem[]>} 子任务列表
   *
   * 请求方式：GET /api/v1/daily-plans/:parentId/subtasks
   */
  getSubtasks: (parentId: number) =>
    request.get<any, PlanItem[]>(`/daily-plans/${parentId}/subtasks`),

  /**
   * updateSubtaskStatus — 更新子任务状态
   *
   * @param {number} id     - 子任务 ID
   * @param {string} status - 新状态
   * @returns {Promise<void>} 更新成功无返回数据
   *
   * 请求方式：PUT /api/v1/daily-plans/subtasks/:id/status?status=xxx
   */
  updateSubtaskStatus: (id: number, status: string) =>
    request.put<any, void>(`/daily-plans/subtasks/${id}/status`, null, { params: { status } }),

  /**
   * convertToMainTask — 将子任务转换为主任务
   *
   * 子任务脱离父任务，成为独立的顶层任务。
   *
   * @param {number} id - 子任务 ID
   * @returns {Promise<void>} 转换成功无返回数据
   *
   * 请求方式：POST /api/v1/daily-plans/subtasks/:id/convert
   */
  convertToMainTask: (id: number) =>
    request.post<any, void>(`/daily-plans/subtasks/${id}/convert`),

  // ---- 数据分析 ----

  /**
   * getCompletionTrend — 获取任务完成趋势
   *
   * @param {string} startDate - 起始日期
   * @param {string} endDate   - 截止日期
   * @returns {Promise<any[]>} 趋势数据（日期+完成数序列）
   *
   * 请求方式：GET /api/v1/daily-plans/analytics/trend?startDate=xxx&endDate=xxx
   */
  getCompletionTrend: (startDate: string, endDate: string) =>
    request.get<any, any[]>('/daily-plans/analytics/trend', { params: { startDate, endDate } }),

  /**
   * getCategoryDistribution — 获取任务分类分布
   *
   * @param {string} startDate - 起始日期
   * @param {string} endDate   - 截止日期
   * @returns {Promise<any>} 分类分布数据
   *
   * 请求方式：GET /api/v1/daily-plans/analytics/category?startDate=xxx&endDate=xxx
   */
  getCategoryDistribution: (startDate: string, endDate: string) =>
    request.get<any, any>('/daily-plans/analytics/category', { params: { startDate, endDate } }),

  /**
   * getPriorityDistribution — 获取优先级分布
   *
   * @param {string} startDate - 起始日期
   * @param {string} endDate   - 截止日期
   * @returns {Promise<any>} 优先级分布数据
   *
   * 请求方式：GET /api/v1/daily-plans/analytics/priority?startDate=xxx&endDate=xxx
   */
  getPriorityDistribution: (startDate: string, endDate: string) =>
    request.get<any, any>('/daily-plans/analytics/priority', { params: { startDate, endDate } }),

  /**
   * getTimeDistribution — 获取时间分布
   *
   * @param {string} startDate - 起始日期
   * @param {string} endDate   - 截止日期
   * @returns {Promise<any>} 时间分布数据
   *
   * 请求方式：GET /api/v1/daily-plans/analytics/time?startDate=xxx&endDate=xxx
   */
  getTimeDistribution: (startDate: string, endDate: string) =>
    request.get<any, any>('/daily-plans/analytics/time', { params: { startDate, endDate } }),

  // ---- 时间块相关 ----

  /**
   * detectTimeConflicts — 检测时间冲突
   *
   * 检查指定日期是否有时间块重叠（两个时间块的时间范围有交叉）。
   *
   * @param {string} planDate - 计划日期
   * @returns {Promise<PlanItem[]>} 存在冲突的时间块列表
   *
   * 请求方式：GET /api/v1/daily-plans/timeblock/conflicts?planDate=xxx
   */
  detectTimeConflicts: (planDate: string) =>
    request.get<any, PlanItem[]>('/daily-plans/timeblock/conflicts', { params: { planDate } }),

  /**
   * getTimeBlocks — 获取指定日期的时间块列表
   *
   * @param {string} planDate - 计划日期
   * @returns {Promise<PlanItem[]>} 时间块列表
   *
   * 请求方式：GET /api/v1/daily-plans/timeblock?planDate=xxx
   */
  getTimeBlocks: (planDate: string) =>
    request.get<any, PlanItem[]>('/daily-plans/timeblock', { params: { planDate } })
}

// ============================================================================
// 【提醒相关接口】
// ============================================================================

/**
 * ReminderSetRequest — 设置提醒的请求数据结构
 *
 * @interface ReminderSetRequest
 */
export interface ReminderSetRequest {
  // 提醒类型
  // START：任务开始时提醒
  // DUE：任务截止时提醒
  // CUSTOM：自定义时间提醒
  reminderType: 'START' | 'DUE' | 'CUSTOM'
  reminderTime?: string       // 提醒的具体时间（CUSTOM 类型必填）
  advanceMinutes?: number     // 提前多少分钟提醒（START/DUE 类型使用）
}

/**
 * ReminderInfo — 提醒信息的数据结构
 *
 * @interface ReminderInfo
 */
export interface ReminderInfo {
  id: number                  // 提醒记录 ID
  reminderType: string        // 提醒类型
  reminderTime: string        // 提醒时间
  isSent: boolean             // 是否已发送提醒
}

/**
 * reminderApi — 提醒相关的 API 方法集合
 *
 * 管理任务的提醒设置，包括设置、删除、查询提醒。
 */
export const reminderApi = {
  /**
   * setReminder — 为任务设置提醒
   *
   * @param {number} planId              - 任务 ID
   * @param {ReminderSetRequest} data    - 提醒设置数据
   * @returns {Promise<void>} 设置成功无返回数据
   *
   * 请求方式：POST /api/v1/plan-reminders/:planId
   */
  setReminder: (planId: number, data: ReminderSetRequest) =>
    request.post<any, void>(`/plan-reminders/${planId}`, data),

  /**
   * deleteReminder — 删除任务的提醒
   *
   * @param {number} planId - 任务 ID
   * @returns {Promise<void>} 删除成功无返回数据
   *
   * 请求方式：DELETE /api/v1/plan-reminders/:planId
   */
  deleteReminder: (planId: number) =>
    request.delete<any, void>(`/plan-reminders/${planId}`),

  /**
   * getReminders — 获取任务的所有提醒
   *
   * @param {number} planId - 任务 ID
   * @returns {Promise<ReminderInfo[]>} 提醒列表
   *
   * 请求方式：GET /api/v1/plan-reminders/:planId
   */
  getReminders: (planId: number) =>
    request.get<any, ReminderInfo[]>(`/plan-reminders/${planId}`)
}

// ============================================================================
// 【批量操作相关接口】
// ============================================================================

/**
 * BatchUpdateRequest — 批量更新的请求数据结构
 *
 * @interface BatchUpdateRequest
 */
export interface BatchUpdateRequest {
  ids: number[]               // 要操作的任务 ID 列表
  priority?: string           // 统一设置的优先级（可选）
  category?: string           // 统一设置的分类（可选）
  status?: string             // 统一设置的状态（可选）
}

/**
 * batchApi — 批量操作相关的 API 方法集合
 *
 * 提供批量更新、删除、推迟、完成等操作，减少多次单独请求的开销。
 */
export const batchApi = {
  /**
   * batchUpdate — 批量更新任务属性
   *
   * @param {BatchUpdateRequest} data - 批量更新数据（包含 ID 列表和要更新的属性）
   * @returns {Promise<void>} 更新成功无返回数据
   *
   * 请求方式：PUT /api/v1/daily-plans/batch-update
   */
  batchUpdate: (data: BatchUpdateRequest) =>
    request.put<any, void>('/daily-plans/batch-update', data),

  /**
   * batchDelete — 批量删除任务
   *
   * 注意：Axios 的 DELETE 请求传递 body 数据需要使用 { data: ids } 格式，
   * 而不是直接传第二个参数（第二个参数是 config 对象）。
   *
   * @param {number[]} ids - 要删除的任务 ID 列表
   * @returns {Promise<void>} 删除成功无返回数据
   *
   * 请求方式：DELETE /api/v1/daily-plans/batch-delete
   * 请求体：[1, 2, 3]（ID 数组）
   */
  batchDelete: (ids: number[]) =>
    request.delete<any, void>('/daily-plans/batch-delete', { data: ids }),

  /**
   * batchPostpone — 批量推迟任务到明天
   *
   * @param {number[]} ids - 要推迟的任务 ID 列表
   * @returns {Promise<void>} 推迟成功无返回数据
   *
   * 请求方式：PUT /api/v1/daily-plans/batch-postpone
   * 请求体：[1, 2, 3]（ID 数组）
   */
  batchPostpone: (ids: number[]) =>
    request.put<any, void>('/daily-plans/batch-postpone', ids),

  /**
   * batchComplete — 批量标记任务为已完成
   *
   * @param {number[]} ids - 要完成的任务 ID 列表
   * @returns {Promise<void>} 操作成功无返回数据
   *
   * 请求方式：PUT /api/v1/daily-plans/batch-complete
   * 请求体：[1, 2, 3]（ID 数组）
   */
  batchComplete: (ids: number[]) =>
    request.put<any, void>('/daily-plans/batch-complete', ids)
}

// ============================================================================
// 【标签相关接口】
// ============================================================================

/**
 * TagItem — 标签的数据结构
 *
 * @interface TagItem
 */
export interface TagItem {
  id: number          // 标签唯一标识 ID
  userId: number      // 所属用户 ID
  name: string        // 标签名称
  color: string       // 标签颜色（十六进制颜色值）
  createdAt: string   // 创建时间
  updatedAt: string   // 更新时间
}

/**
 * TagCreateRequest — 创建标签的请求数据结构
 *
 * @interface TagCreateRequest
 */
export interface TagCreateRequest {
  name: string        // 标签名称（必填）
  color?: string      // 标签颜色（可选，默认使用预设颜色）
}

/**
 * tagApi — 标签管理相关的 API 方法集合
 *
 * 提供标签的 CRUD 操作，以及标签与任务的关联管理。
 */
export const tagApi = {
  /**
   * list — 获取所有标签
   *
   * @returns {Promise<TagItem[]>} 标签列表
   *
   * 请求方式：GET /api/v1/plan-tags
   */
  list: () => request.get<any, TagItem[]>('/plan-tags'),

  /**
   * create — 创建新标签
   *
   * @param {TagCreateRequest} data - 标签数据
   * @returns {Promise<TagItem>} 创建的标签对象
   *
   * 请求方式：POST /api/v1/plan-tags
   */
  create: (data: TagCreateRequest) => request.post<any, TagItem>('/plan-tags', data),

  /**
   * update — 更新标签
   *
   * @param {number} id              - 标签 ID
   * @param {TagCreateRequest} data  - 更新后的标签数据
   * @returns {Promise<TagItem>} 更新后的标签对象
   *
   * 请求方式：PUT /api/v1/plan-tags/:id
   */
  update: (id: number, data: TagCreateRequest) =>
    request.put<any, TagItem>(`/plan-tags/${id}`, data),

  /**
   * delete — 删除标签
   *
   * @param {number} id - 标签 ID
   * @returns {Promise<void>} 删除成功无返回数据
   *
   * 请求方式：DELETE /api/v1/plan-tags/:id
   */
  delete: (id: number) => request.delete<any, void>(`/plan-tags/${id}`),

  /**
   * addTagsToPlan — 为任务添加标签
   *
   * 将多个标签关联到指定任务。
   *
   * @param {number} planId  - 任务 ID
   * @param {number[]} tagIds - 要添加的标签 ID 列表
   * @returns {Promise<void>} 添加成功无返回数据
   *
   * 请求方式：POST /api/v1/plan-tags/plans/:planId
   * 请求体：[1, 2, 3]（标签 ID 数组）
   */
  addTagsToPlan: (planId: number, tagIds: number[]) =>
    request.post<any, void>(`/plan-tags/plans/${planId}`, tagIds),

  /**
   * removeTagsFromPlan — 从任务中移除标签
   *
   * 取消指定任务与多个标签的关联。
   *
   * @param {number} planId  - 任务 ID
   * @param {number[]} tagIds - 要移除的标签 ID 列表
   * @returns {Promise<void>} 移除成功无返回数据
   *
   * 请求方式：DELETE /api/v1/plan-tags/plans/:planId
   * 请求体：[1, 2, 3]（标签 ID 数组）
   */
  removeTagsFromPlan: (planId: number, tagIds: number[]) =>
    request.delete<any, void>(`/plan-tags/plans/${planId}`, { data: tagIds }),

  /**
   * getPlanTags — 获取任务关联的所有标签
   *
   * @param {number} planId - 任务 ID
   * @returns {Promise<TagItem[]>} 标签列表
   *
   * 请求方式：GET /api/v1/plan-tags/plans/:planId
   */
  getPlanTags: (planId: number) =>
    request.get<any, TagItem[]>(`/plan-tags/plans/${planId}`)
}

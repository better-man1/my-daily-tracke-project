/**
 * ============================================================================
 * 【目标管理 API 模块（Goal API）】
 * ============================================================================
 *
 * 【模块用途】
 * 封装目标管理（OKR 风格）相关的 HTTP 请求，包括目标的增删改查、
 * 目标树形结构查询、进度更新、统计分析等功能。
 *
 * 【设计思想】
 * 1. OKR 目标体系：支持五年规划、年度目标、月度目标、周目标的多层级目标体系。
 *    通过 parentId 实现父子关系，通过 keyResults 实现关键结果（OKR）。
 * 2. 树形结构查询：getTree 接口返回包含 children 子目标的树形结构，
 *    前端可以直接用此数据渲染树形组件。
 * 3. 状态机模式：目标有四种状态（未开始、进行中、已完成、已放弃），
 *    使用 TypeScript 字面量联合类型约束合法的状态值。
 *
 * 【学习要点】
 * - 递归类型定义（GoalItem 的 children 字段引用自身）
 * - 多种字面量联合类型（goalType、status 等）
 * - 可选属性与可空类型的组合使用
 * - PUT 请求的 URL 参数传递方式
 *
 * ============================================================================
 */

// 导入封装好的 Axios 请求实例
import request from './request'

// ============================================================================
// 【TypeScript 接口定义】
// ============================================================================

/**
 * GoalKr — 关键结果（Key Result）的数据结构
 *
 * 在 OKR 方法论中，每个目标（Objective）下可以有多个关键结果（Key Results），
 * 用于量化衡量目标的完成程度。例如：
 * - 目标：提高编程能力
 *   - 关键结果1：完成 10 个项目（targetValue=10, currentValue=3）
 *   - 关键结果2：阅读 5 本技术书籍（targetValue=5, currentValue=2）
 *
 * @interface GoalKr
 */
export interface GoalKr {
  id?: number              // 关键结果 ID（创建时不传，更新时必传）
  title: string            // 关键结果标题（描述可量化的目标）
  targetValue: number      // 目标值（如：10 个项目）
  currentValue: number     // 当前已完成值（如：3 个项目）
  unit: string             // 单位（如：个、本、小时、% 等）
  progress?: number        // 自动计算的完成进度百分比（0-100）
  sortOrder?: number       // 排序序号（用于调整关键结果的显示顺序）
}

/**
 * GoalItem — 目标的数据结构
 *
 * 描述一个完整的目标对象，包含目标的基本信息、进度、状态、
 * 子目标（children）和关键结果（keyResults）。
 *
 * 注意 children 字段的递归类型定义：children?: GoalItem[]
 * 这表示子目标也是 GoalItem 类型，可以无限嵌套，形成树形结构。
 *
 * @interface GoalItem
 */
export interface GoalItem {
  id: number                                   // 目标唯一标识 ID
  parentId: number | null                      // 父目标 ID（null 表示顶层目标）
  title: string                                // 目标标题
  description: string | null                   // 目标详细描述
  goalType: 'FIVE_YEAR' | 'YEARLY' | 'MONTHLY' | 'WEEKLY'  // 目标类型
                                               // FIVE_YEAR：五年规划
                                               // YEARLY：年度目标
                                               // MONTHLY：月度目标
                                               // WEEKLY：周目标
  category: string                             // 目标分类（如：工作、学习、健康等）
  startDate: string                            // 开始日期（格式：'YYYY-MM-DD'）
  endDate: string                              // 截止日期（格式：'YYYY-MM-DD'）
  progress: number                             // 完成进度（0-100 的百分比）
  status: 'NOT_STARTED' | 'IN_PROGRESS' | 'COMPLETED' | 'ABANDONED'  // 目标状态
           // NOT_STARTED：未开始
           // IN_PROGRESS：进行中
           // COMPLETED：已完成
           // ABANDONED：已放弃
  priority: number                             // 优先级（数值越大优先级越高）
  sortOrder: number                            // 排序序号
  children?: GoalItem[]                        // 子目标列表（递归结构）
  keyResults?: GoalKr[]                        // 关键结果列表
  createdAt: string                            // 创建时间
}

/**
 * CreateGoalRequest — 创建/更新目标的请求数据结构
 *
 * 与 GoalItem 的区别：
 * - 大部分字段是可选的（创建时只需提供 title、goalType、startDate、endDate）
 * - 没有 id、createdAt 等自动生成字段
 * - 字段类型更宽松（如 goalType 用 string 而非字面量联合，便于前端传值）
 *
 * @interface CreateGoalRequest
 */
export interface CreateGoalRequest {
  parentId?: number | null   // 父目标 ID（可选，不传则为顶层目标）
  title: string              // 目标标题（必填）
  description?: string       // 目标描述（可选）
  goalType: string           // 目标类型（必填：FIVE_YEAR/YEARLY/MONTHLY/WEEKLY）
  category?: string          // 目标分类（可选）
  startDate: string          // 开始日期（必填）
  endDate: string            // 截止日期（必填）
  progress?: number          // 初始进度（可选，默认 0）
  status?: string            // 初始状态（可选，默认 NOT_STARTED）
  priority?: number          // 优先级（可选）
  sortOrder?: number         // 排序序号（可选）
  keyResults?: GoalKr[]      // 关键结果列表（可选）
}

// ============================================================================
// 【API 方法定义】
// ============================================================================

/**
 * goalApi — 目标管理相关的 API 方法集合
 *
 * 提供目标的 CRUD 操作、树形结构查询、进度更新、统计分析等功能。
 */
export const goalApi = {
  /**
   * list — 查询目标列表
   *
   * 支持按目标类型、分类、状态筛选。返回扁平化的目标列表。
   *
   * @param {Object} params - 查询参数（均为可选）
   * @param {string} [params.goalType] - 目标类型筛选
   * @param {string} [params.category] - 分类筛选
   * @param {string} [params.status]   - 状态筛选
   * @returns {Promise<GoalItem[]>} 目标列表
   *
   * 请求方式：GET /api/v1/goals
   */
  list: (params: { goalType?: string; category?: string; status?: string }) =>
    request.get<any, GoalItem[]>('/goals', { params }),

  /**
   * create — 创建新目标
   *
   * @param {CreateGoalRequest} data - 目标数据
   * @returns {Promise<GoalItem>} 创建成功后返回完整的目标对象
   *
   * 请求方式：POST /api/v1/goals
   */
  create: (data: CreateGoalRequest) => request.post<any, GoalItem>('/goals', data),

  /**
   * update — 更新指定的目标
   *
   * @param {number} id              - 目标 ID
   * @param {CreateGoalRequest} data - 更新后的目标数据
   * @returns {Promise<GoalItem>} 更新后的完整目标对象
   *
   * 请求方式：PUT /api/v1/goals/:id
   */
  update: (id: number, data: CreateGoalRequest) => request.put<any, GoalItem>(`/goals/${id}`, data),

  /**
   * delete — 删除指定的目标
   *
   * @param {number} id - 目标 ID
   * @returns {Promise<void>} 删除成功无返回数据
   *
   * 请求方式：DELETE /api/v1/goals/:id
   */
  delete: (id: number) => request.delete<any, void>(`/goals/${id}`),

  /**
   * updateProgress — 更新目标进度
   *
   * 独立于 update 的进度更新接口，适用于快速调整进度的场景。
   * 通过 URL 参数传递进度值，无需发送请求体。
   *
   * @param {number} id       - 目标 ID
   * @param {number} progress - 新的进度值（0-100）
   * @returns {Promise<void>} 更新成功无返回数据
   *
   * 请求方式：PUT /api/v1/goals/:id/progress?progress=50
   * 注意：第三个参数 { params: { progress } } 是 Axios 的 config 对象，
   * params 中的值会被序列化为 URL query string
   */
  updateProgress: (id: number, progress: number) =>
    request.put<any, void>(`/goals/${id}/progress`, null, { params: { progress } }),

  /**
   * getTree — 获取目标树形结构
   *
   * 返回包含父子层级关系的树形目标结构，前端可直接用于树形组件渲染。
   * 与 list 的区别：list 返回扁平列表，getTree 返回嵌套的树形结构。
   *
   * @param {string} [goalType] - 可选，按目标类型筛选
   * @returns {Promise<GoalItem[]>} 树形结构的目标列表（每个节点可能包含 children）
   *
   * 请求方式：GET /api/v1/goals/tree?goalType=xxx
   */
  getTree: (goalType?: string) =>
    request.get<any, GoalItem[]>('/goals/tree', { params: { goalType } }),

  /**
   * getStatistics — 获取目标统计数据
   *
   * 返回目标完成情况的汇总信息（如总数、完成数、进行中等）。
   *
   * @returns {Promise<Record<string, any>>} 目标统计数据
   *
   * 请求方式：GET /api/v1/goals/statistics
   */
  getStatistics: () => request.get<any, Record<string, any>>('/goals/statistics')
}

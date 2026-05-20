/**
 * ============================================================================
 * 【每日总结 API 模块（Summary API）】
 * ============================================================================
 *
 * 【模块用途】
 * 封装每日总结/日记相关的 HTTP 请求，包括总结的增删改查、
 * 连续打卡天数统计、心情趋势分析等功能。
 *
 * 【设计思想】
 * 1. 结构化日记：每天的总结不仅仅是文字，还包含心情评分、成就记录、
 *    改进计划、感恩事项等结构化字段，便于后续分析和回顾。
 * 2. 趋势分析：通过 mood-trend 接口提供心情和评分的历史趋势数据，
 *    用于绘制折线图，帮助用户了解自己的情绪变化规律。
 * 3. 打卡激励：streak 接口返回连续打卡天数，用于实现类似 GitHub
 *    贡献图的打卡激励功能，鼓励用户坚持每日总结。
 *
 * 【学习要点】
 * - 内联类型定义（getStreak 和 getMoodTrend 的返回类型直接在函数中定义）
 * - 默认参数值（getMoodTrend 的 days = 30）
 * - 联合类型（string | null）和可选属性（?）的使用
 * - 泛型数组的返回类型
 *
 * ============================================================================
 */

// 导入封装好的 Axios 请求实例
import request from './request'

// ============================================================================
// 【TypeScript 接口定义】
// ============================================================================

/**
 * SummaryItem — 每日总结的数据结构
 *
 * 描述一条完整的每日总结记录，包含日期、心情、评分、以及各个维度的文字内容。
 *
 * @interface SummaryItem
 */
export interface SummaryItem {
  id: number                  // 总结记录唯一标识 ID
  summaryDate: string         // 总结日期（格式：'YYYY-MM-DD'）
  mood: number                // 心情评分（1-5，1=很差，5=很好）
  score: number               // 综合评分（0-100，对当天整体表现的自评）
  achievement: string | null  // 今日成就（记录今天完成的有价值的事情）
  improvement: string | null  // 改进空间（反思今天做得不好的地方）
  tomorrowPlan: string | null // 明日计划（对明天的简要规划）
  gratitude: string | null    // 感恩事项（记录今天值得感恩的事情）
  healthNote: string | null   // 健康记录（如运动、饮食、睡眠等）
  freeWriting: string | null  // 自由书写（不限格式的日记内容）
  tags: string | null         // 标签（逗号分隔的标签字符串）
  createdAt: string           // 创建时间
}

/**
 * CreateSummaryRequest — 创建/更新总结的请求数据结构
 *
 * 与 SummaryItem 的区别：
 * - 没有 id、createdAt 等自动生成字段
 * - 所有内容字段都是可选的（用户可以只填写部分内容）
 * - gratitude 和 tags 在请求时是 string[] 数组，后端存储时转为字符串
 *
 * @interface CreateSummaryRequest
 */
export interface CreateSummaryRequest {
  summaryDate: string       // 总结日期（必填，格式：'YYYY-MM-DD'）
  mood?: number             // 心情评分（可选，1-5）
  score?: number            // 综合评分（可选，0-100）
  achievement?: string      // 今日成就（可选）
  improvement?: string      // 改进空间（可选）
  tomorrowPlan?: string     // 明日计划（可选）
  gratitude?: string[]      // 感恩事项列表（可选，数组形式）
  healthNote?: string       // 健康记录（可选）
  freeWriting?: string      // 自由书写（可选）
  tags?: string[]           // 标签列表（可选，数组形式）
}

// ============================================================================
// 【API 方法定义】
// ============================================================================

/**
 * summaryApi — 每日总结相关的 API 方法集合
 *
 * 提供总结的 CRUD 操作、今日总结查询、连续打卡统计、心情趋势分析等功能。
 */
export const summaryApi = {
  /**
   * list — 分页查询总结列表
   *
   * @param {Object} params - 查询参数
   * @param {number} [params.pageNum]   - 页码
   * @param {number} [params.pageSize]  - 每页条数
   * @param {string} [params.startDate] - 起始日期筛选
   * @param {string} [params.endDate]   - 截止日期筛选
   * @returns {Promise<SummaryItem[]>} 总结列表
   *
   * 请求方式：GET /api/v1/summaries
   */
  list: (params: { pageNum?: number; pageSize?: number; startDate?: string; endDate?: string }) =>
    request.get<any, SummaryItem[]>('/summaries', { params }),

  /**
   * create — 创建新的每日总结
   *
   * @param {CreateSummaryRequest} data - 总结数据
   * @returns {Promise<SummaryItem>} 创建成功后返回完整的总结记录
   *
   * 请求方式：POST /api/v1/summaries
   */
  create: (data: CreateSummaryRequest) => request.post<any, SummaryItem>('/summaries', data),

  /**
   * update — 更新指定的总结记录
   *
   * @param {number} id                   - 总结记录 ID
   * @param {CreateSummaryRequest} data   - 更新后的总结数据
   * @returns {Promise<SummaryItem>} 更新后的完整记录
   *
   * 请求方式：PUT /api/v1/summaries/:id
   */
  update: (id: number, data: CreateSummaryRequest) =>
    request.put<any, SummaryItem>(`/summaries/${id}`, data),

  /**
   * delete — 删除指定的总结记录
   *
   * @param {number} id - 总结记录 ID
   * @returns {Promise<void>} 删除成功无返回数据
   *
   * 请求方式：DELETE /api/v1/summaries/:id
   */
  delete: (id: number) => request.delete<any, void>(`/summaries/${id}`),

  /**
   * getToday — 获取今日的总结记录
   *
   * 如果今天已经写过总结，返回总结数据；否则返回 null。
   * 用于首页判断今天是否已写总结，决定显示"查看"还是"新建"按钮。
   *
   * @returns {Promise<SummaryItem | null>} 今日总结数据，未写则返回 null
   *
   * 请求方式：GET /api/v1/summaries/today
   */
  getToday: () => request.get<any, SummaryItem | null>('/summaries/today'),

  /**
   * getStreak — 获取连续打卡统计数据
   *
   * 返回当前连续打卡天数、历史最长连续天数、以及总打卡天数。
   * 用于在页面上展示打卡成就和激励用户坚持。
   *
   * @returns {Promise<{ currentStreak: number; longestStreak: number; totalDays: number }>}
   *   - currentStreak：当前连续打卡天数
   *   - longestStreak：历史最长连续打卡天数
   *   - totalDays：累计打卡总天数
   *
   * 请求方式：GET /api/v1/summaries/streak
   *
   * 注意：返回类型直接在函数签名中定义，而不是单独声明一个 interface。
   * 这种方式适合只在一个地方使用的简单类型。
   */
  getStreak: () =>
    request.get<any, { currentStreak: number; longestStreak: number; totalDays: number }>(
      '/summaries/streak'
    ),

  /**
   * getMoodTrend — 获取心情趋势数据
   *
   * 返回最近 N 天的心情和评分数据，用于绘制折线图。
   *
   * @param {number} [days=30] - 查询天数，默认 30 天
   *   使用 ES6 默认参数语法：如果调用时不传参，默认值为 30
   *   例如：getMoodTrend() 查询最近 30 天，getMoodTrend(7) 查询最近 7 天
   * @returns {Promise<Array<{ date: string; mood: number; score: number }>>}
   *   心情趋势数据数组，每个元素包含：
   *   - date：日期字符串
   *   - mood：心情评分（1-5）
   *   - score：综合评分（0-100）
   *
   * 请求方式：GET /api/v1/summaries/mood-trend?days=30
   */
  getMoodTrend: (days = 30) =>
    request.get<any, Array<{ date: string; mood: number; score: number }>>(
      '/summaries/mood-trend',
      {
        params: { days }
      }
    )
}

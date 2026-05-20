/**
 * ============================================================================
 * 【仪表盘 API 模块（Dashboard API）】
 * ============================================================================
 *
 * 【模块用途】
 * 封装仪表盘（数据概览）相关的 HTTP 请求，提供不同时间维度的汇总数据。
 * 仪表盘是用户进入应用后看到的第一个页面，展示今日/本周/本月/本年的关键数据摘要。
 *
 * 【设计思想】
 * 1. 时间维度抽象：将统计数据按时间粒度划分为 today（今日）、week（本周）、
 *    month（本月）、year（本年）四个维度，每个维度对应一个独立的 API 接口。
 * 2. 趋势数据支持：通过 trend 接口提供自定义时间范围的趋势数据，
 *    用于绘制折线图等可视化图表。
 * 3. 灵活的数据结构：使用 Record<string, any> 作为响应类型，
 *    因为仪表盘数据结构可能随需求频繁变化，不需要严格约束。
 *
 * 【学习要点】
 * - 无参数的 GET 请求写法（使用箭头函数隐式返回）
 * - Record<string, any> 类型的使用场景
 * - Axios params 传参与 URL 拼接的区别
 *
 * ============================================================================
 */

// 导入封装好的 Axios 请求实例
import request from './request'

/**
 * dashboardApi — 仪表盘相关的 API 方法集合
 *
 * 提供不同时间维度的数据汇总查询，以及自定义时间范围的趋势数据。
 *
 * 使用方式：
 * ```ts
 * import { dashboardApi } from '@/api/dashboard'
 *
 * // 获取今日数据
 * const todayData = await dashboardApi.getToday()
 *
 * // 获取指定时间范围的趋势数据
 * const trendData = await dashboardApi.getTrend('2024-01-01', '2024-01-31')
 * ```
 */
export const dashboardApi = {
  /**
   * getToday — 获取今日数据概览
   *
   * 返回今天的计划完成情况、收支摘要、摘录数量、总结等汇总信息。
   *
   * @returns {Promise<Record<string, any>>} 今日数据概览（动态结构，包含多个模块的数据）
   *
   * 请求方式：GET /api/v1/dashboard/today
   */
  getToday: () => request.get<any, Record<string, any>>('/dashboard/today'),

  /**
   * getWeek — 获取本周数据概览
   *
   * 返回本周（周一至周日）的汇总数据。
   *
   * @returns {Promise<Record<string, any>>} 本周数据概览
   *
   * 请求方式：GET /api/v1/dashboard/week
   */
  getWeek: () => request.get<any, Record<string, any>>('/dashboard/week'),

  /**
   * getMonth — 获取本月数据概览
   *
   * 返回本月（当月1日至月末）的汇总数据。
   *
   * @returns {Promise<Record<string, any>>} 本月数据概览
   *
   * 请求方式：GET /api/v1/dashboard/month
   */
  getMonth: () => request.get<any, Record<string, any>>('/dashboard/month'),

  /**
   * getYear — 获取本年数据概览
   *
   * 返回今年的年度汇总数据。
   *
   * @returns {Promise<Record<string, any>>} 本年数据概览
   *
   * 请求方式：GET /api/v1/dashboard/year
   */
  getYear: () => request.get<any, Record<string, any>>('/dashboard/year'),

  /**
   * getTrend — 获取自定义时间范围的趋势数据
   *
   * 根据起止日期查询趋势数据，通常用于绘制折线图、柱状图等数据可视化图表。
   *
   * @param {string} startDate - 起始日期（格式：'YYYY-MM-DD'）
   * @param {string} endDate   - 截止日期（格式：'YYYY-MM-DD'）
   * @returns {Promise<Record<string, any>>} 趋势数据（通常包含日期序列和对应的数值）
   *
   * 请求方式：GET /api/v1/dashboard/trend?startDate=xxx&endDate=xxx
   *
   * 注意：params 中的参数会自动序列化为 URL query string，
   * 例如 { startDate: '2024-01-01', endDate: '2024-01-31' }
   * 会被转换为 ?startDate=2024-01-01&endDate=2024-01-31
   */
  getTrend: (startDate: string, endDate: string) =>
    request.get<any, Record<string, any>>('/dashboard/trend', { params: { startDate, endDate } })
}

/**
 * 仪表盘相关 API
 */
import http from '@/utils/request'

export const dashboardApi = {
  /** 获取今日概览数据 */
  getToday: () => http.get<Record<string, any>>('/dashboard/today'),

  /** 获取本周数据 */
  getWeek: () => http.get<Record<string, any>>('/dashboard/week'),

  /** 获取本月数据 */
  getMonth: () => http.get<Record<string, any>>('/dashboard/month'),

  /** 获取趋势数据 */
  getTrend: (startDate: string, endDate: string) =>
    http.get<Record<string, any>>('/dashboard/trend', { params: { startDate, endDate } })
}

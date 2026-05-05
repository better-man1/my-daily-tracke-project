import request from './request'

export const dashboardApi = {
  getToday: () => request.get<any, Record<string, any>>('/dashboard/today'),
  getWeek: () => request.get<any, Record<string, any>>('/dashboard/week'),
  getMonth: () => request.get<any, Record<string, any>>('/dashboard/month'),
  getYear: () => request.get<any, Record<string, any>>('/dashboard/year'),
  getTrend: (startDate: string, endDate: string, type?: string) =>
    request.get<any, Record<string, any>>('/dashboard/trend', {
      params: { startDate, endDate, type }
    })
}

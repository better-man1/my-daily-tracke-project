import request from './request'

export const dashboardApi = {
  getToday: () => request.get<any, Record<string, any>>('/dashboard/today'),
  getWeek: () => request.get<any, Record<string, any>>('/dashboard/week'),
  getMonth: () => request.get<any, Record<string, any>>('/dashboard/month'),
  getYear: () => request.get<any, Record<string, any>>('/dashboard/year')
}

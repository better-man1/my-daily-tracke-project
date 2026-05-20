import request from './request'

export const dashboardApi = {
  getToday: () =>
    request.get<any, any>('/dashboard/today'),

  getWeek: () =>
    request.get<any, any>('/dashboard/week'),

  getMonth: () =>
    request.get<any, any>('/dashboard/month'),

  getYear: () =>
    request.get<any, any>('/dashboard/year'),

  getTrend: (startDate: string, endDate: string) =>
    request.get<any, any>('/dashboard/trend', {
      params: { startDate, endDate }
    })
}
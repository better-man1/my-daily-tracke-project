import request from './request'

// ============================================================================
// 【TypeScript 接口定义】
// ============================================================================

export interface AccountingItem {
  id: number
  userId: number
  type: 'INCOME' | 'EXPENSE'
  amount: number
  category: string
  date: string
  description: string | null
  createdAt: string
  updatedAt: string
}

export interface CreateAccountingRequest {
  type: 'INCOME' | 'EXPENSE'
  amount: number
  category: string
  date: string
  description?: string
}

export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
}

export interface AccountingStatistics {
  totalIncome: number
  totalExpense: number
  balance: number
  incomeByCategory: Record<string, number>
  expenseByCategory: Record<string, number>
  dailyTrend: Array<{ date: string; income: number; expense: number }>
}

export const accountingApi = {
  // 分页查询
  list: (params?: {
    page?: number
    pageSize?: number
    type?: 'INCOME' | 'EXPENSE'
    category?: string
    startDate?: string
    endDate?: string
    keyword?: string
  }) => request.get<any, PageResult<AccountingItem>>('/accounting', { params }),

  // 创建记录
  create: (data: CreateAccountingRequest) =>
    request.post<any, AccountingItem>('/accounting', data),

  // 更新记录
  update: (id: number, data: Partial<CreateAccountingRequest>) =>
    request.put<any, AccountingItem>(`/accounting/${id}`, data),

  // 删除记录
  delete: (id: number) =>
    request.delete<any, void>(`/accounting/${id}`),

  // 获取分类列表
  getCategories: (type?: 'INCOME' | 'EXPENSE') =>
    request.get<any, string[]>('/accounting/categories', { params: { type } }),

  // 获取统计数据
  getStatistics: (startDate: string, endDate: string) =>
    request.get<any, AccountingStatistics>('/accounting/statistics', {
      params: { startDate, endDate }
    }),

  // 获取月度统计
  getMonthlyStatistics: (year: number, month: number) =>
    request.get<any, AccountingStatistics>('/accounting/statistics/monthly', {
      params: { year, month }
    })
}

import request from './request'

export interface AccountingItem {
  id: number
  type: 'INCOME' | 'EXPENSE'
  amount: number
  categoryId: number
  categoryName: string
  parentCategoryName: string | null
  accountType: string
  remark: string | null
  images: string | null
  accountingDate: string
  accountingTime: string | null
  createdAt: string
}

export interface CreateAccountingRequest {
  type: 'INCOME' | 'EXPENSE'
  amount: number
  categoryId: number
  accountType?: string
  remark?: string
  images?: string[]
  accountingDate: string
  accountingTime?: string
}

export interface AccountingStats {
  startDate: string
  endDate: string
  totalIncome: number
  totalExpense: number
  balance: number
  categoryStats?: Array<{
    categoryId: number
    categoryName: string
    type: string
    amount: number
    percentage: number
  }>
}

export interface PageResult<T> {
  records: T[]
  total: number
  pageNum: number
  pageSize: number
  pages: number
}

export const accountingApi = {
  page: (params: {
    pageNum?: number
    pageSize?: number
    startDate?: string
    endDate?: string
    type?: string
  }) => request.get<any, PageResult<AccountingItem>>('/accounting', { params }),

  create: (data: CreateAccountingRequest) =>
    request.post<any, AccountingItem>('/accounting', data),

  update: (id: number, data: CreateAccountingRequest) =>
    request.put<any, AccountingItem>(`/accounting/${id}`, data),

  delete: (id: number) =>
    request.delete<any, void>(`/accounting/${id}`),

  dailyStats: (date?: string) =>
    request.get<any, AccountingStats>('/accounting/statistics/daily', { params: { date } }),

  monthlyStats: (year: number, month: number) =>
    request.get<any, AccountingStats>('/accounting/statistics/monthly', { params: { year, month } }),

  yearlyStats: (year: number) =>
    request.get<any, AccountingStats>('/accounting/statistics/yearly', { params: { year } }),

  categoryStats: (params: { startDate?: string; endDate?: string; type?: string }) =>
    request.get<any, AccountingStats['categoryStats']>('/accounting/statistics/category', { params }),

  setBudget: (data: { categoryId?: number; budgetYear: number; budgetMonth: number; amount: number }) =>
    request.post<any, void>('/accounting/budget', data),

  getBudget: (year: number, month: number) =>
    request.get<any, Record<string, any>>('/accounting/budget', { params: { year, month } }),

  getCategories: (type?: string) =>
    request.get<any, any[]>('/accounting/categories', { params: { type } })
}

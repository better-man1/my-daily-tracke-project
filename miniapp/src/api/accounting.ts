/**
 * 记账管理相关 API
 */
import http from '@/utils/request'

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
  /** 分页查询记账记录 */
  page: (params: {
    pageNum?: number
    pageSize?: number
    startDate?: string
    endDate?: string
    type?: string
  }) => http.get<PageResult<AccountingItem>>('/accounting', { params }),

  /** 创建记账记录 */
  create: (data: CreateAccountingRequest) =>
    http.post<AccountingItem>('/accounting', data),

  /** 更新记账记录 */
  update: (id: number, data: CreateAccountingRequest) =>
    http.put<AccountingItem>(`/accounting/${id}`, data),

  /** 删除记账记录 */
  delete: (id: number) =>
    http.delete<void>(`/accounting/${id}`),

  /** 获取日统计 */
  dailyStats: (date?: string) =>
    http.get<AccountingStats>('/accounting/statistics/daily', { params: { date } }),

  /** 获取月统计 */
  monthlyStats: (year: number, month: number) =>
    http.get<AccountingStats>('/accounting/statistics/monthly', { params: { year, month } }),

  /** 获取分类列表 */
  getCategories: (type?: string) =>
    http.get<any[]>('/accounting/categories', { params: { type } })
}

/**
 * ============================================================================
 * 【记账 API 模块（Accounting API）】
 * ============================================================================
 *
 * 【模块用途】
 * 封装记账功能相关的所有 HTTP 请求，包括记账记录的增删改查、
 * 分类统计（日/月/年/分类维度）、预算管理等。
 *
 * 【设计思想】
 * 1. 领域模型映射：每个 TypeScript 接口都对应后端的一个数据实体，
 *    前端通过这些接口了解后端返回的数据结构，无需查看后端代码。
 * 2. 泛型分页封装：PageResult<T> 是通用的分页响应结构，T 表示列表项的类型，
 *    这样不同模块的分页查询（记账、摘录等）可以复用同一套分页结构定义。
 * 3. RESTful API 风格：
 *    - GET    /accounting              — 查询列表
 *    - POST   /accounting              — 新建记录
 *    - PUT    /accounting/:id          — 更新记录
 *    - DELETE /accounting/:id          — 删除记录
 *    - GET    /accounting/statistics/* — 各维度统计
 *
 * 【学习要点】
 * - TypeScript 字面量联合类型（'INCOME' | 'EXPENSE'）
 * - TypeScript 泛型接口（PageResult<T>）
 * - 可选属性（?）和可空类型（| null）的区别
 * - Axios GET 请求的 params 传参方式
 *
 * ============================================================================
 */

// 导入封装好的 Axios 请求实例
import request from './request'

// ============================================================================
// 【TypeScript 接口定义】
// ============================================================================

/**
 * AccountingItem — 记账记录的数据结构（对应后端的记账实体）
 *
 * 每条记账记录包含收入/支出的完整信息，包括金额、分类、日期、备注等。
 * 这个接口描述的是从后端获取的完整数据（包含 id、createdAt 等自动生成的字段）。
 *
 * @interface AccountingItem
 */
export interface AccountingItem {
  id: number                         // 记录唯一标识 ID
  type: 'INCOME' | 'EXPENSE'         // 记录类型：'INCOME'（收入）或 'EXPENSE'（支出）
                                     // 使用字面量联合类型，限定只能取这两个值
  amount: number                     // 金额（单位：元）
  categoryId: number                 // 分类 ID（关联分类表）
  categoryName: string               // 分类名称（后端已做 JOIN 查询，直接返回名称）
  parentCategoryName: string | null  // 父级分类名称，可能为 null（一级分类没有父级）
  accountType: string                // 账户类型（如：现金、银行卡、微信、支付宝等）
  remark: string | null              // 备注信息，可能为 null（未填写备注）
  images: string | null              // 图片 URL（多张图片可能以逗号分隔），可能为 null
  accountingDate: string             // 记账日期（格式：'YYYY-MM-DD'）
  accountingTime: string | null      // 记账时间（格式：'HH:mm:ss'），可能为 null
  createdAt: string                  // 记录创建时间（ISO 8601 格式字符串）
}

/**
 * CreateAccountingRequest — 创建/更新记账记录的请求数据结构
 *
 * 与 AccountingItem 的区别：
 * - 没有 id、createdAt 等自动生成的字段
 * - 大部分字段是可选的（?），因为更新时可能只修改部分字段
 * - images 是 string[] 数组类型（前端上传多张图片），
 *   而后端返回的 AccountingItem.images 是 string（逗号分隔的 URL 字符串）
 *
 * @interface CreateAccountingRequest
 */
export interface CreateAccountingRequest {
  type: 'INCOME' | 'EXPENSE'  // 记录类型：必填
  amount: number               // 金额：必填
  categoryId: number           // 分类 ID：必填
  accountType?: string         // 账户类型：可选，默认可能为 "现金"
  remark?: string              // 备注：可选
  images?: string[]            // 图片 URL 数组：可选
  accountingDate: string       // 记账日期：必填（格式：'YYYY-MM-DD'）
  accountingTime?: string      // 记账时间：可选（格式：'HH:mm'）
}

/**
 * AccountingStats — 记账统计数据结构
 *
 * 用于日/月/年维度的收支统计，包含总收入、总支出、余额，
 * 以及可选的分类维度统计（各类别的金额和占比）。
 *
 * @interface AccountingStats
 */
export interface AccountingStats {
  startDate: string                   // 统计起始日期
  endDate: string                     // 统计截止日期
  totalIncome: number                 // 总收入金额
  totalExpense: number                // 总支出金额
  balance: number                     // 余额（totalIncome - totalExpense）
  categoryStats?: Array<{             // 分类维度统计（可选）
    categoryId: number                // 分类 ID
    categoryName: string              // 分类名称
    type: string                      // 类型（'INCOME' 或 'EXPENSE'）
    amount: number                    // 该分类的总金额
    percentage: number                // 该分类占总金额的百分比（0-100）
  }>
}

/**
 * PageResult<T> — 通用分页响应结构（泛型接口）
 *
 * 这是整个项目中分页查询的统一返回格式，支持泛型参数 T。
 * T 表示列表中每条数据的类型，如 PageResult<AccountingItem>、PageResult<ExcerptItem> 等。
 *
 * 为什么使用泛型？
 * - 不同模块的分页查询返回不同的数据类型
 * - 通过泛型可以在不重复定义分页结构的情况下保证类型安全
 * - 例如：PageResult<AccountingItem> 的 records 就是 AccountingItem[] 类型
 *
 * @interface PageResult<T>
 * @template T - 列表项的数据类型
 */
export interface PageResult<T> {
  records: T[]      // 当前页的数据列表
  total: number     // 总记录数（用于计算总页数）
  pageNum: number   // 当前页码（从 1 开始）
  pageSize: number  // 每页显示条数
  pages: number     // 总页数（total / pageSize 向上取整）
}

// ============================================================================
// 【API 方法定义】
// ============================================================================

/**
 * accountingApi — 记账相关的 API 方法集合
 *
 * 提供记账记录的 CRUD（增删改查）操作和多维度统计查询。
 */
export const accountingApi = {
  /**
   * page — 分页查询记账记录
   *
   * 支持按日期范围、类型筛选，返回分页结果。
   *
   * @param {Object} params - 查询参数
   * @param {number} [params.pageNum]   - 页码，默认 1
   * @param {number} [params.pageSize]  - 每页条数，默认 10
   * @param {string} [params.startDate] - 起始日期筛选（格式：'YYYY-MM-DD'）
   * @param {string} [params.endDate]   - 截止日期筛选（格式：'YYYY-MM-DD'）
   * @param {string} [params.type]      - 类型筛选（'INCOME' 或 'EXPENSE'）
   * @returns {Promise<PageResult<AccountingItem>>} 分页记账记录
   *
   * 请求方式：GET /api/v1/accounting
   * 参数通过 URL query string 传递（Axios 的 params 选项会自动序列化）
   */
  page: (params: {
    pageNum?: number
    pageSize?: number
    startDate?: string
    endDate?: string
    type?: string
  }) => request.get<any, PageResult<AccountingItem>>('/accounting', { params }),

  /**
   * create — 创建新的记账记录
   *
   * @param {CreateAccountingRequest} data - 记账数据
   * @returns {Promise<AccountingItem>} 创建成功后返回完整的记账记录（包含自动生成的 id 等）
   *
   * 请求方式：POST /api/v1/accounting
   */
  create: (data: CreateAccountingRequest) => request.post<any, AccountingItem>('/accounting', data),

  /**
   * update — 更新指定的记账记录
   *
   * @param {number} id - 要更新的记录 ID
   * @param {CreateAccountingRequest} data - 更新后的记账数据
   * @returns {Promise<AccountingItem>} 更新后的完整记录
   *
   * 请求方式：PUT /api/v1/accounting/:id
   * 使用模板字符串 `/accounting/${id}` 动态拼接 ID 到 URL 路径中
   */
  update: (id: number, data: CreateAccountingRequest) =>
    request.put<any, AccountingItem>(`/accounting/${id}`, data),

  /**
   * delete — 删除指定的记账记录
   *
   * @param {number} id - 要删除的记录 ID
   * @returns {Promise<void>} 删除成功无返回数据
   *
   * 请求方式：DELETE /api/v1/accounting/:id
   */
  delete: (id: number) => request.delete<any, void>(`/accounting/${id}`),

  /**
   * dailyStats — 获取某天的收支统计
   *
   * @param {string} [date] - 日期（格式：'YYYY-MM-DD'），不传则默认查今天
   * @returns {Promise<AccountingStats>} 当天的收支统计数据
   *
   * 请求方式：GET /api/v1/accounting/statistics/daily?date=xxx
   */
  dailyStats: (date?: string) =>
    request.get<any, AccountingStats>('/accounting/statistics/daily', { params: { date } }),

  /**
   * monthlyStats — 获取某月的收支统计
   *
   * @param {number} year  - 年份（如 2024）
   * @param {number} month - 月份（1-12）
   * @returns {Promise<AccountingStats>} 当月的收支统计数据
   *
   * 请求方式：GET /api/v1/accounting/statistics/monthly?year=xxx&month=xxx
   */
  monthlyStats: (year: number, month: number) =>
    request.get<any, AccountingStats>('/accounting/statistics/monthly', {
      params: { year, month }
    }),

  /**
   * yearlyStats — 获取某年的收支统计
   *
   * @param {number} year - 年份（如 2024）
   * @returns {Promise<AccountingStats>} 当年的收支统计数据
   *
   * 请求方式：GET /api/v1/accounting/statistics/yearly?year=xxx
   */
  yearlyStats: (year: number) =>
    request.get<any, AccountingStats>('/accounting/statistics/yearly', { params: { year } }),

  /**
   * categoryStats — 获取分类维度的统计数据
   *
   * 返回各分类的收入/支出金额和占比，用于绘制饼图等可视化图表。
   * 使用 AccountingStats['categoryStats'] 类型访问嵌套的类型定义。
   *
   * @param {Object} params - 查询参数
   * @param {string} [params.startDate] - 起始日期
   * @param {string} [params.endDate]   - 截止日期
   * @param {string} [params.type]      - 类型筛选
   * @returns {Promise<AccountingStats['categoryStats']>} 分类统计数据
   *
   * 请求方式：GET /api/v1/accounting/statistics/category
   */
  categoryStats: (params: { startDate?: string; endDate?: string; type?: string }) =>
    request.get<any, AccountingStats['categoryStats']>('/accounting/statistics/category', {
      params
    }),

  /**
   * setBudget — 设置月度预算
   *
   * 可以设置总预算或某个分类的预算。
   *
   * @param {Object} data - 预算数据
   * @param {number} [data.categoryId]  - 分类 ID（不传则设置总预算）
   * @param {number} data.budgetYear    - 预算年份
   * @param {number} data.budgetMonth   - 预算月份
   * @param {number} data.amount        - 预算金额
   * @returns {Promise<void>} 设置成功无返回数据
   *
   * 请求方式：POST /api/v1/accounting/budget
   */
  setBudget: (data: {
    categoryId?: number
    budgetYear: number
    budgetMonth: number
    amount: number
  }) => request.post<any, void>('/accounting/budget', data),

  /**
   * getBudget — 获取指定月份的预算设置
   *
   * @param {number} year  - 年份
   * @param {number} month - 月份
   * @returns {Promise<Record<string, any>>} 预算数据（键值对结构）
   *
   * 请求方式：GET /api/v1/accounting/budget?year=xxx&month=xxx
   */
  getBudget: (year: number, month: number) =>
    request.get<any, Record<string, any>>('/accounting/budget', { params: { year, month } }),

  /**
   * getCategories — 获取记账分类列表
   *
   * 返回所有可用的收支分类，用于前端表单中的分类选择器。
   *
   * @param {string} [type] - 类型筛选（'INCOME' 或 'EXPENSE'），不传则返回所有分类
   * @returns {Promise<any[]>} 分类列表
   *
   * 请求方式：GET /api/v1/accounting/categories?type=xxx
   */
  getCategories: (type?: string) =>
    request.get<any, any[]>('/accounting/categories', { params: { type } })
}

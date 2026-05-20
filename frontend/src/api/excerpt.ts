/**
 * ============================================================================
 * 【摘录 API 模块（Excerpt API）】
 * ============================================================================
 *
 * 【模块用途】
 * 封装摘录/读书笔记相关的 HTTP 请求，包括摘录的增删改查、
 * 分页查询、收藏管理、标签管理、搜索、随机摘录、Markdown 导出等功能。
 *
 * 【设计思想】
 * 1. 知识收集：支持从多种来源（书籍、文章、网页等）收集摘录内容，
 *    并附加个人思考和标签分类。
 * 2. 标签体系：每条摘录可以关联多个标签，支持按标签筛选和搜索。
 * 3. 收藏机制：toggleFavorite 实现收藏/取消收藏的切换，
 *    使用 PUT 而非 POST 语义上表示"更新收藏状态"。
 * 4. 随机回顾：getRandom 接口随机返回一条摘录，用于日常知识回顾。
 * 5. 模块复用：从 accounting.ts 导入 PageResult 泛型接口，复用分页结构。
 *
 * 【学习要点】
 * - 跨模块导入和复用 TypeScript 接口（import type { PageResult } from './accounting'）
 * - 嵌套对象类型（tags: Array<{ id: number; name: string; color: string }>）
 * - PUT 请求用于状态切换（toggleFavorite）
 * - 搜索功能的 API 设计
 *
 * ============================================================================
 */

// 导入封装好的 Axios 请求实例
import request from './request'

// 从 accounting 模块导入通用的分页结果类型
// import type 表示只导入类型信息，不会生成运行时代码（Tree-shaking 友好）
import type { PageResult } from './accounting'

// ============================================================================
// 【TypeScript 接口定义】
// ============================================================================

/**
 * ExcerptItem — 摘录记录的数据结构
 *
 * 描述一条完整的摘录记录，包含内容、来源、个人思考、标签等信息。
 *
 * @interface ExcerptItem
 */
export interface ExcerptItem {
  id: number                  // 摘录记录唯一标识 ID
  content: string             // 摘录内容（核心字段，原文摘录的文字）
  sourceType: string          // 来源类型（如：book-书籍、article-文章、video-视频等）
  sourceTitle: string | null  // 来源标题（如书名、文章标题）
  sourceUrl: string | null    // 来源 URL（如果是网页摘录）
  thought: string | null      // 个人思考/读后感（对摘录内容的感想和笔记）
  images: string | null       // 配图 URL（可能有多张，逗号分隔）
  isFavorite: number          // 是否收藏（0=未收藏，1=已收藏）
                              // 注意：使用 number 而非 boolean，因为后端数据库通常用 TINYINT(1) 存储
  excerptDate: string         // 摘录日期（格式：'YYYY-MM-DD'）
  tags: Array<{               // 关联的标签列表（后端通过 JOIN 查询返回完整标签信息）
    id: number                // 标签 ID
    name: string              // 标签名称
    color: string             // 标签颜色（十六进制颜色值，如 '#FF5733'）
  }>
  createdAt: string           // 创建时间
}

/**
 * CreateExcerptRequest — 创建/更新摘录的请求数据结构
 *
 * @interface CreateExcerptRequest
 */
export interface CreateExcerptRequest {
  content: string          // 摘录内容（必填）
  sourceType?: string      // 来源类型（可选）
  sourceTitle?: string     // 来源标题（可选）
  sourceUrl?: string       // 来源 URL（可选）
  thought?: string         // 个人思考（可选）
  images?: string[]        // 配图 URL 数组（可选）
  excerptDate: string      // 摘录日期（必填）
  tagIds?: number[]        // 要关联的标签 ID 列表（可选）
  isFavorite?: number      // 是否收藏（可选，0 或 1）
}

// ============================================================================
// 【API 方法定义】
// ============================================================================

/**
 * excerptApi — 摘录相关的 API 方法集合
 *
 * 提供摘录的 CRUD、分页查询、收藏、标签管理、搜索、导出等功能。
 */
export const excerptApi = {
  /**
   * page — 分页查询摘录列表
   *
   * 支持按日期范围、来源类型、标签、收藏状态筛选。
   *
   * @param {Object} params - 查询参数
   * @param {number} [params.pageNum]    - 页码
   * @param {number} [params.pageSize]   - 每页条数
   * @param {string} [params.startDate]  - 起始日期筛选
   * @param {string} [params.endDate]    - 截止日期筛选
   * @param {string} [params.sourceType] - 来源类型筛选
   * @param {number} [params.tagId]      - 标签 ID 筛选
   * @param {number} [params.isFavorite] - 收藏状态筛选（0 或 1）
   * @returns {Promise<PageResult<ExcerptItem>>} 分页摘录列表
   *
   * 请求方式：GET /api/v1/excerpts
   *
   * 注意：PageResult<ExcerptItem> 是从 accounting.ts 导入的泛型接口，
   * 体现了 TypeScript 泛型的代码复用能力。
   */
  page: (params: {
    pageNum?: number
    pageSize?: number
    startDate?: string
    endDate?: string
    sourceType?: string
    tagId?: number
    isFavorite?: number
  }) => request.get<any, PageResult<ExcerptItem>>('/excerpts', { params }),

  /**
   * create — 创建新的摘录记录
   *
   * @param {CreateExcerptRequest} data - 摘录数据
   * @returns {Promise<ExcerptItem>} 创建成功后返回完整的摘录记录
   *
   * 请求方式：POST /api/v1/excerpts
   */
  create: (data: CreateExcerptRequest) => request.post<any, ExcerptItem>('/excerpts', data),

  /**
   * update — 更新指定的摘录记录
   *
   * @param {number} id                    - 摘录记录 ID
   * @param {CreateExcerptRequest} data    - 更新后的摘录数据
   * @returns {Promise<ExcerptItem>} 更新后的完整记录
   *
   * 请求方式：PUT /api/v1/excerpts/:id
   */
  update: (id: number, data: CreateExcerptRequest) =>
    request.put<any, ExcerptItem>(`/excerpts/${id}`, data),

  /**
   * delete — 删除指定的摘录记录
   *
   * @param {number} id - 摘录记录 ID
   * @returns {Promise<void>} 删除成功无返回数据
   *
   * 请求方式：DELETE /api/v1/excerpts/:id
   */
  delete: (id: number) => request.delete<any, void>(`/excerpts/${id}`),

  /**
   * toggleFavorite — 切换摘录的收藏状态
   *
   * 如果当前是已收藏状态，则取消收藏；反之则添加收藏。
   * 使用 PUT 方法，语义上是"更新收藏状态"。
   *
   * @param {number} id - 摘录记录 ID
   * @returns {Promise<void>} 切换成功无返回数据
   *
   * 请求方式：PUT /api/v1/excerpts/:id/favorite
   */
  toggleFavorite: (id: number) => request.put<any, void>(`/excerpts/${id}/favorite`),

  /**
   * getRandom — 随机获取一条摘录
   *
   * 每次调用返回数据库中随机的一条摘录记录，用于日常知识回顾。
   * 类似"每日一句"功能。
   *
   * @returns {Promise<ExcerptItem>} 随机摘录记录
   *
   * 请求方式：GET /api/v1/excerpts/random
   */
  getRandom: () => request.get<any, ExcerptItem>('/excerpts/random'),

  /**
   * getAllTags — 获取所有摘录标签
   *
   * 返回用户创建的所有标签，用于标签选择器下拉框。
   *
   * @returns {Promise<any[]>} 标签列表
   *
   * 请求方式：GET /api/v1/excerpts/tags
   */
  getAllTags: () => request.get<any, any[]>('/excerpts/tags'),

  /**
   * createTag — 创建新的摘录标签
   *
   * 通过 URL 参数传递标签名称和颜色（而非请求体），
   * 因为数据量很小，直接放在 URL 参数中更简洁。
   *
   * @param {string} name     - 标签名称
   * @param {string} [color]  - 标签颜色（可选，十六进制颜色值）
   * @returns {Promise<any>} 创建的标签对象
   *
   * 请求方式：POST /api/v1/excerpts/tags?name=xxx&color=xxx
   * 注意：第二个参数为 null（没有请求体），第三个参数是 config
   */
  createTag: (name: string, color?: string) =>
    request.post<any, any>('/excerpts/tags', null, { params: { name, color } }),

  /**
   * search — 按关键词搜索摘录
   *
   * 全文搜索摘录内容和标题，支持分页。
   *
   * @param {string} keyword           - 搜索关键词
   * @param {number} [pageNum=1]       - 页码，默认第 1 页
   * @param {number} [pageSize=20]     - 每页条数，默认 20 条
   * @returns {Promise<PageResult<ExcerptItem>>} 搜索结果的分页列表
   *
   * 请求方式：GET /api/v1/excerpts/search?keyword=xxx&pageNum=1&pageSize=20
   */
  search: (keyword: string, pageNum = 1, pageSize = 20) =>
    request.get<any, PageResult<ExcerptItem>>('/excerpts/search', {
      params: { keyword, pageNum, pageSize }
    }),

  /**
   * exportMarkdown — 导出所有摘录为 Markdown 格式
   *
   * 将所有摘录转换为 Markdown 文本返回，用户可以保存为 .md 文件。
   *
   * @returns {Promise<string>} Markdown 格式的摘录文本
   *
   * 请求方式：GET /api/v1/excerpts/export-markdown
   */
  exportMarkdown: () => request.get<any, string>('/excerpts/export-markdown')
}

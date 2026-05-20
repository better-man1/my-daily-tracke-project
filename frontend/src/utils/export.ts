/**
 * ============================================================================
 * 【数据导出工具模块（Export Utility）】
 * ============================================================================
 *
 * 【模块用途】
 * 提供通用的数据导出功能，支持将数据导出为 Excel（.xlsx）、CSV（.csv）、
 * JSON（.json）三种格式。同时针对项目中的各业务模块（计划、记账、摘录、
 * 总结、目标、仪表盘）提供了专用的导出函数，自动进行数据格式化和中文化处理。
 *
 * 【设计思想】
 * 1. 泛型基础函数 + 专用函数的分层设计：
 *    - exportData<T>：通用底层导出函数，接收任意数据数组和导出选项
 *    - exportPlanData、exportAccountingData 等：业务专用函数，
 *      负责将后端数据转换为中文友好的导出格式，然后调用通用函数
 *
 * 2. 策略模式：根据 format 参数（'xlsx' | 'csv' | 'json'）选择不同的导出策略，
 *    使用 switch-case 实现。
 *
 * 3. 数据脱敏与格式化：专用导出函数在导出前会将后端的英文枚举值
 *    （如 'TODO' => '待办'）转换为中文，使导出文件更易读。
 *
 * 【依赖库说明】
 * - xlsx（SheetJS）：纯前端的 Excel 读写库，支持 .xlsx、.csv 等格式的生成
 *   不需要后端支持，所有操作在浏览器端完成。
 * - file-saver：提供跨浏览器的文件保存功能（兼容不同浏览器的下载 API）
 * - dayjs：轻量级日期处理库，用于格式化日期字符串
 *
 * 【学习要点】
 * - TypeScript 泛型约束（<T extends Record<string, any>>）
 * - SheetJS 的基本用法（创建 workbook、worksheet、导出文件）
 * - Blob API 的使用（创建二进制文件对象）
 * - file-saver 的 saveAs 函数（触发浏览器文件下载）
 * - 数据映射和转换（将后端数据转为中文友好的导出格式）
 * - Record<string, string> 映射表的使用
 *
 * ============================================================================
 */

// ---- 【导入依赖】 ----

// xlsx：SheetJS 库，纯前端处理 Excel 文件
// import * as XLSX 将整个库导入为命名空间对象，避免与变量名冲突
import * as XLSX from 'xlsx'

// saveAs：file-saver 库的核心函数，用于触发浏览器文件下载
// 原理：创建一个隐藏的 <a> 标签，设置 download 属性，模拟用户点击
import { saveAs } from 'file-saver'

// dayjs：轻量级日期处理库，用于格式化日期
// 与 moment.js 相比，dayjs 只有 2KB 大小，API 几乎完全兼容
import dayjs from 'dayjs'

// ============================================================================
// 【类型定义】
// ============================================================================

/**
 * ExportFormat — 导出文件格式类型
 *
 * 使用 TypeScript 字面量联合类型约束合法的导出格式：
 * - 'xlsx'：Excel 2007+ 格式（最常用，支持多 sheet、格式化等）
 * - 'csv'：逗号分隔值格式（纯文本，兼容性好）
 * - 'json'：JSON 格式（保留完整的数据结构）
 */
export type ExportFormat = 'xlsx' | 'csv' | 'json'

/**
 * ExportOptions — 导出选项接口
 *
 * @interface ExportOptions
 * @property {string}        [filename]  - 导出文件名（不含扩展名），默认 'export_YYYYMMDD_HHmmss'
 * @property {ExportFormat}  [format]    - 导出格式，默认 'xlsx'
 * @property {string}        [sheetName] - Excel 中的工作表名称，默认 'Sheet1'
 */
export interface ExportOptions {
  filename?: string       // 文件名（不含扩展名）
  format?: ExportFormat   // 导出格式
  sheetName?: string      // Excel 工作表名称
}

// ============================================================================
// 【核心导出函数】
// ============================================================================

/**
 * exportData — 通用数据导出函数
 *
 * 这是所有业务导出函数的基础。接收一个数据数组和可选的导出选项，
 * 根据指定的格式生成文件并触发浏览器下载。
 *
 * @template T — 数据项的类型，必须是一个对象（Record<string, any>）
 *   泛型约束 T extends Record<string, any> 确保 data 的元素是对象而非原始类型
 *
 * @param {T[]} data             - 要导出的数据数组，每个元素是一行数据
 * @param {ExportOptions} [options={}] - 导出选项（可选）
 *
 * @example
 * ```ts
 * // 导出为 Excel
 * exportData([{ name: '张三', age: 25 }], { filename: '用户列表', format: 'xlsx' })
 *
 * // 导出为 CSV
 * exportData([{ name: '张三', age: 25 }], { format: 'csv' })
 *
 * // 导出为 JSON
 * exportData([{ name: '张三', age: 25 }], { format: 'json' })
 * ```
 *
 * 处理流程：
 * 1. 解构并设置默认的导出选项
 * 2. 检查数据是否为空（空数据不导出）
 * 3. 使用 SheetJS 创建 Workbook 和 Worksheet
 * 4. 根据 format 参数选择导出方式
 */
export function exportData<T extends Record<string, any>>(
  data: T[],
  options: ExportOptions = {}
) {
  // 解构选项并设置默认值
  // dayjs().format('YYYYMMDD_HHmmss') 生成类似 '20240115_083000' 的时间戳字符串
  const {
    filename = `export_${dayjs().format('YYYYMMDD_HHmmss')}`,
    format = 'xlsx',
    sheetName = 'Sheet1'
  } = options

  // 空数据检查：如果没有数据，打印警告并直接返回
  if (!data || data.length === 0) {
    console.warn('No data to export')
    return
  }

  // ---- 使用 SheetJS 创建 Excel 工作簿 ----
  // XLSX.utils.book_new()：创建一个新的空白工作簿（Workbook）
  const workbook = XLSX.utils.book_new()
  // XLSX.utils.json_to_sheet(data)：将 JSON 数组转换为工作表（Worksheet）
  // 自动使用对象的 key 作为列标题
  const worksheet = XLSX.utils.json_to_sheet(data)
  // XLSX.utils.book_append_sheet()：将工作表添加到工作簿中
  XLSX.utils.book_append_sheet(workbook, worksheet, sheetName)

  // 根据格式选择不同的导出方式
  let blob: Blob

  switch (format) {
    case 'csv':
      // CSV 格式：将 worksheet 转为 CSV 字符串，创建 Blob 并下载
      // Blob（Binary Large Object）是浏览器中表示二进制数据的对象
      const csv = XLSX.utils.sheet_to_csv(worksheet)
      blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
      // saveAs 触发浏览器文件下载对话框
      saveAs(blob, `${filename}.csv`)
      break

    case 'json':
      // JSON 格式：将数据序列化为格式化的 JSON 字符串
      // JSON.stringify 的第三个参数 2 表示使用 2 个空格缩进，便于阅读
      const json = JSON.stringify(data, null, 2)
      blob = new Blob([json], { type: 'application/json;charset=utf-8;' })
      saveAs(blob, `${filename}.json`)
      break

    case 'xlsx':
    default:
      // Excel 格式：直接使用 SheetJS 的 writeFile 方法生成并下载 .xlsx 文件
      // XLSX.writeFile 内部会处理 Blob 创建和下载，无需手动调用 saveAs
      XLSX.writeFile(workbook, `${filename}.xlsx`)
      break
  }
}

// ============================================================================
// 【业务专用导出函数】
// ============================================================================
//
// 以下函数针对不同的业务模块，将后端返回的数据转换为中文友好的导出格式。
// 每个函数负责：
// 1. 使用 .map() 将后端数据映射为中文标签的对象
// 2. 调用通用 exportData 函数执行实际导出
//
// 数据映射使用对象字面量简写（如 '序号': index + 1），
// 中文字符串作为对象的 key，直接成为 Excel 的列标题。
// ============================================================================

/**
 * exportPlanData — 导出计划数据
 *
 * @param {any[]} data    - 计划数据数组（来自 planApi.list 的响应）
 * @param {string} [filename] - 自定义文件名
 *
 * 导出列：序号、任务内容、优先级、状态、创建时间、完成时间、备注
 */
export function exportPlanData(data: any[], filename?: string) {
  // 使用 map 将每条数据转换为中文标签的导出对象
  const exportData = data.map((item, index) => ({
    '序号': index + 1,
    '任务内容': item.title || item.content || '',
    '优先级': item.priority || '',
    '状态': getStatusText(item.status),       // 英文枚举转中文
    '创建时间': formatDate(item.createdAt),    // 日期格式化
    '完成时间': formatDate(item.completedAt),
    '备注': item.remark || ''
  }))

  // 注意：这里的 exportData 是局部变量（映射后的数据），与顶层的 exportData 函数同名
  // JavaScript 的函数声明提升机制使得这里调用的是顶层函数而非局部变量
  exportData(exportData, {
    filename: filename || `计划数据_${dayjs().format('YYYYMMDD')}`,
    sheetName: '计划列表'
  })
}

/**
 * exportAccountingData — 导出记账数据
 *
 * @param {any[]} data    - 记账数据数组
 * @param {string} [filename] - 自定义文件名
 *
 * 导出列：序号、类型、金额、分类、备注、日期、创建时间
 */
export function exportAccountingData(data: any[], filename?: string) {
  const exportData = data.map((item, index) => ({
    '序号': index + 1,
    '类型': item.type === 'income' ? '收入' : '支出',  // 英文转中文
    '金额': item.amount || 0,
    '分类': item.category || '',
    '备注': item.remark || '',
    '日期': formatDate(item.date),
    '创建时间': formatDate(item.createdAt)
  }))

  exportData(exportData, {
    filename: filename || `记账数据_${dayjs().format('YYYYMMDD')}`,
    sheetName: '收支记录'
  })
}

/**
 * exportExcerptData — 导出摘录数据
 *
 * @param {any[]} data    - 摘录数据数组
 * @param {string} [filename] - 自定义文件名
 *
 * 导出列：序号、内容、来源、标签、日期、创建时间
 */
export function exportExcerptData(data: any[], filename?: string) {
  const exportData = data.map((item, index) => ({
    '序号': index + 1,
    '内容': item.content || '',
    '来源': item.source || '',
    '标签': (item.tags || []).join(', '),   // 标签数组用逗号连接
    '日期': formatDate(item.date),
    '创建时间': formatDate(item.createdAt)
  }))

  exportData(exportData, {
    filename: filename || `摘录数据_${dayjs().format('YYYYMMDD')}`,
    sheetName: '摘录列表'
  })
}

/**
 * exportSummaryData — 导出总结数据
 *
 * @param {any[]} data    - 总结数据数组
 * @param {string} [filename] - 自定义文件名
 *
 * 导出列：序号、日期、心情、总结内容、创建时间
 */
export function exportSummaryData(data: any[], filename?: string) {
  const exportData = data.map((item, index) => ({
    '序号': index + 1,
    '日期': formatDate(item.date),
    '心情': getMoodText(item.mood),         // 数字心情值转中文描述
    '总结内容': item.content || '',
    '创建时间': formatDate(item.createdAt)
  }))

  exportData(exportData, {
    filename: filename || `总结数据_${dayjs().format('YYYYMMDD')}`,
    sheetName: '总结列表'
  })
}

/**
 * exportGoalData — 导出目标数据
 *
 * @param {any[]} data    - 目标数据数组
 * @param {string} [filename] - 自定义文件名
 *
 * 导出列：序号、目标名称、描述、进度、状态、开始时间、目标时间、创建时间
 */
export function exportGoalData(data: any[], filename?: string) {
  const exportData = data.map((item, index) => ({
    '序号': index + 1,
    '目标名称': item.title || '',
    '描述': item.description || '',
    '进度': `${item.progress || 0}%`,       // 添加百分号
    '状态': getGoalStatusText(item.status),
    '开始时间': formatDate(item.startDate),
    '目标时间': formatDate(item.targetDate),
    '创建时间': formatDate(item.createdAt)
  }))

  exportData(exportData, {
    filename: filename || `目标数据_${dayjs().format('YYYYMMDD')}`,
    sheetName: '目标列表'
  })
}

/**
 * exportDashboardData — 导出仪表盘数据
 *
 * 与其他导出函数不同，仪表盘数据不是列表而是汇总数据，
 * 所以手动构建导出对象数组，每个元素代表一个数据模块。
 *
 * @param {any} data      - 仪表盘汇总数据（包含 plan、accounting、excerpt、summary、goal 子对象）
 * @param {string} [filename] - 自定义文件名
 */
export function exportDashboardData(data: any, filename?: string) {
  // 手动构建导出数据，每个对象对应仪表盘上的一个模块
  const exportData = [
    {
      '数据类型': '计划',
      '总任务数': data.plan?.total || 0,              // ?. 可选链，防止 plan 为 undefined 时报错
      '已完成': data.plan?.done || 0,
      '完成率': `${data.plan?.completionRate || 0}%`
    },
    {
      '数据类型': '记账',
      '收入': data.accounting?.income || 0,
      '支出': data.accounting?.expense || 0,
      '净收入': (data.accounting?.income || 0) - (data.accounting?.expense || 0)
    },
    {
      '数据类型': '摘录',
      '数量': data.excerptCount || 0
    },
    {
      '数据类型': '总结',
      '天数': data.summary?.days || 0,
      '平均心情': data.summary?.avgMood || '-'
    },
    {
      '数据类型': '目标',
      '进行中': data.goal?.activeCount || 0,
      '平均进度': `${data.goal?.avgProgress || 0}%`
    }
  ]

  exportData(exportData, {
    filename: filename || `仪表盘数据_${dayjs().format('YYYYMMDD')}`,
    sheetName: '数据汇总'
  })
}

// ============================================================================
// 【辅助函数】
// ============================================================================
//
// 以下函数用于将后端返回的英文枚举值和数字值转换为中文描述。
// 使用 Record<string, string> 映射表实现，输入 key 输出对应的中文 value。
// ============================================================================

/**
 * formatDate — 日期格式化
 *
 * 将各种格式的日期输入统一格式化为 'YYYY-MM-DD HH:mm:ss' 格式。
 * 使用 dayjs 库处理，能自动解析 ISO 8601、时间戳等多种日期格式。
 *
 * @param {string | Date | null | undefined} date - 日期输入（可能是各种格式或空值）
 * @returns {string} 格式化后的日期字符串，空值返回空字符串
 */
function formatDate(date: string | Date | null | undefined): string {
  if (!date) return ''                    // 空值直接返回空字符串
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')  // 统一格式化
}

/**
 * getStatusText — 计划状态英文转中文
 *
 * @param {string} status - 英文状态值
 * @returns {string} 中文状态描述
 */
function getStatusText(status: string): string {
  // 使用映射表（Record<key, value>）实现枚举到中文的转换
  const statusMap: Record<string, string> = {
    todo: '待办',
    'in-progress': '进行中',
    done: '已完成',
    cancelled: '已取消'
  }
  // 查找映射表，找不到则返回原始值（兜底处理）
  return statusMap[status] || status
}

/**
 * getMoodText — 心情数字转中文描述
 *
 * @param {number} mood - 心情值（1-5）
 * @returns {string} 带表情的心情描述
 */
function getMoodText(mood: number): string {
  const moodMap: Record<number, string> = {
    1: '很差',    // 1 分：很差
    2: '较差',    // 2 分：较差
    3: '一般',    // 3 分：一般
    4: '较好',    // 4 分：较好
    5: '很好'     // 5 分：很好
  }
  return moodMap[mood] || '-'
}

/**
 * getGoalStatusText — 目标状态英文转中文
 *
 * @param {string} status - 英文状态值
 * @returns {string} 中文状态描述
 */
function getGoalStatusText(status: string): string {
  const statusMap: Record<string, string> = {
    pending: '未开始',
    'in-progress': '进行中',
    completed: '已完成',
    paused: '已暂停'
  }
  return statusMap[status] || status
}

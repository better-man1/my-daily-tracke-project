import * as XLSX from 'xlsx'
import { saveAs } from 'file-saver'
import dayjs from 'dayjs'

/**
 * 导出类型
 */
export type ExportFormat = 'xlsx' | 'csv' | 'json'

/**
 * 导出选项
 */
export interface ExportOptions {
  filename?: string
  format?: ExportFormat
  sheetName?: string
}

/**
 * 导出数据到文件
 * @param data 要导出的数据数组
 * @param options 导出选项
 */
export function exportData<T extends Record<string, any>>(
  data: T[],
  options: ExportOptions = {}
) {
  const {
    filename = `export_${dayjs().format('YYYYMMDD_HHmmss')}`,
    format = 'xlsx',
    sheetName = 'Sheet1'
  } = options

  if (!data || data.length === 0) {
    console.warn('No data to export')
    return
  }

  const workbook = XLSX.utils.book_new()
  const worksheet = XLSX.utils.json_to_sheet(data)
  XLSX.utils.book_append_sheet(workbook, worksheet, sheetName)

  let blob: Blob

  switch (format) {
    case 'csv':
      const csv = XLSX.utils.sheet_to_csv(worksheet)
      blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
      saveAs(blob, `${filename}.csv`)
      break
    case 'json':
      const json = JSON.stringify(data, null, 2)
      blob = new Blob([json], { type: 'application/json;charset=utf-8;' })
      saveAs(blob, `${filename}.json`)
      break
    case 'xlsx':
    default:
      XLSX.writeFile(workbook, `${filename}.xlsx`)
      break
  }
}

/**
 * 导出计划数据
 */
export function exportPlanData(data: any[], filename?: string) {
  const exportData = data.map((item, index) => ({
    '序号': index + 1,
    '任务内容': item.title || item.content || '',
    '优先级': item.priority || '',
    '状态': getStatusText(item.status),
    '创建时间': formatDate(item.createdAt),
    '完成时间': formatDate(item.completedAt),
    '备注': item.remark || ''
  }))

  exportData(exportData, {
    filename: filename || `计划数据_${dayjs().format('YYYYMMDD')}`,
    sheetName: '计划列表'
  })
}

/**
 * 导出记账数据
 */
export function exportAccountingData(data: any[], filename?: string) {
  const exportData = data.map((item, index) => ({
    '序号': index + 1,
    '类型': item.type === 'income' ? '收入' : '支出',
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
 * 导出摘录数据
 */
export function exportExcerptData(data: any[], filename?: string) {
  const exportData = data.map((item, index) => ({
    '序号': index + 1,
    '内容': item.content || '',
    '来源': item.source || '',
    '标签': (item.tags || []).join(', '),
    '日期': formatDate(item.date),
    '创建时间': formatDate(item.createdAt)
  }))

  exportData(exportData, {
    filename: filename || `摘录数据_${dayjs().format('YYYYMMDD')}`,
    sheetName: '摘录列表'
  })
}

/**
 * 导出总结数据
 */
export function exportSummaryData(data: any[], filename?: string) {
  const exportData = data.map((item, index) => ({
    '序号': index + 1,
    '日期': formatDate(item.date),
    '心情': getMoodText(item.mood),
    '总结内容': item.content || '',
    '创建时间': formatDate(item.createdAt)
  }))

  exportData(exportData, {
    filename: filename || `总结数据_${dayjs().format('YYYYMMDD')}`,
    sheetName: '总结列表'
  })
}

/**
 * 导出目标数据
 */
export function exportGoalData(data: any[], filename?: string) {
  const exportData = data.map((item, index) => ({
    '序号': index + 1,
    '目标名称': item.title || '',
    '描述': item.description || '',
    '进度': `${item.progress || 0}%`,
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
 * 导出仪表盘数据
 */
export function exportDashboardData(data: any, filename?: string) {
  const exportData = [
    {
      '数据类型': '计划',
      '总任务数': data.plan?.total || 0,
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

// 辅助函数
function formatDate(date: string | Date | null | undefined): string {
  if (!date) return ''
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

function getStatusText(status: string): string {
  const statusMap: Record<string, string> = {
    todo: '待办',
    'in-progress': '进行中',
    done: '已完成',
    cancelled: '已取消'
  }
  return statusMap[status] || status
}

function getMoodText(mood: number): string {
  const moodMap: Record<number, string> = {
    1: '😢 很差',
    2: '😔 较差',
    3: '😐 一般',
    4: '😊 较好',
    5: '😄 很好'
  }
  return moodMap[mood] || '-'
}

function getGoalStatusText(status: string): string {
  const statusMap: Record<string, string> = {
    pending: '未开始',
    'in-progress': '进行中',
    completed: '已完成',
    paused: '已暂停'
  }
  return statusMap[status] || status
}

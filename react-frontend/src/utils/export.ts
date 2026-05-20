/**
 * 导出工具函数
 */

import { saveAs } from 'file-saver'

/**
 * 导出为 CSV
 */
export function exportToCSV(data: any[], filename: string): void {
  const headers = Object.keys(data[0] || {})
  const csvContent = [
    headers.join(','),
    ...data.map(row => headers.map(header => row[header]).join(','))
  ].join('\n')

  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  saveAs(blob, `${filename}.csv`)
}

/**
 * 导出为 JSON
 */
export function exportToJSON(data: any[], filename: string): void {
  const jsonContent = JSON.stringify(data, null, 2)
  const blob = new Blob([jsonContent], { type: 'application/json;charset=utf-8;' })
  saveAs(blob, `${filename}.json`)
}
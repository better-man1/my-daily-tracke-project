/**
 * 格式化工具函数
 */

import dayjs from 'dayjs'

/**
 * 格式化日期
 */
export function formatDate(date: string | Date, format: string = 'YYYY-MM-DD'): string {
  return dayjs(date).format(format)
}

/**
 * 格式化时间
 */
export function formatTime(time: string | Date): string {
  return dayjs(time).format('HH:mm:ss')
}

/**
 * 格式化金额
 */
export function formatMoney(amount: number): string {
  return amount.toFixed(2)
}

/**
 * 格式化百分比
 */
export function formatPercent(value: number): string {
  return `${value.toFixed(1)}%`
}
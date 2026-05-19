/**
 * 日期工具函数
 */
import dayjs from 'dayjs'

/** 格式化日期为 YYYY-MM-DD */
export function formatDate(date?: string | Date): string {
  return dayjs(date).format('YYYY-MM-DD')
}

/** 格式化日期为 MM月DD日 */
export function formatDateCN(date?: string | Date): string {
  return dayjs(date).format('M月D日')
}

/** 格式化日期为 YYYY年MM月DD日 */
export function formatDateFullCN(date?: string | Date): string {
  return dayjs(date).format('YYYY年M月D日')
}

/** 获取星期文本 */
export function getWeekDay(date?: string | Date): string {
  const days = ['日', '一', '二', '三', '四', '五', '六']
  return `周${days[dayjs(date).day()]}`
}

/** 获取今天的日期字符串 */
export function getToday(): string {
  return dayjs().format('YYYY-MM-DD')
}

/** 获取相对时间描述 */
export function getRelativeTime(date: string): string {
  const now = dayjs()
  const target = dayjs(date)
  const diffMin = now.diff(target, 'minute')
  const diffHour = now.diff(target, 'hour')
  const diffDay = now.diff(target, 'day')

  if (diffMin < 1) return '刚刚'
  if (diffMin < 60) return `${diffMin}分钟前`
  if (diffHour < 24) return `${diffHour}小时前`
  if (diffDay < 7) return `${diffDay}天前`
  return target.format('M月D日')
}

/** 格式化金额 */
export function formatMoney(amount: number): string {
  return amount.toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

/** 格式化分钟为 x小时x分钟 */
export function formatMinutes(mins: number): string {
  if (!mins || mins <= 0) return '0分钟'
  const h = Math.floor(mins / 60)
  const m = mins % 60
  if (h === 0) return `${m}分钟`
  if (m === 0) return `${h}小时`
  return `${h}小时${m}分钟`
}

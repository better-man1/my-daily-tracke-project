/**
 * ============================================================================
 * date.ts — 日期与格式化工具函数
 * ============================================================================
 *
 * 【文件说明】
 * 提供日期格式化、金额格式化、时间描述等通用工具函数。
 * 使用 dayjs 作为日期处理库（轻量级的 moment.js 替代品，仅 2KB）。
 *
 * 【为什么选择 dayjs？】
 * - 体积小（2KB gzipped），适合小程序对包体积的严格限制
 * - API 与 moment.js 兼容，学习成本低
 * - 支持链式调用、不可变数据
 * - 支持插件扩展（相对时间、自定义解析等）
 *
 * 【小程序包体积限制】
 * - 微信小程序主包限制 2MB
 * - 分包每个限制 2MB
 * - 总包限制通常 20MB
 * 因此必须选择轻量级库，moment.js（约 70KB）不推荐使用。
 *
 * 【参考文档】
 * - dayjs 中文文档: https://dayjs.gitee.io/docs/zh-CN/parser
 * ============================================================================
 */
import dayjs from 'dayjs'

/**
 * 格式化日期为 YYYY-MM-DD 格式
 * @param date - 可选，传入 Date 对象或日期字符串，不传则默认当前时间
 * @returns 如 '2025-01-15'
 */
export function formatDate(date?: string | Date): string {
  return dayjs(date).format('YYYY-MM-DD')
}

/**
 * 格式化日期为中文短格式：M月D日
 * @param date - 可选，传入 Date 对象或日期字符串
 * @returns 如 '1月15日'
 */
export function formatDateCN(date?: string | Date): string {
  return dayjs(date).format('M月D日')
}

/**
 * 格式化日期为中文完整格式：YYYY年M月D日
 * @param date - 可选，传入 Date 对象或日期字符串
 * @returns 如 '2025年1月15日'
 */
export function formatDateFullCN(date?: string | Date): string {
  return dayjs(date).format('YYYY年M月D日')
}

/**
 * 获取星期文本
 * @param date - 可选，传入 Date 对象或日期字符串
 * @returns 如 '周一'、'周二'... '周日'
 */
export function getWeekDay(date?: string | Date): string {
  const days = ['日', '一', '二', '三', '四', '五', '六']
  return `周${days[dayjs(date).day()]}`
}

/**
 * 获取今天的日期字符串（YYYY-MM-DD）
 * @returns 如 '2025-01-15'
 */
export function getToday(): string {
  return dayjs().format('YYYY-MM-DD')
}

/**
 * 获取相对时间描述（用于显示"刚刚"、"3分钟前"等）
 * @param date - 日期字符串
 * @returns 相对时间描述文本
 */
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

/**
 * 格式化金额（保留两位小数，加千分位逗号）
 * @param amount - 金额数值
 * @returns 如 '1,234.56'
 */
export function formatMoney(amount: number): string {
  return amount.toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

/**
 * 格式化分钟数为易读的中文描述
 * @param mins - 分钟数
 * @returns 如 '2小时30分钟'、'45分钟'、'3小时'
 */
export function formatMinutes(mins: number): string {
  if (!mins || mins <= 0) return '0分钟'
  const h = Math.floor(mins / 60)
  const m = mins % 60
  if (h === 0) return `${m}分钟`
  if (m === 0) return `${h}小时`
  return `${h}小时${m}分钟`
}

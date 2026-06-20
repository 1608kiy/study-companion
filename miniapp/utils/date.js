// 日期工具函数 - 使用本地时间，避免时区问题

/**
 * 获取今天的本地日期字符串 (YYYY-MM-DD)
 */
export const getToday = () => {
  const now = new Date()
  return formatDate(now)
}

/**
 * 获取当前月份字符串 (YYYY-MM)
 */
export const getCurrentMonth = () => {
  const now = new Date()
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
}

/**
 * 格式化日期为本地日期字符串 (YYYY-MM-DD)
 */
export const formatDate = (date) => {
  if (typeof date === 'string') {
    // 如果已经是字符串格式，直接返回
    if (/^\d{4}-\d{2}-\d{2}$/.test(date)) return date
    // 否则解析后格式化
    date = new Date(date)
  }
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

/**
 * 格式化日期为本地月份字符串 (YYYY-MM)
 */
export const formatMonth = (date) => {
  if (typeof date === 'string') {
    if (/^\d{4}-\d{2}$/.test(date)) return date
    date = new Date(date)
  }
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  return `${year}-${month}`
}

/**
 * 获取前一天的日期
 */
export const getPrevDay = (dateStr) => {
  const date = parseDate(dateStr)
  date.setDate(date.getDate() - 1)
  return formatDate(date)
}

/**
 * 获取后一天的日期
 */
export const getNextDay = (dateStr) => {
  const date = parseDate(dateStr)
  date.setDate(date.getDate() + 1)
  return formatDate(date)
}

/**
 * 获取上一个月
 */
export const getPrevMonth = (monthStr) => {
  const [year, month] = monthStr.split('-').map(Number)
  const date = new Date(year, month - 2, 1)
  return formatMonth(date)
}

/**
 * 获取下一个月
 */
export const getNextMonth = (monthStr) => {
  const [year, month] = monthStr.split('-').map(Number)
  const date = new Date(year, month, 1)
  return formatMonth(date)
}

/**
 * 解析日期字符串为本地 Date 对象
 * 避免 "YYYY-MM-DD" 被解析为 UTC
 */
export const parseDate = (dateStr) => {
  if (dateStr instanceof Date) return dateStr
  if (/^\d{4}-\d{2}-\d{2}$/.test(dateStr)) {
    const [year, month, day] = dateStr.split('-').map(Number)
    return new Date(year, month - 1, day)
  }
  return new Date(dateStr)
}

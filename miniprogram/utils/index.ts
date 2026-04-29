/**
 * 通用工具函数（小程序适配版 —— 无 window/document 依赖）
 */

/** 格式化日期 YYYY-MM-DD */
export function formatDate(date: string | Date): string {
  if (!date) return ''
  const d = typeof date === 'string' ? new Date(date) : date
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

/** 格式化日期时间 YYYY-MM-DD HH:mm */
export function formatDateTime(date: string | Date): string {
  if (!date) return ''
  const d = typeof date === 'string' ? new Date(date) : date
  const dateStr = formatDate(d)
  const h = String(d.getHours()).padStart(2, '0')
  const min = String(d.getMinutes()).padStart(2, '0')
  return `${dateStr} ${h}:${min}`
}

/** 获取系统信息 */
export function getSystemInfo() {
  return uni.getSystemInfoSync()
}

/** 判断是否为小屏幕（宽度 < 375） */
export function isSmallScreen(): boolean {
  const { screenWidth } = uni.getSystemInfoSync()
  return screenWidth < 375
}

/** 复制文本到剪贴板 */
export function copyText(text: string): Promise<void> {
  return new Promise((resolve, reject) => {
    uni.setClipboardData({
      data: text,
      success: () => resolve(),
      fail: reject,
    })
  })
}

/** 解析图片字符串（JSON 数组或逗号分隔） */
export function parseImages(images: string): string[] {
  if (!images) return []
  try {
    const arr = JSON.parse(images)
    return Array.isArray(arr) ? arr : []
  } catch {
    return images.split(',').map((s) => s.trim()).filter(Boolean)
  }
}

/** 获取第一张图片 */
export function getFirstImage(images: string): string {
  const arr = parseImages(images)
  return arr[0] || ''
}

/** 设备状态映射 */
export const DEVICE_STATUS_MAP: Record<number, string> = {
  0: '离线',
  1: '在线',
  2: '飞行中',
  3: '维护中',
  4: '已报废',
}

/** 订单状态映射 */
export const ORDER_STATUS_MAP: Record<number, string> = {
  0: '待支付',
  1: '进行中',
  2: '已完成',
  3: '已取消',
}

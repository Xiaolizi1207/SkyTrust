import request from '@/utils/request'
import type { ApiResult, PriceForecast, DispatchTask } from '@/types/api'

/** 获取价格预测 */
export function getPriceForecast(baseId: string) {
  return request.get<ApiResult<PriceForecast>>(`/pricing/forecast/${baseId}`)
}

/** 获取派遣任务列表（可选按基地筛选） */
export function getDispatchTasks(baseId?: string) {
  const params = baseId ? { baseId } : undefined
  return request.get<ApiResult<DispatchTask[]>>('/pricing/dispatch-tasks', { params })
}

/** 创建派遣任务 */
export function createDispatchTask(task: Omit<DispatchTask, 'id'>) {
  return request.post<ApiResult<DispatchTask>>('/pricing/dispatch-tasks', task)
}

/** 获取需求热力图数据 */
export function getDemandHeatmap() {
  return request.get<ApiResult<{ baseId: string; demand: number; lat: number; lon: number }[]>>('/pricing/demand-heatmap')
}

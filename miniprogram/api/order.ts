import request from '@/utils/request'
import type { ApiResult, CreateOrderParams, RentalOrderVO, OrderQueryParams, PageResult } from '@/types/api'

/** 创建租赁订单 */
export function createOrderApi(data: CreateOrderParams) {
  return request.post<ApiResult<RentalOrderVO>>('/orders', data)
}

/** 获取订单列表（分页） */
export function getOrderListApi(params: OrderQueryParams) {
  return request.get<ApiResult<PageResult<RentalOrderVO>>>('/orders', params)
}

/** 获取订单详情 */
export function getOrderDetailApi(id: number) {
  return request.get<ApiResult<RentalOrderVO>>(`/orders/${id}`)
}

/** 取消订单（reason 通过 query 参数传递，匹配后端 @RequestParam） */
export function cancelOrderApi(id: number, reason?: string) {
  const reasonParam = encodeURIComponent(reason || '用户主动取消')
  return request.post<ApiResult<null>>(`/orders/${id}/cancel?reason=${reasonParam}`)
}

/** 续租订单 */
export function renewOrderApi(id: number, days: number) {
  return request.put<ApiResult<RentalOrderVO>>(`/orders/${id}/renew`, { days })
}

import request from '@/utils/request'
import type { ApiResult, CreateOrderParams, RentalOrderVO, OrderQueryParams, PageResult } from '@/types/api'

/** 创建租赁订单 */
export function createOrderApi(data: CreateOrderParams) {
  return request.post<ApiResult<RentalOrderVO>>('/rental-orders', data)
}

/** 获取订单列表（分页） */
export function getOrderListApi(params: OrderQueryParams) {
  return request.get<ApiResult<PageResult<RentalOrderVO>>>('/rental-orders', params)
}

/** 获取订单详情 */
export function getOrderDetailApi(id: number) {
  return request.get<ApiResult<RentalOrderVO>>(`/rental-orders/${id}`)
}

/** 取消订单 */
export function cancelOrderApi(id: number) {
  return request.put<ApiResult<null>>(`/rental-orders/${id}/cancel`)
}

/** 续租订单 */
export function renewOrderApi(id: number, days: number) {
  return request.put<ApiResult<RentalOrderVO>>(`/rental-orders/${id}/renew`, { days })
}

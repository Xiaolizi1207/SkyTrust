import request from '@/utils/request'
import type { ApiResult, RentalOrderVO, RentalOrderDTO, RentalOrderQueryParams } from '@/types/api'

/** 分页查询租赁订单 */
export function getOrderListApi(params: RentalOrderQueryParams) {
  return request.get<ApiResult<RentalOrderVO[]>>('/orders', { params })
}

/** 获取订单详情 */
export function getOrderApi(id: number) {
  return request.get<ApiResult<RentalOrderVO>>(`/orders/${id}`)
}

/** 创建租赁订单 */
export function createOrderApi(data: RentalOrderDTO) {
  return request.post<ApiResult<RentalOrderVO>>('/orders', data)
}

/** 更新订单 */
export function updateOrderApi(id: number, data: RentalOrderDTO) {
  return request.put<ApiResult<RentalOrderVO>>(`/orders/${id}`, data)
}

/** 删除订单（逻辑删除） */
export function deleteOrderApi(id: number) {
  return request.delete<ApiResult<null>>(`/orders/${id}`)
}

/** 按用户ID查询 */
export function getOrdersByUserIdApi(userId: number) {
  return request.get<ApiResult<RentalOrderVO[]>>(`/orders/user/${userId}`)
}

/** 按设备ID查询 */
export function getOrdersByDeviceIdApi(deviceId: number) {
  return request.get<ApiResult<RentalOrderVO[]>>(`/orders/device/${deviceId}`)
}

/** 按状态查询 */
export function getOrdersByStatusApi(status: number) {
  return request.get<ApiResult<RentalOrderVO[]>>(`/orders/status/${status}`)
}

/** 按订单号查询 */
export function getOrderByOrderNoApi(orderNo: string) {
  return request.get<ApiResult<RentalOrderVO>>(`/orders/orderNo/${encodeURIComponent(orderNo)}`)
}

/** 取消订单 */
export function cancelOrderApi(id: number, reason: string) {
  return request.post<ApiResult<null>>(`/orders/${id}/cancel`, null, {
    params: { reason },
  })
}

/** 完成订单 */
export function completeOrderApi(id: number) {
  return request.post<ApiResult<null>>(`/orders/${id}/complete`)
}

/** 续租订单 */
export function renewOrderApi(id: number, days: number) {
  return request.put<ApiResult<RentalOrderVO>>(`/orders/${id}/renew`, { days })
}

/** 生成订单号 */
export function generateOrderNoApi() {
  return request.get<ApiResult<string>>('/orders/generate-order-no')
}

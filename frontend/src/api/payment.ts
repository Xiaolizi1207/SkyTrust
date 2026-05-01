import request from '@/utils/request'
import type { ApiResult, PaymentVO, PaymentDTO, PaymentQueryParams } from '@/types/api'

/** 分页查询支付记录 */
export function getPaymentListApi(params: PaymentQueryParams) {
  return request.get<ApiResult<PaymentVO[]>>('/payments', { params })
}

/** 获取支付记录详情 */
export function getPaymentApi(id: number) {
  return request.get<ApiResult<PaymentVO>>(`/payments/${id}`)
}

/** 创建支付记录 */
export function createPaymentApi(data: PaymentDTO) {
  return request.post<ApiResult<PaymentVO>>('/payments', data)
}

/** 更新支付记录 */
export function updatePaymentApi(id: number, data: PaymentDTO) {
  return request.put<ApiResult<PaymentVO>>(`/payments/${id}`, data)
}

/** 删除支付记录 */
export function deletePaymentApi(id: number) {
  return request.delete<ApiResult<null>>(`/payments/${id}`)
}

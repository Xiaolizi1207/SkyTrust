import request from '@/utils/request'
import type { ApiResult, PaymentParams, PaymentResult } from '@/types/api'

/** 统一下单（微信支付） */
export function createPaymentApi(data: PaymentParams) {
  return request.post<ApiResult<PaymentResult>>('/payments', data)
}

/** 查询支付状态 */
export function getPaymentStatusApi(orderId: number) {
  return request.get<ApiResult<{ payStatus: number }>>(`/payments/status/${orderId}`)
}

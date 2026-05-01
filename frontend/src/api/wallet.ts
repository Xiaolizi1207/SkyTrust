import request from '@/utils/request'
import type { ApiResult, WalletVO, WalletTransactionVO, RechargeDTO } from '@/types/api'

/** 获取钱包余额 */
export function getBalanceApi() {
  return request.get<ApiResult<WalletVO>>('/wallet/balance')
}

/** 获取交易记录（分页） */
export function getTransactionsApi(page: number, size: number) {
  return request.get<ApiResult<WalletTransactionVO[]>>('/wallet/transactions', {
    params: { page, size },
  })
}

/** 钱包充值 */
export function rechargeApi(data: RechargeDTO) {
  return request.post<ApiResult<WalletVO>>('/wallet/recharge', data)
}

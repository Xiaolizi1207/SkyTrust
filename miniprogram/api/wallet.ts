import request from '@/utils/request'
import type { ApiResult, WalletVO, WalletTransactionVO, RechargeParams } from '@/types/api'

export function getWalletBalanceApi() {
  return request.get<ApiResult<WalletVO>>('/wallet/balance')
}

export function getWalletTransactionsApi(page = 1, size = 10) {
  return request.get<ApiResult<WalletTransactionVO[]>>('/wallet/transactions', { page, size })
}

export function rechargeApi(data: RechargeParams) {
  return request.post<ApiResult<WalletVO>>('/wallet/recharge', data)
}

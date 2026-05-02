import request from '@/utils/request'
import type { ApiResult, DronePassport, FlightLogEntry, LicenseRecord } from '@/types/api'

export function getDronePassportApi(tokenId: number) {
  return request.get<ApiResult<DronePassport>>(`/blockchain/passport/${tokenId}`)
}

export function getFlightLogsApi(orderId: string) {
  return request.get<ApiResult<FlightLogEntry[]>>(`/blockchain/flight-logs/${orderId}`)
}

export function verifyFlightLogApi(orderId: string, logHash: string) {
  return request.post<ApiResult<{ verified: boolean }>>('/blockchain/flight-logs/verify', { orderId, logHash })
}

export function verifyLicenseApi(orderId: string) {
  return request.get<ApiResult<LicenseRecord>>(`/blockchain/license/verify/${orderId}`)
}

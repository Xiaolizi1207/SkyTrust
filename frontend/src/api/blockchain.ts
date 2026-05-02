import request from '@/utils/request'
import type {
  ApiResult,
  DronePassport,
  RepairRecord,
  LicenseRecord,
  RentalOrder,
  FlightLogEntry,
} from '@/types/api'

/** 获取无人机 Passport（区块链绑定信息）	*/
export function getDronePassport(tokenId: number) {
  return request.get<ApiResult<DronePassport>>(`/blockchain/passport/${tokenId}`)
}

/** 获取维修历史记录（区块链绑定） */
export function getRepairHistory(tokenId: number) {
  return request.get<ApiResult<RepairRecord[]>>(`/blockchain/passport/${tokenId}/repair-history`)
}

/** 验证许可证（License） */
export function verifyLicense(orderId: string) {
  return request.get<ApiResult<LicenseRecord>>(`/blockchain/license/verify/${orderId}`)
}

/** 创建租赁订单（Blockchain） */
export function createRentalOrder(userDID: string, licenseHashInput: string, signature: string) {
  const data = {
    userDID,
    licenseHash: licenseHashInput,
    signature,
  }
  return request.post<ApiResult<RentalOrder>>('/blockchain/rental-orders', data)
}

/** 根据订单获取许可证哈希值 */
export function getLicenseHashByOrder(orderId: string) {
  return request.get<ApiResult<string>>(`/blockchain/license/hash/${orderId}`)
}

/** 提交飞行日志 */
export function submitFlightLogs(orderId: string, logEntries: { logHash: string; timestamp: number }[]) {
  return request.post<ApiResult<void>>(`/blockchain/flight-logs/${orderId}`, { entries: logEntries })
}

/** 验证飞行日志完整性 */
export function verifyFlightLogIntegrity(orderId: string, externalHash: string) {
  return request.post<ApiResult<boolean>>('/blockchain/flight-logs/verify', {
    orderId,
    externalHash,
  })
}

/** 获取飞行日志 */
export function getFlightLogs(orderId: string) {
  return request.get<ApiResult<FlightLogEntry[]>>(`/blockchain/flight-logs/${orderId}`)
}

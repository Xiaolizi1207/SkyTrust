import request from '@/utils/request'
import type { ApiResult, DeviceVO, DeviceQueryParams, DeviceStatistics } from '@/types/api'

/** 分页查询设备列表 */
export function getDeviceListApi(params: DeviceQueryParams) {
  return request.get<ApiResult<DeviceVO[]>>('/devices', params)
}

/** 获取设备详情 */
export function getDeviceApi(id: number) {
  return request.get<ApiResult<DeviceVO>>(`/devices/${id}`)
}

/** 获取可用设备列表 */
export function getAvailableDevicesApi() {
  return request.get<ApiResult<DeviceVO[]>>('/devices/available')
}

/** 获取附近设备 */
export function getNearbyDevicesApi(latitude: number, longitude: number, radius?: number) {
  return request.get<ApiResult<DeviceVO[]>>('/devices/nearby', { latitude, longitude, radius })
}

/** 检查设备是否可用 */
export function checkDeviceAvailableApi(id: number) {
  return request.get<ApiResult<boolean>>(`/devices/${id}/available`)
}

/** 获取设备统计信息 */
export function getDeviceStatisticsApi() {
  return request.get<ApiResult<DeviceStatistics>>('/devices/statistics')
}

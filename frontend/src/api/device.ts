import request from '@/utils/request'
import type { ApiResult, DeviceVO, DeviceDTO, DeviceQueryParams, DeviceStatistics } from '@/types/api'

/** 分页查询设备列表 */
export function getDeviceListApi(params: DeviceQueryParams) {
  return request.get<ApiResult<DeviceVO[]>>('/devices', { params })
}

/** 获取设备详情 */
export function getDeviceApi(id: number) {
  return request.get<ApiResult<DeviceVO>>(`/devices/${id}`)
}

/** 创建设备 */
export function createDeviceApi(data: DeviceDTO) {
  return request.post<ApiResult<DeviceVO>>('/devices', data)
}

/** 更新设备 */
export function updateDeviceApi(id: number, data: DeviceDTO) {
  return request.put<ApiResult<DeviceVO>>(`/devices/${id}`, data)
}

/** 删除设备 */
export function deleteDeviceApi(id: number) {
  return request.delete<ApiResult<null>>(`/devices/${id}`)
}

/** 更新设备状态 */
export function updateDeviceStatusApi(id: number, status: number) {
  return request.put<ApiResult<null>>(`/devices/${id}/status?status=${status}`)
}

/** 更新设备位置 */
export function updateDeviceLocationApi(id: number, latitude: number, longitude: number, altitude?: number) {
  let url = `/devices/${id}/location?latitude=${latitude}&longitude=${longitude}`
  if (altitude != null) url += `&altitude=${altitude}`
  return request.put<ApiResult<null>>(url)
}

/** 更新设备电量 */
export function updateDeviceBatteryApi(id: number, batteryLevel: number) {
  return request.put<ApiResult<null>>(`/devices/${id}/battery?batteryLevel=${batteryLevel}`)
}

/** 更新设备飞行数据 */
export function updateDeviceFlightDataApi(id: number, speed: number, totalFlightHours: number) {
  return request.put<ApiResult<null>>(`/devices/${id}/flight-data?speed=${speed}&totalFlightHours=${totalFlightHours}`)
}

/** 获取可用设备列表 */
export function getAvailableDevicesApi() {
  return request.get<ApiResult<DeviceVO[]>>('/devices/available')
}

/** 获取附近设备 */
export function getNearbyDevicesApi(latitude: number, longitude: number, radius?: number) {
  return request.get<ApiResult<DeviceVO[]>>('/devices/nearby', { params: { latitude, longitude, radius } })
}

/** 检查设备是否可用 */
export function checkDeviceAvailableApi(id: number) {
  return request.get<ApiResult<boolean>>(`/devices/${id}/available`)
}

/** 设备维护 */
export function maintainDeviceApi(id: number, remark: string) {
  return request.post<ApiResult<null>>(`/devices/${id}/maintain?remark=${encodeURIComponent(remark)}`)
}

/** 设备报废 */
export function scrapDeviceApi(id: number, remark: string) {
  return request.post<ApiResult<null>>(`/devices/${id}/scrap?remark=${encodeURIComponent(remark)}`)
}

/** 获取设备统计信息 */
export function getDeviceStatisticsApi() {
  return request.get<ApiResult<DeviceStatistics>>('/devices/statistics')
}

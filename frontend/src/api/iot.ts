import request from '@/utils/request'
import type { ApiResult, IotCommandResult, DeviceRealtimeStatus } from '@/types/api'

/** 下发设备指令 */
export function sendCommandApi(deviceId: number, command: string) {
  return request.post<ApiResult<IotCommandResult>>(`/iot/devices/${deviceId}/command`, null, {
    params: { command },
  })
}

/** 请求设备遥测刷新 */
export function requestTelemetryApi(deviceId: number) {
  return request.post<ApiResult<{ deviceId: number; success: boolean }>>(`/iot/devices/${deviceId}/telemetry-request`)
}

/** 获取设备实时状态 */
export function getDeviceRealtimeStatusApi(deviceId: number) {
  return request.get<ApiResult<DeviceRealtimeStatus>>(`/iot/devices/${deviceId}/status`)
}

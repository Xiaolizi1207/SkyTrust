import request from '@/utils/request'
import type { ApiResult, NoFlyZone, GeofenceCheckResult } from '@/types/api'

/** 获取禁飞区列表 */
export function getNoFlyZones() {
  return request.get<ApiResult<NoFlyZone[]>>('/geofence/no-fly-zones')
}

/** 更新禁飞区信息 */
export function updateNoFlyZone(zoneId: string, data: Partial<NoFlyZone>) {
  return request.put<ApiResult<void>>(`/geofence/no-fly-zones/${zoneId}`, data)
}

/** 检查当前位置是否安全/可能违规 */
export function checkPosition(lat: number, lon: number, speed: number, heading: number) {
  return request.get<ApiResult<GeofenceCheckResult>>('/geofence/check-position', {
    params: { lat, lon, speed, heading },
  })
}

/** 上传区域的 GeoJSON 数据 */
export function uploadZoneGeoJSON(geoJSON: object) {
  return request.post<ApiResult<void>>('/geofence/upload-geojson', geoJSON)
}

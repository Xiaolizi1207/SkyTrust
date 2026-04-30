import request from '@/utils/request'
import type { ApiResult, UserInfo } from '@/types/api'

export function getCurrentUserApi() {
  return request.get<ApiResult<UserInfo>>('/users/me')
}

export function updateUserApi(id: number, data: Record<string, any>) {
  return request.put<ApiResult<UserInfo>>(`/users/${id}`, data)
}

export function updatePasswordApi(id: number, oldPassword: string, newPassword: string) {
  return request.put<ApiResult<null>>(`/users/${id}/password`, { oldPassword, newPassword })
}

import request from '@/utils/request'
import type { ApiResult, UserAdminVO, UserAdminDTO, UserQueryParams } from '@/types/api'

/** 分页查询用户列表（后台） */
export function getUserListApi(params: UserQueryParams) {
  return request.get<ApiResult<UserAdminVO[]>>('/users', { params })
}

/** 获取用户详情 */
export function getUserApi(id: number) {
  return request.get<ApiResult<UserAdminVO>>(`/users/${id}`)
}

/** 创建用户（后台） */
export function createUserApi(data: UserAdminDTO) {
  return request.post<ApiResult<UserAdminVO>>('/users', data)
}

/** 更新用户 */
export function updateUserApi(id: number, data: UserAdminDTO) {
  return request.put<ApiResult<UserAdminVO>>(`/users/${id}`, data)
}

/** 删除用户（逻辑删除） */
export function deleteUserApi(id: number) {
  return request.delete<ApiResult<null>>(`/users/${id}`)
}

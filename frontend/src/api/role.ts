import request from '@/utils/request'
import type { ApiResult, RoleVO, RoleDTO, RoleQueryParams } from '@/types/api'
import type { MenuNode } from '@/api/menu'

/** 分页查询角色列表 */
export function getRoleListApi(params: RoleQueryParams) {
  return request.get<ApiResult<RoleVO[]>>('/roles', { params })
}

/** 获取角色详情 */
export function getRoleApi(id: number) {
  return request.get<ApiResult<RoleVO>>(`/roles/${id}`)
}

/** 创建角色 */
export function createRoleApi(data: RoleDTO) {
  return request.post<ApiResult<RoleVO>>('/roles', data)
}

/** 更新角色 */
export function updateRoleApi(id: number, data: RoleDTO) {
  return request.put<ApiResult<RoleVO>>(`/roles/${id}`, data)
}

/** 删除角色 */
export function deleteRoleApi(id: number) {
  return request.delete<ApiResult<null>>(`/roles/${id}`)
}

/** 所有角色（不分页，用于下拉选择） */
export function getAllRolesApi() {
  return request.get<ApiResult<RoleVO[]>>('/roles', { params: { size: 999 } })
}

/** 为角色分配菜单 */
export function assignRoleMenusApi(roleId: number, menuIds: number[]) {
  return request.put<ApiResult<null>>(`/roles/${roleId}/menus`, { menuIds })
}

/** 获取角色已分配的菜单ID列表 */
export function getRoleMenuIdsApi(roleId: number) {
  return request.get<ApiResult<number[]>>(`/roles/${roleId}/menus`)
}

/** 获取角色的菜单树 */
export function getRoleMenuTreeApi(roleId: number) {
  return request.get<ApiResult<MenuNode[]>>(`/menus/role/${roleId}/tree`)
}

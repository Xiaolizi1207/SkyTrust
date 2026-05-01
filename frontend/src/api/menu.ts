import request from '@/utils/request'
import type { ApiResult } from '@/types/api'

/** 菜单节点 */
export interface MenuNode {
  id: number
  parentId: number
  menuName: string
  menuCode: string
  menuPath?: string
  component?: string
  icon?: string
  menuType: number
  sortOrder: number
  perms?: string
  isExternal: number
  isCache: number
  isVisible: number
  children?: MenuNode[]
}

/** 菜单创建/更新请求 */
export interface MenuDTO {
  parentId: number
  menuName: string
  menuCode: string
  menuPath?: string
  component?: string
  icon?: string
  menuType: number
  sortOrder?: number
  perms?: string
  isExternal?: number
  isCache?: number
  isVisible?: number
}

/** ========== 当前用户菜单 ========== */

/** 获取当前用户有权限的菜单树 */
export function getUserMenusApi() {
  return request.get<ApiResult<MenuNode[]>>('/menus/current/tree')
}

/** ========== 菜单管理后台 CRUD ========== */

/** 获取全部菜单树（后台管理用） */
export function getMenuTreeApi() {
  return request.get<ApiResult<MenuNode[]>>('/menus/tree')
}

/** 获取菜单详情 */
export function getMenuApi(id: number) {
  return request.get<ApiResult<MenuNode>>(`/menus/${id}`)
}

/** 创建菜单 */
export function createMenuApi(data: MenuDTO) {
  return request.post<ApiResult<MenuNode>>('/menus', data)
}

/** 更新菜单 */
export function updateMenuApi(id: number, data: MenuDTO) {
  return request.put<ApiResult<MenuNode>>(`/menus/${id}`, data)
}

/** 删除菜单 */
export function deleteMenuApi(id: number) {
  return request.delete<ApiResult<null>>(`/menus/${id}`)
}

/** 分页查询菜单列表 */
export function getMenuListApi(params: { page?: number; size?: number; menuName?: string }) {
  return request.get<ApiResult<MenuNode[]>>('/menus', { params })
}

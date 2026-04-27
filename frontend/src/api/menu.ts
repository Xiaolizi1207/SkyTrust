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

/** 获取当前用户有权限的菜单树 */
export function getUserMenusApi() {
  return request.get<ApiResult<MenuNode[]>>('/menus/current/tree')
}

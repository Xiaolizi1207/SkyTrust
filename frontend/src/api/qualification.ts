import request from '@/utils/request'
import type { ApiResult } from '@/types/api'

export interface QualificationDTO {
  company: string
  contact: string
  phone: string
  email: string
  scale: string
  reason: string
}

export function submitQualificationApi(data: QualificationDTO) {
  return request.post<ApiResult<{ status: string; message: string }>>('/qualification', data)
}

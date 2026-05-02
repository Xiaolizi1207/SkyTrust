import request from '@/utils/request'
import type {
  ApiResult,
  ChatMessage,
  FlightParams,
  PreFlightChecklist,
} from '@/types/api'

/** 发送聊天消息，获取机器人回复及可选的元数据 */
export function sendChatMessage(
  message: string,
  conversationId?: string,
) {
  const payload: any = { message }
  if (conversationId) payload.conversationId = conversationId
  return request.post<ApiResult<{
    reply: string
    flightParams?: FlightParams
    checklist?: PreFlightChecklist
    dangerousCommand?: boolean
    conversationId: string
  }>>('/ai/chat', payload)
}

/** 确认危险命令执行请求 */
export function confirmDangerousCommand(conversationId: string, commandId: string) {
  return request.post<ApiResult<void>>('/ai/dangerous-commands/confirm', {
    conversationId,
    commandId,
  })
}

/** 发送飞行参数给 AI 服务进行分析/建议 */
export function sendFlightParams(deviceId: string, params: FlightParams) {
  return request.post<ApiResult<void>>('/ai/flight-params', {
    deviceId,
    params,
  })
}

/** 获取某次对话的历史消息 */
export function getChatHistory(conversationId: string) {
  return request.get<ApiResult<ChatMessage[]>>('/ai/chat/history', {
    params: { conversationId },
  })
}

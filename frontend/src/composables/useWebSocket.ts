import { ref, onMounted, onUnmounted } from 'vue'
import type { WsMessage, DeviceTelemetry } from '@/types/api'

export function useWebSocket() {
  const connected = ref(false)
  const telemetry = ref<Map<number, DeviceTelemetry>>(new Map())
  const error = ref<string | null>(null)
  const lastMessage = ref<WsMessage | null>(null)

  let ws: WebSocket | null = null
  let reconnectTimer: ReturnType<typeof setTimeout> | null = null
  let reconnectAttempts = 0
  const maxReconnectDelay = 30000

  function connect() {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    const host = window.location.hostname
    const url = `${protocol}//${host}:9090/ws/device-updates`

    try {
      ws = new WebSocket(url)
    } catch (e) {
      error.value = 'WebSocket not supported'
      scheduleReconnect()
      return
    }

    ws.onopen = () => {
      connected.value = true
      error.value = null
      reconnectAttempts = 0
    }

    ws.onmessage = (event) => {
      try {
        const msg: WsMessage = JSON.parse(event.data)
        lastMessage.value = msg
        if (msg.type === 'device_update' && msg.data) {
          telemetry.value.set(msg.deviceId, msg.data)
          // trigger reactivity
          telemetry.value = new Map(telemetry.value)
        }
      } catch {
        // ignore malformed messages
      }
    }

    ws.onclose = () => {
      connected.value = false
      scheduleReconnect()
    }

    ws.onerror = () => {
      error.value = 'WebSocket connection error'
    }
  }

  function scheduleReconnect() {
    if (reconnectTimer) return
    const delay = Math.min(1000 * Math.pow(2, reconnectAttempts), maxReconnectDelay)
    reconnectAttempts++
    reconnectTimer = setTimeout(() => {
      reconnectTimer = null
      connect()
    }, delay)
  }

  function disconnect() {
    if (reconnectTimer) {
      clearTimeout(reconnectTimer)
      reconnectTimer = null
    }
    if (ws) {
      ws.close()
      ws = null
    }
  }

  onMounted(() => connect())
  onUnmounted(() => disconnect())

  return { connected, telemetry, error, lastMessage, disconnect }
}

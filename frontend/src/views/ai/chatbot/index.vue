<template>
  <section class="chatbot-page page-container">
    <div class="chat-layout">
      <!-- Left Panel: Conversation List -->
      <aside class="conv-panel card">
        <div class="conv-header">
          <h3>💬 对话记录</h3>
          <button class="btn-icon" @click="startNewConversation" title="新建对话">＋</button>
        </div>
        <div v-if="conversations.length === 0" class="empty-state">暂无对话记录</div>
        <div v-for="conv in conversations" :key="conv.id" class="conv-item" :class="{ active: conv.id === activeConvId }" @click="selectConversation(conv.id)">
          <span class="conv-title">{{ conv.title }}</span>
          <span class="conv-time">{{ formatTime(conv.updatedAt) }}</span>
        </div>
      </aside>

      <!-- Main Chat Area -->
      <main class="chat-main card">
        <div v-if="!activeConvId" class="empty-state welcome">
          <h2>🤖 SkyTrust AI 助手</h2>
          <p>选择或新建对话开始交互。我可以帮你：</p>
          <ul>
            <li>📋 生成飞行前检查清单</li>
            <li>🎯 推荐拍摄参数（航线、云台、曝光）</li>
            <li>⚠️ 危险指令二次确认</li>
            <li>📤 一键下发参数到无人机</li>
          </ul>
        </div>
        <template v-else>
          <!-- Message List -->
          <div class="message-list" ref="msgContainer">
            <div v-if="loadingHistory" class="state">加载历史消息…</div>
            <div v-for="(msg, idx) in messages" :key="idx" class="message" :class="msg.role">
              <div class="msg-avatar">{{ msg.role === 'assistant' ? '🤖' : '👤' }}</div>
              <div class="msg-bubble" :class="msg.role">
                <div class="msg-content">{{ msg.content }}</div>

                <!-- Pre-flight Checklist (assistant only) -->
                <div v-if="msg.checklist?.length" class="msg-checklist">
                  <div class="checklist-title">📋 飞行前检查清单</div>
                  <label v-for="(item, ci) in msg.checklist" :key="ci" class="checklist-item" :class="{ critical: item.critical }">
                    <input type="checkbox" v-model="item.checked" />
                    <span>{{ item.item }}</span>
                    <span v-if="item.critical" class="critical-tag">⚠️ 关键</span>
                  </label>
                </div>

                <!-- Flight Parameters (assistant only) -->
                <div v-if="msg.flightParams" class="msg-params">
                  <div class="params-title">🎯 飞行参数</div>
                  <div class="params-grid">
                    <div class="param-item"><span class="param-key">航线点数</span><span>{{ msg.flightParams.route?.length ?? 0 }}</span></div>
                    <div class="param-item"><span class="param-key">云台俯仰</span><span>{{ msg.flightParams.gimbalPitch }}°</span></div>
                    <div class="param-item"><span class="param-key">曝光</span><span>{{ msg.flightParams.exposure }} EV</span></div>
                    <div class="param-item"><span class="param-key">速度</span><span>{{ msg.flightParams.speed }} m/s</span></div>
                  </div>
                </div>

                <!-- Dangerous Command Warning -->
                <div v-if="msg.dangerousCommand" class="danger-warning">
                  <span class="danger-icon">⚠️</span>
                  <div>
                    <strong>危险指令警告</strong>
                    <p>此操作可能涉及超视距飞行或超风速操作，请确认您已了解风险。</p>
                    <button class="btn danger" @click="confirmDanger(msg.commandId!)">确认执行</button>
                  </div>
                </div>
              </div>
              <span class="msg-time">{{ formatTime(msg.timestamp) }}</span>
            </div>

            <!-- Typing Indicator -->
            <div v-if="waitingReply" class="message assistant">
              <div class="msg-avatar">🤖</div>
              <div class="msg-bubble assistant typing"><span class="dot"></span><span class="dot"></span><span class="dot"></span></div>
            </div>
          </div>

          <!-- Input Area -->
          <div class="input-area">
            <textarea v-model="inputText" class="input" rows="2" placeholder="输入消息，例如：我想拍一个环绕湖面的落日镜头…" @keydown.enter.exact.prevent="sendMessage" :disabled="waitingReply"></textarea>
            <button class="btn primary" @click="sendMessage" :disabled="!inputText.trim() || waitingReply">发送</button>
          </div>
        </template>
      </main>

      <!-- Right Panel: Flight Params -->
      <aside v-if="activeParams && activeConvId" class="params-panel card">
        <div class="params-panel-header">
          <h3>🎛️ 当前飞行参数</h3>
          <button class="btn-icon" @click="activeParams = null">✕</button>
        </div>
        <div class="params-detail">
          <div v-if="activeParams.route?.length" class="param-block">
            <strong>航线 ({{ activeParams.route.length }} 点)</strong>
            <div v-for="(wp, wi) in activeParams.route.slice(0, 4)" :key="wi" class="waypoint">
              <span>{{ wi + 1 }}. ({{ wp.lat.toFixed(4) }}, {{ wp.lon.toFixed(4) }}, {{ wp.alt }}m)</span>
            </div>
          </div>
          <div class="param-block">
            <div class="param-line"><span>云台俯仰</span><strong>{{ activeParams.gimbalPitch }}°</strong></div>
            <div class="param-line"><span>曝光</span><strong>{{ activeParams.exposure }} EV</strong></div>
            <div class="param-line"><span>速度</span><strong>{{ activeParams.speed }} m/s</strong></div>
          </div>
        </div>
        <div class="params-actions">
          <div class="form-group">
            <label>设备 ID</label>
            <select v-model="sendDeviceId" class="input">
              <option value="">选择设备…</option>
              <option value="drone-001">drone-001</option>
              <option value="drone-002">drone-002</option>
            </select>
          </div>
          <button class="btn primary" @click="doSendParams" :disabled="!sendDeviceId || sendingParams">
            {{ sendingParams ? '下发中…' : '📤 下发到无人机' }}
          </button>
        </div>
      </aside>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref, reactive, nextTick, onMounted } from 'vue'
import type { ChatMessage, FlightParams, PreFlightChecklist } from '@/types/api'
import { sendChatMessage, confirmDangerousCommand, sendFlightParams, getChatHistory } from '@/api/ai'

interface Conv {
  id: string; title: string; updatedAt: number;
}
interface UIMessage extends ChatMessage {
  checklist?: PreFlightChecklist
  flightParams?: FlightParams
  dangerousCommand?: boolean
  commandId?: string
}

const conversations = ref<Conv[]>([])
const activeConvId = ref('')
const messages = ref<UIMessage[]>([])
const inputText = ref('')
const waitingReply = ref(false)
const loadingHistory = ref(false)
const msgContainer = ref<HTMLDivElement>()
const activeParams = ref<FlightParams | null>(null)
const sendDeviceId = ref('')
const sendingParams = ref(false)

function formatTime(ts: string | number) {
  if (!ts) return ''
  const d = new Date(typeof ts === 'number' && ts < 1e12 ? ts * 1000 : ts)
  return d.toLocaleString('zh-CN', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}

function scrollToBottom() {
  nextTick(() => {
    if (msgContainer.value) msgContainer.value.scrollTop = msgContainer.value.scrollHeight
  })
}

function startNewConversation() {
  const id = `conv-${Date.now()}`
  conversations.value.unshift({ id, title: '新对话', updatedAt: Date.now() })
  activeConvId.value = id
  messages.value = []
  activeParams.value = null
}

async function selectConversation(id: string) {
  activeConvId.value = id
  messages.value = []
  activeParams.value = null
  loadingHistory.value = true
  try {
    const res = await getChatHistory(id)
    const history = res?.data ?? []
    messages.value = history.map(m => ({ ...m } as UIMessage))
  } catch { /* silent */ }
  finally { loadingHistory.value = false }
  scrollToBottom()
}

async function sendMessage() {
  const text = inputText.value.trim()
  if (!text || waitingReply.value) return
  const userMsg: UIMessage = { role: 'user', content: text, timestamp: Date.now() }
  messages.value.push(userMsg)
  inputText.value = ''
  waitingReply.value = true
  scrollToBottom()

  if (!conversations.value.find(c => c.id === activeConvId.value)) {
    startNewConversation()
  }

  try {
    const res = await sendChatMessage(text, activeConvId.value || undefined)
    const data = res?.data
    const reply: UIMessage = {
      role: 'assistant',
      content: data?.reply ?? '抱歉，我无法处理该请求。',
      timestamp: Date.now(),
      checklist: data?.checklist,
      flightParams: data?.flightParams,
      dangerousCommand: data?.dangerousCommand,
      commandId: data?.conversationId || '',
    }
    messages.value.push(reply)
    if (data?.flightParams) activeParams.value = data.flightParams
    const conv = conversations.value.find(c => c.id === activeConvId.value)
    if (conv) { conv.title = text.slice(0, 20); conv.updatedAt = Date.now() }
  } catch {
    messages.value.push({ role: 'assistant', content: '❌ 网络错误，请稍后重试。', timestamp: Date.now() })
  } finally {
    waitingReply.value = false
    scrollToBottom()
  }
}

async function confirmDanger(cmdId: string) {
  try {
    await confirmDangerousCommand(activeConvId.value, cmdId)
    messages.value.push({ role: 'assistant', content: '✅ 危险指令已确认执行。', timestamp: Date.now() })
  } catch {
    messages.value.push({ role: 'assistant', content: '❌ 确认失败。', timestamp: Date.now() })
  }
  scrollToBottom()
}

async function doSendParams() {
  if (!sendDeviceId.value || !activeParams.value) return
  sendingParams.value = true
  try {
    await sendFlightParams(sendDeviceId.value, activeParams.value)
    messages.value.push({ role: 'assistant', content: `✅ 飞行参数已下发至设备 ${sendDeviceId.value}。`, timestamp: Date.now() })
  } catch {
    messages.value.push({ role: 'assistant', content: '❌ 参数下发失败，请检查设备连接。', timestamp: Date.now() })
  } finally {
    sendingParams.value = false
    scrollToBottom()
  }
}

onMounted(() => {
  // Load existing conversations from localStorage for demo
  try {
    const saved = localStorage.getItem('skytrust-conversations')
    if (saved) conversations.value = JSON.parse(saved)
  } catch {}
})
</script>

<style scoped>
.chatbot-page { padding: 16px 0; height: calc(100vh - 120px); }
.chat-layout { display: flex; gap: 16px; height: 100%; }

/* Left panel */
.conv-panel { width: 240px; flex-shrink: 0; display: flex; flex-direction: column; padding: 12px; overflow-y: auto; }
.conv-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.conv-header h3 { margin: 0; font-size: 15px; }
.conv-item { padding: 10px; border-radius: 8px; cursor: pointer; margin-bottom: 4px; }
.conv-item:hover, .conv-item.active { background: var(--card-bg, #f0f0f0); }
.conv-title { display: block; font-size: 13px; font-weight: 500; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.conv-time { font-size: 11px; color: var(--text-secondary, #999); }
.btn-icon { background: none; border: none; cursor: pointer; font-size: 18px; padding: 2px 6px; border-radius: 4px; }
.btn-icon:hover { background: var(--border-color, #eee); }

/* Main chat */
.chat-main { flex: 1; display: flex; flex-direction: column; padding: 0; overflow: hidden; }
.chat-main .welcome { display: flex; flex-direction: column; align-items: center; justify-content: center; height: 100%; text-align: center; gap: 12px; padding: 40px; }
.welcome h2 { font-size: 24px; }
.welcome ul { text-align: left; list-style: none; padding: 0; }
.welcome li { padding: 4px 0; }

.message-list { flex: 1; overflow-y: auto; padding: 16px; display: flex; flex-direction: column; gap: 12px; }
.message { display: flex; gap: 8px; max-width: 80%; }
.message.user { align-self: flex-end; flex-direction: row-reverse; }
.message.assistant { align-self: flex-start; }
.msg-avatar { font-size: 28px; line-height: 1; }
.msg-bubble { padding: 10px 14px; border-radius: 14px; font-size: 14px; line-height: 1.5; }
.msg-bubble.user { background: var(--primary-color, #4a90d9); color: white; border-bottom-right-radius: 4px; }
.msg-bubble.assistant { background: var(--card-bg, #f0f0f0); color: var(--text-primary, #333); border-bottom-left-radius: 4px; }
.msg-time { font-size: 10px; color: var(--text-secondary, #bbb); align-self: flex-end; }

/* Typing */
.msg-bubble.typing { display: flex; gap: 4px; padding: 14px 18px; }
.dot { width: 6px; height: 6px; background: #aaa; border-radius: 50%; animation: blink 1.4s infinite; }
.dot:nth-child(2) { animation-delay: .2s; }
.dot:nth-child(3) { animation-delay: .4s; }
@keyframes blink { 0%,60%,100% { opacity: 0.3; } 30% { opacity: 1; } }

/* Checklist in message */
.msg-checklist { margin-top: 10px; padding: 10px; background: rgba(255,255,255,0.15); border-radius: 8px; }
.checklist-title { font-weight: 600; margin-bottom: 6px; font-size: 13px; }
.checklist-item { display: flex; align-items: center; gap: 8px; padding: 3px 0; font-size: 12px; }
.checklist-item.critical { font-weight: 500; }
.critical-tag { color: #e53935; font-size: 10px; font-weight: 600; }

/* Flight Params in message */
.msg-params { margin-top: 10px; padding: 10px; background: rgba(255,255,255,0.15); border-radius: 8px; }
.params-title { font-weight: 600; margin-bottom: 6px; font-size: 13px; }
.params-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 4px 12px; }
.param-item { display: flex; justify-content: space-between; font-size: 12px; }
.param-key { opacity: 0.7; }

/* Danger warning */
.danger-warning { margin-top: 10px; padding: 12px; background: #fff3e0; border: 1px solid #ff9800; border-radius: 8px; display: flex; gap: 10px; }
.danger-icon { font-size: 20px; flex-shrink: 0; }
.danger-warning strong { color: #e65100; }
.danger-warning p { margin: 4px 0 8px; font-size: 12px; }

/* Input */
.input-area { display: flex; gap: 10px; padding: 12px 16px; border-top: 1px solid var(--border-color, #eee); background: white; }
.input-area .input { width: 100%; }
.input-area .btn { flex-shrink: 0; }

/* Right panel */
.params-panel { width: 260px; flex-shrink: 0; display: flex; flex-direction: column; padding: 12px; overflow-y: auto; }
.params-panel-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.params-panel-header h3 { margin: 0; font-size: 14px; }
.param-block { margin-bottom: 12px; }
.param-block strong { display: block; margin-bottom: 4px; font-size: 13px; }
.waypoint { font-size: 11px; font-family: monospace; padding: 2px 0; }
.param-line { display: flex; justify-content: space-between; padding: 4px 0; font-size: 13px; }
.params-actions { margin-top: auto; display: flex; flex-direction: column; gap: 10px; padding-top: 12px; border-top: 1px solid var(--border-color, #eee); }

/* Common */
.card { background: white; border-radius: 12px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
.empty-state { padding: 24px; text-align: center; color: var(--text-secondary, #999); font-size: 14px; }
.badge { padding: 2px 10px; border-radius: 12px; font-size: 12px; }
</style>

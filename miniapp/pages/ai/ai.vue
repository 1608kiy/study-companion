<template>
  <view class="ai-container">
    <!-- 聊天消息 -->
    <scroll-view 
      class="chat-messages" 
      scroll-y 
      :scroll-into-view="scrollToView"
    >
      <view 
        v-for="(msg, index) in messages" 
        :key="index" 
        :id="'msg-' + index"
        class="message" 
        :class="msg.role"
      >
        <view class="message-avatar">
          <text v-if="msg.role === 'user'">我</text>
          <text v-else>AI</text>
        </view>
        <view class="message-bubble">
          <rich-text :nodes="renderMarkdown(msg.content)"></rich-text>
        </view>
      </view>
    </scroll-view>
    
    <!-- 快捷操作 -->
    <view class="quick-actions">
      <button class="action-btn" @click="handleWeeklyReport">周报</button>
      <button class="action-btn" @click="handleMonthlyReport">月报</button>
      <button class="action-btn" @click="handleFocusJudge">专注度</button>
    </view>
    
    <!-- 输入区域 -->
    <view class="input-area">
      <input 
        v-model="inputMessage" 
        class="chat-input" 
        placeholder="向AI助手提问..."
        @confirm="sendMessage"
      />
      <button class="btn-send" @click="sendMessage" :loading="loading">
        发送
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { aiApi } from '../../api/modules'
import { renderMarkdown } from '../../utils/markdown'

const CHAT_HISTORY_KEY = 'ai_chat_history'
const MAX_HISTORY = 20

const messages = ref([])
const inputMessage = ref('')
const loading = ref(false)
const scrollToView = ref('')

const saveHistory = () => {
  try {
    const historyToSave = messages.value
      .filter(m => m.role === 'user' || m.role === 'assistant')
      .slice(-MAX_HISTORY)
      .map(m => ({ role: m.role, content: m.content }))
    uni.setStorageSync(CHAT_HISTORY_KEY, JSON.stringify(historyToSave))
  } catch (e) {
    // 忽略
  }
}

const loadHistory = () => {
  try {
    const saved = uni.getStorageSync(CHAT_HISTORY_KEY)
    if (saved) {
      const parsed = JSON.parse(saved)
      if (Array.isArray(parsed) && parsed.length > 0) {
        messages.value = parsed
        return true
      }
    }
  } catch (e) {
    // 忽略
  }
  return false
}

const scrollToBottom = async () => {
  await nextTick()
  if (messages.value.length > 0) {
    scrollToView.value = 'msg-' + (messages.value.length - 1)
  }
}

const sendMessage = async () => {
  if (!inputMessage.value.trim() || loading.value) return
  
  const userMessage = {
    role: 'user',
    content: inputMessage.value.trim()
  }
  
  messages.value.push(userMessage)
  inputMessage.value = ''
  loading.value = true
  saveHistory()
  await scrollToBottom()
  
  try {
    const history = messages.value
      .filter(m => m.role === 'user' || m.role === 'assistant')
      .slice(0, -1)
      .slice(-MAX_HISTORY)
      .map(m => ({ role: m.role, content: m.content }))
    
    const res = await aiApi.chat({ question: userMessage.content, history })
    messages.value.push({
      role: 'assistant',
      content: res.data.answer
    })
    saveHistory()
  } catch (error) {
    messages.value.push({
      role: 'assistant',
      content: '抱歉，AI服务暂时不可用，请稍后再试。'
    })
  } finally {
    loading.value = false
    await scrollToBottom()
  }
}

const handleWeeklyReport = async () => {
  loading.value = true
  try {
    const res = await aiApi.generateWeeklyReport()
    messages.value.push({
      role: 'assistant',
      content: res.data.content
    })
    saveHistory()
    await scrollToBottom()
  } catch (error) {
    uni.showToast({ title: '生成周报失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

const handleMonthlyReport = async () => {
  loading.value = true
  try {
    const res = await aiApi.generateMonthlyReport()
    messages.value.push({
      role: 'assistant',
      content: res.data.content
    })
    saveHistory()
    await scrollToBottom()
  } catch (error) {
    uni.showToast({ title: '生成月报失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

const handleFocusJudge = async () => {
  loading.value = true
  try {
    const res = await aiApi.judgeFocus()
    messages.value.push({
      role: 'assistant',
      content: res.data.content
    })
    saveHistory()
    await scrollToBottom()
  } catch (error) {
    uni.showToast({ title: '专注度评估失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  const hasHistory = loadHistory()
  if (!hasHistory) {
    messages.value.push({
      role: 'assistant',
      content: '你好！我是智学伴AI助手，可以帮你分析学习数据、生成报告、回答问题。有什么可以帮你的吗？'
    })
  }
})
</script>

<style scoped>
.ai-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f8fafc;
}

.chat-messages {
  flex: 1;
  padding: 20rpx;
}

.message {
  display: flex;
  gap: 16rpx;
  margin-bottom: 24rpx;
}

.message.user {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  font-weight: 600;
  flex-shrink: 0;
}

.message.user .message-avatar {
  background: #6366f1;
  color: #fff;
}

.message.assistant .message-avatar {
  background: #10b981;
  color: #fff;
}

.message-bubble {
  max-width: 70%;
  padding: 20rpx;
  border-radius: 16rpx;
  background: #fff;
  border: 1rpx solid #e2e8f0;
}

.message.user .message-bubble {
  background: #6366f1;
  color: #fff;
}

.quick-actions {
  display: flex;
  gap: 16rpx;
  padding: 16rpx 20rpx;
  background: #fff;
  border-top: 1rpx solid #e2e8f0;
}

.action-btn {
  flex: 1;
  height: 64rpx;
  line-height: 64rpx;
  font-size: 24rpx;
  background: #f8fafc;
  color: #6366f1;
  border-radius: 12rpx;
  border: 1rpx solid #e2e8f0;
}

.input-area {
  display: flex;
  gap: 16rpx;
  padding: 20rpx;
  background: #fff;
  border-top: 1rpx solid #e2e8f0;
}

.chat-input {
  flex: 1;
  height: 80rpx;
  background: #f8fafc;
  border-radius: 12rpx;
  padding: 0 24rpx;
  font-size: 28rpx;
  border: 1rpx solid #e2e8f0;
}

.btn-send {
  width: 120rpx;
  height: 80rpx;
  line-height: 80rpx;
  background: #6366f1;
  color: #fff;
  border-radius: 12rpx;
  font-size: 28rpx;
}
</style>

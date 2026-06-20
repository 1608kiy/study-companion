<template>
  <main-layout :showTabbar="true" :currentTab="4">
    <view class="ai-page">
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
        <view v-if="loading" class="message assistant">
          <view class="message-avatar">
            <text>AI</text>
          </view>
          <view class="message-bubble loading-bubble">
            <text class="loading-text">思考中...</text>
          </view>
        </view>
      </scroll-view>
      
      <!-- 底部区域 -->
      <view class="bottom-area">
        <!-- 快捷操作 -->
        <view class="quick-actions">
          <button 
            class="action-btn-item" 
            :class="{ disabled: loading }" 
            @click="!loading && handleWeeklyReport()"
          >
            📊 周报
          </button>
          <button 
            class="action-btn-item" 
            :class="{ disabled: loading }" 
            @click="!loading && handleMonthlyReport()"
          >
            📈 月报
          </button>
          <button 
            class="action-btn-item" 
            :class="{ disabled: loading }" 
            @click="!loading && handleFocusJudge()"
          >
            🎯 专注度
          </button>
        </view>
        
        <!-- 输入区域 -->
        <view class="input-area">
          <input 
            v-model="inputMessage" 
            class="chat-input" 
            placeholder="向AI助手提问..."
            @confirm="sendMessage"
            :disabled="loading"
          />
          <button 
            class="btn-send" 
            @click="sendMessage" 
            :loading="loading"
            :disabled="!inputMessage.trim() || loading"
          >
            发送
          </button>
        </view>
      </view>
    </view>
  </main-layout>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { aiApi } from '../../api/modules'
import { renderMarkdown } from '../../utils/markdown'
import MainLayout from '../../components/main-layout.vue'
import { showShareMenu } from '../../utils/share'

const messages = ref([])
const inputMessage = ref('')
const loading = ref(false)
const scrollToView = ref('')

const loadHistory = async () => {
  try {
    const res = await aiApi.getChatHistory(20)
    if (res.data && res.data.length > 0) {
      messages.value = res.data
      return true
    }
  } catch (e) {
    console.error('加载聊天记录失败:', e)
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
  await scrollToBottom()
  
  try {
    const history = messages.value
      .filter(m => m.role === 'user' || m.role === 'assistant')
      .slice(0, -1)
      .slice(-20)
      .map(m => ({ role: m.role, content: m.content }))
    
    const res = await aiApi.chat({ question: userMessage.content, history })
    messages.value.push({
      role: 'assistant',
      content: res.data.answer
    })
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
    uni.showToast({ title: error.message || '生成周报失败', icon: 'none' })
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
    uni.showToast({ title: error.message || '生成月报失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

const handleFocusJudge = async () => {
  loading.value = true
  try {
    const res = await aiApi.judgeFocus('用户请求专注度评估')
    messages.value.push({
      role: 'assistant',
      content: res.data.content
    })
    saveHistory()
    await scrollToBottom()
  } catch (error) {
    uni.showToast({ title: error.message || '专注度评估失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

const onShareAppMessage = () => {
  return {
    title: '智学伴 AI 助手 - 智能学习分析',
    path: '/pages/ai/ai'
  }
}

const onShareTimeline = () => {
  return {
    title: '智学伴 AI 助手 - 智能学习分析',
    path: '/pages/ai/ai'
  }
}

onMounted(async () => {
  const hasHistory = await loadHistory()
  if (!hasHistory) {
    messages.value.push({
      role: 'assistant',
      content: '你好！我是智学伴AI助手，可以帮你分析学习数据、生成报告、回答问题。有什么可以帮你的吗？'
    })
  }
  await scrollToBottom()
  showShareMenu()
})
</script>

<style scoped>
.ai-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f8fafc;
}

.chat-messages {
  flex: 1;
  padding: 20rpx;
  padding-bottom: 20rpx;
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
  background: #6366f1;
  color: #fff;
}

.message.assistant .message-avatar {
  background: #10b981;
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
  border-color: #6366f1;
}

.loading-bubble {
  display: flex;
  align-items: center;
}

.loading-text {
  font-size: 26rpx;
  color: #64748b;
}

/* 底部区域 */
.bottom-area {
  background: #fff;
  border-top: 1rpx solid #e2e8f0;
  /* 安全区域 */
  padding-bottom: env(safe-area-inset-bottom, 0px);
}

.quick-actions {
  display: flex;
  gap: 16rpx;
  padding: 16rpx 20rpx;
  border-bottom: 1rpx solid #e2e8f0;
}

.action-btn-item {
  flex: 1;
  height: 80rpx;
  line-height: 80rpx;
  font-size: 26rpx;
  background: #f8fafc;
  color: #6366f1;
  border-radius: 16rpx;
  border: 1rpx solid #e2e8f0;
  font-weight: 600;
}

.action-btn-item:active {
  background: #eef2ff;
}

.action-btn-item.disabled {
  opacity: 0.5;
}

.input-area {
  display: flex;
  gap: 16rpx;
  padding: 20rpx;
}

.chat-input {
  flex: 1;
  height: 88rpx;
  background: #f8fafc;
  border-radius: 16rpx;
  padding: 0 24rpx;
  font-size: 28rpx;
  border: 1rpx solid #e2e8f0;
}

.btn-send {
  width: 120rpx;
  height: 88rpx;
  line-height: 88rpx;
  background: #6366f1;
  color: #fff;
  border-radius: 16rpx;
  font-size: 28rpx;
  font-weight: 600;
}

.btn-send:active {
  background: #4f46e5;
}
</style>

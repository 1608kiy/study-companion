<template>
  <div class="ai-container">
    <el-row :gutter="20">
      <el-col :xs="24" :md="16">
        <el-card class="chat-card">
          <template #header>
            <span class="card-title">AI学习助手</span>
          </template>
          
          <div class="chat-messages" ref="chatContainer">
            <div 
              v-for="(message, index) in messages" 
              :key="index" 
              class="message" 
              :class="message.role"
            >
              <div class="message-avatar">
                <el-avatar v-if="message.role === 'user'" :size="36" icon="User" class="user-avatar" />
                <el-avatar v-else :size="36" class="ai-avatar">AI</el-avatar>
              </div>
              <div class="message-content">
                <div class="message-bubble" v-html="renderMarkdown(message.content)"></div>
                <div class="message-time">{{ message.time }}</div>
              </div>
            </div>
          </div>
          
          <div class="chat-input">
            <el-input 
              v-model="inputMessage" 
              placeholder="向AI助手提问..." 
              @keyup.enter="sendMessage"
              :disabled="loading"
              size="large"
              class="chat-input-field"
            >
              <template #append>
                <el-button @click="sendMessage" :loading="loading" class="send-btn">
                  <el-icon><Promotion /></el-icon>
                </el-button>
              </template>
            </el-input>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :md="8">
        <el-card>
          <template #header>
            <span class="card-title">快捷功能</span>
          </template>
          
          <div class="quick-actions">
            <div class="action-card" :class="{ disabled: actionLoading }" @click="!actionLoading && generateReport('weekly')">
              <div class="action-icon blue">
                <el-icon size="20"><Document /></el-icon>
              </div>
              <span class="action-text">生成周报</span>
            </div>
            <div class="action-card" :class="{ disabled: actionLoading }" @click="!actionLoading && generateReport('monthly')">
              <div class="action-icon green">
                <el-icon size="20"><Document /></el-icon>
              </div>
              <span class="action-text">生成月报</span>
            </div>
            <div class="action-card" :class="{ disabled: actionLoading }" @click="!actionLoading && judgeFocus()">
              <div class="action-icon orange">
                <el-icon size="20"><Aim /></el-icon>
              </div>
              <span class="action-text">专注度评估</span>
            </div>
            <div class="action-card" :class="{ disabled: actionLoading }" @click="!actionLoading && generateShareImage()">
              <div class="action-icon purple">
                <el-icon size="20"><Share /></el-icon>
              </div>
              <span class="action-text">生成分享图</span>
            </div>
          </div>
        </el-card>
        
        <el-card class="report-card" v-if="currentReport">
          <template #header>
            <div class="card-header">
              <span class="card-title">{{ currentReport.type === 'weekly' ? '周报' : '月报' }}</span>
              <el-button type="primary" text @click="currentReport = null">
                <el-icon><Close /></el-icon>
              </el-button>
            </div>
          </template>
          <div class="report-content" v-html="renderMarkdown(currentReport.content)"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { aiApi } from '@/api/modules'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import { renderMarkdown } from '@/utils/markdown'

const chatContainer = ref(null)
const inputMessage = ref('')
const loading = ref(false)
const actionLoading = ref(false)
const messages = ref([])
const currentReport = ref(null)

const scrollToBottom = async () => {
  await nextTick()
  if (chatContainer.value) {
    chatContainer.value.scrollTop = chatContainer.value.scrollHeight
  }
}

const sendMessage = async () => {
  if (!inputMessage.value.trim() || loading.value) return
  
  const userMessage = {
    role: 'user',
    content: inputMessage.value.trim(),
    time: dayjs().format('HH:mm'),
  }
  
  messages.value.push(userMessage)
  inputMessage.value = ''
  loading.value = true
  
  try {
    const res = await aiApi.chat({ question: userMessage.content })
    const aiMessage = {
      role: 'assistant',
      content: res.data.answer,
      time: dayjs().format('HH:mm'),
    }
    messages.value.push(aiMessage)
  } catch (error) {
    messages.value.push({
      role: 'assistant',
      content: '抱歉，AI服务暂时不可用，请稍后再试。',
      time: dayjs().format('HH:mm'),
    })
  } finally {
    loading.value = false
    await scrollToBottom()
  }
}

const generateReport = async (type) => {
  actionLoading.value = true
  try {
    let res
    if (type === 'weekly') {
      res = await aiApi.generateWeeklyReport()
    } else {
      res = await aiApi.generateMonthlyReport()
    }
    currentReport.value = {
      type,
      content: res.data.content,
    }
    ElMessage.success('报告生成成功')
  } catch (error) {
    ElMessage.error('报告生成失败')
  } finally {
    actionLoading.value = false
  }
}

const judgeFocus = async () => {
  actionLoading.value = true
  try {
    const res = await aiApi.judgeFocus()
    messages.value.push({
      role: 'assistant',
      content: res.data.content,
      time: dayjs().format('HH:mm'),
    })
    await scrollToBottom()
  } catch (error) {
    ElMessage.error('专注度评估失败')
  } finally {
    actionLoading.value = false
  }
}

const generateShareImage = async () => {
  actionLoading.value = true
  try {
    const res = await aiApi.getShareImage()
    messages.value.push({
      role: 'assistant',
      content: '分享图片已生成！\n\n' + res.data.content,
      time: dayjs().format('HH:mm'),
    })
    await scrollToBottom()
  } catch (error) {
    ElMessage.error('分享图片生成失败')
  } finally {
    actionLoading.value = false
  }
}

onMounted(() => {
  messages.value.push({
    role: 'assistant',
    content: '你好！我是智学伴AI助手，可以帮你分析学习数据、生成报告、回答问题。有什么可以帮你的吗？',
    time: dayjs().format('HH:mm'),
  })
})
</script>

<style scoped>
.ai-container {
  padding: 0;
}

.card-title {
  font-weight: 600;
  color: var(--text-primary);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chat-card {
  height: calc(100vh - 140px);
  display: flex;
  flex-direction: column;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background-color: var(--bg-page);
  border-radius: 10px;
  margin-bottom: 16px;
}

.message {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.message.user {
  flex-direction: row-reverse;
}

.message-avatar {
  flex-shrink: 0;
}

.user-avatar {
  background: var(--primary);
}

.ai-avatar {
  background: var(--success);
}

.message-content {
  max-width: 70%;
}

.message.user .message-content {
  text-align: right;
}

.message-bubble {
  padding: 12px 16px;
  border-radius: 12px;
  background-color: white;
  border: 1px solid var(--border);
  text-align: left;
  font-size: 14px;
  line-height: 1.6;
}

.message.user .message-bubble {
  background: var(--primary);
  color: white;
  border-color: var(--primary);
}

.message-time {
  font-size: 11px;
  color: var(--text-muted);
  margin-top: 4px;
}

.chat-input {
  padding: 0;
}

.chat-input :deep(.send-btn) {
  background: var(--primary);
  border-color: var(--primary);
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.action-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 20px 12px;
  background: var(--bg-page);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
}

.action-card:hover {
  background: var(--bg-page);
  transform: translateY(-1px);
}

.action-card.disabled {
  opacity: 0.5;
  cursor: not-allowed;
  pointer-events: none;
}

.action-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.action-icon.blue { background: var(--stat-blue); }
.action-icon.green { background: var(--stat-green); }
.action-icon.orange { background: var(--stat-orange); }
.action-icon.purple { background: var(--stat-purple); }

.action-text {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-secondary);
}

.report-card {
  margin-top: 20px;
}

.report-content {
  line-height: 1.8;
  max-height: 400px;
  overflow-y: auto;
  font-size: 14px;
}

.report-content :deep(h1) {
  font-size: 20px;
  margin-bottom: 12px;
}

.report-content :deep(h2) {
  font-size: 16px;
  margin-bottom: 8px;
}

@media (max-width: 767px) {
  .chat-card {
    height: calc(100vh - 100px);
  }
  .message-content {
    max-width: 85%;
  }
  .quick-actions {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>

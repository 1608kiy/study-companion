<template>
  <main-layout :showTabbar="true" :currentTab="2">
    <scroll-view 
      class="diary-scroll" 
      scroll-y
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <!-- 日期导航 -->
      <view class="date-nav">
        <text class="date-prev" @click="prevDay">◀</text>
        <picker mode="date" :value="currentDate" @change="onDateChange">
          <text class="date-current">{{ currentDate }}</text>
        </picker>
        <text class="date-next" @click="nextDay">▶</text>
      </view>
      
      <!-- 骨架屏 -->
      <view v-if="loading" class="skeleton-wrapper">
        <view class="skeleton-card"></view>
      </view>
      
      <!-- 实际内容 -->
      <view v-else class="page-container">
        <!-- 日记详情 -->
        <view v-if="currentDiary" class="card">
          <view class="diary-header">
            <text class="diary-date">{{ currentDiary.diaryDate }}</text>
            <view class="diary-actions" v-if="currentDiary.aiGenerated && currentDiary.aiGenerateCount < 3">
              <text class="action-btn" @click="handleAIGenerate">重新生成</text>
            </view>
          </view>
          <view class="diary-content">
            <rich-text :nodes="renderMarkdown(currentDiary.content)"></rich-text>
          </view>
          <view v-if="currentDiary.aiGenerated" class="ai-badge">
            <text>AI 生成</text>
          </view>
        </view>
        
        <!-- 空状态 -->
        <view v-else class="empty-state">
          <text class="empty-icon">📝</text>
          <text class="empty-text">这一天还没有日记</text>
          <view class="empty-actions">
            <button class="btn-sm" @click="handleWrite">写日记</button>
            <button class="btn-secondary" style="width: 200rpx; height: 80rpx; line-height: 80rpx; font-size: 28rpx;" @click="handleAIGenerate" :loading="aiLoading">
              AI 生成
            </button>
          </view>
        </view>
      </view>
    </scroll-view>
    
    <!-- 写日记弹窗 -->
    <view v-if="showWriteDialog" class="dialog-mask" @click="closeDialog">
      <view class="dialog-content" @click.stop>
        <view class="dialog-header">
          <text class="dialog-title">写日记</text>
          <text class="dialog-close" @click="closeDialog">✕</text>
        </view>
        <view class="dialog-body">
          <view class="form-item">
            <text class="form-label">日记内容 *</text>
            <textarea 
              v-model="diaryForm.content" 
              class="form-textarea" 
              placeholder="记录今天的学习心得..."
              :maxlength="5000"
            />
          </view>
          <view class="form-item">
            <text class="form-label">今日总结</text>
            <input 
              v-model="diaryForm.summary" 
              class="form-input" 
              placeholder="一句话总结今天"
            />
          </view>
          <view class="form-item">
            <text class="form-label">明日计划</text>
            <input 
              v-model="diaryForm.plan" 
              class="form-input" 
              placeholder="明天打算做什么"
            />
          </view>
          <view class="form-item">
            <text class="form-label">今日反思</text>
            <input 
              v-model="diaryForm.reflection" 
              class="form-input" 
              placeholder="有什么可以改进的"
            />
          </view>
          <view class="form-item">
            <text class="form-label">遇到的问题</text>
            <input 
              v-model="diaryForm.problems" 
              class="form-input" 
              placeholder="遇到了什么困难"
            />
          </view>
        </view>
        <view class="dialog-footer">
          <button class="dialog-btn-cancel" @click="closeDialog">取消</button>
          <button class="dialog-btn-confirm" @click="submitDiary" :loading="submitting">
            创建
          </button>
        </view>
      </view>
    </view>
  </main-layout>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { diaryApi } from '../../api/modules'
import { renderMarkdown } from '../../utils/markdown'
import MainLayout from '../../components/main-layout.vue'
import { getToday, getPrevDay, getNextDay } from '../../utils/date'

const loading = ref(true)
const refreshing = ref(false)
const aiLoading = ref(false)
const submitting = ref(false)
const currentDiary = ref(null)
const currentDate = ref(getToday())
const showWriteDialog = ref(false)

const diaryForm = ref({
  content: '',
  summary: '',
  plan: '',
  reflection: '',
  problems: ''
})

const prevDay = () => {
  currentDate.value = getPrevDay(currentDate.value)
}

const nextDay = () => {
  currentDate.value = getNextDay(currentDate.value)
}

const onDateChange = (e) => {
  currentDate.value = e.detail.value
}

const loadDiary = async (date) => {
  try {
    const res = await diaryApi.getByDate(date)
    currentDiary.value = res.data
  } catch (error) {
    currentDiary.value = null
  }
}

const handleWrite = () => {
  diaryForm.value = { content: '', summary: '', plan: '', reflection: '', problems: '' }
  showWriteDialog.value = true
}

const closeDialog = () => {
  showWriteDialog.value = false
}

const submitDiary = async () => {
  if (!diaryForm.value.content?.trim()) {
    uni.showToast({ title: '请输入日记内容', icon: 'none' })
    return
  }
  
  submitting.value = true
  try {
    await diaryApi.create({ ...diaryForm.value, diaryDate: currentDate.value })
    uni.showToast({ title: '创建成功', icon: 'success' })
    closeDialog()
    await loadDiary(currentDate.value)
  } catch (error) {
    uni.showToast({ title: error.message || '操作失败', icon: 'none' })
  } finally {
    submitting.value = false
  }
}

const handleAIGenerate = async () => {
  aiLoading.value = true
  try {
    let res
    if (currentDiary.value) {
      // 重新生成现有日记
      res = await diaryApi.regenerate(currentDiary.value.id)
    } else {
      // 生成新日记
      res = await diaryApi.generate()
    }
    currentDiary.value = res.data
    uni.showToast({ title: '生成成功', icon: 'success' })
  } catch (error) {
    uni.showToast({ title: error.message || 'AI生成失败', icon: 'none' })
  } finally {
    aiLoading.value = false
  }
}

const onRefresh = async () => {
  refreshing.value = true
  try {
    loading.value = true
    await loadDiary(currentDate.value)
  } finally {
    refreshing.value = false
    loading.value = false
  }
}

watch(currentDate, (newDate) => {
  loadDiary(newDate)
})

onMounted(async () => {
  loading.value = true
  await loadDiary(currentDate.value)
  loading.value = false
})
</script>

<style scoped>
.diary-scroll {
  height: 100vh;
}

.diary-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.diary-date {
  font-size: 30rpx;
  font-weight: 600;
  color: #1e293b;
}

.diary-actions {
  display: flex;
  gap: 8rpx;
}

.diary-content {
  font-size: 28rpx;
  line-height: 1.8;
  color: #1e293b;
}

.ai-badge {
  display: inline-block;
  padding: 8rpx 16rpx;
  background: #eef2ff;
  border-radius: 8rpx;
  margin-top: 20rpx;
}

.ai-badge text {
  font-size: 22rpx;
  color: #6366f1;
}

.empty-actions {
  display: flex;
  gap: 20rpx;
  justify-content: center;
}

.skeleton-card {
  height: 400rpx;
  background: #e2e8f0;
  border-radius: 16rpx;
}
</style>

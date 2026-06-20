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
            <view class="diary-actions">
              <text class="action-btn" @click="handleEdit">编辑</text>
              <text class="action-btn delete" @click="handleDelete">删除</text>
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
            <button class="btn-primary" style="width: 200rpx; height: 80rpx; line-height: 80rpx;" @click="handleWrite">写日记</button>
            <button class="btn-secondary" style="width: 200rpx; height: 80rpx; line-height: 80rpx;" @click="handleAIGenerate" :loading="aiLoading">
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
          <text class="dialog-title">{{ isEdit ? '编辑日记' : '写日记' }}</text>
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
          <button class="btn-cancel" @click="closeDialog">取消</button>
          <button class="btn-confirm" @click="submitDiary" :loading="submitting">
            {{ isEdit ? '保存' : '创建' }}
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

const loading = ref(true)
const refreshing = ref(false)
const aiLoading = ref(false)
const submitting = ref(false)
const currentDiary = ref(null)
const currentDate = ref(new Date().toISOString().split('T')[0])
const showWriteDialog = ref(false)
const isEdit = ref(false)

const diaryForm = ref({
  content: '',
  summary: '',
  plan: '',
  reflection: '',
  problems: ''
})

const prevDay = () => {
  const date = new Date(currentDate.value)
  date.setDate(date.getDate() - 1)
  currentDate.value = date.toISOString().split('T')[0]
}

const nextDay = () => {
  const date = new Date(currentDate.value)
  date.setDate(date.getDate() + 1)
  currentDate.value = date.toISOString().split('T')[0]
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
  isEdit.value = false
  diaryForm.value = {
    content: '',
    summary: '',
    plan: '',
    reflection: '',
    problems: ''
  }
  showWriteDialog.value = true
}

const handleEdit = () => {
  if (!currentDiary.value) return
  isEdit.value = true
  diaryForm.value = {
    content: currentDiary.value.content || '',
    summary: currentDiary.value.summary || '',
    plan: currentDiary.value.plan || '',
    reflection: currentDiary.value.reflection || '',
    problems: currentDiary.value.problems || ''
  }
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
    if (isEdit.value && currentDiary.value) {
      await diaryApi.update(currentDiary.value.id, diaryForm.value)
      uni.showToast({ title: '保存成功', icon: 'success' })
    } else {
      await diaryApi.create({
        ...diaryForm.value,
        diaryDate: currentDate.value
      })
      uni.showToast({ title: '创建成功', icon: 'success' })
    }
    closeDialog()
    await loadDiary(currentDate.value)
  } catch (error) {
    uni.showToast({ title: error.message || '操作失败', icon: 'none' })
  } finally {
    submitting.value = false
  }
}

const handleDelete = () => {
  uni.showModal({
    title: '确认删除',
    content: '确定要删除这篇日记吗？',
    success: async (res) => {
      if (res.confirm && currentDiary.value) {
        try {
          await diaryApi.delete(currentDiary.value.id)
          currentDiary.value = null
          uni.showToast({ title: '删除成功', icon: 'success' })
        } catch (error) {
          uni.showToast({ title: error.message || '删除失败', icon: 'none' })
        }
      }
    }
  })
}

const handleAIGenerate = async () => {
  aiLoading.value = true
  try {
    const res = await diaryApi.generate()
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

.form-item {
  margin-bottom: 24rpx;
}

.btn-cancel {
  flex: 1;
  height: 80rpx;
  background: #f8fafc;
  color: #64748b;
  border-radius: 12rpx;
  font-size: 28rpx;
}

.btn-confirm {
  flex: 1;
  height: 80rpx;
  background: #6366f1;
  color: #fff;
  border-radius: 12rpx;
  font-size: 28rpx;
  font-weight: 600;
}

/* 骨架屏 */
.skeleton-card {
  height: 400rpx;
  background: #e2e8f0;
  border-radius: 16rpx;
}
</style>

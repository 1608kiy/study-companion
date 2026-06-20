<template>
  <main-layout :showTabbar="true" :currentTab="2">
    <view class="diary-container">
      <!-- 日记详情 -->
      <view v-if="currentDiary" class="card diary-detail">
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
        <text class="empty-text">选择日期查看日记</text>
      </view>
      
      <!-- 写日记按钮 -->
      <button class="btn-write" @click="handleWrite">
        写日记
      </button>
      
      <!-- AI生成按钮 -->
      <button class="btn-ai" @click="handleAIGenerate">
        AI 生成日记
      </button>
    </view>
  </main-layout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { diaryApi } from '../../api/modules'
import { renderMarkdown } from '../../utils/markdown'
import MainLayout from '../../components/main-layout.vue'

const currentDiary = ref(null)
const today = new Date().toISOString().split('T')[0]

const loadDiary = async (date) => {
  try {
    const res = await diaryApi.getByDate(date)
    currentDiary.value = res.data
  } catch (error) {
    currentDiary.value = null
  }
}

const handleWrite = () => {
  uni.showToast({ title: '功能开发中', icon: 'none' })
}

const handleEdit = () => {
  uni.showToast({ title: '功能开发中', icon: 'none' })
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
          console.error('删除失败:', error)
        }
      }
    }
  })
}

const handleAIGenerate = async () => {
  try {
    const res = await diaryApi.generate()
    currentDiary.value = res.data
    uni.showToast({ title: '生成成功', icon: 'success' })
  } catch (error) {
    console.error('AI生成失败:', error)
  }
}

onMounted(() => {
  loadDiary(today)
})
</script>

<style scoped>
.diary-container {
  padding: 20rpx;
  background: #f8fafc;
  min-height: 100vh;
}

.card {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  border: 1rpx solid #e2e8f0;
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
  gap: 20rpx;
}

.action-btn {
  font-size: 26rpx;
  color: #6366f1;
}

.action-btn.delete {
  color: #ef4444;
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

.empty-state {
  text-align: center;
  padding: 100rpx 0;
}

.empty-text {
  font-size: 28rpx;
  color: #94a3b8;
}

.btn-write {
  width: 100%;
  height: 88rpx;
  background: #6366f1;
  color: #fff;
  border-radius: 16rpx;
  font-size: 30rpx;
  font-weight: 600;
  margin-bottom: 20rpx;
}

.btn-ai {
  width: 100%;
  height: 88rpx;
  background: #fff;
  color: #6366f1;
  border-radius: 16rpx;
  font-size: 30rpx;
  font-weight: 600;
  border: 1rpx solid #6366f1;
}
</style>

<template>
  <main-layout :showTabbar="true" :currentTab="3">
    <view class="stats-container">
      <!-- 月份选择 -->
      <view class="month-picker">
        <text class="month-text">{{ selectedMonth }}</text>
      </view>
      
      <!-- 统计卡片 -->
      <view class="stats-row">
        <view class="stat-card stat-blue">
          <text class="stat-value">{{ stats.totalDays || 0 }}</text>
          <text class="stat-label">学习天数</text>
        </view>
        <view class="stat-card stat-green">
          <text class="stat-value">{{ formatDuration(stats.totalDuration) }}</text>
          <text class="stat-label">总学习时长</text>
        </view>
        <view class="stat-card stat-orange">
          <text class="stat-value">{{ stats.avgDuration || 0 }}</text>
          <text class="stat-label">日均时长(分钟)</text>
        </view>
        <view class="stat-card stat-purple">
          <text class="stat-value">{{ stats.maxDuration || 0 }}</text>
          <text class="stat-label">最长单日(分钟)</text>
        </view>
      </view>
      
      <!-- 科目分布 -->
      <view class="card">
        <text class="card-title">科目分布</text>
        <view v-if="subjectData.length === 0" class="empty-tip">
          <text>暂无数据</text>
        </view>
        <view v-else class="subject-list">
          <view 
            v-for="item in subjectData" 
            :key="item.name" 
            class="subject-item"
          >
            <text class="subject-name">{{ item.name }}</text>
            <view class="subject-bar">
              <view 
                class="bar-fill" 
                :style="{ width: (item.value / maxSubjectValue * 100) + '%' }"
              ></view>
            </view>
            <text class="subject-value">{{ item.value }}分钟</text>
          </view>
        </view>
      </view>
    </view>
  </main-layout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { goalApi, studyRecordApi } from '../../api/modules'
import MainLayout from '../../components/main-layout.vue'

const selectedMonth = ref(new Date().toISOString().slice(0, 7))
const stats = ref({})
const subjectData = ref([])

const maxSubjectValue = computed(() => {
  return Math.max(...subjectData.value.map(item => item.value), 1)
})

const formatDuration = (minutes) => {
  if (!minutes) return '0小时'
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  if (hours === 0) return `${mins}分钟`
  if (mins === 0) return `${hours}小时`
  return `${hours}小时${mins}分钟`
}

const loadStats = async () => {
  try {
    const res = await goalApi.getMonthlyStats({ month: selectedMonth.value })
    stats.value = res.data || {}
  } catch (error) {
    console.error('获取统计失败:', error)
  }
}

const loadSubjectStats = async () => {
  try {
    const res = await studyRecordApi.getStats()
    const subjectStats = res.data?.subjectStats || {}
    subjectData.value = Object.entries(subjectStats).map(([name, value]) => ({
      name,
      value
    }))
  } catch (error) {
    console.error('获取科目统计失败:', error)
  }
}

onMounted(async () => {
  await Promise.all([
    loadStats(),
    loadSubjectStats()
  ])
})
</script>

<style scoped>
.stats-container {
  padding: 20rpx;
  background: #f8fafc;
  min-height: 100vh;
}

.month-picker {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  border: 1rpx solid #e2e8f0;
  text-align: center;
}

.month-text {
  font-size: 30rpx;
  font-weight: 600;
  color: #1e293b;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
  margin-bottom: 20rpx;
}

.stat-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  border: 1rpx solid #e2e8f0;
}

.stat-value {
  display: block;
  font-size: 36rpx;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 8rpx;
}

.stat-label {
  display: block;
  font-size: 22rpx;
  color: #64748b;
}

.stat-blue .stat-value { color: #3b82f6; }
.stat-green .stat-value { color: #10b981; }
.stat-orange .stat-value { color: #f59e0b; }
.stat-purple .stat-value { color: #8b5cf6; }

.card {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  border: 1rpx solid #e2e8f0;
}

.card-title {
  display: block;
  font-size: 30rpx;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 20rpx;
}

.empty-tip {
  text-align: center;
  padding: 40rpx;
  color: #94a3b8;
  font-size: 26rpx;
}

.subject-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.subject-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.subject-name {
  width: 120rpx;
  font-size: 26rpx;
  color: #1e293b;
}

.subject-bar {
  flex: 1;
  height: 24rpx;
  background: #f1f5f9;
  border-radius: 12rpx;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  background: #6366f1;
  border-radius: 12rpx;
}

.subject-value {
  width: 120rpx;
  text-align: right;
  font-size: 24rpx;
  color: #64748b;
}
</style>

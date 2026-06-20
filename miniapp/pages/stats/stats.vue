<template>
  <main-layout :showTabbar="true" :currentTab="3">
    <scroll-view 
      class="stats-scroll" 
      scroll-y
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <!-- 骨架屏 -->
      <view v-if="loading" class="skeleton-wrapper">
        <view class="skeleton-month"></view>
        <view class="skeleton-stats">
          <view class="skeleton-card" v-for="i in 4" :key="i"></view>
        </view>
        <view class="skeleton-chart"></view>
      </view>
      
      <!-- 实际内容 -->
      <view v-else class="stats-container">
        <!-- 月份选择 -->
        <view class="month-picker">
          <text class="month-prev" @click="prevMonth">◀</text>
          <picker mode="date" fields="month" :value="selectedMonth" @change="onMonthChange">
            <text class="month-text">{{ selectedMonth }}</text>
          </picker>
          <text class="month-next" @click="nextMonth">▶</text>
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
    </scroll-view>
  </main-layout>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { goalApi, studyRecordApi } from '../../api/modules'
import MainLayout from '../../components/main-layout.vue'

const loading = ref(true)
const refreshing = ref(false)
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

const prevMonth = () => {
  const [year, month] = selectedMonth.value.split('-').map(Number)
  const date = new Date(year, month - 2, 1)
  selectedMonth.value = date.toISOString().slice(0, 7)
}

const nextMonth = () => {
  const [year, month] = selectedMonth.value.split('-').map(Number)
  const date = new Date(year, month, 1)
  selectedMonth.value = date.toISOString().slice(0, 7)
}

const onMonthChange = (e) => {
  selectedMonth.value = e.detail.value
}

const loadStats = async () => {
  try {
    const res = await goalApi.getMonthlyStats({ month: selectedMonth.value })
    stats.value = res.data || {}
  } catch (error) {
    uni.showToast({ title: '获取统计数据失败', icon: 'none' })
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
    uni.showToast({ title: '获取科目统计失败', icon: 'none' })
  }
}

const loadData = async () => {
  loading.value = true
  try {
    await Promise.all([
      loadStats(),
      loadSubjectStats()
    ])
  } finally {
    loading.value = false
  }
}

const onRefresh = async () => {
  refreshing.value = true
  try {
    await loadData()
  } finally {
    refreshing.value = false
  }
}

watch(selectedMonth, () => {
  loadStats()
})

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.stats-scroll {
  height: 100vh;
}

.stats-container {
  padding: 20rpx;
  background: #f8fafc;
  min-height: 100vh;
}

/* 骨架屏 */
.skeleton-wrapper {
  padding: 20rpx;
  animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.skeleton-month {
  height: 80rpx;
  background: #e2e8f0;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
}

.skeleton-stats {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
  margin-bottom: 20rpx;
}

.skeleton-card {
  height: 120rpx;
  background: #e2e8f0;
  border-radius: 16rpx;
}

.skeleton-chart {
  height: 300rpx;
  background: #e2e8f0;
  border-radius: 16rpx;
}

/* 月份选择 */
.month-picker {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 40rpx;
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  border: 1rpx solid #e2e8f0;
}

.month-prev, .month-next {
  font-size: 28rpx;
  color: #6366f1;
  padding: 10rpx;
}

.month-text {
  font-size: 30rpx;
  font-weight: 600;
  color: #1e293b;
}

/* 统计卡片 */
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

/* 卡片 */
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

/* 科目分布 */
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

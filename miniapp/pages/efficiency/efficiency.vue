<template>
  <main-layout :showTabbar="false">
    <scroll-view class="page-scroll" scroll-y>
      <view class="page-container">
        <view class="page-header">
          <text class="page-title">学习效率分析</text>
          <text class="page-subtitle">基于最近30天数据</text>
        </view>
        
        <!-- 总体统计 -->
        <view class="stats-grid">
          <view class="stat-item">
            <text class="stat-value">{{ analysis.totalSessions || 0 }}</text>
            <text class="stat-label">学习次数</text>
          </view>
          <view class="stat-item">
            <text class="stat-value">{{ analysis.avgFocusLevel || 0 }}</text>
            <text class="stat-label">平均专注度</text>
          </view>
          <view class="stat-item">
            <text class="stat-value">{{ bestHourText }}</text>
            <text class="stat-label">最佳时段</text>
          </view>
          <view class="stat-item">
            <text class="stat-value">{{ bestDayText }}</text>
            <text class="stat-label">最佳学习日</text>
          </view>
        </view>
        
        <!-- 最佳学习时段 -->
        <view class="card">
          <text class="card-title">学习时段分布</text>
          <view class="hourly-chart">
            <view 
              v-for="hour in 24" 
              :key="hour" 
              class="hour-bar"
            >
              <view 
                class="bar" 
                :style="{ height: getHourlyHeight(hour - 1) + '%' }"
              ></view>
              <text class="hour-label" v-if="(hour - 1) % 3 === 0">{{ hour - 1 }}时</text>
            </view>
          </view>
        </view>
        
        <!-- 科目分布 -->
        <view class="card">
          <text class="card-title">科目时长分布</text>
          <view class="subject-list">
            <view 
              v-for="(duration, name) in analysis.subjectDistribution" 
              :key="name" 
              class="subject-item"
            >
              <view class="subject-info">
                <text class="subject-name">{{ name }}</text>
                <text class="subject-duration">{{ duration }}分钟</text>
              </view>
              <view class="progress-bar">
                <view 
                  class="progress-fill" 
                  :style="{ width: getSubjectPercent(duration) + '%' }"
                ></view>
              </view>
            </view>
          </view>
        </view>
        
        <!-- 学习建议 -->
        <view class="card">
          <text class="card-title">学习建议</text>
          <view class="advice-list">
            <view class="advice-item" v-if="analysis.bestHours?.length">
              <text class="advice-icon">⏰</text>
              <text class="advice-text">您在 {{ bestHoursText }} 时段学习效率最高</text>
            </view>
            <view class="advice-item" v-if="bestFocusSubject">
              <text class="advice-icon">⭐</text>
              <text class="advice-text">学习 {{ bestFocusSubject }} 时专注度最高</text>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>
  </main-layout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { studyRecordApi } from '../../api/modules'
import MainLayout from '../../components/main-layout.vue'

const analysis = ref({})

const bestHourText = computed(() => {
  const hour = analysis.value.bestHourOfDay
  if (hour == null) return '-'
  return `${hour}:00`
})

const bestDayText = computed(() => {
  const day = analysis.value.bestDayOfWeek
  if (day == null) return '-'
  const days = ['', '周一', '周二', '周三', '周四', '周五', '周六', '周日']
  return days[day] || '-'
})

const bestHoursText = computed(() => {
  const hours = analysis.value.bestHours || []
  return hours.map(h => `${h}:00`).join('、')
})

const bestFocusSubject = computed(() => {
  const focus = analysis.value.subjectFocus || {}
  const entries = Object.entries(focus)
  if (!entries.length) return null
  return entries.sort((a, b) => b[1] - a[1])[0][0]
})

const maxHourlyValue = computed(() => {
  const dist = analysis.value.hourlyDistribution || {}
  return Math.max(...Object.values(dist), 1)
})

const maxSubjectValue = computed(() => {
  const dist = analysis.value.subjectDistribution || {}
  return Math.max(...Object.values(dist), 1)
})

const getHourlyHeight = (hour) => {
  const dist = analysis.value.hourlyDistribution || {}
  return ((dist[hour] || 0) / maxHourlyValue.value) * 100
}

const getSubjectPercent = (duration) => {
  return (duration / maxSubjectValue.value) * 100
}

onMounted(async () => {
  try {
    const res = await studyRecordApi.getEfficiency()
    analysis.value = res.data || {}
  } catch (error) {
    uni.showToast({ title: '获取数据失败', icon: 'none' })
  }
})
</script>

<style scoped>
.page-scroll {
  height: 100vh;
}

.page-header {
  margin-bottom: 30rpx;
}

.page-title {
  display: block;
  font-size: 40rpx;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 8rpx;
}

.page-subtitle {
  display: block;
  font-size: 26rpx;
  color: #64748b;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
  margin-bottom: 30rpx;
}

.stat-item {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  text-align: center;
  border: 1rpx solid #e2e8f0;
}

.stat-value {
  display: block;
  font-size: 40rpx;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 8rpx;
}

.stat-label {
  display: block;
  font-size: 24rpx;
  color: #64748b;
}

.hourly-chart {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  height: 200rpx;
  padding: 0 8rpx;
}

.hour-bar {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4rpx;
}

.bar {
  width: 16rpx;
  min-height: 4rpx;
  background: #6366f1;
  border-radius: 4rpx 4rpx 0 0;
}

.hour-label {
  font-size: 18rpx;
  color: #64748b;
}

.subject-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.subject-item {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.subject-info {
  display: flex;
  justify-content: space-between;
}

.subject-name {
  font-size: 26rpx;
  color: #1e293b;
}

.subject-duration {
  font-size: 24rpx;
  color: #64748b;
}

.progress-bar {
  height: 16rpx;
  background: #e2e8f0;
  border-radius: 8rpx;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: #10b981;
  border-radius: 8rpx;
}

.advice-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.advice-item {
  display: flex;
  align-items: flex-start;
  gap: 12rpx;
  padding: 16rpx;
  background: #f8fafc;
  border-radius: 12rpx;
}

.advice-icon {
  font-size: 32rpx;
}

.advice-text {
  font-size: 26rpx;
  color: #64748b;
  line-height: 1.6;
}
</style>

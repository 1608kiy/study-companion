<template>
  <main-layout :showTabbar="true" :currentTab="0">
    <scroll-view 
      class="home-scroll" 
      scroll-y 
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <!-- 骨架屏 -->
      <view v-if="loading" class="skeleton-wrapper">
        <view class="skeleton-welcome"></view>
        <view class="stats-row">
          <view class="skeleton-card" v-for="i in 4" :key="i"></view>
        </view>
        <view class="skeleton-card-large"></view>
      </view>
      
      <!-- 实际内容 -->
      <view v-else class="page-container">
        <!-- 欢迎语 -->
        <view class="welcome-section">
          <text class="welcome-text">欢迎回来，{{ userStore.nickname }} 👋</text>
          <text class="welcome-sub">今天也要加油学习哦</text>
        </view>
        
        <!-- 统计卡片 -->
        <view class="stats-row">
          <view class="stat-card stat-blue">
            <text class="stat-value">{{ todayStats.duration || 0 }}</text>
            <text class="stat-label">今日学习(分钟)</text>
          </view>
          <view class="stat-card stat-green">
            <text class="stat-value">{{ todayStats.streak || 0 }}</text>
            <text class="stat-label">连续打卡(天)</text>
          </view>
          <view class="stat-card stat-orange">
            <text class="stat-value">{{ todayStats.diaryCount || 0 }}</text>
            <text class="stat-label">本周日记(篇)</text>
          </view>
          <view class="stat-card stat-purple">
            <text class="stat-value">{{ todayStats.goalProgress || 0 }}%</text>
            <text class="stat-label">目标完成度</text>
          </view>
        </view>
        
        <!-- 打卡区域 -->
        <view class="card">
          <text class="card-title">今日打卡</text>
          <view class="checkin-content">
            <view class="progress-ring" :style="{ borderColor: todayCheckedIn ? '#10b981' : '#e2e8f0' }">
              <text class="progress-value">{{ checkInProgress }}%</text>
              <text class="progress-label">今日完成</text>
            </view>
            <button 
              v-if="!todayCheckedIn" 
              class="btn-sm" 
              @click="handleCheckIn"
            >
              打卡
            </button>
            <view v-else class="tag-success">
              <text>✓ 已打卡</text>
            </view>
          </view>
        </view>
        
        <!-- 学习趋势 -->
        <view class="card">
          <text class="card-title">学习趋势</text>
          <view class="chart-container">
            <view class="trend-bars">
              <view 
                v-for="(value, index) in weeklyData" 
                :key="index"
                class="bar-item"
              >
                <view class="bar" :style="{ height: (value / maxWeeklyValue * 100) + '%' }"></view>
                <text class="bar-label">{{ weekDays[index] }}</text>
              </view>
            </view>
          </view>
        </view>
        
        <!-- 设置入口 -->
        <view class="settings-entry" @click="goSettings">
          <text>⚙️ 设置</text>
        </view>
      </view>
    </scroll-view>
  </main-layout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onShareAppMessage, onShareTimeline } from '@dcloudio/uni-app'
import { useUserStore } from '../../store/user'
import { checkInApi, studyRecordApi, diaryApi } from '../../api/modules'
import MainLayout from '../../components/main-layout.vue'
import { generateCheckInShare, showShareMenu } from '../../utils/share'
import { getCurrentMonth } from '../../utils/date'

const userStore = useUserStore()
const dailyGoal = computed(() => userStore.userInfo?.dailyGoal || 120)

const loading = ref(true)
const refreshing = ref(false)

const todayStats = ref({
  duration: 0,
  streak: 0,
  diaryCount: 0,
  goalProgress: 0
})

const checkInStatusData = ref({
  totalDuration: 0,
  streak: 0,
  isCompleted: false
})

const weeklyData = ref([0, 0, 0, 0, 0, 0, 0])

// 动态生成最近7天的标签
const weekDays = computed(() => {
  const days = []
  const dayNames = ['日', '一', '二', '三', '四', '五', '六']
  for (let i = 6; i >= 0; i--) {
    const date = new Date()
    date.setDate(date.getDate() - i)
    days.push(dayNames[date.getDay()])
  }
  return days
})

const maxWeeklyValue = computed(() => {
  return Math.max(...weeklyData.value, 1)
})

const todayCheckedIn = computed(() => checkInStatusData.value.isCompleted)
const checkInProgress = computed(() => {
  const progress = Math.min(100, (checkInStatusData.value.totalDuration / dailyGoal.value) * 100)
  return Math.round(progress)
})

const handleCheckIn = async () => {
  try {
    await checkInApi.checkIn()
    uni.showToast({ title: '打卡成功！', icon: 'success' })
    await loadCheckInStatus()
  } catch (error) {
    uni.showToast({ title: error.message || '打卡失败', icon: 'none' })
  }
}

const goSettings = () => {
  uni.navigateTo({ url: '/pages/settings/settings' })
}

const loadCheckInStatus = async () => {
  try {
    const res = await checkInApi.getTodayStatus()
    checkInStatusData.value = res.data
  } catch (error) {
    uni.showToast({ title: '获取打卡状态失败', icon: 'none' })
  }
}

const loadTodayStats = async () => {
  try {
    const [statsRes, diaryRes] = await Promise.all([
      studyRecordApi.getStats(),
      diaryApi.getList({ month: getCurrentMonth() })
    ])
    const diaryCount = (diaryRes.data || []).length
    todayStats.value = {
      duration: statsRes.data.todayDuration || 0,
      streak: statsRes.data.currentStreak || 0,
      diaryCount,
      goalProgress: Math.min(100, Math.round(((statsRes.data.todayDuration || 0) / dailyGoal.value) * 100))
    }
    weeklyData.value = statsRes.data.weeklyDurations || [0, 0, 0, 0, 0, 0, 0]
  } catch (error) {
    uni.showToast({ title: '获取统计数据失败', icon: 'none' })
  }
}

const loadData = async () => {
  loading.value = true
  try {
    await Promise.all([
      loadCheckInStatus(),
      loadTodayStats()
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

// 注册分享回调
onShareAppMessage(() => {
  return generateCheckInShare(todayStats.value)
})

onShareTimeline(() => {
  return generateCheckInShare(todayStats.value)
})

onMounted(() => {
  loadData()
  showShareMenu()
})
</script>

<style scoped>
.home-scroll {
  height: 100vh;
}

.welcome-section {
  margin-bottom: 30rpx;
}

.welcome-text {
  display: block;
  font-size: 40rpx;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 8rpx;
}

.welcome-sub {
  display: block;
  font-size: 26rpx;
  color: #64748b;
}

.checkin-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 30rpx;
}

.progress-ring {
  width: 200rpx;
  height: 200rpx;
  border-radius: 50%;
  border: 16rpx solid #e2e8f0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.progress-value {
  font-size: 40rpx;
  font-weight: 700;
  color: #1e293b;
}

.progress-label {
  font-size: 20rpx;
  color: #64748b;
}

.chart-container {
  height: 300rpx;
}

.trend-bars {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  height: 250rpx;
  padding: 0 10rpx;
}

.bar-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
}

.bar {
  width: 40rpx;
  min-height: 10rpx;
  background: #6366f1;
  border-radius: 8rpx 8rpx 0 0;
}

.bar-label {
  font-size: 20rpx;
  color: #64748b;
}

.settings-entry {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24rpx;
  background: #fff;
  border-radius: 16rpx;
  border: 1rpx solid #e2e8f0;
  margin-top: 20rpx;
}

.settings-entry:active {
  background: #f8fafc;
}

.settings-entry text {
  font-size: 28rpx;
  color: #6366f1;
}

.skeleton-welcome {
  height: 80rpx;
  background: #e2e8f0;
  border-radius: 12rpx;
  margin-bottom: 30rpx;
}

.skeleton-card {
  height: 120rpx;
  background: #e2e8f0;
  border-radius: 16rpx;
}

.skeleton-card-large {
  height: 300rpx;
  background: #e2e8f0;
  border-radius: 16rpx;
}
</style>

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
        <view class="skeleton-stats">
          <view class="skeleton-card" v-for="i in 4" :key="i"></view>
        </view>
        <view class="skeleton-card-large"></view>
      </view>
      
      <!-- 实际内容 -->
      <view v-else class="home-container">
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
        <view class="card checkin-card">
          <text class="card-title">今日打卡</text>
          <view class="checkin-content">
            <view class="progress-ring">
              <text class="progress-value">{{ checkInProgress }}%</text>
              <text class="progress-label">今日完成</text>
            </view>
            <button 
              v-if="!todayCheckedIn" 
              class="btn-checkin" 
              @click="handleCheckIn"
            >
              打卡
            </button>
            <view v-else class="checked-tag">
              <text>✓ 已打卡</text>
            </view>
          </view>
        </view>
        
        <!-- 学习趋势 -->
        <view class="card chart-card">
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
import { useUserStore } from '../../store/user'
import { checkInApi, studyRecordApi, diaryApi } from '../../api/modules'
import MainLayout from '../../components/main-layout.vue'

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
const weekDays = ['一', '二', '三', '四', '五', '六', '日']

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
    console.error('获取打卡状态失败:', error)
  }
}

const loadTodayStats = async () => {
  try {
    const [statsRes, diaryRes] = await Promise.all([
      studyRecordApi.getStats(),
      diaryApi.getList({ month: new Date().toISOString().slice(0, 7) })
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

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.home-scroll {
  height: 100vh;
}

.home-container {
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

.skeleton-welcome {
  height: 80rpx;
  background: #e2e8f0;
  border-radius: 12rpx;
  margin-bottom: 30rpx;
}

.skeleton-stats {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
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

/* 欢迎语 */
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

/* 统计卡片 */
.stats-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
  margin-bottom: 30rpx;
}

.stat-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  border: 1rpx solid #e2e8f0;
}

.stat-value {
  display: block;
  font-size: 44rpx;
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

/* 打卡 */
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

.btn-checkin {
  width: 240rpx;
  height: 80rpx;
  background: #6366f1;
  color: #fff;
  border-radius: 16rpx;
  font-size: 28rpx;
  font-weight: 600;
}

.checked-tag {
  padding: 16rpx 40rpx;
  background: #ecfdf5;
  border-radius: 16rpx;
  color: #10b981;
  font-size: 28rpx;
}

/* 趋势图 */
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

/* 设置入口 */
.settings-entry {
  text-align: center;
  padding: 24rpx;
  background: #fff;
  border-radius: 16rpx;
  border: 1rpx solid #e2e8f0;
  margin-top: 20rpx;
}

.settings-entry text {
  font-size: 28rpx;
  color: #6366f1;
}
</style>

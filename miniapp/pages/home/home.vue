<template>
  <view class="home-container">
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
        <!-- 这里使用 echarts-for-weixin -->
        <text class="chart-placeholder">近7天学习趋势</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '../../store/user'
import { checkInApi, studyRecordApi, diaryApi } from '../../api/modules'

const userStore = useUserStore()
const dailyGoal = computed(() => userStore.userInfo?.dailyGoal || 120)

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
    console.error('打卡失败:', error)
  }
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
      diaryApi.getList()
    ])
    const diaryCount = (diaryRes.data || []).length
    todayStats.value = {
      duration: statsRes.data.todayDuration || 0,
      streak: statsRes.data.currentStreak || 0,
      diaryCount,
      goalProgress: Math.min(100, Math.round(((statsRes.data.todayDuration || 0) / dailyGoal.value) * 100))
    }
  } catch (error) {
    console.error('获取今日统计失败:', error)
  }
}

onMounted(async () => {
  await Promise.all([
    loadCheckInStatus(),
    loadTodayStats()
  ])
})
</script>

<style scoped>
.home-container {
  padding: 20rpx;
  background: #f8fafc;
  min-height: 100vh;
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

.chart-container {
  height: 300rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chart-placeholder {
  font-size: 26rpx;
  color: #94a3b8;
}
</style>

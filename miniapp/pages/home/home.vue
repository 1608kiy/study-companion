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
            <text 
              v-if="!todayCheckedIn" 
              class="replenish-link" 
              @click="showReplenish = true"
            >
              补卡申请
            </text>
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
        <view class="entry-row">
          <view class="entry-item" @click="goEfficiency">
            <text class="entry-icon">📊</text>
            <text class="entry-text">效率分析</text>
          </view>
          <view class="entry-item" @click="goMaterials">
            <text class="entry-icon">📁</text>
            <text class="entry-text">学习资料</text>
          </view>
          <view class="entry-item" @click="goSettings">
            <text class="entry-icon">⚙️</text>
            <text class="entry-text">设置</text>
          </view>
        </view>
      </view>
    </scroll-view>
    
    <!-- 补卡弹窗 -->
    <view v-if="showReplenish" class="modal-overlay" @click="showReplenish = false">
      <view class="modal-content" @click.stop>
        <text class="modal-title">补卡申请</text>
        
        <!-- 步骤1: 填写信息 -->
        <view v-if="replenishStep === 1">
          <view class="form-item">
            <text class="form-label">断签日期</text>
            <picker mode="date" :value="missDate" @change="onDateChange">
              <view class="picker-value">{{ missDate || '请选择日期' }}</view>
            </picker>
          </view>
          <view class="form-item">
            <text class="form-label">断签原因</text>
            <textarea 
              v-model="missReason" 
              class="form-textarea" 
              placeholder="请说明断签原因（如：生病、出差、考试等）"
            />
          </view>
          <view class="tip">
            <text>AI 将根据您的原因判断是否允许补签</text>
          </view>
          <view class="modal-btns">
            <button class="btn-cancel" @click="showReplenish = false">取消</button>
            <button class="btn-primary" @click="handleSubmitReplenish" :loading="submitting">提交申请</button>
          </view>
        </view>
        
        <!-- 步骤2: AI判断结果 -->
        <view v-if="replenishStep === 2" class="result-section">
          <view v-if="judging" class="judging">
            <text>AI 正在评估...</text>
          </view>
          <view v-else class="judgment-result">
            <text :class="['result-icon', judgmentAllow ? 'success' : 'fail']">
              {{ judgmentAllow ? '✓' : '✗' }}
            </text>
            <text class="result-text">{{ judgmentAllow ? '允许补签' : '不允许补签' }}</text>
            <text class="result-reason">{{ judgmentReason }}</text>
          </view>
          <view class="modal-btns">
            <button class="btn-cancel" @click="replenishStep = 1">返回</button>
            <button v-if="judgmentAllow" class="btn-primary" @click="handleReplenish" :loading="replenishing">确认补签</button>
          </view>
        </view>
        
        <!-- 步骤3: 成功 -->
        <view v-if="replenishStep === 3" class="success-section">
          <text class="success-icon">✓</text>
          <text class="success-text">补签成功！</text>
          <text class="success-date">{{ missDate }} 的打卡已补签</text>
          <button class="btn-primary" @click="showReplenish = false">完成</button>
        </view>
      </view>
    </view>
  </main-layout>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
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
const showReplenish = ref(false)
const replenishStep = ref(1)
const submitting = ref(false)
const judging = ref(false)
const replenishing = ref(false)
const missDate = ref('')
const missReason = ref('')
const currentMissRecordId = ref(null)
const judgmentAllow = ref(false)
const judgmentReason = ref('')

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

const goEfficiency = () => {
  uni.navigateTo({ url: '/pages/efficiency/efficiency' })
}

const goMaterials = () => {
  uni.navigateTo({ url: '/pages/materials/materials' })
}

const onDateChange = (e) => {
  missDate.value = e.detail.value
}

const handleSubmitReplenish = async () => {
  if (!missDate.value) {
    uni.showToast({ title: '请选择断签日期', icon: 'none' })
    return
  }
  if (!missReason.value) {
    uni.showToast({ title: '请填写断签原因', icon: 'none' })
    return
  }
  
  submitting.value = true
  try {
    const missRes = await checkInApi.recordMiss({
      missDate: missDate.value,
      reason: missReason.value
    })
    currentMissRecordId.value = missRes.data.id
    
    replenishStep.value = 2
    judging.value = true
    const judgeRes = await checkInApi.aiJudge(currentMissRecordId.value)
    judgmentAllow.value = judgeRes.data.aiAllowReplenish
    judgmentReason.value = judgeRes.data.reason || (judgmentAllow.value ? '符合补签条件' : '不符合补签条件')
  } catch (error) {
    uni.showToast({ title: error.message || '提交失败', icon: 'none' })
  } finally {
    submitting.value = false
    judging.value = false
  }
}

const handleReplenish = async () => {
  replenishing.value = true
  try {
    await checkInApi.replenish(currentMissRecordId.value)
    replenishStep.value = 3
    await loadCheckInStatus()
  } catch (error) {
    uni.showToast({ title: error.message || '补签失败', icon: 'none' })
  } finally {
    replenishing.value = false
  }
}

watch(showReplenish, (val) => {
  if (val) {
    replenishStep.value = 1
    missDate.value = ''
    missReason.value = ''
    currentMissRecordId.value = null
  }
})

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

.entry-row {
  display: flex;
  gap: 20rpx;
  margin-top: 20rpx;
}

.entry-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
  padding: 24rpx;
  background: #fff;
  border-radius: 16rpx;
  border: 1rpx solid #e2e8f0;
}

.entry-item:active {
  background: #f8fafc;
}

.entry-icon {
  font-size: 48rpx;
}

.entry-text {
  font-size: 24rpx;
  color: #64748b;
}

.replenish-link {
  font-size: 24rpx;
  color: #f59e0b;
  margin-top: 16rpx;
}

/* 弹窗样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}

.modal-content {
  width: 85%;
  background: #fff;
  border-radius: 24rpx;
  padding: 40rpx;
}

.modal-title {
  display: block;
  font-size: 36rpx;
  font-weight: 700;
  color: #1e293b;
  text-align: center;
  margin-bottom: 30rpx;
}

.form-item {
  margin-bottom: 24rpx;
}

.form-label {
  display: block;
  font-size: 28rpx;
  color: #64748b;
  margin-bottom: 12rpx;
}

.picker-value {
  padding: 20rpx;
  background: #f8fafc;
  border-radius: 12rpx;
  font-size: 28rpx;
  color: #1e293b;
}

.form-textarea {
  width: 100%;
  height: 150rpx;
  padding: 20rpx;
  background: #f8fafc;
  border-radius: 12rpx;
  font-size: 28rpx;
  color: #1e293b;
}

.tip {
  padding: 16rpx;
  background: #f0f9ff;
  border-radius: 12rpx;
  margin-bottom: 24rpx;
}

.tip text {
  font-size: 24rpx;
  color: #0ea5e9;
}

.modal-btns {
  display: flex;
  gap: 20rpx;
  margin-top: 30rpx;
}

.btn-cancel {
  flex: 1;
  height: 80rpx;
  background: #f1f5f9;
  border-radius: 12rpx;
  font-size: 28rpx;
  color: #64748b;
}

.btn-primary {
  flex: 1;
  height: 80rpx;
  background: #6366f1;
  border-radius: 12rpx;
  font-size: 28rpx;
  color: #fff;
}

.result-section,
.success-section {
  text-align: center;
  padding: 30rpx 0;
}

.judging text {
  font-size: 28rpx;
  color: #64748b;
}

.judgment-result {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
}

.result-icon {
  font-size: 80rpx;
}

.result-icon.success { color: #10b981; }
.result-icon.fail { color: #ef4444; }

.result-text {
  font-size: 32rpx;
  font-weight: 600;
  color: #1e293b;
}

.result-reason {
  font-size: 26rpx;
  color: #64748b;
}

.success-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
}

.success-icon {
  font-size: 100rpx;
  color: #10b981;
}

.success-text {
  font-size: 36rpx;
  font-weight: 600;
  color: #10b981;
}

.success-date {
  font-size: 26rpx;
  color: #64748b;
  margin-bottom: 20rpx;
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

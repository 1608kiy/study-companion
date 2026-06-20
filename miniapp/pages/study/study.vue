<template>
  <main-layout :showTabbar="true" :currentTab="1">
    <scroll-view 
      class="study-scroll" 
      scroll-y
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <!-- 骨架屏 -->
      <view v-if="loading" class="skeleton-wrapper">
        <view class="skeleton-timer"></view>
        <view class="skeleton-stats"></view>
        <view class="skeleton-records"></view>
      </view>
      
      <!-- 实际内容 -->
      <view v-else class="page-container">
        <!-- 计时器区域 -->
        <view class="card timer-card">
          <view class="timer-display">
            <text class="timer-value">{{ studyStore.elapsedDisplay }}</text>
            <text class="timer-subject">{{ studyStore.timerState.subjectName || '选择科目开始学习' }}</text>
          </view>
          
          <!-- 科目选择 -->
          <view class="subject-picker" v-if="!studyStore.isTimerRunning">
            <picker :range="subjectNames" @change="onSubjectChange">
              <view class="picker-value">
                {{ selectedSubject ? selectedSubject.name : '请选择科目' }}
              </view>
            </picker>
          </view>
          
          <!-- 控制按钮 -->
          <view class="timer-controls">
            <button 
              v-if="!studyStore.isTimerRunning && !studyStore.isPaused"
              class="btn-primary" 
              :class="{ 'btn-disabled': !selectedSubject }"
              :disabled="!selectedSubject"
              @click="handleStart"
            >
              开始学习
            </button>
            <button 
              v-if="studyStore.isTimerRunning"
              class="btn-pause" 
              @click="handlePause"
            >
              暂停
            </button>
            <button 
              v-if="studyStore.isPaused"
              class="btn-resume" 
              @click="handleResume"
            >
              继续
            </button>
            <button 
              v-if="studyStore.isTimerRunning || studyStore.isPaused"
              class="btn-stop" 
              @click="handleStop"
            >
              停止
            </button>
          </view>
          
          <text v-if="!selectedSubject && !studyStore.isTimerRunning" class="hint-text">
            请先选择科目
          </text>
        </view>
        
        <!-- 今日统计 -->
        <view class="card">
          <text class="card-title">今日统计</text>
          <view class="stats-row">
            <view class="stat-item">
              <text class="stat-item-value">{{ todayStats.duration }}</text>
              <text class="stat-item-label">总时长(分钟)</text>
            </view>
            <view class="stat-item">
              <text class="stat-item-value">{{ todayStats.count }}</text>
              <text class="stat-item-label">学习次数</text>
            </view>
          </view>
        </view>
        
        <!-- 今日记录 -->
        <view class="card">
          <view class="card-header">
            <text class="card-title">今日记录</text>
            <text class="manage-link" @click="goSubjects">管理科目</text>
          </view>
          <view v-if="studyRecords.length === 0" class="empty-state">
            <text class="empty-text">暂无学习记录</text>
          </view>
          <view v-else class="record-list">
            <view 
              v-for="record in studyRecords" 
              :key="record.id" 
              class="record-item"
            >
              <view class="record-info">
                <text class="record-subject">{{ record.subjectName }}</text>
                <text class="record-time">{{ record.startTime }} - {{ record.endTime }}</text>
              </view>
              <text class="record-duration">{{ record.duration }}分钟</text>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>
  </main-layout>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useStudyStore } from '../../store/study'
import { subjectApi, studyRecordApi } from '../../api/modules'
import MainLayout from '../../components/main-layout.vue'
import { getToday } from '../../utils/date'

const studyStore = useStudyStore()

const loading = ref(true)
const refreshing = ref(false)
const subjects = ref([])
const selectedSubject = ref(null)
const studyRecords = ref([])
const todayStats = ref({ duration: 0, count: 0 })
let timerInterval = null

const subjectNames = computed(() => subjects.value.map(s => s.name))

const onSubjectChange = (e) => {
  const index = e.detail.value
  selectedSubject.value = subjects.value[index]
}

const goSubjects = () => {
  uni.navigateTo({ url: '/pages/subjects/subjects' })
}

const handleStart = async () => {
  if (!selectedSubject.value) {
    uni.showToast({ title: '请先选择科目', icon: 'none' })
    return
  }
  try {
    await studyStore.startTimer(selectedSubject.value.id)
    startTimerInterval()
  } catch (error) {
    uni.showToast({ title: error.message || '开始计时失败', icon: 'none' })
  }
}

const handlePause = async () => {
  try {
    await studyStore.pauseTimer()
    stopTimerInterval()
  } catch (error) {
    uni.showToast({ title: error.message || '暂停失败', icon: 'none' })
  }
}

const handleResume = async () => {
  try {
    await studyStore.resumeTimer()
    startTimerInterval()
  } catch (error) {
    uni.showToast({ title: error.message || '恢复失败', icon: 'none' })
  }
}

const handleStop = () => {
  uni.showModal({
    title: '确认停止',
    content: '确定要停止计时吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await studyStore.stopTimer()
          stopTimerInterval()
          await loadTodayRecords()
          uni.showToast({ title: '计时结束', icon: 'success' })
        } catch (error) {
          uni.showToast({ title: error.message || '停止失败', icon: 'none' })
        }
      }
    }
  })
}

const startTimerInterval = () => {
  stopTimerInterval()
  timerInterval = setInterval(() => {
    studyStore.incrementElapsed()
  }, 1000)
}

const stopTimerInterval = () => {
  if (timerInterval) {
    clearInterval(timerInterval)
    timerInterval = null
  }
}

const loadSubjects = async () => {
  try {
    const res = await subjectApi.getList()
    subjects.value = res.data || []
  } catch (error) {
    uni.showToast({ title: '获取科目失败', icon: 'none' })
  }
}

const loadTodayRecords = async () => {
  try {
    const today = getToday()
    const res = await studyRecordApi.getList({ startDate: today })
    studyRecords.value = res.data || []
    todayStats.value = {
      duration: studyRecords.value.reduce((sum, r) => sum + r.duration, 0),
      count: studyRecords.value.length
    }
  } catch (error) {
    uni.showToast({ title: '获取记录失败', icon: 'none' })
  }
}

const loadData = async () => {
  loading.value = true
  try {
    await Promise.all([
      loadSubjects(),
      loadTodayRecords(),
      studyStore.getTimerState()
    ])
    
    if (studyStore.isTimerRunning) {
      startTimerInterval()
    }
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

onUnmounted(() => {
  stopTimerInterval()
})
</script>

<style scoped>
.study-scroll {
  height: 100vh;
}

.timer-card {
  text-align: center;
}

.timer-display {
  margin-bottom: 40rpx;
}

.timer-value {
  display: block;
  font-size: 96rpx;
  font-weight: 700;
  color: #1e293b;
  font-family: 'Courier New', Courier, monospace;
}

.timer-subject {
  display: block;
  font-size: 28rpx;
  color: #64748b;
  margin-top: 12rpx;
}

.subject-picker {
  margin-bottom: 30rpx;
}

.picker-value {
  height: 88rpx;
  line-height: 88rpx;
  background: #f8fafc;
  border-radius: 12rpx;
  padding: 0 24rpx;
  font-size: 28rpx;
  color: #1e293b;
  border: 1rpx solid #e2e8f0;
}

.timer-controls {
  display: flex;
  gap: 20rpx;
}

.btn-primary {
  flex: 1;
}

.btn-disabled {
  opacity: 0.5;
}

.hint-text {
  display: block;
  font-size: 24rpx;
  color: #6b7280;
  margin-top: 16rpx;
}

/* 统计 */
.stat-item {
  flex: 1;
  text-align: center;
  padding: 20rpx;
  background: #f8fafc;
  border-radius: 12rpx;
}

.stat-item-value {
  display: block;
  font-size: 40rpx;
  font-weight: 700;
  color: #6366f1;
}

.stat-item-label {
  display: block;
  font-size: 22rpx;
  color: #64748b;
  margin-top: 8rpx;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.manage-link {
  font-size: 24rpx;
  color: #6366f1;
  padding: 16rpx;
}

/* 记录列表 */
.record-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.record-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx;
  background: #f8fafc;
  border-radius: 12rpx;
}

.record-info {
  flex: 1;
}

.record-subject {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
  color: #1e293b;
}

.record-time {
  display: block;
  font-size: 22rpx;
  color: #64748b;
  margin-top: 4rpx;
}

.record-duration {
  font-size: 28rpx;
  font-weight: 600;
  color: #6366f1;
}

/* 骨架屏 */
.skeleton-timer {
  height: 400rpx;
  background: #e2e8f0;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
}

.skeleton-stats {
  height: 150rpx;
  background: #e2e8f0;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
}

.skeleton-records {
  height: 300rpx;
  background: #e2e8f0;
  border-radius: 16rpx;
}
</style>

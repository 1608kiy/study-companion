<template>
  <main-layout :showTabbar="true" :currentTab="1">
    <view class="study-container">
      <!-- 计时器区域 -->
      <view class="timer-card">
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
            class="btn-start" 
            @click="handleStart"
            :disabled="!selectedSubject"
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
      </view>
      
      <!-- 今日统计 -->
      <view class="card stats-card">
        <text class="card-title">今日统计</text>
        <view class="stats-row">
          <view class="stat-item">
            <text class="stat-value">{{ todayStats.duration }}</text>
            <text class="stat-label">总时长(分钟)</text>
          </view>
          <view class="stat-item">
            <text class="stat-value">{{ todayStats.count }}</text>
            <text class="stat-label">学习次数</text>
          </view>
        </view>
      </view>
      
      <!-- 今日记录 -->
      <view class="card records-card">
        <text class="card-title">今日记录</text>
        <view v-if="studyRecords.length === 0" class="empty-tip">
          <text>暂无学习记录</text>
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
  </main-layout>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useStudyStore } from '../../store/study'
import { subjectApi, studyRecordApi } from '../../api/modules'
import MainLayout from '../../components/main-layout.vue'

const studyStore = useStudyStore()

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

const handleStart = async () => {
  if (!selectedSubject.value) {
    uni.showToast({ title: '请先选择科目', icon: 'none' })
    return
  }
  try {
    await studyStore.startTimer(selectedSubject.value.id)
    startTimerInterval()
  } catch (error) {
    console.error('开始计时失败:', error)
  }
}

const handlePause = async () => {
  try {
    await studyStore.pauseTimer()
    stopTimerInterval()
  } catch (error) {
    console.error('暂停失败:', error)
  }
}

const handleResume = async () => {
  try {
    await studyStore.resumeTimer()
    startTimerInterval()
  } catch (error) {
    console.error('恢复失败:', error)
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
          console.error('停止失败:', error)
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
    console.error('获取科目失败:', error)
  }
}

const loadTodayRecords = async () => {
  try {
    const today = new Date().toISOString().split('T')[0]
    const res = await studyRecordApi.getList({ startDate: today })
    studyRecords.value = res.data || []
    todayStats.value = {
      duration: studyRecords.value.reduce((sum, r) => sum + r.duration, 0),
      count: studyRecords.value.length
    }
  } catch (error) {
    console.error('获取记录失败:', error)
  }
}

onMounted(async () => {
  await Promise.all([
    loadSubjects(),
    loadTodayRecords(),
    studyStore.getTimerState()
  ])
  
  if (studyStore.isTimerRunning) {
    startTimerInterval()
  }
})

onUnmounted(() => {
  stopTimerInterval()
})
</script>

<style scoped>
.study-container {
  padding: 20rpx;
  background: #f8fafc;
  min-height: 100vh;
}

.timer-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 40rpx;
  margin-bottom: 20rpx;
  border: 1rpx solid #e2e8f0;
}

.timer-display {
  text-align: center;
  margin-bottom: 40rpx;
}

.timer-value {
  display: block;
  font-size: 96rpx;
  font-weight: 700;
  color: #1e293b;
  font-family: monospace;
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
  height: 80rpx;
  line-height: 80rpx;
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

.btn-start, .btn-pause, .btn-resume, .btn-stop {
  flex: 1;
  height: 88rpx;
  border-radius: 16rpx;
  font-size: 28rpx;
  font-weight: 600;
}

.btn-start {
  background: #6366f1;
  color: #fff;
}

.btn-pause {
  background: #f59e0b;
  color: #fff;
}

.btn-resume {
  background: #10b981;
  color: #fff;
}

.btn-stop {
  background: #ef4444;
  color: #fff;
}

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

.stats-row {
  display: flex;
  gap: 20rpx;
}

.stat-item {
  flex: 1;
  text-align: center;
  padding: 20rpx;
  background: #f8fafc;
  border-radius: 12rpx;
}

.stat-value {
  display: block;
  font-size: 40rpx;
  font-weight: 700;
  color: #6366f1;
}

.stat-label {
  display: block;
  font-size: 22rpx;
  color: #64748b;
  margin-top: 8rpx;
}

.empty-tip {
  text-align: center;
  padding: 40rpx;
  color: #94a3b8;
  font-size: 26rpx;
}

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
</style>

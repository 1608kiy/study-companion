// 学习状态管理
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { studyRecordApi } from '../api/modules'

export const useStudyStore = defineStore('study', () => {
  // 状态
  const timerState = ref({
    isRunning: false,
    subjectId: null,
    subjectName: '',
    startTime: null,
    elapsedSeconds: 0
  })
  const studyRecords = ref([])
  const studyStats = ref(null)

  // 计算属性
  const isTimerRunning = computed(() => timerState.value.isRunning)
  const isPaused = computed(() => !timerState.value.isRunning && timerState.value.subjectId !== null)
  const elapsedMinutes = computed(() => Math.floor(timerState.value.elapsedSeconds / 60))
  const elapsedSecondsDisplay = computed(() => timerState.value.elapsedSeconds % 60)
  const elapsedDisplay = computed(() => {
    const m = String(elapsedMinutes.value).padStart(2, '0')
    const s = String(elapsedSecondsDisplay.value).padStart(2, '0')
    return `${m}:${s}`
  })

  // 开始计时
  const startTimer = async (subjectId) => {
    const res = await studyRecordApi.startTimer({ subjectId })
    timerState.value = {
      isRunning: true,
      subjectId: res.data.subjectId,
      subjectName: res.data.subjectName,
      startTime: res.data.startTime,
      elapsedSeconds: 0
    }
    return res
  }

  // 暂停计时
  const pauseTimer = async () => {
    const res = await studyRecordApi.pauseTimer()
    timerState.value.isRunning = false
    return res
  }

  // 恢复计时
  const resumeTimer = async () => {
    const res = await studyRecordApi.resumeTimer()
    timerState.value.isRunning = true
    return res
  }

  // 停止计时
  const stopTimer = async () => {
    const res = await studyRecordApi.stopTimer()
    timerState.value = {
      isRunning: false,
      subjectId: null,
      subjectName: '',
      startTime: null,
      elapsedSeconds: 0
    }
    return res
  }

  // 获取计时状态
  const getTimerState = async () => {
    const res = await studyRecordApi.getTimerState()
    if (res.data) {
      timerState.value = {
        isRunning: res.data.isRunning || false,
        subjectId: res.data.subjectId || null,
        subjectName: res.data.subjectName || '',
        startTime: res.data.startTime || null,
        elapsedSeconds: res.data.elapsedSeconds || 0
      }
    }
    return res
  }

  // 获取学习记录
  const getStudyRecords = async (params) => {
    const res = await studyRecordApi.getList(params)
    studyRecords.value = res.data || []
    return res
  }

  // 获取学习统计
  const getStudyStats = async () => {
    const res = await studyRecordApi.getStats()
    studyStats.value = res.data
    return res
  }

  // 增加计时秒数（前端显示用）
  const incrementElapsed = () => {
    timerState.value.elapsedSeconds++
  }

  return {
    timerState,
    studyRecords,
    studyStats,
    isTimerRunning,
    isPaused,
    elapsedMinutes,
    elapsedSecondsDisplay,
    elapsedDisplay,
    startTimer,
    pauseTimer,
    resumeTimer,
    stopTimer,
    getTimerState,
    getStudyRecords,
    getStudyStats,
    incrementElapsed
  }
})

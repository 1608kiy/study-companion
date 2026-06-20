import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { studyRecordApi } from '@/api/modules'

export const useStudyStore = defineStore('study', () => {
  const timerState = ref({
    isRunning: false,
    subjectId: null,
    subjectName: '',
    startTime: null,
    elapsedSeconds: 0,
  })
  const studyRecords = ref([])
  const studyStats = ref(null)

  const isTimerRunning = computed(() => timerState.value.isRunning)
  const isPaused = computed(() => !timerState.value.isRunning && timerState.value.subjectId !== null)
  const elapsedMinutes = computed(() => Math.floor(timerState.value.elapsedSeconds / 60))
  const elapsedSecondsDisplay = computed(() => timerState.value.elapsedSeconds % 60)

  const startTimer = async (subjectId) => {
    const res = await studyRecordApi.startTimer({ subjectId })
    timerState.value = res.data
    return res
  }

  const pauseTimer = async () => {
    const res = await studyRecordApi.pauseTimer()
    timerState.value = res.data
    return res
  }

  const resumeTimer = async () => {
    const res = await studyRecordApi.resumeTimer()
    timerState.value = res.data
    return res
  }

  const stopTimer = async () => {
    const res = await studyRecordApi.stopTimer()
    timerState.value = {
      isRunning: false,
      subjectId: null,
      subjectName: '',
      startTime: null,
      elapsedSeconds: 0,
    }
    return res
  }

  const getTimerState = async () => {
    const res = await studyRecordApi.getTimerState()
    timerState.value = res.data
    return res
  }

  const getStudyRecords = async (params) => {
    const res = await studyRecordApi.getList(params)
    studyRecords.value = res.data
    return res
  }

  const getStudyStats = async () => {
    const res = await studyRecordApi.getStats()
    studyStats.value = res.data
    return res
  }

  return {
    timerState,
    studyRecords,
    studyStats,
    isTimerRunning,
    isPaused,
    elapsedMinutes,
    elapsedSecondsDisplay,
    startTimer,
    pauseTimer,
    resumeTimer,
    stopTimer,
    getTimerState,
    getStudyRecords,
    getStudyStats,
  }
})

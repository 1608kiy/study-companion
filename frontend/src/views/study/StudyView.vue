<template>
  <div class="study-container">
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card class="timer-card">
          <template #header>
            <span class="card-title">番茄钟计时</span>
          </template>
          <div class="timer-content">
            <div class="timer-display">
              <div class="timer-ring">
                <div class="timer-inner">
                  <span class="timer-time">{{ formatTime(elapsedSeconds) }}</span>
                  <span class="timer-subject">{{ currentSubjectName || '选择科目开始学习' }}</span>
                </div>
              </div>
            </div>
            <div class="timer-controls">
              <el-select 
                v-model="selectedSubjectId" 
                placeholder="选择科目" 
                :disabled="isRunning"
                class="subject-select"
                size="large"
              >
                <el-option 
                  v-for="subject in subjects" 
                  :key="subject.id" 
                  :label="subject.name" 
                  :value="subject.id" 
                />
              </el-select>
              <div class="control-buttons">
                <el-button 
                  v-if="!isRunning" 
                  type="primary" 
                  size="large" 
                  class="control-btn start-btn"
                  @click="startTimer"
                  :disabled="!selectedSubjectId"
                >
                  <el-icon><VideoPlay /></el-icon>
                  开始学习
                </el-button>
                <template v-else>
                  <el-button 
                    v-if="!isPaused" 
                    type="warning" 
                    size="large" 
                    class="control-btn"
                    @click="pauseTimer"
                  >
                    <el-icon><VideoPause /></el-icon>
                    暂停
                  </el-button>
                  <el-button 
                    v-else 
                    type="success" 
                    size="large" 
                    class="control-btn"
                    @click="resumeTimer"
                  >
                    <el-icon><VideoPlay /></el-icon>
                    继续
                  </el-button>
                  <el-button type="danger" size="large" class="control-btn" @click="stopTimer">
                    <el-icon><VideoDel /></el-icon>
                    停止
                  </el-button>
                </template>
              </div>
            </div>
          </div>
        </el-card>
        
        <el-card class="records-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">今日学习记录</span>
              <el-button type="primary" text @click="refreshRecords">
                <el-icon><Refresh /></el-icon>
                刷新
              </el-button>
            </div>
          </template>
          <el-table :data="todayRecords" style="width: 100%" stripe>
            <el-table-column prop="subjectName" label="科目" width="120" />
            <el-table-column prop="duration" label="时长(分钟)" width="100" />
            <el-table-column prop="startTime" label="开始时间" width="180" />
            <el-table-column prop="endTime" label="结束时间" width="180" />
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button type="primary" text size="small" @click="editRecord(row)">编辑</el-button>
                <el-button type="danger" text size="small" @click="deleteRecord(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card class="stats-card">
          <template #header>
            <span class="card-title">今日统计</span>
          </template>
          <div class="today-stats">
            <div class="stat-item">
              <div class="stat-item-icon blue">
                <el-icon><Timer /></el-icon>
              </div>
              <div class="stat-item-info">
                <span class="stat-item-value">{{ todayStats.duration || 0 }} 分钟</span>
                <span class="stat-item-label">学习时长</span>
              </div>
            </div>
            <div class="stat-item">
              <div class="stat-item-icon green">
                <el-icon><Finished /></el-icon>
              </div>
              <div class="stat-item-info">
                <span class="stat-item-value">{{ todayStats.count || 0 }} 次</span>
                <span class="stat-item-label">学习次数</span>
              </div>
            </div>
            <div class="stat-item">
              <div class="stat-item-icon purple">
                <el-icon><TrendCharts /></el-icon>
              </div>
              <div class="stat-item-info">
                <span class="stat-item-value">{{ todayStats.progress || 0 }}%</span>
                <span class="stat-item-label">目标完成</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useStudyStore } from '@/stores/study'
import { subjectApi } from '@/api/modules'
import { ElMessage, ElMessageBox } from 'element-plus'

const studyStore = useStudyStore()

const subjects = ref([])
const selectedSubjectId = ref(null)
const todayRecords = ref([])
const todayStats = ref({
  duration: 0,
  count: 0,
  progress: 0,
})

const isRunning = computed(() => studyStore.isTimerRunning)
const isPaused = computed(() => studyStore.timerState.paused)
const elapsedSeconds = computed(() => studyStore.timerState.elapsedSeconds)
const currentSubjectName = computed(() => studyStore.timerState.subjectName)

let timerInterval = null

const formatTime = (seconds) => {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`
}

const startTimer = async () => {
  if (!selectedSubjectId.value) {
    ElMessage.warning('请先选择科目')
    return
  }
  
  try {
    await studyStore.startTimer(selectedSubjectId.value)
    startTimerInterval()
    ElMessage.success('计时开始')
  } catch (error) {
    console.error('开始计时失败:', error)
  }
}

const pauseTimer = async () => {
  try {
    await studyStore.pauseTimer()
    stopTimerInterval()
    ElMessage.info('计时已暂停')
  } catch (error) {
    console.error('暂停计时失败:', error)
  }
}

const resumeTimer = async () => {
  try {
    await studyStore.resumeTimer()
    startTimerInterval()
    ElMessage.success('计时已继续')
  } catch (error) {
    console.error('继续计时失败:', error)
  }
}

const stopTimer = async () => {
  try {
    await ElMessageBox.confirm('确定要停止计时吗？', '确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    
    await studyStore.stopTimer()
    stopTimerInterval()
    ElMessage.success('计时已停止')
    await refreshRecords()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('停止计时失败:', error)
    }
  }
}

const startTimerInterval = () => {
  stopTimerInterval()
  timerInterval = setInterval(() => {
    studyStore.timerState.elapsedSeconds++
  }, 1000)
}

const stopTimerInterval = () => {
  if (timerInterval) {
    clearInterval(timerInterval)
    timerInterval = null
  }
}

const refreshRecords = async () => {
  try {
    const res = await studyStore.getStudyRecords({ startDate: new Date().toISOString().split('T')[0] })
    todayRecords.value = res.data || []
    todayStats.value = {
      duration: todayRecords.value.reduce((sum, r) => sum + (r.duration || 0), 0),
      count: todayRecords.value.length,
      progress: Math.min(100, Math.round((todayRecords.value.reduce((sum, r) => sum + (r.duration || 0), 0) / 120) * 100)),
    }
  } catch (error) {
    console.error('获取学习记录失败:', error)
  }
}

const loadSubjects = async () => {
  try {
    const res = await subjectApi.getList()
    subjects.value = res.data || []
  } catch (error) {
    console.error('获取科目列表失败:', error)
  }
}

const editRecord = (record) => {
  console.log('编辑记录:', record)
}

const deleteRecord = async (record) => {
  try {
    await ElMessageBox.confirm('确定要删除这条记录吗？', '确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    
    ElMessage.success('删除成功')
    await refreshRecords()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

onMounted(async () => {
  await Promise.all([
    loadSubjects(),
    refreshRecords(),
    studyStore.getTimerState(),
  ])
  
  if (isRunning.value && !isPaused.value) {
    startTimerInterval()
  }
})

onUnmounted(() => {
  stopTimerInterval()
})
</script>

<style scoped>
.study-container {
  padding: 0;
}

.card-title {
  font-weight: 600;
  color: #1e293b;
}

.timer-card {
  margin-bottom: 20px;
}

.timer-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 32px 0;
}

.timer-display {
  margin-bottom: 32px;
}

.timer-ring {
  width: 220px;
  height: 220px;
  border-radius: 50%;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 32px rgba(99, 102, 241, 0.3);
}

.timer-inner {
  width: 196px;
  height: 196px;
  border-radius: 50%;
  background: white;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.timer-time {
  font-size: 48px;
  font-weight: 700;
  color: #1e293b;
  font-variant-numeric: tabular-nums;
}

.timer-subject {
  font-size: 14px;
  color: #64748b;
  margin-top: 4px;
}

.timer-controls {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.subject-select {
  width: 220px;
}

.control-buttons {
  display: flex;
  gap: 12px;
}

.control-btn {
  height: 44px;
  padding: 0 24px;
  border-radius: 10px;
  font-weight: 600;
}

.start-btn {
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  border: none;
}

.start-btn:hover {
  background: linear-gradient(135deg, #5558e6 0%, #7c4fdb 100%);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stats-card {
  height: 100%;
}

.today-stats {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px;
  background: #f8fafc;
  border-radius: 10px;
}

.stat-item-icon {
  width: 42px;
  height: 42px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 18px;
  flex-shrink: 0;
}

.stat-item-icon.blue { background: linear-gradient(135deg, #3b82f6, #2563eb); }
.stat-item-icon.green { background: linear-gradient(135deg, #10b981, #059669); }
.stat-item-icon.purple { background: linear-gradient(135deg, #8b5cf6, #7c3aed); }

.stat-item-info {
  display: flex;
  flex-direction: column;
}

.stat-item-value {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
}

.stat-item-label {
  font-size: 12px;
  color: #94a3b8;
}
</style>

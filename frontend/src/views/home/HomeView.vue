<template>
  <div class="home-container">
    <div class="welcome-section">
      <h2>欢迎回来，{{ userInfo?.nickname || '学习者' }} 👋</h2>
      <p class="welcome-sub">今天也要加油学习哦</p>
    </div>
    
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="12" :sm="12" :md="6">
        <div class="stat-card stat-blue">
          <div class="stat-icon">
            <el-icon size="24"><Timer /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ todayStats.duration || 0 }}</div>
            <div class="stat-label">今日学习(分钟)</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6">
        <div class="stat-card stat-green">
          <div class="stat-icon">
            <el-icon size="24"><Calendar /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ todayStats.streak || 0 }}</div>
            <div class="stat-label">连续打卡(天)</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6">
        <div class="stat-card stat-orange">
          <div class="stat-icon">
            <el-icon size="24"><Notebook /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ todayStats.diaryCount || 0 }}</div>
            <div class="stat-label">本周日记(篇)</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6">
        <div class="stat-card stat-purple">
          <div class="stat-icon">
            <el-icon size="24"><Trophy /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ todayStats.goalProgress || 0 }}%</div>
            <div class="stat-label">目标完成度</div>
          </div>
        </div>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" class="content-row">
      <el-col :xs="24" :md="16">
        <el-card class="chart-card">
          <template #header>
            <span class="card-title">学习趋势</span>
          </template>
          <ChartView :option="trendChartOption" :height="320" />
        </el-card>
      </el-col>
      <el-col :xs="24" :md="8">
        <el-card class="checkin-card">
          <template #header>
            <span class="card-title">今日打卡</span>
          </template>
          <div class="checkin-content">
            <el-progress 
              type="circle" 
              :percentage="checkInProgress" 
              :width="130"
              :stroke-width="10"
              :status="checkInStatus"
            >
              <template #default="{ percentage }">
                <div class="progress-text">
                  <span class="progress-value">{{ percentage }}%</span>
                  <span class="progress-label">今日完成</span>
                </div>
              </template>
            </el-progress>
            <el-button 
              v-if="!todayCheckedIn" 
              type="primary" 
              size="large" 
              class="checkin-btn"
              @click="handleCheckIn"
            >
              <el-icon><Check /></el-icon>
              打卡
            </el-button>
            <el-tag v-else type="success" size="large" class="checked-tag">
              <el-icon><CircleCheck /></el-icon>
              已打卡
            </el-tag>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { checkInApi, studyRecordApi, diaryApi } from '@/api/modules'
import { ElMessage } from 'element-plus'
import ChartView from '@/components/ChartView.vue'
import dayjs from 'dayjs'

const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo)
const dailyGoal = computed(() => userStore.userInfo?.dailyGoal || 120)

const todayStats = ref({
  duration: 0,
  streak: 0,
  diaryCount: 0,
  goalProgress: 0,
})

const weeklyData = ref([])

const checkInStatusData = ref({
  totalDuration: 0,
  streak: 0,
  isCompleted: false,
  dailyGoal: 120,
})

const todayCheckedIn = computed(() => checkInStatusData.value.isCompleted)
const checkInProgress = computed(() => {
  const progress = Math.min(100, (checkInStatusData.value.totalDuration / dailyGoal.value) * 100)
  return Math.round(progress)
})
const checkInStatus = computed(() => todayCheckedIn.value ? 'success' : '')

const trendChartOption = computed(() => {
  const days = []
  for (let i = 6; i >= 0; i--) {
    days.push(dayjs().subtract(i, 'day').format('MM/DD'))
  }
  
  const values = weeklyData.value.length ? weeklyData.value : [0, 0, 0, 0, 0, 0, 0]

  return {
    tooltip: { trigger: 'axis' },
    grid: { top: 20, right: 20, bottom: 30, left: 50 },
    xAxis: {
      type: 'category',
      data: days,
      axisLine: { lineStyle: { color: '#e2e8f0' } },
      axisLabel: { color: '#64748b' },
    },
    yAxis: {
      type: 'value',
      name: '分钟',
      axisLine: { show: false },
      splitLine: { lineStyle: { color: '#f1f5f9' } },
      axisLabel: { color: '#64748b' },
    },
    series: [{
      type: 'bar',
      data: values,
      barWidth: 24,
      itemStyle: {
        borderRadius: [4, 4, 0, 0],
        color: '#6366f1',
      },
    }],
  }
})

const handleCheckIn = async () => {
  try {
    await checkInApi.checkIn()
    ElMessage.success('打卡成功！')
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
      diaryApi.getList(),
    ])
    const diaryCount = (diaryRes.data || []).length
    todayStats.value = {
      duration: statsRes.data.todayDuration || 0,
      streak: statsRes.data.currentStreak || 0,
      diaryCount,
      goalProgress: Math.min(100, Math.round(((statsRes.data.todayDuration || 0) / dailyGoal.value) * 100)),
    }
    weeklyData.value = statsRes.data.weeklyDurations || []
  } catch (error) {
    console.error('获取今日统计失败:', error)
  }
}

onMounted(async () => {
  await Promise.all([
    loadCheckInStatus(),
    loadTodayStats(),
  ])
})
</script>

<style scoped>
.home-container { padding: 0; }

.welcome-section { margin-bottom: 24px; }
.welcome-section h2 { font-size: 24px; font-weight: 700; color: var(--text-primary); margin-bottom: 4px; }
.welcome-sub { color: var(--text-secondary); font-size: 14px; }

.stats-row { margin-bottom: 20px; }

.stat-card {
  background: white;
  border-radius: var(--radius-lg);
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  border: 1px solid var(--border);
}

.stat-icon {
  width: 44px; height: 44px; border-radius: var(--radius);
  display: flex; align-items: center; justify-content: center;
  color: white; flex-shrink: 0;
}

.stat-blue .stat-icon { background: var(--stat-blue); }
.stat-green .stat-icon { background: var(--stat-green); }
.stat-orange .stat-icon { background: var(--stat-orange); }
.stat-purple .stat-icon { background: var(--stat-purple); }

.stat-info { flex: 1; }
.stat-value { font-size: 28px; font-weight: 700; color: var(--text-primary); line-height: 1.2; }
.stat-label { font-size: 13px; color: var(--text-secondary); margin-top: 2px; }

.content-row { margin-bottom: 20px; }
.card-title { font-weight: 600; color: var(--text-primary); }

.chart-card { height: 100%; }

.checkin-card { height: 100%; }
.checkin-content {
  display: flex; flex-direction: column; align-items: center;
  gap: 20px; padding: 16px 0;
}

.progress-text { display: flex; flex-direction: column; align-items: center; }
.progress-value { font-size: 28px; font-weight: 700; color: var(--text-primary); }
.progress-label { font-size: 12px; color: var(--text-muted); margin-top: 4px; }

.checkin-btn { width: 120px; height: 40px; border-radius: 10px; font-weight: 600; }
.checked-tag { height: 40px; padding: 0 20px; font-size: 14px; border-radius: 10px; }
</style>

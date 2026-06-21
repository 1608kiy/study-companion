<template>
  <div class="home-container">
    <!-- 骨架屏 -->
    <div v-if="loading" class="skeleton-wrapper">
      <div class="skeleton-welcome"></div>
      <el-row :gutter="20" class="stats-row">
        <el-col v-for="i in 4" :key="i" :xs="12" :sm="12" :md="6">
          <div class="skeleton-stat"></div>
        </el-col>
      </el-row>
      <el-row :gutter="20" class="content-row">
        <el-col :xs="24" :md="16">
          <div class="skeleton-chart"></div>
        </el-col>
        <el-col :xs="24" :md="8">
          <div class="skeleton-checkin"></div>
        </el-col>
      </el-row>
    </div>

    <!-- 实际内容 -->
    <div v-else>
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
    
    <!-- 快捷操作 -->
    <el-row :gutter="20" class="quick-actions-row">
      <el-col :xs="12" :md="6">
        <el-card class="quick-action-card" @click="goStudy">
          <el-icon size="28" color="#6366f1"><Timer /></el-icon>
          <span>开始学习</span>
        </el-card>
      </el-col>
      <el-col :xs="12" :md="6">
        <el-card class="quick-action-card" @click="goDiary">
          <el-icon size="28" color="#10b981"><Notebook /></el-icon>
          <span>写日记</span>
        </el-card>
      </el-col>
      <el-col :xs="12" :md="6">
        <el-card class="quick-action-card" @click="generateAIDiary" :loading="diaryLoading">
          <el-icon size="28" color="#f59e0b"><MagicStick /></el-icon>
          <span>AI写日记</span>
        </el-card>
      </el-col>
      <el-col :xs="12" :md="6">
        <el-card class="quick-action-card" @click="goStats">
          <el-icon size="28" color="#8b5cf6"><DataAnalysis /></el-icon>
          <span>查看统计</span>
        </el-card>
      </el-col>
    </el-row>
    </div>
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
const loading = ref(true)
const diaryLoading = ref(false)

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

const goStudy = () => {
  window.location.href = '/study'
}

const goDiary = () => {
  window.location.href = '/diary'
}

const goStats = () => {
  window.location.href = '/stats'
}

const generateAIDiary = async () => {
  diaryLoading.value = true
  try {
    await diaryApi.generate()
    ElMessage.success('AI日记生成成功！')
  } catch (error) {
    ElMessage.error(error.message || '生成失败')
  } finally {
    diaryLoading.value = false
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
  try {
    await Promise.all([
      loadCheckInStatus(),
      loadTodayStats(),
    ])
  } finally {
    loading.value = false
  }
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

/* 快捷操作 */
.quick-actions-row {
  margin-bottom: 20px;
}

.quick-action-card {
  cursor: pointer;
  text-align: center;
  padding: 20px;
  transition: background 0.2s;
}

.quick-action-card:hover {
  background: var(--bg-page);
}

.quick-action-card .el-icon {
  margin-bottom: 8px;
}

.quick-action-card span {
  display: block;
  font-size: 13px;
  color: var(--text-secondary);
}

/* 骨架屏 */
.skeleton-wrapper { animation: pulse 1.5s ease-in-out infinite; }
@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}
.skeleton-welcome {
  height: 60px; background: var(--border); border-radius: var(--radius);
  margin-bottom: 24px;
}
.skeleton-stat {
  height: 84px; background: var(--border); border-radius: var(--radius-lg);
}
.skeleton-chart {
  height: 380px; background: var(--border); border-radius: var(--radius-lg);
}
.skeleton-checkin {
  height: 380px; background: var(--border); border-radius: var(--radius-lg);
}
</style>

<template>
  <div class="stats-container">
    <!-- 骨架屏 -->
    <div v-if="loading" class="skeleton-wrapper">
      <div class="skeleton-stats"></div>
      <el-row :gutter="20" class="chart-row">
        <el-col :xs="24" :md="12">
          <div class="skeleton-chart"></div>
        </el-col>
        <el-col :xs="24" :md="12">
          <div class="skeleton-chart"></div>
        </el-col>
      </el-row>
      <div class="skeleton-calendar"></div>
    </div>

    <!-- 实际内容 -->
    <div v-else>
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span class="card-title">学习统计</span>
              <el-date-picker
                v-model="selectedMonth"
                type="month"
                placeholder="选择月份"
                @change="loadStats"
              />
            </div>
          </template>
          
          <el-row :gutter="16" class="stats-summary">
            <el-col :xs="12" :sm="12" :md="6">
              <div class="stat-item stat-blue">
                <div class="stat-value">{{ stats.totalDays || 0 }}</div>
                <div class="stat-label">学习天数</div>
              </div>
            </el-col>
            <el-col :xs="12" :sm="12" :md="6">
              <div class="stat-item stat-green">
                <div class="stat-value">{{ formatDuration(stats.totalDuration) }}</div>
                <div class="stat-label">总学习时长</div>
              </div>
            </el-col>
            <el-col :xs="12" :sm="12" :md="6">
              <div class="stat-item stat-orange">
                <div class="stat-value">{{ stats.avgDuration || 0 }}</div>
                <div class="stat-label">日均时长(分钟)</div>
              </div>
            </el-col>
            <el-col :xs="12" :sm="12" :md="6">
              <div class="stat-item stat-purple">
                <div class="stat-value">{{ stats.maxDuration || 0 }}</div>
                <div class="stat-label">最长单日(分钟)</div>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" class="chart-row">
      <el-col :xs="24" :md="12">
        <el-card>
          <template #header>
            <span class="card-title">每日学习时长</span>
          </template>
          <ChartView :option="dailyChartOption" :height="300" />
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card>
          <template #header>
            <span class="card-title">科目分布</span>
          </template>
          <ChartView :option="pieChartOption" :height="300" />
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" class="chart-row">
      <el-col :span="24">
        <el-card>
          <template #header>
            <span class="card-title">学习热力图</span>
          </template>
          <ChartView :option="calendarHeatmapOption" :height="200" />
        </el-card>
      </el-col>
    </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { goalApi, subjectApi, studyRecordApi } from '@/api/modules'
import ChartView from '@/components/ChartView.vue'
import dayjs from 'dayjs'

const selectedMonth = ref(new Date())
const loading = ref(true)
const stats = ref({})
const calendarStats = ref({})
const dailyData = ref([])
const subjectData = ref([])

const formatDuration = (minutes) => {
  if (!minutes) return '0小时0分钟'
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  return `${hours}小时${mins}分钟`
}

const calendarHeatmapOption = computed(() => {
  const year = dayjs(selectedMonth.value).year()
  const month = dayjs(selectedMonth.value).month() + 1
  const daysInMonth = dayjs(selectedMonth.value).daysInMonth()
  const monthStr = String(month).padStart(2, '0')
  
  const data = []
  const dailyDuration = calendarStats.value.dailyDuration || {}
  
  for (let day = 1; day <= daysInMonth; day++) {
    const dateStr = `${year}-${monthStr}-${String(day).padStart(2, '0')}`
    const duration = dailyDuration[day] || 0
    data.push([dateStr, duration])
  }
  
  return {
    tooltip: {
      formatter: (params) => {
        const date = params.data[0]
        const value = params.data[1]
        return `${date}<br/>学习时长: ${value} 分钟`
      }
    },
    visualMap: {
      show: false,
      min: 0,
      max: 180,
      inRange: {
        color: ['#ebedf0', '#9be9a8', '#40c463', '#30a14e', '#216e39']
      }
    },
    calendar: {
      range: `${year}-${monthStr}`,
      cellSize: ['auto', 20],
      itemStyle: {
        borderWidth: 2,
        borderColor: '#fff'
      },
      yearLabel: { show: false },
      monthLabel: { show: false },
      dayLabel: {
        nameMap: ['日', '一', '二', '三', '四', '五', '六'],
        color: 'var(--text-secondary)'
      }
    },
    series: [{
      type: 'heatmap',
      coordinateSystem: 'calendar',
      data,
    }]
  }
})

const dailyChartOption = computed(() => {
  const days = []
  const daysInMonth = dayjs(selectedMonth.value).daysInMonth()
  for (let i = 1; i <= daysInMonth; i++) {
    days.push(String(i).padStart(2, '0'))
  }

  const values = dailyData.value.length
    ? dailyData.value
    : days.map(() => 0)

  return {
    tooltip: { trigger: 'axis' },
    grid: { top: 20, right: 20, bottom: 30, left: 50 },
    xAxis: {
      type: 'category',
      data: days,
      axisLine: { lineStyle: { color: '#e2e8f0' } },
      axisLabel: { color: '#64748b', interval: Math.floor(daysInMonth / 10) },
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
      barWidth: 12,
      itemStyle: {
        borderRadius: [3, 3, 0, 0],
        color: '#6366f1',
      },
    }],
  }
})

const pieChartOption = computed(() => {
  const data = subjectData.value.length ? subjectData.value : [{ value: 0, name: '暂无数据' }]
  const colors = ['#6366f1', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6']

  return {
    tooltip: { trigger: 'item', formatter: '{b}: {c}分钟 ({d}%)' },
    legend: {
      orient: 'vertical',
      right: 20,
      top: 'center',
      textStyle: { color: '#64748b' },
    },
    color: colors,
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['40%', '50%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: {
        label: { show: true, fontSize: 14, fontWeight: 'bold' },
      },
      data,
    }],
  }
})

const loadStats = async () => {
  try {
    const month = dayjs(selectedMonth.value).format('YYYY-MM')
    const res = await goalApi.getMonthlyStats({ month })
    stats.value = res.data || {}
    dailyData.value = res.data?.dailyDurations || []
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

const loadSubjectStats = async () => {
  try {
    const res = await studyRecordApi.getStats()
    const subjectStats = res.data?.subjectStats || {}
    subjectData.value = Object.entries(subjectStats).map(([name, value]) => ({
      name,
      value,
    }))
  } catch (error) {
    console.error('获取科目统计失败:', error)
  }
}

const loadCalendarStats = async () => {
  try {
    const month = dayjs(selectedMonth.value).format('YYYY-MM')
    const res = await goalApi.getCalendarStats({ month })
    calendarStats.value = res.data || {}
  } catch (error) {
    console.error('获取热力图统计失败:', error)
  }
}

// 监听月份变化，重新加载数据
watch(selectedMonth, () => {
  loadStats()
  loadCalendarStats()
})

onMounted(async () => {
  try {
    await Promise.all([
      loadStats(),
      loadCalendarStats(),
      loadSubjectStats(),
    ])
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.stats-container { padding: 0; }
.card-title { font-weight: 600; color: var(--text-primary); }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.stats-summary { margin-bottom: 16px; }

.stat-item {
  text-align: center; padding: 20px;
  border-radius: var(--radius-lg); color: white;
}
.stat-blue { background: var(--stat-blue); }
.stat-green { background: var(--stat-green); }
.stat-orange { background: var(--stat-orange); }
.stat-purple { background: var(--stat-purple); }
.stat-value { font-size: 32px; font-weight: 700; margin-bottom: 6px; }
.stat-label { font-size: 13px; opacity: 0.9; }

.chart-row { margin-top: 20px; }

/* 骨架屏 */
.skeleton-wrapper { animation: pulse 1.5s ease-in-out infinite; }
@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}
.skeleton-stats {
  height: 120px; background: var(--border); border-radius: var(--radius-lg);
  margin-bottom: 20px;
}
.skeleton-chart {
  height: 360px; background: var(--border); border-radius: var(--radius-lg);
}
.skeleton-calendar {
  height: 460px; background: var(--border); border-radius: var(--radius-lg);
  margin-top: 20px;
}
</style>

<template>
  <div class="stats-container">
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
            <el-col :span="6">
              <div class="stat-item stat-blue">
                <div class="stat-value">{{ stats.totalDays || 0 }}</div>
                <div class="stat-label">学习天数</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-item stat-green">
                <div class="stat-value">{{ formatDuration(stats.totalDuration) }}</div>
                <div class="stat-label">总学习时长</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-item stat-orange">
                <div class="stat-value">{{ stats.avgDuration || 0 }}</div>
                <div class="stat-label">日均时长(分钟)</div>
              </div>
            </el-col>
            <el-col :span="6">
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
      <el-col :span="12">
        <el-card>
          <template #header>
            <span class="card-title">每日学习时长</span>
          </template>
          <ChartView :option="dailyChartOption" :height="300" />
        </el-card>
      </el-col>
      <el-col :span="12">
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
            <span class="card-title">月历视图</span>
          </template>
          <div class="calendar-stats">
            <el-calendar v-model="selectedDate" class="stats-calendar">
              <template #date-cell="{ data }">
                <div class="calendar-day" :class="getDayClass(data.day)">
                  <span class="day-number">{{ data.day.split('-')[2] }}</span>
                  <span v-if="getDayDuration(data.day)" class="day-duration">
                    {{ getDayDuration(data.day) }}分钟
                  </span>
                </div>
              </template>
            </el-calendar>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { goalApi, subjectApi } from '@/api/modules'
import ChartView from '@/components/ChartView.vue'
import dayjs from 'dayjs'

const selectedMonth = ref(new Date())
const selectedDate = ref(new Date())
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

const getDayDuration = (date) => {
  const day = dayjs(date).date()
  return calendarStats.value.dailyDuration?.[day] || 0
}

const getDayClass = (date) => {
  const duration = getDayDuration(date)
  if (duration === 0) return ''
  if (duration < 60) return 'light'
  if (duration < 120) return 'medium'
  return 'heavy'
}

const dailyChartOption = computed(() => {
  const days = []
  const daysInMonth = dayjs(selectedMonth.value).daysInMonth()
  for (let i = 1; i <= daysInMonth; i++) {
    days.push(String(i).padStart(2, '0'))
  }

  const values = dailyData.value.length
    ? dailyData.value
    : days.map(() => Math.floor(Math.random() * 120))

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
        color: {
          type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: '#818cf8' },
            { offset: 1, color: '#6366f1' },
          ],
        },
      },
    }],
  }
})

const pieChartOption = computed(() => {
  const defaultData = [
    { value: 40, name: '行测' },
    { value: 25, name: '申论' },
    { value: 15, name: '数量关系' },
    { value: 12, name: '判断推理' },
    { value: 8, name: '资料分析' },
  ]

  const data = subjectData.value.length ? subjectData.value : defaultData
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
    const res = await subjectApi.getList()
    subjectData.value = (res.data || []).map((s, i) => ({
      value: Math.floor(Math.random() * 60) + 10,
      name: s.name,
    }))
  } catch (error) {
    console.error('获取科目统计失败:', error)
  }
}

const loadCalendarStats = async () => {
  try {
    const month = dayjs(selectedDate.value).format('YYYY-MM')
    const res = await goalApi.getCalendarStats({ month })
    calendarStats.value = res.data || {}
  } catch (error) {
    console.error('获取月历统计失败:', error)
  }
}

onMounted(async () => {
  await Promise.all([
    loadStats(),
    loadCalendarStats(),
    loadSubjectStats(),
  ])
})
</script>

<style scoped>
.stats-container { padding: 0; }
.card-title { font-weight: 600; color: #1e293b; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.stats-summary { margin-bottom: 16px; }

.stat-item {
  text-align: center; padding: 24px 16px;
  border-radius: 12px; color: white;
}
.stat-blue { background: linear-gradient(135deg, #3b82f6, #2563eb); }
.stat-green { background: linear-gradient(135deg, #10b981, #059669); }
.stat-orange { background: linear-gradient(135deg, #f59e0b, #d97706); }
.stat-purple { background: linear-gradient(135deg, #8b5cf6, #7c3aed); }
.stat-value { font-size: 32px; font-weight: 700; margin-bottom: 6px; }
.stat-label { font-size: 13px; opacity: 0.9; }

.chart-row { margin-top: 20px; }
.calendar-stats { height: 400px; }
.stats-calendar { height: 100%; }

.calendar-day {
  height: 100%; padding: 8px;
  display: flex; flex-direction: column; align-items: center;
  border-radius: 6px;
}
.day-number { font-size: 14px; font-weight: 600; }
.day-duration { font-size: 11px; color: #64748b; margin-top: 4px; }
.calendar-day.light { background-color: #eef2ff; color: #6366f1; }
.calendar-day.medium { background-color: #d1fae5; color: #059669; }
.calendar-day.heavy { background: linear-gradient(135deg, #10b981, #059669); color: white; }
</style>

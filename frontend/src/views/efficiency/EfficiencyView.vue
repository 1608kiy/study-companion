<template>
  <div class="efficiency-container">
    <div class="page-header">
      <h2>学习效率分析</h2>
      <p class="subtitle">基于最近30天数据分析</p>
    </div>

    <div v-if="loading" class="skeleton-wrapper">
      <div v-for="i in 4" :key="i" class="skeleton-card"></div>
    </div>

    <div v-else>
      <!-- 总体统计 -->
      <el-row :gutter="20" class="stats-row">
        <el-col :xs="12" :md="6">
          <div class="stat-card stat-blue">
            <div class="stat-value">{{ analysis.totalSessions || 0 }}</div>
            <div class="stat-label">学习次数</div>
          </div>
        </el-col>
        <el-col :xs="12" :md="6">
          <div class="stat-card stat-green">
            <div class="stat-value">{{ analysis.avgFocusLevel || 0 }}</div>
            <div class="stat-label">平均专注度</div>
          </div>
        </el-col>
        <el-col :xs="12" :md="6">
          <div class="stat-card stat-orange">
            <div class="stat-value">{{ bestHourText }}</div>
            <div class="stat-label">最佳学习时段</div>
          </div>
        </el-col>
        <el-col :xs="12" :md="6">
          <div class="stat-card stat-purple">
            <div class="stat-value">{{ bestDayText }}</div>
            <div class="stat-label">最佳学习日</div>
          </div>
        </el-col>
      </el-row>

      <el-row :gutter="20" class="chart-row">
        <!-- 每小时学习分布 -->
        <el-col :xs="24" :md="12">
          <el-card class="chart-card">
            <template #header>
              <span class="card-title">学习时段分布</span>
            </template>
            <ChartView :option="hourlyChartOption" :height="300" />
          </el-card>
        </el-col>

        <!-- 科目分布 -->
        <el-col :xs="24" :md="12">
          <el-card class="chart-card">
            <template #header>
              <span class="card-title">科目时长分布</span>
            </template>
            <ChartView :option="subjectChartOption" :height="300" />
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" class="chart-row">
        <!-- 专注度趋势 -->
        <el-col :xs="24" :md="12">
          <el-card class="chart-card">
            <template #header>
              <span class="card-title">专注度趋势</span>
            </template>
            <ChartView :option="focusTrendOption" :height="300" />
          </el-card>
        </el-col>

        <!-- 科目专注度 -->
        <el-col :xs="24" :md="12">
          <el-card class="chart-card">
            <template #header>
              <span class="card-title">科目专注度对比</span>
            </template>
            <ChartView :option="subjectFocusOption" :height="300" />
          </el-card>
        </el-col>
      </el-row>

      <!-- 最佳学习建议 -->
      <el-card class="advice-card">
        <template #header>
          <span class="card-title">学习建议</span>
        </template>
        <div class="advice-content">
          <div class="advice-item" v-if="analysis.bestHours?.length">
            <el-icon><Clock /></el-icon>
            <span>您在 <strong>{{ bestHoursText }}</strong> 时段学习效率最高，建议安排重要科目</span>
          </div>
          <div class="advice-item" v-if="bestFocusSubject">
            <el-icon><Star /></el-icon>
            <span>学习 <strong>{{ bestFocusSubject }}</strong> 时专注度最高，可参考此状态学习其他科目</span>
          </div>
          <div class="advice-item" v-if="analysis.avgFocusLevel < 3">
            <el-icon><Warning /></el-icon>
            <span>平均专注度偏低，建议使用番茄钟工作法，每25分钟休息5分钟</span>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { studyRecordApi } from '@/api/modules'
import ChartView from '@/components/ChartView.vue'

const loading = ref(true)
const analysis = ref({})

const bestHourText = computed(() => {
  const hour = analysis.value.bestHourOfDay
  if (hour == null) return '-'
  return `${hour}:00`
})

const bestDayText = computed(() => {
  const day = analysis.value.bestDayOfWeek
  if (day == null) return '-'
  const days = ['', '周一', '周二', '周三', '周四', '周五', '周六', '周日']
  return days[day] || '-'
})

const bestHoursText = computed(() => {
  const hours = analysis.value.bestHours || []
  return hours.map(h => `${h}:00`).join('、')
})

const bestFocusSubject = computed(() => {
  const focus = analysis.value.subjectFocus || {}
  const entries = Object.entries(focus)
  if (!entries.length) return null
  return entries.sort((a, b) => b[1] - a[1])[0][0]
})

const hourlyChartOption = computed(() => {
  const dist = analysis.value.hourlyDistribution || {}
  const hours = Array.from({ length: 24 }, (_, i) => i)
  const values = hours.map(h => dist[h] || 0)

  return {
    tooltip: { trigger: 'axis' },
    grid: { top: 20, right: 20, bottom: 30, left: 50 },
    xAxis: {
      type: 'category',
      data: hours.map(h => `${h}时`),
      axisLabel: { color: '#64748b', interval: 2 },
    },
    yAxis: {
      type: 'value',
      name: '分钟',
      axisLine: { show: false },
      splitLine: { lineStyle: { color: '#f1f5f9' } },
    },
    series: [{
      type: 'bar',
      data: values,
      barWidth: 16,
      itemStyle: {
        borderRadius: [4, 4, 0, 0],
        color: '#6366f1',
      },
    }],
  }
})

const subjectChartOption = computed(() => {
  const dist = analysis.value.subjectDistribution || {}
  const data = Object.entries(dist).map(([name, value]) => ({ name, value }))
  
  return {
    tooltip: { trigger: 'item', formatter: '{b}: {c}分钟 ({d}%)' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      data,
      label: { show: true, formatter: '{b}\n{d}%' },
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
    }],
  }
})

const focusTrendOption = computed(() => {
  const trend = analysis.value.focusTrend || []
  const dates = trend.map(t => t.date?.substring(5))
  const focusLevels = trend.map(t => t.focusLevel)
  const aiFocusLevels = trend.map(t => t.aiFocusLevel)

  return {
    tooltip: { trigger: 'axis' },
    legend: { data: ['自评专注度', 'AI专注度'], bottom: 0 },
    grid: { top: 20, right: 20, bottom: 40, left: 40 },
    xAxis: {
      type: 'category',
      data: dates,
      axisLabel: { color: '#64748b', rotate: 45 },
    },
    yAxis: {
      type: 'value',
      min: 0,
      max: 5,
      axisLine: { show: false },
      splitLine: { lineStyle: { color: '#f1f5f9' } },
    },
    series: [
      {
        name: '自评专注度',
        type: 'line',
        data: focusLevels,
        smooth: true,
        itemStyle: { color: '#6366f1' },
      },
      {
        name: 'AI专注度',
        type: 'line',
        data: aiFocusLevels,
        smooth: true,
        itemStyle: { color: '#10b981' },
      },
    ],
  }
})

const subjectFocusOption = computed(() => {
  const focus = analysis.value.subjectFocus || {}
  const subjects = Object.keys(focus)
  const values = Object.values(focus)

  return {
    tooltip: { trigger: 'axis' },
    grid: { top: 20, right: 20, bottom: 30, left: 100 },
    xAxis: {
      type: 'value',
      min: 0,
      max: 5,
      axisLine: { show: false },
      splitLine: { lineStyle: { color: '#f1f5f9' } },
    },
    yAxis: {
      type: 'category',
      data: subjects,
      axisLabel: { color: '#64748b' },
    },
    series: [{
      type: 'bar',
      data: values,
      barWidth: 20,
      itemStyle: {
        borderRadius: [0, 4, 4, 0],
        color: '#10b981',
      },
    }],
  }
})

onMounted(async () => {
  try {
    const res = await studyRecordApi.getEfficiency()
    analysis.value = res.data || {}
  } catch (error) {
    console.error('获取效率分析失败:', error)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.efficiency-container { padding: 0; }

.page-header { margin-bottom: 24px; }
.page-header h2 { font-size: 24px; font-weight: 700; color: var(--text-primary); margin-bottom: 4px; }
.subtitle { color: var(--text-secondary); font-size: 14px; }

.stats-row { margin-bottom: 20px; }

.stat-card {
  background: white;
  border-radius: var(--radius-lg);
  padding: 20px;
  text-align: center;
  border: 1px solid var(--border);
}

.stat-blue { border-left: 4px solid var(--stat-blue); }
.stat-green { border-left: 4px solid var(--stat-green); }
.stat-orange { border-left: 4px solid var(--stat-orange); }
.stat-purple { border-left: 4px solid var(--stat-purple); }

.stat-value { font-size: 28px; font-weight: 700; color: var(--text-primary); }
.stat-label { font-size: 13px; color: var(--text-secondary); margin-top: 4px; }

.chart-row { margin-bottom: 20px; }
.chart-card { height: 100%; }
.card-title { font-weight: 600; color: var(--text-primary); }

.advice-card { margin-bottom: 20px; }

.advice-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.advice-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px;
  background: var(--bg-page);
  border-radius: var(--radius);
}

.advice-item .el-icon {
  color: var(--primary);
  margin-top: 2px;
}

.advice-item span {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
}

.skeleton-wrapper {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.skeleton-card {
  height: 360px;
  background: var(--border);
  border-radius: var(--radius-lg);
  animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}
</style>

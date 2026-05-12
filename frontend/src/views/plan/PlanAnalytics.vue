<template>
  <div class="plan-analytics">
    <!-- 时间维度切换 -->
    <div class="time-selector">
      <el-radio-group v-model="timeRange" size="small" @change="loadAnalytics">
        <el-radio-button label="week">本周</el-radio-button>
        <el-radio-button label="month">本月</el-radio-button>
        <el-radio-button label="year">本年</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 快速统计卡片 -->
    <div class="quick-stats">
      <div class="stat-card">
        <div class="stat-icon" style="background: rgba(99, 102, 241, 0.1)">📊</div>
        <div class="stat-content">
          <div class="stat-value">{{ quickStats.totalPlans }}</div>
          <div class="stat-label">总任务数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: rgba(16, 185, 129, 0.1)">✅</div>
        <div class="stat-content">
          <div class="stat-value">{{ quickStats.completedPlans }}</div>
          <div class="stat-label">已完成</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: rgba(245, 158, 11, 0.1)">⏱</div>
        <div class="stat-content">
          <div class="stat-value">{{ quickStats.totalHours }}h</div>
          <div class="stat-label">总用时</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: rgba(239, 68, 68, 0.1)">📈</div>
        <div class="stat-content">
          <div class="stat-value">{{ quickStats.avgCompletionRate }}%</div>
          <div class="stat-label">平均完成率</div>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-grid">
      <!-- 完成率趋势 -->
      <div class="chart-card">
        <div class="chart-header">
          <h3>完成率趋势</h3>
        </div>
        <div ref="trendChartRef" class="chart-container"></div>
      </div>

      <!-- 分类分布 -->
      <div class="chart-card">
        <div class="chart-header">
          <h3>分类分布</h3>
        </div>
        <div ref="categoryChartRef" class="chart-container"></div>
      </div>

      <!-- 优先级分布 -->
      <div class="chart-card">
        <div class="chart-header">
          <h3>优先级分布</h3>
        </div>
        <div ref="priorityChartRef" class="chart-container"></div>
      </div>

      <!-- 时间分配 -->
      <div class="chart-card">
        <div class="chart-header">
          <h3>时间分配</h3>
        </div>
        <div ref="timeChartRef" class="chart-container"></div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import * as echarts from 'echarts'
import dayjs from 'dayjs'
import { planApi } from '@/api/plan'

const timeRange = ref('week')
const quickStats = ref({
  totalPlans: 0,
  completedPlans: 0,
  totalHours: 0,
  avgCompletionRate: 0
})

const trendChartRef = ref<HTMLElement>()
const categoryChartRef = ref<HTMLElement>()
const priorityChartRef = ref<HTMLElement>()
const timeChartRef = ref<HTMLElement>()

let trendChart: echarts.ECharts | null = null
let categoryChart: echarts.ECharts | null = null
let priorityChart: echarts.ECharts | null = null
let timeChart: echarts.ECharts | null = null

const categories = [
  { value: 'WORK', label: '工作', color: '#6366f1' },
  { value: 'STUDY', label: '学习', color: '#10b981' },
  { value: 'LIFE', label: '生活', color: '#f59e0b' },
  { value: 'HEALTH', label: '健康', color: '#ef4444' }
]

const priorities = [
  { value: 'P0', label: 'P0 紧急', color: '#ef4444' },
  { value: 'P1', label: 'P1 重要', color: '#f97316' },
  { value: 'P2', label: 'P2 普通', color: '#3b82f6' },
  { value: 'P3', label: 'P3 低优', color: '#6b7280' }
]

function getDateRange() {
  const now = dayjs()
  let startDate: dayjs.Dayjs
  let endDate = now

  switch (timeRange.value) {
    case 'week':
      startDate = now.startOf('week')
      break
    case 'month':
      startDate = now.startOf('month')
      break
    case 'year':
      startDate = now.startOf('year')
      break
    default:
      startDate = now.startOf('week')
  }

  return {
    startDate: startDate.format('YYYY-MM-DD'),
    endDate: endDate.format('YYYY-MM-DD')
  }
}

async function loadAnalytics() {
  const { startDate, endDate } = getDateRange()

  try {
    const [trend, category, priority, time] = await Promise.all([
      planApi.getCompletionTrend(startDate, endDate),
      planApi.getCategoryDistribution(startDate, endDate),
      planApi.getPriorityDistribution(startDate, endDate),
      planApi.getTimeDistribution(startDate, endDate)
    ])

    // 更新快速统计
    quickStats.value.totalPlans = trend.reduce((sum, t) => sum + t.total, 0)
    quickStats.value.completedPlans = trend.reduce((sum, t) => sum + t.done, 0)
    quickStats.value.totalHours = Math.round((time.total || 0) / 60)
    quickStats.value.avgCompletionRate = Math.round(
      trend.reduce((sum, t) => sum + t.completionRate, 0) / (trend.length || 1)
    )

    // 更新图表
    updateTrendChart(trend)
    updateCategoryChart(category)
    updatePriorityChart(priority)
    updateTimeChart(time)
  } catch (error) {
    console.error('Failed to load analytics', error)
  }
}

function updateTrendChart(data: any[]) {
  if (!trendChart) return

  const option = {
    tooltip: {
      trigger: 'axis'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: data.map(d => dayjs(d.date).format('MM-DD')),
      axisLine: { lineStyle: { color: '#64748b' } },
      axisLabel: { color: '#94a3b8' }
    },
    yAxis: {
      type: 'value',
      max: 100,
      axisLine: { lineStyle: { color: '#64748b' } },
      axisLabel: {
        color: '#94a3b8',
        formatter: '{value}%'
      },
      splitLine: { lineStyle: { color: 'rgba(255,255,255,0.05)' } }
    },
    series: [
      {
        name: '完成率',
        type: 'line',
        smooth: true,
        data: data.map(d => d.completionRate),
        lineStyle: { color: '#6366f1', width: 3 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(99, 102, 241, 0.3)' },
            { offset: 1, color: 'rgba(99, 102, 241, 0)' }
          ])
        },
        itemStyle: { color: '#6366f1' }
      }
    ]
  }

  trendChart.setOption(option)
}

function updateCategoryChart(data: any) {
  if (!categoryChart) return

  const chartData = categories.map(cat => ({
    name: cat.label,
    value: data.data?.[cat.value] || 0,
    itemStyle: { color: cat.color }
  }))

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: 10,
      top: 'center',
      textStyle: { color: '#94a3b8' }
    },
    series: [
      {
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 6,
          borderColor: '#1e293b',
          borderWidth: 2
        },
        label: { show: false },
        emphasis: {
          label: {
            show: true,
            fontSize: 14,
            fontWeight: 'bold',
            color: '#fff'
          }
        },
        data: chartData
      }
    ]
  }

  categoryChart.setOption(option)
}

function updatePriorityChart(data: any) {
  if (!priorityChart) return

  const chartData = priorities.map(pri => ({
    name: pri.label,
    value: data.data?.[pri.value] || 0,
    itemStyle: { color: pri.color }
  }))

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'value',
      axisLine: { lineStyle: { color: '#64748b' } },
      axisLabel: { color: '#94a3b8' },
      splitLine: { lineStyle: { color: 'rgba(255,255,255,0.05)' } }
    },
    yAxis: {
      type: 'category',
      data: priorities.map(p => p.label),
      axisLine: { lineStyle: { color: '#64748b' } },
      axisLabel: { color: '#94a3b8' }
    },
    series: [
      {
        type: 'bar',
        data: chartData,
        barWidth: '60%',
        itemStyle: {
          borderRadius: [0, 4, 4, 0]
        }
      }
    ]
  }

  priorityChart.setOption(option)
}

function updateTimeChart(data: any) {
  if (!timeChart) return

  const chartData = categories.map(cat => data.data?.[cat.value] || 0)
  const maxValue = Math.max(...chartData, 1)

  const option = {
    tooltip: {
      trigger: 'axis'
    },
    radar: {
      indicator: categories.map(cat => ({
        name: cat.label,
        max: maxValue * 1.2
      })),
      shape: 'circle',
      splitNumber: 4,
      axisName: {
        color: '#94a3b8'
      },
      splitLine: {
        lineStyle: {
          color: 'rgba(255,255,255,0.1)'
        }
      },
      splitArea: {
        show: false
      },
      axisLine: {
        lineStyle: {
          color: 'rgba(255,255,255,0.1)'
        }
      }
    },
    series: [
      {
        type: 'radar',
        data: [
          {
            value: chartData,
            name: '时间分配',
            areaStyle: {
              color: 'rgba(99, 102, 241, 0.3)'
            },
            lineStyle: {
              color: '#6366f1',
              width: 2
            },
            itemStyle: {
              color: '#6366f1'
            }
          }
        ]
      }
    ]
  }

  timeChart.setOption(option)
}

function initCharts() {
  if (trendChartRef.value) {
    trendChart = echarts.init(trendChartRef.value)
  }
  if (categoryChartRef.value) {
    categoryChart = echarts.init(categoryChartRef.value)
  }
  if (priorityChartRef.value) {
    priorityChart = echarts.init(priorityChartRef.value)
  }
  if (timeChartRef.value) {
    timeChart = echarts.init(timeChartRef.value)
  }

  // 响应式
  window.addEventListener('resize', handleResize)
}

function handleResize() {
  trendChart?.resize()
  categoryChart?.resize()
  priorityChart?.resize()
  timeChart?.resize()
}

function disposeCharts() {
  trendChart?.dispose()
  categoryChart?.dispose()
  priorityChart?.dispose()
  timeChart?.dispose()
  window.removeEventListener('resize', handleResize)
}

onMounted(() => {
  initCharts()
  loadAnalytics()
})

onUnmounted(disposeCharts)
</script>

<style lang="scss" scoped>
.plan-analytics {
  .time-selector {
    margin-bottom: 20px;
    display: flex;
    justify-content: center;
  }

  .quick-stats {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 16px;
    margin-bottom: 24px;

    .stat-card {
      display: flex;
      align-items: center;
      gap: 16px;
      padding: 20px;
      background: $bg-card;
      border: 1px solid $border;
      border-radius: $radius-md;
      transition: $transition-normal;

      &:hover {
        border-color: $primary;
        box-shadow: 0 4px 12px rgba(99, 102, 241, 0.15);
      }

      .stat-icon {
        width: 48px;
        height: 48px;
        border-radius: $radius-md;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 24px;
      }

      .stat-content {
        .stat-value {
          font-size: 24px;
          font-weight: 700;
          color: $text-primary;
          line-height: 1.2;
        }

        .stat-label {
          font-size: 13px;
          color: $text-secondary;
          margin-top: 4px;
        }
      }
    }
  }

  .charts-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 20px;

    @media (max-width: 768px) {
      grid-template-columns: 1fr;
    }

    .chart-card {
      background: $bg-card;
      border: 1px solid $border;
      border-radius: $radius-md;
      padding: 20px;
      min-height: 350px;

      .chart-header {
        margin-bottom: 16px;

        h3 {
          font-size: 16px;
          font-weight: 600;
          color: $text-primary;
          margin: 0;
        }
      }

      .chart-container {
        height: 280px;
      }
    }
  }
}
</style>

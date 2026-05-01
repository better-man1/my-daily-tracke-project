<template>
  <div class="dashboard">
    <!-- 标题 -->
    <div class="page-header">
      <div>
        <h1 class="page-title">数据看板</h1>
        <p class="page-subtitle">{{ greeting }}，{{ userStore.nickname }}</p>
      </div>
      <el-radio-group v-model="period" size="small" @change="loadData">
        <el-radio-button label="today">今日</el-radio-button>
        <el-radio-button label="week">本周</el-radio-button>
        <el-radio-button label="month">本月</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 统计卡片 -->
    <div class="stat-grid">
      <!-- 计划完成率 -->
      <div
        class="stat-card card--glow"
        style="--stat-color: #6366f1; --stat-bg: rgba(99, 102, 241, 0.12)"
      >
        <div class="stat-badge">
          {{ period === 'today' ? '今日' : period === 'week' ? '本周' : '本月' }}
        </div>
        <div class="stat-icon">📋</div>
        <div class="stat-value">
          {{ planStats.completionRate ?? 0 }}<span style="font-size: 16px">%</span>
        </div>
        <div class="stat-label">计划完成率</div>
        <div style="margin-top: 8px; font-size: 12px; color: #94a3b8">
          已完成 {{ planStats.done ?? 0 }} / 共 {{ planStats.total ?? 0 }} 个
        </div>
      </div>

      <!-- 支出 -->
      <div
        class="stat-card card--glow"
        style="--stat-color: #ef4444; --stat-bg: rgba(239, 68, 68, 0.12)"
      >
        <div class="stat-badge">支出</div>
        <div class="stat-icon">💸</div>
        <div class="stat-value">¥{{ formatAmount(accountingStats.totalExpense) }}</div>
        <div class="stat-label">支出金额</div>
        <div style="margin-top: 8px; font-size: 12px; color: #94a3b8">
          收入 ¥{{ formatAmount(accountingStats.totalIncome) }}
        </div>
      </div>

      <!-- 摘录数 -->
      <div
        class="stat-card card--glow"
        style="--stat-color: #f59e0b; --stat-bg: rgba(245, 158, 11, 0.12)"
      >
        <div class="stat-icon">📖</div>
        <div class="stat-value">{{ excerptCount }}</div>
        <div class="stat-label">摘录条数</div>
      </div>

      <!-- 总结天数 -->
      <div
        class="stat-card card--glow"
        style="--stat-color: #ec4899; --stat-bg: rgba(236, 72, 153, 0.12)"
      >
        <div class="stat-icon">✍️</div>
        <div class="stat-value">{{ summaryInfo.hasSummary ? '已完成' : '未完成' }}</div>
        <div class="stat-label">今日总结</div>
        <div v-if="summaryInfo.mood" style="margin-top: 8px; font-size: 12px; color: #94a3b8">
          心情：{{ moodEmoji[summaryInfo.mood - 1] }}
        </div>
      </div>

      <!-- 目标进度 -->
      <div
        class="stat-card card--glow"
        style="--stat-color: #3b82f6; --stat-bg: rgba(59, 130, 246, 0.12)"
      >
        <div class="stat-icon">🎯</div>
        <div class="stat-value">
          {{ goalStats.avgProgress ?? 0 }}<span style="font-size: 16px">%</span>
        </div>
        <div class="stat-label">目标平均进度</div>
        <div style="margin-top: 8px; font-size: 12px; color: #94a3b8">
          进行中 {{ goalStats.activeCount ?? 0 }} 个
        </div>
      </div>

      <!-- 连续打卡 -->
      <div
        class="stat-card card--glow"
        style="--stat-color: #10b981; --stat-bg: rgba(16, 185, 129, 0.12)"
      >
        <div class="stat-icon">🔥</div>
        <div class="stat-value">{{ streak.currentStreak ?? 0 }}</div>
        <div class="stat-label">连续总结天数</div>
        <div style="margin-top: 8px; font-size: 12px; color: #94a3b8">
          历史最长 {{ streak.longestStreak ?? 0 }} 天
        </div>
      </div>
    </div>

    <!-- 图表区 -->
    <div class="chart-grid">
      <!-- 本周计划趋势 -->
      <div class="card chart-card">
        <div class="card-title">本周计划趋势</div>
        <div ref="planChartRef" class="chart" />
      </div>

      <!-- 财务收支趋势 -->
      <div class="card chart-card">
        <div class="card-title flex justify-between items-center">
          <span>收支趋势 (近 7 天)</span>
        </div>
        <div ref="financeChartRef" class="chart" />
      </div>

      <!-- 情绪趋势 -->
      <div class="card chart-card">
        <div class="card-title">近 30 天情绪趋势</div>
        <div ref="moodChartRef" class="chart" />
      </div>

      <!-- 摘录趋势 -->
      <div class="card chart-card">
        <div class="card-title">近 7 天摘录数趋势</div>
        <div ref="excerptChartRef" class="chart" />
      </div>
    </div>

    <!-- 今日快速操作 -->
    <div class="quick-actions card">
      <div class="card-title">快速记录</div>
      <div class="action-grid">
        <router-link to="/plan" class="action-btn">
          <span class="action-icon">📋</span>
          <span>今日计划</span>
        </router-link>
        <router-link to="/accounting" class="action-btn">
          <span class="action-icon">💰</span>
          <span>记一笔账</span>
        </router-link>
        <router-link to="/excerpt" class="action-btn">
          <span class="action-icon">📖</span>
          <span>新增摘录</span>
        </router-link>
        <router-link to="/summary" class="action-btn">
          <span class="action-icon">✍️</span>
          <span>写总结</span>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useDark } from '@vueuse/core'
import { useUserStore } from '@/stores/user'
import { dashboardApi } from '@/api/dashboard'
import { summaryApi } from '@/api/summary'
import * as echarts from 'echarts/core'
import { BarChart, LineChart } from 'echarts/charts'
import {
  TooltipComponent,
  GridComponent,
  LegendComponent,
  GraphicComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import dayjs from 'dayjs'

echarts.use([
  BarChart,
  LineChart,
  TooltipComponent,
  GridComponent,
  LegendComponent,
  GraphicComponent,
  CanvasRenderer
])

const userStore = useUserStore()
const period = ref<'today' | 'week' | 'month'>('today')
const isDark = useDark()

watch(isDark, () => {
  const charts = [planChart, moodChart, financeChart, excerptChart]
  charts.forEach(c => {
    if (c) {
      c.dispose()
    }
  })
  planChart = null
  moodChart = null
  financeChart = null
  excerptChart = null
  renderAllCharts()
})

// 数据
const planStats = ref<Record<string, any>>({})
const accountingStats = ref<Record<string, any>>({})
const excerptCount = ref(0)
const summaryInfo = ref<Record<string, any>>({})
const goalStats = ref<Record<string, any>>({})
const streak = ref<Record<string, any>>({})
const moodTrend = ref<any[]>([])
const weekData = ref<Record<string, any>>({})

// 图表 refs
const planChartRef = ref<HTMLElement>()
const moodChartRef = ref<HTMLElement>()
const financeChartRef = ref<HTMLElement>()
const excerptChartRef = ref<HTMLElement>()

let planChart: echarts.ECharts | null = null
let moodChart: echarts.ECharts | null = null
let financeChart: echarts.ECharts | null = null
let excerptChart: echarts.ECharts | null = null
const trendData = ref<Record<string, any>>({})

const moodEmoji = ['😢', '😔', '😐', '😊', '😄']

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '凌晨好'
  if (h < 12) return '早上好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

function formatAmount(val: any) {
  const n = Number(val ?? 0)
  return n >= 10000 ? (n / 10000).toFixed(1) + 'w' : n.toFixed(2)
}

async function loadData() {
  // 今日概览
  const today = await dashboardApi.getToday()
  planStats.value = today.plan ?? {}
  accountingStats.value = today.accounting ?? {}
  excerptCount.value = today.excerptCount ?? 0
  summaryInfo.value = today.summary ?? {}
  goalStats.value = today.goal ?? {}

  // 本周数据（用于图表）
  const week = await dashboardApi.getWeek()
  weekData.value = week

  // 情绪趋势
  moodTrend.value = await summaryApi.getMoodTrend(30)

  // 连续天数
  try {
    const s = await summaryApi.getStreak()
    streak.value = s as any
  } catch {
    streak.value = {}
  }

  // 趋势分析（近7天）
  const end = dayjs().format('YYYY-MM-DD')
  const start = dayjs().subtract(6, 'day').format('YYYY-MM-DD')
  trendData.value = await dashboardApi.getTrend(start, end)

  await nextTick()
  renderAllCharts()
}

function renderAllCharts() {
  renderPlanChart()
  renderMoodChart()
  renderFinanceChart()
  renderExcerptChart()
}

function renderPlanChart() {
  if (!planChartRef.value) return
  if (!planChart) {
    planChart = echarts.init(planChartRef.value, isDark.value ? 'dark' : 'light')
  }
  const planByDate = weekData.value.planByDate ?? {}
  const donePlanByDate = weekData.value.donePlanByDate ?? {}
  const days: string[] = []
  for (let i = 6; i >= 0; i--) {
    days.push(dayjs().subtract(i, 'day').format('MM-DD'))
  }
  const dates = days.map((d) =>
    dayjs()
      .subtract(6 - days.indexOf(d), 'day')
      .format('YYYY-MM-DD')
  )

  planChart.setOption({
    backgroundColor: 'transparent',
    tooltip: { trigger: 'axis' },
    legend: { data: ['任务数', '已完成'], textStyle: { color: '#94a3b8' } },
    grid: { top: 40, right: 16, bottom: 24, left: 40 },
    xAxis: {
      type: 'category',
      data: days,
      axisLabel: { color: '#64748b', fontSize: 11 },
      axisLine: { lineStyle: { color: 'rgba(255,255,255,0.08)' } }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      axisLabel: { color: '#64748b', fontSize: 11 },
      splitLine: { lineStyle: { color: 'rgba(255,255,255,0.04)' } }
    },
    series: [
      {
        name: '任务数',
        type: 'bar',
        data: dates.map((d) => planByDate[d] ?? 0),
        itemStyle: { color: 'rgba(99,102,241,0.6)', borderRadius: [4, 4, 0, 0] }
      },
      {
        name: '已完成',
        type: 'bar',
        data: dates.map((d) => donePlanByDate[d] ?? 0),
        itemStyle: { color: '#6366f1', borderRadius: [4, 4, 0, 0] }
      }
    ]
  })
}

function renderMoodChart() {
  if (!moodChartRef.value) return
  if (!moodChart) {
    moodChart = echarts.init(moodChartRef.value, isDark.value ? 'dark' : 'light')
  }
  const data = moodTrend.value
  moodChart.setOption({
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      formatter: (params: any) => {
        const d = params[0]
        return `${d.name}<br/>心情：${moodEmoji[(d.value ?? 3) - 1]}`
      }
    },
    grid: { top: 20, right: 16, bottom: 24, left: 40 },
    xAxis: {
      type: 'category',
      data: data.map((d: any) => dayjs(d.date).format('MM-DD')),
      axisLabel: { color: '#64748b', fontSize: 10, rotate: 30 },
      axisLine: { lineStyle: { color: 'rgba(255,255,255,0.08)' } }
    },
    yAxis: {
      type: 'value',
      min: 1,
      max: 5,
      minInterval: 1,
      axisLabel: {
        color: '#64748b',
        fontSize: 11,
        formatter: (val: number) => moodEmoji[val - 1] ?? val
      },
      splitLine: { lineStyle: { color: 'rgba(255,255,255,0.04)' } }
    },
    series: [
      {
        type: 'line',
        data: data.map((d: any) => d.mood),
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: { color: '#ec4899', width: 2 },
        itemStyle: { color: '#ec4899' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(236,72,153,0.3)' },
            { offset: 1, color: 'rgba(236,72,153,0)' }
          ])
        }
      }
    ]
  })
}

function renderFinanceChart() {
  if (!financeChartRef.value) return
  if (!financeChart) {
    financeChart = echarts.init(financeChartRef.value, isDark.value ? 'dark' : 'light')
  }
  const data = trendData.value.financeTrend ?? {}
  const days: string[] = []
  for (let i = 6; i >= 0; i--) {
    days.push(dayjs().subtract(i, 'day').format('YYYY-MM-DD'))
  }

  financeChart.setOption({
    backgroundColor: 'transparent',
    tooltip: { trigger: 'axis' },
    legend: { data: ['收入', '支出'], textStyle: { color: '#94a3b8' } },
    grid: { top: 40, right: 16, bottom: 24, left: 40 },
    xAxis: {
      type: 'category',
      data: days.map(d => dayjs(d).format('MM-DD')),
      axisLabel: { color: '#64748b', fontSize: 11 },
      axisLine: { lineStyle: { color: 'rgba(255,255,255,0.08)' } }
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: '#64748b', fontSize: 11 },
      splitLine: { lineStyle: { color: 'rgba(255,255,255,0.04)' } }
    },
    series: [
      {
        name: '收入',
        type: 'line',
        data: days.map(d => data[d]?.income ?? 0),
        smooth: true,
        itemStyle: { color: '#10b981' }
      },
      {
        name: '支出',
        type: 'line',
        data: days.map(d => data[d]?.expense ?? 0),
        smooth: true,
        itemStyle: { color: '#ef4444' }
      }
    ]
  })
}

function renderExcerptChart() {
  if (!excerptChartRef.value) return
  if (!excerptChart) {
    excerptChart = echarts.init(excerptChartRef.value, isDark.value ? 'dark' : 'light')
  }
  const data = trendData.value.excerptTrend ?? {}
  const days: string[] = []
  for (let i = 6; i >= 0; i--) {
    days.push(dayjs().subtract(i, 'day').format('YYYY-MM-DD'))
  }

  excerptChart.setOption({
    backgroundColor: 'transparent',
    tooltip: { trigger: 'axis' },
    grid: { top: 20, right: 16, bottom: 24, left: 40 },
    xAxis: {
      type: 'category',
      data: days.map(d => dayjs(d).format('MM-DD')),
      axisLabel: { color: '#64748b', fontSize: 11 }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      axisLabel: { color: '#64748b', fontSize: 11 }
    },
    series: [
      {
        type: 'bar',
        data: days.map(d => data[d] ?? 0),
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#f59e0b' },
            { offset: 1, color: '#fbbf24' }
          ]),
          borderRadius: [4, 4, 0, 0]
        }
      }
    ]
  })
}

onMounted(() => {
  loadData()
  const resizeHandler = () => {
    planChart?.resize()
    moodChart?.resize()
    financeChart?.resize()
    excerptChart?.resize()
  }
  window.addEventListener('resize', resizeHandler)
  onUnmounted(() => {
    window.removeEventListener('resize', resizeHandler)
    planChart?.dispose()
    moodChart?.dispose()
    financeChart?.dispose()
    excerptChart?.dispose()
  })
})
</script>

<style lang="scss" scoped>
.dashboard {
  .stat-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 16px;
    margin-bottom: 20px;

    @media (max-width: 1200px) {
      grid-template-columns: repeat(2, 1fr);
    }
  }

  .chart-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 16px;
    margin-bottom: 20px;

    .chart-card {
      .card-title {
        font-size: 14px;
        font-weight: 600;
        color: $text-secondary;
        margin-bottom: 12px;
      }

      .chart {
        height: 220px;
        width: 100%;
      }
    }
  }

  .quick-actions {
    .card-title {
      font-size: 14px;
      font-weight: 600;
      color: $text-secondary;
      margin-bottom: 16px;
    }

    .action-grid {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 12px;
    }

    .action-btn {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 8px;
      padding: 20px 16px;
      background: $bg-input;
      border: 1px solid $border;
      border-radius: $radius-md;
      color: $text-secondary;
      text-decoration: none;
      transition: $transition-normal;
      font-size: 13px;

      &:hover {
        background: rgba($primary, 0.1);
        border-color: rgba($primary, 0.3);
        color: $primary-light;
        transform: translateY(-2px);

        .action-icon {
          transform: scale(1.15);
        }
      }

      .action-icon {
        font-size: 24px;
        transition: transform 0.2s ease;
      }
    }
  }
}
</style>

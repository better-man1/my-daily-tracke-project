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
        <el-radio-button label="year">年度</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 统计卡片 -->
    <div class="stat-grid">
      <!-- 计划完成率 -->
      <div class="stat-card card--glow" style="--stat-color: #6366f1; --stat-bg: rgba(99,102,241,0.12)">
        <div class="stat-badge">{{ periodLabel }}</div>
        <div class="stat-icon">📋</div>
        <div class="stat-value">{{ planStats.completionRate ?? 0 }}<span style="font-size:16px">%</span></div>
        <div class="stat-label">计划完成率</div>
        <div style="margin-top:8px;font-size:12px;color:#94a3b8">
          已完成 {{ planStats.done ?? 0 }} / 共 {{ planStats.total ?? 0 }} 个
        </div>
      </div>

      <!-- 支出 -->
      <div class="stat-card card--glow" style="--stat-color: #ef4444; --stat-bg: rgba(239,68,68,0.12)">
        <div class="stat-badge">支出</div>
        <div class="stat-icon">💸</div>
        <div class="stat-value">¥{{ formatAmount(accountingStats.totalExpense ?? accountingStats.expense) }}</div>
        <div class="stat-label">支出金额</div>
        <div style="margin-top:8px;font-size:12px;color:#94a3b8">
          收入 ¥{{ formatAmount(accountingStats.totalIncome ?? accountingStats.income) }}
        </div>
      </div>

      <!-- 摘录数 -->
      <div class="stat-card card--glow" style="--stat-color: #f59e0b; --stat-bg: rgba(245,158,11,0.12)">
        <div class="stat-icon">📖</div>
        <div class="stat-value">{{ excerptCount }}</div>
        <div class="stat-label">摘录条数</div>
      </div>

      <!-- 总结 -->
      <div class="stat-card card--glow" style="--stat-color: #ec4899; --stat-bg: rgba(236,72,153,0.12)">
        <div class="stat-icon">✍️</div>
        <div class="stat-value">{{ summaryText }}</div>
        <div class="stat-label">{{ period === 'today' ? '今日总结' : '总结天数' }}</div>
        <div v-if="summaryInfo.mood && period === 'today'" style="margin-top:8px;font-size:12px;color:#94a3b8">
          心情：{{ moodEmoji[summaryInfo.mood - 1] }}
        </div>
      </div>

      <!-- 目标进度 -->
      <div class="stat-card card--glow" style="--stat-color: #3b82f6; --stat-bg: rgba(59,130,246,0.12)">
        <div class="stat-icon">🎯</div>
        <div class="stat-value">{{ goalStats.avgProgress ?? 0 }}<span style="font-size:16px">%</span></div>
        <div class="stat-label">目标平均进度</div>
        <div style="margin-top:8px;font-size:12px;color:#94a3b8">
          进行中 {{ goalStats.activeCount ?? 0 }} 个
        </div>
      </div>

      <!-- 连续打卡 -->
      <div class="stat-card card--glow" style="--stat-color: #10b981; --stat-bg: rgba(16,185,129,0.12)">
        <div class="stat-icon">🔥</div>
        <div class="stat-value">{{ streak.currentStreak ?? 0 }}</div>
        <div class="stat-label">连续总结天数</div>
        <div style="margin-top:8px;font-size:12px;color:#94a3b8">
          历史最长 {{ streak.longestStreak ?? 0 }} 天
        </div>
      </div>
    </div>

    <!-- 图表区 -->
    <div class="chart-grid">
      <!-- 主图表（根据 period 动态变换） -->
      <div class="card chart-card">
        <div class="card-title">{{ primaryChartTitle }}</div>
        <div ref="primaryChartRef" class="chart" />
      </div>

      <!-- 情绪趋势（始终显示） -->
      <div class="card chart-card">
        <div class="card-title">近 30 天情绪趋势</div>
        <div ref="moodChartRef" class="chart" />
      </div>
    </div>

    <!-- 年度月度收支图（month/year 时显示） -->
    <div v-if="period === 'month' || period === 'year'" class="card chart-card--wide">
      <div class="card-title">{{ period === 'year' ? '年度月度收支分析' : '本月收支趋势' }}</div>
      <div ref="accChartRef" class="chart chart--tall" />
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
import { useUserStore } from '@/stores/user'
import { dashboardApi } from '@/api/dashboard'
import { summaryApi } from '@/api/summary'
import * as echarts from 'echarts'
import dayjs from 'dayjs'

const userStore = useUserStore()
const period = ref<'today' | 'week' | 'month' | 'year'>('today')

// 数据
const planStats = ref<Record<string, any>>({})
const accountingStats = ref<Record<string, any>>({})
const excerptCount = ref(0)
const summaryInfo = ref<Record<string, any>>({})
const goalStats = ref<Record<string, any>>({})
const streak = ref<Record<string, any>>({})
const moodTrend = ref<any[]>([])
const weekData = ref<Record<string, any>>({})
const monthData = ref<Record<string, any>>({})
const yearData = ref<Record<string, any>>({})
const trendData = ref<Record<string, any>>({})

// 图表 refs
const primaryChartRef = ref<HTMLElement>()
const moodChartRef = ref<HTMLElement>()
const accChartRef = ref<HTMLElement>()
let primaryChart: echarts.ECharts | null = null
let moodChart: echarts.ECharts | null = null
let accChart: echarts.ECharts | null = null

const moodEmoji = ['😢', '😔', '😐', '😊', '😄']

const periodLabel = computed(() => {
  const map: Record<string, string> = { today: '今日', week: '本周', month: '本月', year: '年度' }
  return map[period.value]
})

const primaryChartTitle = computed(() => {
  const map: Record<string, string> = {
    today: '今日计划分类',
    week: '本周计划趋势',
    month: '本月计划完成趋势',
    year: '年度目标达成'
  }
  return map[period.value]
})

const summaryText = computed(() => {
  if (period.value === 'today') {
    return summaryInfo.value.hasSummary ? '已完成' : '未完成'
  }
  const d = period.value === 'week'
    ? weekData.value?.weekSummaryDays
    : period.value === 'month'
      ? monthData.value?.summaryDays
      : yearData.value?.summaryDays
  return d ?? 0
})

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6)  return '凌晨好'
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
  // 今日概览（基础数据，始终加载）
  const today = await dashboardApi.getToday()
  planStats.value = today.plan ?? {}
  accountingStats.value = today.accounting ?? {}
  excerptCount.value = today.excerptCount ?? 0
  summaryInfo.value = today.summary ?? {}
  goalStats.value = today.goal ?? {}

  // 按 period 加载对应数据
  if (period.value === 'week') {
    weekData.value = await dashboardApi.getWeek()
    planStats.value = {
      total: Object.values(weekData.value.planByDate ?? {}).reduce((a: any, b: any) => +a + +b, 0),
      done: Object.values(weekData.value.donePlanByDate ?? {}).reduce((a: any, b: any) => +a + +b, 0),
      completionRate: 0
    }
    const t = planStats.value.total as number
    const d = planStats.value.done as number
    planStats.value.completionRate = t > 0 ? Math.round(d * 100 / t) : 0
    accountingStats.value = {
      totalIncome: weekData.value.weekIncome,
      totalExpense: weekData.value.weekExpense
    }
    excerptCount.value = weekData.value.weekExcerpts ?? 0
  } else if (period.value === 'month') {
    monthData.value = await dashboardApi.getMonth()
    planStats.value = {
      total: monthData.value.monthPlanTotal ?? 0,
      done: monthData.value.monthPlanDone ?? 0,
      completionRate: monthData.value.monthCompletionRate ?? 0
    }
    accountingStats.value = {
      totalIncome: monthData.value.monthIncome,
      totalExpense: monthData.value.monthExpense
    }
    // 本月 trend 用于收支图
    const now = dayjs()
    trendData.value = await dashboardApi.getTrend(
      now.startOf('month').format('YYYY-MM-DD'),
      now.endOf('month').format('YYYY-MM-DD'),
      'accounting'
    )
  } else if (period.value === 'year') {
    yearData.value = await dashboardApi.getYear()
    planStats.value = { total: yearData.value.yearlyGoalTotal ?? 0, done: yearData.value.yearlyGoalDone ?? 0 }
    const t = planStats.value.total as number
    const d = planStats.value.done as number
    planStats.value.completionRate = t > 0 ? Math.round(d * 100 / t) : 0
    accountingStats.value = {
      totalIncome: yearData.value.yearIncome,
      totalExpense: yearData.value.yearExpense
    }
    excerptCount.value = yearData.value.excerptCount ?? 0
    // 年度 trend（收支按月）
    const now = dayjs()
    trendData.value = await dashboardApi.getTrend(
      now.startOf('year').format('YYYY-MM-DD'),
      now.endOf('year').format('YYYY-MM-DD'),
      'accounting'
    )
  }

  // 情绪趋势（始终加载）
  moodTrend.value = await summaryApi.getMoodTrend(30)

  // 连续天数
  try {
    streak.value = await summaryApi.getStreak() as any
  } catch { streak.value = {} }

  await nextTick()
  renderPrimaryChart()
  renderMoodChart()
  if (period.value === 'month' || period.value === 'year') {
    renderAccChart()
  }
}

function renderPrimaryChart() {
  if (!primaryChartRef.value) return
  if (!primaryChart) primaryChart = echarts.init(primaryChartRef.value, 'dark')

  if (period.value === 'today') {
    // 今日：计划分类饼图
    const p = planStats.value
    primaryChart.setOption({
      backgroundColor: 'transparent',
      tooltip: { trigger: 'item' },
      legend: { bottom: 0, textStyle: { color: '#94a3b8', fontSize: 11 } },
      series: [{
        type: 'pie',
        radius: ['35%', '65%'],
        center: ['50%', '45%'],
        data: [
          { name: '已完成', value: p.done ?? 0, itemStyle: { color: '#10b981' } },
          { name: '未完成', value: Math.max(0, (p.total ?? 0) - (p.done ?? 0)), itemStyle: { color: '#475569' } }
        ],
        label: { color: '#94a3b8', fontSize: 12 },
        emphasis: { itemStyle: { shadowBlur: 10, shadowColor: 'rgba(0,0,0,0.3)' } }
      }]
    })
  } else if (period.value === 'week') {
    // 本周：每日计划趋势柱状图
    const planByDate = weekData.value.planByDate ?? {}
    const donePlanByDate = weekData.value.donePlanByDate ?? {}
    const days: string[] = []
    for (let i = 6; i >= 0; i--) days.push(dayjs().subtract(i, 'day').format('MM-DD'))
    const dates = days.map((_, i) => dayjs().subtract(6 - i, 'day').format('YYYY-MM-DD'))

    primaryChart.setOption({
      backgroundColor: 'transparent',
      tooltip: { trigger: 'axis' },
      legend: { data: ['任务数', '已完成'], textStyle: { color: '#94a3b8' } },
      grid: { top: 40, right: 16, bottom: 24, left: 40 },
      xAxis: {
        type: 'category', data: days,
        axisLabel: { color: '#64748b', fontSize: 11 },
        axisLine: { lineStyle: { color: 'rgba(255,255,255,0.08)' } }
      },
      yAxis: {
        type: 'value', minInterval: 1,
        axisLabel: { color: '#64748b', fontSize: 11 },
        splitLine: { lineStyle: { color: 'rgba(255,255,255,0.04)' } }
      },
      series: [
        {
          name: '任务数', type: 'bar',
          data: dates.map(d => planByDate[d] ?? 0),
          itemStyle: { color: 'rgba(99,102,241,0.5)', borderRadius: [4, 4, 0, 0] }
        },
        {
          name: '已完成', type: 'bar',
          data: dates.map(d => donePlanByDate[d] ?? 0),
          itemStyle: { color: '#6366f1', borderRadius: [4, 4, 0, 0] }
        }
      ]
    })
  } else if (period.value === 'month') {
    // 本月：每日完成率折线
    const now = dayjs()
    const daysInMonth = now.daysInMonth()
    const labels = Array.from({ length: daysInMonth }, (_, i) => `${i + 1}日`)
    const dates = Array.from({ length: daysInMonth }, (_, i) =>
      now.startOf('month').add(i, 'day').format('YYYY-MM-DD')
    )
    const planTotal = weekData.value.planByDate ?? {}
    const planDone = weekData.value.donePlanByDate ?? {}

    primaryChart.setOption({
      backgroundColor: 'transparent',
      tooltip: { trigger: 'axis' },
      grid: { top: 20, right: 16, bottom: 24, left: 40 },
      xAxis: {
        type: 'category', data: labels,
        axisLabel: { color: '#64748b', fontSize: 10, rotate: 30 },
        axisLine: { lineStyle: { color: 'rgba(255,255,255,0.08)' } }
      },
      yAxis: {
        type: 'value', min: 0, max: 100,
        axisLabel: { color: '#64748b', fontSize: 11, formatter: '{value}%' },
        splitLine: { lineStyle: { color: 'rgba(255,255,255,0.04)' } }
      },
      series: [{
        type: 'line', smooth: true,
        data: dates.map(d => {
          const t = planTotal[d] ?? 0
          const done = planDone[d] ?? 0
          return t > 0 ? Math.round(done * 100 / t) : null
        }),
        lineStyle: { color: '#6366f1', width: 2 },
        itemStyle: { color: '#6366f1' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(99,102,241,0.3)' },
            { offset: 1, color: 'rgba(99,102,241,0)' }
          ])
        }
      }]
    })
  } else if (period.value === 'year') {
    // 年度：目标达成情况柱状图
    const total = yearData.value.yearlyGoalTotal ?? 0
    const done = yearData.value.yearlyGoalDone ?? 0

    primaryChart.setOption({
      backgroundColor: 'transparent',
      tooltip: {},
      series: [{
        type: 'pie',
        radius: ['35%', '65%'],
        center: ['50%', '50%'],
        data: [
          { name: '已完成年度目标', value: done, itemStyle: { color: '#10b981' } },
          { name: '进行中', value: Math.max(0, total - done), itemStyle: { color: '#6366f1' } }
        ],
        label: {
          show: true,
          formatter: '{b}\n{d}%',
          color: '#94a3b8', fontSize: 12
        }
      }]
    })
  }
}

function renderMoodChart() {
  if (!moodChartRef.value) return
  if (!moodChart) moodChart = echarts.init(moodChartRef.value, 'dark')
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
      type: 'value', min: 1, max: 5, minInterval: 1,
      axisLabel: {
        color: '#64748b', fontSize: 11,
        formatter: (val: number) => moodEmoji[val - 1] ?? val
      },
      splitLine: { lineStyle: { color: 'rgba(255,255,255,0.04)' } }
    },
    series: [{
      type: 'line',
      data: data.map((d: any) => d.mood),
      smooth: true, symbol: 'circle', symbolSize: 6,
      lineStyle: { color: '#ec4899', width: 2 },
      itemStyle: { color: '#ec4899' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(236,72,153,0.3)' },
          { offset: 1, color: 'rgba(236,72,153,0)' }
        ])
      }
    }]
  })
}

function renderAccChart() {
  if (!accChartRef.value) return
  if (!accChart) accChart = echarts.init(accChartRef.value, 'dark')

  const incomeByDate = trendData.value.incomeByDate ?? {}
  const expenseByDate = trendData.value.expenseByDate ?? {}

  let labels: string[] = []
  let incomeData: number[] = []
  let expenseData: number[] = []

  if (period.value === 'month') {
    // 本月按日
    const now = dayjs()
    const days = now.daysInMonth()
    for (let i = 1; i <= days; i++) {
      const dateKey = now.startOf('month').add(i - 1, 'day').format('YYYY-MM-DD')
      labels.push(`${i}日`)
      incomeData.push(Number(incomeByDate[dateKey] ?? 0))
      expenseData.push(Number(expenseByDate[dateKey] ?? 0))
    }
  } else {
    // 年度按月汇总
    const year = dayjs().year()
    for (let m = 1; m <= 12; m++) {
      const monthStr = `${year}-${String(m).padStart(2, '0')}`
      labels.push(`${m}月`)
      let inc = 0, exp = 0
      Object.keys(incomeByDate).forEach(k => { if (k.startsWith(monthStr)) inc += Number(incomeByDate[k]) })
      Object.keys(expenseByDate).forEach(k => { if (k.startsWith(monthStr)) exp += Number(expenseByDate[k]) })
      incomeData.push(inc)
      expenseData.push(exp)
    }
  }

  accChart.setOption({
    backgroundColor: 'transparent',
    tooltip: { trigger: 'axis', formatter: (p: any) => p.map((s: any) => `${s.seriesName}：¥${Number(s.value).toFixed(2)}`).join('<br/>') },
    legend: { data: ['收入', '支出'], textStyle: { color: '#94a3b8' } },
    grid: { top: 40, right: 16, bottom: 24, left: 60 },
    xAxis: {
      type: 'category', data: labels,
      axisLabel: { color: '#64748b', fontSize: 11 },
      axisLine: { lineStyle: { color: 'rgba(255,255,255,0.08)' } }
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: '#64748b', fontSize: 11, formatter: (v: number) => v >= 1000 ? `${(v/1000).toFixed(1)}k` : v },
      splitLine: { lineStyle: { color: 'rgba(255,255,255,0.04)' } }
    },
    series: [
      {
        name: '收入', type: 'bar', data: incomeData,
        itemStyle: { color: '#10b981', borderRadius: [4, 4, 0, 0] }
      },
      {
        name: '支出', type: 'bar', data: expenseData,
        itemStyle: { color: '#ef4444', borderRadius: [4, 4, 0, 0] }
      }
    ]
  })
}

// 监听 period 变化重新渲染收支图
watch(period, async (val) => {
  if (val === 'month' || val === 'year') {
    await nextTick()
    if (accChartRef.value && !accChart) {
      accChart = echarts.init(accChartRef.value, 'dark')
    }
    renderAccChart()
  }
})

onMounted(() => {
  loadData()
  const resizeHandler = () => {
    primaryChart?.resize()
    moodChart?.resize()
    accChart?.resize()
  }
  window.addEventListener('resize', resizeHandler)
  onUnmounted(() => {
    window.removeEventListener('resize', resizeHandler)
    primaryChart?.dispose()
    moodChart?.dispose()
    accChart?.dispose()
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
    margin-bottom: 16px;

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

  .chart-card--wide {
    margin-bottom: 20px;
    padding: 20px;

    .card-title {
      font-size: 14px;
      font-weight: 600;
      color: $text-secondary;
      margin-bottom: 12px;
    }

    .chart--tall {
      height: 260px;
      width: 100%;
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

        .action-icon { transform: scale(1.15); }
      }

      .action-icon {
        font-size: 24px;
        transition: transform 0.2s ease;
      }
    }
  }
}
</style>

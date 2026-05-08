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
        <el-radio-button label="year">本年</el-radio-button>
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
          {{ period === 'today' ? '今日' : period === 'week' ? '本周' : period === 'month' ? '本月' : '本年' }}
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
        <div class="stat-badge">
          {{ period === 'today' ? '今日' : period === 'week' ? '本周' : period === 'month' ? '本月' : '本年' }}
        </div>
        <div class="stat-icon">💸</div>
        <div class="stat-value">¥{{ formatAmount(accountingStats.totalExpense || accountingStats.expense) }}</div>
        <div class="stat-label">支出金额</div>
        <div style="margin-top: 8px; font-size: 12px; color: #94a3b8">
          收入 ¥{{ formatAmount(accountingStats.totalIncome || accountingStats.income) }}
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
        <div class="stat-value" v-if="period === 'today'">{{ summaryInfo.hasSummary ? '已完成' : '未完成' }}</div>
        <div class="stat-value" v-else>{{ summaryInfo.days ?? 0 }}<span style="font-size: 16px"> 天</span></div>
        <div class="stat-label">
          {{ period === 'today' ? '今日总结' : period === 'week' ? '本周总结' : period === 'month' ? '本月总结' : '本年总结' }}
        </div>
        <div v-if="period === 'today' && summaryInfo.mood" style="margin-top: 8px; font-size: 12px; color: #94a3b8">
          心情：{{ moodEmoji[summaryInfo.mood - 1] }}
        </div>
        <div v-else-if="summaryInfo.avgMood" style="margin-top: 8px; font-size: 12px; color: #94a3b8">
          平均心情：{{ summaryInfo.avgMood }}
        </div>
      </div>

      <!-- 目标进度 -->
      <div
        class="stat-card card--glow"
        style="--stat-color: #3b82f6; --stat-bg: rgba(59, 130, 246, 0.12)"
      >
        <div class="stat-icon">🎯</div>
        <div class="stat-value" v-if="period === 'year'">
          {{ goalStats.completionRate ?? 0 }}<span style="font-size: 16px">%</span>
        </div>
        <div class="stat-value" v-else>
          {{ goalStats.avgProgress ?? 0 }}<span style="font-size: 16px">%</span>
        </div>
        
        <div class="stat-label">
          {{ period === 'year' ? '年度目标完成率' : '目标平均进度' }}
        </div>
        
        <div v-if="period === 'year'" style="margin-top: 8px; font-size: 12px; color: #94a3b8">
          已完成 {{ goalStats.done ?? 0 }} / 共 {{ goalStats.total ?? 0 }} 个
        </div>
        <div v-else style="margin-top: 8px; font-size: 12px; color: #94a3b8">
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

    <!-- 综合洞察卡片 -->
    <div class="insights-card card">
      <div class="insights-header">
        <div class="insights-title">
          <span class="insights-icon">📊</span>
          <span>本周生产力洞察</span>
        </div>
        <div class="insights-date">{{ weekRange }}</div>
      </div>

      <div class="insights-content">
        <!-- 左侧：关键指标 -->
        <div class="insights-metrics">
          <div class="insight-metric">
            <div class="insight-metric-label">计划完成率</div>
            <div class="insight-metric-value" :style="{ color: getCompletionColor(weekCompletionRate) }">
              {{ weekCompletionRate }}%
            </div>
            <div class="insight-metric-bar">
              <div class="insight-metric-bar-fill" :style="{ width: `${weekCompletionRate}%`, background: getCompletionColor(weekCompletionRate) }" />
            </div>
          </div>
          <div class="insight-metric">
            <div class="insight-metric-label">平均心情</div>
            <div class="insight-metric-value">{{ weekAvgMood }}</div>
            <div class="insight-metric-emoji">{{ getMoodEmoji(weekAvgMood) }}</div>
          </div>
          <div class="insight-metric">
            <div class="insight-metric-label">净收支</div>
            <div class="insight-metric-value" :style="{ color: weekNetIncome >= 0 ? '#10b981' : '#ef4444' }">
              ¥{{ weekNetIncome >= 0 ? '+' : '' }}{{ weekNetIncome.toFixed(2) }}
            </div>
            <div class="insight-metric-trend" :class="{ 'trend-up': weekNetIncome >= 0, 'trend-down': weekNetIncome < 0 }">
              {{ weekNetIncome >= 0 ? '↑ 储蓄' : '↓ 超支' }}
            </div>
          </div>
        </div>

        <!-- 右侧：AI建议 -->
        <div class="insights-suggestion">
          <div class="insights-suggestion-header">
            <span class="insights-suggestion-icon">💡</span>
            <span class="insights-suggestion-title">AI 洞察建议</span>
          </div>
          <div class="insights-suggestion-content">
            <div v-for="(suggestion, index) in aiSuggestions" :key="index" class="suggestion-item">
              <div class="suggestion-icon">{{ suggestion.icon }}</div>
              <div class="suggestion-text">{{ suggestion.text }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 底部：关键词标签 -->
      <div class="insights-tags">
        <span class="insights-tags-label">🏷️ 本周关键词：</span>
        <span v-for="(tag, index) in weekKeywords" :key="index" class="insight-tag">
          {{ tag }}
        </span>
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
          <el-button-group size="small">
            <el-button :type="financeChartType === 'line' ? 'primary' : ''" @click="financeChartType = 'line'; renderFinanceChart()">
              趋势图
            </el-button>
            <el-button :type="financeChartType === 'waterfall' ? 'primary' : ''" @click="financeChartType = 'waterfall'; renderFinanceChart()">
              瀑布图
            </el-button>
          </el-button-group>
        </div>
        <div ref="financeChartRef" class="chart" />
      </div>

      <!-- 情绪趋势 -->
      <div class="card chart-card">
        <div class="card-title flex justify-between items-center">
          <span>情绪分析 (近 30 天)</span>
          <el-button-group size="small">
            <el-button :type="moodChartType === 'trend' ? 'primary' : ''" @click="moodChartType = 'trend'; renderMoodChart()">
              趋势图
            </el-button>
            <el-button :type="moodChartType === 'heatmap' ? 'primary' : ''" @click="moodChartType = 'heatmap'; renderMoodChart()">
              热力图
            </el-button>
          </el-button-group>
        </div>
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
import { BarChart, LineChart, HeatmapChart } from 'echarts/charts'
import {
  TooltipComponent,
  GridComponent,
  LegendComponent,
  GraphicComponent,
  VisualMapComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import dayjs from 'dayjs'

echarts.use([
  BarChart,
  LineChart,
  HeatmapChart,
  TooltipComponent,
  GridComponent,
  LegendComponent,
  GraphicComponent,
  VisualMapComponent,
  CanvasRenderer
])

const userStore = useUserStore()
const period = ref<'today' | 'week' | 'month' | 'year'>('today')
const isDark = useDark()

// 图表切换状态
const financeChartType = ref<'line' | 'waterfall'>('line')
const moodChartType = ref<'trend' | 'heatmap'>('trend')

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

// 综合洞察卡片计算属性
const weekRange = computed(() => {
  const end = dayjs()
  const start = end.subtract(6, 'day')
  return `${start.format('MM/DD')} - ${end.format('MM/DD')}`
})

const weekCompletionRate = computed(() => {
  const planByDate = weekData.value.planByDate ?? {}
  const donePlanByDate = weekData.value.donePlanByDate ?? {}
  let total = 0, done = 0
  Object.values(planByDate).forEach((v: any) => total += v)
  Object.values(donePlanByDate).forEach((v: any) => done += v)
  return total > 0 ? Math.round(done * 100 / total) : 0
})

const weekAvgMood = computed(() => {
  const weekMoods = moodTrend.value.slice(-7)
  if (weekMoods.length === 0) return 3
  const sum = weekMoods.reduce((acc, m) => acc + (m.mood || 3), 0)
  return (sum / weekMoods.length).toFixed(1)
})

const weekNetIncome = computed(() => {
  const financeTrend = trendData.value.financeTrend ?? {}
  let totalIncome = 0, totalExpense = 0
  for (let i = 6; i >= 0; i--) {
    const date = dayjs().subtract(i, 'day').format('YYYY-MM-DD')
    totalIncome += financeTrend[date]?.income || 0
    totalExpense += financeTrend[date]?.expense || 0
  }
  return totalIncome - totalExpense
})

const aiSuggestions = computed(() => {
  const suggestions = []

  // 计划完成率建议
  if (weekCompletionRate.value < 50) {
    suggestions.push({
      icon: '📋',
      text: '本周计划完成率较低，建议优化任务优先级，专注于最重要的 3 件事'
    })
  } else if (weekCompletionRate.value > 90) {
    suggestions.push({
      icon: '🎉',
      text: '本周计划执行力出色！可以尝试设定更有挑战性的目标'
    })
  } else {
    suggestions.push({
      icon: '✨',
      text: '计划执行良好，保持当前节奏，适当留出弹性时间'
    })
  }

  // 情绪建议
  const avgMood = parseFloat(weekAvgMood.value)
  if (avgMood < 3) {
    suggestions.push({
      icon: '🧘',
      text: '本周情绪偏低，建议增加休息时间，尝试冥想或运动来改善心情'
    })
  } else if (avgMood > 4) {
    suggestions.push({
      icon: '😊',
      text: '本周心情保持得很好，继续保持积极的生活态度'
    })
  }

  // 财务建议
  if (weekNetIncome.value < 0) {
    suggestions.push({
      icon: '💰',
      text: '本周出现超支，建议回顾支出明细，优化消费习惯'
    })
  } else if (weekNetIncome.value > 1000) {
    suggestions.push({
      icon: '📈',
      text: '本周储蓄表现优秀，考虑将部分资金用于投资或理财'
    })
  }

  return suggestions.slice(0, 3)
})

const weekKeywords = computed(() => {
  const keywords = []

  // 根据完成率添加关键词
  if (weekCompletionRate.value >= 80) keywords.push('高效')
  else if (weekCompletionRate.value >= 60) keywords.push('稳定')
  else keywords.push('成长中')

  // 根据情绪添加关键词
  const avgMood = parseFloat(weekAvgMood.value)
  if (avgMood >= 4) keywords.push('愉快')
  else if (avgMood >= 3) keywords.push('平静')
  else keywords.push('调整期')

  // 根据财务添加关键词
  if (weekNetIncome.value >= 0) keywords.push('储蓄')
  else keywords.push('消费')

  return keywords
})

// 辅助函数
function getCompletionColor(rate: number) {
  if (rate < 50) return '#ef4444'
  if (rate < 80) return '#f59e0b'
  return '#10b981'
}

function getMoodEmoji(value: string) {
  const mood = parseFloat(value)
  return moodEmoji[Math.max(0, Math.min(4, Math.round(mood) - 1))]
}

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
  // 根据不同的 period 获取对应的数据并映射到卡片变量上
  // 为了保证 "目标进度" 在本周/本月切换时也有基础数据，我们默认获取 today 的 goalStats
  const today = await dashboardApi.getToday()
  let fallbackGoalStats = today.goal ?? {}

  if (period.value === 'today') {
    planStats.value = today.plan ?? {}
    accountingStats.value = today.accounting ?? { income: 0, expense: 0 }
    excerptCount.value = today.excerptCount ?? 0
    summaryInfo.value = today.summary ?? {}
    goalStats.value = today.goal ?? {}
  } else if (period.value === 'week') {
    const res = await dashboardApi.getWeek()
    let total = 0, done = 0;
    if (res.planByDate) Object.values(res.planByDate).forEach((v: any) => total += v)
    if (res.donePlanByDate) Object.values(res.donePlanByDate).forEach((v: any) => done += v)
    planStats.value = { total, done, completionRate: total ? Math.round(done * 100 / total) : 0 }
    accountingStats.value = { income: res.weekIncome ?? 0, expense: res.weekExpense ?? 0 }
    excerptCount.value = res.weekExcerpts ?? 0
    summaryInfo.value = { days: 0 } // 本周无单独的总结天数统计接口返回，默认 0
    goalStats.value = fallbackGoalStats
  } else if (period.value === 'month') {
    const res = await dashboardApi.getMonth()
    planStats.value = { 
      total: res.monthPlanTotal ?? 0, 
      done: res.monthPlanDone ?? 0, 
      completionRate: res.monthCompletionRate ?? 0 
    }
    accountingStats.value = { income: res.monthIncome ?? 0, expense: res.monthExpense ?? 0 }
    excerptCount.value = 0 // 本月摘录数接口未返回
    summaryInfo.value = { days: res.summaryDays ?? 0, avgMood: res.avgMood }
    goalStats.value = fallbackGoalStats
  } else if (period.value === 'year') {
    const res = await dashboardApi.getYear()
    planStats.value = { total: 0, done: 0, completionRate: 0 }
    accountingStats.value = { income: res.yearIncome ?? 0, expense: res.yearExpense ?? 0 }
    excerptCount.value = res.excerptCount ?? 0
    summaryInfo.value = { days: res.summaryDays ?? 0 }
    const totalGoal = res.yearlyGoalTotal ?? 0
    const doneGoal = res.yearlyGoalDone ?? 0
    goalStats.value = { 
      total: totalGoal, 
      done: doneGoal,
      completionRate: totalGoal ? Math.round(doneGoal * 100 / totalGoal) : 0
    }
  }

  // 本周数据（始终加载用于柱状图表）
  weekData.value = await dashboardApi.getWeek()

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

  const planData = dates.map((d) => planByDate[d] ?? 0)
  const doneData = dates.map((d) => donePlanByDate[d] ?? 0)
  const completionRates = dates.map((d, i) => {
    const total = planData[i]
    const done = doneData[i]
    return total > 0 ? Math.round((done / total) * 100) : 0
  })

  // 根据完成率返回颜色
  const getRateColor = (rate: number) => {
    if (rate < 50) return '#ef4444'
    if (rate < 80) return '#f59e0b'
    return '#10b981'
  }

  planChart.setOption({
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(26, 29, 39, 0.95)',
      borderColor: 'rgba(255, 255, 255, 0.1)',
      textStyle: { color: '#e2e8f0' },
      padding: [12, 16],
      formatter: (params: any) => {
        const date = params[0].name
        const index = params[0].dataIndex
        const total = planData[index]
        const done = doneData[index]
        const rate = completionRates[index]
        const rateColor = getRateColor(rate)
        let html = `<div style="margin-bottom: 8px; font-weight: 600">${date}</div>`
        html += `
          <div style="display: flex; align-items: center; margin-bottom: 4px;">
            <span style="width: 10px; height: 10px; border-radius: 50%; background: rgba(99,102,241,0.6); margin-right: 8px;"></span>
            <span style="color: #94a3b8; margin-right: 12px;">任务数:</span>
            <span style="font-weight: 600;">${total}</span>
          </div>
          <div style="display: flex; align-items: center; margin-bottom: 8px;">
            <span style="width: 10px; height: 10px; border-radius: 50%; background: #6366f1; margin-right: 8px;"></span>
            <span style="color: #94a3b8; margin-right: 12px;">已完成:</span>
            <span style="font-weight: 600;">${done}</span>
          </div>
          <div style="margin-top: 8px; padding-top: 8px; border-top: 1px solid rgba(255,255,255,0.1);">
            <span style="color: #94a3b8;">完成率:</span>
            <span style="font-weight: 600; color: ${rateColor}; margin-left: 8px;">${rate}%</span>
          </div>
        `
        return html
      }
    },
    legend: {
      data: ['任务数', '已完成'],
      textStyle: { color: '#94a3b8' },
      bottom: 0,
      left: 'center',
      itemGap: 24
    },
    grid: { top: 32, right: 16, bottom: 40, left: 40 },
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
        data: planData,
        itemStyle: {
          color: 'rgba(99,102,241,0.6)',
          borderRadius: [4, 4, 0, 0],
          borderColor: 'rgba(99,102,241,0.8)',
          borderWidth: 1
        },
        animationDuration: 800,
        animationEasing: 'cubicOut'
      },
      {
        name: '已完成',
        type: 'bar',
        data: doneData,
        itemStyle: {
          color: '#6366f1',
          borderRadius: [4, 4, 0, 0],
          shadowColor: 'rgba(99, 102, 241, 0.3)',
          shadowBlur: 8
        },
        animationDuration: 800,
        animationEasing: 'cubicOut',
        animationDelay: 100
      },
      {
        name: '完成率',
        type: 'bar',
        data: planData.map((_, i) => 0),
        barGap: '-100%',
        z: 10,
        tooltip: { show: false },
        label: {
          show: true,
          position: 'top',
          formatter: (params: any) => {
            const rate = completionRates[params.dataIndex]
            return rate > 0 ? `${rate}%` : ''
          },
          color: (params: any) => getRateColor(completionRates[params.dataIndex]),
          fontSize: 12,
          fontWeight: 600
        },
        itemStyle: { opacity: 0 },
        animationDuration: 0
      }
    ]
  })
}

function renderMoodChart() {
  if (!moodChartRef.value) return
  if (!moodChart) {
    moodChart = echarts.init(moodChartRef.value, isDark.value ? 'dark' : 'light')
  }

  if (moodChartType.value === 'heatmap') {
    renderMoodHeatmap()
  } else {
    renderMoodTrend()
  }
}

function renderMoodTrend() {
  const data = moodTrend.value

  // 情绪区间背景色带
  const moodZones = [
    { min: 1, max: 2, color: 'rgba(239, 68, 68, 0.08)', label: '😢😔' },   // 很差/较差
    { min: 2, max: 3, color: 'rgba(245, 158, 11, 0.08)', label: '😔😐' },  // 较差/一般
    { min: 3, max: 4, color: 'rgba(16, 185, 129, 0.08)', label: '😐😊' },  // 一般/较好
    { min: 4, max: 5, color: 'rgba(59, 130, 246, 0.08)', label: '😊😄' }   // 较好/很好
  ]

  moodChart.setOption({
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(26, 29, 39, 0.95)',
      borderColor: 'rgba(255, 255, 255, 0.1)',
      textStyle: { color: '#e2e8f0' },
      padding: [12, 16],
      formatter: (params: any) => {
        const d = params[0]
        const moodValue = d.value ?? 3
        const moodIndex = Math.max(0, Math.min(4, moodValue - 1))
        return `
          <div style="margin-bottom: 8px; font-weight: 600">${d.name}</div>
          <div style="display: flex; align-items: center;">
            <span style="font-size: 24px; margin-right: 12px;">${moodEmoji[moodIndex]}</span>
            <div>
              <div style="color: #94a3b8; font-size: 12px;">心情指数</div>
              <div style="font-weight: 600;">${moodValue} / 5</div>
            </div>
          </div>
        `
      }
    },
    graphic: moodZones.map(zone => ({
      type: 'rect',
      left: '3%',
      right: '3%',
      top: `${(5 - zone.max) / 4 * 100}%`,
      height: `${(zone.max - zone.min) / 4 * 100}%`,
      shape: { fill: zone.color },
      z: 0,
      silent: true
    })),
    grid: { top: 20, right: 16, bottom: 24, left: 50 },
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
        lineStyle: {
          color: '#ec4899',
          width: 3,
          shadowColor: 'rgba(236, 72, 153, 0.4)',
          shadowBlur: 10
        },
        itemStyle: {
          color: '#ec4899',
          borderColor: '#fff',
          borderWidth: 2
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(236, 72, 153, 0.35)' },
            { offset: 0.5, color: 'rgba(236, 72, 153, 0.15)' },
            { offset: 1, color: 'rgba(236, 72, 153, 0)' }
          ])
        },
        markLine: {
          symbol: 'none',
          lineStyle: { color: '#94a3b8', type: 'dashed', width: 1, opacity: 0.3 },
          data: [
            { yAxis: 1.5 },
            { yAxis: 2.5 },
            { yAxis: 3.5 },
            { yAxis: 4.5 }
          ],
          silent: true
        },
        animationDuration: 1500,
        animationEasing: 'cubicOut'
      }
    ]
  })
}

function renderMoodHeatmap() {
  const data = moodTrend.value

  // 构建热力图数据（按周分组）
  const weeks: any[][] = []
  const weekDates: string[] = []
  const weekLabels: string[] = []

  // 将数据按周分组
  for (let i = 0; i < data.length; i += 7) {
    const weekData = data.slice(i, i + 7)
    const weekMoods = weekData.map((d, idx) => {
      const date = dayjs(d.date)
      const dayOfWeek = date.day() // 0 = 周日, 1 = 周一, ...
      return [dayOfWeek, i / 7, d.mood, d.date, d.mood]
    })
    weeks.push(...weekMoods)

    if (weekData.length > 0) {
      const startDate = dayjs(weekData[0].date)
      const endDate = dayjs(weekData[weekData.length - 1].date)
      weekLabels.push(`${startDate.format('MM/DD')}-${endDate.format('MM/DD')}`)
    }
  }

  // Y轴标签（周一到周日）
  const dayLabels = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']

  moodChart!.setOption({
    backgroundColor: 'transparent',
    tooltip: {
      backgroundColor: 'rgba(26, 29, 39, 0.95)',
      borderColor: 'rgba(255, 255, 255, 0.1)',
      textStyle: { color: '#e2e8f0' },
      padding: [12, 16],
      formatter: (params: any) => {
        const d = params.data
        const mood = d[3]
        const date = d[2]
        return `
          <div style="margin-bottom: 8px; font-weight: 600">${dayjs(date).format('YYYY-MM-DD')}</div>
          <div style="display: flex; align-items: center;">
            <span style="font-size: 28px; margin-right: 16px;">${moodEmoji[mood - 1]}</span>
            <div>
              <div style="color: #94a3b8; font-size: 12px;">心情指数</div>
              <div style="font-size: 20px; font-weight: 600;">${mood} / 5</div>
            </div>
          </div>
        `
      }
    },
    grid: { top: 20, right: 16, bottom: 40, left: 50 },
    xAxis: {
      type: 'category',
      data: weekLabels,
      axisLabel: { color: '#64748b', fontSize: 10, rotate: 30 },
      axisLine: { lineStyle: { color: 'rgba(255,255,255,0.08)' } },
      splitLine: { show: false }
    },
    yAxis: {
      type: 'category',
      data: dayLabels,
      axisLabel: { color: '#64748b', fontSize: 11 },
      axisLine: { lineStyle: { color: 'rgba(255,255,255,0.08)' } },
      splitLine: { show: false }
    },
    visualMap: {
      min: 1,
      max: 5,
      calculable: true,
      orient: 'horizontal',
      left: 'center',
      bottom: 0,
      inRange: {
        color: ['#ef4444', '#f59e0b', '#10b981', '#3b82f6', '#8b5cf6']
      },
      textStyle: { color: '#94a3b8' },
      pieces: [
        { value: 1, label: '😢', color: '#ef4444' },
        { value: 2, label: '😔', color: '#f59e0b' },
        { value: 3, label: '😐', color: '#10b981' },
        { value: 4, label: '😊', color: '#3b82f6' },
        { value: 5, label: '😄', color: '#8b5cf6' }
      ]
    },
    series: [
      {
        type: 'heatmap',
        data: weeks.map(d => [d[0], d[1], d[4]]),
        label: {
          show: true,
          formatter: (params: any) => moodEmoji[params.value[2] - 1],
          fontSize: 16
        },
        itemStyle: {
          borderColor: 'var(--bg-card)',
          borderWidth: 2,
          borderRadius: 4
        },
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        },
        animationDuration: 1000,
        animationEasing: 'cubicOut',
        animationDelay: (idx: number) => idx * 30
      }
    ]
  })
}

function renderFinanceChart() {
  if (!financeChartRef.value) return
  if (!financeChart) {
    financeChart = echarts.init(financeChartRef.value, isDark.value ? 'dark' : 'light')
  }

  if (financeChartType.value === 'waterfall') {
    renderFinanceWaterfall()
  } else {
    renderFinanceLine()
  }
}

function renderFinanceLine() {
  const data = trendData.value.financeTrend ?? {}
  const days: string[] = []
  for (let i = 6; i >= 0; i--) {
    days.push(dayjs().subtract(i, 'day').format('YYYY-MM-DD'))
  }

  const incomeData = days.map(d => data[d]?.income ?? 0)
  const expenseData = days.map(d => data[d]?.expense ?? 0)

  financeChart!.setOption({
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(26, 29, 39, 0.95)',
      borderColor: 'rgba(255, 255, 255, 0.1)',
      textStyle: { color: '#e2e8f0' },
      padding: [12, 16],
      formatter: (params: any) => {
        const date = params[0].name
        let html = `<div style="margin-bottom: 8px; font-weight: 600">${date}</div>`
        params.forEach((p: any) => {
          const color = p.color === '#10b981' ? '#34d399' : '#f87171'
          html += `
            <div style="display: flex; align-items: center; margin-bottom: 4px;">
              <span style="width: 10px; height: 10px; border-radius: 50%; background: ${color}; margin-right: 8px;"></span>
              <span style="color: #94a3b8; margin-right: 12px;">${p.seriesName}:</span>
              <span style="font-weight: 600;">¥${p.value.toFixed(2)}</span>
            </div>
          `
        })
        const netIncome = incomeData[params[0].dataIndex] - expenseData[params[0].dataIndex]
        const netColor = netIncome >= 0 ? '#10b981' : '#ef4444'
        html += `
          <div style="margin-top: 8px; padding-top: 8px; border-top: 1px solid rgba(255,255,255,0.1);">
            <span style="color: #94a3b8;">净收支:</span>
            <span style="font-weight: 600; color: ${netColor}; margin-left: 8px;">¥${netIncome.toFixed(2)}</span>
          </div>
        `
        return html
      }
    },
    legend: {
      data: ['收入', '支出'],
      textStyle: { color: '#94a3b8' },
      bottom: 0,
      left: 'center',
      itemGap: 24
    },
    grid: { top: 16, right: 16, bottom: 40, left: 50 },
    xAxis: {
      type: 'category',
      data: days.map(d => dayjs(d).format('MM-DD')),
      axisLabel: { color: '#64748b', fontSize: 11 },
      axisLine: { lineStyle: { color: 'rgba(255,255,255,0.08)' } }
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: '#64748b', fontSize: 11, formatter: '¥{value}' },
      splitLine: { lineStyle: { color: 'rgba(255,255,255,0.04)' } }
    },
    series: [
      {
        name: '收入',
        type: 'line',
        data: incomeData,
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: { color: '#10b981', width: 3, shadowColor: 'rgba(16, 185, 129, 0.3)', shadowBlur: 10 },
        itemStyle: { color: '#10b981', borderColor: '#fff', borderWidth: 2 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(16, 185, 129, 0.4)' },
            { offset: 0.5, color: 'rgba(16, 185, 129, 0.2)' },
            { offset: 1, color: 'rgba(16, 185, 129, 0)' }
          ])
        },
        animationDuration: 1000,
        animationEasing: 'cubicOut'
      },
      {
        name: '支出',
        type: 'line',
        data: expenseData,
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: { color: '#ef4444', width: 3, shadowColor: 'rgba(239, 68, 68, 0.3)', shadowBlur: 10 },
        itemStyle: { color: '#ef4444', borderColor: '#fff', borderWidth: 2 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(239, 68, 68, 0.4)' },
            { offset: 0.5, color: 'rgba(239, 68, 68, 0.2)' },
            { offset: 1, color: 'rgba(239, 68, 68, 0)' }
          ])
        },
        animationDuration: 1000,
        animationEasing: 'cubicOut',
        animationDelay: 200
      }
    ]
  })
}

function renderFinanceWaterfall() {
  const data = trendData.value.financeTrend ?? {}
  const days: string[] = []
  for (let i = 6; i >= 0; i--) {
    days.push(dayjs().subtract(i, 'day').format('YYYY-MM-DD'))
  }

  // 构建瀑布图数据
  const waterfallData: any[] = []
  let cumulative = 0

  // 起始余额
  waterfallData.push({
    name: '起始',
    value: [0, 0, 0], // [起始, 增量, 结束]
    itemStyle: { color: '#94a3b8' }
  })

  // 每日收支
  days.forEach((date, index) => {
    const income = data[date]?.income || 0
    const expense = data[date]?.expense || 0
    const net = income - expense

    waterfallData.push({
      name: dayjs(date).format('MM-DD'),
      value: [cumulative, net, cumulative + net],
      itemStyle: { color: net >= 0 ? '#10b981' : '#ef4444' },
      income,
      expense
    })

    cumulative += net
  })

  // 期末余额
  waterfallData.push({
    name: '期末',
    value: [cumulative, 0, cumulative],
    itemStyle: { color: '#6366f1' }
  })

  financeChart!.setOption({
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(26, 29, 39, 0.95)',
      borderColor: 'rgba(255, 255, 255, 0.1)',
      textStyle: { color: '#e2e8f0' },
      padding: [12, 16],
      formatter: (params: any) => {
        const d = params[0]
        if (d.data.name === '起始' || d.data.name === '期末') {
          return `
            <div style="margin-bottom: 8px; font-weight: 600">${d.data.name}余额</div>
            <div style="font-size: 18px; font-weight: 600;">¥${d.data.value[2].toFixed(2)}</div>
          `
        }
        const income = d.data.income || 0
        const expense = d.data.expense || 0
        return `
          <div style="margin-bottom: 8px; font-weight: 600">${d.data.name}</div>
          <div style="display: flex; align-items: center; margin-bottom: 4px;">
            <span style="color: #94a3b8; margin-right: 12px;">收入:</span>
            <span style="color: #10b981; font-weight: 600;">+¥${income.toFixed(2)}</span>
          </div>
          <div style="display: flex; align-items: center; margin-bottom: 8px;">
            <span style="color: #94a3b8; margin-right: 12px;">支出:</span>
            <span style="color: #ef4444; font-weight: 600;">-¥${expense.toFixed(2)}</span>
          </div>
          <div style="margin-top: 8px; padding-top: 8px; border-top: 1px solid rgba(255,255,255,0.1);">
            <span style="color: #94a3b8;">当日净收支:</span>
            <span style="color: ${d.data.value[1] >= 0 ? '#10b981' : '#ef4444'}; font-weight: 600; margin-left: 8px;">
              ¥${d.data.value[1].toFixed(2)}
            </span>
          </div>
        `
      }
    },
    grid: { top: 20, right: 16, bottom: 40, left: 50 },
    xAxis: {
      type: 'category',
      data: waterfallData.map(d => d.name),
      axisLabel: { color: '#64748b', fontSize: 11 },
      axisLine: { lineStyle: { color: 'rgba(255,255,255,0.08)' } }
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: '#64748b', fontSize: 11, formatter: '¥{value}' },
      splitLine: { lineStyle: { color: 'rgba(255,255,255,0.04)' } }
    },
    series: [
      {
        type: 'bar',
        data: waterfallData.map(d => {
          const value = d.value
          return {
            value: [value[0], value[2]],
            itemStyle: d.itemStyle
          }
        }),
        barWidth: '60%',
        itemStyle: {
          borderRadius: [4, 4, 0, 0],
          borderColor: 'transparent',
          borderWidth: 0
        },
        label: {
          show: true,
          position: 'top',
          formatter: (params: any) => {
            const index = params.dataIndex
            if (index === 0 || index === waterfallData.length - 1) {
              return `¥${waterfallData[index].value[2].toFixed(2)}`
            }
            const net = waterfallData[index].value[1]
            return `${net >= 0 ? '+' : ''}¥${net.toFixed(2)}`
          },
          color: (params: any) => waterfallData[params.dataIndex].itemStyle.color,
          fontSize: 11,
          fontWeight: 600
        },
        animationDuration: 1000,
        animationEasing: 'cubicOut',
        animationDelay: (idx: number) => idx * 100
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

  const excerptData = days.map(d => data[d] ?? 0)

  excerptChart.setOption({
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(26, 29, 39, 0.95)',
      borderColor: 'rgba(255, 255, 255, 0.1)',
      textStyle: { color: '#e2e8f0' },
      padding: [12, 16],
      formatter: (params: any) => {
        const d = params[0]
        const count = d.value ?? 0
        const avgDaily = excerptData.reduce((a, b) => a + b, 0) / 7
        const isAboveAvg = count >= avgDaily
        return `
          <div style="margin-bottom: 8px; font-weight: 600">${d.name}</div>
          <div style="display: flex; align-items: center; margin-bottom: 8px;">
            <span style="font-size: 24px; margin-right: 12px;">📖</span>
            <div>
              <div style="color: #94a3b8; font-size: 12px;">摘录数量</div>
              <div style="font-weight: 600; font-size: 18px;">${count} 条</div>
            </div>
          </div>
          <div style="margin-top: 8px; padding-top: 8px; border-top: 1px solid rgba(255,255,255,0.1);">
            <span style="color: #94a3b8;">7日平均:</span>
            <span style="font-weight: 600; margin-left: 8px;">${avgDaily.toFixed(1)} 条</span>
            <span style="color: ${isAboveAvg ? '#10b981' : '#ef4444'}; margin-left: 8px; font-size: 12px;">
              ${isAboveAvg ? '↑ 高于平均' : '↓ 低于平均'}
            </span>
          </div>
        `
      }
    },
    grid: { top: 20, right: 16, bottom: 24, left: 40 },
    xAxis: {
      type: 'category',
      data: days.map(d => dayjs(d).format('MM-DD')),
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
        type: 'bar',
        data: excerptData,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#f59e0b' },
            { offset: 1, color: '#fbbf24' }
          ]),
          borderRadius: [4, 4, 0, 0],
          borderColor: 'rgba(251, 191, 36, 0.8)',
          borderWidth: 1,
          shadowColor: 'rgba(245, 158, 11, 0.3)',
          shadowBlur: 8
        },
        label: {
          show: true,
          position: 'top',
          formatter: (params: any) => params.value > 0 ? params.value : '',
          color: '#f59e0b',
          fontSize: 12,
          fontWeight: 600
        },
        animationDuration: 1000,
        animationEasing: 'elasticOut',
        animationDelay: (idx: number) => idx * 100
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

  // 综合洞察卡片
  .insights-card {
    margin-bottom: 20px;
    background: linear-gradient(135deg, rgba(99, 102, 241, 0.05) 0%, rgba(59, 130, 246, 0.05) 100%);
    border: 1px solid rgba(99, 102, 241, 0.15);

    .insights-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
      padding-bottom: 16px;
      border-bottom: 1px solid $border;

      .insights-title {
        display: flex;
        align-items: center;
        gap: 10px;
        font-size: 16px;
        font-weight: 700;
        color: $text-primary;

        .insights-icon {
          font-size: 22px;
        }
      }

      .insights-date {
        font-size: 13px;
        color: $text-muted;
        background: $bg-input;
        padding: 4px 12px;
        border-radius: $radius-full;
      }
    }

    .insights-content {
      display: flex;
      gap: 24px;
      margin-bottom: 20px;

      .insights-metrics {
        flex: 1;
        display: grid;
        grid-template-columns: repeat(3, 1fr);
        gap: 20px;
      }

      .insight-metric {
        display: flex;
        flex-direction: column;
        align-items: center;
        padding: 16px;
        background: var(--bg-card);
        border: 1px solid $border;
        border-radius: $radius-md;
        transition: $transition-normal;

        &:hover {
          transform: translateY(-2px);
          box-shadow: $shadow-md;
          border-color: rgba(99, 102, 241, 0.3);
        }

        .insight-metric-label {
          font-size: 12px;
          color: $text-secondary;
          margin-bottom: 8px;
        }

        .insight-metric-value {
          font-size: 28px;
          font-weight: 700;
          margin-bottom: 8px;
        }

        .insight-metric-bar {
          width: 100%;
          height: 6px;
          background: $bg-input;
          border-radius: $radius-full;
          overflow: hidden;
        }

        .insight-metric-bar-fill {
          height: 100%;
          border-radius: $radius-full;
          transition: width 0.8s ease;
        }

        .insight-metric-emoji {
          font-size: 28px;
          margin-top: -8px;
        }

        .insight-metric-trend {
          font-size: 12px;
          font-weight: 600;
          margin-top: 4px;

          &.trend-up {
            color: $success;
          }

          &.trend-down {
            color: $danger;
          }
        }
      }

      .insights-suggestion {
        flex: 1;
        background: var(--bg-card);
        border: 1px solid $border;
        border-radius: $radius-md;
        padding: 20px;

        .insights-suggestion-header {
          display: flex;
          align-items: center;
          gap: 8px;
          margin-bottom: 16px;
          padding-bottom: 12px;
          border-bottom: 1px solid $border;

          .insights-suggestion-icon {
            font-size: 20px;
          }

          .insights-suggestion-title {
            font-size: 14px;
            font-weight: 600;
            color: $text-primary;
          }
        }

        .insights-suggestion-content {
          .suggestion-item {
            display: flex;
            gap: 12px;
            padding: 12px;
            background: $bg-input;
            border-radius: $radius-sm;
            margin-bottom: 8px;
            transition: $transition-normal;

            &:last-child {
              margin-bottom: 0;
            }

            &:hover {
              background: rgba(99, 102, 241, 0.1);
            }

            .suggestion-icon {
              font-size: 18px;
              flex-shrink: 0;
            }

            .suggestion-text {
              font-size: 13px;
              color: $text-secondary;
              line-height: 1.5;
            }
          }
        }
      }
    }

    .insights-tags {
      display: flex;
      align-items: center;
      flex-wrap: wrap;
      gap: 8px;
      padding-top: 16px;
      border-top: 1px solid $border;

      .insights-tags-label {
        font-size: 13px;
        color: $text-secondary;
      }

      .insight-tag {
        display: inline-flex;
        align-items: center;
        padding: 4px 12px;
        background: rgba(99, 102, 241, 0.12);
        color: $primary-light;
        border: 1px solid rgba(99, 102, 241, 0.2);
        border-radius: $radius-full;
        font-size: 12px;
        font-weight: 500;
        transition: $transition-fast;

        &:hover {
          background: rgba(99, 102, 241, 0.2);
          transform: scale(1.05);
        }
      }
    }

    @media (max-width: 1024px) {
      .insights-content {
        flex-direction: column;

        .insights-metrics {
          grid-template-columns: repeat(3, 1fr);
        }
      }
    }

    @media (max-width: 768px) {
      .insights-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 8px;
      }

      .insights-content {
        .insights-metrics {
          grid-template-columns: 1fr;
        }
      }
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

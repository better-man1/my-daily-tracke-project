<template>
  <div class="summary-view">
    <div class="page-header">
      <div>
        <h1 class="page-title">每日总结</h1>
        <p class="page-subtitle">坚持复盘，持续成长</p>
      </div>
      <el-button type="primary" size="small" :icon="Plus" @click="openToday">今日总结</el-button>
    </div>

    <!-- 连续打卡 + 情绪趋势 -->
    <div class="summary-top">
      <!-- 打卡信息 -->
      <div class="card streak-bar">
        <div class="streak-item">
          <div class="streak-num">🔥 {{ streak.currentStreak ?? 0 }}</div>
          <div class="streak-label">当前连续天数</div>
        </div>
        <div class="streak-divider" />
        <div class="streak-item">
          <div class="streak-num">⚡ {{ streak.longestStreak ?? 0 }}</div>
          <div class="streak-label">历史最长</div>
        </div>
        <div class="streak-divider" />
        <div class="streak-item">
          <div class="streak-num">📅 {{ streak.totalDays ?? 0 }}</div>
          <div class="streak-label">累计总结天数</div>
        </div>
        <div class="streak-divider" />
        <div class="streak-item">
          <div class="streak-num" :style="{ color: avgMoodColor }">
            {{ avgMoodEmoji }} {{ avgMoodText }}
          </div>
          <div class="streak-label">近30天均值</div>
        </div>
      </div>

      <!-- 情绪趋势图 -->
      <div class="card chart-card">
        <div class="chart-title">近 30 天情绪 & 评分趋势</div>
        <div ref="moodChartRef" class="mood-chart" />
      </div>
    </div>

    <!-- 总结列表 -->
    <div v-loading="loading" class="summary-list">
      <div v-for="item in list" :key="item.id" class="summary-card card card--glow">
        <!-- 头部 -->
        <div class="summary-header">
          <span class="summary-date">{{ item.summaryDate }}</span>
          <div class="summary-scores">
            <span class="mood-badge">{{ moodEmoji[(item.mood ?? 3) - 1] }}</span>
            <span class="score-badge">{{ item.score ?? '-' }}<span class="score-max">/10</span></span>
          </div>
        </div>

        <!-- 内容区 -->
        <div class="summary-body">
          <div v-if="item.achievement" class="summary-section">
            <div class="section-title">✅ 今日成就</div>
            <div class="section-content">{{ item.achievement }}</div>
          </div>
          <div v-if="item.improvement" class="summary-section">
            <div class="section-title">📈 待改进</div>
            <div class="section-content">{{ item.improvement }}</div>
          </div>
          <div v-if="item.tomorrowPlan" class="summary-section">
            <div class="section-title">📋 明日计划</div>
            <div class="section-content">{{ item.tomorrowPlan }}</div>
          </div>
          <div v-if="item.freeWriting" class="summary-section free-writing">
            <div class="section-title">✍️ 自由日记</div>
            <div class="section-content">{{ item.freeWriting }}</div>
          </div>
        </div>

        <!-- 操作 -->
        <div class="summary-actions">
          <el-icon class="icon-btn" @click="editItem(item)"><Edit /></el-icon>
          <el-icon class="icon-btn danger" @click="deleteItem(item.id)"><Delete /></el-icon>
        </div>
      </div>

      <div v-if="!loading && list.length === 0" class="empty-state">
        <div class="empty-icon">✍️</div>
        <p>还没有总结记录，从今天开始吧</p>
        <el-button type="primary" size="small" @click="openToday">写今日总结</el-button>
      </div>
    </div>

    <!-- 分页 -->
    <el-pagination
      v-if="total > pageSize"
      v-model:current-page="pageNum"
      :page-size="pageSize"
      :total="total"
      layout="prev, pager, next"
      class="mt-md"
      @current-change="loadList"
    />

    <!-- 编辑弹窗 -->
    <el-dialog v-model="showDialog" :title="editing ? '编辑总结' : '今日总结'" width="640px" destroy-on-close>
      <el-form ref="formRef" :model="form" label-width="90px">
        <el-form-item label="总结日期">
          <el-date-picker v-model="form.summaryDate" type="date" format="YYYY-MM-DD" value-format="YYYY-MM-DD" />
        </el-form-item>
        <div class="flex gap-md">
          <el-form-item label="今日心情" style="flex:1">
            <el-rate v-model="form.mood" :max="5" :texts="moodEmoji" show-text style="height:32px" />
          </el-form-item>
          <el-form-item label="今日评分" style="flex:1">
            <el-input-number v-model="form.score" :min="1" :max="10" style="width:120px" />
            <span class="text-muted text-sm" style="margin-left:8px">/ 10</span>
          </el-form-item>
        </div>
        <el-form-item label="今日成就">
          <el-input v-model="form.achievement" type="textarea" :rows="2" placeholder="今天做到了什么值得骄傲的事？" />
        </el-form-item>
        <el-form-item label="待改进">
          <el-input v-model="form.improvement" type="textarea" :rows="2" placeholder="哪些方面可以做得更好？" />
        </el-form-item>
        <el-form-item label="明日计划">
          <el-input v-model="form.tomorrowPlan" type="textarea" :rows="2" placeholder="明天的主要任务是什么？" />
        </el-form-item>
        <el-form-item label="健康记录">
          <el-input v-model="form.healthNote" placeholder="睡眠/运动/饮食情况..." />
        </el-form-item>
        <el-form-item label="自由日记">
          <el-input v-model="form.freeWriting" type="textarea" :rows="3" placeholder="随意写写今天的感受..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import { summaryApi } from '@/api/summary'
import type { SummaryItem } from '@/api/summary'
import * as echarts from 'echarts'
import dayjs from 'dayjs'

const list = ref<SummaryItem[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = 10
const streak = ref<Record<string, any>>({})
const moodTrend = ref<any[]>([])
const loading = ref(false)
const saving = ref(false)
const showDialog = ref(false)
const editing = ref<SummaryItem | null>(null)
const formRef = ref<FormInstance>()
const moodChartRef = ref<HTMLElement>()
let moodChart: echarts.ECharts | null = null

const moodEmoji = ['😢', '😔', '😐', '😊', '😄']

const form = reactive({
  summaryDate: dayjs().format('YYYY-MM-DD'),
  mood: 3,
  score: 7,
  achievement: '',
  improvement: '',
  tomorrowPlan: '',
  healthNote: '',
  freeWriting: ''
})

// 近30天平均心情
const avgMood = computed(() => {
  if (!moodTrend.value.length) return 0
  const sum = moodTrend.value.reduce((a: number, b: any) => a + (b.mood ?? 3), 0)
  return sum / moodTrend.value.length
})

const avgMoodEmoji = computed(() => moodEmoji[Math.round(avgMood.value) - 1] ?? '😐')
const avgMoodText = computed(() => avgMood.value > 0 ? avgMood.value.toFixed(1) : '--')
const avgMoodColor = computed(() => {
  const v = avgMood.value
  if (v >= 4) return '#10b981'
  if (v >= 3) return '#f59e0b'
  return '#ef4444'
})

async function loadList() {
  loading.value = true
  try {
    const res = await summaryApi.list({ pageNum: pageNum.value, pageSize })
    list.value = res as any
    // 若返回 array 直接用，若有 total 字段则设置
    if (Array.isArray(res)) {
      total.value = (res as any[]).length >= pageSize ? pageNum.value * pageSize + 1 : (pageNum.value - 1) * pageSize + (res as any[]).length
    }
  } finally {
    loading.value = false
  }
}

async function loadStreak() {
  try { streak.value = await summaryApi.getStreak() as any } catch {}
}

async function loadMoodTrend() {
  try {
    moodTrend.value = await summaryApi.getMoodTrend(30)
    await nextTick()
    renderMoodChart()
  } catch {}
}

function renderMoodChart() {
  if (!moodChartRef.value) return
  if (!moodChart) moodChart = echarts.init(moodChartRef.value, 'dark')

  const data = moodTrend.value
  if (!data.length) {
    moodChart.setOption({
      backgroundColor: 'transparent',
      graphic: [{
        type: 'text',
        left: 'center', top: 'middle',
        style: { text: '暂无数据，开始记录你的每日心情吧 ✍️', fill: '#64748b', fontSize: 13 }
      }]
    })
    return
  }

  const dates = data.map((d: any) => dayjs(d.date).format('MM-DD'))
  const moods = data.map((d: any) => d.mood)
  const scores = data.map((d: any) => d.score)

  moodChart.setOption({
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      formatter: (params: any) => {
        const d = params[0]
        const s = params[1]
        return `${d.name}<br/>心情：${moodEmoji[(d.value ?? 3) - 1]}<br/>评分：${s?.value ?? '-'}/10`
      }
    },
    legend: {
      data: ['心情', '评分'],
      textStyle: { color: '#94a3b8', fontSize: 11 },
      right: 0
    },
    grid: { top: 30, right: 60, bottom: 24, left: 44 },
    xAxis: {
      type: 'category',
      data: dates,
      axisLabel: { color: '#64748b', fontSize: 10, rotate: dates.length > 20 ? 30 : 0 },
      axisLine: { lineStyle: { color: 'rgba(255,255,255,0.08)' } }
    },
    yAxis: [
      {
        type: 'value', name: '心情', min: 1, max: 5, minInterval: 1,
        axisLabel: {
          color: '#64748b', fontSize: 10,
          formatter: (v: number) => moodEmoji[v - 1] ?? v
        },
        splitLine: { lineStyle: { color: 'rgba(255,255,255,0.04)' } }
      },
      {
        type: 'value', name: '评分', min: 0, max: 10, minInterval: 1,
        axisLabel: { color: '#64748b', fontSize: 10 },
        splitLine: { show: false }
      }
    ],
    series: [
      {
        name: '心情', type: 'line', yAxisIndex: 0,
        data: moods, smooth: true,
        symbol: 'circle', symbolSize: 5,
        lineStyle: { color: '#ec4899', width: 2 },
        itemStyle: { color: '#ec4899' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(236,72,153,0.25)' },
            { offset: 1, color: 'rgba(236,72,153,0)' }
          ])
        }
      },
      {
        name: '评分', type: 'bar', yAxisIndex: 1,
        data: scores,
        itemStyle: { color: 'rgba(99,102,241,0.5)', borderRadius: [3, 3, 0, 0] },
        barMaxWidth: 16
      }
    ]
  })
}

function openToday() {
  editing.value = null
  Object.assign(form, {
    summaryDate: dayjs().format('YYYY-MM-DD'),
    mood: 3, score: 7,
    achievement: '', improvement: '', tomorrowPlan: '', healthNote: '', freeWriting: ''
  })
  showDialog.value = true
}

function editItem(item: SummaryItem) {
  editing.value = item
  Object.assign(form, {
    summaryDate: item.summaryDate,
    mood: item.mood ?? 3,
    score: item.score ?? 7,
    achievement: item.achievement ?? '',
    improvement: item.improvement ?? '',
    tomorrowPlan: item.tomorrowPlan ?? '',
    healthNote: (item as any).healthNote ?? '',
    freeWriting: item.freeWriting ?? ''
  })
  showDialog.value = true
}

async function deleteItem(id: number) {
  await ElMessageBox.confirm('确认删除这条总结？', '提示', { type: 'warning' })
  await summaryApi.delete(id)
  ElMessage.success('已删除')
  await Promise.all([loadList(), loadStreak(), loadMoodTrend()])
}

async function save() {
  saving.value = true
  try {
    if (editing.value) {
      await summaryApi.update(editing.value.id, form as any)
    } else {
      await summaryApi.create(form as any)
    }
    ElMessage.success('保存成功')
    showDialog.value = false
    await Promise.all([loadList(), loadStreak(), loadMoodTrend()])
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadList()
  loadStreak()
  loadMoodTrend()
  const resizeHandler = () => moodChart?.resize()
  window.addEventListener('resize', resizeHandler)
  onUnmounted(() => {
    window.removeEventListener('resize', resizeHandler)
    moodChart?.dispose()
  })
})
</script>

<style lang="scss" scoped>
.summary-view {
  .summary-top {
    display: grid;
    grid-template-columns: auto 1fr;
    gap: 16px;
    margin-bottom: 20px;
    align-items: stretch;

    @media (max-width: 900px) {
      grid-template-columns: 1fr;
    }
  }

  .streak-bar {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 0;
    padding: 20px 28px;
    min-width: 200px;

    .streak-item {
      text-align: center;
      padding: 10px 0;
      width: 100%;

      .streak-num { font-size: 22px; font-weight: 700; color: $text-primary; }
      .streak-label { font-size: 11px; color: $text-muted; margin-top: 3px; }
    }

    .streak-divider {
      width: 80%;
      height: 1px;
      background: $border;
    }
  }

  .chart-card {
    padding: 16px 20px;

    .chart-title {
      font-size: 13px;
      font-weight: 600;
      color: $text-secondary;
      margin-bottom: 12px;
    }

    .mood-chart {
      height: 220px;
      width: 100%;
    }
  }

  .summary-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .summary-card {
    position: relative;

    .summary-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 12px;

      .summary-date {
        font-size: 15px;
        font-weight: 600;
        color: $text-primary;
      }

      .summary-scores {
        display: flex;
        align-items: center;
        gap: 10px;

        .mood-badge { font-size: 22px; }
        .score-badge {
          font-size: 18px;
          font-weight: 700;
          color: $primary-light;

          .score-max {
            font-size: 12px;
            font-weight: 400;
            color: $text-muted;
          }
        }
      }
    }

    .summary-body {
      display: flex;
      flex-direction: column;
      gap: 8px;
    }

    .summary-section {
      .section-title {
        font-size: 11px;
        font-weight: 600;
        color: $text-secondary;
        text-transform: uppercase;
        letter-spacing: 0.5px;
        margin-bottom: 4px;
      }
      .section-content {
        font-size: 14px;
        color: $text-primary;
        line-height: 1.65;
      }
    }

    .free-writing .section-content {
      padding: 8px 12px;
      background: $bg-input;
      border-radius: $radius-sm;
      border-left: 2px solid rgba($primary, 0.6);
    }

    .summary-actions {
      position: absolute;
      top: 16px;
      right: 16px;
      display: flex;
      gap: 6px;
      opacity: 0;
      transition: $transition-fast;

      .icon-btn {
        color: $text-muted;
        cursor: pointer;
        padding: 4px;
        border-radius: $radius-sm;
        font-size: 15px;
        transition: $transition-fast;

        &:hover { color: $primary-light; background: $primary-100; }
        &.danger:hover { color: $danger; background: rgba($danger, 0.1); }
      }
    }

    &:hover .summary-actions { opacity: 1; }
  }

  .empty-state {
    text-align: center;
    padding: 60px 20px;
    color: $text-muted;
    .empty-icon { font-size: 48px; margin-bottom: 12px; }
    p { margin-bottom: 16px; }
  }
}
</style>

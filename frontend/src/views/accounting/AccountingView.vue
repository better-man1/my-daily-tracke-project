<template>
  <div class="accounting-view">
    <div class="page-header">
      <div>
        <h1 class="page-title">每日记账</h1>
        <p class="page-subtitle">记录每一笔收支，掌握财务状况</p>
      </div>
      <el-button type="primary" size="small" :icon="Plus" @click="openAdd">新增账目</el-button>
    </div>

    <!-- 月度统计卡片 -->
    <div class="month-stats">
      <div class="stat-card card--glow" style="--stat-color:#10b981;--stat-bg:rgba(16,185,129,0.12)">
        <div class="stat-icon">💰</div>
        <div class="stat-value">¥{{ formatAmount(monthStats.totalIncome) }}</div>
        <div class="stat-label">本月收入</div>
      </div>
      <div class="stat-card card--glow" style="--stat-color:#ef4444;--stat-bg:rgba(239,68,68,0.12)">
        <div class="stat-icon">💸</div>
        <div class="stat-value">¥{{ formatAmount(monthStats.totalExpense) }}</div>
        <div class="stat-label">本月支出</div>
      </div>
      <div class="stat-card card--glow"
        :style="{
          '--stat-color': (monthStats.balance ?? 0) >= 0 ? '#10b981' : '#ef4444',
          '--stat-bg': (monthStats.balance ?? 0) >= 0 ? 'rgba(16,185,129,0.12)' : 'rgba(239,68,68,0.12)'
        }">
        <div class="stat-icon">📊</div>
        <div class="stat-value">¥{{ formatAmount(Math.abs(monthStats.balance ?? 0)) }}</div>
        <div class="stat-label">{{ (monthStats.balance ?? 0) >= 0 ? '结余' : '超支' }}</div>
      </div>

      <!-- 预算执行 -->
      <div v-if="budget.totalBudget" class="stat-card card--glow budget-card"
        :style="{ '--stat-color': budgetOverrun ? '#ef4444' : '#6366f1', '--stat-bg': budgetOverrun ? 'rgba(239,68,68,0.12)' : 'rgba(99,102,241,0.12)' }">
        <div class="stat-icon">🎯</div>
        <div class="stat-value">{{ budgetRate }}%</div>
        <div class="stat-label">预算执行率</div>
        <el-progress
          :percentage="Math.min(budgetRate, 100)"
          :color="budgetOverrun ? '#ef4444' : '#6366f1'"
          :show-text="false"
          style="margin-top:8px"
        />
        <div style="font-size:11px;color:#94a3b8;margin-top:4px">
          剩余 ¥{{ formatAmount(budget.budgetRemaining) }}
        </div>
      </div>
    </div>

    <!-- 图表 + 筛选 -->
    <div class="chart-filter-row">
      <!-- 分类统计饼图 -->
      <div class="card chart-card">
        <div class="chart-header">
          <span class="chart-title">支出分类分布</span>
          <el-radio-group v-model="catType" size="small" @change="loadCatStats">
            <el-radio-button label="EXPENSE">支出</el-radio-button>
            <el-radio-button label="INCOME">收入</el-radio-button>
          </el-radio-group>
        </div>
        <div ref="pieChartRef" class="pie-chart" />
      </div>

      <!-- 右侧：预算设置 + 筛选 -->
      <div class="right-panel">
        <!-- 预算面板 -->
        <div class="card budget-panel">
          <div class="panel-title">💼 月度预算管理</div>
          <div class="budget-form">
            <div class="budget-row">
              <span class="budget-label">{{ currentYearMonth }} 总预算</span>
              <el-input-number
                v-model="budgetAmount"
                :min="0" :precision="2"
                size="small"
                style="width:130px"
              />
              <el-button size="small" type="primary" @click="saveBudget">设置</el-button>
            </div>
            <div v-if="budget.totalBudget" class="budget-info">
              <div class="budget-info-row">
                <span>总预算</span>
                <span class="value">¥{{ formatAmount(budget.totalBudget) }}</span>
              </div>
              <div class="budget-info-row">
                <span>已使用</span>
                <span class="value" :class="{ 'danger-text': budgetOverrun }">¥{{ formatAmount(budget.budgetUsed) }}</span>
              </div>
              <div class="budget-info-row">
                <span>剩余</span>
                <span class="value" :class="{ 'success-text': !budgetOverrun }">¥{{ formatAmount(budget.budgetRemaining) }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 筛选条件 -->
        <div class="card filter-card">
          <div class="panel-title">🔍 筛选账目</div>
          <div class="filter-controls">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              size="small"
              @change="loadList"
            />
            <el-radio-group v-model="filterType" size="small" @change="loadList">
              <el-radio-button label="">全部</el-radio-button>
              <el-radio-button label="INCOME">收入</el-radio-button>
              <el-radio-button label="EXPENSE">支出</el-radio-button>
            </el-radio-group>
          </div>
        </div>
      </div>
    </div>

    <!-- 账目列表 -->
    <div v-loading="loading" class="accounting-list">
      <div v-for="item in list" :key="item.id" class="accounting-card card">
        <div class="acc-left">
          <div class="acc-type-dot" :class="item.type" />
          <div class="acc-info">
            <div class="acc-category">{{ item.parentCategoryName ? `${item.parentCategoryName} / ` : '' }}{{ item.categoryName }}</div>
            <div class="acc-meta text-muted text-xs">
              {{ item.accountingDate }} · {{ accountTypeLabel(item.accountType) }}
              <span v-if="item.remark"> · {{ item.remark }}</span>
            </div>
          </div>
        </div>
        <div class="acc-right">
          <div class="acc-amount" :class="item.type">
            {{ item.type === 'INCOME' ? '+' : '-' }}¥{{ formatAmount(item.amount) }}
          </div>
          <el-icon class="icon-btn" @click="deleteItem(item.id)"><Delete /></el-icon>
        </div>
      </div>

      <div v-if="!loading && list.length === 0" class="empty-state">
        <div class="empty-icon">💰</div>
        <p>暂无账目记录</p>
        <el-button type="primary" size="small" @click="openAdd">记一笔</el-button>
      </div>
    </div>

    <!-- 分页 -->
    <el-pagination
      v-if="totalCount > pageSize"
      v-model:current-page="pageNum"
      :page-size="pageSize"
      :total="totalCount"
      layout="prev, pager, next"
      class="mt-md"
      @current-change="loadList"
    />

    <!-- 新增弹窗 -->
    <el-dialog v-model="showAddDialog" title="新增账目" width="480px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="类型" prop="type">
          <el-radio-group v-model="form.type" @change="onTypeChange">
            <el-radio-button label="EXPENSE">支出</el-radio-button>
            <el-radio-button label="INCOME">收入</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="金额" prop="amount">
          <el-input-number v-model="form.amount" :min="0.01" :precision="2" :step="1" style="width:200px" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-cascader
            v-model="form.categoryId"
            :options="filteredCategoryOptions"
            placeholder="选择分类"
            :props="{ value: 'id', label: 'name', children: 'children', emitPath: false }"
            style="width:100%"
          />
        </el-form-item>
        <el-form-item label="日期" prop="accountingDate">
          <el-date-picker v-model="form.accountingDate" type="date" format="YYYY-MM-DD" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="账户">
          <el-select v-model="form.accountType">
            <el-option label="微信" value="WECHAT" />
            <el-option label="支付宝" value="ALIPAY" />
            <el-option label="现金" value="CASH" />
            <el-option label="银行卡" value="BANK" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" placeholder="可选备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveItem">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { Plus, Delete } from '@element-plus/icons-vue'
import { accountingApi } from '@/api/accounting'
import type { AccountingItem } from '@/api/accounting'
import * as echarts from 'echarts'
import dayjs from 'dayjs'

const now = dayjs()
const currentYearMonth = now.format('YYYY年MM月')

const list = ref<AccountingItem[]>([])
const totalCount = ref(0)
const pageNum = ref(1)
const pageSize = 20
const monthStats = ref<Record<string, any>>({ totalIncome: 0, totalExpense: 0, balance: 0 })
const budget = ref<Record<string, any>>({})
const catStats = ref<any[]>([])
const catType = ref<'EXPENSE' | 'INCOME'>('EXPENSE')
const budgetAmount = ref(0)
const loading = ref(false)
const saving = ref(false)
const showAddDialog = ref(false)
const filterType = ref('')
const dateRange = ref<[string, string] | null>(null)
const formRef = ref<FormInstance>()
const categoryOptions = ref<any[]>([])
const pieChartRef = ref<HTMLElement>()
let pieChart: echarts.ECharts | null = null

const form = reactive({
  type: 'EXPENSE' as 'EXPENSE' | 'INCOME',
  amount: 0,
  categoryId: undefined as number | undefined,
  accountingDate: dayjs().format('YYYY-MM-DD'),
  accountType: 'WECHAT',
  remark: ''
})

const rules = {
  type: [{ required: true }],
  amount: [{ required: true, message: '请输入金额', type: 'number' as const }],
  categoryId: [{ required: true, message: '请选择分类' }],
  accountingDate: [{ required: true, message: '请选择日期' }]
}

// 根据类型过滤分类选项
const filteredCategoryOptions = computed(() =>
  categoryOptions.value.filter(c => c.type === form.type)
)

const budgetRate = computed(() => {
  if (!budget.value.totalBudget || budget.value.totalBudget === 0) return 0
  return Math.round((Number(budget.value.budgetUsed ?? 0) / Number(budget.value.totalBudget)) * 100)
})

const budgetOverrun = computed(() => budgetRate.value > 100)

function formatAmount(val: any) {
  return Number(val ?? 0).toFixed(2)
}

function accountTypeLabel(val: string) {
  const map: Record<string, string> = {
    WECHAT: '微信', ALIPAY: '支付宝', CASH: '现金', BANK: '银行卡'
  }
  return map[val] ?? val
}

function onTypeChange() {
  form.categoryId = undefined
}

async function loadList() {
  loading.value = true
  try {
    const res = await accountingApi.page({
      pageNum: pageNum.value, pageSize,
      startDate: dateRange.value?.[0],
      endDate: dateRange.value?.[1],
      type: filterType.value || undefined
    })
    list.value = res.records
    totalCount.value = res.total
  } finally {
    loading.value = false
  }
}

async function loadMonthStats() {
  monthStats.value = await accountingApi.monthlyStats(now.year(), now.month() + 1)
}

async function loadBudget() {
  try {
    budget.value = await accountingApi.getBudget(now.year(), now.month() + 1)
    budgetAmount.value = Number(budget.value.totalBudget ?? 0)
  } catch {}
}

async function loadCatStats() {
  try {
    const monthStart = now.startOf('month').format('YYYY-MM-DD')
    const monthEnd = now.endOf('month').format('YYYY-MM-DD')
    catStats.value = await accountingApi.categoryStats({
      startDate: monthStart,
      endDate: monthEnd,
      type: catType.value
    }) ?? []
    await nextTick()
    renderPieChart()
  } catch {}
}

function renderPieChart() {
  if (!pieChartRef.value) return
  if (!pieChart) pieChart = echarts.init(pieChartRef.value, 'dark')

  if (!catStats.value || !catStats.value.length) {
    pieChart.setOption({
      backgroundColor: 'transparent',
      graphic: [{
        type: 'text', left: 'center', top: 'middle',
        style: { text: `本月暂无${catType.value === 'EXPENSE' ? '支出' : '收入'}数据`, fill: '#64748b', fontSize: 13 }
      }]
    })
    return
  }

  const colors = ['#6366f1', '#10b981', '#f59e0b', '#ef4444', '#3b82f6', '#8b5cf6', '#ec4899', '#06b6d4']
  const pieData = catStats.value.map((item, i) => ({
    name: item.categoryName,
    value: Number(item.amount ?? 0),
    itemStyle: { color: colors[i % colors.length] }
  }))

  pieChart.setOption({
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'item',
      formatter: (p: any) => `${p.name}<br/>¥${Number(p.value).toFixed(2)}<br/>${p.percent}%`
    },
    legend: {
      orient: 'vertical', right: 0, top: 'center',
      textStyle: { color: '#94a3b8', fontSize: 11 },
      formatter: (name: string) => {
        const item = catStats.value.find(c => c.categoryName === name)
        return item ? `${name} ${Number(item.percentage ?? 0).toFixed(1)}%` : name
      }
    },
    series: [{
      type: 'pie',
      radius: ['35%', '65%'],
      center: ['35%', '50%'],
      data: pieData,
      label: { show: false },
      emphasis: {
        itemStyle: { shadowBlur: 10, shadowColor: 'rgba(0,0,0,0.3)' }
      }
    }]
  })
}

async function saveBudget() {
  if (!budgetAmount.value || budgetAmount.value <= 0) {
    ElMessage.warning('请输入有效的预算金额')
    return
  }
  await accountingApi.setBudget({
    budgetYear: now.year(),
    budgetMonth: now.month() + 1,
    amount: budgetAmount.value
  })
  ElMessage.success('预算已设置')
  await loadBudget()
  await loadMonthStats()
}

function openAdd() {
  Object.assign(form, {
    type: 'EXPENSE', amount: 0, categoryId: undefined,
    accountingDate: dayjs().format('YYYY-MM-DD'), accountType: 'WECHAT', remark: ''
  })
  showAddDialog.value = true
}

async function saveItem() {
  await formRef.value?.validate()
  saving.value = true
  try {
    await accountingApi.create(form as any)
    ElMessage.success('记录成功')
    showAddDialog.value = false
    await Promise.all([loadList(), loadMonthStats(), loadBudget(), loadCatStats()])
  } finally {
    saving.value = false
  }
}

async function deleteItem(id: number) {
  await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' })
  await accountingApi.delete(id)
  ElMessage.success('已删除')
  await Promise.all([loadList(), loadMonthStats(), loadBudget(), loadCatStats()])
}

async function loadCategories() {
  categoryOptions.value = await accountingApi.getCategories()
}

onMounted(() => {
  loadList()
  loadMonthStats()
  loadBudget()
  loadCategories()
  loadCatStats()
  const resizeHandler = () => pieChart?.resize()
  window.addEventListener('resize', resizeHandler)
  onUnmounted(() => {
    window.removeEventListener('resize', resizeHandler)
    pieChart?.dispose()
  })
})
</script>

<style lang="scss" scoped>
.accounting-view {
  .month-stats {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 16px;
    margin-bottom: 20px;

    @media (max-width: 1100px) {
      grid-template-columns: repeat(2, 1fr);
    }

    .budget-card {
      :deep(.el-progress-bar__outer) {
        background: rgba(255, 255, 255, 0.08);
      }
    }
  }

  .chart-filter-row {
    display: grid;
    grid-template-columns: 1fr 340px;
    gap: 16px;
    margin-bottom: 20px;
    align-items: start;

    @media (max-width: 1000px) {
      grid-template-columns: 1fr;
    }
  }

  .chart-card {
    padding: 16px 20px;

    .chart-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 12px;

      .chart-title {
        font-size: 13px;
        font-weight: 600;
        color: $text-secondary;
      }
    }

    .pie-chart {
      height: 220px;
      width: 100%;
    }
  }

  .right-panel {
    display: flex;
    flex-direction: column;
    gap: 12px;

    .budget-panel, .filter-card {
      padding: 16px;

      .panel-title {
        font-size: 13px;
        font-weight: 600;
        color: $text-secondary;
        margin-bottom: 12px;
      }
    }

    .budget-form {
      .budget-row {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 12px;

        .budget-label {
          font-size: 12px;
          color: $text-secondary;
          white-space: nowrap;
          min-width: 80px;
        }
      }

      .budget-info {
        background: $bg-input;
        border-radius: $radius-sm;
        padding: 10px 12px;

        .budget-info-row {
          display: flex;
          justify-content: space-between;
          font-size: 12px;
          color: $text-secondary;
          padding: 3px 0;

          .value {
            font-weight: 600;
            color: $text-primary;

            &.danger-text { color: $danger; }
            &.success-text { color: $success; }
          }
        }
      }
    }

    .filter-controls {
      display: flex;
      flex-direction: column;
      gap: 10px;
    }
  }

  .accounting-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .accounting-card {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 14px 16px;
    transition: $transition-fast;

    &:hover {
      background: rgba(255, 255, 255, 0.03);
    }

    .acc-left {
      display: flex;
      align-items: center;
      gap: 12px;
      flex: 1;
      min-width: 0;

      .acc-type-dot {
        width: 10px; height: 10px;
        border-radius: 50%;
        flex-shrink: 0;

        &.INCOME { background: $success; }
        &.EXPENSE { background: $danger; }
      }

      .acc-info {
        min-width: 0;

        .acc-category { font-size: 14px; font-weight: 500; }
        .acc-meta { margin-top: 2px; }
      }
    }

    .acc-right {
      display: flex;
      align-items: center;
      gap: 10px;
      flex-shrink: 0;

      .acc-amount {
        font-size: 16px;
        font-weight: 700;
        white-space: nowrap;

        &.INCOME { color: $success; }
        &.EXPENSE { color: $danger; }
      }

      .icon-btn {
        color: $text-muted;
        cursor: pointer;
        padding: 4px;
        border-radius: $radius-sm;
        font-size: 15px;
        transition: $transition-fast;
        opacity: 0;

        &:hover { color: $danger; background: rgba($danger, 0.1); }
      }
    }

    &:hover .icon-btn { opacity: 1; }
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

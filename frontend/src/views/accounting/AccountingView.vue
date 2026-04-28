<template>
  <div class="accounting-view">
    <div class="page-header">
      <div>
        <h1 class="page-title">每日记账</h1>
        <p class="page-subtitle">记录每一笔收支，掌握财务状况</p>
      </div>
      <el-button type="primary" size="small" :icon="Plus" @click="showAddDialog = true"
        >新增账目</el-button
      >
    </div>

    <!-- 月度统计卡片 -->
    <div class="month-stats">
      <div class="stat-card" style="--stat-color: #10b981; --stat-bg: rgba(16, 185, 129, 0.12)">
        <div class="stat-icon">💰</div>
        <div class="stat-value">¥{{ formatAmount(monthStats.totalIncome) }}</div>
        <div class="stat-label">本月收入</div>
      </div>
      <div class="stat-card" style="--stat-color: #ef4444; --stat-bg: rgba(239, 68, 68, 0.12)">
        <div class="stat-icon">💸</div>
        <div class="stat-value">¥{{ formatAmount(monthStats.totalExpense) }}</div>
        <div class="stat-label">本月支出</div>
      </div>
      <div
        class="stat-card"
        :style="{
          '--stat-color': monthStats.balance >= 0 ? '#10b981' : '#ef4444',
          '--stat-bg': monthStats.balance >= 0 ? 'rgba(16,185,129,0.12)' : 'rgba(239,68,68,0.12)'
        }"
      >
        <div class="stat-icon">📊</div>
        <div class="stat-value">¥{{ formatAmount(Math.abs(monthStats.balance)) }}</div>
        <div class="stat-label">{{ monthStats.balance >= 0 ? '结余' : '超支' }}</div>
      </div>
    </div>

    <!-- 预算卡片 -->
    <div class="budget-card card mb-md">
      <div v-if="budgetInfo && budgetInfo.totalBudget">
        <div class="flex items-center justify-between mb-sm">
          <span class="font-bold">本月总预算</span>
          <div class="text-sm">
            已用: <span class="text-danger font-bold">¥{{ formatAmount(budgetInfo.budgetUsed) }}</span>
            / 共 ¥{{ formatAmount(budgetInfo.totalBudget) }}
            <el-button type="primary" link size="small" style="margin-left: 8px" @click="openBudgetDialog">修改</el-button>
          </div>
        </div>
        <el-progress
          :percentage="budgetInfo.budgetRate > 100 ? 100 : Number(budgetInfo.budgetRate)"
          :status="budgetInfo.budgetRate >= 100 ? 'exception' : budgetInfo.budgetRate > 80 ? 'warning' : 'success'"
          :stroke-width="10"
        />
        <div v-if="budgetInfo.budgetRemaining >= 0" class="text-xs text-muted mt-xs text-right">
          剩余 ¥{{ formatAmount(budgetInfo.budgetRemaining) }}
        </div>
        <div v-else class="text-xs text-danger mt-xs text-right">
          超支 ¥{{ formatAmount(Math.abs(budgetInfo.budgetRemaining)) }}
        </div>
      </div>
      <div v-else class="flex items-center justify-between">
        <span class="text-muted text-sm">尚未设置本月预算，合理规划财务从预算开始</span>
        <el-button type="primary" plain size="small" @click="openBudgetDialog">设置预算</el-button>
      </div>
    </div>

    <!-- 筛选 -->
    <div class="filter-bar flex items-center gap-md mb-md">
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

    <!-- 账目列表 -->
    <div v-loading="loading" class="accounting-list">
      <div v-for="item in list" :key="item.id" class="accounting-card card">
        <div class="acc-type-dot" :class="item.type" />
        <div class="acc-info">
          <div class="acc-category">
            {{ item.parentCategoryName ? `${item.parentCategoryName} / ` : ''
            }}{{ item.categoryName }}
          </div>
          <div class="acc-remark text-muted text-sm">{{ item.remark || item.accountingDate }}</div>
        </div>
        <div class="acc-amount" :class="item.type">
          {{ item.type === 'INCOME' ? '+' : '-' }}¥{{ formatAmount(item.amount) }}
        </div>
        <div class="acc-actions">
          <el-icon class="icon-btn" @click="deleteItem(item.id)"><Delete /></el-icon>
        </div>
      </div>

      <div v-if="!loading && list.length === 0" class="empty-state">
        <div class="empty-icon">💰</div>
        <p>暂无账目记录</p>
      </div>
    </div>

    <!-- 新增弹窗 -->
    <el-dialog v-model="showAddDialog" title="新增账目" width="480px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio-button label="EXPENSE">支出</el-radio-button>
            <el-radio-button label="INCOME">收入</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="金额" prop="amount">
          <el-input-number
            v-model="form.amount"
            :min="0.01"
            :precision="2"
            :step="1"
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-cascader
            v-model="form.categoryId"
            :options="categoryOptions"
            placeholder="选择分类"
            :props="{ value: 'id', label: 'name', children: 'children', emitPath: false }"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="日期" prop="accountingDate">
          <el-date-picker
            v-model="form.accountingDate"
            type="date"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
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

    <!-- 设置预算弹窗 -->
    <el-dialog v-model="showBudgetDialog" title="设置总预算" width="400px" destroy-on-close>
      <el-form :model="budgetForm" label-width="100px">
        <el-form-item label="预算月份">
          <el-date-picker
            v-model="budgetForm.month"
            type="month"
            format="YYYY-MM"
            value-format="YYYY-MM"
            placeholder="选择月份"
          />
        </el-form-item>
        <el-form-item label="总预算金额">
          <el-input-number v-model="budgetForm.amount" :min="1" :step="100" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showBudgetDialog = false">取消</el-button>
        <el-button type="primary" :loading="savingBudget" @click="saveBudget">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { Plus, Delete } from '@element-plus/icons-vue'
import { accountingApi } from '@/api/accounting'
import type { AccountingItem } from '@/api/accounting'
import dayjs from 'dayjs'

const list = ref<AccountingItem[]>([])
const monthStats = ref<Record<string, any>>({ totalIncome: 0, totalExpense: 0, balance: 0 })
const budgetInfo = ref<Record<string, any> | null>(null)
const loading = ref(false)
const saving = ref(false)
const showAddDialog = ref(false)
const showBudgetDialog = ref(false)
const savingBudget = ref(false)
const filterType = ref('')
const dateRange = ref<[string, string] | null>(null)
const formRef = ref<FormInstance>()
const categoryOptions = ref<any[]>([])

const budgetForm = reactive({
  month: dayjs().format('YYYY-MM'),
  amount: 3000
})

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

function formatAmount(val: any) {
  return Number(val ?? 0).toFixed(2)
}

async function loadList() {
  loading.value = true
  try {
    const res = await accountingApi.page({
      pageNum: 1,
      pageSize: 50,
      startDate: dateRange.value?.[0],
      endDate: dateRange.value?.[1],
      type: filterType.value || undefined
    })
    list.value = res.records
  } finally {
    loading.value = false
  }
}

async function loadMonthStats() {
  const now = dayjs()
  monthStats.value = await accountingApi.monthlyStats(now.year(), now.month() + 1)
  try {
    budgetInfo.value = await accountingApi.getBudget(now.year(), now.month() + 1)
  } catch {
    budgetInfo.value = null
  }
}

function openBudgetDialog() {
  budgetForm.month = dayjs().format('YYYY-MM')
  budgetForm.amount = budgetInfo.value?.totalBudget || 3000
  showBudgetDialog.value = true
}

async function saveBudget() {
  savingBudget.value = true
  try {
    const year = Number(budgetForm.month.split('-')[0])
    const month = Number(budgetForm.month.split('-')[1])
    await accountingApi.setBudget({
      budgetYear: year,
      budgetMonth: month,
      amount: budgetForm.amount
    })
    ElMessage.success('设置预算成功')
    showBudgetDialog.value = false
    loadMonthStats()
  } finally {
    savingBudget.value = false
  }
}

async function saveItem() {
  await formRef.value?.validate()
  saving.value = true
  try {
    await accountingApi.create(form as any)
    ElMessage.success('记录成功')
    showAddDialog.value = false
    Object.assign(form, { type: 'EXPENSE', amount: 0, categoryId: undefined, remark: '' })
    await Promise.all([loadList(), loadMonthStats()])
  } finally {
    saving.value = false
  }
}

async function deleteItem(id: number) {
  await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' })
  await accountingApi.delete(id)
  ElMessage.success('已删除')
  await Promise.all([loadList(), loadMonthStats()])
}

async function loadCategories() {
  categoryOptions.value = await accountingApi.getCategories()
}

onMounted(() => {
  loadList()
  loadMonthStats()
  loadCategories()
})
</script>

<style lang="scss" scoped>
.accounting-view {
  .month-stats {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 16px;
    margin-bottom: 20px;
  }

  .budget-card {
    padding: 16px 20px;
  }

  .accounting-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .accounting-card {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 14px 16px;

    .acc-type-dot {
      width: 10px;
      height: 10px;
      border-radius: 50%;
      flex-shrink: 0;

      &.INCOME {
        background: $success;
      }
      &.EXPENSE {
        background: $danger;
      }
    }

    .acc-info {
      flex: 1;
      min-width: 0;

      .acc-category {
        font-size: 14px;
        font-weight: 500;
      }
      .acc-remark {
        margin-top: 2px;
      }
    }

    .acc-amount {
      font-size: 16px;
      font-weight: 700;
      white-space: nowrap;

      &.INCOME {
        color: $success;
      }
      &.EXPENSE {
        color: $danger;
      }
    }

    .acc-actions {
      .icon-btn {
        color: $text-muted;
        cursor: pointer;
        padding: 4px;
        border-radius: $radius-sm;
        transition: $transition-fast;

        &:hover {
          color: $danger;
          background: rgba($danger, 0.1);
        }
      }
    }
  }

  .empty-state {
    text-align: center;
    padding: 60px 20px;
    color: $text-muted;
    .empty-icon {
      font-size: 48px;
      margin-bottom: 12px;
    }
  }
}
</style>

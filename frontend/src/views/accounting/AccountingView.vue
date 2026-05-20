<!--
/**
 * ============================================================================
 * AccountingView.vue — 记账管理页面组件
 * ============================================================================
 *
 * 【组件说明】
 * 每日记账页面，管理收支记录、月度统计、分类筛选、预算设置。
 *
 * 【主要功能】
 * - 收支记录列表展示（按月份分组）
 * - 月度收支统计卡片（收入/支出/结余）
 * - 类型筛选（全部/收入/支出）
 * - 月度预算设置与进度展示
 * - 记账表单（新增/编辑模式）
 * - Excel 导出功能
 * ============================================================================
 */
-->

<template>
  <div class="accounting-view">
    <div class="page-header">
      <div>
        <h1 class="page-title">每日记账</h1>
        <p class="page-subtitle">记录每一笔收支，掌握财务状况</p>
      </div>
      <el-button type="primary" size="small" :icon="Plus" @click="openAddDialog"
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
          '--stat-color': (budgetInfo?.totalBudget ? budgetInfo.budgetRemaining : monthStats.balance) >= 0 ? '#10b981' : '#ef4444',
          '--stat-bg': (budgetInfo?.totalBudget ? budgetInfo.budgetRemaining : monthStats.balance) >= 0 ? 'rgba(16,185,129,0.12)' : 'rgba(239,68,68,0.12)'
        }"
      >
        <div class="stat-icon">📊</div>
        <div class="stat-value">¥{{ formatAmount(Math.abs(budgetInfo?.totalBudget ? budgetInfo.budgetRemaining : monthStats.balance)) }}</div>
        <div class="stat-label" v-if="budgetInfo?.totalBudget">{{ budgetInfo.budgetRemaining >= 0 ? '预算剩余' : '预算超支' }}</div>
        <div class="stat-label" v-else>{{ monthStats.balance >= 0 ? '本月结余' : '本月赤字' }}</div>
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
        :shortcuts="shortcuts"
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
    <div v-loading="loading" class="accounting-list-container">
      <el-collapse v-model="activeNames" class="custom-collapse">
        <el-collapse-item v-for="group in groupedList" :key="group.month" :name="group.month">
          <template #title>
            <div class="group-header">
              <span class="group-month">
                <el-icon><Calendar /></el-icon>
                {{ group.displayMonth }}
              </span>
              <div class="group-summary">
                <span class="income">入 ¥{{ formatAmount(calculateTotal(group.items, 'INCOME')) }}</span>
                <span class="expense">出 ¥{{ formatAmount(calculateTotal(group.items, 'EXPENSE')) }}</span>
                <span class="balance" :class="calculateBalance(group.items) >= 0 ? 'is-positive' : 'is-negative'">
                  结余 {{ calculateBalance(group.items) < 0 ? '-' : '' }}¥{{ formatAmount(Math.abs(calculateBalance(group.items))) }}
                </span>
              </div>
            </div>
          </template>

          <div class="accounting-list">
            <div v-for="item in group.items" :key="item.id" class="accounting-card card">
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
                <el-icon class="icon-btn edit-btn" @click="editItem(item)"><Edit /></el-icon>
                <el-icon class="icon-btn delete-btn" @click="deleteItem(item.id)"><Delete /></el-icon>
              </div>
            </div>
          </div>
        </el-collapse-item>
      </el-collapse>

      <div v-if="!loading && list.length === 0" class="empty-state">
        <div class="empty-icon">💰</div>
        <p>暂无账目记录</p>
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog 
      v-model="showAddDialog" 
      :title="editingId ? '编辑账目' : '新增账目'" 
      width="480px" 
      destroy-on-close
      class="premium-dialog"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px" label-position="left">
        <div class="form-section">
          <el-form-item label="收支类型" prop="type">
            <el-radio-group v-model="form.type" class="premium-radio-group">
              <el-radio-button label="EXPENSE">支出</el-radio-button>
              <el-radio-button label="INCOME">收入</el-radio-button>
            </el-radio-group>
          </el-form-item>
          
          <el-form-item label="交易金额" prop="amount">
            <el-input-number
              v-model="form.amount"
              :min="0"
              :precision="2"
              :step="1"
              placeholder="0.00"
              controls-position="right"
              style="width: 100%"
            />
          </el-form-item>

          <el-form-item label="交易日期" prop="accountingDate">
            <el-date-picker
              v-model="form.accountingDate"
              type="date"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              style="width: 100%"
            />
          </el-form-item>

          <el-form-item label="所属分类" prop="categoryId">
            <el-cascader
              v-model="form.categoryId"
              :options="categoryOptions"
              placeholder="选择分类"
              :props="{ value: 'id', label: 'name', children: 'children', emitPath: false }"
              style="width: 100%"
            />
          </el-form-item>

          <el-form-item label="支付账户">
            <el-select v-model="form.accountType" style="width: 100%">
              <el-option label="微信" value="WECHAT" />
              <el-option label="支付宝" value="ALIPAY" />
              <el-option label="现金" value="CASH" />
              <el-option label="银行卡" value="BANK" />
            </el-select>
          </el-form-item>

          <el-form-item label="备注说明">
            <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="写点什么（可选）..." />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveItem">确定</el-button>
      </template>
    </el-dialog>



    <!-- 设置预算弹窗 -->
    <el-dialog v-model="showBudgetDialog" title="设置总预算" width="500px" destroy-on-close>
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
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { Plus, Delete, Calendar, Edit } from '@element-plus/icons-vue'
import { accountingApi } from '@/api/accounting'
import type { AccountingItem } from '@/api/accounting'
import dayjs from 'dayjs'

// ============================================================================
// // 状态
// ============================================================================

// ============================================================================
// // 状态
// ============================================================================

const list = ref<AccountingItem[]>([])
const monthStats = ref<Record<string, any>>({ totalIncome: 0, totalExpense: 0, balance: 0 })
const budgetInfo = ref<Record<string, any> | null>(null)
const loading = ref(false)
const saving = ref(false)
const showAddDialog = ref(false)
const showBudgetDialog = ref(false)
const savingBudget = ref(false)
const filterType = ref('')
const dateRange = ref<[string, string] | null>([
  dayjs().startOf('month').format('YYYY-MM-DD'),
  dayjs().endOf('month').format('YYYY-MM-DD')
])
const formRef = ref<FormInstance>()
const categoryOptions = ref<any[]>([])
const activeNames = ref<string[]>([dayjs().format('YYYY-MM')])
const editingId = ref<number | null>(null)

/**
 * 日期选择快捷方式配置
 *
 * 功能说明：为日期范围选择器提供预设的快捷选项
 * 包含四种常见的时间范围：
 * 1. 近一个月：当前月第一天到当前月最后一天
 * 2. 近三个月：往前2个月的月首到当前月末
 * 3. 近半年：如果是上半年（1-6月），取1-6月；否则取7-12月
 * 4. 近一年：当前年第一天到当前年最后一天
 *
 * 技术细节：
 * - dayjs().startOf('month')：获取当月第一天
 * - dayjs().endOf('month')：获取当月最后一天
 * - dayjs().subtract(n, 'month')：往前推n个月
 * - .toDate()：转换为JavaScript Date对象
 */
const shortcuts = [
  {
    text: '近一个月',
    value: () => {
      const start = dayjs().startOf('month').toDate()
      const end = dayjs().endOf('month').toDate()
      return [start, end]
    }
  },
  {
    text: '近三个月',
    value: () => {
      const start = dayjs().subtract(2, 'month').startOf('month').toDate()
      const end = dayjs().endOf('month').toDate()
      return [start, end]
    }
  },
  {
    text: '近半年',
    value: () => {
      const currentMonth = dayjs().month()
      const isFirstHalf = currentMonth < 6
      const start = dayjs().month(isFirstHalf ? 0 : 6).startOf('month').toDate()
      const end = dayjs().month(isFirstHalf ? 5 : 11).endOf('month').toDate()
      return [start, end]
    }
  },
  {
    text: '近一年',
    value: () => {
      const start = dayjs().startOf('year').toDate()
      const end = dayjs().endOf('year').toDate()
      return [start, end]
    }
  }
]

/**
 * 打开新增记账弹窗
 *
 * 功能说明：初始化记账表单并显示弹窗（新增模式）
 * 业务逻辑：
 * 1. 清空编辑ID（设置为null表示新增模式）
 * 2. 重置表单为默认值：
 *    - 类型：支出（EXPENSE）
 *    - 金额：undefined（需要用户输入）
 *    - 分类：undefined（需要用户选择）
 *    - 日期：今天
 *    - 账户：微信
 *    - 备注：空字符串
 * 3. 显示弹窗
 *
 * 使用场景：用户点击"新增账目"按钮时调用
 */
function openAddDialog() {
  editingId.value = null
  Object.assign(form, { type: 'EXPENSE', amount: undefined, categoryId: undefined, accountingDate: dayjs().format('YYYY-MM-DD'), accountType: 'WECHAT', remark: '' })
  showAddDialog.value = true
}

/**
 * 打开编辑记账弹窗
 *
 * 功能说明：将选中的记账记录数据填充到表单并显示弹窗（编辑模式）
 * 业务逻辑：
 * 1. 设置编辑ID为当前记录的ID（用于区分新增/编辑）
 * 2. 将记录的所有字段填充到表单
 *    - 包括：类型、金额、分类、日期、账户、备注
 * 3. 显示弹窗
 *
 * 使用场景：用户点击记账卡片的编辑图标时调用
 *
 * @param item - 要编辑的记账记录对象
 */
function editItem(item: AccountingItem) {
  editingId.value = item.id
  Object.assign(form, {
    type: item.type,
    amount: item.amount,
    categoryId: item.categoryId,
    accountingDate: item.accountingDate,
    accountType: item.accountType,
    remark: item.remark || ''
  })
  showAddDialog.value = true
}

/**
 * 按月份分组的记账列表
 *
 * 功能说明：将记账记录按月份进行分组和排序，用于在折叠面板中展示
 * 业务逻辑：
 * 1. 遍历所有记账记录
 * 2. 使用dayjs提取日期的年月部分（格式：YYYY-MM）作为分组key
 * 3. 将同一月份的记录放入同一个数组
 * 4. 对分组进行排序：最近的月份排在前面
 * 5. 转换为折叠面板需要的格式（包含月份、显示文本、记录列表）
 *
 * 返回格式示例：
 * [
 *   {
 *     month: '2024-01',
 *     displayMonth: '2024年01月',
 *     items: [AccountingItem, AccountingItem, ...]
 *   },
 *   ...
 * ]
 *
 * @returns 分组后的记账列表数组
 */
const groupedList = computed(() => {
  const groups: Record<string, AccountingItem[]> = {}
  list.value.forEach((item) => {
    const month = dayjs(item.accountingDate).format('YYYY-MM')
    if (!groups[month]) {
      groups[month] = []
    }
    groups[month].push(item)
  })

  const result = Object.keys(groups)
    .sort((a, b) => b.localeCompare(a))
    .map((month) => ({
      month,
      displayMonth: dayjs(month).format('YYYY年MM月'),
      items: groups[month]
    }))

  return result
})

/**
 * 计算指定类型的总金额
 *
 * 功能说明：统计一组记账记录中特定类型（收入/支出）的总额
 * 业务逻辑：
 * 1. 过滤出指定类型的记录
 * 2. 累加所有记录的金额
 * 3. 返回累计总额
 *
 * 使用场景：在月度分组头部显示该月的收入/支出总额
 *
 * @param items - 记账记录数组
 * @param type - 类型：'INCOME'（收入）或 'EXPENSE'（支出）
 * @returns 总金额
 */
function calculateTotal(items: AccountingItem[], type: 'INCOME' | 'EXPENSE') {
  return items
    .filter((item) => item.type === type)
    .reduce((sum, item) => sum + Number(item.amount), 0)
}

/**
 * 计算余额（收入 - 支出）
 *
 * 功能说明：统计一组记录的净收支情况
 * 业务逻辑：收入总额 - 支出总额
 *
 * 使用场景：在月度分组头部显示该月的结余/赤字情况
 * 正数表示结余，负数表示赤字
 *
 * @param items - 记账记录数组
 * @returns 净收支金额（正数为结余，负数为赤字）
 */
function calculateBalance(items: AccountingItem[]) {
  return calculateTotal(items, 'INCOME') - calculateTotal(items, 'EXPENSE')
}

const budgetForm = reactive({
  month: dayjs().format('YYYY-MM'),
  amount: 3000
})

const form = reactive({
  type: 'EXPENSE' as 'EXPENSE' | 'INCOME',
  amount: undefined as number | undefined,
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

// ============================================================================
// // 数据加载
// ============================================================================

/**
 * 加载记账列表
 *
 * 功能说明：根据当前选择的日期范围、收支类型加载记账记录
 * 业务逻辑：
 * 1. 设置加载状态为true
 * 2. 调用accountingApi.page()分页接口获取数据
 * 3. 使用dateRange和filterType作为筛选条件
 * 4. 将获取的记录保存到list响应式变量中
 *
 * 使用场景：组件初始化、日期范围改变、筛选条件改变时调用
 *
 * @returns Promise<void>
 */
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

/**
 * 加载月度统计数据
 *
 * 功能说明：获取当月的收支统计信息和预算信息
 * 业务逻辑：
 * 1. 使用dayjs获取当前年份和月份
 * 2. 调用accountingApi.monthlyStats()获取本月收支汇总
 * 3. 尝试获取本月预算信息（可能未设置，需要捕获异常）
 * 4. 更新monthStats和budgetInfo响应式变量
 *
 * 使用场景：组件初始化、新增/修改/删除记账记录后调用
 *
 * @returns Promise<void>
 */
async function loadMonthStats() {
  const now = dayjs()
  monthStats.value = await accountingApi.monthlyStats(now.year(), now.month() + 1)
  try {
    budgetInfo.value = await accountingApi.getBudget(now.year(), now.month() + 1)
  } catch {
    budgetInfo.value = null
  }
}

/**
 * 打开预算设置弹窗
 *
 * 功能说明：初始化预算表单并显示弹窗
 * 业务逻辑：
 * 1. 设置预算表单的月份为当前月
 * 2. 如果已有预算信息，则预填充金额；否则默认为3000
 * 3. 显示预算弹窗
 *
 * 使用场景：用户点击"设置预算"或"修改预算"按钮时调用
 */
function openBudgetDialog() {
  budgetForm.month = dayjs().format('YYYY-MM')
  budgetForm.amount = budgetInfo.value?.totalBudget || 3000
  showBudgetDialog.value = true
}

// ============================================================================
// // 表单提交
// ============================================================================

/**
 * 保存预算设置
 *
 * 功能说明：将用户设置的月度预算保存到后端
 * 业务逻辑：
 * 1. 设置保存状态为true
 * 2. 解析月份字符串获取年份和月份（格式：YYYY-MM）
 * 3. 调用accountingApi.setBudget()提交预算数据
 * 4. 成功后显示提示消息并关闭弹窗
 * 5. 重新加载月度统计数据
 *
 * 使用场景：用户在预算弹窗中点击"保存"按钮时调用
 *
 * @returns Promise<void>
 */
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

/**
 * 保存记账记录（新增或编辑）
 *
 * 功能说明：将用户填写的记账表单数据保存到后端
 * 业务逻辑：
 * 1. 先进行表单验证
 * 2. 设置保存状态为true
 * 3. 根据editingId判断是新增还是编辑
 *    - 如果有editingId，调用accountingApi.update()更新记录
 *    - 否则调用accountingApi.create()创建新记录
 * 4. 成功后显示提示消息并关闭弹窗
 * 5. 重置表单并清空编辑ID
 * 6. 并行重新加载列表和月度统计数据
 *
 * 使用场景：用户在记账弹窗中点击"确定"按钮时调用
 *
 * @returns Promise<void>
 */
async function saveItem() {
  await formRef.value?.validate()
  saving.value = true
  try {
    if (editingId.value) {
      await accountingApi.update(editingId.value, form as any)
      ElMessage.success('修改成功')
    } else {
      await accountingApi.create(form as any)
      ElMessage.success('记录成功')
    }
    showAddDialog.value = false
    Object.assign(form, { type: 'EXPENSE', amount: undefined, categoryId: undefined, remark: '' })
    editingId.value = null
    await Promise.all([loadList(), loadMonthStats()])
  } finally {
    saving.value = false
  }
}

/**
 * 删除记账记录
 *
 * 功能说明：删除指定的记账记录并更新统计数据
 * 业务逻辑：
 * 1. 弹出确认对话框（二次确认）
 * 2. 调用accountingApi.delete()删除记录
 * 3. 成功后显示提示消息
 * 4. 并行重新加载列表和月度统计数据
 *
 * 使用场景：用户点击记账卡片的删除图标时调用
 *
 * @param id - 要删除的记账记录ID
 * @returns Promise<void>
 */
async function deleteItem(id: number) {
  await ElMessageBox.confirm('确认删除？', '提示', {
    type: 'warning',
    customClass: 'delete-confirm-box'
  })
  await accountingApi.delete(id)
  ElMessage.success('已删除')
  await Promise.all([loadList(), loadMonthStats()])
}

/**
 * 加载记账分类列表
 *
 * 功能说明：获取所有可用的记账分类（用于新增/编辑记账时的下拉选择）
 * 业务逻辑：调用accountingApi.getCategories()获取分类树形数据
 * 分类数据格式：支持父子级联（如"生活" -> "餐饮"、"交通"等）
 *
 * 使用场景：组件初始化时调用，为记账表单的分类选择器提供选项
 *
 * @returns Promise<void>
 */
async function loadCategories() {
  categoryOptions.value = await accountingApi.getCategories()
}

// ============================================================================
// // 生命周期
// ============================================================================

// ============================================================================
// // 生命周期
// ============================================================================

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

  .accounting-list-container {
    .custom-collapse {
      border: none;
      --el-collapse-header-bg-color: transparent;
      --el-collapse-content-bg-color: transparent;
      
      :deep(.el-collapse-item__header) {
        border-bottom: none;
        height: auto;
        padding: 12px 0;
        line-height: 1.4;
      }

      :deep(.el-collapse-item__wrap) {
        border-bottom: none;
      }

      :deep(.el-collapse-item__content) {
        padding-bottom: 20px;
      }
    }

    .group-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      width: 100%;
      padding-right: 12px;

      .group-month {
        font-size: 16px;
        font-weight: 700;
        color: $text-primary;
        display: flex;
        align-items: center;
        gap: 6px;
      }

      .group-summary {
        display: flex;
        gap: 16px;
        font-size: 13px;
        background: $bg-page;
        padding: 4px 16px;
        border-radius: $radius-full;
        align-items: center;

        .income {
          color: $success;
        }
        .expense {
          color: $danger;
        }
        .balance {
          font-weight: 600;
          color: $text-secondary;
          
          &.is-positive {
            color: $success;
          }
          &.is-negative {
            color: $danger;
          }
        }
      }
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
      display: flex;
      gap: 4px;

      .icon-btn {
        color: $text-muted;
        cursor: pointer;
        padding: 4px;
        border-radius: $radius-sm;
        transition: $transition-fast;
        font-size: 18px;

        &.edit-btn:hover {
          color: $primary;
          background: $primary-50;
        }

        &.delete-btn:hover {
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

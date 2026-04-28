<template>
  <div class="plan-view">
    <!-- 标题 + 日期选择 -->
    <div class="page-header">
      <div>
        <h1 class="page-title">每日计划</h1>
        <p class="page-subtitle">
          {{ selectedDate }} · {{ stats.total ?? 0 }} 个任务，已完成 {{ stats.done ?? 0 }} 个
        </p>
      </div>
      <div class="flex items-center gap-md">
        <el-date-picker
          v-model="selectedDate"
          type="date"
          placeholder="选择日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          size="small"
          @change="loadPlans"
        />
        <el-button type="primary" size="small" :icon="Plus" @click="showAddDialog = true"
          >新增任务</el-button
        >
      </div>
    </div>

    <!-- 统计栏 -->
    <div class="stats-bar">
      <div class="stat-pill" v-for="s in statusStats" :key="s.label">
        <span class="dot" :style="{ background: s.color }"></span>
        <span class="label">{{ s.label }}</span>
        <span class="value">{{ s.count }}</span>
      </div>
      <div class="stat-pill ml-auto">
        <span class="label">完成率</span>
        <span class="value" style="color: #6366f1">{{ stats.completionRate ?? 0 }}%</span>
      </div>
    </div>

    <!-- 分类筛选 -->
    <div class="filter-bar">
      <el-radio-group v-model="filterCategory" size="small" @change="filterPlans">
        <el-radio-button label="">全部</el-radio-button>
        <el-radio-button v-for="cat in categories" :key="cat.value" :label="cat.value">{{
          cat.label
        }}</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 任务列表 -->
    <div v-loading="loading" class="task-list">
      <div
        v-for="(plan, idx) in filteredPlans"
        :key="plan.id"
        class="task-card card animate-fade-in-up"
        :class="{ done: plan.status === 'DONE', cancelled: plan.status === 'CANCELLED' }"
        :style="{ animationDelay: idx * 0.04 + 's' }"
      >
        <!-- 左侧：完成复选框 -->
        <div class="task-check" @click="toggleStatus(plan)">
          <div class="check-circle" :class="plan.status">
            <el-icon v-if="plan.status === 'DONE'"><Check /></el-icon>
          </div>
        </div>

        <!-- 内容 -->
        <div class="task-content">
          <div class="task-title">{{ plan.title }}</div>
          <div v-if="plan.description" class="task-desc">{{ plan.description }}</div>
          <div class="task-meta">
            <span class="priority-badge" :class="plan.priority.toLowerCase()">{{
              plan.priority
            }}</span>
            <span class="tag">{{ categoryLabel(plan.category) }}</span>
            <span v-if="plan.estimatedMins" class="text-muted text-xs"
              >⏱ {{ plan.estimatedMins }}min</span
            >
          </div>
        </div>

        <!-- 右侧操作 -->
        <div class="task-actions">
          <el-dropdown trigger="click" @command="(cmd: string) => handleCommand(cmd, plan)">
            <el-icon class="action-trigger"><MoreFilled /></el-icon>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="edit">编辑</el-dropdown-item>
                <el-dropdown-item command="postpone">顺延至明天</el-dropdown-item>
                <el-dropdown-item v-if="plan.status !== 'IN_PROGRESS'" command="inprogress"
                  >标记进行中</el-dropdown-item
                >
                <el-dropdown-item v-if="plan.status !== 'CANCELLED'" command="cancel"
                  >取消</el-dropdown-item
                >
                <el-dropdown-item command="delete" divided class="danger">删除</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="!loading && filteredPlans.length === 0" class="empty-state">
        <div class="empty-icon">📋</div>
        <p>今天还没有任务，快来添加吧</p>
        <el-button type="primary" size="small" @click="showAddDialog = true">添加任务</el-button>
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="showAddDialog"
      :title="editingPlan ? '编辑任务' : '新增任务'"
      width="520px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="任务标题" prop="title">
          <el-input v-model="form.title" placeholder="输入任务标题" />
        </el-form-item>
        <el-form-item label="任务描述">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="可选" />
        </el-form-item>
        <el-form-item label="计划日期" prop="planDate">
          <el-date-picker
            v-model="form.planDate"
            type="date"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <div class="flex gap-md">
          <el-form-item label="优先级" style="flex: 1">
            <el-select v-model="form.priority">
              <el-option v-for="p in priorities" :key="p.value" :label="p.label" :value="p.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="分类" style="flex: 1">
            <el-select v-model="form.category">
              <el-option v-for="c in categories" :key="c.value" :label="c.label" :value="c.value" />
            </el-select>
          </el-form-item>
        </div>
        <el-form-item label="预估时间">
          <el-input-number
            v-model="form.estimatedMins"
            :min="0"
            :step="15"
            placeholder="分钟"
            style="width: 140px"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="savePlan">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { Plus, Check, MoreFilled } from '@element-plus/icons-vue'
import { planApi } from '@/api/plan'
import type { PlanItem } from '@/api/plan'
import dayjs from 'dayjs'

const selectedDate = ref(dayjs().format('YYYY-MM-DD'))
const filterCategory = ref('')
const plans = ref<PlanItem[]>([])
const stats = ref<Record<string, any>>({})
const loading = ref(false)
const saving = ref(false)
const showAddDialog = ref(false)
const editingPlan = ref<PlanItem | null>(null)
const formRef = ref<FormInstance>()

const categories = [
  { value: 'WORK', label: '工作' },
  { value: 'STUDY', label: '学习' },
  { value: 'LIFE', label: '生活' },
  { value: 'HEALTH', label: '健康' }
]

const priorities = [
  { value: 'P0', label: 'P0 - 紧急' },
  { value: 'P1', label: 'P1 - 重要' },
  { value: 'P2', label: 'P2 - 普通' },
  { value: 'P3', label: 'P3 - 低优' }
]

const form = reactive({
  title: '',
  description: '',
  planDate: selectedDate.value,
  priority: 'P2',
  category: 'WORK',
  estimatedMins: undefined as number | undefined
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  planDate: [{ required: true, message: '请选择日期', trigger: 'change' }]
}

const filteredPlans = computed(() =>
  filterCategory.value
    ? plans.value.filter((p) => p.category === filterCategory.value)
    : plans.value
)

const statusStats = computed(() => [
  {
    label: '待完成',
    count: plans.value.filter((p) => p.status === 'TODO').length,
    color: '#64748b'
  },
  {
    label: '进行中',
    count: plans.value.filter((p) => p.status === 'IN_PROGRESS').length,
    color: '#f59e0b'
  },
  {
    label: '已完成',
    count: plans.value.filter((p) => p.status === 'DONE').length,
    color: '#10b981'
  },
  {
    label: '已取消',
    count: plans.value.filter((p) => p.status === 'CANCELLED').length,
    color: '#ef4444'
  }
])

function categoryLabel(val: string) {
  return categories.find((c) => c.value === val)?.label ?? val
}

async function loadPlans() {
  loading.value = true
  try {
    ;[plans.value, stats.value] = await Promise.all([
      planApi.list(selectedDate.value),
      planApi.statistics(selectedDate.value)
    ])
  } finally {
    loading.value = false
  }
}

async function toggleStatus(plan: PlanItem) {
  const next =
    plan.status === 'TODO' ? 'IN_PROGRESS' : plan.status === 'IN_PROGRESS' ? 'DONE' : 'TODO'
  await planApi.updateStatus(plan.id, next)
  plan.status = next as any
  stats.value = await planApi.statistics(selectedDate.value)
}

function filterPlans() {
  /* reactive */
}

function handleCommand(cmd: string, plan: PlanItem) {
  if (cmd === 'edit') {
    editingPlan.value = plan
    Object.assign(form, {
      title: plan.title,
      description: plan.description ?? '',
      planDate: plan.planDate,
      priority: plan.priority,
      category: plan.category,
      estimatedMins: plan.estimatedMins ?? undefined
    })
    showAddDialog.value = true
  } else if (cmd === 'postpone') {
    planApi.postpone(plan.id).then(() => {
      ElMessage.success('已顺延至明天')
      loadPlans()
    })
  } else if (cmd === 'inprogress') {
    planApi.updateStatus(plan.id, 'IN_PROGRESS').then(() => {
      plan.status = 'IN_PROGRESS'
    })
  } else if (cmd === 'cancel') {
    planApi.updateStatus(plan.id, 'CANCELLED').then(() => {
      plan.status = 'CANCELLED'
    })
  } else if (cmd === 'delete') {
    ElMessageBox.confirm('确认删除此任务？', '提示', { type: 'warning' }).then(() => {
      planApi.delete(plan.id).then(() => {
        ElMessage.success('已删除')
        loadPlans()
      })
    })
  }
}

async function savePlan() {
  await formRef.value?.validate()
  saving.value = true
  try {
    if (editingPlan.value) {
      await planApi.update(editingPlan.value.id, form as any)
      ElMessage.success('更新成功')
    } else {
      await planApi.create(form as any)
      ElMessage.success('创建成功')
    }
    showAddDialog.value = false
    editingPlan.value = null
    Object.assign(form, {
      title: '',
      description: '',
      planDate: selectedDate.value,
      priority: 'P2',
      category: 'WORK',
      estimatedMins: undefined
    })
    loadPlans()
  } finally {
    saving.value = false
  }
}

onMounted(loadPlans)
</script>

<style lang="scss" scoped>
.plan-view {
  .stats-bar {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 12px 16px;
    background: $bg-card;
    border: 1px solid $border;
    border-radius: $radius-md;
    margin-bottom: 12px;

    .stat-pill {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 13px;

      .dot {
        width: 8px;
        height: 8px;
        border-radius: 50%;
      }

      .label {
        color: $text-secondary;
      }
      .value {
        font-weight: 600;
        color: $text-primary;
      }
    }

    .ml-auto {
      margin-left: auto;
    }
  }

  .filter-bar {
    margin-bottom: 16px;
  }

  .task-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .task-card {
    display: flex;
    align-items: flex-start;
    gap: 12px;
    padding: 14px 16px;
    transition: $transition-normal;

    &.done {
      opacity: 0.6;
      .task-title {
        text-decoration: line-through;
        color: $text-muted;
      }
    }

    &.cancelled {
      opacity: 0.4;
      .task-title {
        text-decoration: line-through;
      }
    }

    .task-check {
      padding-top: 2px;
      cursor: pointer;

      .check-circle {
        width: 20px;
        height: 20px;
        border-radius: 50%;
        border: 2px solid $text-muted;
        display: flex;
        align-items: center;
        justify-content: center;
        transition: $transition-fast;
        font-size: 12px;

        &.IN_PROGRESS {
          border-color: $warning;
          background: rgba($warning, 0.1);
        }

        &.DONE {
          border-color: $success;
          background: $success;
          color: white;
        }

        &.CANCELLED {
          border-color: $danger;
        }

        &:hover {
          border-color: $primary;
          background: $primary-50;
        }
      }
    }

    .task-content {
      flex: 1;
      min-width: 0;

      .task-title {
        font-size: 14px;
        font-weight: 500;
        color: $text-primary;
        margin-bottom: 4px;
        transition: $transition-fast;
      }

      .task-desc {
        font-size: 12px;
        color: $text-secondary;
        margin-bottom: 6px;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }

      .task-meta {
        display: flex;
        align-items: center;
        gap: 6px;
        flex-wrap: wrap;
      }
    }

    .task-actions {
      flex-shrink: 0;

      .action-trigger {
        color: $text-muted;
        cursor: pointer;
        padding: 4px;
        border-radius: $radius-sm;
        transition: $transition-fast;
        font-size: 16px;

        &:hover {
          color: $text-primary;
          background: rgba(255, 255, 255, 0.06);
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
    p {
      margin-bottom: 16px;
    }
  }
}

:deep(.danger) {
  color: $danger !important;
}
</style>

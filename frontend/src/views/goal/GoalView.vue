<template>
  <div class="goal-view">
    <div class="page-header">
      <div>
        <h1 class="page-title">目标管理</h1>
        <p class="page-subtitle">追踪目标，实现人生蓝图</p>
      </div>
      <el-button type="primary" size="small" :icon="Plus" @click="openAdd">新增目标</el-button>
    </div>

    <!-- 类型标签 -->
    <el-tabs v-model="activeType" @tab-change="loadGoals">
      <el-tab-pane label="全部" name="" />
      <el-tab-pane label="🌟 五年" name="FIVE_YEAR" />
      <el-tab-pane label="📅 年度" name="YEARLY" />
      <el-tab-pane label="📆 月度" name="MONTHLY" />
      <el-tab-pane label="🗓️ 周计划" name="WEEKLY" />
    </el-tabs>

    <!-- 目标列表 -->
    <div v-loading="loading" class="goal-list">
      <div v-for="goal in goals" :key="goal.id" class="goal-card card card--glow">
        <!-- 头部 -->
        <div class="goal-header">
          <div class="goal-title-area">
            <span class="goal-type-badge">{{ typeLabel(goal.goalType) }}</span>
            <h3 class="goal-title">{{ goal.title }}</h3>
          </div>
          <div class="goal-status-area">
            <span class="goal-status" :class="goal.status.toLowerCase()">{{
              statusLabel(goal.status)
            }}</span>
            <el-dropdown trigger="click" @command="(cmd: string) => handleCommand(cmd, goal)">
              <el-icon class="icon-btn"><MoreFilled /></el-icon>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="edit">编辑</el-dropdown-item>
                  <el-dropdown-item command="delete" class="danger">删除</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>

        <!-- 描述 -->
        <p v-if="goal.description" class="goal-desc text-secondary text-sm">
          {{ goal.description }}
        </p>

        <!-- 进度条 -->
        <div class="goal-progress">
          <div class="progress-header">
            <span class="text-sm text-secondary">完成进度</span>
            <span class="text-sm font-bold" :style="{ color: progressColor(goal.progress) }"
              >{{ goal.progress }}%</span
            >
          </div>
          <el-slider
            v-model="goal.progress"
            :min="0"
            :max="100"
            :step="5"
            @change="(v: number) => updateProgress(goal.id, v)"
            :color="progressColor(goal.progress)"
          />
        </div>

        <!-- 时间线 -->
        <div class="goal-timeline text-xs text-muted">
          <span>📅 {{ goal.startDate }}</span>
          <span> → </span>
          <span>🏁 {{ goal.endDate }}</span>
        </div>
      </div>

      <div v-if="!loading && goals.length === 0" class="empty-state">
        <div class="empty-icon">🎯</div>
        <p>还没有目标，设定你的第一个目标</p>
        <el-button type="primary" size="small" @click="openAdd">添加目标</el-button>
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="showDialog"
      :title="editing ? '编辑目标' : '新增目标'"
      width="560px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="目标标题" prop="title">
          <el-input v-model="form.title" placeholder="输入目标标题" />
        </el-form-item>
        <el-form-item label="目标类型" prop="goalType">
          <el-select v-model="form.goalType" style="width: 100%">
            <el-option label="五年目标" value="FIVE_YEAR" />
            <el-option label="年度目标" value="YEARLY" />
            <el-option label="月度目标" value="MONTHLY" />
            <el-option label="周计划" value="WEEKLY" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" />
        </el-form-item>
        <div class="flex gap-md">
          <el-form-item label="开始日期" prop="startDate" style="flex: 1">
            <el-date-picker
              v-model="form.startDate"
              type="date"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
            />
          </el-form-item>
          <el-form-item label="截止日期" prop="endDate" style="flex: 1">
            <el-date-picker
              v-model="form.endDate"
              type="date"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
            />
          </el-form-item>
        </div>
        <el-form-item label="初始进度">
          <el-slider v-model="form.progress" :min="0" :max="100" :step="5" show-input />
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { Plus, MoreFilled } from '@element-plus/icons-vue'
import { goalApi } from '@/api/goal'
import type { GoalItem } from '@/api/goal'
import dayjs from 'dayjs'

const goals = ref<GoalItem[]>([])
const activeType = ref('')
const loading = ref(false)
const saving = ref(false)
const showDialog = ref(false)
const editing = ref<GoalItem | null>(null)
const formRef = ref<FormInstance>()

const form = reactive({
  title: '',
  description: '',
  goalType: 'YEARLY',
  startDate: dayjs().startOf('year').format('YYYY-MM-DD'),
  endDate: dayjs().endOf('year').format('YYYY-MM-DD'),
  progress: 0
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  goalType: [{ required: true, message: '请选择类型' }],
  startDate: [{ required: true, message: '请选择开始日期' }],
  endDate: [{ required: true, message: '请选择截止日期' }]
}

const typeMap: Record<string, string> = {
  FIVE_YEAR: '五年',
  YEARLY: '年度',
  MONTHLY: '月度',
  WEEKLY: '周'
}
const statusMap: Record<string, string> = {
  NOT_STARTED: '未开始',
  IN_PROGRESS: '进行中',
  COMPLETED: '已完成',
  ABANDONED: '已放弃'
}

function typeLabel(t: string) {
  return typeMap[t] ?? t
}
function statusLabel(s: string) {
  return statusMap[s] ?? s
}
function progressColor(p: number) {
  if (p >= 80) return '#10b981'
  if (p >= 50) return '#6366f1'
  if (p >= 20) return '#f59e0b'
  return '#64748b'
}

async function loadGoals() {
  loading.value = true
  try {
    goals.value = await goalApi.list({ goalType: activeType.value || undefined })
  } finally {
    loading.value = false
  }
}

async function updateProgress(id: number, progress: number) {
  await goalApi.updateProgress(id, progress)
}

function openAdd() {
  editing.value = null
  Object.assign(form, {
    title: '',
    description: '',
    goalType: 'YEARLY',
    startDate: dayjs().startOf('year').format('YYYY-MM-DD'),
    endDate: dayjs().endOf('year').format('YYYY-MM-DD'),
    progress: 0
  })
  showDialog.value = true
}

function handleCommand(cmd: string, goal: GoalItem) {
  if (cmd === 'edit') {
    editing.value = goal
    Object.assign(form, {
      title: goal.title,
      description: goal.description ?? '',
      goalType: goal.goalType,
      startDate: goal.startDate,
      endDate: goal.endDate,
      progress: goal.progress
    })
    showDialog.value = true
  } else if (cmd === 'delete') {
    ElMessageBox.confirm('确认删除此目标？', '提示', { type: 'warning' }).then(() => {
      goalApi.delete(goal.id).then(() => {
        ElMessage.success('已删除')
        loadGoals()
      })
    })
  }
}

async function save() {
  await formRef.value?.validate()
  saving.value = true
  try {
    if (editing.value) {
      await goalApi.update(editing.value.id, form as any)
    } else {
      await goalApi.create(form as any)
    }
    ElMessage.success('保存成功')
    showDialog.value = false
    loadGoals()
  } finally {
    saving.value = false
  }
}

onMounted(loadGoals)
</script>

<style lang="scss" scoped>
.goal-view {
  .goal-list {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
    gap: 16px;
    margin-top: 16px;
  }

  .goal-card {
    .goal-header {
      display: flex;
      align-items: flex-start;
      justify-content: space-between;
      margin-bottom: 8px;

      .goal-title-area {
        flex: 1;
        min-width: 0;

        .goal-type-badge {
          font-size: 11px;
          padding: 2px 8px;
          border-radius: $radius-full;
          background: $primary-100;
          color: $primary-light;
          display: inline-block;
          margin-bottom: 6px;
        }

        .goal-title {
          font-size: 15px;
          font-weight: 600;
          color: $text-primary;
        }
      }

      .goal-status-area {
        display: flex;
        align-items: center;
        gap: 8px;
        flex-shrink: 0;

        .goal-status {
          font-size: 11px;
          padding: 2px 8px;
          border-radius: $radius-full;

          &.not_started {
            background: rgba(100, 116, 139, 0.15);
            color: $text-muted;
          }
          &.in_progress {
            background: rgba(245, 158, 11, 0.15);
            color: $warning;
          }
          &.completed {
            background: rgba(16, 185, 129, 0.15);
            color: $success;
          }
          &.abandoned {
            background: rgba(239, 68, 68, 0.15);
            color: $danger;
          }
        }

        .icon-btn {
          color: $text-muted;
          cursor: pointer;
          font-size: 16px;
          padding: 4px;
          border-radius: $radius-sm;
          transition: $transition-fast;

          &:hover {
            color: $text-primary;
            background: rgba(255, 255, 255, 0.06);
          }
        }
      }
    }

    .goal-desc {
      margin-bottom: 12px;
      line-height: 1.5;
    }

    .goal-progress {
      margin-bottom: 10px;

      .progress-header {
        display: flex;
        justify-content: space-between;
        margin-bottom: 4px;
      }
    }

    .goal-timeline {
      display: flex;
      align-items: center;
      gap: 0;
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

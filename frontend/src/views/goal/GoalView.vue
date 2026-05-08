<template>
  <div class="goal-view">
    <div class="page-header">
      <div>
        <h1 class="page-title">目标管理</h1>
        <p class="page-subtitle">追踪目标，实现人生蓝图</p>
      </div>
      <el-button type="primary" size="small" :icon="Plus" @click="openAdd">新增目标</el-button>
    </div>

    <div class="stats-bar mb-md">
      <div class="stat-pill">
        <span class="label">总目标数</span>
        <span class="value">{{ stats.totalCount ?? 0 }}</span>
      </div>
      <div class="stat-pill">
        <span class="label">进行中</span>
        <span class="value text-warning">{{ stats.activeCount ?? 0 }}</span>
      </div>
      <div class="stat-pill">
        <span class="label">已完成</span>
        <span class="value text-success">{{ stats.completedCount ?? 0 }}</span>
      </div>
      <div class="stat-pill ml-auto">
        <span class="label">平均进度</span>
        <span class="value text-primary">{{ stats.avgProgress ?? 0 }}%</span>
      </div>
    </div>

    <!-- 类型与视图控制 -->
    <div class="flex items-center justify-between mb-md">
      <el-tabs v-model="activeType" @tab-change="loadGoals" style="margin-bottom: 0;">
        <el-tab-pane label="全部" name="" />
        <el-tab-pane label="🌟 五年" name="FIVE_YEAR" />
        <el-tab-pane label="📅 年度" name="YEARLY" />
        <el-tab-pane label="📆 月度" name="MONTHLY" />
        <el-tab-pane label="🗓️ 周计划" name="WEEKLY" />
      </el-tabs>

      <el-radio-group v-model="viewMode" size="small" @change="loadGoals">
        <el-radio-button label="card">卡片视图</el-radio-button>
        <el-radio-button label="tree">树形视图</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 目标列表 -->
    <div v-loading="loading" element-loading-background="transparent" class="goal-content w-full">
      
      <!-- 卡片视图分组显示 -->
      <div v-if="viewMode === 'card'" class="w-full">
        <div v-for="group in groupedCardList" :key="group.name" class="goal-group mb-lg">
          <div v-if="group.name !== '全部目标'" class="group-header mb-md flex items-center">
            <h3 class="text-lg font-bold" style="color: var(--el-color-primary);">📂 {{ group.name }}</h3>
            <span class="text-muted text-sm ml-sm">({{ group.items.length }})</span>
          </div>
          <div class="goal-list">
            <div v-for="goal in group.items" :key="goal.id" class="goal-card card card--glow">
        <!-- 头部 -->
        <div class="goal-header">
          <div class="goal-title-area">
            <span class="goal-type-badge">{{ typeLabel(goal.goalType) }}</span>
            <h3 class="goal-title">{{ goal.title }}</h3>
          </div>
          <div v-if="goal.parentId" class="parent-info text-xs text-muted">
            属于父目标: {{ getParentTitle(goal.parentId) }}
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
            @change="(v: any) => updateProgress(goal.id, v as number)"
            :color="progressColor(goal.progress)"
          />
        </div>

        <!-- 关键结果 OKR -->
        <div v-if="goal.keyResults && goal.keyResults.length > 0" class="goal-krs">
          <div class="kr-header text-xs text-secondary mb-xs">关键结果 (KR)</div>
          <div v-for="kr in goal.keyResults" :key="kr.id" class="kr-item">
            <div class="flex justify-between text-xs mb-1">
              <span>{{ kr.title }}</span>
              <span>{{ kr.currentValue }}/{{ kr.targetValue }} {{ kr.unit }}</span>
            </div>
            <el-progress :percentage="kr.progress" :show-text="false" :stroke-width="4" />
          </div>
        </div>

        <!-- 时间线 -->
        <div class="goal-timeline text-xs text-muted">
          <span>📅 {{ goal.startDate }}</span>
          <span> → </span>
          <span>🏁 {{ goal.endDate }}</span>
        </div>
        </div>
      </div>
    </div>
  </div>

  <!-- 树形视图 -->
      <el-table
        v-if="viewMode === 'tree'"
        :data="goals"
        style="width: 100%; border-radius: 8px;"
        row-key="id"
        border
        default-expand-all
      >
        <el-table-column prop="title" label="目标名称" min-width="240">
          <template #default="{ row }">
            <span class="font-bold">{{ row.title }}</span>
            <span class="text-xs text-muted ml-sm" v-if="row.description">{{ row.description }}</span>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <span class="goal-type-badge text-xs px-2 py-1 bg-primary-100 text-primary rounded-full">{{ typeLabel(row.goalType) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="进度" width="200">
          <template #default="{ row }">
            <div class="flex items-center gap-sm">
              <el-progress :percentage="row.progress" :color="progressColor(row.progress)" style="flex: 1" />
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <span class="goal-status text-xs px-2 py-1 rounded-full" :class="row.status.toLowerCase()">{{ statusLabel(row.status) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleCommand('edit', row)">编辑</el-button>
            <el-button link type="danger" @click="handleCommand('delete', row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

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
      width="780px"
      destroy-on-close
      class="goal-dialog"
      top="5vh"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" label-position="left">
        <div class="form-section">
          <el-form-item label="目标标题" prop="title">
            <el-input v-model="form.title" placeholder="例如：掌握前端进阶技术" />
          </el-form-item>
          
          <div class="form-row">
            <el-form-item label="目标类型" prop="goalType">
              <el-select v-model="form.goalType" style="width: 100%">
                <el-option label="五年目标" value="FIVE_YEAR" />
                <el-option label="年度目标" value="YEARLY" />
                <el-option label="月度目标" value="MONTHLY" />
                <el-option label="周计划" value="WEEKLY" />
              </el-select>
            </el-form-item>
            <el-form-item label="父级目标">
              <el-select v-model="form.parentId" clearable placeholder="可选" style="width: 100%">
                <el-option
                  v-for="g in allGoals"
                  :key="g.id"
                  :label="`[${typeLabel(g.goalType)}] ${g.title}`"
                  :value="g.id"
                  :disabled="editing?.id === g.id"
                />
              </el-select>
            </el-form-item>
          </div>

          <el-form-item label="目标描述">
            <el-input v-model="form.description" type="textarea" :rows="2" placeholder="目标的具体背景或意义..." />
          </el-form-item>

          <div class="form-row">
            <el-form-item label="开始日期" prop="startDate">
              <el-date-picker
                v-model="form.startDate"
                type="date"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="截止日期" prop="endDate">
              <el-date-picker
                v-model="form.endDate"
                type="date"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </div>

          <el-form-item label="初始进度">
            <div class="flex items-center gap-md w-full">
              <el-slider v-model="form.progress" :min="0" :max="100" :step="5" style="flex: 1" />
              <el-input-number v-model="form.progress" :min="0" :max="100" :step="5" controls-position="right" style="width: 100px" />
            </div>
          </el-form-item>
        </div>

        <!-- 关键结果管理 -->
        <div class="kr-section">
          <div class="section-header">
            <span class="title">关键结果 (OKR KRs)</span>
            <el-button type="primary" link :icon="Plus" class="add-kr-btn" @click="addKr">添加 KR</el-button>
          </div>
          
          <div v-if="form.keyResults.length === 0" class="kr-empty">
            暂无关键结果，点击右上方添加
          </div>
          
          <div class="kr-list-container">
            <div v-for="(kr, idx) in form.keyResults" :key="idx" class="kr-form-card">
              <div class="kr-card-header">
                <el-input v-model="kr.title" placeholder="KR 描述 (例如：完成 3 个实战项目)" class="kr-title-input" />
                <el-button type="danger" link :icon="Delete" class="delete-kr-btn" @click="removeKr(idx)" />
              </div>
              <div class="kr-card-body">
                <div class="kr-value-row">
                  <div class="input-group">
                    <span class="label">当前值</span>
                    <el-input-number v-model="kr.currentValue" :precision="0" controls-position="right" />
                  </div>
                  <div class="input-group">
                    <span class="label">目标值</span>
                    <el-input-number v-model="kr.targetValue" :precision="0" controls-position="right" />
                  </div>
                  <div class="input-group">
                    <span class="label">单位</span>
                    <el-input v-model="kr.unit" placeholder="单位" />
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </el-form>



      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { Plus, MoreFilled, Delete } from '@element-plus/icons-vue'
import { goalApi } from '@/api/goal'
import type { GoalItem, GoalKr } from '@/api/goal'
import dayjs from 'dayjs'

const goals = ref<GoalItem[]>([])
const allGoals = ref<GoalItem[]>([])
const activeType = ref('')
const viewMode = ref<'card' | 'tree'>('card')

const groupedCardList = computed(() => {
  if (viewMode.value !== 'card') return []

  const groups: Record<string, GoalItem[]> = {}

  const getGroupPath = (goal: GoalItem) => {
    if (!activeType.value || activeType.value === 'FIVE_YEAR') {
      return '全部目标'
    }

    let path = []
    let currentId = goal.parentId
    let depth = 0
    while (currentId && depth < 5) {
      const parent = allGoals.value.find(g => g.id === currentId)
      if (parent) {
        path.unshift(parent.title)
        currentId = parent.parentId
      } else {
        break
      }
      depth++
    }

    if (path.length === 0) {
      return '独立目标'
    }
    return path.join(' > ')
  }

  goals.value.forEach(goal => {
    const path = getGroupPath(goal)
    if (!groups[path]) {
      groups[path] = []
    }
    groups[path].push(goal)
  })

  return Object.keys(groups)
    .sort((a, b) => {
      if (a === '全部目标') return -1;
      if (a === '独立目标') return 1;
      if (b === '独立目标') return -1;
      return a.localeCompare(b);
    })
    .map(key => ({
      name: key,
      items: groups[key].sort((a, b) => dayjs(a.startDate).valueOf() - dayjs(b.startDate).valueOf())
    }))
})

const stats = ref<Record<string, any>>({})
const loading = ref(false)
const saving = ref(false)
const showDialog = ref(false)
const editing = ref<GoalItem | null>(null)
const formRef = ref<FormInstance>()

const form = reactive({
  parentId: null as number | null,
  title: '',
  description: '',
  goalType: 'YEARLY',
  startDate: dayjs().startOf('year').format('YYYY-MM-DD'),
  endDate: dayjs().endOf('year').format('YYYY-MM-DD'),
  progress: 0,
  keyResults: [] as GoalKr[]
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
    if (viewMode.value === 'tree') {
      goals.value = await goalApi.getTree(activeType.value || undefined)
    } else {
      goals.value = await goalApi.list({ goalType: activeType.value || undefined })
    }
    
    // 同时加载全量列表用于父级选择
    allGoals.value = await goalApi.list({})
    
    // 加载统计信息
    stats.value = await goalApi.getStatistics()
  } finally {
    loading.value = false
  }
}

function getParentTitle(parentId: number) {
  return allGoals.value.find(g => g.id === parentId)?.title || '未知'
}

function addKr() {
  form.keyResults.push({
    title: '',
    targetValue: 100,
    currentValue: 0,
    unit: '%'
  })
}

function removeKr(index: number) {
  form.keyResults.splice(index, 1)
}

async function updateProgress(id: number, progress: number) {
  await goalApi.updateProgress(id, progress)
}

function openAdd() {
  editing.value = null
  Object.assign(form, {
    parentId: null,
    title: '',
    description: '',
    goalType: 'YEARLY',
    startDate: dayjs().startOf('year').format('YYYY-MM-DD'),
    endDate: dayjs().endOf('year').format('YYYY-MM-DD'),
    progress: 0,
    keyResults: []
  })
  showDialog.value = true
}

function handleCommand(cmd: string, goal: GoalItem) {
  if (cmd === 'edit') {
    editing.value = goal
    Object.assign(form, {
      parentId: goal.parentId,
      title: goal.title,
      description: goal.description ?? '',
      goalType: goal.goalType,
      startDate: goal.startDate,
      endDate: goal.endDate,
      progress: goal.progress,
      keyResults: goal.keyResults ? JSON.parse(JSON.stringify(goal.keyResults)) : []
    })
    showDialog.value = true
  } else if (cmd === 'delete') {
    ElMessageBox.confirm('确认删除此目标？', '提示', { 
      type: 'warning',
      customClass: 'delete-confirm-box'
    }).then(() => {
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
  .stats-bar {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 12px 16px;
    background: $bg-card;
    border: 1px solid $border;
    border-radius: $radius-md;

    .stat-pill {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 13px;

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

  .goal-list {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
    gap: 16px;
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

      .parent-info {
        margin-top: 4px;
        font-size: 11px;
        color: $text-muted;
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
          font-size: 18px;
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

    .goal-krs {
      margin-top: 12px;
      padding: 10px;
      background: rgba(255, 255, 255, 0.03);
      border-radius: $radius-sm;
      .kr-item {
        margin-bottom: 8px;
        &:last-child { margin-bottom: 0; }
      }
    }

    .goal-timeline {
      display: flex;
      justify-content: space-between;
      border-top: 1px solid rgba(255, 255, 255, 0.05);
      padding-top: 10px;
      margin-top: auto;
    }
  }

  // 目标弹窗深度优化
  .goal-dialog {
    border-radius: 16px !important;
    overflow: hidden;
    display: flex;
    flex-direction: column;
    margin-bottom: 0 !important;
    box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25) !important;
    max-height: 85vh;
    margin-top: 5vh !important;

    :deep(.el-dialog__header) {
      margin-right: 0;
      padding: 20px 24px;
      border-bottom: 1px solid rgba($border, 0.6);
      
      .el-dialog__title {
        font-weight: 700;
        font-size: 18px;
      }
    }

    :deep(.el-dialog__body) {
      padding: 24px;
      flex: 1;
      overflow-y: auto;
      background: #fcfcfd;

      /* 滚动条 */
      &::-webkit-scrollbar {
        width: 6px;
      }
      &::-webkit-scrollbar-thumb {
        background: rgba($text-muted, 0.2);
        border-radius: 10px;
      }
    }

    :deep(.el-dialog__footer) {
      padding: 16px 24px;
      border-top: 1px solid rgba($border, 0.6);
      background: #fff;
    }

    .form-section {
      margin-bottom: 28px;
    }

    .form-row {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 24px;
    }

    :deep(.el-form-item) {
      margin-bottom: 20px;
      
      .el-form-item__label {
        font-size: 13px;
        font-weight: 600;
        color: #475569;
      }
    }

    .kr-section {
      .section-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 16px;
        padding-bottom: 8px;
        border-bottom: 2px solid rgba($primary, 0.1);

        .title {
          font-size: 15px;
          font-weight: 700;
          color: $text-primary;
          display: flex;
          align-items: center;
          gap: 8px;

          &::before {
            content: '';
            width: 4px;
            height: 16px;
            background: $primary;
            border-radius: 2px;
          }
        }

        .add-kr-btn {
          height: 32px;
          padding: 0 16px;
          background: $primary !important;
          color: #fff !important;
          font-weight: 600;
          border-radius: 8px;
          font-size: 13px;
          box-shadow: 0 4px 10px rgba($primary, 0.2);
          
          &:hover {
            background: $primary-light !important;
            transform: translateY(-1px);
            box-shadow: 0 6px 15px rgba($primary, 0.3);
          }
        }
      }

      .kr-empty {
        text-align: center;
        padding: 24px;
        color: $text-muted;
        font-size: 13px;
        background: rgba(0, 0, 0, 0.05);
        border-radius: 8px;
      }

      .kr-list-container {
        padding: 4px;
        max-height: 450px;
        overflow-y: auto;
        margin-right: -10px;
        padding-right: 10px;

        /* 内部滚动条样式 */
        &::-webkit-scrollbar {
          width: 4px;
        }
        &::-webkit-scrollbar-thumb {
          background: rgba($text-muted, 0.15);
          border-radius: 10px;
        }
      }


      .kr-form-card {
        background: #fff;
        border: 1px solid $border;
        border-radius: 12px;
        padding: 16px;
        margin-bottom: 16px;
        box-shadow: 0 2px 4px rgba(0,0,0,0.02);
        transition: all 0.2s ease;

        &:hover {
          border-color: $primary-light;
          box-shadow: 0 8px 20px rgba(0, 0, 0, 0.06);
        }

        .kr-card-header {
          display: flex;
          gap: 12px;
          align-items: flex-start;
          margin-bottom: 16px;

          .kr-title-input {
            flex: 1;
            :deep(.el-input__wrapper) {
              box-shadow: none !important;
              background: #f8fafc !important;
              border: 1px solid #e2e8f0;
              padding: 4px 12px;
              border-radius: 8px;
              
              .el-input__inner {
                font-weight: 600;
                font-size: 14px;
                color: $text-primary;
              }
            }
          }
          
          .delete-kr-btn {
            padding: 8px;
            font-size: 16px;
            color: #94a3b8;
            &:hover { color: $danger; }
          }
        }

        .kr-card-body {
          .kr-value-row {
            display: grid;
            grid-template-columns: 1fr 1fr 100px;
            gap: 16px;

            .input-group {
              .label {
                display: block;
                font-size: 11px;
                color: #64748b;
                font-weight: 700;
                margin-bottom: 6px;
                text-transform: uppercase;
              }

              :deep(.el-input-number), :deep(.el-input) {
                width: 100%;
                .el-input__wrapper {
                  border-radius: 8px;
                }
              }
            }
          }
        }
      }
    }
  }

  // 暗色模式优化
  &.dark-mode, :global(html.dark) & {
    .goal-dialog {
      :deep(.el-dialog__body) {
        background: #1e293b;
      }
      :deep(.el-dialog__footer) {
        background: #1e293b;
      }
      .kr-form-card {
        background: #0f172a;
        border-color: rgba(255,255,255,0.1);
      }
      .kr-title-input :deep(.el-input__wrapper) {
        background: #1e293b !important;
        border-color: rgba(255,255,255,0.1) !important;
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

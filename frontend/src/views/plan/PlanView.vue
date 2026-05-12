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
        <el-button type="info" size="small" :icon="Document" @click="openTemplateDialog">从模板添加</el-button>
        <el-button type="primary" size="small" :icon="Plus" @click="showAddDialog = true">新增任务</el-button>
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
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="任务列表" name="list">
          <el-radio-group v-model="filterCategory" size="small" @change="filterPlans">
            <el-radio-button label="">全部</el-radio-button>
            <el-radio-button v-for="cat in categories" :key="cat.value" :label="cat.value">{{
              cat.label
            }}</el-radio-button>
          </el-radio-group>
        </el-tab-pane>
        <el-tab-pane label="数据统计" name="analytics">
          <!-- 数据统计组件 -->
          <PlanAnalytics />
        </el-tab-pane>
        <el-tab-pane label="时间块" name="timeblock">
          <!-- 时间块视图 -->
          <TimeBlockView :plan-date="selectedDate" />
        </el-tab-pane>
        <el-tab-pane label="日历" name="calendar">
          <!-- 日历视图 -->
          <CalendarView @select-date="handleCalendarDateSelect" />
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 批量操作工具栏 -->
    <div v-if="activeTab === 'list' && selectedPlans.length > 0" class="batch-toolbar">
      <span class="selected-count">已选择 {{ selectedPlans.length }} 个任务</span>
      <div class="batch-actions">
        <el-button size="small" @click="handleBatchComplete">完成</el-button>
        <el-button size="small" @click="handleBatchPostpone">顺延</el-button>
        <el-dropdown trigger="click" @command="handleBatchUpdate">
          <el-button size="small">
            批量修改 <el-icon class="el-icon--right"><ArrowRight /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="priority">修改优先级</el-dropdown-item>
              <el-dropdown-item command="category">修改分类</el-dropdown-item>
              <el-dropdown-item command="status">修改状态</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <el-button size="small" type="danger" @click="handleBatchDelete">删除</el-button>
        <el-button size="small" @click="clearSelection">取消选择</el-button>
      </div>
    </div>

    <!-- 任务列表 -->
    <div v-if="activeTab === 'list'" v-loading="loading" class="task-list" ref="taskListRef">
      <div
        v-for="(plan, idx) in filteredPlans"
        :key="plan.id"
        :data-task-id="plan.id"
        class="task-card card animate-fade-in-up"
        :class="{
          done: plan.status === 'DONE',
          cancelled: plan.status === 'CANCELLED',
          selected: selectedTaskIndex === idx
        }"
        :style="{ animationDelay: idx * 0.04 + 's' }"
      >
        <!-- 左侧：选择框和完成复选框 -->
        <div class="task-left">
          <el-checkbox
            v-model="selectedPlans"
            :value="plan.id"
            @change="handleSelectionChange"
            @click.stop
          />
          <div class="task-check" @click="toggleStatus(plan)">
            <div class="check-circle" :class="plan.status">
              <el-icon v-if="plan.status === 'DONE'"><Check /></el-icon>
            </div>
          </div>
        </div>

        <!-- 内容 -->
        <div class="task-content">
          <div class="task-title">
            {{ plan.title }}
            <el-icon
              v-if="plan.subtaskCount && plan.subtaskCount > 0"
              class="expand-icon"
              :class="{ expanded: expandedTasks.includes(plan.id) }"
              @click="toggleExpand(plan.id)"
            >
              <ArrowRight />
            </el-icon>
          </div>
          <div v-if="plan.description" class="task-desc">{{ plan.description }}</div>
          <!-- 子任务进度条 -->
          <div v-if="plan.subtaskCount && plan.subtaskCount > 0" class="subtask-progress">
            <div class="progress-bar">
              <div
                class="progress-fill"
                :style="{ width: subtaskProgress(plan) + '%' }"
              ></div>
            </div>
            <span class="progress-text">{{ plan.completedSubtaskCount || 0 }}/{{ plan.subtaskCount }}</span>
          </div>
          <div class="task-meta">
            <span v-if="plan.repeatType && plan.repeatType !== 'NONE'" class="repeat-badge" :title="`重复: ${repeatTypeLabel(plan.repeatType)}`">
              🔄
            </span>
            <span class="priority-badge" :class="plan.priority.toLowerCase()">{{
              plan.priority
            }}</span>
            <span class="tag">{{ categoryLabel(plan.category) }}</span>
            <span v-if="plan.estimatedMins" class="text-muted text-xs"
              >⏱ {{ plan.estimatedMins }}min</span
            >
            <div v-if="plan.tags && plan.tags.length > 0" class="task-tags">
              <span
                v-for="tag in plan.tags"
                :key="tag.id"
                class="task-tag"
                :style="{ background: `${tag.color}20`, color: tag.color }"
              >
                {{ tag.name }}
              </span>
            </div>
          </div>
        </div>

        <!-- 右侧操作 -->
        <div class="task-actions">
          <el-dropdown trigger="click" @command="(cmd: string) => handleCommand(cmd, plan)">
            <el-icon class="action-trigger"><MoreFilled /></el-icon>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="edit">编辑</el-dropdown-item>
                <el-dropdown-item v-if="!plan.parentId" command="addSubtask">添加子任务</el-dropdown-item>
                <el-dropdown-item command="postpone">顺延至明天</el-dropdown-item>
                <el-dropdown-item v-if="plan.status !== 'IN_PROGRESS'" command="inprogress">标记进行中</el-dropdown-item>
                <el-dropdown-item command="saveTemplate">保存为模板</el-dropdown-item>
                <el-dropdown-item v-if="plan.status !== 'CANCELLED'" command="cancel">取消</el-dropdown-item>
                <el-dropdown-item command="delete" divided class="danger">删除</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>

        <!-- 子任务列表 -->
        <div v-if="expandedTasks.includes(plan.id)" class="subtask-list">
          <div
            v-for="subtask in getSubtasksForPlan(plan.id)"
            :key="subtask.id"
            class="subtask-item"
            :class="{ done: subtask.status === 'DONE' }"
          >
            <div class="subtask-check" @click="toggleSubtaskStatus(subtask)">
              <div class="check-circle" :class="subtask.status">
                <el-icon v-if="subtask.status === 'DONE'"><Check /></el-icon>
              </div>
            </div>
            <span class="subtask-title">{{ subtask.title }}</span>
            <el-icon class="subtask-delete" @click="deleteSubtask(subtask)"><Delete /></el-icon>
          </div>
          <div v-if="getSubtasksForPlan(plan.id).length === 0" class="subtask-empty">
            暂无子任务
          </div>
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
      width="480px"
      destroy-on-close
      class="premium-dialog"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px" label-position="left">
        <div class="form-section">
          <el-form-item label="任务标题" prop="title">
            <el-input v-model="form.title" placeholder="要做点什么呢？" />
          </el-form-item>
          
          <el-form-item label="任务描述">
            <el-input v-model="form.description" type="textarea" :rows="2" placeholder="补充详细描述（可选）..." />
          </el-form-item>

          <el-form-item label="计划日期" prop="planDate">
            <el-date-picker
              v-model="form.planDate"
              type="date"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              style="width: 100%"
            />
          </el-form-item>

          <el-form-item label="优先级">
            <el-select v-model="form.priority" style="width: 100%">
              <el-option v-for="p in priorities" :key="p.value" :label="p.label" :value="p.value" />
            </el-select>
          </el-form-item>

          <el-form-item label="所属分类">
            <el-select v-model="form.category" style="width: 100%">
              <el-option v-for="c in categories" :key="c.value" :label="c.label" :value="c.value" />
            </el-select>
          </el-form-item>

          <el-form-item label="预估用时">
            <el-input-number v-model="form.estimatedMinutes" :min="0" :step="15" controls-position="right" style="width: 120px" />
            <span class="ml-sm text-muted">分钟</span>
          </el-form-item>

          <!-- 重复设置 -->
          <el-form-item label="重复">
            <el-select v-model="form.repeatType" style="width: 100%" placeholder="不重复" @change="handleRepeatTypeChange">
              <el-option label="不重复" value="" />
              <el-option label="每天" value="DAILY" />
              <el-option label="每周" value="WEEKLY" />
              <el-option label="每月" value="MONTHLY" />
            </el-select>
          </el-form-item>

          <el-form-item v-if="form.repeatType === 'WEEKLY'" label="重复于">
            <el-checkbox-group v-model="weeklyDays">
              <el-checkbox-button label="1">一</el-checkbox-button>
              <el-checkbox-button label="2">二</el-checkbox-button>
              <el-checkbox-button label="3">三</el-checkbox-button>
              <el-checkbox-button label="4">四</el-checkbox-button>
              <el-checkbox-button label="5">五</el-checkbox-button>
              <el-checkbox-button label="6">六</el-checkbox-button>
              <el-checkbox-button label="7">日</el-checkbox-button>
            </el-checkbox-group>
          </el-form-item>

          <el-form-item v-if="form.repeatType === 'MONTHLY'" label="重复于">
            <el-checkbox-group v-model="monthlyDays">
              <el-checkbox-button label="1">1号</el-checkbox-button>
              <el-checkbox-button label="15">15号</el-checkbox-button>
              <el-checkbox-button label="28">28号</el-checkbox-button>
            </el-checkbox-group>
          </el-form-item>

          <el-form-item v-if="form.repeatType" label="结束日期">
            <el-date-picker
              v-model="form.repeatEndDate"
              type="date"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              placeholder="选择结束日期（可选）"
              style="width: 100%"
            />
          </el-form-item>

          <!-- 提醒设置 -->
          <el-form-item label="提醒">
            <el-select v-model="form.reminderType" style="width: 100%" placeholder="不提醒" @change="handleReminderTypeChange">
              <el-option label="不提醒" value="" />
              <el-option label="任务开始时" value="START" />
              <el-option label="任务截止前" value="DUE" />
              <el-option label="自定义时间" value="CUSTOM" />
            </el-select>
          </el-form-item>

          <el-form-item v-if="form.reminderType === 'START' || form.reminderType === 'DUE'" label="提前提醒">
            <el-radio-group v-model="form.advanceMinutes">
              <el-radio-button :label="0">不提前</el-radio-button>
              <el-radio-button :label="5">5分钟</el-radio-button>
              <el-radio-button :label="15">15分钟</el-radio-button>
              <el-radio-button :label="30">30分钟</el-radio-button>
              <el-radio-button :label="60">1小时</el-radio-button>
            </el-radio-group>
          </el-form-item>

          <el-form-item v-if="form.reminderType === 'CUSTOM'" label="提醒时间">
            <el-date-picker
              v-model="form.reminderTime"
              type="datetime"
              format="YYYY-MM-DD HH:mm"
              value-format="YYYY-MM-DD HH:mm"
              placeholder="选择提醒时间"
              style="width: 100%"
            />
          </el-form-item>

          <!-- 标签选择 -->
          <el-form-item label="标签">
            <el-select
              v-model="form.tagIds"
              multiple
              filterable
              allow-create
              placeholder="选择或创建标签"
              style="width: 100%"
            >
              <el-option
                v-for="tag in allTags"
                :key="tag.id"
                :label="tag.name"
                :value="tag.id"
              >
                <span class="tag-option">
                  <span class="tag-dot" :style="{ background: tag.color }"></span>
                  <span>{{ tag.name }}</span>
                </span>
              </el-option>
            </el-select>
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="savePlan">确定</el-button>
      </template>
    </el-dialog>


    <!-- 模板选择弹窗 -->
    <el-dialog v-model="showTemplateDialog" title="从模板创建任务" width="560px" destroy-on-close>
      <div class="template-list">
        <div v-for="tpl in templates" :key="tpl.id" class="template-item card mb-sm cursor-pointer" @click="createFromTemplate(tpl)">
          <div class="font-bold mb-xs">{{ tpl.templateName }}</div>
          <div class="text-sm text-secondary">{{ tpl.title }}</div>
          <div class="flex gap-sm mt-xs">
            <span class="tag">{{ categoryLabel(tpl.category) }}</span>
            <span class="priority-badge" :class="tpl.priority.toLowerCase()">{{ tpl.priority }}</span>
            <span v-if="tpl.estimatedMins" class="text-xs text-muted">⏱ {{ tpl.estimatedMins }}m</span>
          </div>
        </div>
        <div v-if="templates.length === 0" class="text-center text-muted py-md">
          暂无模板，可将现有任务保存为模板
        </div>
      </div>
    </el-dialog>

    <!-- 命令面板 -->
    <CommandPalette
      v-model:visible="showCommandPalette"
      :tasks="filteredPlans"
      @executeCommand="handleCommandExecute"
      @selectTask="handleTaskSelectFromPalette"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { Plus, Check, MoreFilled, Document, ArrowRight, Delete } from '@element-plus/icons-vue'
import { planApi, reminderApi, batchApi, tagApi } from '@/api/plan'
import type { PlanItem } from '@/api/plan'
import type { TagItem } from '@/api/plan'
import dayjs from 'dayjs'
import Sortable from 'sortablejs'
import PlanAnalytics from './PlanAnalytics.vue'
import TimeBlockView from './TimeBlockView.vue'
import CalendarView from './CalendarView.vue'
import CommandPalette from '../../components/plan/CommandPalette.vue'
import { useKeyboardShortcuts } from '@/composables/useKeyboardShortcuts'

const selectedDate = ref(dayjs().format('YYYY-MM-DD'))
const filterCategory = ref('')
const activeTab = ref('list')
const plans = ref<PlanItem[]>([])
const templates = ref<PlanItem[]>([])
const stats = ref<Record<string, any>>({})
const loading = ref(false)
const saving = ref(false)
const showAddDialog = ref(false)
const showTemplateDialog = ref(false)
const editingPlan = ref<PlanItem | null>(null)
const formRef = ref<FormInstance>()
const taskListRef = ref<HTMLElement>()
const expandedTasks = ref<number[]>([])
const subtasksCache = ref<Record<number, PlanItem[]>>({})
const selectedPlans = ref<number[]>([])
const allTags = ref<TagItem[]>([])
const showCommandPalette = ref(false)
const selectedTaskIndex = ref(-1)
let sortableInstance: Sortable | null = null

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
  estimatedMins: undefined as number | undefined,
  repeatType: '',
  repeatPattern: '',
  repeatEndDate: '',
  reminderType: '',
  advanceMinutes: 0,
  reminderTime: '',
  tagIds: [] as number[]
})

const weeklyDays = ref<number[]>([])
const monthlyDays = ref<number[]>([])

const repeatTypes = [
  { value: '', label: '不重复' },
  { value: 'DAILY', label: '每天' },
  { value: 'WEEKLY', label: '每周' },
  { value: 'MONTHLY', label: '每月' }
]

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

function repeatTypeLabel(val: string) {
  return repeatTypes.find((t) => t.value === val)?.label ?? val
}

function handleRepeatTypeChange() {
  // 切换重复类型时清空选择
  weeklyDays.value = []
  monthlyDays.value = []
  form.repeatPattern = ''
}

async function loadTags() {
  try {
    allTags.value = await tagApi.list()
  } catch (error) {
    console.error('Failed to load tags', error)
  }
}

function buildRepeatPattern() {
  if (form.repeatType === 'WEEKLY' && weeklyDays.value.length > 0) {
    return JSON.stringify({ days: weeklyDays.value.map(Number) })
  } else if (form.repeatType === 'MONTHLY' && monthlyDays.value.length > 0) {
    return JSON.stringify({ days: monthlyDays.value.map(Number) })
  }
  return ''
}

function handleReminderTypeChange() {
  // 切换提醒类型时重置提醒时间
  form.advanceMinutes = 0
  form.reminderTime = ''
}

// 子任务相关函数
function subtaskProgress(plan: PlanItem) {
  if (!plan.subtaskCount || plan.subtaskCount === 0) return 0
  return Math.round(((plan.completedSubtaskCount || 0) / plan.subtaskCount) * 100)
}

async function toggleExpand(planId: number) {
  const index = expandedTasks.value.indexOf(planId)
  if (index > -1) {
    expandedTasks.value.splice(index, 1)
  } else {
    expandedTasks.value.push(planId)
    // 加载子任务
    if (!subtasksCache.value[planId]) {
      try {
        subtasksCache.value[planId] = await planApi.getSubtasks(planId)
      } catch (error) {
        console.error('Failed to load subtasks', error)
      }
    }
  }
}

function getSubtasksForPlan(planId: number) {
  return subtasksCache.value[planId] || []
}

async function toggleSubtaskStatus(subtask: PlanItem) {
  const next =
    subtask.status === 'TODO' ? 'DONE' : subtask.status === 'DONE' ? 'TODO' : 'DONE'

  try {
    await planApi.updateSubtaskStatus(subtask.id, next)
    subtask.status = next as any
    // 更新父任务进度
    const parentPlan = plans.value.find(p => p.id === subtask.parentId)
    if (parentPlan) {
      parentPlan.completedSubtaskCount = (parentPlan.completedSubtaskCount || 0) + (next === 'DONE' ? 1 : -1)
    }
  } catch (error) {
    console.error('Failed to update subtask status', error)
  }
}

async function deleteSubtask(subtask: PlanItem) {
  try {
    await planApi.delete(subtask.id)
    // 从缓存中移除
    if (subtask.parentId && subtasksCache.value[subtask.parentId]) {
      subtasksCache.value[subtask.parentId] = subtasksCache.value[subtask.parentId].filter(s => s.id !== subtask.id)
      // 更新父任务计数
      const parentPlan = plans.value.find(p => p.id === subtask.parentId)
      if (parentPlan) {
        parentPlan.subtaskCount = Math.max(0, (parentPlan.subtaskCount || 0) - 1)
        if (subtask.status === 'DONE') {
          parentPlan.completedSubtaskCount = Math.max(0, (parentPlan.completedSubtaskCount || 0) - 1)
        }
      }
    }
  } catch (error) {
    console.error('Failed to delete subtask', error)
  }
}

async function loadPlans() {
  loading.value = true
  try {
    ;[plans.value, stats.value] = await Promise.all([
      planApi.list(selectedDate.value),
      planApi.statistics(selectedDate.value)
    ])

    // 加载每个任务的标签
    for (const plan of plans.value) {
      try {
        const tags = await tagApi.getPlanTags(plan.id)
        ;(plan as any).tags = tags
      } catch (error) {
        console.error('Failed to load tags for plan', plan.id, error)
        ;(plan as any).tags = []
      }
    }

    await nextTick()
    initSortable()
  } finally {
    loading.value = false
  }
}

function initSortable() {
  if (!taskListRef.value) return
  if (sortableInstance) sortableInstance.destroy()
  sortableInstance = Sortable.create(taskListRef.value, {
    animation: 150,
    disabled: !!filterCategory.value, // 只在"全部"分类下允许拖拽
    onEnd: async (evt) => {
      const { oldIndex, newIndex } = evt
      if (oldIndex === undefined || newIndex === undefined || oldIndex === newIndex) return
      
      const movedItem = filteredPlans.value.splice(oldIndex, 1)[0]
      filteredPlans.value.splice(newIndex, 0, movedItem)
      
      const sortMap: Record<number, number> = {}
      filteredPlans.value.forEach((p, idx) => {
        sortMap[p.id] = idx
      })
      
      try {
        await planApi.batchSort(sortMap)
      } catch (error) {
        ElMessage.error('排序保存失败')
        loadPlans()
      }
    }
  })
}

async function toggleStatus(plan: PlanItem) {
  const next =
    plan.status === 'TODO' ? 'IN_PROGRESS' : plan.status === 'IN_PROGRESS' ? 'DONE' : 'TODO'
  await planApi.updateStatus(plan.id, next)
  plan.status = next as any
  stats.value = await planApi.statistics(selectedDate.value)
}

function filterPlans() {
  if (sortableInstance) {
    sortableInstance.option('disabled', !!filterCategory.value)
  }
}

function handleTabChange(tabName: string) {
  if (tabName === 'list') {
    // 切换到列表视图时重新初始化拖拽
    nextTick(initSortable)
  }
}

function handleCalendarDateSelect(date: string) {
  selectedDate.value = date
  activeTab.value = 'list'
  loadPlans()
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
  } else if (cmd === 'addSubtask') {
    ElMessageBox.prompt('请输入子任务标题', '添加子任务', {
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    }).then(({ value }) => {
      if (!value) return
      planApi.createSubtask(plan.id, {
        title: value,
        planDate: plan.planDate,
        category: plan.category
      }).then((subtask) => {
        // 添加到缓存
        if (!subtasksCache.value[plan.id]) {
          subtasksCache.value[plan.id] = []
        }
        subtasksCache.value[plan.id].push(subtask)
        // 更新父任务计数
        plan.subtaskCount = (plan.subtaskCount || 0) + 1
        // 自动展开
        if (!expandedTasks.value.includes(plan.id)) {
          toggleExpand(plan.id)
        }
        ElMessage.success('子任务创建成功')
      })
    })
  } else if (cmd === 'postpone') {
    planApi.postpone(plan.id).then(() => {
      ElMessage.success('已顺延至明天')
      loadPlans()
    })
  } else if (cmd === 'saveTemplate') {
    ElMessageBox.prompt('请输入模板名称', '保存为模板', {
      inputValue: plan.title,
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    }).then(({ value }) => {
      if (!value) return
      planApi.saveAsTemplate(plan.id, value).then(() => {
        ElMessage.success('已保存为模板')
      })
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
    ElMessageBox.confirm('确认删除此任务？', '提示', { 
      type: 'warning',
      customClass: 'delete-confirm-box'
    }).then(() => {
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
    // 构建重复模式
    const repeatPattern = buildRepeatPattern()
    const requestData = {
      ...form,
      repeatPattern: repeatPattern || undefined,
      repeatType: form.repeatType || undefined,
      repeatEndDate: form.repeatEndDate || undefined
    }

    let planId: number
    if (editingPlan.value) {
      await planApi.update(editingPlan.value.id, requestData as any)
      planId = editingPlan.value.id
      ElMessage.success('更新成功')
    } else {
      const created = await planApi.create(requestData as any)
      planId = created.id
      ElMessage.success('创建成功')
    }

    // 保存提醒设置
    if (form.reminderType) {
      try {
        await reminderApi.setReminder(planId, {
          reminderType: form.reminderType as any,
          advanceMinutes: form.advanceMinutes,
          reminderTime: form.reminderTime || undefined
        })
      } catch (error) {
        console.error('Failed to set reminder', error)
      }
    }

    // 保存标签
    if (form.tagIds && form.tagIds.length > 0) {
      try {
        await tagApi.addTagsToPlan(planId, form.tagIds)
      } catch (error) {
        console.error('Failed to add tags', error)
      }
    }

    showAddDialog.value = false
    editingPlan.value = null
    Object.assign(form, {
      title: '',
      description: '',
      planDate: selectedDate.value,
      priority: 'P2',
      category: 'WORK',
      estimatedMins: undefined,
      repeatType: '',
      repeatPattern: '',
      repeatEndDate: '',
      reminderType: '',
      advanceMinutes: 0,
      reminderTime: '',
      tagIds: []
    })
    weeklyDays.value = []
    monthlyDays.value = []
    loadPlans()
  } finally {
    saving.value = false
  }
}

async function openTemplateDialog() {
  try {
    templates.value = await planApi.listTemplates()
    showTemplateDialog.value = true
  } catch (error) {
    console.error('Failed to load templates', error)
  }
}

async function createFromTemplate(tpl: PlanItem) {
  try {
    await planApi.create({
      title: tpl.title,
      description: tpl.description || '',
      planDate: selectedDate.value,
      priority: tpl.priority,
      category: tpl.category,
      estimatedMins: tpl.estimatedMins || undefined,
      sortOrder: plans.value.length
    })
    ElMessage.success('从模板创建成功')
    showTemplateDialog.value = false
    loadPlans()
  } catch (error) {
    console.error('Failed to create from template', error)
  }
}

// 批量操作相关函数
function handleSelectionChange() {
  // 选择状态改变时的处理
}

function clearSelection() {
  selectedPlans.value = []
}

async function handleBatchComplete() {
  if (selectedPlans.value.length === 0) return

  try {
    await batchApi.batchComplete(selectedPlans.value)
    ElMessage.success('批量完成成功')
    clearSelection()
    loadPlans()
  } catch (error) {
    console.error('Failed to batch complete', error)
  }
}

async function handleBatchPostpone() {
  if (selectedPlans.value.length === 0) return

  try {
    await batchApi.batchPostpone(selectedPlans.value)
    ElMessage.success('批量顺延成功')
    clearSelection()
    loadPlans()
  } catch (error) {
    console.error('Failed to batch postpone', error)
  }
}

async function handleBatchDelete() {
  if (selectedPlans.value.length === 0) return

  ElMessageBox.confirm(`确认删除选中的 ${selectedPlans.value.length} 个任务？`, '批量删除', {
    type: 'warning'
  }).then(async () => {
    try {
      await batchApi.batchDelete(selectedPlans.value)
      ElMessage.success('批量删除成功')
      clearSelection()
      loadPlans()
    } catch (error) {
      console.error('Failed to batch delete', error)
    }
  })
}

async function handleBatchUpdate(cmd: string) {
  if (selectedPlans.value.length === 0) return

  if (cmd === 'priority') {
    ElMessageBox.prompt('请选择新优先级', '修改优先级', {
      inputType: 'select',
      inputOptions: [
        { value: 'P0', label: 'P0 - 紧急' },
        { value: 'P1', label: 'P1 - 重要' },
        { value: 'P2', label: 'P2 - 普通' },
        { value: 'P3', label: 'P3 - 低优' }
      ]
    }).then(({ value }) => {
      if (!value) return
      batchApi.batchUpdate({ ids: selectedPlans.value, priority: value }).then(() => {
        ElMessage.success('批量更新成功')
        clearSelection()
        loadPlans()
      })
    })
  } else if (cmd === 'category') {
    ElMessageBox.prompt('请选择新分类', '修改分类', {
      inputType: 'select',
      inputOptions: categories
    }).then(({ value }) => {
      if (!value) return
      batchApi.batchUpdate({ ids: selectedPlans.value, category: value }).then(() => {
        ElMessage.success('批量更新成功')
        clearSelection()
        loadPlans()
      })
    })
  } else if (cmd === 'status') {
    ElMessageBox.prompt('请选择新状态', '修改状态', {
      inputType: 'select',
      inputOptions: [
        { value: 'TODO', label: '待完成' },
        { value: 'IN_PROGRESS', label: '进行中' },
        { value: 'DONE', label: '已完成' },
        { value: 'CANCELLED', label: '已取消' }
      ]
    }).then(({ value }) => {
      if (!value) return
      batchApi.batchUpdate({ ids: selectedPlans.value, status: value }).then(() => {
        ElMessage.success('批量更新成功')
        clearSelection()
        loadPlans()
      })
    })
  }
}

// 初始化快捷键
const { openCommandPalette, getShortcutsHelp } = useKeyboardShortcuts({
  onNewTask: () => {
    showAddDialog.value = true
  },
  onToggleStatus: (plan: PlanItem) => {
    toggleStatus(plan)
  },
  onDeleteTask: (plan: PlanItem) => {
    handleCommand('delete', plan)
  },
  onEditTask: (plan: PlanItem) => {
    handleCommand('edit', plan)
  },
  onOpenCommandPalette: () => {
    showCommandPalette.value = true
  },
  getSelectedTask: () => {
    if (selectedTaskIndex.value >= 0 && selectedTaskIndex.value < filteredPlans.value.length) {
      return filteredPlans.value[selectedTaskIndex.value]
    }
    return null
  },
  getAllTasks: () => filteredPlans.value,
  onSelectTask: (task: PlanItem, index: number) => {
    selectedTaskIndex.value = index
  }
})

function handleCommandExecute(commandId: string) {
  switch (commandId) {
    case 'new-task':
      showAddDialog.value = true
      break
    case 'complete-selected':
      const selectedTask = selectedTaskIndex.value >= 0 ? filteredPlans.value[selectedTaskIndex.value] : null
      if (selectedTask) {
        toggleStatus(selectedTask)
      }
      break
    case 'postpone-selected':
      const postponeTask = selectedTaskIndex.value >= 0 ? filteredPlans.value[selectedTaskIndex.value] : null
      if (postponeTask) {
        planApi.postpone(postponeTask.id).then(() => {
          ElMessage.success('已顺延至明天')
          loadPlans()
        })
      }
      break
    case 'delete-selected':
      const deleteTask = selectedTaskIndex.value >= 0 ? filteredPlans.value[selectedTaskIndex.value] : null
      if (deleteTask) {
        handleCommand('delete', deleteTask)
      }
      break
    case 'toggle-view':
      const views = ['list', 'analytics', 'timeblock', 'calendar']
      const currentIndex = views.indexOf(activeTab.value)
      activeTab.value = views[(currentIndex + 1) % views.length]
      break
    case 'go-today':
      selectedDate.value = dayjs().format('YYYY-MM-DD')
      loadPlans()
      break
    case 'clear-completed':
      const completedPlans = filteredPlans.value.filter(p => p.status === 'DONE')
      if (completedPlans.length > 0) {
        ElMessageBox.confirm(`确认清除 ${completedPlans.length} 个已完成的任务？`, '提示', {
          type: 'warning'
        }).then(async () => {
          try {
            await batchApi.batchDelete(completedPlans.map(p => p.id))
            ElMessage.success('清除成功')
            loadPlans()
          } catch (error) {
            console.error('Failed to clear completed', error)
          }
        })
      } else {
        ElMessage.info('没有已完成的任务')
      }
      break
  }
}

function handleTaskSelectFromPalette(task: PlanItem) {
  // 选中任务并高亮显示
  const index = filteredPlans.value.findIndex(p => p.id === task.id)
  if (index >= 0) {
    selectedTaskIndex.value = index
    // 滚动到该任务
    const taskElement = document.querySelector(`[data-task-id="${task.id}"]`)
    if (taskElement) {
      taskElement.scrollIntoView({ behavior: 'smooth', block: 'center' })
    }
  }
}

onMounted(() => {
  loadPlans()
  loadTags()
})
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

  .batch-toolbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 16px;
    background: rgba(99, 102, 241, 0.1);
    border: 1px solid rgba(99, 102, 241, 0.3);
    border-radius: $radius-md;
    margin-bottom: 12px;

    .selected-count {
      font-size: 14px;
      color: $text-primary;
      font-weight: 600;
    }

    .batch-actions {
      display: flex;
      gap: 8px;
    }
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

    &.selected {
      border-color: $primary;
      background: rgba(99, 102, 241, 0.08);
      box-shadow: 0 0 0 2px rgba(99, 102, 241, 0.2);
    }

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

    .task-left {
      display: flex;
      align-items: center;
      gap: 8px;
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
        display: flex;
        align-items: center;
        gap: 4px;

        .expand-icon {
          font-size: 14px;
          cursor: pointer;
          transition: transform 0.2s;
          color: $text-muted;

          &.expanded {
            transform: rotate(90deg);
          }

          &:hover {
            color: $primary;
          }
        }
      }

      .subtask-progress {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 6px;

        .progress-bar {
          flex: 1;
          height: 4px;
          background: rgba(255, 255, 255, 0.1);
          border-radius: 2px;
          overflow: hidden;

          .progress-fill {
            height: 100%;
            background: $primary;
            transition: width 0.3s ease;
          }
        }

        .progress-text {
          font-size: 11px;
          color: $text-secondary;
          min-width: 40px;
          text-align: right;
        }
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

        .repeat-badge {
          font-size: 12px;
          cursor: help;
        }

        .task-tags {
          display: flex;
          gap: 4px;
          align-items: center;

          .task-tag {
            font-size: 10px;
            padding: 2px 6px;
            border-radius: 4px;
            font-weight: 500;
          }
        }
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
        font-size: 18px;

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

  .subtask-list {
    margin-left: 32px;
    margin-top: 8px;
    padding-left: 12px;
    border-left: 2px solid rgba(255, 255, 255, 0.1);
  }

  .subtask-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 6px 8px;
    margin-bottom: 4px;
    border-radius: $radius-sm;
    transition: $transition-fast;

    &:hover {
      background: rgba(255, 255, 255, 0.05);
    }

    &.done {
      .subtask-title {
        text-decoration: line-through;
        color: $text-muted;
      }
    }

    .subtask-check {
      cursor: pointer;
      .check-circle {
        width: 16px;
        height: 16px;
        border-radius: 50%;
        border: 2px solid $text-muted;
        display: flex;
        align-items: center;
        justify-content: center;
        transition: $transition-fast;
        font-size: 10px;

        &.DONE {
          border-color: $success;
          background: $success;
          color: white;
        }
      }
    }

    .subtask-title {
      flex: 1;
      font-size: 13px;
      color: $text-primary;
      transition: $transition-fast;
    }

    .subtask-delete {
      font-size: 14px;
      color: $text-muted;
      cursor: pointer;
      opacity: 0;
      transition: $transition-fast;

      &:hover {
        color: $danger;
      }
    }

    &:hover .subtask-delete {
      opacity: 1;
    }
  }

  .subtask-empty {
    padding: 12px;
    text-align: center;
    color: $text-muted;
    font-size: 12px;
  }

  .template-item {
    transition: $transition-fast;
    &:hover {
      border-color: $primary;
      background: $primary-50;
    }
  }
}

:deep(.tag-option) {
  display: flex;
  align-items: center;
  gap: 6px;

  .tag-dot {
    width: 10px;
    height: 10px;
    border-radius: 50%;
  }
}

:deep(.danger) {
  color: $danger !important;
}
</style>

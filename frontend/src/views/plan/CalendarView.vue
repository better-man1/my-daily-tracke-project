<template>
  <div class="calendar-view">
    <!-- 月份切换 -->
    <div class="calendar-header">
      <el-button-group>
        <el-button :icon="ArrowLeft" @click="prevMonth">上个月</el-button>
        <el-button :icon="ArrowRight" @click="nextMonth">下个月</el-button>
      </el-button-group>
      <div class="current-month">{{ currentMonthYear }}</div>
      <el-button @click="goToToday">今天</el-button>
    </div>

    <!-- 日历网格 -->
    <div class="calendar-grid">
      <!-- 星期头 -->
      <div class="week-header">
        <div v-for="day in weekDays" :key="day" class="weekday">{{ day }}</div>
      </div>

      <!-- 日期格子 -->
      <div class="calendar-body">
        <div
          v-for="(day, idx) in calendarDays"
          :key="idx"
          class="calendar-day"
          :class="{
            'other-month': day.isOtherMonth,
            'today': day.isToday,
            'selected': selectedDate === day.date
          }"
          @click="selectDate(day)"
        >
          <div class="day-number">{{ day.day }}</div>
          <div class="day-plans">
            <div
              v-for="plan in day.plans.slice(0, 3)"
              :key="plan.id"
              class="plan-chip"
              :class="[
                `priority-${plan.priority.toLowerCase()}`,
                { done: plan.status === 'DONE' }
              ]"
              @click.stop="handlePlanClick(plan)"
            >
              <span class="plan-title">{{ plan.title }}</span>
              <span v-if="plan.subtaskCount && plan.subtaskCount > 0" class="plan-subtask">
                {{ plan.completedSubtaskCount || 0 }}/{{ plan.subtaskCount }}
              </span>
            </div>
            <div v-if="day.plans.length > 3" class="more-plans" @click.stop="showMorePlans(day)">
              +{{ day.plans.length - 3 }} 更多
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 选中日期的任务列表 -->
    <div v-if="selectedDate" class="day-details">
      <div class="details-header">
        <h3>{{ selectedDate }} 的任务</h3>
        <el-button size="small" type="primary" @click="handleCreatePlan">新增任务</el-button>
      </div>
      <div v-loading="loadingPlans" class="day-plans-list">
        <div
          v-for="plan in selectedDayPlans"
          :key="plan.id"
          class="plan-item"
          :class="{ done: plan.status === 'DONE' }"
          @click="handlePlanClick(plan)"
        >
          <div class="plan-info">
            <div class="plan-main">
              <span class="plan-priority" :class="plan.priority.toLowerCase()">{{ plan.priority }}</span>
              <span class="plan-title">{{ plan.title }}</span>
            </div>
            <div class="plan-meta">
              <span class="plan-category">{{ categoryLabel(plan.category) }}</span>
              <span v-if="plan.estimatedMins">⏱ {{ plan.estimatedMins }}min</span>
            </div>
          </div>
          <div class="plan-status">
            <el-icon v-if="plan.status === 'DONE'" class="status-icon done"><Check /></el-icon>
            <el-icon v-else-if="plan.status === 'IN_PROGRESS'" class="status-icon inprogress"><Clock /></el-icon>
            <el-icon v-else class="status-icon todo"><CircleClose /></el-icon>
          </div>
        </div>
        <div v-if="selectedDayPlans.length === 0" class="empty-state">
          该日期暂无任务
        </div>
      </div>
    </div>

    <!-- 任务详情弹窗 -->
    <el-dialog
      v-model="showPlanDialog"
      :title="selectedPlan ? '任务详情' : '新增任务'"
      width="600px"
      destroy-on-close
    >
      <div v-if="selectedPlan" class="plan-detail">
        <div class="detail-header">
          <span class="detail-priority" :class="selectedPlan.priority.toLowerCase()">
            {{ selectedPlan.priority }}
          </span>
          <h2>{{ selectedPlan.title }}</h2>
        </div>
        <div v-if="selectedPlan.description" class="detail-desc">{{ selectedPlan.description }}</div>
        <div class="detail-meta">
          <div class="meta-item">
            <span class="meta-label">分类:</span>
            <span>{{ categoryLabel(selectedPlan.category) }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">状态:</span>
            <span :class="`status-${selectedPlan.status.toLowerCase()}`">{{ statusLabel(selectedPlan.status) }}</span>
          </div>
          <div v-if="selectedPlan.estimatedMins" class="meta-item">
            <span class="meta-label">预估:</span>
            <span>{{ selectedPlan.estimatedMins }} 分钟</span>
          </div>
          <div v-if="selectedPlan.startTime && selectedPlan.endTime" class="meta-item">
            <span class="meta-label">时间:</span>
            <span>{{ selectedPlan.startTime }} - {{ selectedPlan.endTime }}</span>
          </div>
          <div v-if="selectedPlan.repeatType" class="meta-item">
            <span class="meta-label">重复:</span>
            <span>{{ repeatTypeLabel(selectedPlan.repeatType) }}</span>
          </div>
        </div>
        <div v-if="selectedPlan.subtaskCount && selectedPlan.subtaskCount > 0" class="detail-subtasks">
          <div class="subtask-header">
            <span>子任务进度</span>
            <span>{{ selectedPlan.completedSubtaskCount || 0 }} / {{ selectedPlan.subtaskCount }}</span>
          </div>
          <div class="subtask-progress">
            <div class="progress-bar">
              <div class="progress-fill" :style="{ width: subtaskProgress(selectedPlan) + '%' }"></div>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="showPlanDialog = false">关闭</el-button>
        <el-button v-if="selectedPlan" type="primary" @click="editPlan">编辑</el-button>
        <el-button v-if="selectedPlan" type="danger" @click="deletePlan">删除</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, ArrowRight, Check, Clock, CircleClose } from '@element-plus/icons-vue'
import { planApi } from '@/api/plan'
import type { PlanItem } from '@/api/plan'
import dayjs, { Dayjs } from 'dayjs'

const emit = defineEmits<{
  (e: 'selectDate', date: string): void
}>()

const weekDays = ['日', '一', '二', '三', '四', '五', '六']
const currentMonth = ref(dayjs())
const selectedDate = ref('')
const selectedPlan = ref<PlanItem | null>(null)
const showPlanDialog = ref(false)
const calendarPlans = ref<Record<string, PlanItem[]>>({})
const loadingPlans = ref(false)

const categories = [
  { value: 'WORK', label: '工作' },
  { value: 'STUDY', label: '学习' },
  { value: 'LIFE', label: '生活' },
  { value: 'HEALTH', label: '健康' }
]

const currentMonthYear = computed(() => {
  return currentMonth.value.format('YYYY 年 MM 月')
})

const selectedDayPlans = computed(() => {
  return calendarPlans.value[selectedDate.value] || []
})

const calendarDays = computed(() => {
  const days = []
  const firstDay = currentMonth.value.startOf('month')
  const lastDay = currentMonth.value.endOf('month')
  const startWeekday = firstDay.day()
  const endWeekday = lastDay.day()

  // 上个月的日期
  const prevMonth = currentMonth.value.subtract(1, 'month')
  const prevMonthLastDay = prevMonth.endOf('month')
  for (let i = startWeekday - 1; i >= 0; i--) {
    const day = prevMonthLastDay.subtract(i, 'day')
    days.push({
      date: day.format('YYYY-MM-DD'),
      day: day.date(),
      isOtherMonth: true,
      isToday: day.isSame(dayjs(), 'day'),
      plans: calendarPlans.value[day.format('YYYY-MM-DD')] || []
    })
  }

  // 当前月的日期
  for (let i = 1; i <= lastDay.date(); i++) {
    const day = firstDay.date(i)
    days.push({
      date: day.format('YYYY-MM-DD'),
      day: i,
      isOtherMonth: false,
      isToday: day.isSame(dayjs(), 'day'),
      plans: calendarPlans.value[day.format('YYYY-MM-DD')] || []
    })
  }

  // 下个月的日期
  const nextMonth = currentMonth.value.add(1, 'month')
  for (let i = 1; i <= 6 - endWeekday; i++) {
    const day = nextMonth.date(i)
    days.push({
      date: day.format('YYYY-MM-DD'),
      day: i,
      isOtherMonth: true,
      isToday: day.isSame(dayjs(), 'day'),
      plans: calendarPlans.value[day.format('YYYY-MM-DD')] || []
    })
  }

  return days
})

function categoryLabel(val: string) {
  return categories.find(c => c.value === val)?.label || val
}

function statusLabel(val: string) {
  const labels: Record<string, string> = {
    'TODO': '待完成',
    'IN_PROGRESS': '进行中',
    'DONE': '已完成',
    'CANCELLED': '已取消'
  }
  return labels[val] || val
}

function repeatTypeLabel(val: string) {
  const labels: Record<string, string> = {
    'NONE': '不重复',
    'DAILY': '每天',
    'WEEKLY': '每周',
    'MONTHLY': '每月',
    'CUSTOM': '自定义'
  }
  return labels[val] || val
}

function subtaskProgress(plan: PlanItem) {
  if (!plan.subtaskCount || plan.subtaskCount === 0) return 0
  return Math.round(((plan.completedSubtaskCount || 0) / plan.subtaskCount) * 100)
}

function prevMonth() {
  currentMonth.value = currentMonth.value.subtract(1, 'month')
  loadMonthPlans()
}

function nextMonth() {
  currentMonth.value = currentMonth.value.add(1, 'month')
  loadMonthPlans()
}

function goToToday() {
  currentMonth.value = dayjs()
  selectDate({
    date: dayjs().format('YYYY-MM-DD')
  })
  loadMonthPlans()
}

function selectDate(day: any) {
  selectedDate.value = day.date
  emit('selectDate', day.date)
}

async function loadMonthPlans() {
  loadingPlans.value = true
  try {
    const startDate = currentMonth.value.startOf('month').format('YYYY-MM-DD')
    const endDate = currentMonth.value.endOf('month').format('YYYY-MM-DD')

    // 加载当前月所有日期的任务
    const promises: Promise<PlanItem[]>[] = []
    let current = dayjs(startDate)
    while (!current.isAfter(dayjs(endDate))) {
      promises.push(planApi.list(current.format('YYYY-MM-DD')))
      current = current.add(1, 'day')
    }

    const results = await Promise.all(promises)
    calendarPlans.value = {}
    let idx = 0
    current = dayjs(startDate)
    while (!current.isAfter(dayjs(endDate))) {
      calendarPlans.value[current.format('YYYY-MM-DD')] = results[idx] || []
      current = current.add(1, 'day')
      idx++
    }
  } catch (error) {
    console.error('Failed to load month plans', error)
  } finally {
    loadingPlans.value = false
  }
}

function handlePlanClick(plan: PlanItem) {
  selectedPlan.value = plan
  showPlanDialog.value = true
}

function handleCreatePlan() {
  emit('selectDate', selectedDate.value)
  // 触发父组件打开新建任务弹窗
  ElMessage.info('请在任务列表中创建新任务')
}

function showMorePlans(day: any) {
  selectedDate.value = day.date
  emit('selectDate', day.date)
}

function editPlan() {
  emit('selectDate', selectedDate.value)
  showPlanDialog.value = false
  // 触发父组件打开编辑任务弹窗
  ElMessage.info('请在任务列表中编辑任务')
}

async function deletePlan() {
  if (!selectedPlan.value) return

  ElMessageBox.confirm('确认删除此任务？', '提示', { type: 'warning' }).then(async () => {
    try {
      await planApi.delete(selectedPlan.value!.id)
      ElMessage.success('删除成功')
      showPlanDialog.value = false
      selectedPlan.value = null
      loadMonthPlans()
    } catch (error) {
      console.error('Failed to delete plan', error)
    }
  })
}

onMounted(() => {
  selectedDate.value = dayjs().format('YYYY-MM-DD')
  loadMonthPlans()
})
</script>

<style lang="scss" scoped>
.calendar-view {
  display: flex;
  flex-direction: column;
  gap: 20px;

  .calendar-header {
    display: flex;
    align-items: center;
    gap: 16px;

    .current-month {
      font-size: 18px;
      font-weight: 600;
      color: $text-primary;
      min-width: 120px;
      text-align: center;
    }
  }

  .calendar-grid {
    background: $bg-card;
    border: 1px solid $border;
    border-radius: $radius-md;
    overflow: hidden;

    .week-header {
      display: grid;
      grid-template-columns: repeat(7, 1fr);
      background: rgba(99, 102, 241, 0.05);
      border-bottom: 1px solid $border;

      .weekday {
        padding: 12px;
        text-align: center;
        font-weight: 600;
        color: $text-secondary;
      }
    }

    .calendar-body {
      display: grid;
      grid-template-columns: repeat(7, 1fr);
      grid-auto-rows: minmax(100px, 1fr);

      .calendar-day {
        border-right: 1px solid $border;
        border-bottom: 1px solid $border;
        padding: 8px;
        cursor: pointer;
        transition: $transition-fast;
        min-height: 100px;

        &:hover {
          background: rgba(99, 102, 241, 0.05);
        }

        &.other-month {
          opacity: 0.3;
        }

        &.today {
          background: rgba(99, 102, 241, 0.08);
        }

        &.selected {
          background: rgba(99, 102, 241, 0.15);
          border-color: $primary;
        }

        .day-number {
          font-size: 14px;
          font-weight: 600;
          color: $text-primary;
          margin-bottom: 8px;
        }

        .day-plans {
          display: flex;
          flex-direction: column;
          gap: 4px;

          .plan-chip {
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 4px 8px;
            border-radius: 4px;
            font-size: 11px;
            cursor: pointer;
            transition: $transition-fast;
            border-left: 3px solid;

            &.priority-p0 { background: rgba(239, 68, 68, 0.1); border-left-color: #ef4444; }
            &.priority-p1 { background: rgba(249, 115, 22, 0.1); border-left-color: #f97316; }
            &.priority-p2 { background: rgba(59, 130, 246, 0.1); border-left-color: #3b82f6; }
            &.priority-p3 { background: rgba(107, 114, 128, 0.1); border-left-color: #6b7280; }

            &.done {
              opacity: 0.5;
              text-decoration: line-through;
            }

            &:hover {
              transform: translateX(2px);
            }

            .plan-title {
              flex: 1;
              overflow: hidden;
              text-overflow: ellipsis;
              white-space: nowrap;
            }

            .plan-subtask {
              font-size: 10px;
              color: $text-secondary;
              margin-left: 4px;
            }
          }

          .more-plans {
            font-size: 11px;
            color: $primary;
            cursor: pointer;
            text-align: center;
            padding: 2px;

            &:hover {
              text-decoration: underline;
            }
          }
        }
      }
    }
  }

  .day-details {
    background: $bg-card;
    border: 1px solid $border;
    border-radius: $radius-md;
    padding: 20px;

    .details-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;

      h3 {
        margin: 0;
        font-size: 16px;
        font-weight: 600;
        color: $text-primary;
      }
    }

    .day-plans-list {
      display: flex;
      flex-direction: column;
      gap: 8px;

      .plan-item {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 12px;
        background: rgba(255, 255, 255, 0.03);
        border: 1px solid $border;
        border-radius: $radius-sm;
        cursor: pointer;
        transition: $transition-fast;

        &:hover {
          border-color: $primary;
          background: rgba(99, 102, 241, 0.05);
        }

        &.done {
          opacity: 0.6;
          .plan-main .plan-title {
            text-decoration: line-through;
          }
        }

        .plan-info {
          flex: 1;

          .plan-main {
            display: flex;
            align-items: center;
            gap: 8px;
            margin-bottom: 4px;

            .plan-priority {
              font-size: 11px;
              font-weight: 600;
              padding: 2px 6px;
              border-radius: 4px;

              &.p0 { background: rgba(239, 68, 68, 0.2); color: #ef4444; }
              &.p1 { background: rgba(249, 115, 22, 0.2); color: #f97316; }
              &.p2 { background: rgba(59, 130, 246, 0.2); color: #3b82f6; }
              &.p3 { background: rgba(107, 114, 128, 0.2); color: #6b7280; }
            }

            .plan-title {
              font-size: 14px;
              font-weight: 500;
              color: $text-primary;
            }
          }

          .plan-meta {
            display: flex;
            gap: 12px;
            font-size: 12px;
            color: $text-secondary;

            .plan-category {
              padding: 2px 6px;
              background: rgba(99, 102, 241, 0.1);
              border-radius: 4px;
              color: #6366f1;
            }
          }
        }

        .plan-status {
          .status-icon {
            font-size: 20px;

            &.done { color: $success; }
            &.inprogress { color: $warning; }
            &.todo { color: $text-muted; }
          }
        }
      }

      .empty-state {
        text-align: center;
        padding: 40px 20px;
        color: $text-muted;
      }
    }
  }
}

.plan-detail {
  .detail-header {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 16px;

    .detail-priority {
      font-size: 12px;
      font-weight: 600;
      padding: 4px 8px;
      border-radius: 4px;

      &.p0 { background: rgba(239, 68, 68, 0.2); color: #ef4444; }
      &.p1 { background: rgba(249, 115, 22, 0.2); color: #f97316; }
      &.p2 { background: rgba(59, 130, 246, 0.2); color: #3b82f6; }
      &.p3 { background: rgba(107, 114, 128, 0.2); color: #6b7280; }

      h2 {
        margin: 0;
        font-size: 18px;
        font-weight: 600;
        color: $text-primary;
      }
    }
  }

  .detail-desc {
    color: $text-secondary;
    margin-bottom: 20px;
    line-height: 1.6;
  }

  .detail-meta {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
    margin-bottom: 20px;

    .meta-item {
      display: flex;
      gap: 8px;

      .meta-label {
        color: $text-secondary;
        min-width: 60px;
      }

      .status-todo { color: #64748b; }
      .status-in_progress { color: #f59e0b; }
      .status-done { color: #10b981; }
      .status-cancelled { color: #ef4444; }
    }
  }

  .detail-subtasks {
    background: rgba(255, 255, 255, 0.03);
    border-radius: $radius-sm;
    padding: 12px;

    .subtask-header {
      display: flex;
      justify-content: space-between;
      margin-bottom: 8px;
      font-size: 14px;
      color: $text-primary;
    }

    .subtask-progress {
      .progress-bar {
        height: 6px;
        background: rgba(255, 255, 255, 0.1);
        border-radius: 3px;
        overflow: hidden;

        .progress-fill {
          height: 100%;
          background: $primary;
          transition: width 0.3s ease;
        }
      }
    }
  }
}
</style>

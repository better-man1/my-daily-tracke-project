<template>
  <div class="time-block-view">
    <!-- 时间线视图 -->
    <div class="timeline-container">
      <div class="time-scale">
        <div v-for="hour in hours" :key="hour" class="hour-mark" :style="{ top: (hour * 60) + 'px' }">
          <span class="hour-label">{{ String(hour).padStart(2, '0') }}:00</span>
          <div class="hour-line"></div>
        </div>
      </div>

      <div class="time-blocks-area" ref="blocksAreaRef">
        <div
          v-for="block in sortedBlocks"
          :key="block.id"
          class="time-block-card"
          :class="{ done: block.status === 'DONE', conflict: isConflict(block) }"
          :style="getBlockStyle(block)"
          @click="handleBlockClick(block)"
        >
          <div class="block-header">
            <span class="block-time">{{ formatTimeRange(block) }}</span>
            <span v-if="block.category" class="block-category" :style="{ background: getCategoryColor(block.category) }">
              {{ getCategoryLabel(block.category) }}
            </span>
          </div>
          <div class="block-title">{{ block.title }}</div>
          <div v-if="block.description" class="block-desc">{{ block.description }}</div>
          <div class="block-footer">
            <span v-if="block.estimatedMins" class="block-duration">⏱ {{ block.estimatedMins }}min</span>
            <span v-if="isConflict(block)" class="conflict-badge">⚠️ 冲突</span>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-if="sortedBlocks.length === 0" class="empty-state">
          <div class="empty-icon">📅</div>
          <p>今天还没有时间块</p>
          <el-button type="primary" size="small" @click="handleCreateBlock">创建时间块</el-button>
        </div>
      </div>
    </div>

    <!-- 快速创建时间块按钮 -->
    <div class="quick-actions">
      <el-button type="primary" size="small" :icon="Plus" @click="handleCreateBlock">创建时间块</el-button>
      <el-button size="small" @click="refreshBlocks">刷新</el-button>
    </div>

    <!-- 编辑/创建时间块弹窗 -->
    <el-dialog
      v-model="showBlockDialog"
      :title="editingBlock ? '编辑时间块' : '创建时间块'"
      width="500px"
      destroy-on-close
    >
      <el-form ref="blockFormRef" :model="blockForm" :rules="blockRules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="blockForm.title" placeholder="输入时间块标题" />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-time-picker
            v-model="blockForm.startTime"
            format="HH:mm"
            value-format="HH:mm"
            placeholder="选择开始时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-time-picker
            v-model="blockForm.endTime"
            format="HH:mm"
            value-format="HH:mm"
            placeholder="选择结束时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="blockForm.description" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="blockForm.category" style="width: 100%">
            <el-option v-for="cat in categories" :key="cat.value" :label="cat.label" :value="cat.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="预估时间">
          <el-input-number v-model="blockForm.estimatedMins" :min="15" :step="15" controls-position="right" />
          <span class="ml-sm text-muted">分钟</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showBlockDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveBlock">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { planApi } from '@/api/plan'
import type { PlanItem } from '@/api/plan'

const props = defineProps<{
  planDate: string
}>()

const emit = defineEmits<{
  (e: 'changeDate', date: string): void
}>()

const hours = Array.from({ length: 24 }, (_, i) => i)
const blocks = ref<PlanItem[]>([])
const conflicts = ref<PlanItem[]>([])
const loading = ref(false)
const showBlockDialog = ref(false)
const editingBlock = ref<PlanItem | null>(null)
const saving = ref(false)

const categories = [
  { value: 'WORK', label: '工作', color: '#6366f1' },
  { value: 'STUDY', label: '学习', color: '#10b981' },
  { value: 'LIFE', label: '生活', color: '#f59e0b' },
  { value: 'HEALTH', label: '健康', color: '#ef4444' }
]

const blockForm = reactive({
  title: '',
  startTime: '',
  endTime: '',
  description: '',
  category: 'WORK',
  estimatedMins: 60,
  isTimeblock: 1
})

const blockRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
}

const sortedBlocks = computed(() => {
  return [...blocks.value].sort((a, b) => {
    if (!a.startTime || !b.startTime) return 0
    return a.startTime.localeCompare(b.startTime)
  })
})

function getCategoryColor(category: string) {
  return categories.find(c => c.value === category)?.color || '#6366f1'
}

function getCategoryLabel(category: string) {
  return categories.find(c => c.value === category)?.label || category
}

function formatTimeRange(block: PlanItem) {
  if (!block.startTime || !block.endTime) return ''
  return `${block.startTime} - ${block.endTime}`
}

function getBlockStyle(block: PlanItem) {
  if (!block.startTime) return {}

  const [hours, minutes] = block.startTime.split(':').map(Number)
  const top = hours * 60 + minutes

  let height = 60 // 默认高度
  if (block.startTime && block.endTime) {
    const [endH, endM] = block.endTime.split(':').map(Number)
    const duration = (endH * 60 + endM) - (hours * 60 + minutes)
    height = Math.max(duration, 30)
  }

  return {
    top: `${top}px`,
    height: `${height}px`,
    left: '10px',
    right: '10px',
    backgroundColor: `${getCategoryColor(block.category)}20`,
    borderColor: getCategoryColor(block.category)
  }
}

function isConflict(block: PlanItem) {
  return conflicts.value.some(c => c.id === block.id)
}

function handleBlockClick(block: PlanItem) {
  editingBlock.value = block
  Object.assign(blockForm, {
    title: block.title,
    startTime: block.startTime || '',
    endTime: block.endTime || '',
    description: block.description || '',
    category: block.category,
    estimatedMins: block.estimatedMins || 60
  })
  showBlockDialog.value = true
}

function handleCreateBlock() {
  editingBlock.value = null
  Object.assign(blockForm, {
    title: '',
    startTime: '',
    endTime: '',
    description: '',
    category: 'WORK',
    estimatedMins: 60
  })
  showBlockDialog.value = true
}

async function saveBlock() {
  try {
    await planApi.create({
      ...blockForm,
      planDate: props.planDate,
      priority: 'P2'
    } as any)
    ElMessage.success(editingBlock.value ? '更新成功' : '创建成功')
    showBlockDialog.value = false
    await refreshBlocks()
  } catch (error) {
    console.error('Failed to save time block', error)
  }
}

async function refreshBlocks() {
  loading.value = true
  try {
    const [blocksData, conflictsData] = await Promise.all([
      planApi.getTimeBlocks(props.planDate),
      planApi.detectTimeConflicts(props.planDate)
    ])
    blocks.value = blocksData
    conflicts.value = conflictsData
  } catch (error) {
    console.error('Failed to load time blocks', error)
  } finally {
    loading.value = false
  }
}

onMounted(refreshBlocks)
</script>

<style lang="scss" scoped>
.time-block-view {
  display: flex;
  flex-direction: column;
  height: 100%;

  .timeline-container {
    flex: 1;
    display: flex;
    overflow-y: auto;
    position: relative;
    background: $bg-card;
    border: 1px solid $border;
    border-radius: $radius-md;
    margin-bottom: 12px;
    min-height: 1440px; // 24小时 * 60分钟

    .time-scale {
      position: sticky;
      left: 0;
      width: 70px;
      background: $bg-card;
      border-right: 1px solid $border;
      z-index: 10;

      .hour-mark {
        position: absolute;
        left: 0;
        right: 0;
        height: 1px;

        .hour-label {
          position: absolute;
          left: 8px;
          top: -8px;
          font-size: 12px;
          color: $text-secondary;
        }

        .hour-line {
          position: absolute;
          left: 50px;
          right: 0;
          height: 1px;
          background: rgba(255, 255, 255, 0.05);
        }
      }
    }

    .time-blocks-area {
      flex: 1;
      position: relative;

      .time-block-card {
        position: absolute;
        border-left: 4px solid;
        border-radius: $radius-sm;
        padding: 8px 12px;
        cursor: pointer;
        transition: $transition-fast;
        overflow: hidden;

        &:hover {
          transform: translateX(4px);
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        }

        &.done {
          opacity: 0.5;
          .block-title {
            text-decoration: line-through;
          }
        }

        &.conflict {
          border-color: $danger !important;
          background: rgba(239, 68, 68, 0.1) !important;
        }

        .block-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 4px;

          .block-time {
            font-size: 11px;
            color: $text-secondary;
            font-weight: 600;
          }

          .block-category {
            font-size: 10px;
            padding: 2px 6px;
            border-radius: 4px;
            color: white;
          }
        }

        .block-title {
          font-size: 13px;
          font-weight: 600;
          color: $text-primary;
          margin-bottom: 4px;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }

        .block-desc {
          font-size: 11px;
          color: $text-secondary;
          margin-bottom: 6px;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
          overflow: hidden;
        }

        .block-footer {
          display: flex;
          justify-content: space-between;
          align-items: center;

          .block-duration {
            font-size: 10px;
            color: $text-secondary;
          }

          .conflict-badge {
            font-size: 10px;
            color: $danger;
            font-weight: 600;
          }
        }
      }

      .empty-state {
        text-align: center;
        padding: 60px 20px;
        color: $text-muted;
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);

        .empty-icon {
          font-size: 48px;
          margin-bottom: 12px;
        }

        p {
          margin-bottom: 16px;
        }
      }
    }
  }

  .quick-actions {
    display: flex;
    gap: 8px;
    justify-content: center;
  }
}
</style>

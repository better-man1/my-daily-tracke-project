<template>
  <el-dialog
    :model-value="visible"
    @update:model-value="emit('update:visible', $event)"
    title="命令面板"
    width="600px"
    :close-on-click-modal="false"
    @close="handleClose"
    class="command-palette-dialog"
  >
    <div class="command-search">
      <el-input
        v-model="searchQuery"
        placeholder="输入命令或搜索..."
        size="large"
        :prefix-icon="Search"
        autofocus
        @input="handleSearch"
        @keydown.up.prevent="handleKeyUp"
        @keydown.down.prevent="handleKeyDown"
        @keydown.enter="handleEnter"
      />
    </div>

    <div class="command-list" v-if="visible">
      <!-- 快捷命令 -->
      <div v-if="searchQuery === ''" class="command-section">
        <div class="section-title">快速操作</div>
        <div
          v-for="cmd in quickCommands"
          :key="cmd.id"
          class="command-item"
          :class="{ selected: selectedIndex === quickCommands.indexOf(cmd) }"
          @click="executeCommand(cmd)"
          @mouseenter="selectedIndex = quickCommands.indexOf(cmd)"
        >
          <div class="command-icon">{{ cmd.icon }}</div>
          <div class="command-info">
            <div class="command-name">{{ cmd.name }}</div>
            <div class="command-desc">{{ cmd.description }}</div>
          </div>
          <div class="command-shortcut">{{ cmd.shortcut }}</div>
        </div>
      </div>

      <!-- 搜索结果 -->
      <div v-else class="command-section">
        <div class="section-title">搜索结果</div>
        <div
          v-for="result in searchResults"
          :key="result.id"
          class="command-item"
          :class="{ selected: selectedIndex === searchResults.indexOf(result) }"
          @click="selectResult(result)"
          @mouseenter="selectedIndex = searchResults.indexOf(result)"
        >
          <div class="command-icon">{{ result.icon }}</div>
          <div class="command-info">
            <div class="command-name">{{ result.name }}</div>
            <div class="command-desc">{{ result.description }}</div>
          </div>
        </div>
        <div v-if="searchResults.length === 0" class="no-results">
          未找到相关结果
        </div>
      </div>
    </div>

    <div class="command-footer">
      <div class="shortcuts-hint">
        <span class="hint-item"><kbd>↑</kbd> <kbd>↓</kbd> 导航</span>
        <span class="hint-item"><kbd>Enter</kbd> 执行</span>
        <span class="hint-item"><kbd>Esc</kbd> 关闭</span>
      </div>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { Search } from '@element-plus/icons-vue'
import type { PlanItem } from '@/api/plan'

const props = defineProps<{
  visible: boolean
  tasks: PlanItem[]
}>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'executeCommand', commandId: string): void
  (e: 'selectTask', task: PlanItem): void
}>()

const searchQuery = ref('')
const selectedIndex = ref(0)

const quickCommands = [
  { id: 'new-task', name: '新建任务', description: '创建一个新的任务', icon: '➕', shortcut: 'N' },
  { id: 'new-subtask', name: '添加子任务', description: '为选中任务添加子任务', icon: '📝', shortcut: 'Shift+N' },
  { id: 'complete-selected', name: '完成任务', description: '标记选中的任务为已完成', icon: '✅', shortcut: 'Space' },
  { id: 'postpone-selected', name: '顺延任务', description: '将选中的任务顺延到明天', icon: '📅', shortcut: 'P' },
  { id: 'delete-selected', name: '删除任务', description: '删除选中的任务', icon: '🗑️', shortcut: 'Delete' },
  { id: 'toggle-view', name: '切换视图', description: '在列表视图和时间块视图之间切换', icon: '🔄', shortcut: 'V' },
  { id: 'go-today', name: '跳转到今天', description: '查看今天的任务', icon: '📅', shortcut: 'T' },
  { id: 'clear-completed', name: '清除已完成', description: '清除所有已完成的任务', icon: '🧹', shortcut: 'Shift+C' }
]

const searchResults = computed(() => {
  if (!searchQuery.value) return []

  const query = searchQuery.value.toLowerCase()

  // 搜索任务
  const taskResults = props.tasks
    .filter(task => task.title.toLowerCase().includes(query))
    .map(task => ({
      id: `task-${task.id}`,
      name: task.title,
      description: `${task.priority} · ${task.category}`,
      icon: task.status === 'DONE' ? '✅' : '📋',
      type: 'task',
      task
    }))

  // 搜索命令
  const commandResults = quickCommands
    .filter(cmd =>
      cmd.name.toLowerCase().includes(query) ||
      cmd.description.toLowerCase().includes(query)
    )
    .map(cmd => ({
      id: `cmd-${cmd.id}`,
      name: cmd.name,
      description: cmd.description,
      icon: cmd.icon,
      type: 'command',
      command: cmd
    }))

  return [...taskResults, ...commandResults]
})

const currentList = computed(() => {
  return searchQuery.value ? searchResults.value : quickCommands
})

function handleSearch() {
  selectedIndex.value = 0
}

function handleKeyUp() {
  selectedIndex.value = Math.max(0, selectedIndex.value - 1)
}

function handleKeyDown() {
  selectedIndex.value = Math.min(currentList.value.length - 1, selectedIndex.value + 1)
}

function handleEnter() {
  const item = currentList.value[selectedIndex.value]
  if (item) {
    if (item.type === 'task') {
      selectResult(item)
    } else {
      executeCommand(item.command)
    }
  }
}

function executeCommand(cmd: any) {
  emit('executeCommand', cmd.id)
  handleClose()
}

function selectResult(result: any) {
  if (result.type === 'task' && result.task) {
    emit('selectTask', result.task)
    handleClose()
  } else if (result.type === 'command') {
    executeCommand(result.command)
  }
}

function handleClose() {
  emit('update:visible', false)
  searchQuery.value = ''
  selectedIndex.value = 0
}

watch(() => props.visible, (newVal) => {
  if (newVal) {
    searchQuery.value = ''
    selectedIndex.value = 0
  }
})
</script>

<style lang="scss" scoped>
.command-palette-dialog {
  :deep(.el-dialog__header) {
    padding: 20px 20px 0;
  }

  :deep(.el-dialog__body) {
    padding: 20px;
  }

  :deep(.el-dialog__footer) {
    padding: 0 20px 20px;
  }
}

.command-search {
  margin-bottom: 16px;

  :deep(.el-input__wrapper) {
    border-radius: $radius-md;
  }
}

.command-list {
  max-height: 400px;
  overflow-y: auto;

  .command-section {
    .section-title {
      font-size: 12px;
      font-weight: 600;
      color: $text-secondary;
      margin-bottom: 8px;
      padding: 0 8px;
      text-transform: uppercase;
    }

    .command-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 10px 12px;
      border-radius: $radius-sm;
      cursor: pointer;
      transition: $transition-fast;

      &:hover {
        background: rgba(255, 255, 255, 0.05);
      }

      &.selected {
        background: rgba(99, 102, 241, 0.15);
      }

      .command-icon {
        width: 32px;
        height: 32px;
        display: flex;
        align-items: center;
        justify-content: center;
        background: rgba(255, 255, 255, 0.05);
        border-radius: $radius-sm;
        font-size: 16px;
      }

      .command-info {
        flex: 1;

        .command-name {
          font-size: 14px;
          font-weight: 500;
          color: $text-primary;
          margin-bottom: 2px;
        }

        .command-desc {
          font-size: 12px;
          color: $text-secondary;
        }
      }

      .command-shortcut {
        font-size: 11px;
        color: $text-secondary;
        font-family: monospace;
        background: rgba(255, 255, 255, 0.05);
        padding: 4px 8px;
        border-radius: 4px;
      }
    }

    .no-results {
      text-align: center;
      padding: 40px 20px;
      color: $text-muted;
    }
  }
}

.command-footer {
  border-top: 1px solid $border;
  padding-top: 12px;
  margin-top: 12px;

  .shortcuts-hint {
    display: flex;
    justify-content: center;
    gap: 16px;
    font-size: 12px;
    color: $text-secondary;

    .hint-item {
      display: flex;
      align-items: center;
      gap: 4px;

      kbd {
        background: rgba(255, 255, 255, 0.1);
        border: 1px solid $border;
        border-radius: 4px;
        padding: 2px 6px;
        font-family: monospace;
        font-size: 11px;
      }
    }
  }
}
</style>

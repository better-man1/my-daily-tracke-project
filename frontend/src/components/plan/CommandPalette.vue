<!--
/**
 * ============================================================================
 * CommandPalette.vue — 命令面板/快速操作对话框
 * ============================================================================
 *
 * 【组件说明】
 * 提供类似 VS Code 命令面板的快速操作入口。
 * 支持默认快捷命令、实时搜索、键盘导航。
 * ============================================================================
 */
-->

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

// ============================================================================
// // 状态
// ============================================================================

// ============================================================================
// // 状态
// ============================================================================

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

/**
 * 搜索结果列表
 *
 * 功能说明：根据用户输入实时搜索任务和命令
 * 业务逻辑：
 * 1. 如果没有输入，返回空数组
 * 2. 将输入转为小写进行不区分大小写的搜索
 * 3. 搜索任务：
 *    - 过滤标题包含关键词的任务
 *    - 转换为统一格式：id、name、description、icon、type、task
 *    - 图标根据完成状态显示不同emoji
 * 4. 搜索命令：
 *    - 过滤名称或描述包含关键词的命令
 *    - 转换为统一格式：id、name、description、icon、type、command
 * 5. 合并任务结果和命令结果
 *
 * 返回格式示例：
 * [
 *   { id: 'task-1', name: '完成报告', description: 'P0 · 工作', icon: '📋', type: 'task', task: {...} },
 *   { id: 'cmd-delete', name: '删除任务', description: '删除选中的任务', icon: '🗑️', type: 'command', command: {...} }
 * ]
 *
 * @returns 搜索结果数组
 */
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

/**
 * 当前显示的列表
 *
 * 功能说明：根据是否有搜索词返回对应的数据源
 * 业务逻辑：
 * - 有搜索词：返回searchResults（搜索结果）
 * - 无搜索词：返回quickCommands（快捷命令）
 *
 * 使用场景：渲染命令列表时使用
 *
 * @returns 当前要显示的列表数据
 */
const currentList = computed(() => {
  return searchQuery.value ? searchResults.value : quickCommands
})

// ============================================================================
// // 交互处理
// ============================================================================

/**
 * 处理搜索输入
 *
 * 功能说明：搜索框内容变化时的处理
 * 业务逻辑：重置选中索引为0（选中第一个结果）
 *
 * 使用场景：用户在搜索框输入内容时触发
 */
function handleSearch() {
  selectedIndex.value = 0
}

/**
 * 向上导航
 *
 * 功能说明：键盘向上键的处理，选中上一项
 * 业务逻辑：选中索引减1，最小值为0
 *
 * 使用场景：用户按↑键时触发
 */
function handleKeyUp() {
  selectedIndex.value = Math.max(0, selectedIndex.value - 1)
}

/**
 * 向下导航
 *
 * 功能说明：键盘向下键的处理，选中下一项
 * 业务逻辑：选中索引加1，最大值为列表长度-1
 *
 * 使用场景：用户按↓键时触发
 */
function handleKeyDown() {
  selectedIndex.value = Math.min(currentList.value.length - 1, selectedIndex.value + 1)
}

/**
 * 处理回车键
 *
 * 功能说明：执行当前选中的项
 * 业务逻辑：
 * 1. 获取当前选中的项
 * 2. 根据类型执行不同操作：
 *    - task：调用selectResult选中任务
 *    - command：调用executeCommand执行命令
 *
 * 使用场景：用户按Enter键时触发
 */
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

/**
 * 执行命令
 *
 * 功能说明：触发命令执行事件并关闭面板
 * 业务逻辑：
 * 1. 向父组件发出executeCommand事件，传递命令ID
 * 2. 关闭命令面板
 *
 * 使用场景：用户点击命令项或按Enter键时调用
 *
 * @param cmd - 命令对象
 */
function executeCommand(cmd: any) {
  emit('executeCommand', cmd.id)
  handleClose()
}

/**
 * 选中搜索结果
 *
 * 功能说明：处理搜索结果的选中操作
 * 业务逻辑：
 * 1. 如果是任务类型，发出selectTask事件
 * 2. 如果是命令类型，执行该命令
 * 3. 关闭命令面板
 *
 * 使用场景：用户点击搜索结果项时调用
 *
 * @param result - 搜索结果对象
 */
function selectResult(result: any) {
  if (result.type === 'task' && result.task) {
    emit('selectTask', result.task)
    handleClose()
  } else if (result.type === 'command') {
    executeCommand(result.command)
  }
}

/**
 * 关闭命令面板
 *
 * 功能说明：清理状态并关闭弹窗
 * 业务逻辑：
 * 1. 更新visible状态为false
 * 2. 清空搜索词
 * 3. 重置选中索引
 *
 * 使用场景：用户点击关闭按钮、按Esc键、执行命令后调用
 */
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

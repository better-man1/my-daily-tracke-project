import { onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { PlanItem } from '@/api/plan'
import { planApi, batchApi } from '@/api/plan'

interface KeyboardShortcutsOptions {
  // 快捷键操作
  onNewTask?: () => void
  onCompleteTask?: (plan: PlanItem) => void
  onDeleteTask?: (plan: PlanItem) => void
  onEditTask?: (plan: PlanItem) => void
  onToggleStatus?: (plan: PlanItem) => void
  onOpenCommandPalette?: () => void
  // 获取当前选中的任务
  getSelectedTask?: () => PlanItem | null
  // 获取所有任务
  getAllTasks?: () => PlanItem[]
}

export function useKeyboardShortcuts(options: KeyboardShortcutsOptions) {
  const shortcuts: Record<string, string> = {
    'Space': '完成任务',
    'n': '新建任务',
    'Delete': '删除任务',
    'e': '编辑任务',
    'Escape': '取消选择/关闭弹窗',
    'Cmd+k': '打开命令面板',
    'Ctrl+k': '打开命令面板',
    'a': '全选任务',
    'Cmd+a': '全选任务',
    'Ctrl+a': '全选任务'
  }

  let selectedTaskIndex = -1
  let isCommandPaletteOpen = false

  function handleKeydown(e: KeyboardEvent) {
    // 如果在输入框中，不触发快捷键
    const target = e.target as HTMLElement
    const isInput = target.tagName === 'INPUT' ||
                     target.tagName === 'TEXTAREA' ||
                     target.isContentEditable

    if (isInput && !e.metaKey && !e.ctrlKey) {
      return
    }

    const key = e.key.toLowerCase()
    const isMac = navigator.platform.toUpperCase().indexOf('MAC') >= 0
    const cmdOrCtrl = isMac ? e.metaKey : e.ctrlKey

    // 命令面板快捷键
    if ((cmdOrCtrl && key === 'k') || (key === 'k' && isCommandPaletteOpen)) {
      e.preventDefault()
      if (options.onOpenCommandPalette) {
        options.onOpenCommandPalette()
      }
      return
    }

    // 如果命令面板打开，处理其他快捷键
    if (isCommandPaletteOpen) {
      if (key === 'escape') {
        isCommandPaletteOpen = false
      }
      return
    }

    // Space - 完成任务
    if (key === ' ' && !isInput) {
      e.preventDefault()
      const selectedTask = options.getSelectedTask?.()
      if (selectedTask && options.onToggleStatus) {
        options.onToggleStatus(selectedTask)
      }
      return
    }

    // N - 新建任务
    if (key === 'n' && !cmdOrCtrl) {
      e.preventDefault()
      if (options.onNewTask) {
        options.onNewTask()
      }
      return
    }

    // E - 编辑任务
    if (key === 'e' && !isInput) {
      e.preventDefault()
      const selectedTask = options.getSelectedTask?.()
      if (selectedTask && options.onEditTask) {
        options.onEditTask(selectedTask)
      }
      return
    }

    // Delete - 删除任务
    if (key === 'delete' && !isInput) {
      e.preventDefault()
      const selectedTask = options.getSelectedTask?.()
      if (selectedTask && options.onDeleteTask) {
        options.onDeleteTask(selectedTask)
      }
      return
    }

    // Escape - 取消选择
    if (key === 'escape') {
      selectedTaskIndex = -1
      return
    }

    // A - 全选任务
    if ((cmdOrCtrl && key === 'a') || key === 'a' && !isInput) {
      if (cmdOrCtrl) {
        e.preventDefault()
      }
      // 全选逻辑由调用方处理
      return
    }

    // 上下键 - 选择任务
    if (key === 'arrowdown' || key === 'arrowup') {
      const tasks = options.getAllTasks?.() || []
      if (tasks.length === 0) return

      e.preventDefault()

      if (key === 'arrowdown') {
        selectedTaskIndex = Math.min(selectedTaskIndex + 1, tasks.length - 1)
      } else {
        selectedTaskIndex = Math.max(selectedTaskIndex - 1, 0)
      }

      // 通知调用方任务选择已改变
      const selectedTask = tasks[selectedTaskIndex]
      if (selectedTask && options.onSelectTask) {
        options.onSelectTask(selectedTask, selectedTaskIndex)
      }
      return
    }

    // Enter - 执行选中任务的操作
    if (key === 'enter' && !isInput) {
      const selectedTask = options.getSelectedTask?.()
      if (selectedTask) {
        // 可以执行编辑或其他操作
        if (options.onEditTask) {
          options.onEditTask(selectedTask)
        }
      }
      return
    }
  }

  function openCommandPalette() {
    isCommandPaletteOpen = true
  }

  function closeCommandPalette() {
    isCommandPaletteOpen = false
  }

  function getShortcutsHelp() {
    return Object.entries(shortcuts).map(([key, description]) => ({
      key: key.replace('Cmd', '⌘').replace('Ctrl', '⌃'),
      description
    }))
  }

  onMounted(() => {
    window.addEventListener('keydown', handleKeydown)
  })

  onUnmounted(() => {
    window.removeEventListener('keydown', handleKeydown)
  })

  return {
    shortcuts,
    openCommandPalette,
    closeCommandPalette,
    getShortcutsHelp,
    isCommandPaletteOpen: () => isCommandPaletteOpen
  }
}

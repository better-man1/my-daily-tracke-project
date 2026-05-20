/**
 * ============================================================================
 * CommandPalette.tsx — 命令面板/快速操作对话框
 * ============================================================================
 *
 * 【组件说明】
 * 提供类似 VS Code 命令面板的快速操作入口。
 * 支持默认快捷命令、实时搜索、键盘导航。
 * ============================================================================
 */

import React, { useState, useMemo, useEffect, useRef } from 'react'
import { Modal, Input, Empty, Space } from 'antd'
import type { InputRef } from 'antd/es/input'
import { SearchOutlined } from '@ant-design/icons'
import type { PlanItem } from '@/api/plan'
import './CommandPalette.css'

interface CommandPaletteProps {
  visible: boolean
  onClose: () => void
  onExecuteCommand: (commandId: string) => void
  onSelectTask: (task: PlanItem) => void
  tasks: PlanItem[]
}

interface QuickCommand {
  id: string
  name: string
  description: string
  icon: string
  shortcut: string
}

interface SearchResult {
  id: string
  name: string
  description: string
  icon: string
  type: 'task' | 'command'
  task?: PlanItem
  command?: QuickCommand
}

const quickCommands: QuickCommand[] = [
  { id: 'new-task', name: '新建任务', description: '创建一个新的任务', icon: '➕', shortcut: 'N' },
  { id: 'new-subtask', name: '添加子任务', description: '为选中任务添加子任务', icon: '📝', shortcut: 'Shift+N' },
  { id: 'complete-selected', name: '完成任务', description: '标记选中的任务为已完成', icon: '✅', shortcut: 'Space' },
  { id: 'postpone-selected', name: '顺延任务', description: '将选中的任务顺延到明天', icon: '📅', shortcut: 'P' },
  { id: 'delete-selected', name: '删除任务', description: '删除选中的任务', icon: '🗑️', shortcut: 'Delete' },
  { id: 'toggle-view', name: '切换视图', description: '在列表视图和时间块视图之间切换', icon: '🔄', shortcut: 'V' },
  { id: 'go-today', name: '跳转到今天', description: '查看今天的任务', icon: '📅', shortcut: 'T' },
  { id: 'clear-completed', name: '清除已完成', description: '清除所有已完成的任务', icon: '🧹', shortcut: 'Shift+C' }
]

const CommandPalette: React.FC<CommandPaletteProps> = ({
  visible,
  onClose,
  onExecuteCommand,
  onSelectTask,
  tasks
}) => {
  // ============================================================================
  // // 状态
  // ============================================================================
  const [searchQuery, setSearchQuery] = useState('')
  const [selectedIndex, setSelectedIndex] = useState(0)
  const inputRef = useRef<InputRef>(null)

  // ============================================================================
  // // 计算属性
  // ============================================================================
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
  const searchResults = useMemo((): SearchResult[] => {
    if (!searchQuery) return []

    const query = searchQuery.toLowerCase()

    // 搜索任务
    const taskResults = tasks
      .filter(task => task.title.toLowerCase().includes(query))
      .map(task => ({
        id: `task-${task.id}`,
        name: task.title,
        description: `${task.priority} · ${task.category}`,
        icon: task.status === 'DONE' ? '✅' : '📋',
        type: 'task' as const,
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
        type: 'command' as const,
        command: cmd
      }))

    return [...taskResults, ...commandResults]
  }, [searchQuery, tasks])

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
  const currentList = useMemo(() => {
    return searchQuery ? searchResults : quickCommands
  }, [searchQuery, searchResults])

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
  const handleSearch = (value: string) => {
    setSearchQuery(value)
    setSelectedIndex(0)
  }

  /**
   * 向上导航
   *
   * 功能说明：键盘向上键的处理，选中上一项
   * 业务逻辑：选中索引减1，最小值为0
   *
   * 使用场景：用户按↑键时触发
   */
  const handleKeyUp = () => {
    setSelectedIndex(Math.max(0, selectedIndex - 1))
  }

  /**
   * 向下导航
   *
   * 功能说明：键盘向下键的处理，选中下一项
   * 业务逻辑：选中索引加1，最大值为列表长度-1
   *
   * 使用场景：用户按↓键时触发
   */
  const handleKeyDown = () => {
    setSelectedIndex(Math.min(currentList.length - 1, selectedIndex + 1))
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
  const handleEnter = () => {
    const item = currentList[selectedIndex] as SearchResult
    if (item) {
      if (item.type === 'task') {
        selectResult(item)
      } else {
        executeCommand(item.command!)
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
  const executeCommand = (cmd: QuickCommand) => {
    onExecuteCommand(cmd.id)
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
  const selectResult = (result: SearchResult) => {
    if (result.type === 'task' && result.task) {
      onSelectTask(result.task)
      handleClose()
    } else if (result.type === 'command') {
      executeCommand(result.command!)
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
  const handleClose = () => {
    setSearchQuery('')
    setSelectedIndex(0)
    onClose()
  }

  // ============================================================================
  // // 生命周期
  // ============================================================================
  useEffect(() => {
    if (visible) {
      setSearchQuery('')
      setSelectedIndex(0)
      // 延迟聚焦以确保Modal已经渲染
      setTimeout(() => {
        inputRef.current?.focus()
      }, 100)
    }
  }, [visible])

  // ============================================================================
  // // 渲染
  // ============================================================================
  return (
    <Modal
      open={visible}
      onCancel={handleClose}
      title="命令面板"
      width={600}
      footer={null}
      closeIcon={null}
      className="command-palette-modal"
      destroyOnClose
    >
      <div className="command-palette">
        <div className="command-search">
          <Input
            ref={inputRef}
            value={searchQuery}
            onChange={e => handleSearch(e.target.value)}
            placeholder="输入命令或搜索..."
            size="large"
            prefix={<SearchOutlined />}
            onKeyDown={e => {
              e.preventDefault()
              if (e.key === 'ArrowUp') {
                handleKeyUp()
              } else if (e.key === 'ArrowDown') {
                handleKeyDown()
              } else if (e.key === 'Enter') {
                handleEnter()
              } else if (e.key === 'Escape') {
                handleClose()
              }
            }}
          />
        </div>

        {visible && (
          <div className="command-list">
            {/* 快捷命令 */}
            {!searchQuery && (
              <div className="command-section">
                <div className="section-title">快速操作</div>
                {quickCommands.map((cmd, index) => (
                  <div
                    key={cmd.id}
                    className={`command-item ${selectedIndex === index ? 'selected' : ''}`}
                    onClick={() => executeCommand(cmd)}
                    onMouseEnter={() => setSelectedIndex(index)}
                  >
                    <div className="command-icon">{cmd.icon}</div>
                    <div className="command-info">
                      <div className="command-name">{cmd.name}</div>
                      <div className="command-desc">{cmd.description}</div>
                    </div>
                    <div className="command-shortcut">{cmd.shortcut}</div>
                  </div>
                ))}
              </div>
            )}

            {/* 搜索结果 */}
            {searchQuery && (
              <div className="command-section">
                <div className="section-title">搜索结果</div>
                {searchResults.map((result, index) => (
                  <div
                    key={result.id}
                    className={`command-item ${selectedIndex === index ? 'selected' : ''}`}
                    onClick={() => selectResult(result)}
                    onMouseEnter={() => setSelectedIndex(index)}
                  >
                    <div className="command-icon">{result.icon}</div>
                    <div className="command-info">
                      <div className="command-name">{result.name}</div>
                      <div className="command-desc">{result.description}</div>
                    </div>
                  </div>
                ))}
                {searchResults.length === 0 && (
                  <Empty
                    image={Empty.PRESENTED_IMAGE_SIMPLE}
                    description="未找到相关结果"
                  />
                )}
              </div>
            )}
          </div>
        )}

        <div className="command-footer">
          <Space size={16}>
            <span className="hint-item"><kbd>↑</kbd> <kbd>↓</kbd> 导航</span>
            <span className="hint-item"><kbd>Enter</kbd> 执行</span>
            <span className="hint-item"><kbd>Esc</kbd> 关闭</span>
          </Space>
        </div>
      </div>
    </Modal>
  )
}

export default CommandPalette

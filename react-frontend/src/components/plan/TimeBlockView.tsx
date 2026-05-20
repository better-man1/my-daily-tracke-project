/**
 * ============================================================================
 * TimeBlockView.tsx — 时间块视图组件
 * ============================================================================
 *
 * 【组件说明】
 * 以24小时时间线展示当日任务的时间块视图组件。
 *
 * 【主要功能】
 * - 时间线渲染（00:00-23:59）
 * - 任务时间块展示（位置/高度计算）
 * - 时间冲突检测
 * - 快速创建任务
 * ============================================================================
 */

import React, { useState, useMemo, useEffect } from 'react'
import { Button, Modal, Form, Input, Select, TimePicker, InputNumber, message, Empty } from 'antd'
import { PlusOutlined, ClockCircleOutlined } from '@ant-design/icons'
import { planApi, type PlanItem } from '@/api/plan'
import type { Dayjs } from 'dayjs'
import dayjs from 'dayjs'
import './TimeBlockView.css'

interface TimeBlockViewProps {
  planDate: string
  onChangeDate?: (date: string) => void
}

interface BlockFormValues {
  title: string
  startTime: Dayjs | null
  endTime: Dayjs | null
  description: string
  category: string
  estimatedMins: number
  isTimeblock: number
}

const hours = Array.from({ length: 24 }, (_, i) => i)

const categories = [
  { value: 'WORK', label: '工作', color: '#6366f1' },
  { value: 'STUDY', label: '学习', color: '#10b981' },
  { value: 'LIFE', label: '生活', color: '#f59e0b' },
  { value: 'HEALTH', label: '健康', color: '#ef4444' }
]

const TimeBlockView: React.FC<TimeBlockViewProps> = ({ planDate, onChangeDate }) => {
  // ============================================================================
  // // 状态
  // ============================================================================
  const [blocks, setBlocks] = useState<PlanItem[]>([])
  const [conflicts, setConflicts] = useState<PlanItem[]>([])
  const [loading, setLoading] = useState(false)
  const [showBlockDialog, setShowBlockDialog] = useState(false)
  const [editingBlock, setEditingBlock] = useState<PlanItem | null>(null)
  const [saving, setSaving] = useState(false)
  const [form] = Form.useForm<BlockFormValues>()

  // ============================================================================
  // // 计算属性
  // ============================================================================
  const sortedBlocks = useMemo(() => {
    return [...blocks].sort((a, b) => {
      if (!a.startTime || !b.startTime) return 0
      return a.startTime.localeCompare(b.startTime)
    })
  }, [blocks])

  // ============================================================================
  // // 辅助函数
  // ============================================================================
  const getCategoryColor = (category: string) => {
    return categories.find(c => c.value === category)?.color || '#6366f1'
  }

  const getCategoryLabel = (category: string) => {
    return categories.find(c => c.value === category)?.label || category
  }

  const formatTimeRange = (block: PlanItem) => {
    if (!block.startTime || !block.endTime) return ''
    return `${block.startTime} - ${block.endTime}`
  }

  /**
   * 计算时间块的样式（位置和高度）
   *
   * 功能说明：根据任务的开始和结束时间计算在时间线上的位置和高度
   * 业务逻辑：
   * 1. 解析开始时间的时和分，计算距离顶部（00:00）的像素值（1分钟=1px）
   * 2. 计算持续时间（结束时间 - 开始时间）
   * 3. 最小高度限制为30分钟（保证可读性）
   * 4. 返回CSS样式对象
   *
   * 样式计算：
   * - top: 开始时间的分钟数（px）
   * - height: 持续时间（px）
   * - left/right: 留出边距
   * - backgroundColor: 分类颜色的半透明版本
   * - borderColor: 分类颜色
   *
   * 使用场景：渲染时间块卡片时调用
   *
   * @param block - 任务对象，包含startTime和endTime
   * @returns CSS样式对象
   */
  const getBlockStyle = (block: PlanItem): React.CSSProperties => {
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

  /**
   * 判断时间块是否存在冲突
   *
   * 功能说明：检查任务是否在冲突列表中
   * 业务逻辑：在conflicts数组中查找是否存在与当前任务ID相同的记录
   *
   * 使用场景：渲染时间块卡片时，决定是否显示冲突标识
   *
   * @param block - 任务对象
   * @returns 是否存在冲突（true/false）
   */
  const isConflict = (block: PlanItem) => {
    return conflicts.some(c => c.id === block.id)
  }

  // ============================================================================
  // // 交互处理
  // ============================================================================
  const handleBlockClick = (block: PlanItem) => {
    setEditingBlock(block)
    form.setFieldsValue({
      title: block.title,
      startTime: block.startTime ? dayjs(block.startTime, 'HH:mm') : null,
      endTime: block.endTime ? dayjs(block.endTime, 'HH:mm') : null,
      description: block.description || '',
      category: block.category,
      estimatedMins: block.estimatedMins || 60,
      isTimeblock: 1
    })
    setShowBlockDialog(true)
  }

  const handleCreateBlock = () => {
    setEditingBlock(null)
    form.resetFields()
    form.setFieldsValue({
      category: 'WORK',
      estimatedMins: 60,
      isTimeblock: 1
    })
    setShowBlockDialog(true)
  }

  // ============================================================================
  // // 表单提交
  // ============================================================================
  /**
   * 保存时间块（新增或编辑）
   *
   * 功能说明：保存时间块任务并刷新显示
   * 业务逻辑：
   * 1. 构建请求数据（合并表单数据、日期、优先级）
   * 2. 根据editingBlock状态判断新增或编辑
   * 3. 调用API保存数据
   * 4. 成功后关闭弹窗并刷新时间块列表
   *
   * 使用场景：用户在时间块弹窗中点击"确定"按钮时调用
   */
  const saveBlock = async () => {
    try {
      const values = await form.validateFields()
      setSaving(true)

      const data = {
        ...values,
        planDate,
        priority: 'P2',
        startTime: values.startTime ? values.startTime.format('HH:mm') : undefined,
        endTime: values.endTime ? values.endTime.format('HH:mm') : undefined
      }

      if (editingBlock) {
        await planApi.update(editingBlock.id, data)
        message.success('更新成功')
      } else {
        await planApi.create(data)
        message.success('创建成功')
      }

      setShowBlockDialog(false)
      await refreshBlocks()
    } catch (error) {
      console.error('Failed to save time block', error)
      if (error instanceof Error && error.message !== 'Validation failed') {
        message.error('保存失败')
      }
    } finally {
      setSaving(false)
    }
  }

  /**
   * 刷新时间块和冲突数据
   *
   * 功能说明：重新加载指定日期的所有时间块和冲突检测
   * 业务逻辑：
   * 1. 并行请求两个接口：
   *    - 时间块列表
   *    - 时间冲突检测
   * 2. 更新blocks和conflicts响应式变量
   *
   * 使用场景：组件初始化、保存时间块后、点击刷新按钮时调用
   */
  const refreshBlocks = async () => {
    setLoading(true)
    try {
      const [blocksData, conflictsData] = await Promise.all([
        planApi.getTimeBlocks(planDate),
        planApi.detectTimeConflicts(planDate)
      ])
      setBlocks(blocksData)
      setConflicts(conflictsData)
    } catch (error) {
      console.error('Failed to load time blocks', error)
    } finally {
      setLoading(false)
    }
  }

  // ============================================================================
  // // 生命周期
  // ============================================================================
  useEffect(() => {
    refreshBlocks()
  }, [planDate])

  // ============================================================================
  // // 渲染
  // ============================================================================
  return (
    <div className="time-block-view">
      {/* 时间线视图 */}
      <div className="timeline-container">
        <div className="time-scale">
          {hours.map(hour => (
            <div key={hour} className="hour-mark" style={{ top: `${hour * 60}px` }}>
              <span className="hour-label">{String(hour).padStart(2, '0')}:00</span>
              <div className="hour-line"></div>
            </div>
          ))}
        </div>

        <div className="time-blocks-area">
          {sortedBlocks.map(block => (
            <div
              key={block.id}
              className={`time-block-card ${block.status === 'DONE' ? 'done' : ''} ${isConflict(block) ? 'conflict' : ''}`}
              style={getBlockStyle(block)}
              onClick={() => handleBlockClick(block)}
            >
              <div className="block-header">
                <span className="block-time">{formatTimeRange(block)}</span>
                {block.category && (
                  <span className="block-category" style={{ background: getCategoryColor(block.category) }}>
                    {getCategoryLabel(block.category)}
                  </span>
                )}
              </div>
              <div className="block-title">{block.title}</div>
              {block.description && <div className="block-desc">{block.description}</div>}
              <div className="block-footer">
                {block.estimatedMins && <span className="block-duration">⏱ {block.estimatedMins}min</span>}
                {isConflict(block) && <span className="conflict-badge">⚠️ 冲突</span>}
              </div>
            </div>
          ))}

          {/* 空状态 */}
          {sortedBlocks.length === 0 && (
            <div className="empty-state">
              <Empty
                image={Empty.PRESENTED_IMAGE_SIMPLE}
                description="今天还没有时间块"
              >
                <Button type="primary" icon={<PlusOutlined />} onClick={handleCreateBlock}>
                  创建时间块
                </Button>
              </Empty>
            </div>
          )}
        </div>
      </div>

      {/* 快速创建时间块按钮 */}
      <div className="quick-actions">
        <Button type="primary" icon={<PlusOutlined />} onClick={handleCreateBlock}>
          创建时间块
        </Button>
        <Button icon={<ClockCircleOutlined />} onClick={refreshBlocks}>
          刷新
        </Button>
      </div>

      {/* 编辑/创建时间块弹窗 */}
      <Modal
        title={editingBlock ? '编辑时间块' : '创建时间块'}
        open={showBlockDialog}
        onOk={saveBlock}
        onCancel={() => setShowBlockDialog(false)}
        confirmLoading={saving}
        width={500}
        destroyOnClose
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="title"
            label="标题"
            rules={[{ required: true, message: '请输入时间块标题' }]}
          >
            <Input placeholder="输入时间块标题" />
          </Form.Item>
          <Form.Item
            name="startTime"
            label="开始时间"
            rules={[{ required: true, message: '请选择开始时间' }]}
          >
            <TimePicker format="HH:mm" style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item
            name="endTime"
            label="结束时间"
            rules={[{ required: true, message: '请选择结束时间' }]}
          >
            <TimePicker format="HH:mm" style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="description" label="描述">
            <Input.TextArea rows={2} />
          </Form.Item>
          <Form.Item name="category" label="分类" initialValue="WORK">
            <Select>
              {categories.map(cat => (
                <Select.Option key={cat.value} value={cat.value}>
                  {cat.label}
                </Select.Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item name="estimatedMins" label="预估时间" initialValue={60}>
            <InputNumber min={15} step={15} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="isTimeblock" initialValue={1} hidden>
            <Input />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default TimeBlockView
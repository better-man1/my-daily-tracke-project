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

// 数组生成器：生成包含 0 至 23 的整型数字数组，用于渲染 24 小时轴刻度
const hours = Array.from({ length: 24 }, (_, i) => i)

const categories = [
  { value: 'WORK', label: '工作', color: '#6366f1' },
  { value: 'STUDY', label: '学习', color: '#10b981' },
  { value: 'LIFE', label: '生活', color: '#f59e0b' },
  { value: 'HEALTH', label: '健康', color: '#ef4444' }
]

const TimeBlockView: React.FC<TimeBlockViewProps> = ({ planDate, onChangeDate }) => {
  // ============================================================================
  // // 状态（State）
  // ============================================================================
  
  // 今日时间块任务数组
  const [blocks, setBlocks] = useState<PlanItem[]>([])
  // 时间发生冲突的任务数组
  const [conflicts, setConflicts] = useState<PlanItem[]>([])
  const [loading, setLoading] = useState(false)
  const [showBlockDialog, setShowBlockDialog] = useState(false)
  const [editingBlock, setEditingBlock] = useState<PlanItem | null>(null)
  const [saving, setSaving] = useState(false)
  
  // 创建时间块编辑表单实例
  const [form] = Form.useForm<BlockFormValues>()

  // ============================================================================
  // // 计算属性（useMemo）
  // ============================================================================
  
  /**
   * 1. 数组排序缓存：
   *    - 时间轴视图必须按照时间先后顺序从上至下依次排列。
   *    - 这里使用 `useMemo` 对 `blocks` 进行浅拷贝并按 `startTime` 字符串进行升序排列。
   *    - 只有当 `blocks` 数组本身发生修改时，才会重新触发排序，避免每次无谓的 render 重复排序。
   */
  const sortedBlocks = useMemo(() => {
    return [...blocks].sort((a, b) => {
      if (!a.startTime || !b.startTime) return 0
      return a.startTime.localeCompare(b.startTime)
    })
  }, [blocks])

  // ============================================================================
  // // 辅助函数 & 样式动态计算
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
   * 2. 【React 核心概念：动态样式运算 (Dynamic Inline Styling)】
   *    - 场景：在 24 小时时间轴上，每个任务卡片的位置（top 偏移量）和大小（height 高度）必须与它的开始时间、结束时间精确绑定。
   *    - 实现细节：
   *      a. 设定基础尺标：1 分钟对应 1 像素（1px）。
   *      b. 计算 `top`（相对起点的垂直位置）：
   *         - 将开始时间 "08:30" 拆分为 `hours = 8`, `minutes = 30`。
   *         - 距离 00:00 的总分钟数 = `8 * 60 + 30 = 510`。
   *         - 返回 `top: 510px`。
   *      c. 计算 `height`（卡片高度）：
   *         - 计算结束时间与开始时间的差值（如 90 分钟）。
   *         - 设定防字数拥挤的最小高度兜底 `Math.max(duration, 30)`。
   *         - 返回 `height: 90px`。
   *      d. 结合分类，提取出半透明背景颜色 `'#xxxxxx20'` 和边框颜色，实现高水准的设计美学。
   */
  const getBlockStyle = (block: PlanItem): React.CSSProperties => {
    if (!block.startTime) return {}

    const [hoursVal, minutesVal] = block.startTime.split(':').map(Number)
    const top = hoursVal * 60 + minutesVal

    let height = 60 // 默认 1 小时高度
    if (block.startTime && block.endTime) {
      const [endH, endM] = block.endTime.split(':').map(Number)
      const duration = (endH * 60 + endM) - (hoursVal * 60 + minutesVal)
      height = Math.max(duration, 30) // 最小高度为 30px，保障卡片内容显示完整
    }

    return {
      top: `${top}px`,
      height: `${height}px`,
      left: '10px',
      right: '10px',
      backgroundColor: `${getCategoryColor(block.category)}20`, // 取主色调并添加 20 的十六进制透明度
      borderColor: getCategoryColor(block.category)
    }
  }

  /**
   * 3. 冲突标志匹配：
   *    - 判断当前块是否在后端时间冲突检测接口返回的 `conflicts` 列表中。
   *    - 若在，则在卡片边缘加上红色高亮动画边框。
   */
  const isConflict = (block: PlanItem) => {
    return conflicts.some(c => c.id === block.id)
  }

  // ============================================================================
  // // 交互处理与数据更新
  // ============================================================================
  
  // 点击现有时间块进行编辑
  const handleBlockClick = (block: PlanItem) => {
    setEditingBlock(block)
    // 利用 dayjs 将时间字符串 "HH:mm" 解析并回填入 Antd TimePicker 组件中
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

  // 打开创建新时间块弹窗
  const handleCreateBlock = () => {
    setEditingBlock(null)
    form.resetFields()
    form.setFieldsValue({
      category: 'WORK',
      estimatedMins: 60,
      isTimeblock: 1 // 强制设定为 1，告诉后端这是一个具备物理起止时间的时间块，而不是普通待办
    })
    setShowBlockDialog(true)
  }

  // 表单验证并保存数据
  const saveBlock = async () => {
    try {
      const values = await form.validateFields()
      setSaving(true)

      const data = {
        ...values,
        planDate,
        priority: 'P2', // 默认 P2 优先级
        // 将 dayjs 的时间数据重新转换回 "HH:mm" 字符串格式保存入库
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
      await refreshBlocks() // 保存成功后刷新数据
    } catch (error) {
      console.error('保存时间块失败', error)
      if (error instanceof Error && error.message !== 'Validation failed') {
        message.error('保存失败')
      }
    } finally {
      setSaving(false)
    }
  }

  /**
   * 并发刷新列表数据和冲突检测：
   * - 通过 Promise.all 并行请求时间块列表及冲突算法检查。
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
      console.error('加载时间块数据失败', error)
    } finally {
      setLoading(false)
    }
  }

  // ============================================================================
  // // 生命周期
  // ============================================================================
  
  // 依赖 [planDate]：当计划日期切换时，自动刷新加载数据
  useEffect(() => {
    refreshBlocks()
  }, [planDate])

  // ============================================================================
  // // 渲染
  // ============================================================================
  return (
    <div className="time-block-view">
      <div className="timeline-container">
        {/* 左侧：00:00 - 23:00 绝对定位刻度线 */}
        <div className="time-scale">
          {hours.map(hour => (
            <div key={hour} className="hour-mark" style={{ top: `${hour * 60}px` }}>
              <span className="hour-label">{String(hour).padStart(2, '0')}:00</span>
              <div className="hour-line"></div>
            </div>
          ))}
        </div>

        {/* 右侧：卡片绘制放置区域 */}
        <div className="time-blocks-area">
          {sortedBlocks.map(block => (
            <div
              key={block.id}
              // 动态样式类匹配：包括完成划线效果和冲突发光红色轮廓效果
              className={`time-block-card ${block.status === 'DONE' ? 'done' : ''} ${isConflict(block) ? 'conflict' : ''}`}
              style={getBlockStyle(block)} // 注入算好的绝对定位位置
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

          {/* 缺省空状态 */}
          {sortedBlocks.length === 0 && (
            <div className="empty-state">
              <Empty
                image={Empty.PRESENTED_IMAGE_SIMPLE}
                description="今天还没有规划任何时间块"
              >
                <Button type="primary" icon={<PlusOutlined />} onClick={handleCreateBlock}>
                  创建时间块
                </Button>
              </Empty>
            </div>
          )}
        </div>
      </div>

      {/* 底部悬浮控制台 */}
      <div className="quick-actions">
        <Button type="primary" icon={<PlusOutlined />} onClick={handleCreateBlock}>
          创建时间块
        </Button>
        <Button icon={<ClockCircleOutlined />} onClick={refreshBlocks}>
          刷新
        </Button>
      </div>

      {/* 编辑/创建弹窗 */}
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
          {/* 隐藏的辅助字段，告知后端当前创建的并非普通任务，而是固定了物理起止时间的时间块 */}
          <Form.Item name="isTimeblock" initialValue={1} hidden>
            <Input />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default TimeBlockView
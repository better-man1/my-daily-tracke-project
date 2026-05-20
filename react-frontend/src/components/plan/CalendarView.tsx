/**
 * ============================================================================
 * CalendarView.tsx — 日历视图组件
 * ============================================================================
 *
 * 【组件说明】
 * 以月历形式展示每日任务的日历视图组件。
 *
 * 【主要功能】
 * - 月份切换
 * - 日期选择高亮
 * - 每日任务预览
 * - 任务状态标识
 * - emit 事件通知父组件
 * ============================================================================
 */

import React, { useState, useMemo, useEffect } from 'react'
import { Button, Modal, Tag, Spin } from 'antd'
import { LeftOutlined, RightOutlined, CheckOutlined, ClockCircleOutlined, CloseCircleOutlined } from '@ant-design/icons'
import { planApi, type PlanItem } from '@/api/plan'
import dayjs, { Dayjs } from 'dayjs'
import './CalendarView.css'

interface CalendarDay {
  date: string
  day: number
  isOtherMonth: boolean
  isToday: boolean
  plans: PlanItem[]
}

interface CalendarViewProps {
  onSelectDate?: (date: string) => void
}

const weekDays = ['日', '一', '二', '三', '四', '五', '六']

const categories = [
  { value: 'WORK', label: '工作' },
  { value: 'STUDY', label: '学习' },
  { value: 'LIFE', label: '生活' },
  { value: 'HEALTH', label: '健康' }
]

const statusMap: Record<string, { label: string; color: string }> = {
  'TODO': { label: '待完成', color: 'default' },
  'IN_PROGRESS': { label: '进行中', color: 'processing' },
  'DONE': { label: '已完成', color: 'success' },
  'CANCELLED': { label: '已取消', color: 'error' }
}

const repeatTypeMap: Record<string, string> = {
  'NONE': '不重复',
  'DAILY': '每天',
  'WEEKLY': '每周',
  'MONTHLY': '每月',
  'CUSTOM': '自定义'
}

const priorityColors: Record<string, string> = {
  'P0': '#ef4444',
  'P1': '#f97316',
  'P2': '#3b82f6',
  'P3': '#6b7280'
}

const CalendarView: React.FC<CalendarViewProps> = ({ onSelectDate }) => {
  // ============================================================================
  // // 状态
  // ============================================================================
  const [currentMonth, setCurrentMonth] = useState<Dayjs>(dayjs())
  const [selectedDate, setSelectedDate] = useState<string>(dayjs().format('YYYY-MM-DD'))
  const [selectedPlan, setSelectedPlan] = useState<PlanItem | null>(null)
  const [showPlanDialog, setShowPlanDialog] = useState(false)
  const [calendarPlans, setCalendarPlans] = useState<Record<string, PlanItem[]>>({})
  const [loadingPlans, setLoadingPlans] = useState(false)

  // ============================================================================
  // // 计算属性
  // ============================================================================
  const currentMonthYear = useMemo(() => {
    return currentMonth.format('YYYY 年 MM 月')
  }, [currentMonth])

  const selectedDayPlans = useMemo(() => {
    return calendarPlans[selectedDate] || []
  }, [calendarPlans, selectedDate])

  const calendarDays = useMemo((): CalendarDay[] => {
    const days: CalendarDay[] = []
    const firstDay = currentMonth.startOf('month')
    const lastDay = currentMonth.endOf('month')
    const startWeekday = firstDay.day()
    const endWeekday = lastDay.day()

    // 上个月的日期
    const prevMonth = currentMonth.subtract(1, 'month')
    const prevMonthLastDay = prevMonth.endOf('month')
    for (let i = startWeekday - 1; i >= 0; i--) {
      const day = prevMonthLastDay.subtract(i, 'day')
      days.push({
        date: day.format('YYYY-MM-DD'),
        day: day.date(),
        isOtherMonth: true,
        isToday: day.isSame(dayjs(), 'day'),
        plans: calendarPlans[day.format('YYYY-MM-DD')] || []
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
        plans: calendarPlans[day.format('YYYY-MM-DD')] || []
      })
    }

    // 下个月的日期
    const nextMonth = currentMonth.add(1, 'month')
    for (let i = 1; i <= 6 - endWeekday; i++) {
      const day = nextMonth.date(i)
      days.push({
        date: day.format('YYYY-MM-DD'),
        day: i,
        isOtherMonth: true,
        isToday: day.isSame(dayjs(), 'day'),
        plans: calendarPlans[day.format('YYYY-MM-DD')] || []
      })
    }

    return days
  }, [currentMonth, calendarPlans])

  // ============================================================================
  // // 辅助函数
  // ============================================================================
  const categoryLabel = (val: string) => {
    return categories.find(c => c.value === val)?.label || val
  }

  const statusLabel = (val: string) => {
    return statusMap[val]?.label || val
  }

  const repeatTypeLabel = (val: string) => {
    return repeatTypeMap[val] || val
  }

  const subtaskProgress = (plan: PlanItem) => {
    if (!plan.subtaskCount || plan.subtaskCount === 0) return 0
    return Math.round(((plan.completedSubtaskCount || 0) / plan.subtaskCount) * 100)
  }

  // ============================================================================
  // // 导航
  // ============================================================================
  const prevMonth = () => {
    setCurrentMonth(currentMonth.subtract(1, 'month'))
  }

  const nextMonth = () => {
    setCurrentMonth(currentMonth.add(1, 'month'))
  }

  const goToToday = () => {
    setCurrentMonth(dayjs())
    const today = dayjs().format('YYYY-MM-DD')
    selectDate({ date: today, day: dayjs().date(), isOtherMonth: false, isToday: true, plans: [] })
  }

  const selectDate = (day: CalendarDay) => {
    setSelectedDate(day.date)
    onSelectDate?.(day.date)
  }

  // ============================================================================
  // // 数据加载
  // ============================================================================
  /**
   * 加载当月所有日期的任务数据
   *
   * 功能说明：批量加载当前月份每一天的任务列表
   * 业务逻辑：
   * 1. 获取当月第一天和最后一天日期
   * 2. 创建日期数组用于存储所有请求
   * 3. 循环为每一天创建一个API请求
   * 4. 使用Promise.all并行请求所有数据
   * 5. 将结果整理为以日期为key的对象结构：{ '2024-01-01': [Task1, Task2], ... }
   *
   * 使用场景：组件初始化、切换月份时调用
   */
  const loadMonthPlans = async () => {
    setLoadingPlans(true)
    try {
      const startDate = currentMonth.startOf('month').format('YYYY-MM-DD')
      const endDate = currentMonth.endOf('month').format('YYYY-MM-DD')

      // 加载当前月所有日期的任务
      const promises: Promise<PlanItem[]>[] = []
      let current = dayjs(startDate)
      while (!current.isAfter(dayjs(endDate))) {
        promises.push(planApi.list(current.format('YYYY-MM-DD')))
        current = current.add(1, 'day')
      }

      const results = await Promise.all(promises)
      const newCalendarPlans: Record<string, PlanItem[]> = {}
      let idx = 0
      current = dayjs(startDate)
      while (!current.isAfter(dayjs(endDate))) {
        newCalendarPlans[current.format('YYYY-MM-DD')] = results[idx] || []
        current = current.add(1, 'day')
        idx++
      }

      setCalendarPlans(newCalendarPlans)
    } catch (error) {
      console.error('Failed to load month plans', error)
    } finally {
      setLoadingPlans(false)
    }
  }

  // ============================================================================
  // // 交互处理
  // ============================================================================
  const handlePlanClick = (plan: PlanItem, e: React.MouseEvent) => {
    e.stopPropagation()
    setSelectedPlan(plan)
    setShowPlanDialog(true)
  }

  const handleCreatePlan = () => {
    onSelectDate?.(selectedDate)
    // 触发父组件打开新建任务弹窗
    Modal.info({ title: '提示', content: '请在任务列表中创建新任务' })
  }

  const showMorePlans = (day: CalendarDay) => {
    selectDate(day)
  }

  const editPlan = () => {
    onSelectDate?.(selectedDate)
    setShowPlanDialog(false)
    // 触发父组件打开编辑任务弹窗
    Modal.info({ title: '提示', content: '请在任务列表中编辑任务' })
  }

  const deletePlan = async () => {
    if (!selectedPlan) return

    Modal.confirm({
      title: '提示',
      content: '确认删除此任务？',
      okType: 'danger',
      onOk: async () => {
        try {
          await planApi.delete(selectedPlan.id)
          Modal.success({ content: '删除成功' })
          setShowPlanDialog(false)
          setSelectedPlan(null)
          loadMonthPlans()
        } catch (error) {
          console.error('Failed to delete plan', error)
        }
      }
    })
  }

  // ============================================================================
  // // 生命周期
  // ============================================================================
  useEffect(() => {
    setSelectedDate(dayjs().format('YYYY-MM-DD'))
    loadMonthPlans()
  }, [currentMonth])

  // ============================================================================
  // // 渲染
  // ============================================================================
  return (
    <div className="calendar-view">
      {/* 月份切换 */}
      <div className="calendar-header">
        <Button.Group>
          <Button icon={<LeftOutlined />} onClick={prevMonth}>上个月</Button>
          <Button icon={<RightOutlined />} onClick={nextMonth}>下个月</Button>
        </Button.Group>
        <div className="current-month">{currentMonthYear}</div>
        <Button onClick={goToToday}>今天</Button>
      </div>

      {/* 日历网格 */}
      <div className="calendar-grid">
        {/* 星期头 */}
        <div className="week-header">
          {weekDays.map(day => (
            <div key={day} className="weekday">{day}</div>
          ))}
        </div>

        {/* 日期格子 */}
        <div className="calendar-body">
          {calendarDays.map((day, idx) => (
            <div
              key={idx}
              className={`calendar-day ${day.isOtherMonth ? 'other-month' : ''} ${day.isToday ? 'today' : ''} ${selectedDate === day.date ? 'selected' : ''}`}
              onClick={() => selectDate(day)}
            >
              <div className="day-number">{day.day}</div>
              <div className="day-plans">
                {day.plans.slice(0, 3).map(plan => (
                  <div
                    key={plan.id}
                    className={`plan-chip priority-${plan.priority.toLowerCase()} ${plan.status === 'DONE' ? 'done' : ''}`}
                    onClick={(e) => handlePlanClick(plan, e)}
                    style={{ borderLeftColor: priorityColors[plan.priority] }}
                  >
                    <span className="plan-title">{plan.title}</span>
                    {plan.subtaskCount && plan.subtaskCount > 0 && (
                      <span className="plan-subtask">
                        {plan.completedSubtaskCount || 0}/{plan.subtaskCount}
                      </span>
                    )}
                  </div>
                ))}
                {day.plans.length > 3 && (
                  <div className="more-plans" onClick={(e) => { e.stopPropagation(); showMorePlans(day) }}>
                    +{day.plans.length - 3} 更多
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* 选中日期的任务列表 */}
      {selectedDate && (
        <div className="day-details">
          <div className="details-header">
            <h3>{selectedDate} 的任务</h3>
            <Button type="primary" size="small" onClick={handleCreatePlan}>新增任务</Button>
          </div>
          <Spin spinning={loadingPlans}>
            <div className="day-plans-list">
              {selectedDayPlans.map(plan => (
                <div
                  key={plan.id}
                  className={`plan-item ${plan.status === 'DONE' ? 'done' : ''}`}
                  onClick={() => handlePlanClick(plan, {} as React.MouseEvent)}
                >
                  <div className="plan-info">
                    <div className="plan-main">
                      <Tag className="plan-priority" color={priorityColors[plan.priority]}>{plan.priority}</Tag>
                      <span className="plan-title">{plan.title}</span>
                    </div>
                    <div className="plan-meta">
                      <span className="plan-category">{categoryLabel(plan.category)}</span>
                      {plan.estimatedMins && <span>⏱ {plan.estimatedMins}min</span>}
                    </div>
                  </div>
                  <div className="plan-status">
                    {plan.status === 'DONE' && <CheckOutlined className="status-icon done" />}
                    {plan.status === 'IN_PROGRESS' && <ClockCircleOutlined className="status-icon inprogress" />}
                    {plan.status === 'TODO' && <CloseCircleOutlined className="status-icon todo" />}
                  </div>
                </div>
              ))}
              {selectedDayPlans.length === 0 && (
                <div className="empty-state">该日期暂无任务</div>
              )}
            </div>
          </Spin>
        </div>
      )}

      {/* 任务详情弹窗 */}
      <Modal
        open={showPlanDialog}
        title={selectedPlan ? '任务详情' : '新增任务'}
        width={600}
        onCancel={() => setShowPlanDialog(false)}
        footer={
          <>
            <Button onClick={() => setShowPlanDialog(false)}>关闭</Button>
            {selectedPlan && <Button type="primary" onClick={editPlan}>编辑</Button>}
            {selectedPlan && <Button danger onClick={deletePlan}>删除</Button>}
          </>
        }
        destroyOnClose
      >
        {selectedPlan && (
          <div className="plan-detail">
            <div className="detail-header">
              <Tag className="detail-priority" color={priorityColors[selectedPlan.priority]}>
                {selectedPlan.priority}
              </Tag>
              <h2>{selectedPlan.title}</h2>
            </div>
            {selectedPlan.description && <div className="detail-desc">{selectedPlan.description}</div>}
            <div className="detail-meta">
              <div className="meta-item">
                <span className="meta-label">分类:</span>
                <span>{categoryLabel(selectedPlan.category)}</span>
              </div>
              <div className="meta-item">
                <span className="meta-label">状态:</span>
                <Tag color={statusMap[selectedPlan.status]?.color}>{statusLabel(selectedPlan.status)}</Tag>
              </div>
              {selectedPlan.estimatedMins && (
                <div className="meta-item">
                  <span className="meta-label">预估:</span>
                  <span>{selectedPlan.estimatedMins} 分钟</span>
                </div>
              )}
              {selectedPlan.startTime && selectedPlan.endTime && (
                <div className="meta-item">
                  <span className="meta-label">时间:</span>
                  <span>{selectedPlan.startTime} - {selectedPlan.endTime}</span>
                </div>
              )}
              {selectedPlan.repeatType && (
                <div className="meta-item">
                  <span className="meta-label">重复:</span>
                  <span>{repeatTypeLabel(selectedPlan.repeatType)}</span>
                </div>
              )}
            </div>
            {selectedPlan.subtaskCount && selectedPlan.subtaskCount > 0 && (
              <div className="detail-subtasks">
                <div className="subtask-header">
                  <span>子任务进度</span>
                  <span>{selectedPlan.completedSubtaskCount || 0} / {selectedPlan.subtaskCount}</span>
                </div>
                <div className="subtask-progress">
                  <div className="progress-bar">
                    <div className="progress-fill" style={{ width: `${subtaskProgress(selectedPlan)}%` }}></div>
                  </div>
                </div>
              </div>
            )}
          </div>
        )}
      </Modal>
    </div>
  )
}

export default CalendarView
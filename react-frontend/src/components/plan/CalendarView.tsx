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
  // // 状态（State）
  // ============================================================================
  
  // 当前处于的月份 Dayjs 对象
  const [currentMonth, setCurrentMonth] = useState<Dayjs>(dayjs())
  // 当前选中的具体日期字符串："YYYY-MM-DD"
  const [selectedDate, setSelectedDate] = useState<string>(dayjs().format('YYYY-MM-DD'))
  const [selectedPlan, setSelectedPlan] = useState<PlanItem | null>(null)
  const [showPlanDialog, setShowPlanDialog] = useState(false)
  
  // 以日期字符串为 key，存储每天任务列表的对象哈希表：{ "2026-05-20": [Task1, Task2] }
  const [calendarPlans, setCalendarPlans] = useState<Record<string, PlanItem[]>>({})
  const [loadingPlans, setLoadingPlans] = useState(false)

  // ============================================================================
  // // 计算属性（useMemo 性能优化）
  // ============================================================================
  
  /**
   * 1. 【React 核心概念：使用 useMemo 缓存计算结果 (useMemo Caching)】
   *    - 在组件重新渲染时，所有在函数内部声明的普通变量都会被重新计算。
   *    - 如果某项计算比较重（例如：解析日期、生成多达 42 个格子并过滤任务），无谓地重复计算会造成 CPU 性能浪费。
   *    - `useMemo` 可以将计算结果缓存（记忆化）起来：
   *      - 只有当依赖项（如 `currentMonth` 或 `calendarPlans`）改变时，它才会重新运行计算函数。
   *      - 若由于其它无关状态（如 `selectedPlan`、`showPlanDialog`）变动触发重新渲染，React 会直接返回上一次缓存的结果。
   */
  const currentMonthYear = useMemo(() => {
    return currentMonth.format('YYYY 年 MM 月')
  }, [currentMonth])

  // 计算当前选中日期的计划列表数据
  const selectedDayPlans = useMemo(() => {
    return calendarPlans[selectedDate] || []
  }, [calendarPlans, selectedDate])

  // 生成日历中需要展示的完整 42 天网格数据（上月兜底天数 + 本月天数 + 下月补充天数）
  const calendarDays = useMemo((): CalendarDay[] => {
    const days: CalendarDay[] = []
    const firstDay = currentMonth.startOf('month')
    const lastDay = currentMonth.endOf('month')
    const startWeekday = firstDay.day() // 本月第一天是周几
    const endWeekday = lastDay.day()   // 本月最后一天是周几

    // 1. 上个月补充日期天数（前置置灰部分）
    const prevMonth = currentMonth.subtract(1, 'month')
    const prevMonthLastDay = prevMonth.endOf('month')
    for (let i = startWeekday - 1; i >= 0; i--) {
      const day = prevMonthLastDay.subtract(i, 'day')
      const dateStr = day.format('YYYY-MM-DD')
      days.push({
        date: dateStr,
        day: day.date(),
        isOtherMonth: true,
        isToday: day.isSame(dayjs(), 'day'),
        plans: calendarPlans[dateStr] || []
      })
    }

    // 2. 本月日期天数（主体高亮部分）
    for (let i = 1; i <= lastDay.date(); i++) {
      const day = firstDay.date(i)
      const dateStr = day.format('YYYY-MM-DD')
      days.push({
        date: dateStr,
        day: i,
        isOtherMonth: false,
        isToday: day.isSame(dayjs(), 'day'),
        plans: calendarPlans[dateStr] || []
      })
    }

    // 3. 下个月补充日期天数（后置置灰部分）
    const nextMonth = currentMonth.add(1, 'month')
    for (let i = 1; i <= 6 - endWeekday; i++) {
      const day = nextMonth.date(i)
      const dateStr = day.format('YYYY-MM-DD')
      days.push({
        date: dateStr,
        day: i,
        isOtherMonth: true,
        isToday: day.isSame(dayjs(), 'day'),
        plans: calendarPlans[dateStr] || []
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
  // // 导航处理
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
    // 强制选中今天
    selectDate({ date: today, day: dayjs().date(), isOtherMonth: false, isToday: true, plans: [] })
  }

  const selectDate = (day: CalendarDay) => {
    setSelectedDate(day.date)
    onSelectDate?.(day.date) // 触发父组件的选中日期事件
  }

  // ============================================================================
  // // 数据加载（Promise.all 并发请求）
  // ============================================================================
  /**
   * 2. 【异步数据流设计：并发网络请求 (Concurrent Requests)】
   *    - 场景：进入月历时，我们需要呈现每一天格子里包含的任务预览。
   *    - 挑战：后端并没有提供“一键获取整月每日任务数组哈希表”的接口，仅有按天获取列表的 `list(date)`。
   *    - 解决方案：
   *      a. 计算出当月第一天到最后一天，循环为每一天构建一个 API 请求 Promise 实例。
   *      b. 使用 `Promise.all(promises)`：让浏览器**并发**发起这 30 个网络请求，而不是排队等待。
   *      c. 待全部请求返回后，统一整理并更新到 `calendarPlans` 状态中。
   */
  const loadMonthPlans = async () => {
    setLoadingPlans(true)
    try {
      const startDate = currentMonth.startOf('month').format('YYYY-MM-DD')
      const endDate = currentMonth.endOf('month').format('YYYY-MM-DD')

      const promises: Promise<PlanItem[]>[] = []
      let current = dayjs(startDate)
      
      // 循环构建每日请求 Promise
      while (!current.isAfter(dayjs(endDate))) {
        promises.push(planApi.list(current.format('YYYY-MM-DD')))
        current = current.add(1, 'day')
      }

      // 并发请求
      const results = await Promise.all(promises)
      
      // 整合为以日期为 key 的哈希映射对象
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
      console.error('批量加载当月计划任务失败:', error)
    } finally {
      setLoadingPlans(false)
    }
  }

  // ============================================================================
  // // 交互处理
  // ============================================================================
  const handlePlanClick = (plan: PlanItem, e: React.MouseEvent) => {
    // 3. 阻止事件冒泡：防止点击任务芯片时，触发外层日历格子的 selectDate 选中动作
    e.stopPropagation()
    setSelectedPlan(plan)
    setShowPlanDialog(true)
  }

  const handleCreatePlan = () => {
    onSelectDate?.(selectedDate)
    Modal.info({ title: '提示', content: '请在右侧/下方“任务列表”中直接创建新任务' })
  }

  const showMorePlans = (day: CalendarDay) => {
    selectDate(day)
  }

  const editPlan = () => {
    onSelectDate?.(selectedDate)
    setShowPlanDialog(false)
    Modal.info({ title: '提示', content: '请在右侧/下方“任务列表”中直接双击编辑任务' })
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
          message.success('删除成功')
          setShowPlanDialog(false)
          setSelectedPlan(null)
          loadMonthPlans() // 刷新当月日历
        } catch (error) {
          console.error('删除任务失败', error)
        }
      }
    })
  }

  // ============================================================================
  // // 生命周期
  // ============================================================================
  
  // 依赖项为 [currentMonth]，意味着每次切换月份时，都会重新拉取新月份的所有任务
  useEffect(() => {
    loadMonthPlans()
  }, [currentMonth])

  // ============================================================================
  // // 渲染
  // ============================================================================
  return (
    <div className="calendar-view">
      {/* 头部切换 */}
      <div className="calendar-header">
        <Button.Group>
          <Button icon={<LeftOutlined />} onClick={prevMonth}>上个月</Button>
          <Button icon={<RightOutlined />} onClick={nextMonth}>下个月</Button>
        </Button.Group>
        <div className="current-month">{currentMonthYear}</div>
        <Button onClick={goToToday}>今天</Button>
      </div>

      {/* 日历主体 */}
      <div className="calendar-grid">
        <div className="week-header">
          {weekDays.map(day => (
            <div key={day} className="weekday">{day}</div>
          ))}
        </div>

        <div className="calendar-body">
          {calendarDays.map((day, idx) => (
            <div
              key={idx}
              className={`calendar-day ${day.isOtherMonth ? 'other-month' : ''} ${day.isToday ? 'today' : ''} ${selectedDate === day.date ? 'selected' : ''}`}
              onClick={() => selectDate(day)}
            >
              <div className="day-number">{day.day}</div>
              <div className="day-plans">
                {/* 每日格子空间有限，最多呈现 3 个任务片预览 */}
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
                {/* 超过 3 个任务时，显示“更多”指示器 */}
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

      {/* 选中日期的任务清单详情列表 */}
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

      {/* 任务详细属性展示 Modal 弹窗 */}
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
                    {/* 子任务进度条计算与内联百分比宽度绑定 */}
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
/**
 * ============================================================================
 * PlanAnalytics.tsx — 计划数据统计组件
 * ============================================================================
 *
 * 【组件说明】
 * 使用 ECharts 展示计划完成情况的多种图表。
 *
 * 【主要功能】
 * - 完成率趋势折线图
 * - 分类分布饼图
 * - 优先级分布柱状图
 * - 时间分配雷达图
 * - 快速统计卡片
 * ============================================================================
 */

import React, { useState, useEffect, useRef } from 'react'
import { Radio, Card, Row, Col, Spin } from 'antd'
import * as echarts from 'echarts/core'
import {
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent
} from 'echarts/components'
import { LineChart, PieChart, BarChart, RadarChart } from 'echarts/charts'
import { CanvasRenderer } from 'echarts/renderers'
import ReactECharts from 'echarts-for-react'
import dayjs from 'dayjs'
import { planApi } from '@/api/plan'
import './PlanAnalytics.css'

// 注册 ECharts 组件
echarts.use([
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent,
  LineChart,
  PieChart,
  BarChart,
  RadarChart,
  CanvasRenderer
])

type TimeRange = 'week' | 'month' | 'year'

interface QuickStats {
  totalPlans: number
  completedPlans: number
  totalHours: number
  avgCompletionRate: number
}

const categories = [
  { value: 'WORK', label: '工作', color: '#6366f1' },
  { value: 'STUDY', label: '学习', color: '#10b981' },
  { value: 'LIFE', label: '生活', color: '#f59e0b' },
  { value: 'HEALTH', label: '健康', color: '#ef4444' }
]

const priorities = [
  { value: 'P0', label: 'P0 紧急', color: '#ef4444' },
  { value: 'P1', label: 'P1 重要', color: '#f97316' },
  { value: 'P2', label: 'P2 普通', color: '#3b82f6' },
  { value: 'P3', label: 'P3 低优', color: '#6b7280' }
]

const PlanAnalytics: React.FC = () => {
  // ============================================================================
  // // 状态
  // ============================================================================
  const [timeRange, setTimeRange] = useState<TimeRange>('week')
  const [quickStats, setQuickStats] = useState<QuickStats>({
    totalPlans: 0,
    completedPlans: 0,
    totalHours: 0,
    avgCompletionRate: 0
  })
  const [trendData, setTrendData] = useState<any[]>([])
  const [categoryData, setCategoryData] = useState<any>({})
  const [priorityData, setPriorityData] = useState<any>({})
  const [timeData, setTimeData] = useState<any>({})
  const [loading, setLoading] = useState(false)

  // ============================================================================
  // // 辅助函数
  // ============================================================================
  const getDateRange = () => {
    const now = dayjs()
    let startDate: dayjs.Dayjs
    const endDate = now

    switch (timeRange) {
      case 'week':
        startDate = now.startOf('week')
        break
      case 'month':
        startDate = now.startOf('month')
        break
      case 'year':
        startDate = now.startOf('year')
        break
      default:
        startDate = now.startOf('week')
    }

    return {
      startDate: startDate.format('YYYY-MM-DD'),
      endDate: endDate.format('YYYY-MM-DD')
    }
  }

  // ============================================================================
  // // 数据加载
  // ============================================================================
  const loadAnalytics = async () => {
    setLoading(true)
    try {
      const { startDate, endDate } = getDateRange()

      const [trend, category, priority, time] = await Promise.all([
        planApi.getCompletionTrend(startDate, endDate),
        planApi.getCategoryDistribution(startDate, endDate),
        planApi.getPriorityDistribution(startDate, endDate),
        planApi.getTimeDistribution(startDate, endDate)
      ])

      // 更新快速统计
      setQuickStats({
        totalPlans: trend.reduce((sum: number, t: any) => sum + t.total, 0),
        completedPlans: trend.reduce((sum: number, t: any) => sum + t.done, 0),
        totalHours: Math.round((time.total || 0) / 60),
        avgCompletionRate: Math.round(
          trend.reduce((sum: number, t: any) => sum + t.completionRate, 0) / (trend.length || 1)
        )
      })

      // 更新图表数据
      setTrendData(trend)
      setCategoryData(category)
      setPriorityData(priority)
      setTimeData(time)
    } catch (error) {
      console.error('Failed to load analytics', error)
    } finally {
      setLoading(false)
    }
  }

  // ============================================================================
  // // 图表配置
  // ============================================================================
  const getTrendChartOption = (): echarts.EChartsCoreOption => ({
    tooltip: {
      trigger: 'axis'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: trendData.map(d => dayjs(d.date).format('MM-DD')),
      axisLine: { lineStyle: { color: '#64748b' } },
      axisLabel: { color: '#94a3b8' }
    },
    yAxis: {
      type: 'value',
      max: 100,
      axisLine: { lineStyle: { color: '#64748b' } },
      axisLabel: {
        color: '#94a3b8',
        formatter: '{value}%'
      },
      splitLine: { lineStyle: { color: 'rgba(255,255,255,0.05)' } }
    },
    series: [
      {
        name: '完成率',
        type: 'line',
        smooth: true,
        data: trendData.map(d => d.completionRate),
        lineStyle: { color: '#6366f1', width: 3 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(99, 102, 241, 0.3)' },
            { offset: 1, color: 'rgba(99, 102, 241, 0)' }
          ])
        },
        itemStyle: { color: '#6366f1' }
      }
    ]
  })

  const getCategoryChartOption = (): echarts.EChartsCoreOption => {
    const chartData = categories.map(cat => ({
      name: cat.label,
      value: categoryData.data?.[cat.value] || 0,
      itemStyle: { color: cat.color }
    }))

    return {
      tooltip: {
        trigger: 'item',
        formatter: '{b}: {c} ({d}%)'
      },
      legend: {
        orient: 'vertical',
        right: 10,
        top: 'center',
        textStyle: { color: '#94a3b8' }
      },
      series: [
        {
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 6,
            borderColor: '#1e293b',
            borderWidth: 2
          },
          label: { show: false },
          emphasis: {
            label: {
              show: true,
              fontSize: 14,
              fontWeight: 'bold',
              color: '#fff'
            }
          },
          data: chartData
        }
      ]
    }
  }

  const getPriorityChartOption = (): echarts.EChartsCoreOption => {
    const chartData = priorities.map(pri => ({
      name: pri.label,
      value: priorityData.data?.[pri.value] || 0,
      itemStyle: { color: pri.color }
    }))

    return {
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' }
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'value',
        axisLine: { lineStyle: { color: '#64748b' } },
        axisLabel: { color: '#94a3b8' },
        splitLine: { lineStyle: { color: 'rgba(255,255,255,0.05)' } }
      },
      yAxis: {
        type: 'category',
        data: priorities.map(p => p.label),
        axisLine: { lineStyle: { color: '#64748b' } },
        axisLabel: { color: '#94a3b8' }
      },
      series: [
        {
          type: 'bar',
          data: chartData,
          barWidth: '60%',
          itemStyle: {
            borderRadius: [0, 4, 4, 0]
          }
        }
      ]
    }
  }

  const getTimeChartOption = (): echarts.EChartsCoreOption => {
    const chartData = categories.map(cat => timeData.data?.[cat.value] || 0)
    const maxValue = Math.max(...chartData, 1)

    return {
      tooltip: {
        trigger: 'axis'
      },
      radar: {
        indicator: categories.map(cat => ({
          name: cat.label,
          max: maxValue * 1.2
        })),
        shape: 'circle',
        splitNumber: 4,
        axisName: {
          color: '#94a3b8'
        },
        splitLine: {
          lineStyle: {
            color: 'rgba(255,255,255,0.1)'
          }
        },
        splitArea: {
          show: false
        },
        axisLine: {
          lineStyle: {
            color: 'rgba(255,255,255,0.1)'
          }
        }
      },
      series: [
        {
          type: 'radar',
          data: [
            {
              value: chartData,
              name: '时间分配',
              areaStyle: {
                color: 'rgba(99, 102, 241, 0.3)'
              },
              lineStyle: {
                color: '#6366f1',
                width: 2
              },
              itemStyle: {
                color: '#6366f1'
              }
            }
          ]
        }
      ]
    }
  }

  // ============================================================================
  // // 生命周期
  // ============================================================================
  useEffect(() => {
    loadAnalytics()
  }, [timeRange])

  // ============================================================================
  // // 渲染
  // ============================================================================
  return (
    <Spin spinning={loading}>
      <div className="plan-analytics">
        {/* 时间维度切换 */}
        <div className="time-selector">
          <Radio.Group value={timeRange} onChange={e => setTimeRange(e.target.value)} size="small">
            <Radio.Button value="week">本周</Radio.Button>
            <Radio.Button value="month">本月</Radio.Button>
            <Radio.Button value="year">本年</Radio.Button>
          </Radio.Group>
        </div>

        {/* 快速统计卡片 */}
        <div className="quick-stats">
          <div className="stat-card">
            <div className="stat-icon" style={{ background: 'rgba(99, 102, 241, 0.1)' }}>📊</div>
            <div className="stat-content">
              <div className="stat-value">{quickStats.totalPlans}</div>
              <div className="stat-label">总任务数</div>
            </div>
          </div>
          <div className="stat-card">
            <div className="stat-icon" style={{ background: 'rgba(16, 185, 129, 0.1)' }}>✅</div>
            <div className="stat-content">
              <div className="stat-value">{quickStats.completedPlans}</div>
              <div className="stat-label">已完成</div>
            </div>
          </div>
          <div className="stat-card">
            <div className="stat-icon" style={{ background: 'rgba(245, 158, 11, 0.1)' }}>⏱</div>
            <div className="stat-content">
              <div className="stat-value">{quickStats.totalHours}h</div>
              <div className="stat-label">总用时</div>
            </div>
          </div>
          <div className="stat-card">
            <div className="stat-icon" style={{ background: 'rgba(239, 68, 68, 0.1)' }}>📈</div>
            <div className="stat-content">
              <div className="stat-value">{quickStats.avgCompletionRate}%</div>
              <div className="stat-label">平均完成率</div>
            </div>
          </div>
        </div>

        {/* 图表区域 */}
        <div className="charts-grid">
          {/* 完成率趋势 */}
          <div className="chart-card">
            <div className="chart-header">
              <h3>完成率趋势</h3>
            </div>
            <div className="chart-container">
              <ReactECharts option={getTrendChartOption()} style={{ height: '100%' }} />
            </div>
          </div>

          {/* 分类分布 */}
          <div className="chart-card">
            <div className="chart-header">
              <h3>分类分布</h3>
            </div>
            <div className="chart-container">
              <ReactECharts option={getCategoryChartOption()} style={{ height: '100%' }} />
            </div>
          </div>

          {/* 优先级分布 */}
          <div className="chart-card">
            <div className="chart-header">
              <h3>优先级分布</h3>
            </div>
            <div className="chart-container">
              <ReactECharts option={getPriorityChartOption()} style={{ height: '100%' }} />
            </div>
          </div>

          {/* 时间分配 */}
          <div className="chart-card">
            <div className="chart-header">
              <h3>时间分配</h3>
            </div>
            <div className="chart-container">
              <ReactECharts option={getTimeChartOption()} style={{ height: '100%' }} />
            </div>
          </div>
        </div>
      </div>
    </Spin>
  )
}

export default PlanAnalytics

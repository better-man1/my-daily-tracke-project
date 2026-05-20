import React, { useState, useEffect } from 'react'
import { Row, Col, Card, Statistic, Radio, Spin } from 'antd'
// 导入各种图形化展示所需的图标
import {
  ArrowUpOutlined,
  ArrowDownOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  WalletOutlined,
  BookOutlined,
  EditOutlined,
  TrophyOutlined
} from '@ant-design/icons'
// 导入 ECharts 图表组件库，用于将普通 DOM 图表库（ECharts）转换成响应式的 React 组件
import ReactECharts from 'echarts-for-react'
import { dashboardApi } from '@/api/dashboard'

/**
 * ============================================================================
 * 【数据看板页面组件 (src/pages/dashboard/DashboardPage.tsx)】
 * ============================================================================
 *
 * 【知识点解析：0-1 学习 React】
 *
 * 1. 【React 核心概念：基于依赖项的数据获取 (useEffect with Dependencies)】
 *    - 在开发 Web 应用时，经常需要“用户切换筛选条件，页面数据立即重新拉取并更新”。
 *    - 传统开发中，我们需要在单选框的 `onChange` 回调里手动调用一次 `fetchData()`。
 *    - React 推荐使用【声明式副作用控制】模型：
 *      - 我们声明一个状态 `timeRange`（表示当前时间维度：今天/本周/本月/本年）。
 *      - 我们声明一个 `useEffect`，并将 `timeRange` 放入它的依赖项数组中：`}, [timeRange])`。
 *      - **React 副作用规则**：一旦依赖项数组里的任何值发生改变，React 就会自动重新执行 `useEffect` 内部的回调函数。
 *      - 这样，不论是初始化挂载时，还是后续用户点击 Radio 按钮修改了 `timeRange`，`fetchData()` 都会被自动且稳妥地触发，无需手动在各种事件中织入网络请求。
 *
 * 2. 【React 核心概念：在 React 中集成非 React 组件（如 ECharts）】
 *    - ECharts 属于直接操作真实 DOM 的经典库，而 React 是通过虚拟 DOM 驱动的，两者的结合需要一个桥梁（如 `echarts-for-react`）。
 *    - 原理：`ReactECharts` 组件内部会在首次加载时通过 `ref` 获取底层的真实 DOM 节点并初始化 ECharts 实例。
 *    - 数据流动：我们编写配置生成函数（如 `getPlanTrendOption`），它读取组件 state 中的 `data`。
 *      - 当网络请求成功，我们调用 `setData(response)`。
 *      - React 监听到数据变动，触发 `DashboardPage` 重新渲染。
 *      - 重新渲染时，`getPlanTrendOption` 计算出包含最新数据的 option 对象，并作为属性传给 `<ReactECharts option={...} />`。
 *      - `ReactECharts` 检测到 option 的引用或内容变了，在底层调用 `chart.setOption(...)` 更新图表。这就实现了“ECharts 响应式”。
 *
 * 3. 什么是可选链操作符 (Optional Chaining `?.`)？
 *    - 在数据获取成功前，`data` 状态默认是 `null`。
 *    - 如果直接调用 `data.planTrend`，JS 会抛出 `Cannot read properties of null` 的严重错误，导致页面白屏崩溃。
 *    - 使用 `data?.planTrend`，当 `data` 为 `null` 或 `undefined` 时，它会安全地返回 `undefined` 而不会报错，
 *      我们再配合 `|| []` 即可进行安全降级，这是 React 渲染异步数据时的必备防崩溃技能。
 */
const DashboardPage: React.FC = () => {
  // 控制当前页面的全局数据加载状态
  const [loading, setLoading] = useState(false)
  // 时间筛选状态：今日/本周/本月/本年，初始为“今日”
  const [timeRange, setTimeRange] = useState<'today' | 'week' | 'month' | 'year'>('today')
  // 看板核心统计数据存储状态，接口未返回前默认为 null
  const [data, setData] = useState<any>(null)

  // 依赖项数组中包含 timeRange，一旦它被修改，下面的回调函数就会执行
  useEffect(() => {
    fetchData()
  }, [timeRange])

  /**
   * 根据当前的时间维度 timeRange，拉取数据看板各项指标
   */
  const fetchData = async () => {
    try {
      setLoading(true) // 开启 Loading，给用户一个良好的等待视觉反馈
      let response
      // 根据状态动态匹配 API 接口方法
      switch (timeRange) {
        case 'today':
          response = await dashboardApi.getToday()
          break
        case 'week':
          response = await dashboardApi.getWeek()
          break
        case 'month':
          response = await dashboardApi.getMonth()
          break
        case 'year':
          response = await dashboardApi.getYear()
          break
      }
      // 保存接口返回的数据到状态，触发页面视图重绘
      setData(response)
    } catch (error) {
      console.error('获取数据失败:', error)
    } finally {
      setLoading(false) // 无论成败，关闭 Loading 遮罩
    }
  }

  /**
   * 动态生成计划完成趋势折线图的配置对象
   */
  const getPlanTrendOption = () => ({
    tooltip: {
      trigger: 'axis' // 鼠标悬浮在折线上时显示纵向辅助指示线及气泡提示
    },
    legend: {
      data: ['计划数', '完成数']
    },
    xAxis: {
      type: 'category',
      // 使用可选链安全提取 date 数据
      data: data?.planTrend?.map((item: any) => item.date) || []
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '计划数',
        type: 'line',
        data: data?.planTrend?.map((item: any) => item.total) || [],
        smooth: true // 开启平滑曲线
      },
      {
        name: '完成数',
        type: 'line',
        data: data?.planTrend?.map((item: any) => item.completed) || [],
        smooth: true
      }
    ]
  })

  /**
   * 动态生成收支对比柱状图的配置对象
   */
  const getAccountingTrendOption = () => ({
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['收入', '支出']
    },
    xAxis: {
      type: 'category',
      data: data?.accountingTrend?.map((item: any) => item.date) || []
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '收入',
        type: 'bar',
        data: data?.accountingTrend?.map((item: any) => item.income) || [],
        itemStyle: { color: '#52c41a' } // 绿色代表收入
      },
      {
        name: '支出',
        type: 'bar',
        data: data?.accountingTrend?.map((item: any) => item.expense) || [],
        itemStyle: { color: '#ff4d4f' } // 红色代表支出
      }
    ]
  })

  return (
    <div>
      {/* 头部标题与单选筛选区 */}
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h2>数据看板</h2>
        {/* Antd Radio.Group 双向绑定 timeRange 状态，点击时调用 setTimeRange 更新 */}
        <Radio.Group value={timeRange} onChange={(e) => setTimeRange(e.target.value)}>
          <Radio.Button value="today">今日</Radio.Button>
          <Radio.Button value="week">本周</Radio.Button>
          <Radio.Button value="month">本月</Radio.Button>
          <Radio.Button value="year">本年</Radio.Button>
        </Radio.Group>
      </div>

      {/* 使用 Spin 组件包裹数据展示区域，spinning 传入 loading 状态以实现动态数据加载遮罩 */}
      <Spin spinning={loading}>
        {/* 第一行统计卡片 */}
        <Row gutter={16} style={{ marginBottom: 16 }}>
          <Col span={6}>
            <Card>
              <Statistic
                title="总计划数"
                value={data?.planStats?.total || 0}
                prefix={<ClockCircleOutlined />}
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card>
              <Statistic
                title="已完成"
                value={data?.planStats?.completed || 0}
                prefix={<CheckCircleOutlined />}
                valueStyle={{ color: '#3f8600' }}
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card>
              <Statistic
                title="总收入"
                value={data?.accountingStats?.totalIncome || 0}
                precision={2} // 保留两位小数
                prefix={<ArrowUpOutlined />}
                suffix="元"
                valueStyle={{ color: '#3f8600' }}
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card>
              <Statistic
                title="总支出"
                value={data?.accountingStats?.totalExpense || 0}
                precision={2}
                prefix={<ArrowDownOutlined />}
                suffix="元"
                valueStyle={{ color: '#cf1322' }}
              />
            </Card>
          </Col>
        </Row>

        {/* 第二行统计卡片 */}
        <Row gutter={16} style={{ marginBottom: 16 }}>
          <Col span={6}>
            <Card>
              <Statistic
                title="摘录数量"
                value={data?.excerptCount || 0}
                prefix={<BookOutlined />}
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card>
              <Statistic
                title="总结天数"
                value={data?.summaryCount || 0}
                prefix={<EditOutlined />}
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card>
              <Statistic
                title="目标进度"
                value={data?.goalProgress || 0}
                precision={1}
                suffix="%"
                prefix={<TrophyOutlined />}
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card>
              {/* 净收支：计算公式（收入 - 支出），根据结果正负，动态展示红绿颜色样式 */}
              <Card>
                <Statistic
                  title="净收支"
                  value={(data?.accountingStats?.totalIncome || 0) - (data?.accountingStats?.totalExpense || 0)}
                  precision={2}
                  prefix={<WalletOutlined />}
                  suffix="元"
                  valueStyle={{
                    color: (data?.accountingStats?.totalIncome || 0) >= (data?.accountingStats?.totalExpense || 0)
                      ? '#3f8600'
                      : '#cf1322'
                  }}
                />
              </Card>
            </Card>
          </Col>
        </Row>

        {/* 图表展示区：将两套 ECharts 图表并排渲染 */}
        <Row gutter={16}>
          <Col span={12}>
            <Card title="计划完成趋势" bordered={false}>
              {/* 注入折线图配置 */}
              <ReactECharts option={getPlanTrendOption()} style={{ height: 300 }} />
            </Card>
          </Col>
          <Col span={12}>
            <Card title="收支趋势" bordered={false}>
              {/* 注入柱状图配置 */}
              <ReactECharts option={getAccountingTrendOption()} style={{ height: 300 }} />
            </Card>
          </Col>
        </Row>
      </Spin>
    </div>
  )
}

export default DashboardPage
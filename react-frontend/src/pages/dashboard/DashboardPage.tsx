import React, { useState, useEffect } from 'react'
import { Row, Col, Card, Statistic, Radio, Spin } from 'antd'
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
import ReactECharts from 'echarts-for-react'
import { dashboardApi } from '@/api/dashboard'

/**
 * DashboardPage — 数据看板页面
 */
const DashboardPage: React.FC = () => {
  const [loading, setLoading] = useState(false)
  const [timeRange, setTimeRange] = useState<'today' | 'week' | 'month' | 'year'>('today')
  const [data, setData] = useState<any>(null)

  useEffect(() => {
    fetchData()
  }, [timeRange])

  const fetchData = async () => {
    try {
      setLoading(true)
      let response
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
      setData(response)
    } catch (error) {
      console.error('获取数据失败:', error)
    } finally {
      setLoading(false)
    }
  }

  // 计划完成趋势图配置
  const getPlanTrendOption = () => ({
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['计划数', '完成数']
    },
    xAxis: {
      type: 'category',
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
        smooth: true
      },
      {
        name: '完成数',
        type: 'line',
        data: data?.planTrend?.map((item: any) => item.completed) || [],
        smooth: true
      }
    ]
  })

  // 收支趋势图配置
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
        itemStyle: { color: '#52c41a' }
      },
      {
        name: '支出',
        type: 'bar',
        data: data?.accountingTrend?.map((item: any) => item.expense) || [],
        itemStyle: { color: '#ff4d4f' }
      }
    ]
  })

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h2>数据看板</h2>
        <Radio.Group value={timeRange} onChange={(e) => setTimeRange(e.target.value)}>
          <Radio.Button value="today">今日</Radio.Button>
          <Radio.Button value="week">本周</Radio.Button>
          <Radio.Button value="month">本月</Radio.Button>
          <Radio.Button value="year">本年</Radio.Button>
        </Radio.Group>
      </div>

      <Spin spinning={loading}>
        {/* 统计卡片 */}
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
                precision={2}
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
          </Col>
        </Row>

        {/* 图表区域 */}
        <Row gutter={16}>
          <Col span={12}>
            <Card title="计划完成趋势" bordered={false}>
              <ReactECharts option={getPlanTrendOption()} style={{ height: 300 }} />
            </Card>
          </Col>
          <Col span={12}>
            <Card title="收支趋势" bordered={false}>
              <ReactECharts option={getAccountingTrendOption()} style={{ height: 300 }} />
            </Card>
          </Col>
        </Row>
      </Spin>
    </div>
  )
}

export default DashboardPage
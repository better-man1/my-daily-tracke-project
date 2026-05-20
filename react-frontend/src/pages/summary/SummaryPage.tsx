import React, { useState, useEffect } from 'react'
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons'
import { Table, Button, Modal, Form, Input, Rate, DatePicker, Space, message, Popconfirm, Card, Row, Col, Statistic } from 'antd'
import type { ColumnsType } from 'antd/es/table'
import type { Dayjs } from 'dayjs'
import dayjs from 'dayjs'
import { summaryApi, type SummaryItem, type CreateSummaryRequest } from '@/api/summary'

const { TextArea } = Input

/**
 * SummaryPage — 每日总结页面
 */
const SummaryPage: React.FC = () => {
  const [loading, setLoading] = useState(false)
  const [summaries, setSummaries] = useState<SummaryItem[]>([])
  const [modalVisible, setModalVisible] = useState(false)
  const [editingSummary, setEditingSummary] = useState<SummaryItem | null>(null)
  const [selectedDate, setSelectedDate] = useState<Dayjs>(dayjs())
  const [streak, setStreak] = useState<{ currentStreak: number; longestStreak: number } | null>(null)
  const [form] = Form.useForm()

  useEffect(() => {
    fetchSummaries()
    fetchStreak()
  }, [selectedDate])

  const fetchSummaries = async () => {
    try {
      setLoading(true)
      const startDate = selectedDate.startOf('month').format('YYYY-MM-DD')
      const endDate = selectedDate.endOf('month').format('YYYY-MM-DD')
      const data = await summaryApi.list({ startDate, endDate })
      setSummaries(data)
    } catch (error) {
      console.error('获取总结失败:', error)
    } finally {
      setLoading(false)
    }
  }

  const fetchStreak = async () => {
    try {
      const data = await summaryApi.getStreak()
      setStreak(data)
    } catch (error) {
      console.error('获取打卡数据失败:', error)
    }
  }

  const handleAdd = () => {
    setEditingSummary(null)
    form.resetFields()
    form.setFieldsValue({
      summaryDate: dayjs(),
      mood: 3,
      score: 75
    })
    setModalVisible(true)
  }

  const handleEdit = (record: SummaryItem) => {
    setEditingSummary(record)
    form.setFieldsValue({
      ...record,
      summaryDate: dayjs(record.summaryDate)
    })
    setModalVisible(true)
  }

  const handleDelete = async (id: number) => {
    try {
      await summaryApi.delete(id)
      message.success('删除成功')
      fetchSummaries()
    } catch (error) {
      console.error('删除失败:', error)
    }
  }

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields()
      const data: CreateSummaryRequest = {
        ...values,
        summaryDate: values.summaryDate.format('YYYY-MM-DD')
      }

      if (editingSummary) {
        await summaryApi.update(editingSummary.id, data)
        message.success('更新成功')
      } else {
        await summaryApi.create(data)
        message.success('创建成功')
      }

      setModalVisible(false)
      fetchSummaries()
      fetchStreak()
    } catch (error) {
      console.error('提交失败:', error)
    }
  }

  const columns: ColumnsType<SummaryItem> = [
    {
      title: '日期',
      dataIndex: 'summaryDate',
      key: 'summaryDate',
      width: 120
    },
    {
      title: '心情',
      dataIndex: 'mood',
      key: 'mood',
      width: 150,
      render: (mood) => <Rate disabled value={mood} />
    },
    {
      title: '综合评分',
      dataIndex: 'score',
      key: 'score',
      width: 120,
      render: (score) => (
        <span style={{ fontWeight: 'bold', color: score >= 80 ? '#52c41a' : score >= 60 ? '#faad14' : '#ff4d4f' }}>
          {score}
        </span>
      )
    },
    {
      title: '今日成就',
      dataIndex: 'achievement',
      key: 'achievement',
      ellipsis: true
    },
    {
      title: '改进空间',
      dataIndex: 'improvement',
      key: 'improvement',
      ellipsis: true
    },
    {
      title: '操作',
      key: 'action',
      width: 150,
      render: (_, record) => (
        <Space size="small">
          <Button
            type="link"
            size="small"
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
          />
          <Popconfirm
            title="确定删除？"
            onConfirm={() => handleDelete(record.id)}
            okText="确定"
            cancelText="取消"
          >
            <Button type="link" size="small" danger icon={<DeleteOutlined />} />
          </Popconfirm>
        </Space>
      )
    }
  ]

  const moodEmojis = ['😞', '😐', '🙂', '😊', '🤩']

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h2>每日总结</h2>
        <Space>
          <DatePicker.MonthPicker
            value={selectedDate}
            onChange={(date) => date && setSelectedDate(date)}
          />
          <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
            新建总结
          </Button>
        </Space>
      </div>

      {/* 打卡统计 */}
      {streak && (
        <Row gutter={16} style={{ marginBottom: 16 }}>
          <Col span={12}>
            <Card>
              <Statistic
                title="当前连续打卡"
                value={streak.currentStreak}
                suffix="天"
                valueStyle={{ color: '#3f8600' }}
              />
            </Card>
          </Col>
          <Col span={12}>
            <Card>
              <Statistic
                title="最长连续打卡"
                value={streak.longestStreak}
                suffix="天"
                valueStyle={{ color: '#1890ff' }}
              />
            </Card>
          </Col>
        </Row>
      )}

      <Table
        columns={columns}
        dataSource={summaries}
        rowKey="id"
        loading={loading}
        pagination={false}
        expandable={{
          expandedRowRender: (record) => (
            <div style={{ padding: 16 }}>
              <Row gutter={16}>
                <Col span={12}>
                  <Card title="今日成就" size="small">
                    {record.achievement || '暂无'}
                  </Card>
                </Col>
                <Col span={12}>
                  <Card title="改进空间" size="small">
                    {record.improvement || '暂无'}
                  </Card>
                </Col>
              </Row>
              <Row gutter={16} style={{ marginTop: 16 }}>
                <Col span={12}>
                  <Card title="明日计划" size="small">
                    {record.tomorrowPlan || '暂无'}
                  </Card>
                </Col>
                <Col span={12}>
                  <Card title="感恩事项" size="small">
                    {record.gratitude || '暂无'}
                  </Card>
                </Col>
              </Row>
              {record.healthNote && (
                <Card title="健康记录" size="small" style={{ marginTop: 16 }}>
                  {record.healthNote}
                </Card>
              )}
            </div>
          )
        }}
      />

      <Modal
        title={editingSummary ? '编辑总结' : '新建总结'}
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => setModalVisible(false)}
        width={700}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="summaryDate"
            label="日期"
            rules={[{ required: true, message: '请选择日期' }]}
          >
            <DatePicker style={{ width: '100%' }} />
          </Form.Item>

          <Form.Item name="mood" label="心情" rules={[{ required: true, message: '请选择心情' }]}>
            <Rate character={({ index = 0 }) => moodEmojis[index]} />
          </Form.Item>

          <Form.Item name="score" label="综合评分" rules={[{ required: true, message: '请输入综合评分' }]}>
            <Input type="number" min={0} max={100} placeholder="0-100" />
          </Form.Item>

          <Form.Item name="achievement" label="今日成就">
            <TextArea rows={2} placeholder="今天完成了什么有价值的事情？" />
          </Form.Item>

          <Form.Item name="improvement" label="改进空间">
            <TextArea rows={2} placeholder="今天有什么可以改进的地方？" />
          </Form.Item>

          <Form.Item name="tomorrowPlan" label="明日计划">
            <TextArea rows={2} placeholder="明天计划做什么？" />
          </Form.Item>

          <Form.Item name="gratitude" label="感恩事项">
            <TextArea rows={2} placeholder="今天有什么值得感恩的事情？" />
          </Form.Item>

          <Form.Item name="healthNote" label="健康记录">
            <TextArea rows={2} placeholder="运动、饮食、睡眠等记录" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default SummaryPage

import React, { useState, useEffect } from 'react'
import { PlusOutlined, EditOutlined, DeleteOutlined, TrophyOutlined } from '@ant-design/icons'
import { Tree, Button, Modal, Form, Input, Select, DatePicker, Space, message, Popconfirm, Card, Row, Col, Statistic, Progress } from 'antd'
import type { DataNode } from 'antd/es/tree'
import type { Dayjs } from 'dayjs'
import dayjs from 'dayjs'
import { goalApi, type GoalItem, type CreateGoalRequest } from '@/api/goal'

const { TextArea } = Input
const { Option } = Select

/**
 * GoalPage — 目标管理页面
 */
const GoalPage: React.FC = () => {
  const [loading, setLoading] = useState(false)
  const [goals, setGoals] = useState<GoalItem[]>([])
  const [modalVisible, setModalVisible] = useState(false)
  const [editingGoal, setEditingGoal] = useState<GoalItem | null>(null)
  const [form] = Form.useForm()
  const [statistics, setStatistics] = useState<any>(null)

  useEffect(() => {
    fetchGoals()
    fetchStatistics()
  }, [])

  const fetchGoals = async () => {
    try {
      setLoading(true)
      const data = await goalApi.getTree()
      setGoals(data)
    } catch (error) {
      console.error('获取目标失败:', error)
    } finally {
      setLoading(false)
    }
  }

  const fetchStatistics = async () => {
    try {
      const data = await goalApi.getStatistics()
      setStatistics(data)
    } catch (error) {
      console.error('获取统计数据失败:', error)
    }
  }

  const convertToTreeData = (goals: GoalItem[]): DataNode[] => {
    return goals.map(goal => ({
      title: (
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <span>
            <span style={{ marginRight: 8 }}>{goal.title}</span>
            <span style={{ fontSize: 12, color: '#999' }}>
              {goal.status === 'COMPLETED' && '✓ '}
              {goal.progress}%
            </span>
          </span>
          <Space size="small">
            <Button
              type="link"
              size="small"
              icon={<EditOutlined />}
              onClick={(e) => {
                e.stopPropagation()
                handleEdit(goal)
              }}
            />
            <Popconfirm
              title="确定删除？"
              onConfirm={(e) => {
                e?.stopPropagation()
                handleDelete(goal.id)
              }}
              okText="确定"
              cancelText="取消"
            >
              <Button
                type="link"
                size="small"
                danger
                icon={<DeleteOutlined />}
                onClick={(e) => e.stopPropagation()}
              />
            </Popconfirm>
          </Space>
        </div>
      ),
      key: goal.id,
      children: goal.children ? convertToTreeData(goal.children) : undefined
    }))
  }

  const handleAdd = (parentId: number | null = null) => {
    setEditingGoal(null)
    form.resetFields()
    if (parentId) {
      form.setFieldsValue({ parentId })
    }
    setModalVisible(true)
  }

  const handleEdit = (goal: GoalItem) => {
    setEditingGoal(goal)
    form.setFieldsValue({
      ...goal,
      startDate: dayjs(goal.startDate),
      targetDate: dayjs(goal.targetDate)
    })
    setModalVisible(true)
  }

  const handleDelete = async (id: number) => {
    try {
      await goalApi.delete(id)
      message.success('删除成功')
      fetchGoals()
      fetchStatistics()
    } catch (error) {
      console.error('删除失败:', error)
    }
  }

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields()
      const data: CreateGoalRequest = {
        ...values,
        startDate: values.startDate.format('YYYY-MM-DD'),
        targetDate: values.targetDate.format('YYYY-MM-DD')
      }

      if (editingGoal) {
        await goalApi.update(editingGoal.id, data)
        message.success('更新成功')
      } else {
        await goalApi.create(data)
        message.success('创建成功')
      }

      setModalVisible(false)
      fetchGoals()
      fetchStatistics()
    } catch (error) {
      console.error('提交失败:', error)
    }
  }

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h2>目标管理</h2>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => handleAdd()}>
          新建目标
        </Button>
      </div>

      {/* 统计卡片 */}
      {statistics && (
        <Row gutter={16} style={{ marginBottom: 16 }}>
          <Col span={6}>
            <Card>
              <Statistic
                title="总目标数"
                value={statistics.totalGoals}
                prefix={<TrophyOutlined />}
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card>
              <Statistic
                title="已完成"
                value={statistics.completedGoals}
                valueStyle={{ color: '#3f8600' }}
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card>
              <Statistic
                title="进行中"
                value={statistics.inProgressGoals}
                valueStyle={{ color: '#1890ff' }}
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card>
              <Statistic
                title="整体进度"
                value={statistics.overallProgress}
                suffix="%"
                precision={1}
              />
            </Card>
          </Col>
        </Row>
      )}

      <Card title="目标树" bordered={false}>
        <Tree
          treeData={convertToTreeData(goals)}
          showLine
          defaultExpandAll
        />
      </Card>

      <Modal
        title={editingGoal ? '编辑目标' : '新建目标'}
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => setModalVisible(false)}
        width={700}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="title"
            label="目标标题"
            rules={[{ required: true, message: '请输入目标标题' }]}
          >
            <Input placeholder="请输入目标标题" />
          </Form.Item>

          <Form.Item name="description" label="目标描述">
            <TextArea rows={3} placeholder="请输入目标描述" />
          </Form.Item>

          <Form.Item
            name="goalType"
            label="目标类型"
            rules={[{ required: true, message: '请选择目标类型' }]}
          >
            <Select>
              <Option value="FIVE_YEAR">五年规划</Option>
              <Option value="YEARLY">年度目标</Option>
              <Option value="MONTHLY">月度目标</Option>
              <Option value="WEEKLY">周目标</Option>
            </Select>
          </Form.Item>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="startDate"
                label="开始日期"
                rules={[{ required: true, message: '请选择开始日期' }]}
              >
                <DatePicker style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="targetDate"
                label="目标日期"
                rules={[{ required: true, message: '请选择目标日期' }]}
              >
                <DatePicker style={{ width: '100%' }} />
              </Form.Item>
            </Col>
          </Row>

          <Form.Item name="progress" label="当前进度" initialValue={0}>
            <Input type="number" min={0} max={100} placeholder="0-100" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default GoalPage

import React, { useState, useEffect } from 'react'
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons'
import { Table, Button, Modal, Form, Input, Select, DatePicker, Space, message, Popconfirm, Tag, Switch, Card, Row, Col, Statistic } from 'antd'
import type { ColumnsType } from 'antd/es/table'
import type { Dayjs } from 'dayjs'
import dayjs from 'dayjs'
import { planApi, type PlanItem, type CreatePlanRequest } from '@/api/plan'
import { DndContext, closestCenter, KeyboardSensor, PointerSensor, useSensor, useSensors } from '@dnd-kit/core'
import { arrayMove, SortableContext, sortableKeyboardCoordinates, useSortable, verticalListSortingStrategy } from '@dnd-kit/sortable'
import { CSS } from '@dnd-kit/utilities'

const { Option } = Select

/**
 * SortableRow — 可拖拽的表格行组件
 */
function SortableRow({ id, children, ...props }: any) {
  const { attributes, listeners, setNodeRef, transform, transition, isDragging } = useSortable({
    id
  })

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
    cursor: isDragging ? 'grabbing' : 'grab'
  }

  return (
    <tr ref={setNodeRef} style={style} {...attributes} {...listeners} {...props}>
      {children}
    </tr>
  )
}

/**
 * PlanPage — 每日计划页面
 */
const PlanPage: React.FC = () => {
  const [loading, setLoading] = useState(false)
  const [plans, setPlans] = useState<PlanItem[]>([])
  const [selectedDate, setSelectedDate] = useState<Dayjs>(dayjs())
  const [modalVisible, setModalVisible] = useState(false)
  const [editingPlan, setEditingPlan] = useState<PlanItem | null>(null)
  const [form] = Form.useForm()
  const [statistics, setStatistics] = useState<any>(null)

  const sensors = useSensors(
    useSensor(PointerSensor),
    useSensor(KeyboardSensor, {
      coordinateGetter: sortableKeyboardCoordinates
    })
  )

  useEffect(() => {
    fetchPlans()
    fetchStatistics()
  }, [selectedDate])

  const fetchPlans = async () => {
    try {
      setLoading(true)
      const dateStr = selectedDate.format('YYYY-MM-DD')
      const data = await planApi.list(dateStr)
      setPlans(data)
    } catch (error) {
      console.error('获取计划失败:', error)
    } finally {
      setLoading(false)
    }
  }

  const fetchStatistics = async () => {
    try {
      const startDate = selectedDate.startOf('month').format('YYYY-MM-DD')
      const endDate = selectedDate.endOf('month').format('YYYY-MM-DD')
      const data = await planApi.getStatistics(startDate, endDate)
      setStatistics(data[data.length - 1]) // 获取最新一天的统计
    } catch (error) {
      console.error('获取统计数据失败:', error)
    }
  }

  const handleDragEnd = async (event: any) => {
    const { active, over } = event
    if (active.id !== over?.id) {
      setPlans((items) => {
        const oldIndex = items.findIndex((item) => item.id === active.id)
        const newIndex = items.findIndex((item) => item.id === over?.id)
        const newItems = arrayMove(items, oldIndex, newIndex)
        // 更新排序
        const reorderData = newItems.map((item, index) => ({ id: item.id, sortOrder: index }))
        planApi.reorder(reorderData).catch(console.error)
        return newItems
      })
    }
  }

  const handleAdd = () => {
    setEditingPlan(null)
    form.resetFields()
    setModalVisible(true)
  }

  const handleEdit = (record: PlanItem) => {
    setEditingPlan(record)
    form.setFieldsValue({
      ...record,
      startTime: record.startTime ? dayjs(record.startTime, 'HH:mm') : null,
      endTime: record.endTime ? dayjs(record.endTime, 'HH:mm') : null
    })
    setModalVisible(true)
  }

  const handleDelete = async (id: number) => {
    try {
      await planApi.delete(id)
      message.success('删除成功')
      fetchPlans()
    } catch (error) {
      console.error('删除失败:', error)
    }
  }

  const handleStatusChange = async (id: number, status: string) => {
    try {
      await planApi.updateStatus(id, status)
      message.success('状态更新成功')
      fetchPlans()
    } catch (error) {
      console.error('状态更新失败:', error)
    }
  }

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields()
      const data: CreatePlanRequest = {
        ...values,
        planDate: selectedDate.format('YYYY-MM-DD'),
        startTime: values.startTime ? values.startTime.format('HH:mm') : undefined,
        endTime: values.endTime ? values.endTime.format('HH:mm') : undefined
      }

      if (editingPlan) {
        await planApi.update(editingPlan.id, data)
        message.success('更新成功')
      } else {
        await planApi.create(data)
        message.success('创建成功')
      }

      setModalVisible(false)
      fetchPlans()
    } catch (error) {
      console.error('提交失败:', error)
    }
  }

  const columns: ColumnsType<PlanItem> = [
    {
      title: '任务',
      dataIndex: 'title',
      key: 'title',
      render: (text, record) => (
        <div>
          <div style={{ fontWeight: record.status === 'DONE' ? 'normal' : 'bold' }}>
            {text}
          </div>
          {record.description && (
            <div style={{ fontSize: 12, color: '#999' }}>{record.description}</div>
          )}
        </div>
      )
    },
    {
      title: '优先级',
      dataIndex: 'priority',
      key: 'priority',
      width: 100,
      render: (priority) => {
        const colors = {
          'P0': 'red',
          'P1': 'orange',
          'P2': 'blue',
          'P3': 'default'
        }
        return <Tag color={colors[priority as keyof typeof colors]}>{priority}</Tag>
      }
    },
    {
      title: '分类',
      dataIndex: 'category',
      key: 'category',
      width: 100,
      render: (category) => {
        const map = { 'WORK': '工作', 'STUDY': '学习', 'LIFE': '生活', 'HEALTH': '健康' }
        return map[category as keyof typeof map] || category
      }
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status, record) => (
        <Select
          value={status}
          size="small"
          style={{ width: 100 }}
          onChange={(value) => handleStatusChange(record.id, value)}
        >
          <Option value="TODO">待办</Option>
          <Option value="IN_PROGRESS">进行中</Option>
          <Option value="DONE">已完成</Option>
          <Option value="CANCELLED">已取消</Option>
        </Select>
      )
    },
    {
      title: '时间',
      key: 'time',
      width: 120,
      render: (_, record) => (
        <div>
          {record.startTime && <div>{record.startTime} - {record.endTime}</div>}
          {record.estimatedMins && <div style={{ fontSize: 12, color: '#999' }}>预计 {record.estimatedMins} 分钟</div>}
        </div>
      )
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

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h2>每日计划</h2>
        <Space>
          <DatePicker
            value={selectedDate}
            onChange={(date) => date && setSelectedDate(date)}
          />
          <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
            新建任务
          </Button>
        </Space>
      </div>

      {/* 统计卡片 */}
      {statistics && (
        <Row gutter={16} style={{ marginBottom: 16 }}>
          <Col span={6}>
            <Card>
              <Statistic title="总任务" value={statistics.total} />
            </Card>
          </Col>
          <Col span={6}>
            <Card>
              <Statistic
                title="已完成"
                value={statistics.done}
                valueStyle={{ color: '#3f8600' }}
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card>
              <Statistic
                title="进行中"
                value={statistics.inProgress}
                valueStyle={{ color: '#1890ff' }}
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card>
              <Statistic
                title="完成率"
                value={statistics.completionRate}
                suffix="%"
                precision={1}
              />
            </Card>
          </Col>
        </Row>
      )}

      <Table
        columns={columns}
        dataSource={plans}
        rowKey="id"
        loading={loading}
        pagination={false}
        components={{
          body: {
            row: SortableRow
          }
        }}
        rowClassName={(record) => record.status === 'DONE' ? 'completed-row' : ''}
      />

      <Modal
        title={editingPlan ? '编辑任务' : '新建任务'}
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => setModalVisible(false)}
        width={600}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="title"
            label="任务标题"
            rules={[{ required: true, message: '请输入任务标题' }]}
          >
            <Input placeholder="请输入任务标题" />
          </Form.Item>

          <Form.Item name="description" label="任务描述">
            <Input.TextArea rows={3} placeholder="请输入任务描述" />
          </Form.Item>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="priority" label="优先级" initialValue="P2">
                <Select>
                  <Option value="P0">P0 - 紧急重要</Option>
                  <Option value="P1">P1 - 重要不紧急</Option>
                  <Option value="P2">P2 - 紧急不重要</Option>
                  <Option value="P3">P3 - 不紧急不重要</Option>
                </Select>
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="category" label="分类" initialValue="WORK">
                <Select>
                  <Option value="WORK">工作</Option>
                  <Option value="STUDY">学习</Option>
                  <Option value="LIFE">生活</Option>
                  <Option value="HEALTH">健康</Option>
                </Select>
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={8}>
              <Form.Item name="startTime" label="开始时间">
                <DatePicker.TimePicker format="HH:mm" style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item name="endTime" label="结束时间">
                <DatePicker.TimePicker format="HH:mm" style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item name="estimatedMins" label="预估时长（分钟）">
                <Input type="number" />
              </Form.Item>
            </Col>
          </Row>
        </Form>
      </Modal>
    </div>
  )
}

export default PlanPage
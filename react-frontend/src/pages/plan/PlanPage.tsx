import React, { useState, useEffect } from 'react'
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons'
import { Table, Button, Modal, Form, Input, Select, DatePicker, Space, message, Popconfirm, Tag, Switch, Card, Row, Col, Statistic } from 'antd'
import type { ColumnsType } from 'antd/es/table'
import type { Dayjs } from 'dayjs'
import dayjs from 'dayjs'
import { planApi, type PlanItem, type CreatePlanRequest } from '@/api/plan'
// 导入 dnd-kit 拖拽库的核心组件
// - DndContext: 提供拖拽上下文环境的容器
// - closestCenter: 碰撞检测算法（以最近的中心点为准判断覆盖）
// - PointerSensor / KeyboardSensor: 鼠标/键盘传感器，识别拖拽动作
// - useSensors / useSensor: 实例化传感器的 hooks
import { DndContext, closestCenter, KeyboardSensor, PointerSensor, useSensor, useSensors } from '@dnd-kit/core'
// - arrayMove: 帮助快速移动数组中两项位置的纯函数
// - SortableContext: 提供排序上下文的容器，告知哪些元素可进行拖动排序
// - useSortable: 使某个 DOM 元素具备“可拖拽属性”的核心 hook
import { arrayMove, SortableContext, sortableKeyboardCoordinates, useSortable, verticalListSortingStrategy } from '@dnd-kit/sortable'
import { CSS } from '@dnd-kit/utilities'

const { Option } = Select

/**
 * ============================================================================
 * 【可拖动排序行组件 (SortableRow)】
 * ============================================================================
 *
 * 【知识点解析：0-1 学习 React】
 *
 * 1. 什么是拖拽传感器和可排序组件？
 *    - 在 HTML 表格中，`<tr>` 标签定义了一行。我们要让表格行支持拖拽，不能直接操纵 DOM，
 *      而是需要将其封装成一个 React 组件，并在内部使用 `@dnd-kit/sortable` 提供的 `useSortable` 钩子。
 *    - `useSortable({ id })`：
 *      - 接收唯一标识 `id`。
 *      - 返回一组属性：
 *        - `setNodeRef`：用于绑定到真实的 HTML DOM 节点上，通知 dnd-kit 拖拽区域在哪里。
 *        - `attributes` & `listeners`：包含拖拽触发的所有事件监听器（如 `onMouseDown`）和无障碍属性。
 *        - `transform` & `transition`：当前被拖拽行由于偏移产生的位置变换样式。
 *      - 我们通过 JSX 将这些属性解构展开到原生的 `<tr>` 上：`<tr style={style} {...attributes} {...listeners} {...props}>`。
 */
function SortableRow({ id, children, ...props }: any) {
  const { attributes, listeners, setNodeRef, transform, transition, isDragging } = useSortable({
    id
  })

  // 将偏移量（transform）转换成 CSS style 字符串，并应用平滑过渡（transition）
  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
    cursor: isDragging ? 'grabbing' : 'grab' // 拖拽中手势为“抓取”，普通状态为“小手”
  }

  return (
    <tr ref={setNodeRef} style={style} {...attributes} {...listeners} {...props}>
      {children}
    </tr>
  )
}

/**
 * ============================================================================
 * 【每日计划页面组件 (src/pages/plan/PlanPage.tsx)】
 * ============================================================================
 *
 * 【知识点解析：0-1 学习 React】
 *
 * 1. dnd-kit 的工作流 (Drag & Drop Flow)：
 *    - 传感器初始化：使用 `useSensors` 初始化 Pointer（鼠标/手指）和 Keyboard 传感器，并设置键盘移动的步长参数。
 *    - 容器配置：
 *      - 最外层包裹 `<DndContext>`，绑定 `onDragEnd` 事件。
 *      - 表格内部包裹 `<SortableContext>`，传入元素 ID 数组 `items={plans.map(p => p.id)}`。
 *    - 自定义表格行渲染：利用 Antd Table 的 `components` 属性，把底层的 `row` 渲染器替换为我们的 `SortableRow`。
 *    - 排序更新：当拖动结束时，触发 `handleDragEnd`。计算出旧序号（oldIndex）和新序号（newIndex），
 *      调用 `arrayMove(plans, oldIndex, newIndex)` 产生一个全新的计划数组。
 *
 * 2. React 的状态不可变性 (Immutability)：
 *    - 永远不要直接修改状态，例如 `plans[0] = newPlan; setPlans(plans)`。这不会触发组件重绘，
 *      因为 React 采用的是“浅层引用对比”，数组的内存地址没有变。
 *    - 必须使用纯函数方式或传入回调函数更新状态：
 *      `setPlans((items) => { const newItems = ...; return newItems; })`。
 *      通过返回一个新的数组对象，React 才能识别出状态发生改变，进而进行差异化 DOM 重建（Reconciliation）。
 */
const PlanPage: React.FC = () => {
  // 定义本地响应式数据状态
  const [loading, setLoading] = useState(false)
  const [plans, setPlans] = useState<PlanItem[]>([])
  // 日历选择状态，默认是当前天。使用 Dayjs 库处理日期时间
  const [selectedDate, setSelectedDate] = useState<Dayjs>(dayjs())
  const [modalVisible, setModalVisible] = useState(false)
  const [editingPlan, setEditingPlan] = useState<PlanItem | null>(null)
  const [form] = Form.useForm()
  const [statistics, setStatistics] = useState<any>(null)

  // 1. 配置拖拽感知传感器：启用鼠标拖拽和键盘辅助排序
  const sensors = useSensors(
    useSensor(PointerSensor),
    useSensor(KeyboardSensor, {
      coordinateGetter: sortableKeyboardCoordinates
    })
  )

  // 2. 声明生命周期副作用：每当用户点击日期选择器改变 selectedDate 时，重新加载计划数据和统计指标
  useEffect(() => {
    fetchPlans()
    fetchStatistics()
  }, [selectedDate])

  /**
   * 加载指定日期的计划任务列表
   */
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

  /**
   * 获取月度统计数据
   */
  const fetchStatistics = async () => {
    try {
      const startDate = selectedDate.startOf('month').format('YYYY-MM-DD')
      const endDate = selectedDate.endOf('month').format('YYYY-MM-DD')
      const data = await planApi.getStatistics(startDate, endDate)
      // 获取当前所选日期对应的那一天统计信息
      setStatistics(data[data.length - 1])
    } catch (error) {
      console.error('获取统计数据失败:', error)
    }
  }

  /**
   * 拖拽结束回调函数：处理物理位置变动后的状态重组及接口同步
   */
  const handleDragEnd = async (event: any) => {
    const { active, over } = event
    // active: 当前拖拽的元素节点。over: 被拖拽元素悬停其上的目标元素节点。
    if (active.id !== over?.id) {
      // 传入回调函数更新 state
      setPlans((items) => {
        const oldIndex = items.findIndex((item) => item.id === active.id)
        const newIndex = items.findIndex((item) => item.id === over?.id)
        // 通过 arrayMove 产生全新的数组对象，以遵守不可变数据原则
        const newItems = arrayMove(items, oldIndex, newIndex)
        
        // 整理排序数据并同步上报给后端数据库，保持前后端顺序的一致
        const reorderData = newItems.map((item, index) => ({ id: item.id, sortOrder: index }))
        planApi.reorder(reorderData).catch(console.error)
        
        return newItems // 返回全新数组以触发 UI 重绘
      })
    }
  }

  // 打开新建弹窗
  const handleAdd = () => {
    setEditingPlan(null)
    form.resetFields()
    setModalVisible(true)
  }

  // 打开编辑弹窗并回显数据
  const handleEdit = (record: PlanItem) => {
    setEditingPlan(record)
    // 使用 dayjs 转化时间字符串，以填充 Antd TimePicker 要求的 Dayjs 数据类型
    form.setFieldsValue({
      ...record,
      startTime: record.startTime ? dayjs(record.startTime, 'HH:mm') : null,
      endTime: record.endTime ? dayjs(record.endTime, 'HH:mm') : null
    })
    setModalVisible(true)
  }

  // 删除计划
  const handleDelete = async (id: number) => {
    try {
      await planApi.delete(id)
      message.success('删除成功')
      fetchPlans() // 刷新列表
    } catch (error) {
      console.error('删除失败:', error)
    }
  }

  // 在表格直接下拉修改任务状态
  const handleStatusChange = async (id: number, status: string) => {
    try {
      await planApi.updateStatus(id, status)
      message.success('状态更新成功')
      fetchPlans() // 刷新以展示最新状态
    } catch (error) {
      console.error('状态更新失败:', error)
    }
  }

  // 弹窗表单提交处理
  const handleSubmit = async () => {
    try {
      // 1. 手动触发表单验证，通过后获取表单所有数据
      const values = await form.validateFields()
      // 2. 格式化数据，转换为后端所需的数据结构
      const data: CreatePlanRequest = {
        ...values,
        planDate: selectedDate.format('YYYY-MM-DD'),
        // 将 dayjs 对象转换为 "HH:mm" 的字符串格式上报
        startTime: values.startTime ? values.startTime.format('HH:mm') : undefined,
        endTime: values.endTime ? values.endTime.format('HH:mm') : undefined
      }

      if (editingPlan) {
        // 调用修改 API
        await planApi.update(editingPlan.id, data)
        message.success('更新成功')
      } else {
        // 调用创建 API
        await planApi.create(data)
        message.success('创建成功')
      }

      setModalVisible(false) // 关闭 Modal
      fetchPlans()           // 刷新列表数据
    } catch (error) {
      console.error('提交失败:', error)
    }
  }

  // 定义表格的列配置
  const columns: ColumnsType<PlanItem> = [
    {
      title: '任务',
      dataIndex: 'title',
      key: 'title',
      // 自定义渲染内容：任务标题 + 任务描述
      render: (text, record) => (
        <div>
          {/* 若任务已完成，文字变细表示已归档 */}
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
      // 行内直接修改状态，不需要进弹窗，提升操作效率
      render: (status, record) => (
        <Select
          value={status}
          size="small"
          style={{ width: 100 }}
          // 点击表格中的 Select 直接阻止冒泡或调用更新 API
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
          {/* Popconfirm：防误触删除的确认冒泡框 */}
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
      {/* 顶部操作通栏 */}
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h2>每日计划</h2>
        <Space>
          <DatePicker
            value={selectedDate}
            onChange={(date) => date && setSelectedDate(date)} // 选择器变动时自动更新 selectedDate 状态
          />
          <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
            新建任务
          </Button>
        </Space>
      </div>

      {/* 统计指标区域 */}
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

      {/*
        3. 拖拽组件树包裹 (DndKit Integration)
        - DndContext：提供全局碰撞检测、传感器和坐标运算环境。
        - SortableContext：传入当前可拖拽排序的所有 ID 列表（必须是 string 或 number 数组）。
      */}
      <DndContext sensors={sensors} collisionDetection={closestCenter} onDragEnd={handleDragEnd}>
        <SortableContext items={plans.map(p => p.id)} strategy={verticalListSortingStrategy}>
          <Table
            columns={columns}
            dataSource={plans}
            rowKey="id"
            loading={loading}
            pagination={false}
            // 核心细节：利用 Table 的 components 配置项，定制渲染 Body Row
            components={{
              body: {
                // 将默认的 <tr> 替换为我们注入了 useSortable 钩子的可拖动行组件
                row: (props: any) => {
                  // props 包含了该行的对应记录 key，即计划数据的 id
                  return <SortableRow id={props['data-row-key']} {...props} />
                }
              }
            }}
            // 动态设置行样式类名：若任务已完成，加上变灰色或划线样式类
            rowClassName={(record) => record.status === 'DONE' ? 'completed-row' : ''}
          />
        </SortableContext>
      </DndContext>

      {/* 新建/编辑任务表单弹窗 */}
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
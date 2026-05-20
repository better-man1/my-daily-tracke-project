import React, { useState, useEffect } from 'react'
import { PlusOutlined, EditOutlined, DeleteOutlined, TrophyOutlined } from '@ant-design/icons'
import { Tree, Button, Modal, Form, Input, Select, DatePicker, Space, message, Popconfirm, Card, Row, Col, Statistic } from 'antd'
import type { DataNode } from 'antd/es/tree'
import dayjs from 'dayjs'
import { goalApi, type GoalItem, type CreateGoalRequest } from '@/api/goal'

const { TextArea } = Input
const { Option } = Select

/**
 * ============================================================================
 * 【目标管理页面组件 (src/pages/goal/GoalPage.tsx)】
 * ============================================================================
 *
 * 【知识点解析：0-1 学习 React】
 *
 * 1. 【React 核心概念：递归树形结构数据转换 (Recursive Tree Transformation)】
 *    - 场景：在目标管理中，目标通常具有“层级结构”（例如：五年目标 -> 年度目标 -> 月度目标 -> 周目标）。
 *    - 挑战：后端接口返回的是一个含有 `children` 数组的嵌套目标数据，而 Ant Design 的 `<Tree>` 组件要求传入特定的 `DataNode[]` 数据契约结构。
 *    - 解决方案：
 *      a. 定义递归转换函数 `convertToTreeData(goals)`。
 *      b. 函数内部遍历当前层级的每一个目标 `goal`：
 *         - 为其定义 `title`（可以是任意复杂的 React 元素，不仅是字符串）。
 *         - 设置唯一的 `key: goal.id`。
 *         - **递归逻辑**：如果该目标存在子孙节点 `goal.children`，则递归调用自身 `convertToTreeData(goal.children)` 并将其赋予 `children` 属性；否则设为 `undefined`。
 *      c. 这是在 React 中处理无限嵌套文件夹、菜单、汇报链时非常经典的数据转换模式。
 *
 * 2. 【React 核心概念：阻止树节点的事件冒泡 (Tree Node Bubbling Control)】
 *    - 场景：在点击树节点的“编辑”或“删除”按钮时，由于按钮被包裹在树节点的 DOM 节点内，浏览器默认会将点击事件向上传播，进而导致树节点**被选中或折叠收起**。
 *    - 实现：在按钮的点击事件回调中，调用 `e.stopPropagation()`（阻止事件冒泡），使事件局限在按钮本身，避免干扰树的正常交互。
 */
const GoalPage: React.FC = () => {
  // ============================================================================
  // // 状态（State）
  // ============================================================================
  const [loading, setLoading] = useState(false)
  const [goals, setGoals] = useState<GoalItem[]>([])
  const [modalVisible, setModalVisible] = useState(false)
  const [editingGoal, setEditingGoal] = useState<GoalItem | null>(null)
  const [form] = Form.useForm()
  // 总目标、已完成目标及整体进度等统计指标
  const [statistics, setStatistics] = useState<any>(null)

  // ============================================================================
  // // 生命周期 (useEffect)
  // ============================================================================
  useEffect(() => {
    fetchGoals()
    fetchStatistics()
  }, [])

  // ============================================================================
  // // 数据加载方法
  // ============================================================================
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

  /**
   * 递归数据转化方法：将 GoalItem[] 转换为 Antd Tree 所需的 DataNode[]
   */
  const convertToTreeData = (goalsList: GoalItem[]): DataNode[] => {
    return goalsList.map(goal => ({
      // title 传入 React 元素：渲染目标标题 + 进度百分比标签 + 动作按钮
      title: (
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', minWidth: 250 }}>
          <span>
            <span style={{ marginRight: 8, fontWeight: goal.status === 'COMPLETED' ? 'normal' : 'bold' }}>
              {goal.title}
            </span>
            <span style={{ fontSize: 12, color: '#999' }}>
              {goal.status === 'COMPLETED' && '✓ '}
              {goal.progress}%
            </span>
          </span>
          {/* 操作区：带冒泡阻止保护 */}
          <Space size="small" style={{ marginLeft: 16 }}>
            <Button
              type="link"
              size="small"
              icon={<EditOutlined />}
              onClick={(e) => {
                // 阻止事件向树节点 DOM 冒泡传播，防误触折叠
                e.stopPropagation()
                handleEdit(goal)
              }}
            />
            <Popconfirm
              title="确定删除此目标及其子目标？"
              onConfirm={(e) => {
                e?.stopPropagation() // 确定时阻止冒泡
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
                onClick={(e) => e.stopPropagation()} // 点击删除图标弹出确认框时阻止冒泡
              />
            </Popconfirm>
          </Space>
        </div>
      ),
      key: goal.id,
      // 核心细节：若有 children 数组，继续向下递归转换，否则设为 undefined
      children: goal.children ? convertToTreeData(goal.children) : undefined
    }))
  }

  // ============================================================================
  // // 交互处理
  // ============================================================================
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
      {/* 头部标题控制栏 */}
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h2>目标管理</h2>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => handleAdd()}>
          新建目标
        </Button>
      </div>

      {/* 统计图表面板 */}
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
                title="整体平均进度"
                value={statistics.overallProgress}
                suffix="%"
                precision={1}
              />
            </Card>
          </Col>
        </Row>
      )}

      {/* 目标树形卡片展示区 */}
      <Card title="目标树" bordered={false} loading={loading}>
        {/* Antd Tree 树形图组件，通过 treeData 绑定递归生成的数据结构 */}
        <Tree
          treeData={convertToTreeData(goals)}
          showLine            // 开启连线示意图，方便查看父子归属
          defaultExpandAll    // 默认展开所有分支层级
        />
      </Card>

      {/* 新增/编辑目标 Modal */}
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

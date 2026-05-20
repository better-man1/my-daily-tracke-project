import React, { useState, useEffect } from 'react'
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons'
import { Table, Button, Modal, Form, Input, Select, DatePicker, Space, message, Popconfirm, Card, Row, Col, Statistic } from 'antd'
import type { ColumnsType } from 'antd/es/table'
import type { Dayjs } from 'dayjs'
import dayjs from 'dayjs'
import { accountingApi, type AccountingItem, type CreateAccountingRequest } from '@/api/accounting'

const { Option } = Select

/**
 * ============================================================================
 * 【每日记账页面组件 (src/pages/accounting/AccountingPage.tsx)】
 * ============================================================================
 *
 * 【知识点解析：0-1 学习 React】
 *
 * 1. 【React 核心概念：多依赖项的组合副作用监听】
 *    - 场景：在记账管理中，用户需要按照“月份时间维度”和“账目类型（收入/支出/全部）”进行双重条件过滤。
 *    - 传统实现：需要分别监听月份 DatePicker 和分类 Select 的变动事件，并在各自的回调里组合两个筛选参数发起查询。
 *    - React 声明式写法：
 *      - 维护 `selectedMonth`（月份状态）和 `typeFilter`（类型筛选状态）。
 *      - 绑定副作用：`}, [selectedMonth, typeFilter])`。
 *      - **逻辑表现**：只要“月份”或“过滤类型”二者有任何一方发生变化，React 都会无缝捕获这一变动，
 *        并自动聚合当前最新的状态发起 API 请求。这能避免由于手动逻辑遗漏导致的前后端数据不一致。
 *
 * 2. 【React 核心概念：基于数据的条件样式渲染 (Row-Value Based Styling)】
 *    - 在财务明细表格中，我们希望“收入显示绿色且带 + 号，支出显示红色且带 - 号”。
 *    - 在 Table 的 `columns` 定义中，我们为列配置自定义 `render` 函数：
 *      `render: (amount, record) => <span style={{ color: record.type === 'INCOME' ? '#52c41a' : '#ff4d4f' }}>...</span>`
 *    - `render` 函数接收当前单元格的值（amount）以及这一行的完整数据对象（record）。
 *    - 我们可以使用三元表达式 `record.type === 'INCOME' ? 'A' : 'B'` 来动态输出相应的 CSS 颜色和前缀符号。
 *
 * 3. 页面多数据源加载设计：
 *    - 当月份改变时，需要重新请求三份完全不同的数据：明细列表（`fetchRecords`）、可用分类（`fetchCategories`）和月度统计结余（`fetchStatistics`）。
 *    - 在 `useEffect` 回调中依次同步调用，React 会将这些请求并发处理，并根据结果独立刷新各个状态块（表格、看板）。
 */
const AccountingPage: React.FC = () => {
  // ============================================================================
  // // 状态（State）
  // ============================================================================
  const [loading, setLoading] = useState(false)
  const [records, setRecords] = useState<AccountingItem[]>([])
  const [modalVisible, setModalVisible] = useState(false)
  const [editingRecord, setEditingRecord] = useState<AccountingItem | null>(null)
  
  // 选中的月份状态，默认为当前月份 (dayjs() 获取当前月份)
  const [selectedMonth, setSelectedMonth] = useState<Dayjs>(dayjs())
  // 所有的记账分类数据库列表（如：餐饮、工资、人情）
  const [categories, setCategories] = useState<string[]>([])
  // 本月收支合计、结余等统计卡片数据
  const [statistics, setStatistics] = useState<any>(null)
  
  // 创建新增/编辑明细的表单实例
  const [form] = Form.useForm()
  // 过滤类型：INCOME（收入）、EXPENSE（支出）、undefined（全部）
  const [typeFilter, setTypeFilter] = useState<'INCOME' | 'EXPENSE' | undefined>(undefined)

  // ============================================================================
  // // 副作用处理 (useEffect)
  // ============================================================================
  
  // 监听 [selectedMonth, typeFilter]，任何筛选条件改变，都会自动刷新本月账单
  useEffect(() => {
    fetchRecords()
    fetchCategories()
    fetchStatistics()
  }, [selectedMonth, typeFilter])

  // ============================================================================
  // // 数据加载方法
  // ============================================================================
  
  /**
   * 获取过滤时间段内的记账明细列表
   */
  const fetchRecords = async () => {
    try {
      setLoading(true)
      // 计算所选月份的开头第一天和最后一天字符串 (YYYY-MM-DD)
      const startDate = selectedMonth.startOf('month').format('YYYY-MM-DD')
      const endDate = selectedMonth.endOf('month').format('YYYY-MM-DD')
      
      const data = await accountingApi.list({
        startDate,
        endDate,
        type: typeFilter
      })
      setRecords(data.list)
    } catch (error) {
      console.error('获取记账记录失败:', error)
    } finally {
      setLoading(false)
    }
  }

  /**
   * 获取所有已被定义过的分类，方便在表单中进行快捷下拉搜索
   */
  const fetchCategories = async () => {
    try {
      const data = await accountingApi.getCategories()
      setCategories(data)
    } catch (error) {
      console.error('获取分类失败:', error)
    }
  }

  /**
   * 汇总月度总收入、总支出和结余指标
   */
  const fetchStatistics = async () => {
    try {
      const year = selectedMonth.year()
      // dayjs 月份从 0 开始，故必须 +1 修正为 1-12 月
      const month = selectedMonth.month() + 1
      const data = await accountingApi.getMonthlyStatistics(year, month)
      setStatistics(data)
    } catch (error) {
      console.error('获取统计数据失败:', error)
    }
  }

  // ============================================================================
  // // 交互行为
  // ============================================================================
  const handleAdd = () => {
    setEditingRecord(null)
    form.resetFields()
    setModalVisible(true)
  }

  const handleEdit = (record: AccountingItem) => {
    setEditingRecord(record)
    // 接口返回的 record.date 是 string，在表单中回显必须包装为 DatePicker 要求的 Dayjs 对象
    form.setFieldsValue({
      ...record,
      date: dayjs(record.date)
    })
    setModalVisible(true)
  }

  const handleDelete = async (id: number) => {
    try {
      await accountingApi.delete(id)
      message.success('删除成功')
      fetchRecords() // 刷新列表
      fetchStatistics() // 刷新统计卡片
    } catch (error) {
      console.error('删除失败:', error)
    }
  }

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields()
      const data: CreateAccountingRequest = {
        ...values,
        // 将 Dayjs 的日期对象重新转化为 YYYY-MM-DD 格式的字符串提交
        date: values.date.format('YYYY-MM-DD')
      }

      if (editingRecord) {
        await accountingApi.update(editingRecord.id, data)
        message.success('更新成功')
      } else {
        await accountingApi.create(data)
        message.success('创建成功')
      }

      setModalVisible(false)
      fetchRecords()
      fetchStatistics()
    } catch (error) {
      console.error('提交失败:', error)
    }
  }

  // ============================================================================
  // // 表格列配置
  // ============================================================================
  const columns: ColumnsType<AccountingItem> = [
    {
      title: '日期',
      dataIndex: 'date',
      key: 'date',
      width: 120
    },
    {
      title: '类型',
      dataIndex: 'type',
      key: 'type',
      width: 100,
      render: (type) => (
        // 基于账目类型，动态调整渲染颜色
        <span style={{ color: type === 'INCOME' ? '#52c41a' : '#ff4d4f' }}>
          {type === 'INCOME' ? '收入' : '支出'}
        </span>
      )
    },
    {
      title: '分类',
      dataIndex: 'category',
      key: 'category',
      width: 120
    },
    {
      title: '金额',
      dataIndex: 'amount',
      key: 'amount',
      width: 120,
      render: (amount, record) => (
        // 渲染成带 + - 号前缀且加粗的格式
        <span style={{ color: record.type === 'INCOME' ? '#52c41a' : '#ff4d4f', fontWeight: 'bold' }}>
          {record.type === 'INCOME' ? '+' : '-'}{amount.toFixed(2)}
        </span>
      )
    },
    {
      title: '描述',
      dataIndex: 'description',
      key: 'description',
      ellipsis: true // 开启溢出自动省略号，优化表格空间排布
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
      {/* 筛选及控制区 */}
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h2>每日记账</h2>
        <Space>
          {/* 月份选择器 DatePicker.MonthPicker */}
          <DatePicker.MonthPicker
            value={selectedMonth}
            onChange={(date) => date && setSelectedMonth(date)}
          />
          {/* 收支类型过滤器 */}
          <Select
            value={typeFilter}
            onChange={setTypeFilter}
            style={{ width: 120 }}
            allowClear // 允许点击右侧 x 清空选择
            placeholder="全部类型"
          >
            <Option value="INCOME">收入</Option>
            <Option value="EXPENSE">支出</Option>
          </Select>
          <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
            新建记录
          </Button>
        </Space>
      </div>

      {/* 看板数据渲染 */}
      {statistics && (
        <Row gutter={16} style={{ marginBottom: 16 }}>
          <Col span={8}>
            <Card>
              <Statistic
                title="总收入"
                value={statistics.totalIncome}
                precision={2}
                prefix="+"
                suffix="元"
                valueStyle={{ color: '#3f8600' }}
              />
            </Card>
          </Col>
          <Col span={8}>
            <Card>
              <Statistic
                title="总支出"
                value={statistics.totalExpense}
                precision={2}
                prefix="-"
                suffix="元"
                valueStyle={{ color: '#cf1322' }}
              />
            </Card>
          </Col>
          <Col span={8}>
            <Card>
              {/* 月度最终结余：根据结余正负判定并显示红绿主色调 */}
              <Card>
                <Statistic
                  title="结余"
                  value={statistics.balance}
                  precision={2}
                  suffix="元"
                  valueStyle={{
                    color: statistics.balance >= 0 ? '#3f8600' : '#cf1322'
                  }}
                />
              </Card>
            </Card>
          </Col>
        </Row>
      )}

      {/* 数据明细列表表格 */}
      <Table
        columns={columns}
        dataSource={records}
        rowKey="id"
        loading={loading}
        pagination={{
          pageSize: 20,
          showSizeChanger: true,
          showTotal: (total) => `共 ${total} 条`
        }}
      />

      {/* 编辑/新建明细弹窗 */}
      <Modal
        title={editingRecord ? '编辑记录' : '新建记录'}
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => setModalVisible(false)}
        width={600}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="type"
            label="类型"
            rules={[{ required: true, message: '请选择类型' }]}
          >
            <Select>
              <Option value="INCOME">收入</Option>
              <Option value="EXPENSE">支出</Option>
            </Select>
          </Form.Item>

          <Form.Item
            name="amount"
            label="金额"
            rules={[{ required: true, message: '请输入金额' }]}
          >
            <Input type="number" step="0.01" placeholder="请输入金额" />
          </Form.Item>

          <Form.Item
            name="category"
            label="分类"
            rules={[{ required: true, message: '请选择分类' }]}
          >
            {/*
              带搜索功能的 Select 下拉框
              showSearch：允许用户直接在下拉框里打字模糊筛选
              optionFilterProp="children"：使用 Option 内部包裹的文字做过滤依据
            */}
            <Select
              showSearch
              placeholder="请选择分类"
              optionFilterProp="children"
            >
              {categories.map(cat => (
                <Option key={cat} value={cat}>{cat}</Option>
              ))}
            </Select>
          </Form.Item>

          <Form.Item
            name="date"
            label="日期"
            rules={[{ required: true, message: '请选择日期' }]}
            initialValue={dayjs()}
          >
            <DatePicker style={{ width: '100%' }} />
          </Form.Item>

          <Form.Item name="description" label="描述">
            <Input.TextArea rows={3} placeholder="请输入描述" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default AccountingPage